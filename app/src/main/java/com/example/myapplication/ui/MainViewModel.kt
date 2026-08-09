package com.example.myapplication.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.Person
import com.example.myapplication.data.PersonRepository
import com.example.myapplication.data.UserPreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(
    private val personRepository: PersonRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val persons: StateFlow<List<Person>> = personRepository.getAllPersons()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val vipCardNumber: StateFlow<String> = userPreferencesRepository.vipCardNumber
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    fun savePerson(person: Person) {
        viewModelScope.launch {
            if (person.id == 0) {
                personRepository.insertPerson(person)
            } else {
                personRepository.updatePerson(person)
            }
        }
    }

    fun deletePerson(person: Person) {
        viewModelScope.launch {
            personRepository.deletePerson(person)
        }
    }

    fun saveVipCardNumber(cardNumber: String) {
        viewModelScope.launch {
            userPreferencesRepository.saveVipCardNumber(cardNumber)
        }
    }

    suspend fun getPersonById(id: Int): Person? {
        return personRepository.getPersonById(id)
    }
}

class MainViewModelFactory(
    private val personRepository: PersonRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(personRepository, userPreferencesRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
