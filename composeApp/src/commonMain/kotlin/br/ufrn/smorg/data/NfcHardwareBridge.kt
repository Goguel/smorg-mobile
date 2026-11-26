package br.ufrn.smorg.data

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Ponte multiplataforma para eventos de leitura física do sensor NFC (Android NfcAdapter).
 * No Android, o `enableReaderMode` da `MainActivity` publica o UID hexadecimal da tag física
 * aproximada do aparelho. No Desktop/Emulador, o simulador de UI permite testar o mesmo fluxo.
 */
object NfcHardwareBridge {
    private val _scannedTags = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val scannedTags: SharedFlow<String> = _scannedTags.asSharedFlow()

    private val _isHardwareSupported = MutableStateFlow(false)
    val isHardwareSupported: StateFlow<Boolean> = _isHardwareSupported.asStateFlow()

    private val _isHardwareEnabled = MutableStateFlow(false)
    val isHardwareEnabled: StateFlow<Boolean> = _isHardwareEnabled.asStateFlow()

    fun updateHardwareStatus(
        supported: Boolean,
        enabled: Boolean,
    ) {
        _isHardwareSupported.value = supported
        _isHardwareEnabled.value = enabled
    }

    fun onPhysicalTagScanned(tagHexId: String) {
        _scannedTags.tryEmit(tagHexId)
    }
}
