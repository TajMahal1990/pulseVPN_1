package com.example.myapplication

import androidx.compose.runtime.MutableState
import com.wireguard.android.backend.GoBackend
import com.wireguard.android.backend.Tunnel
import com.wireguard.config.Config
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.StringReader

fun launchVpn(
    backend: GoBackend,
    tunnel: Tunnel,
    configString: String,
    sharedPrefs: android.content.SharedPreferences,
    showPremiumDialog: MutableState<Boolean>,
    onResult: (Boolean) -> Unit
) {
    CoroutineScope(Dispatchers.Main).launch {
        delay(500) // ⏱ Дать системе время поднять VpnService

        try {
            val config = withContext(Dispatchers.IO) {
                Config.parse(BufferedReader(StringReader(configString)))
            }

            withContext(Dispatchers.IO) {
                backend.setState(tunnel, Tunnel.State.UP, config)
            }

            onResult(true)

            launch {
                delay(100_000)
                stopVpn(backend, tunnel, onSuccess = {
                    onResult(false)
                    sharedPrefs.edit().putLong("trial_end", System.currentTimeMillis() + 86_400_000).apply()
                    showPremiumDialog.value = true
                }, onError = { })
            }
        } catch (e: Exception) {
            onResult(false)
        }
    }
}


fun stopVpn(
    backend: GoBackend,
    tunnel: Tunnel,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    try {
        backend.setState(tunnel, Tunnel.State.DOWN, null)
        onSuccess()
    } catch (e: Exception) {
        onError(e.message ?: "неизвестно")
    }
}

fun formatSpeed(bytesPerSec: Long): String {
    val kb = bytesPerSec / 1024.0
    val mb = kb / 1024.0
    return when {
        mb >= 1 -> "%.1f MB/s".format(mb)
        kb >= 1 -> "%.1f KB/s".format(kb)
        else -> "$bytesPerSec B/s"
    }
}

fun formatDuration(seconds: Int): String = "%02d:%02d".format(seconds / 60, seconds % 60)
