package com.example.pt3.model

import kotlinx.serialization.Serializable

@Serializable
data class Sinhvien(
    val id: String,
    val ten: String,
    val email: String? = null,
    val sdt: String? = null,
    val dia_chi: String? = null,
    val nganh_id: String? = null,
    val image_uri: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
)
