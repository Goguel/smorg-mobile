package br.ufrn.smorg.data

/**
 * Endereço da API no Desktop.
 * Permite sobrescrever via variável de ambiente SMORG_API:
 * - SMORG_API=memoria -> usa o repositório em memória (null)
 * - SMORG_API=http://localhost:8080/api/v1 -> usa servidor local
 * - Padrão -> usa a API publicada no Render
 */
actual val defaultApiBaseUrl: String? =
    when (val envUrl = System.getenv("SMORG_API")) {
        null, "" -> "https://smorg-api.onrender.com/api/v1"
        "memoria" -> null
        else -> envUrl
    }
