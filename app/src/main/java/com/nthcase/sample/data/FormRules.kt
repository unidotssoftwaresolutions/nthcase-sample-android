package com.nthcase.sample.data

/** The club form's fields, as typed. */
data class ClubForm(
    val name: String = "",
    val email: String = "",
    val age: String = "",
    val password: String = "",
    val category: Category? = null,
    val deliveryDate: String? = null,
    val newsletter: Boolean = false,
    val budget: Int = 50,
    val acceptedTerms: Boolean = false,
)

/** One message per invalid field. The texts are part of the spec: every framework shows the same words. */
object FormRules {
    const val NAME = "Enter your name"
    const val EMAIL = "Enter a valid email address"
    const val AGE = "Enter an age from 18 to 120"
    const val PASSWORD = "Use at least 8 characters, including a number"
    const val CATEGORY = "Choose a favourite aisle"
    const val DATE = "Choose a delivery date"
    const val TERMS = "Accept the terms to continue"

    private val emailShape = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")

    fun errors(form: ClubForm): Map<String, String> = buildMap {
        if (form.name.isBlank()) put("name", NAME)
        if (!emailShape.matches(form.email.trim())) put("email", EMAIL)
        val age = form.age.trim().toIntOrNull()
        if (age == null || age !in 18..120) put("age", AGE)
        if (form.password.length < 8 || form.password.none { it.isDigit() }) put("password", PASSWORD)
        if (form.category == null) put("category", CATEGORY)
        if (form.deliveryDate == null) put("date", DATE)
        if (!form.acceptedTerms) put("terms", TERMS)
    }
}
