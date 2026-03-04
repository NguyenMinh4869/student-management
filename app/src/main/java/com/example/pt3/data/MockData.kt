package com.example.pt3.data

import com.example.pt3.model.*

object MockData {
    val nganhs = mutableListOf(
        Nganh("1", "Công nghệ thông tin"),
        Nganh("2", "Kinh tế"),
        Nganh("3", "Ngôn ngữ Anh")
    )

    val sinhviens = mutableListOf(
        Sinhvien("1", "Nguyễn Văn A", "a@gmail.com", "0123456789", "1"),
        Sinhvien("2", "Trần Thị B", "b@gmail.com", "0987654321", "2")
    )

    val users = mutableListOf(
        User(username = "admin", password = "admin123", role = Role.DAOTAO),
        User(username = "student1", password = "123", role = Role.SINHVIEN, sinhvien_id = "1")
    )
    
    var currentUser: User? = null
}
