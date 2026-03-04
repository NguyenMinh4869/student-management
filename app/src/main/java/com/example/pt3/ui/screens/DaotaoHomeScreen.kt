package com.example.pt3.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.pt3.ui.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DaotaoHomeScreen(navController: NavController, viewModel: MainViewModel) {
    val sinhviens by viewModel.sinhviens.collectAsState()
    val nganhs by viewModel.nganhs.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quản lý Đào tạo") },
                actions = {
                    IconButton(onClick = { viewModel.refreshData() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Làm mới")
                    }
                    IconButton(onClick = { navController.navigate("login") }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Đăng xuất")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("edit_sinhvien/new") }) {
                Icon(Icons.Default.Add, contentDescription = "Thêm sinh viên")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            Row(modifier = Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(onClick = { navController.navigate("nganh_list") }) {
                    Text("Quản lý Ngành")
                }
            }

            LazyColumn {
                items(sinhviens) { sv ->
                    val nganh = nganhs.find { it.id == sv.nganh_id }?.ten_nganh ?: "Chưa rõ"
                    ListItem(
                        headlineContent = { Text(sv.ten) },
                        supportingContent = { Text("Ngành: $nganh") },
                        modifier = Modifier.clickable {
                            navController.navigate("home_sinhvien/${sv.id}")
                        },
                        trailingContent = {
                            IconButton(onClick = {
                                viewModel.deleteSinhvien(sv.id)
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Xóa")
                            }
                        }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}
