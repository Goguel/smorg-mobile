package br.ufrn.smorg.data

/**
 * Endereço base da API SmOrg por plataforma.
 * Quando nulo (ex.: SMORG_API=memoria no desktop), o aplicativo utiliza o repositório em memória.
 */
expect val defaultApiBaseUrl: String?
