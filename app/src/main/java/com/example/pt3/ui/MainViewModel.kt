package com.example.pt3.ui

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pt3.data.SupabaseRepository
import com.example.pt3.model.Nganh
import com.example.pt3.model.Role
import com.example.pt3.model.Sinhvien
import com.example.pt3.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    private val repository = SupabaseRepository()

    private val _sinhviens = MutableStateFlow<List<Sinhvien>>(emptyList())
    val sinhviens: StateFlow<List<Sinhvien>> = _sinhviens

    private val _nganhs = MutableStateFlow<List<Nganh>>(emptyList())
    val nganhs: StateFlow<List<Nganh>> = _nganhs

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error

    init {
        refreshData()
    }

    fun refreshData() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                _sinhviens.value = repository.getAllSinhvien()
                _nganhs.value = repository.getAllNganh()
                _users.value = repository.getAllUsers()
            } catch (e: Exception) {
                e.printStackTrace()
                _error.value = "Lỗi tải dữ liệu: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun registerUser(user: User, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                if (user.role == Role.SINHVIEN) {
                    repository.registerStudentUser(user)
                } else {
                    repository.insertUser(user)
                }
                refreshData()
                onSuccess()
            } catch (e: Exception) { 
                e.printStackTrace()
                _error.value = "Lỗi đăng ký: ${e.message}"
            }
        }
    }

    suspend fun uploadImage(fileName: String, byteArray: ByteArray): String? {
        return repository.uploadImage(fileName, byteArray)
    }

    fun addSinhvien(sv: Sinhvien) {
        viewModelScope.launch {
            try {
                repository.insertSinhvien(sv)
                refreshData()
            } catch (e: Exception) { _error.value = e.message }
        }
    }

    fun updateSinhvien(sv: Sinhvien) {
        viewModelScope.launch {
            try {
                repository.updateSinhvien(sv)
                refreshData()
            } catch (e: Exception) { _error.value = e.message }
        }
    }

    fun deleteSinhvien(id: String) {
        viewModelScope.launch {
            try {
                repository.deleteSinhvien(id)
                refreshData()
            } catch (e: Exception) { _error.value = e.message }
        }
    }

    fun addNganh(nganh: Nganh) {
        viewModelScope.launch {
            try {
                repository.insertNganh(nganh)
                refreshData()
            } catch (e: Exception) { _error.value = e.message }
        }
    }

    fun updateNganh(nganh: Nganh) {
        viewModelScope.launch {
            try {
                repository.updateNganh(nganh)
                refreshData()
            } catch (e: Exception) { _error.value = e.message }
        }
    }

    fun deleteNganh(id: String) {
        viewModelScope.launch {
            try {
                repository.deleteNganh(id)
                refreshData()
            } catch (e: Exception) { _error.value = e.message }
        }
    }
}
