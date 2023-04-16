package com.example.myapplication

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.ContextCompat
import java.io.IOException
import java.io.InputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class BluetoothService(private val context: Context) {
    private var bluetoothManager: BluetoothManager? = null
    private var bluetoothAdapter: BluetoothAdapter? = null

    /*
    companion object {
        const val REQUEST_ENABLE_BT = 1
    }
    */

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    fun connect(): BluetoothSocket? {
        Log.d("Bluetooth", "trying to connect")
        bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager?.adapter

        // verifica se o dispositivo suporta bluetooth
        if (bluetoothAdapter == null) {
            Toast.makeText(context, "Este dispositivo nao suporta bluetooth", Toast.LENGTH_LONG).show()
            return null
        }

        // verifica permissoes
        /*
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(context as Activity,
                arrayOf(Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN),
                REQUEST_ENABLE_BT)
            return
        }
        */

        // verifica se o bluetooth esta ativo
        if (bluetoothAdapter?.isEnabled == false) {
            // bluetooth nao esta ativado, solicitacao de ativacao
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            enableBtIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(enableBtIntent)
            return null
        }

        // verifica se existem dispositivos pareados
        val pairedDevices: Set<BluetoothDevice> = bluetoothAdapter!!.bondedDevices
        if (pairedDevices.isEmpty()) {
            Toast.makeText(context, "Nao ha dispositivos pareados", Toast.LENGTH_LONG).show()
            return null
        }

        // exibir a lista de dispositivos pareados
        for (device in pairedDevices) {
            //Toast.makeText(context, "Dispositivo pareado: " + device.name, Toast.LENGTH_LONG).show()
            Log.d("Bluetooth", "Dispositivo pareado: " + device.name)
        }

        // conecta ao primeiro dispositivo pareado
        val device: BluetoothDevice = pairedDevices.first()
        val socket: BluetoothSocket = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"))
        Toast.makeText(context, "Conectando ao dispositivo: " + device.name, Toast.LENGTH_LONG).show()
        return try {
            socket.connect()
            Toast.makeText(context, "Conexao estabelecida", Toast.LENGTH_LONG).show()
            Log.d("Bluetooth", "connected")

            socket
        } catch (e: IOException) {
            Toast.makeText(context, "Nao foi possivel conectar ao dispositivo", Toast.LENGTH_LONG).show()

            null
        }
    }

    /*// funcao para iniciar a thread que recebe mensagens do dispositivo bluetooth
    fun startListeningThread(socket: BluetoothSocket) {
        val thread = Thread(Runnable {
            val inputStream: InputStream = socket.inputStream
            val buffer = ByteArray(1024)
            var bytes: Int

            while (true) {
                try {
                    bytes = inputStream.read(buffer)
                    val message = String(buffer, 0, bytes)
                    // faca algo com a mensagem recebida, por exemplo:
                    Log.d("Bluetooth", "AAAAAAAAAAAAA: $message")
                    //runOnUiThread { Toast.makeText(context, "Mensagem recebida: $message", Toast.LENGTH_LONG).show() }
                } catch (e: IOException) {
                    // erro ao ler mensagem, encerrar a thread
                    Log.d("Bluetooth", "BBBBBBBBBBB")
                    break
                }
            }
        })

        thread.start()
    }*/

    // funcao para iniciar a coroutine que recebe mensagens do dispositivo Bluetooth
    fun startListeningThread(socket: BluetoothSocket) {
        GlobalScope.launch(Dispatchers.IO) {
            val inputStream: InputStream = socket.inputStream
            val buffer = ByteArray(2048)
            var bytes: Int

            while (true) {
                try {
                    bytes = inputStream.read(buffer)
                    val message = String(buffer, 0, bytes)
                    // faca algo com a mensagem recebida, por exemplo:
                    withContext(Dispatchers.Main) { Toast.makeText(context, "Mensagem recebida: $message", Toast.LENGTH_LONG).show() }
                } catch (e: IOException) {
                    // Erro ao ler mensagem, encerrar a coroutine
                    break
                }
            }
        }
    }

    // funcao para desconectar do dispositivo bluetooth
    fun disconnect(socket: BluetoothSocket?) {
        if (socket != null && socket.isConnected) {
            try {
                socket.close()
                Toast.makeText(context, "Desconectado do dispositivo", Toast.LENGTH_LONG).show()
            } catch (e: IOException) {
                Toast.makeText(context, "Erro ao desconectar do dispositivo", Toast.LENGTH_LONG).show()
            }
        }
    }
}
