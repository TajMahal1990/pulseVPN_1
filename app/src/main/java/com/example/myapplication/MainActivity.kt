package com.example.myapplication

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.myapplication.ui.theme.MyApplicationTheme


import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.wireguard.android.backend.Tunnel
import com.wireguard.android.backend.GoBackend
import com.wireguard.config.Config
import com.example.myapplication.ui.theme.MyApplicationTheme
import java.io.BufferedReader
import java.io.StringReader


import androidx.compose.ui.text.style.TextAlign


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
    var isConnected by remember { mutableStateOf(false) }
    var vpnError by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    // Единственный Tunnel для включения и выключения
    val tunnel = remember {
        object : Tunnel {
            override fun getName() = "DemoTunnel"
            override fun onStateChange(newState: Tunnel.State) {
                // Можно логировать статусы или обновлять UI
            }
        }
    }

    val vpnPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            startVpn(context, tunnel,
                onSuccess = {
                    isConnected = true
                    vpnError = null
                },
                onError = { err ->
                    vpnError = "Ошибка: $err"
                    isConnected = false
                })
        } else {
            vpnError = "Разрешение на VPN отклонено"
            isConnected = false
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = {
                if (!isConnected) {
                    val intent = GoBackend.VpnService.prepare(context)
                    if (intent != null) {
                        vpnPermissionLauncher.launch(intent)
                    } else {
                        startVpn(context, tunnel,
                            onSuccess = {
                                isConnected = true
                                vpnError = null
                            },
                            onError = { err ->
                                vpnError = "Ошибка: $err"
                                isConnected = false
                            })
                    }
                } else {
                    stopVpn(context, tunnel,
                        onSuccess = {
                            isConnected = false
                            vpnError = null
                        },
                        onError = { err ->
                            vpnError = "Ошибка при отключении: $err"
                        })
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isConnected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
            )
        ) {
            Text(if (isConnected) "Выключить VPN" else "Включить VPN")
        }

        Spacer(modifier = Modifier.height(16.dp))
        if (isConnected) {
            Text("Статус: VPN активен ✅", color = MaterialTheme.colorScheme.primary)
        } else {
            Text("VPN неактивен", color = MaterialTheme.colorScheme.error)
        }
        vpnError?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                it,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun startVpn(
    context: android.content.Context,
    tunnel: Tunnel,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    val configString = """
       [Interface]
            PrivateKey = eMTgL1HBd3TC/GHSOhCDFyPHlyA/4KjmftZNwAI9dVI=
            Address = 10.66.66.2/32,fd42:42:42::2/128
            DNS = 1.1.1.1,1.0.0.1

            [Peer]
            PublicKey = evSSRsdVYG3D4SI/ANbEj86R1hz3bgG+evzwBl+ce1A=
            PresharedKey = 9LLvDv0QOQ52zDy+UGlr4dGPghLaTrGWCY6Wg7ZaCK0=
            Endpoint = 79.133.46.112:56258
            AllowedIPs = 0.0.0.0/0,::/0
    """.trimIndent()

    try {
        val config = Config.parse(BufferedReader(StringReader(configString)))
        val backend = GoBackend(context)
        backend.setState(tunnel, Tunnel.State.UP, config)
        onSuccess()
    } catch (e: Exception) {
        onError(e.message ?: "неизвестно")
    }
}

private fun stopVpn(
    context: android.content.Context,
    tunnel: Tunnel,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    try {
        val backend = GoBackend(context)
        backend.setState(tunnel, Tunnel.State.DOWN, null)
        onSuccess()
    } catch (e: Exception) {
        onError(e.message ?: "неизвестно")
    }
}
