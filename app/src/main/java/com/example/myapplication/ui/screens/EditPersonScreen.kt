package com.example.myapplication.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.Person
import com.example.myapplication.ui.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPersonScreen(
    viewModel: MainViewModel,
    personId: Int?,
    onNavigateBack: () -> Unit,
) {
    var profileName by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var yearOfBirth by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("M") }
    var address by remember { mutableStateOf("") }
    var apartment by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var stateProv by remember { mutableStateOf("") }
    var postalCode by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var preferredGymId by remember { mutableStateOf("") }

    LaunchedEffect(personId) {
        if ((personId != null) && (personId != -1)) {
            val person = viewModel.getPersonById(personId)
            person?.let {
                profileName = it.profileName
                firstName = it.firstName
                lastName = it.lastName
                email = it.email
                yearOfBirth = it.yearOfBirth.toString()
                gender = it.gender
                address = it.address
                apartment = it.apartment
                city = it.city
                stateProv = it.stateProv
                postalCode = it.postalCode
                phone = it.phone
                preferredGymId = it.preferredGymId
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if ((personId == null) || (personId == -1)) "Add Person" else "Edit Person") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(value = profileName, onValueChange = { profileName = it }, label = { Text("Profile Name") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = firstName, onValueChange = { firstName = it }, label = { Text("First Name") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = lastName, onValueChange = { lastName = it }, label = { Text("Last Name") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email))
            OutlinedTextField(value = yearOfBirth, onValueChange = { yearOfBirth = it }, label = { Text("Year of Birth") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            
            Row(modifier = Modifier.fillMaxWidth()) {
                RadioButton(selected = gender == "M", onClick = { gender = "M" })
                Text("Male", modifier = Modifier.padding(top = 12.dp))
                Spacer(modifier = Modifier.width(16.dp))
                RadioButton(selected = gender == "F", onClick = { gender = "F" })
                Text("Female", modifier = Modifier.padding(top = 12.dp))
            }

            OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Address") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = apartment, onValueChange = { apartment = it }, label = { Text("Apartment") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = city, onValueChange = { city = it }, label = { Text("City") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = stateProv, onValueChange = { stateProv = it }, label = { Text("State/Province") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = postalCode, onValueChange = { postalCode = it }, label = { Text("Postal Code") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
            
            GymDropdown(
                selectedGymId = preferredGymId,
            ) {
                preferredGymId = it
            }

            Button(
                onClick = {
                    val person = Person(
                        id = if (personId == null || personId == -1) 0 else personId,
                        profileName = profileName,
                        firstName = firstName,
                        lastName = lastName,
                        email = email,
                        yearOfBirth = yearOfBirth.toIntOrNull() ?: 0,
                        gender = gender,
                        address = address,
                        apartment = apartment,
                        city = city,
                        stateProv = stateProv,
                        postalCode = postalCode,
                        phone = phone,
                        preferredGymId = preferredGymId
                    )
                    viewModel.savePerson(person)
                    onNavigateBack()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save")
            }

            if (personId != null && personId != -1) {
                TextButton(
                    onClick = {
                        val person = Person(
                            id = personId,
                            profileName = profileName,
                            firstName = firstName,
                            lastName = lastName,
                            email = email,
                            yearOfBirth = yearOfBirth.toIntOrNull() ?: 0,
                            gender = gender,
                            address = address,
                            apartment = apartment,
                            city = city,
                            stateProv = stateProv,
                            postalCode = postalCode,
                            phone = phone,
                            preferredGymId = preferredGymId
                        )
                        viewModel.deletePerson(person)
                        onNavigateBack()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GymDropdown(
    selectedGymId: String,
    onGymSelected: (String) -> Unit,
) {
    val gyms = listOf(
        "1" to "Gatineau",
        "2" to "Montréal (Plateau)",
        "3" to "Québec (Lebourgneuf)",
        "4" to "Québec (Ste-Foy)",
        "5" to "Sherbrooke",
        "6" to "Trois-Rivières",
        "7" to "Beauceville",
        "8" to "Brossard",
        "9" to "Chicoutimi",
        "10" to "Drummondville",
        "11" to "Joliette",
        "12" to "Laval",
        "13" to "Saint-Jérôme"
    )

    var expanded by remember { mutableStateOf(value = false) }
    val selectedGymName = gyms.find { it.first == selectedGymId }?.second ?: ""

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedGymName,
            onValueChange = {},
            readOnly = true,
            label = { Text("Preferred Gym") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            gyms.forEach { gym ->
                DropdownMenuItem(
                    text = { Text(gym.second) },
                    onClick = {
                        onGymSelected(gym.first)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}
