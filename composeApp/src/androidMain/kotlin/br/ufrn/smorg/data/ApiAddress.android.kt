package br.ufrn.smorg.data

/**
 * Endereço da API no Android.
 * Por padrão aponta para a instância em nuvem da API de Web II (Render).
 * Para testar contra o backend rodando localmente na máquina hospedeira do emulador,
 * altere para "http://10.0.2.2:8080/api/v1". Para rodar sem API, altere para null.
 */
actual val defaultApiBaseUrl: String? = "https://smorg-api.onrender.com/api/v1"
