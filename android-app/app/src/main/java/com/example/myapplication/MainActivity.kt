package com.example.myapplication

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothSocket
import android.os.*
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var bluetoothService: BluetoothService
    private lateinit var socket: BluetoothSocket
    private var connected: Boolean = false
    private lateinit var connectButton: Button
    private lateinit var textView: TextView

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        connectButton = findViewById(R.id.btn_connect)
        textView = findViewById(R.id.text_view)

        bluetoothService = BluetoothService(this)

        connectButton.setOnClickListener {
            // conecta ao dispositivo bluetooth
            if (!connected) {
                //val socket: BluetoothSocket? = bluetoothService.connect()
                socket = bluetoothService.connect()!!
                if (socket != null) {
                    bluetoothService.startListeningThread(socket)
                    connected = true
                    textView.text = "Conectado"
                    connectButton.text = "DESCONECTAR BLUETOOTH"
                }
            } else {
                bluetoothService.disconnect(socket)
                connected = false
                textView.text = "Desconectado"
                connectButton.text = "CONECTAR BLUETOOTH"
            }


        }
    }
}
