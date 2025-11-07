package com.example.haircraftco

import org.junit.Test
import com.example.haircraftco.models.User
import org.junit.Assert.*

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }


    @Test
    fun userModelTest() {
        val user = User("id", "name", "email", "phone", "photo", 10)
        assertEquals("name", user.name)
    }


}