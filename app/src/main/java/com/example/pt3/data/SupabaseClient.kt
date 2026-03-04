package com.example.pt3.data

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage

object SupabaseConfig {
    const val SUPABASE_URL = "https://uztuqydphjmsqvkvhonx.supabase.co"
    const val SUPABASE_KEY = "sb_publishable_0iMAG19bmYbo40BGJW2big_ayuLomDe"

    val client = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_KEY
    ) {
        install(Auth)
        install(Postgrest)
        install(Storage)
    }
}
