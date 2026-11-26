package br.ufrn.smorg

import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import br.ufrn.smorg.data.NfcHardwareBridge

class MainActivity :
    ComponentActivity(),
    NfcAdapter.ReaderCallback {
    private var nfcAdapter: NfcAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        updateNfcHardwareStatus()

        setContent {
            App(dataDirectory = filesDir.path)
        }
    }

    override fun onResume() {
        super.onResume()
        updateNfcHardwareStatus()
        val adapter = nfcAdapter
        if (adapter != null && adapter.isEnabled) {
            val flags =
                NfcAdapter.FLAG_READER_NFC_A or
                    NfcAdapter.FLAG_READER_NFC_B or
                    NfcAdapter.FLAG_READER_NFC_F or
                    NfcAdapter.FLAG_READER_NFC_V
            adapter.enableReaderMode(this, this, flags, null)
        }
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter?.disableReaderMode(this)
    }

    override fun onTagDiscovered(tag: Tag?) {
        val idBytes = tag?.id ?: return
        val hexTagId = idBytes.joinToString(":") { byte -> "%02X".format(byte) }
        runOnUiThread {
            NfcHardwareBridge.onPhysicalTagScanned(hexTagId)
        }
    }

    private fun updateNfcHardwareStatus() {
        val adapter = nfcAdapter
        NfcHardwareBridge.updateHardwareStatus(
            supported = adapter != null,
            enabled = adapter?.isEnabled == true,
        )
    }
}
