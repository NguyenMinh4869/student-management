package com.example.pt3.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.PopupProperties
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.pt3.data.MockData
import com.example.pt3.model.Role
import com.example.pt3.model.Sinhvien
import com.example.pt3.ui.MainViewModel
import com.example.pt3.util.PlaceItem
import com.example.pt3.util.searchTomTom
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SinhvienDetailScreen(navController: NavController, viewModel: MainViewModel, studentId: String?) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sinhviens by viewModel.sinhviens.collectAsState()
    val nganhs by viewModel.nganhs.collectAsState()
    val isLoadingGlobal by viewModel.isLoading
    
    var sv by remember { mutableStateOf<Sinhvien?>(null) }
    var isLocalLoading by remember { mutableStateOf(true) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ -> }

    LaunchedEffect(Unit) {
        locationPermissionLauncher.launch(arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ))
    }

    LaunchedEffect(sinhviens, studentId) {
        if (studentId != null && studentId != "null") {
            val found = sinhviens.find { it.id == studentId }
            if (found != null) {
                sv = found
                isLocalLoading = false
            } else {
                viewModel.refreshData()
                isLocalLoading = false
            }
        }
    }
    
    val nganh = remember(nganhs, sv) { nganhs.find { it.id == sv?.nganh_id }?.ten_nganh ?: "Chưa rõ" }
    val currentUser = MockData.currentUser

    var quickAddress by remember { mutableStateOf("") }
    var suggestions by remember { mutableStateOf<List<PlaceItem>>(emptyList()) }
    var isUpdatingLocation by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Thông Tin Sinh Viên", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { 
                        if (currentUser?.role == Role.DAOTAO) navController.popBackStack()
                        else {
                            MockData.currentUser = null
                            navController.navigate("login") { popUpTo(0) { inclusive = true } }
                        }
                    }) {
                        Icon(if (currentUser?.role == Role.DAOTAO) Icons.Default.ArrowBack else Icons.Default.ExitToApp, null)
                    }
                },
                actions = {
                    if (sv != null && (currentUser?.role == Role.DAOTAO || currentUser?.sinhvien_id == studentId)) {
                        IconButton(onClick = { navController.navigate("edit_sinhvien/${sv!!.id}") }) {
                            Icon(Icons.Default.Edit, contentDescription = "Chỉnh sửa")
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    ) { padding ->
        if (sv == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                if (isLoadingGlobal || isLocalLoading) {
                    CircularProgressIndicator()
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.SearchOff, null, Modifier.size(64.dp), Color.Gray)
                        Spacer(Modifier.height(8.dp))
                        Text("Không tìm thấy dữ liệu sinh viên.", color = Color.Gray)
                        Button(onClick = { viewModel.refreshData() }, Modifier.padding(top = 16.dp)) { Text("Thử lại") }
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // AVATAR SECTION
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                        .border(3.dp, MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (!sv!!.image_uri.isNullOrBlank()) {
                        Image(
                            painter = rememberAsyncImagePainter(sv!!.image_uri),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(Icons.Default.Person, null, Modifier.size(70.dp), tint = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                
                // MAIN INFO CARD
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = sv!!.ten,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Ngành: $nganh",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                        
                        HorizontalDivider(Modifier.padding(vertical = 16.dp), thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
                        
                        InfoRow(Icons.Default.Email, "Email", sv!!.email ?: "Chưa cập nhật")
                        InfoRow(Icons.Default.Phone, "SĐT", sv!!.sdt ?: "Chưa cập nhật")
                        InfoRow(Icons.Default.LocationOn, "Địa chỉ", sv!!.dia_chi ?: "Chưa cập nhật")
                        
                        if (sv!!.latitude != null) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                                Icon(Icons.Default.MyLocation, null, Modifier.size(16.dp), Color.Gray)
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "Tọa độ: ${sv!!.latitude}, ${sv!!.longitude}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                // QUICK UPDATE ADDRESS
                if (currentUser?.role == Role.DAOTAO || currentUser?.sinhvien_id == studentId) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = quickAddress,
                            onValueChange = { 
                                quickAddress = it
                                if (it.length > 2) {
                                    scope.launch {
                                        val results = withContext(Dispatchers.IO) { searchTomTom(it) }
                                        suggestions = results
                                    }
                                } else {
                                    suggestions = emptyList()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Tìm địa chỉ bằng TomTom...") },
                            trailingIcon = {
                                if (isUpdatingLocation) {
                                    CircularProgressIndicator(Modifier.size(24.dp))
                                } else {
                                    Icon(Icons.Default.Search, null, tint = MaterialTheme.colorScheme.primary)
                                }
                            },
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true
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
                                            Text(place.displayName, fontWeight = FontWeight.Medium)
                                            Text("${place.lat}, ${place.lon}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                        }
                                    },
                                    onClick = {
                                        scope.launch {
                                            isUpdatingLocation = true
                                            val updatedSv = sv!!.copy(
                                                dia_chi = place.displayName,
                                                latitude = place.lat,
                                                longitude = place.lon
                                            )
                                            viewModel.updateSinhvien(updatedSv)
                                            sv = updatedSv
                                            quickAddress = ""
                                            suggestions = emptyList()
                                            Toast.makeText(context, "Đã cập nhật vị trí!", Toast.LENGTH_SHORT).show()
                                            isUpdatingLocation = false
                                        }
                                    }
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(20.dp))
                }

                // MAP SECTION (MAPBOX - FIXED VISIBILITY)
                if (sv!!.latitude != null && sv!!.longitude != null) {
                    Text(
                        "Vị trí trên bản đồ",
                        modifier = Modifier.align(Alignment.Start).padding(bottom = 8.dp, start = 4.dp),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    val mapboxHtml = """
                        <!DOCTYPE html>
                        <html>
                        <head>
                            <meta charset="utf-8">
                            <meta name="viewport" content="initial-scale=1,maximum-scale=1,user-scalable=no">
                            <link href="https://api.mapbox.com/mapbox-gl-js/v3.1.2/mapbox-gl.css" rel="stylesheet">
                            <script src="https://api.mapbox.com/mapbox-gl-js/v3.1.2/mapbox-gl.js"></script>
                            <style>
                                html, body { height: 100%; margin: 0; padding: 0; overflow: hidden; }
                                #map { position: absolute; top: 0; bottom: 0; width: 100%; border-radius: 16px; background-color: #eee; }
                            </style>
                        </head>
                        <body>
                        <div id="map"></div>
                        <script>
                            try {
                                mapboxgl.accessToken = 'pk.eyJ1IjoiaG9hbmdtaW5oNzEyMDA0IiwiYSI6ImNtZjV3aGg4bjA5OHoyaXBueXl4NTcxaTUifQ.IcNTYz1u1rKxdwNIT8edtA';
                                const map = new mapboxgl.Map({
                                    container: 'map',
                                    style: 'mapbox://styles/mapbox/streets-v12',
                                    center: [${sv!!.longitude}, ${sv!!.latitude}],
                                    zoom: 14,
                                    attributionControl: false
                                });
                                new mapboxgl.Marker({ color: '#f44336' })
                                    .setLngLat([${sv!!.longitude}, ${sv!!.latitude}])
                                    .addTo(map);
                                map.addControl(new mapboxgl.NavigationControl(), 'top-right');
                                console.log("Mapbox Render Success");
                            } catch (e) {
                                console.error("Mapbox JS Error: " + e.message);
                            }
                        </script>
                        </body>
                        </html>
                    """.trimIndent()

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(350.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(24.dp))
                    ) {
                        AndroidView(
                            factory = { ctx ->
                                WebView(ctx).apply {
                                    settings.apply {
                                        javaScriptEnabled = true
                                        domStorageEnabled = true
                                        useWideViewPort = true
                                        loadWithOverviewMode = true
                                        mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                                    }
                                    
                                    webChromeClient = object : WebChromeClient() {
                                        override fun onConsoleMessage(cm: ConsoleMessage?): Boolean {
                                            Log.d("MapboxDebug", "JS: " + cm?.message())
                                            return true
                                        }
                                    }
                                    
                                    webViewClient = object : WebViewClient() {
                                        override fun onReceivedError(v: WebView?, r: WebResourceRequest?, e: WebResourceError?) {
                                            Log.e("MapboxDebug", "Network Error: " + e?.description)
                                        }
                                        override fun onPageFinished(view: WebView?, url: String?) {
                                            Log.d("MapboxDebug", "Page Loaded: $url")
                                        }
                                    }
                                    
                                    loadDataWithBaseURL("https://api.mapbox.com", mapboxHtml, "text/html", "UTF-8", null)
                                }
                            },
                            update = { 
                                Log.d("MapboxDebug", "Updating coordinates: ${sv!!.latitude}, ${sv!!.longitude}")
                                it.loadDataWithBaseURL("https://api.mapbox.com", mapboxHtml, "text/html", "UTF-8", null) 
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
                
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, Modifier.size(18.dp), MaterialTheme.colorScheme.primary)
        }
        Spacer(Modifier.width(16.dp))
        Column {
            Text(label, fontSize = 12.sp, color = Color.Gray)
            Text(value, fontWeight = FontWeight.Medium, fontSize = 15.sp)
        }
    }
}
