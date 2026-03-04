package com.example.pt3.util

import android.util.Log
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/** TomTom API key */
const val TOMTOM_API_KEY = "gKJGvdekolxmDwoHubzjot1BGx4HMYaP"

/**
 * Holds one result from a TomTom Fuzzy Search lookup.
 */
data class PlaceItem(
    val displayName: String,
    val lat: Double,
    val lon: Double
)

/**
 * Queries the TomTom Fuzzy Search API for places matching [query].
 */
fun searchTomTom(query: String, limit: Int = 10): List<PlaceItem> {
    if (query.isBlank()) return emptyList()
    
    // Cấu trúc URL chuẩn: search/2/search/{query}.json
    val encodedQuery = URLEncoder.encode(query, "UTF-8")
    val urlString = "https://api.tomtom.com/search/2/search/$encodedQuery.json" +
            "?key=$TOMTOM_API_KEY" +
            "&typeahead=true" +
            "&limit=$limit" +
            "&countrySet=VN" +
            "&language=vi-VN"
    
    return try {
        val url = URL(urlString)
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        conn.connectTimeout = 5000
        conn.readTimeout = 5000
        
        val responseCode = conn.responseCode
        if (responseCode != 200) {
            val errorBody = conn.errorStream?.bufferedReader()?.use { it.readText() }
            Log.e("TomTomAPI", "Server Error ($responseCode): $errorBody")
            return emptyList()
        }

        val json = conn.inputStream.bufferedReader().use { it.readText() }
        conn.disconnect()
        
        val root = JSONObject(json)
        val results = root.optJSONArray("results") ?: return emptyList()
        
        val items = mutableListOf<PlaceItem>()
        for (i in 0 until results.length()) {
            val item = results.getJSONObject(i)
            val address = item.getJSONObject("address")
            val position = item.getJSONObject("position")
            
            val freeformAddress = address.optString("freeformAddress", "")
            val municipality = address.optString("municipality", "")
            
            // Tạo tên hiển thị đẹp hơn
            val displayName = if (municipality.isNotEmpty() && !freeformAddress.contains(municipality)) {
                "$freeformAddress, $municipality"
            } else {
                freeformAddress
            }
            
            items.add(
                PlaceItem(
                    displayName = displayName,
                    lat = position.getDouble("lat"),
                    lon = position.getDouble("lon")
                )
            )
        }
        items
    } catch (e: Exception) {
        Log.e("TomTomAPI", "Request Failed: ${e.message}")
        emptyList()
    }
}
