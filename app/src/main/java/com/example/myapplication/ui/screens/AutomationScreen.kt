package com.example.myapplication.ui.screens

import android.annotation.SuppressLint
import android.util.Log
import android.view.ViewGroup
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.example.myapplication.data.Person
import com.example.myapplication.ui.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutomationScreen(
    viewModel: MainViewModel,
    personId: Int,
    dailyPassCode: String,
    visitType: String,
    onNavigateBack: () -> Unit
) {
    var person by remember { mutableStateOf<Person?>(null) }
    val vipCardNumber by viewModel.vipCardNumber.collectAsState()

    LaunchedEffect(personId) {
        Log.d("AutomationScreen", "Launched for personId: $personId")
        person = viewModel.getPersonById(personId)
        if (person == null) {
            Log.e("AutomationScreen", "Person not found for id: $personId")
        } else {
            Log.d("AutomationScreen", "Person fetched: ${person?.profileName}")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registration") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (person != null) {
                AutomationWebView(
                    person = person!!,
                    dailyPassCode = dailyPassCode,
                    visitType = visitType,
                    vipCardNumber = vipCardNumber,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun AutomationWebView(
    person: Person,
    dailyPassCode: String,
    visitType: String,
    vipCardNumber: String,
    modifier: Modifier = Modifier
) {
    val url = "https://www.ggpx.info/GuestReg"
    var step by remember { mutableIntStateOf(1) }
    var isLoading by remember { mutableStateOf(true) }

    Box(modifier = modifier) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                Log.d("AutomationWebView", "Creating WebView")
                WebView(context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    setBackgroundColor(android.graphics.Color.WHITE)
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.loadWithOverviewMode = true
                    settings.useWideViewPort = true

                    webChromeClient = object : WebChromeClient() {
                        override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                            consoleMessage?.let {
                                Log.d("AutomationWebView JS", "${it.message()}")
                            }
                            return true
                        }
                    }

                    webViewClient = object : WebViewClient() {
                        override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                            super.onPageStarted(view, url, favicon)
                            Log.d("AutomationWebView", "onPageStarted: $url")
                            isLoading = true
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            Log.d("AutomationWebView", "onPageFinished: $url, Step: $step")
                            isLoading = false

                            if (url?.lowercase()?.contains("guestreg") == true) {
                                if (step == 1) {
                                    Log.d("AutomationWebView", "Injecting Step 1 JS")
                                    val safeGymId = person.preferredGymId.replace("'", "\\'")
                                    val js = """
                                        (function() {
                                            console.log('Step 1: Gym Selection Starting');
                                            var gymDropdown = document.getElementById('GymID');
                                            var gymId = '$safeGymId';
                                            if (gymDropdown && gymId) {
                                                gymDropdown.value = gymId;
                                                gymDropdown.dispatchEvent(new Event('change', { bubbles: true }));
                                                var btnSubmitGym = document.getElementById('btnSubmitGym');
                                                if (btnSubmitGym) {
                                                    btnSubmitGym.click();
                                                }
                                            }
                                        })();
                                    """.trimIndent()

                                    step = 2
                                    view?.evaluateJavascript(js, null)
                                } else if (step == 2) {
                                    Log.d("AutomationWebView", "Injecting Step 2 JS")

                                    val passType = if (visitType == "Free Trial") "GPC" else "GVM"
                                    val promoCode = if (passType == "GVM") vipCardNumber else ""

                                    // Inject direct synchronous DOM population
                                    val js = """
                                        (function() {
                                            console.log('Step 2: Main Form Filling');
                                            
                                            function fill(id, val) {
                                                var el = document.getElementById(id);
                                                if (el && val) {
                                                    el.value = val;
                                                    el.dispatchEvent(new Event('input', { bubbles: true }));
                                                    el.dispatchEvent(new Event('change', { bubbles: true }));
                                                }
                                            }
                                            
                                            function check(id) {
                                                var el = document.getElementById(id);
                                                if (el) el.checked = true;
                                            }

                                            fill('RegCode', '${dailyPassCode.replace("'", "\\'")}');
                                            fill('FirstName', '${person.firstName.replace("'", "\\'")}');
                                            fill('LastName', '${person.lastName.replace("'", "\\'")}');
                                            fill('Email', '${person.email.replace("'", "\\'")}');
                                            fill('YearOfBirth', '${if (person.yearOfBirth > 0) person.yearOfBirth.toString() else ""}');
                                            fill('StreetAddress', '${person.address.replace("'", "\\'")}');
                                            fill('Appartment', '${person.apartment.replace("'", "\\'")}');
                                            fill('City', '${person.city.replace("'", "\\'")}');
                                            fill('StateProv', '${person.stateProv.replace("'", "\\'")}');
                                            fill('PostalCode', '${person.postalCode.replace("'", "\\'")}');
                                            fill('PhoneMobile', '${person.phone.replace("'", "\\'")}');
                                            
                                            if ('${person.gender}' === 'M') check('GenderM');
                                            else if ('${person.gender}' === 'F') check('GenderF');
                                            
                                            fill('GuestPassType', '$passType');
                                            if ('$promoCode') fill('PromoCode', '$promoCode');
                                            
                                            check('GuestServicesAgreement1');
                                            check('GuestServicesAgreement2');
                                            check('GuestServicesAgreement3');
                                            check('Agreement');
                                            
                                            var btnSubmit = document.getElementById('btnSubmit');
                                            if (btnSubmit) btnSubmit.scrollIntoView();
                                        })();
                                    """.trimIndent()

                                    view?.evaluateJavascript(js, null)
                                }
                            }
                        }
                    }
                    loadUrl(url)
                }
            },
            update = { /* Keep empty to prevent re-triggering url loads during recomposition */ }
        )

        if (isLoading) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(androidx.compose.ui.Alignment.TopCenter)
            )
        }
    }
}
