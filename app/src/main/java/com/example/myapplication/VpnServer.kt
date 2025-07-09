package com.example.myapplication


data class VpnServer(val name: String, val flag: String, val config: String)

val configGermany = """
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

val configSingapore = """
[Interface]
PrivateKey = ...
Address = ...
DNS = 1.1.1.1

[Peer]
PublicKey = ...
Endpoint = ...
AllowedIPs = 0.0.0.0/0,::/0
""".trimIndent()

val configFrance = """
[Interface]
PrivateKey = ...
Address = ...
DNS = 1.1.1.1

[Peer]
PublicKey = ...
Endpoint = ...
AllowedIPs = 0.0.0.0/0,::/0
""".trimIndent()
