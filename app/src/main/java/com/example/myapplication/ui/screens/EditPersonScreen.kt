package com.example.myapplication.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.Person
import com.example.myapplication.ui.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPersonScreen(
    viewModel: MainViewModel,
    personId: Int?,
    onNavigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
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
        if (personId != null && personId != -1) {
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
                title = { Text(if (personId == null || personId == -1) "Add Person" else "Edit Person") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
            OutlinedTextField(value = preferredGymId, onValueChange = { preferredGymId = it }, label = { Text("Preferred Gym ID") }, modifier = Modifier.fillMaxWidth())

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
