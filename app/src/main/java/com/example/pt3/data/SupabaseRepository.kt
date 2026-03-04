package com.example.pt3.data

import com.example.pt3.model.Nganh
import com.example.pt3.model.Sinhvien
import com.example.pt3.model.User
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID
import kotlin.time.Duration.Companion.minutes

class SupabaseRepository {
    private val client = SupabaseConfig.client

    // AUTH / USERS
    suspend fun getAllUsers(): List<User> = withContext(Dispatchers.IO) {
        client.from("users").select().decodeList<User>()
    }

    suspend fun insertUser(user: User) = withContext(Dispatchers.IO) {
        client.from("users").insert(user)
    }

    // Đăng ký cho Sinh viên: Tạo đồng thời bản ghi ở bảng sinhvien và users
    suspend fun registerStudentUser(user: User) = withContext(Dispatchers.IO) {
        val studentId = UUID.randomUUID().toString()
        
        // 1. Tạo bản ghi sinh viên cơ bản (với tên lấy từ username)
        val basicSv = Sinhvien(
            id = studentId,
            ten = user.username,
            email = null,
            sdt = null,
            dia_chi = null,
            nganh_id = null
        )
        client.from("sinhvien").insert(basicSv)

        // 2. Tạo user và liên kết với sinh viên vừa tạo qua sinhvien_id
        val newUser = user.copy(sinhvien_id = studentId)
        client.from("users").insert(newUser)
    }

    // NGÀNH
    suspend fun getAllNganh(): List<Nganh> = withContext(Dispatchers.IO) {
        client.from("nganh").select().decodeList<Nganh>()
    }

    suspend fun insertNganh(nganh: Nganh) = withContext(Dispatchers.IO) {
        client.from("nganh").insert(nganh)
    }

    suspend fun updateNganh(nganh: Nganh) = withContext(Dispatchers.IO) {
        client.from("nganh").update(
            mapOf("ten_nganh" to nganh.ten_nganh)
        ) {
            filter { eq("id", nganh.id) }
        }
    }

    suspend fun deleteNganh(id: String) = withContext(Dispatchers.IO) {
        client.from("nganh").delete {
            filter { eq("id", id) }
        }
    }

    // SINH VIÊN
    suspend fun getAllSinhvien(): List<Sinhvien> = withContext(Dispatchers.IO) {
        client.from("sinhvien").select().decodeList<Sinhvien>()
    }

    suspend fun getSinhvienById(id: String): Sinhvien? = withContext(Dispatchers.IO) {
        client.from("sinhvien").select {
            filter { eq("id", id) }
        }.decodeSingleOrNull<Sinhvien>()
    }

    suspend fun insertSinhvien(sv: Sinhvien) = withContext(Dispatchers.IO) {
        client.from("sinhvien").insert(sv)
    }

    suspend fun updateSinhvien(sv: Sinhvien) = withContext(Dispatchers.IO) {
        client.from("sinhvien").update(sv) {
            filter { eq("id", sv.id) }
        }
    }

    suspend fun deleteSinhvien(id: String) = withContext(Dispatchers.IO) {
        client.from("sinhvien").delete {
            filter { eq("id", id) }
        }
    }

    // STORAGE
    suspend fun uploadImage(fileName: String, byteArray: ByteArray): String? = withContext(Dispatchers.IO) {
        try {
            val bucket = client.storage.from("avatars")
            bucket.upload(fileName, byteArray) {
                upsert = true
            }
            return@withContext bucket.publicUrl(fileName)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
