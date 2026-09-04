package com.example.myapplication.ui.components

import android.Manifest
import android.util.Log
import android.view.ViewGroup
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun OCRScanner(
    onTextDetected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        val permissionState = rememberPermissionState(permission = Manifest.permission.CAMERA)
        var variations by remember { mutableStateOf<List<String>>(emptyList()) }
        var selectedIndex by remember { mutableIntStateOf(0) }

        LaunchedEffect(Unit) {
            if (!permissionState.status.isGranted) {
                permissionState.launchPermissionRequest()
            }
        }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Black
        ) {
            if (permissionState.status.isGranted) {
                Box(modifier = Modifier.fillMaxSize()) {
                    CameraPreview(onTextDetected = { text ->
                        if (!variations.contains(text)) {
                            variations = (listOf(text) + variations).take(2)
                            selectedIndex = 0
                        }
                    })
                    
                    // Overlay with rectangular box
                    OCRPreviewOverlay()

                    // Detected variations selection
                    if (variations.isNotEmpty()) {
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 64.dp)
                                .padding(horizontal = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                "Tap the correct code to use it:",
                                color = Color.White,
                                style = MaterialTheme.typography.labelLarge,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            
                            variations.forEach { text ->
                                Card(
                                    onClick = { onTextDetected(text) },
                                    modifier = Modifier.fillMaxWidth(0.8f),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color.Black.copy(alpha = 0.8f),
                                        contentColor = Color.White
                                    ),
                                    border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                                ) {
                                    Text(
                                        text = text,
                                        modifier = Modifier
                                            .padding(16.dp)
                                            .align(Alignment.CenterHorizontally),
                                        style = MaterialTheme.typography.headlineMedium
                                    )
                                }
                            }
                        }
                    }

                    // Close button
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .padding(16.dp)
                            .align(Alignment.TopEnd)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Camera permission is required for OCR", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun CameraPreview(onTextDetected: (String) -> Unit) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }
    val recognizer = remember { TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS) }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { context ->
                val previewView = PreviewView(context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                }

                val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()

                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    val imageAnalyzer = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .also {
                            it.setAnalyzer(cameraExecutor) { imageProxy ->
                                processImageProxy(
                                    recognizer,
                                    imageProxy,
                                    onTextDetected
                                )
                            }
                        }

                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            imageAnalyzer
                        )
                    } catch (e: Exception) {
                        Log.e("OCRScanner", "Use case binding failed", e)
                    }
                }, ContextCompat.getMainExecutor(context))

                previewView
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}

private fun processImageProxy(
    recognizer: com.google.mlkit.vision.text.TextRecognizer,
    imageProxy: ImageProxy,
    onTextDetected: (String) -> Unit
) {
    @Suppress("UnsafeOptInUsageError")
    val mediaImage = imageProxy.image
    if (mediaImage != null) {
        val rotationDegrees = imageProxy.imageInfo.rotationDegrees
        val image = InputImage.fromMediaImage(mediaImage, rotationDegrees)
        
        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                // The image dimensions depend on rotation
                val imageHeight = if (rotationDegrees == 90 || rotationDegrees == 270) image.width else image.height

                // We want to find text in the middle 20% of the image height (matching our UI box)
                val scanTopLimit = imageHeight * 0.4f
                val scanBottomLimit = imageHeight * 0.6f

                val lines = visionText.textBlocks.flatMap { it.lines }
                
                // Find a line that is within the middle horizontal strip of the camera feed
                val validLine = lines.find { line ->
                    val boundingBox = line.boundingBox
                    if (boundingBox != null) {
                        val centerY = boundingBox.centerY().toFloat()
                        centerY in scanTopLimit..scanBottomLimit
                    } else {
                        false
                    }
                }

                val detectedText = validLine?.text?.trim()?.uppercase() ?: ""
                
                if (detectedText.isNotEmpty()) {
                    onTextDetected(detectedText)
                }
            }
            .addOnFailureListener { e ->
                Log.e("OCRScanner", "Text recognition failed", e)
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    } else {
        imageProxy.close()
    }
}

@Composable
fun OCRPreviewOverlay() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        
        // Define the rectangular box area (middle of screen)
        val rectWidth = canvasWidth * 0.8f
        val rectHeight = 100.dp.toPx()
        val left = (canvasWidth - rectWidth) / 2
        val top = (canvasHeight - rectHeight) / 2
        
        val scanRect = Rect(Offset(left, top), Size(rectWidth, rectHeight))

        // Draw darkened background with a hole
        drawRect(
            color = Color.Black.copy(alpha = 0.5f),
            size = size
        )
        
        // Use BlendMode.Clear to make a transparent hole
        drawRoundRect(
            color = Color.Transparent,
            topLeft = scanRect.topLeft,
            size = scanRect.size,
            cornerRadius = CornerRadius(8.dp.toPx()),
            blendMode = BlendMode.Clear
        )

        // Draw box border
        drawRoundRect(
            color = Color.White,
            topLeft = scanRect.topLeft,
            size = scanRect.size,
            cornerRadius = CornerRadius(8.dp.toPx()),
            style = Stroke(width = 2.dp.toPx())
        )
    }
}
