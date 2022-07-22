package com.b22706.smartwatch_test

import android.graphics.Bitmap
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.FragmentActivity
import androidx.wear.ambient.AmbientModeSupport
import com.b22706.smartwatch_test.databinding.ActivityMainBinding
import com.google.android.gms.tasks.Task
import com.google.android.gms.wearable.*
import java.io.ByteArrayOutputStream
import java.io.FileReader
import kotlin.math.sqrt

class MainActivity : FragmentActivity(), SensorEventListener, AmbientModeSupport.AmbientCallbackProvider{
    private var nodeSet: MutableSet<Node> = mutableSetOf()
    private lateinit var binding: ActivityMainBinding
    private lateinit var sensorManager: SensorManager
    private lateinit var ambientController: AmbientModeSupport.AmbientController

    private val acceleration: Acceleration = Acceleration()

    override fun getAmbientCallback(): AmbientModeSupport.AmbientCallback = MyAmbientCallback()
    private class MyAmbientCallback : AmbientModeSupport.AmbientCallback() {
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
                sensorManager.registerListener(this, gyro, SensorManager.SENSOR_DELAY_GAME)
                sensorManager.registerListener(this, heart, SensorManager.SENSOR_DELAY_NORMAL) //
                sensorManager.registerListener(this, acc, SensorManager.SENSOR_DELAY_GAME) //50Hz
            }else{
                sensorManager.unregisterListener(this)
            }
        }

        setupSendMessage()
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
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
            //データ送信
            makePushData(sensorVal, "acc")
        }

        if (event.sensor.type == Sensor.TYPE_GYROSCOPE) {
            //加速度センサデータを取得
            val sensorVal = event.values.clone()
            val x = sensorVal[0]
            val y = sensorVal[1]
            val z = sensorVal[2]
            val norm = sqrt(x*x + y*y + z*z)

            //数値を表示
            //binding.xView.text = "x:".plus(x)
            //binding.yView.text = "y:".plus(y)
            //binding.zView.text = "z:".plus(z)
            //データ送信
            makePushData(sensorVal, "gyroscope")
        }

        if(event.sensor.type == Sensor.TYPE_HEART_RATE){//TYPE_HEART_RATE
            //加速度センサデータを取得
            val sensorVal = event.values.clone()

            //数値を表示
            binding.healthView.text = sensorVal[0].toString()
            //データ送信
            makePushData(sensorVal, "heartRate")
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {

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
        Log.d("watch.requestSensorData","start")
        nodeSet.let {
            pickBestNodeId(it)?.let { nodeId ->
                val data = dataText.toByteArray(Charsets.UTF_8) //バイナリ変換
                Wearable.getMessageClient(applicationContext)
                    .sendMessage(nodeId, tag, data)
                    .apply {
                        addOnSuccessListener {
                            Log.d("watch.requestSensorData","success")
                        }
                        addOnFailureListener {
                            Log.d("watch.requestSensorData","defeat")
                        }
                    }
            }
        }
    }

    // 送信先として最適なノードを選択
    private fun pickBestNodeId(nodes: Set<Node>): String? {
        return nodes.firstOrNull { it.isNearby }?.id ?: nodes.firstOrNull()?.id
    }



    private fun dataItems(){
        val dataRequest = PutDataRequest.create("")
        val data = acceleration.queue
        val reader = FileReader("".plus("/").plus("filename").plus(".csv"))
        //dataRequest.setData()
    }
}