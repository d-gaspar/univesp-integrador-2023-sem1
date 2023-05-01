package com.example.weatheriot

import ai.onnxruntime.OnnxMap
import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.app.AlertDialog
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.graphics.Color
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.*
import android.text.InputType
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import java.io.*
import java.nio.FloatBuffer
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var bluetoothService: BluetoothService
    private lateinit var socket: BluetoothSocket
    private var connected: Boolean = false
    private val messages = mutableListOf<String>()
    private val messagesSpannable = SpannableStringBuilder()
    private val data = mutableListOf<String>()
    private lateinit var connectButton: Button
    private lateinit var textViewBT: TextView

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /*connectButton = findViewById(R.id.btn_connect)
        textViewBT = findViewById(R.id.text_view_bt)

        bluetoothService = BluetoothService(this)

        // desativar sub-menus
        var linearLayout: LinearLayout
        for (i in listOf<Int>(R.id.layout_bt, R.id.layout_data, R.id.layout_predict, R.id.layout_about)) {
            linearLayout = findViewById<LinearLayout>(i)
            linearLayout.isEnabled = false
            linearLayout.visibility = View.INVISIBLE
        }*/
    }

    // ---------------------------------------------------------------------------------------------
    // menu principal

    fun mainMenuBtn (v: View) {
        val contentFrame = findViewById<FrameLayout>(R.id.content_frame)

        // limpa o frame
        contentFrame.removeAllViews()

        when (v.id) {
            R.id.btn_bluetooth -> {
                val content = layoutInflater.inflate(R.layout.bt_layout, null)
                contentFrame.addView(content)
            }
            R.id.btn_data -> {
                val content = layoutInflater.inflate(R.layout.data_layout, null)
                contentFrame.addView(content)

                listData()
            }
            R.id.btn_predict -> {
                val content = layoutInflater.inflate(R.layout.predict_layout, null)
                contentFrame.addView(content)
            }
            R.id.btn_about -> {
                val content = layoutInflater.inflate(R.layout.about_layout, null)
                contentFrame.addView(content)
            }
            else -> {
                Toast.makeText(this, "LAYOUT INEXISTENTE", Toast.LENGTH_SHORT).show()
            }
        }
    }
/*
    fun mainMenuBtn (v: View) {
        var linearLayout: LinearLayout

        // desativa layout ativo
        for (i in listOf<Int>(R.id.layout_bt, R.id.layout_data, R.id.layout_predict, R.id.layout_about)) {
            linearLayout = findViewById<LinearLayout>(i)
            linearLayout.isEnabled = false
            linearLayout.visibility = View.GONE
        }

        // ativa layout atual
        when (v.id) {
            R.id.btn_bluetooth -> {
                linearLayout = findViewById<LinearLayout>(R.id.layout_bt)
                linearLayout.isEnabled = true
                linearLayout.visibility = View.VISIBLE
            }
            R.id.btn_content_table -> {
                linearLayout = findViewById<LinearLayout>(R.id.layout_data)
                linearLayout.isEnabled = true
                linearLayout.visibility = View.VISIBLE

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    listData()
                } else {
                    Toast.makeText(this, "Esta funcionalidade requer a versao Android 'O' ou superior", Toast.LENGTH_SHORT).show()
                }
            }
            R.id.btn_predict -> {
                linearLayout = findViewById<LinearLayout>(R.id.layout_predict)
                linearLayout.isEnabled = true
                linearLayout.visibility = View.VISIBLE
            }
            R.id.btn_about -> {
                linearLayout = findViewById<LinearLayout>(R.id.layout_about)
                linearLayout.isEnabled = true
                linearLayout.visibility = View.VISIBLE
            }
        }
    }

    // ---------------------------------------------------------------------------------------------
    // Bluetooth

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    fun connectBT (v: View) {
        // conecta ao dispositivo bluetooth
        if (!connected) {
            socket = bluetoothService.connect()!!
            if (socket != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    bluetoothService.startListeningThread(socket, ::updateValues)
                    connected = true
                    connectButton.text = "Desconectar"
                } else {
                    Toast.makeText(this, "Esta funcionalidade requer a versao Android 'N' ou superior", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            bluetoothService.disconnect(socket)
            connected = false

            Toast.makeText(this, "Desconectado", Toast.LENGTH_SHORT).show()

            connectButton.text = "Conectar"
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun updateValues (message: String) {
        val dataTime: String = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault()).format(Date())
        val dataTimeMessage = "$dataTime $message" // "yyyy-MM-dd HH:mm:ss.SSS MESSAGE"

        val spannable = SpannableStringBuilder(dataTimeMessage)
        spannable.setSpan(ForegroundColorSpan(Color.GREEN), 0, dataTime.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannable.setSpan(ForegroundColorSpan(Color.WHITE), dataTime.length + 1, spannable.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        // salva mensagem na lista de mensagens
        messages.add(dataTimeMessage)
        messagesSpannable.append(spannable) // alterando a cor

        // imprime no console do aplicativo
        println(dataTimeMessage)
        //textView.text = messages.joinToString(separator = "") // Sem alterar a cor
        textViewBT.text = messagesSpannable // alterando a cor
    }

    fun clearBT (v: View) {
        messages.clear()
        messagesSpannable.clear() // alterando a cor
        //textViewBT.text = messages.joinToString(separator = "") // Sem alterar a cor
        textViewBT.text = messagesSpannable
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun saveBT (v: View) {
        val string = messages.joinToString(separator = "")
        val filenameDefault = "raw_" + SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSS", Locale.getDefault()).format(Date())

        val filenameEditText = EditText(this)
        filenameEditText.setText(filenameDefault)
        val filenameDialog = AlertDialog.Builder(this)
            .setTitle("Salvar arquivo")
            .setMessage("Insira o nome do arquivo:")
            .setView(filenameEditText)
            .setPositiveButton("Salvar") { dialog, which ->
                val filename = filenameEditText.text.toString() + ".txt"
                val file = File(getExternalFilesDir(null), filename)

                try {
                    file.createNewFile()
                    FileOutputStream(file).use {
                        it.write(string.toByteArray())
                        Toast.makeText(this, "Arquivo salvo com sucesso: $filename", Toast.LENGTH_SHORT).show()

                        // limpa o console
                        messages.clear()
                        messagesSpannable.clear() // alterando a cor
                        //textView.text = messages.joinToString(separator = "") // Sem alterar a cor
                        textViewBT.text = messagesSpannable
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this, "Erro ao salvar o arquivo", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .create()

        filenameDialog.show()
    }
*/
    // ---------------------------------------------------------------------------------------------
    // Data

    private fun listData () {
        val path = getExternalFilesDir(null)

        if (path != null) {
            val files = path.listFiles()

            for (file in files) {
                if (file.isFile) {
                    val reader = BufferedReader(FileReader(file))
                    var line = reader.readLine()

                    while (line != null) {
                        if (!data.contains(line)) {
                            data.add(line)
                        }

                        line = reader.readLine()
                    }

                    reader.close()
                }
            }
        }

        val textView: TextView = findViewById(R.id.text_view_data)
        textView.text = data.joinToString(separator = "\n")
    }

    fun exportData (v: View) {
        val emailInputDialog = AlertDialog.Builder(this)
        emailInputDialog.setTitle("Enviar arquivo de texto")
        emailInputDialog.setMessage("Insira o e-mail do destinatário:")
        val input = EditText(this)
        input.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        emailInputDialog.setView(input)
        emailInputDialog.setPositiveButton("Enviar") { _, _ ->
            val recipient = input.text.toString()
            val emailIntent = Intent(Intent.ACTION_SENDTO)
            emailIntent.type = "text/plain"
            emailIntent.data = Uri.parse("mailto:")
            emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(recipient))
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Weather Prediction")
            emailIntent.putExtra(Intent.EXTRA_TEXT, data.joinToString(separator = "\n"))

            startActivity(Intent.createChooser(emailIntent, "Enviar email"))
        }
        emailInputDialog.setNegativeButton("Cancelar") { dialog, _ -> dialog.cancel() }
        emailInputDialog.show()
    }

    // ---------------------------------------------------------------------------------------------
    // Predict

    // cria o OrtSession com o OrtEnvironment
    private fun createORTSession( ortEnvironment: OrtEnvironment, modelID: Int) : OrtSession {
        val modelBytes = resources.openRawResource(modelID).readBytes()
        return ortEnvironment.createSession( modelBytes )
    }

    // faz predicoes com os valores de entrada
    private fun runPrediction(temperature: Float, humidity: Float, pressure: Float, ortSession: OrtSession , ortEnvironment: OrtEnvironment ) : String {
        // obter o nome do nó de entrada.
        val inputName = ortSession.inputNames?.iterator()?.next()

        // criar um objeto FloatBuffer com os valores de entrada.
        val floatBufferInputs = FloatBuffer.wrap(floatArrayOf(temperature, humidity, pressure))

        // cria um tensor de entrada com os valores do buffer de floats de formato (1, 1).
        // IMPORTANTE *************************************************************************************
        // a entrada do modelo eh dada em longArrayOf(1, 3), deve-se observar o codigo em python
        // aqui eh um vetor com 3 entradas
        val inputTensor = OnnxTensor.createTensor(ortEnvironment, floatBufferInputs, longArrayOf(1, 3))

        // Roda o modelo
        val results = ortSession.run( mapOf(inputName to inputTensor))

        // imprime os resultados
        var output_probability = ""
        for ((name, tensor) in results) {
            when (name) {
                "output_label" -> {
                    val values = tensor.value as Array<String>
                    println("$name: ${values.toList()}")
                }
                "output_probability" -> {
                    val mapList = tensor.value as List<OnnxMap>
                    output_probability = mapList.first().value["True"].toString()
                    output_probability = kotlin.math.round(output_probability.toFloat() * 100).toString() + "%"
                    println("Chance de chuva: $output_probability")
                }
                else -> println("$name: ${tensor.value.toString()}")
            }
        }

        // retorna os resultados
        return output_probability
    }

    fun prediction (v: View) {
        val edit_temperature: EditText = findViewById(R.id.predict_temperature)
        val edit_humidity: EditText = findViewById(R.id.predict_humidity)
        val edit_pressure: EditText = findViewById(R.id.predict_pressure)
        val textView_6: TextView = findViewById(R.id.text_view_predict_6h)
        val textView_12: TextView = findViewById(R.id.text_view_predict_12h)
        val textView_24: TextView = findViewById(R.id.text_view_predict_24h)

        // definir valores das variaveis de entrada
        val temperature = edit_temperature.text.toString().toFloat()
        val humidity = edit_humidity.text.toString().toFloat()
        val pressure = edit_pressure.text.toString().toFloat()

        val ortEnvironment = OrtEnvironment.getEnvironment()

        // 6 horas
        var ortSession: OrtSession = createORTSession(ortEnvironment, R.raw.model_logistic_reg_6h)
        var output = runPrediction(temperature, humidity, pressure, ortSession , ortEnvironment)
        textView_6.text = "Chuva em 6h: $output"

        // 12 horas
        ortSession = createORTSession(ortEnvironment, R.raw.model_logistic_reg_12h)
        output = runPrediction(temperature, humidity, pressure, ortSession , ortEnvironment)
        textView_12.text = "Chuva em 12h: $output"

        // 24 horas
        ortSession = createORTSession(ortEnvironment, R.raw.model_logistic_reg_24h)
        output = runPrediction(temperature, humidity, pressure, ortSession , ortEnvironment)
        textView_24.text = "Chuva em 24h: $output"
    }
}
