package com.b22706.smartwatch_test

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.icu.text.DateFormat
import android.icu.text.SimpleDateFormat
import android.os.*
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.wear.ambient.AmbientModeSupport
import com.b22706.smartwatch_test.databinding.ActivityMainBinding
import com.google.android.gms.tasks.Task
import com.google.android.gms.wearable.*
import java.io.File
import java.io.FileReader

import java.util.*
import kotlin.math.sqrt

class MainActivity : FragmentActivity(), SensorEventListener, AmbientModeSupport.AmbientCallbackProvider{
    private var nodeSet: MutableSet<Node> = mutableSetOf()
    private lateinit var binding: ActivityMainBinding
    private lateinit var sensorManager: SensorManager
    private lateinit var ambientController: AmbientModeSupport.AmbientController

    private val acceleration = Acceleration()
    private val gyroscope = Gyroscope()
    private val heartRate = HeartRate()

    private var csvFolderPath = ""
    private var csvAdd = false

    override fun getAmbientCallback(): AmbientModeSupport.AmbientCallback =
        object : AmbientModeSupport.AmbientCallback(){
            override fun onEnterAmbient(ambientDetails: Bundle?) {
                // アンビエントモードに入る際の処理
            }

            override fun onExitAmbient() {
                // アンビエントモードを終了する際の処理
            }

            override fun onUpdateAmbient() {
                // コンテンツを更新時
            }
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        ambientController = AmbientModeSupport.attach(this)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // センサのオン・オフ
        binding.sensorSwitch.setOnClickListener {
            if(binding.sensorSwitch.isChecked){
                //Listenerの登録
                val heart = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)
                val acc = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
                val gyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
                val lAcc = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
                sensorManager.registerListener(this, gyro, SensorManager.SENSOR_DELAY_GAME)
                sensorManager.registerListener(this, heart, SensorManager.SENSOR_DELAY_NORMAL) //
                sensorManager.registerListener(this, acc, SensorManager.SENSOR_DELAY_GAME) //50Hz
            }else{
                sensorManager.unregisterListener(this)
            }
        }

        // 装着側判別用
        binding.rlSwitch.setOnClickListener {
            if(binding.rlSwitch.isChecked){
                binding.rlSwitch.text = "右"
            }else{
                binding.rlSwitch.text = "左"
            }
        }

        // csvの出力
        binding.csvCreateButton.setOnClickListener {
            if(csvAdd){
                csvWrite(binding.csvFileNameText.text.toString())
                binding.csvCreateButton.text = "csv入力"
                csvAdd = false
            }else{
                acceleration.queueReset()
                gyroscope.queueReset()
                heartRate.queueReset()
                binding.csvCreateButton.text = "csv出力"
                csvAdd = true
            }
        }

        requestPermission()
        setupSendMessage()

        val folder = File(this.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).toString(), "csv")
        if(!folder.exists())folder.mkdirs()
        csvFolderPath = folder.path

        Log.d("test", cnvDate(Date(System.currentTimeMillis())))
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
    }

    private fun requestPermission(){
        //外部ストレージへの読み込みが許可されているか
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // 許可されていないので許可ダイアログを表示する
            requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 0)
        }
        //外部ストレージへの書き込みが許可されているか
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // 許可されていないので許可ダイアログを表示する
            requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 0)
        }
        //身体センサが許可されているか
        if (checkSelfPermission(Manifest.permission.BODY_SENSORS) != PackageManager.PERMISSION_GRANTED) {
            // 許可されていないので許可ダイアログを表示する
            requestPermissions(arrayOf(Manifest.permission.BODY_SENSORS), 0)
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            //加速度センサデータを取得
            val sensorVal = event.values.clone()
            val x = sensorVal[0]
            val y = sensorVal[1]
            val z = sensorVal[2]
            //val norm = sqrt(x*x + y*y + z*z)

            //数値を表示
            binding.xView.text = "x:".plus(x)
            binding.yView.text = "y:".plus(y)
            binding.zView.text = "z:".plus(z)

            //データ追加
            if(csvAdd) acceleration.queueAdd(System.currentTimeMillis(),x,y,z)
            //データ送信
//            makePushData(sensorVal, "acc")
        }

        if (event.sensor.type == Sensor.TYPE_GYROSCOPE) {
            //角速度センサデータを取得
            val sensorVal = event.values.clone()
            val x = sensorVal[0]
            val y = sensorVal[1]
            val z = sensorVal[2]
            val norm = sqrt(x*x + y*y + z*z)

            //数値を表示
            //binding.xView.text = "x:".plus(x)
            //binding.yView.text = "y:".plus(y)
            //binding.zView.text = "z:".plus(z)

            //データ追加
            if(csvAdd) gyroscope.queueAdd(System.currentTimeMillis(),x,y,z)
            //データ送信
//            makePushData(sensorVal, "gyroscope")
        }

        if(event.sensor.type == Sensor.TYPE_HEART_RATE){//TYPE_HEART_RATE
            //心拍センサデータを取得
            val sensorVal = event.values.clone()

            //数値を表示
            binding.healthView.text = sensorVal[0].toString()

            if(csvAdd) heartRate.queueAdd(System.currentTimeMillis(), sensorVal[0])
            //データ送信
//            makePushData(sensorVal, "heartRate")
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {

    }

    private fun csvWrite(fileName: String){
        Toast.makeText(
            this,
            "csv writing",
            Toast.LENGTH_SHORT
        ).show()
        val fName = when(binding.fNameSwitch.isChecked){
            true -> cnvDate(Date(System.currentTimeMillis()))
            false -> fileName
        }.plus(when(binding.rlSwitch.isChecked){
            true -> "R"
            false -> "L"
        })

        // スレッド処理
        Handler(Looper.getMainLooper()).post {
            acceleration.csvWriter(csvFolderPath, fName).let {
                when (it) {
                    true -> {
                        Toast.makeText(
                            this,
                            "csv success",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    false -> {
                        Toast.makeText(
                            this,
                            "csv defeat",
                            Toast.LENGTH_LONG
                        ).show()
                        Log.d("csvWrite", "defeat")
                    }
                }
            }
            gyroscope.csvWriter(csvFolderPath, fName).let { if (!it) Log.d("csvWrite", "defeat") }
            heartRate.csvWriter(csvFolderPath, fName).let { if (!it) Log.d("csvWrite", "defeat") }
        }
    }

    private fun cnvDate(date: Date): String {
        //取得する日時のフォーマットを指定
        val df: DateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH-mm-ss", Locale.JAPAN)

        //日時を指定したフォーマットで取得
        return df.format(date)
    }

    // 送信のセットアップ
    private fun setupSendMessage() {
        val capabilityInfo: Task<CapabilityInfo> =
            Wearable.getCapabilityClient(applicationContext)
                .getCapability(
                    "sensorCapabilities",
                    CapabilityClient.FILTER_REACHABLE
                )

        capabilityInfo.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                nodeSet = task.result.nodes
                Log.d("watch.setupRequest","successSetup, ".plus(nodeSet))
            } else {
                Log.d("watch.setupRequest","defeatSetup")
            }
        }
    }

    // データ送信用の文字列生成
    private fun makePushData(data: FloatArray, tag: String){
        // data:センサデータの配列, tag:メッセージのタグ（センサごとに別のタグを設定）
        var message = ""
        // dataから１行分のcsvを生成
        for(len in data){
            when(message){
                "" -> message = len.toString()
                else -> message += ",".plus(len.toString())
            }
        }
        sendSensorData(System.currentTimeMillis().toString().plus(",").plus(message), tag)
    }

    // データを送信
    private fun sendSensorData(dataText: String, tag: String){
        //Log.d("watch.requestSensorData","start")
        nodeSet.let {
            pickBestNodeId(it)?.let { nodeId ->
                val data = dataText.toByteArray(Charsets.UTF_8) //バイナリ変換
                Wearable.getMessageClient(applicationContext)
                    .sendMessage(nodeId, tag, data)
                    .apply {
                        addOnSuccessListener {
                            //Log.d("watch.requestSensorData","success")
                        }
                        addOnFailureListener {
                            //Log.d("watch.requestSensorData","defeat")
                        }
                    }
            }
        }
    }

    // 送信先として最適なノードを選択
    private fun pickBestNodeId(nodes: Set<Node>): String? {
        return nodes.firstOrNull { it.isNearby }?.id ?: nodes.firstOrNull()?.id
    }
}