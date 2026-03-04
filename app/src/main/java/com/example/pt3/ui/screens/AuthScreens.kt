package com.example.pt3.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.pt3.data.MockData
import com.example.pt3.model.Role
import com.example.pt3.model.User
import com.example.pt3.ui.MainViewModel

@Composable
fun LoginScreen(navController: NavController, viewModel: MainViewModel) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val users by viewModel.users.collectAsState()
    val isLoading by viewModel.isLoading
    val context = LocalContext.current

    LaunchedEffect(Unit) { viewModel.refreshData() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(MaterialTheme.colorScheme.primaryContainer, Color.White)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo Placeholder
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(60.dp), tint = Color.White)
            }

            Spacer(Modifier.height(24.dp))
            Text("Chào Mừng Trở Lại", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text("Đăng nhập để tiếp tục quản lý", color = Color.Gray)

            Spacer(Modifier.height(32.dp))

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Tên đăng nhập") },
                leadingIcon = { Icon(Icons.Default.Person, null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Mật khẩu") },
                leadingIcon = { Icon(Icons.Default.Lock, null) },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, null)
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )

            Spacer(Modifier.height(32.dp))

            if (isLoading && users.isEmpty()) {
                CircularProgressIndicator()
            } else {
                Button(
                    onClick = {
                        val user = users.find { it.username.trim() == username.trim() && it.password == password }
                        if (user != null) {
                            MockData.currentUser = user
                            if (user.role == Role.DAOTAO) {
                                navController.navigate("home_daotao") { popUpTo("login") { inclusive = true } }
                            } else {
                                navController.navigate("home_sinhvien/${user.sinhvien_id}") { popUpTo("login") { inclusive = true } }
                            }
                        } else {
                            Toast.makeText(context, "Tài khoản hoặc mật khẩu không chính xác", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("ĐĂNG NHẬP", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            TextButton(
                onClick = { navController.navigate("register") },
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text("Chưa có tài khoản? Đăng ký ngay", color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun RegisterScreen(navController: NavController, viewModel: MainViewModel) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Tạo Tài Khoản", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text("Bắt đầu hành trình học tập của bạn", color = Color.Gray)

        Spacer(Modifier.height(32.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Tên đăng nhập mong muốn") },
            leadingIcon = { Icon(Icons.Default.AccountCircle, null) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        )

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Mật khẩu") },
            leadingIcon = { Icon(Icons.Default.VpnKey, null) },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        )

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Xác nhận mật khẩu") },
            leadingIcon = { Icon(Icons.Default.CheckCircle, null) },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        )

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = {
                if (password != confirmPassword) {
                    Toast.makeText(context, "Mật khẩu không khớp", Toast.LENGTH_SHORT).show()
                } else if (username.isBlank()) {
                    Toast.makeText(context, "Vui lòng nhập tên tài khoản", Toast.LENGTH_SHORT).show()
                } else {
                    viewModel.registerUser(User(username = username, password = password, role = Role.SINHVIEN)) {
                        Toast.makeText(context, "Đăng ký thành công!", Toast.LENGTH_SHORT).show()
                        navController.popBackStack()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("ĐĂNG KÝ SINH VIÊN", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        IconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = null)
        }
    }
}
