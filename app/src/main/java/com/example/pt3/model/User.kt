package com.example.pt3.model

import kotlinx.serialization.Serializable

@Serializable
enum class Role {
    DAOTAO, SINHVIEN
}

@Serializable
data class User(
    val id: String? = null,
    val username: String,
    val password: String,
    val role: Role,
    val sinhvien_id: String? = null
)
