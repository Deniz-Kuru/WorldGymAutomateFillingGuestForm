package com.example.myapplication.data

import kotlinx.coroutines.flow.Flow

class PersonRepository(private val personDao: PersonDao) {
    fun getAllPersons(): Flow<List<Person>> = personDao.getAllPersons()
    suspend fun getPersonById(id: Int): Person? = personDao.getPersonById(id)
    suspend fun insertPerson(person: Person) = personDao.insertPerson(person)
    suspend fun updatePerson(person: Person) = personDao.updatePerson(person)
    suspend fun deletePerson(person: Person) = personDao.deletePerson(person)
}
