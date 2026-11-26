package br.ufrn.smorg.domain

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DomainRulesTest {
    @Test
    fun blankIdentifierIsNotValid() {
        assertFalse(isIdentifierValid("   "))
        assertFalse(isIdentifierValid(""))
    }

    @Test
    fun nonBlankIdentifierIsValid() {
        assertTrue(isIdentifierValid("20240027427"))
    }

    @Test
    fun shortPasswordIsNotValid() {
        assertFalse(isPasswordValid("12345"))
    }

    @Test
    fun sixOrMoreCharPasswordIsValid() {
        assertTrue(isPasswordValid("123456"))
    }
}
