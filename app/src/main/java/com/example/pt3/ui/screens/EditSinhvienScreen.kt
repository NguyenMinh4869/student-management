package com.example.pt3.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.provider.ContactsContract
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.pt3.model.Sinhvien
import com.example.pt3.ui.MainViewModel
import com.example.pt3.util.PlaceItem
import com.example.pt3.util.searchTomTom
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditSinhvienScreen(navController: NavController, viewModel: MainViewModel, studentId: String?) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isNew = studentId == "new"
    
    val sinhviens by viewModel.sinhviens.collectAsState()
    val nganhs by viewModel.nganhs.collectAsState()
    val existingSv = remember(sinhviens, studentId) { sinhviens.find { it.id == studentId } }

    var ten by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var sdt by remember { mutableStateOf("") }
    var diaChi by remember { mutableStateOf("") }
    var nganhId by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<String?>(null) }
    var latitude by remember { mutableStateOf<Double?>(null) }
    var longitude by remember { mutableStateOf<Double?>(null) }

    // Suggestions state
    var suggestions by remember { mutableStateOf<List<PlaceItem>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }

    LaunchedEffect(existingSv) {
        existingSv?.let {
            ten = it.ten
            email = it.email ?: ""
            sdt = it.sdt ?: ""
            diaChi = it.dia_chi ?: ""
            nganhId = it.nganh_id ?: ""
            imageUri = it.image_uri
            latitude = it.latitude
            longitude = it.longitude
        }
    }

    LaunchedEffect(nganhs) {
        if (isNew && nganhId.isEmpty() && nganhs.isNotEmpty()) {
            nganhId = nganhs.first().id
        }
    }

    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var pickedUri by remember { mutableStateOf<Uri?>(null) }
    var isUploading by remember { mutableStateOf(false) }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap != null) {
            capturedBitmap = bitmap
            pickedUri = null
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            pickedUri = uri
            capturedBitmap = null
        }
    }

    val contactLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickContact()) { uri ->
        uri?.let {
            val cursor = context.contentResolver.query(it, null, null, null, null)
            cursor?.use { c ->
                if (c.moveToFirst()) {
                    val id = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts._ID))
                    if (c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts.HAS_PHONE_NUMBER)) == "1") {
                        context.contentResolver.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id, null, null
                        )?.use { p -> if (p.moveToFirst()) sdt = p.getString(p.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)) }
                    }
                }
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { if (it) cameraLauncher.launch(null) }
    var expandedImageMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (isNew) "Thêm Sinh Viên Mới" else "Cập Nhật Hồ Sơ", fontWeight = FontWeight.Bold) },
                navigationIcon = { 
                    IconButton(onClick = { navController.popBackStack() }) { 
                        Icon(Icons.Default.ArrowBack, "Back") 
                    } 
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // AVATAR SECTION
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .border(3.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    .clickable { expandedImageMenu = true },
                contentAlignment = Alignment.Center
            ) {
                val painter = when {
                    capturedBitmap != null -> rememberAsyncImagePainter(capturedBitmap)
                    pickedUri != null -> rememberAsyncImagePainter(pickedUri)
                    else -> rememberAsyncImagePainter(imageUri ?: "https://cdn-icons-png.flaticon.com/512/3135/3135715.png")
                }
                Image(
                    painter = painter,
                    contentDescription = "Avatar",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    Surface(
                        modifier = Modifier
                            .size(45.dp)
                            .padding(4.dp)
                            .clip(CircleShape),
                        color = MaterialTheme.colorScheme.primary,
                        tonalElevation = 8.dp
                    ) {
                        Icon(Icons.Default.CameraAlt, null, modifier = Modifier.padding(8.dp), tint = Color.White)
                    }
                }
            }
            
            Text("Nhấn để thay đổi ảnh", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 8.dp))

            DropdownMenu(expanded = expandedImageMenu, onDismissRequest = { expandedImageMenu = false }) {
                DropdownMenuItem(
                    text = { Text("Chụp ảnh") },
                    leadingIcon = { Icon(Icons.Default.PhotoCamera, null) },
                    onClick = {
                        expandedImageMenu = false
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) cameraLauncher.launch(null)
                        else permissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                )
                DropdownMenuItem(
                    text = { Text("Thư viện") },
                    leadingIcon = { Icon(Icons.Default.Image, null) },
                    onClick = {
                        expandedImageMenu = false
                        galleryLauncher.launch("image/*")
                    }
                )
            }

            Spacer(Modifier.height(24.dp))

            if (isUploading) LinearProgressIndicator(modifier = Modifier.fillMaxWidth().clip(CircleShape))

            // FORM SECTION
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Thông tin cá nhân", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 16.sp)
                    
                    OutlinedTextField(
                        value = ten,
                        onValueChange = { ten = it },
                        label = { Text("Họ và tên") },
                        leadingIcon = { Icon(Icons.Default.Person, null) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    var nganhDropdownExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = nganhDropdownExpanded,
                        onExpandedChange = { nganhDropdownExpanded = !nganhDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = nganhs.find { it.id == nganhId }?.ten_nganh ?: "Chọn ngành học",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Ngành đào tạo") },
                            leadingIcon = { Icon(Icons.Default.School, null) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = nganhDropdownExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = nganhDropdownExpanded,
                            onDismissRequest = { nganhDropdownExpanded = false }
                        ) {
                            nganhs.forEach { nganh ->
                                DropdownMenuItem(
                                    text = { Text(nganh.ten_nganh) },
                                    onClick = {
                                        nganhId = nganh.id
                                        nganhDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                    Text("Thông tin liên hệ", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 16.sp)

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email sinh viên") },
                        leadingIcon = { Icon(Icons.Default.Email, null) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = sdt,
                        onValueChange = { sdt = it },
                        label = { Text("Số điện thoại") },
                        leadingIcon = { Icon(Icons.Default.Phone, null) },
                        trailingIcon = {
                            IconButton(onClick = { contactLauncher.launch(null) }) {
                                Icon(Icons.Default.ContactPhone, null, tint = MaterialTheme.colorScheme.primary)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    // ADDRESS AUTOCOMPLETE FOR ADMIN
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = diaChi,
                            onValueChange = { 
                                diaChi = it
                                if (it.length > 2) {
                                    scope.launch {
                                        isSearching = true
                                        val results = withContext(Dispatchers.IO) { searchTomTom(it) }
                                        suggestions = results
                                        isSearching = false
                                    }
                                } else {
                                    suggestions = emptyList()
                                }
                            },
                            label = { Text("Địa chỉ thường trú") },
                            leadingIcon = { Icon(Icons.Default.LocationOn, null) },
                            trailingIcon = {
                                if (isSearching) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                                else if (latitude != null) Icon(Icons.Default.CheckCircle, null, tint = Color.Green)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            minLines = 1
                        )

                        DropdownMenu(
                            expanded = suggestions.isNotEmpty(),
                            onDismissRequest = { suggestions = emptyList() },
                            modifier = Modifier.fillMaxWidth(0.9f),
                            properties = PopupProperties(focusable = false)
                        ) {
                            suggestions.forEach { place ->
                                DropdownMenuItem(
                                    text = { 
                                        Column {
                                            Text(place.displayName, fontSize = 14.sp)
                                            Text("${place.lat}, ${place.lon}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                        }
                                    },
                                    onClick = {
                                        diaChi = place.displayName
                                        latitude = place.lat
                                        longitude = place.lon
                                        suggestions = emptyList()
                                    }
                                )
                            }
                        }
                    }
                    
                    if (latitude != null) {
                        Text(
                            "Tọa độ đã xác định: $latitude, $longitude", 
                            style = MaterialTheme.typography.labelSmall, 
                            color = Color.Gray,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            // SAVE BUTTON
            Button(
                onClick = {
                    scope.launch {
                        if (ten.isBlank()) {
                            Toast.makeText(context, "Vui lòng nhập tên", Toast.LENGTH_SHORT).show()
                            return@launch
                        }
                        isUploading = true
                        var finalImageUri = imageUri

                        try {
                            if (capturedBitmap != null) {
                                val baos = ByteArrayOutputStream()
                                capturedBitmap!!.compress(Bitmap.CompressFormat.JPEG, 70, baos)
                                val fileName = "avatar_${UUID.randomUUID()}.jpg"
                                val uploadedUrl = viewModel.uploadImage(fileName, baos.toByteArray())
                                if (uploadedUrl != null) finalImageUri = uploadedUrl
                            } else if (pickedUri != null) {
                                val bytes = context.contentResolver.openInputStream(pickedUri!!)?.readBytes()
                                if (bytes != null) {
                                    val fileName = "avatar_${UUID.randomUUID()}.jpg"
                                    val uploadedUrl = viewModel.uploadImage(fileName, bytes)
                                    if (uploadedUrl != null) finalImageUri = uploadedUrl
                                }
                            }

                            val sv = Sinhvien(
                                id = if (isNew) UUID.randomUUID().toString() else studentId!!,
                                ten = ten, 
                                email = email.ifBlank { null }, 
                                sdt = sdt.ifBlank { null },
                                nganh_id = nganhId.ifBlank { null }, 
                                dia_chi = diaChi.ifBlank { null }, 
                                image_uri = finalImageUri,
                                latitude = latitude, 
                                longitude = longitude
                            )
                            
                            if (isNew) viewModel.addSinhvien(sv) else viewModel.updateSinhvien(sv)
                            Toast.makeText(context, "Lưu hồ sơ thành công!", Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
                        } catch (e: Exception) {
                            Toast.makeText(context, "Lỗi: ${e.message}", Toast.LENGTH_LONG).show()
                        } finally {
                            isUploading = false
                        }
                    }
                },
                enabled = !isUploading,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) { 
                if (isUploading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                else Text("CẬP NHẬT DỮ LIỆU", fontWeight = FontWeight.Bold)
            }
            
            Spacer(Modifier.height(24.dp))
        }
    }
}
