package com.mbm.superapp.data.api

/**
 * Backend configuration. Replace these placeholder values with your
 * actual Supabase project credentials.
 *
 * 1. Create a free project at https://supabase.com
 * 2. Copy the project URL and anon key from Settings → API
 * 3. Paste them here
 */
object BackendConfig {
    // Supabase
    const val SUPABASE_URL = "" // e.g. "https://xxxxx.supabase.co"
    const val SUPABASE_ANON_KEY = "" // e.g. "eyJhbGciOi..."

    // Cloudflare Worker (optional CDN proxy)
    const val CLOUDFLARE_WORKER_URL = "" // e.g. "https://mbm-cdn.your-worker.workers.dev"

    val isConfigured get() = SUPABASE_URL.isNotEmpty() && SUPABASE_ANON_KEY.isNotEmpty()
}
