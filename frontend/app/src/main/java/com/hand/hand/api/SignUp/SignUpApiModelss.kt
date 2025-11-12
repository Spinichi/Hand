package com.hand.hand.api.SignUp

data class SignupRequest(
    val email: String,
    val password: String
)

class SignupResponse