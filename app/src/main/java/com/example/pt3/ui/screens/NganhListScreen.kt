package com.example.pt3.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.pt3.model.Nganh
import com.example.pt3.ui.MainViewModel
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NganhListScreen(navController: NavController, viewModel: MainViewModel) {
    val nganhs by viewModel.nganhs.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var editingNganh by remember { mutableStateOf<Nganh?>(null) }
    var nganhName by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quản lý Ngành") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                editingNganh = null
                nganhName = ""
                showDialog = true
            }) {
                Icon(Icons.Default.Add, contentDescription = "Thêm ngành")
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            items(nganhs) { nganh ->
                ListItem(
                    headlineContent = { Text(nganh.ten_nganh) },
                    trailingContent = {
                        Row {
                            IconButton(onClick = {
                                editingNganh = nganh
                                nganhName = nganh.ten_nganh
                                showDialog = true
                            }) {
                                Icon(Icons.Default.Edit, contentDescription = "Sửa")
                            }
                            IconButton(onClick = {
                                viewModel.deleteNganh(nganh.id)
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Xóa")
                            }
                        }
                    }
                )
                HorizontalDivider()
            }
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text(if (editingNganh == null) "Thêm ngành mới" else "Sửa tên ngành") },
                text = {
                    TextField(value = nganhName, onValueChange = { nganhName = it }, label = { Text("Tên ngành") })
                },
                confirmButton = {
                    Button(onClick = {
                        if (editingNganh == null) {
                            viewModel.addNganh(Nganh(UUID.randomUUID().toString(), nganhName))
                        } else {
                            viewModel.updateNganh(editingNganh!!.copy(ten_nganh = nganhName))
                        }
                        showDialog = false
                    }) {
                        Text("Lưu")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("Hủy")
                    }
                }
            )
        }
    }
}
