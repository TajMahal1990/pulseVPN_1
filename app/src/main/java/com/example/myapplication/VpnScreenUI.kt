package com.example.myapplication


import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

//  экран интерфейса VPN, который позволяет выбрать сервер, подключиться или отключиться от VPN, а также отображает статус подключения, скорость трафика и ошибки. Он реагирует на действия пользователя и отображает соответствующую информацию.

@Composable
fun VpnScreenUI(
    selectedServer: VpnServer,
    vpnServers: List<VpnServer>,
    isConnected: Boolean,
    connectedTime: String,
    downloadSpeed: State<String>,
    uploadSpeed: State<String>,
    vpnError: String?,
    onServerSelected: (VpnServer) -> Unit,
    onConnectClicked: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("VPN Client", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(24.dp))

        var expanded by remember { mutableStateOf(false) }
        Button(onClick = { expanded = true }) {
            Text("${selectedServer.flag} ${selectedServer.name}")
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            vpnServers.forEach { server ->
                DropdownMenuItem(
                    onClick = {
                        onServerSelected(server)
                        expanded = false
                    },
                    text = { Text("${server.flag} ${server.name}") }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            if (isConnected) "Статус: VPN активен (${selectedServer.name}) ✅" else "VPN неактивен",
            color = if (isConnected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
        )

        if (isConnected) {
            Spacer(modifier = Modifier.height(8.dp))
            Text("Время подключения: $connectedTime")
        }

        Spacer(modifier = Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("↓")
                Text(downloadSpeed.value)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("↑")
                Text(uploadSpeed.value)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onConnectClicked,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(if (isConnected) "Отключить VPN" else "Подключиться")
        }

        vpnError?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }
    }
}
