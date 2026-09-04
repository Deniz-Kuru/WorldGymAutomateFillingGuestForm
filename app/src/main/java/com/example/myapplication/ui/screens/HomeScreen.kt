package com.example.myapplication.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.Person
import com.example.myapplication.ui.MainViewModel
import com.example.myapplication.ui.components.OCRScanner

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onAddPerson: () -> Unit,
    onEditPerson: (Int) -> Unit,
    onAutomate: (Int, String, String) -> Unit,
    onSettings: () -> Unit,
) {
    val persons by viewModel.persons.collectAsState()
    var showAutomationDialog by remember { mutableStateOf(value = false) }
    var selectedPersonId by remember { mutableIntStateOf(-1) }
    var dailyPassCode by remember { mutableStateOf("") }
    var visitType by remember { mutableStateOf("VIP Guest") }
    var showOCRScanner by remember { mutableStateOf(false) }

    if (showOCRScanner) {
        OCRScanner(
            onTextDetected = { 
                dailyPassCode = it
                showOCRScanner = false
            },
            onDismiss = { showOCRScanner = false }
        )
    }

    if (showAutomationDialog) {
        AlertDialog(
            onDismissRequest = { showAutomationDialog = false },
            title = { Text("Automation Details") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = dailyPassCode,
                        onValueChange = { dailyPassCode = it.uppercase() },
                        label = { Text("Daily Pass Code") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    
                    VisitTypeDropdown(
                        selectedType = visitType,
                    ) {
                        visitType = it
                    }
                }
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { showOCRScanner = true }) {
                        Icon(
                            Icons.Default.PhotoCamera,
                            contentDescription = "Scan Passcode",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    Spacer(modifier = Modifier.weight(1f))

                    Button(
                        onClick = {
                            Log.d("HomeScreen", "Start button clicked. personId: $selectedPersonId, code: $dailyPassCode, type: $visitType")
                            showAutomationDialog = false
                            onAutomate(selectedPersonId, dailyPassCode, visitType)
                        },
                    ) {
                        Text("Start")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        Log.d("HomeScreen", "Cancel button clicked")
                        showAutomationDialog = false 
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                ) {
                    Text("Cancel")
                }
            },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Non-official gym Guest form automation") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
                actions = {
                    IconButton(onClick = onSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddPerson,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Person")
            }
        },
    ) { padding ->
        if (persons.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "No profiles found",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Text(
                        text = "Tap + to add a guest profile",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(persons) { person ->
                    PersonItem(
                        person = person,
                        onEdit = { onEditPerson(person.id) },
                    ) {
                        selectedPersonId = person.id
                        showAutomationDialog = true
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisitTypeDropdown(
    selectedType: String,
    onTypeSelected: (String) -> Unit,
) {
    val types = listOf("Free Trial", "VIP Guest")
    var expanded by remember { mutableStateOf(value = false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth(),
    ) {
        OutlinedTextField(
            value = selectedType,
            onValueChange = {},
            readOnly = true,
            label = { Text("Visit Type") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            types.forEach { type ->
                DropdownMenuItem(
                    text = { Text(type) },
                    onClick = {
                        onTypeSelected(type)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }
        }
    }
}

@Composable
fun PersonItem(
    person: Person,
    onEdit: () -> Unit,
    onAutomate: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
        ) {
            Text(text = "${person.firstName} ${person.lastName}", style = MaterialTheme.typography.titleLarge)
            Text(text = person.email, style = MaterialTheme.typography.bodyMedium)
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(onClick = onEdit) {
                    Text("Edit")
                }
                Button(onClick = onAutomate) {
                    Text("Fill the Form")
                }
            }
        }
    }
}
