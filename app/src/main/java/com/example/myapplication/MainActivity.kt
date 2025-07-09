package com.example.myapplication


import android.app.Activity
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.wireguard.android.backend.GoBackend
import com.wireguard.android.backend.Tunnel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

// Получает Context (нужен для запуска VPN)
//⟶ Создаёт список VPN-серверов с флагами и конфигами.


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                VpnScreen()
            }
        }
    }
}

@Composable
fun VpnScreen() {
    val context = LocalContext.current
    val vpnServers = listOf(
        VpnServer("Germany", "🇩🇪", configGermany),
        VpnServer("Singapore", "🇸🇬", configSingapore),
        VpnServer("France", "🇫🇷", configFrance)
    )

    var selectedServer by remember { mutableStateOf(vpnServers[0]) }
    var isConnected by remember { mutableStateOf(false) }
    var vpnError by remember { mutableStateOf<String?>(null) }
    var connectedTime by remember { mutableStateOf("00:00") }
    val downloadSpeed = remember { mutableStateOf("0 KB/s") }
    val uploadSpeed = remember { mutableStateOf("0 KB/s") }

    val backend = remember { GoBackend(context) }
    val tunnel = remember {
        object : Tunnel {
            override fun getName() = selectedServer.name
            override fun onStateChange(newState: Tunnel.State) {}
        }
    }

    val vpnPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            launchVpn(backend, tunnel, selectedServer.config) {
                isConnected = it
                vpnError = if (it) null else "Ошибка при запуске VPN"
            }
        } else {
            vpnError = "Разрешение на VPN отклонено"
        }
    }

    LaunchedEffect(isConnected) {
        if (isConnected) {
            var lastRx = 0L
            var lastTx = 0L
            var seconds = 0

            while (isConnected) {
                try {
                    val stats = withContext(Dispatchers.IO) {
                        backend.getStatistics(tunnel)
                    }
                    val rx = stats?.totalRx() ?: 0L
                    val tx = stats?.totalTx() ?: 0L
                    downloadSpeed.value = formatSpeed(rx - lastRx)
                    uploadSpeed.value = formatSpeed(tx - lastTx)
                    lastRx = rx
                    lastTx = tx
                    connectedTime = formatDuration(seconds++)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                delay(1000)
            }
            connectedTime = "00:00"
        }
    }

    VpnScreenUI(
        selectedServer = selectedServer,
        vpnServers = vpnServers,
        isConnected = isConnected,
        connectedTime = connectedTime,
        downloadSpeed = downloadSpeed,
        uploadSpeed = uploadSpeed,
        vpnError = vpnError,
        onServerSelected = { selectedServer = it },
        onConnectClicked = {
            if (!isConnected) {
                val intent = GoBackend.VpnService.prepare(context)
                if (intent != null) {
                    vpnPermissionLauncher.launch(intent)
                } else {
                    launchVpn(backend, tunnel, selectedServer.config) {
                        isConnected = it
                        vpnError = if (it) null else "Ошибка при запуске VPN"
                    }
                }
            } else {
                stopVpn(
                    backend,
                    tunnel,
                    onSuccess = { isConnected = false },
                    onError = { vpnError = it }
                )
            }
        }
    )
}
