package com.b22706.smartwatch_test

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.b22706.smartwatch_test.databinding.ActivityMainBinding
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import kotlin.math.sqrt

class MainActivity : AppCompatActivity(), SensorEventListener, MessageClient.OnMessageReceivedListener {
    private lateinit var app: Application
    private lateinit var binding: ActivityMainBinding
    private lateinit var sensorManager: SensorManager

    //センサデータ保管用
    private val acceleration: Acceleration = Acceleration()
    private val watchAcceleration: WatchAcceleration = WatchAcceleration()
    private val gyroscope: Gyroscope = Gyroscope()
    private val watchGyroscope: WatchGyroscope = WatchGyroscope()
    private val heartRate: HeartRate = HeartRate()

    private var fileName = "smartWatch"
    private var csvAdd: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        app = application as Application
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //外部ストレージへの読み込みが許可されているか
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // 許可されていないので許可ダイアログを表示する
            requestPermissions(
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                100
            )
        }
        //外部ストレージへの書き込みが許可されているか
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // 許可されていないので許可ダイアログを表示する
            requestPermissions(
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                100
            )
        }

        // ウォッチからのデータ受信準備
        Wearable.getMessageClient(applicationContext).addListener(this)

        // ボタンが押されたらcsvを出力
        binding.csvWriteButton.setOnClickListener {
            if(csvAdd){
                csvWrite()
                binding.csvWriteButton.text = "csv入力"
                csvAdd = false
            }else{
                fileName = binding.csvFileName.text.toString()
                acceleration.queueReset()
                watchAcceleration.queueReset()
                gyroscope.queueReset()
                watchGyroscope.queueReset()
                heartRate.queueReset()
                binding.csvWriteButton.text = "csv出力"
                csvAdd = true
            }
        }


    }

    override fun onResume() {
        super.onResume()
        // センサを設定
        val acc = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
        val gyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        sensorManager.registerListener(this, acc, SensorManager.SENSOR_DELAY_GAME)
        sensorManager.registerListener(this, gyro, SensorManager.SENSOR_DELAY_GAME)
    }

    override fun onPause() {
        super.onPause()
        // センサの設定解除
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if(event.sensor.type == Sensor.TYPE_LINEAR_ACCELERATION){
            //加速度センサデータを取得
            val sensorVal = event.values.clone()
            val x = sensorVal[0]
            val y = sensorVal[1]
            val z = sensorVal[2]
            val norm = sqrt(x*x + y*y + z*z)

            //数値を表示
            binding.accXView.text = "x:".plus(x)
            binding.accYView.text = "y:".plus(y)
            binding.accZView.text = "z:".plus(z)

            //データ追加
            if(csvAdd) acceleration.queueAdd(System.currentTimeMillis(),x,y,z)
        }

        if(event.sensor.type == Sensor.TYPE_GYROSCOPE){
            //加速度センサデータを取得
            val sensorVal = event.values.clone()
            val x = sensorVal[0]
            val y = sensorVal[1]
            val z = sensorVal[2]
            val norm = sqrt(x*x + y*y + z*z)

            //数値を表示
            binding.gyroXView.text = "x:".plus(x)
            binding.gyroYView.text = "y:".plus(y)
            binding.gyroZView.text = "z:".plus(z)

            //データ追加
            if(csvAdd) gyroscope.queueAdd(System.currentTimeMillis(),x,y,z)
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {

    }

    // 受信したメッセージからデータを整理し処理
    override fun onMessageReceived(messageEvent: MessageEvent) {
        Log.d("phone.onMessageReceived", "get")
        when(messageEvent.path) {
            // tag別に処理を変える
            "acc" -> {
                // 加速度センサ
                val data = messageEvent.data.toString(Charsets.UTF_8) //文字列に変換
                //受け取ったデータmsgはコンマ区切りのcsv形式なので、value[]にそれぞれ格納します。
                val value = data.split(",").dropLastWhile { it.isEmpty() }.toTypedArray()
                val time = value[0].toLong()
                val x = value[1].toFloat()
                val y = value[2].toFloat()
                val z = value[3].toFloat()

                //数値を表示
                binding.sAccXView.text = "x:".plus(x)
                binding.sAccYView.text = "y:".plus(y)
                binding.sAccZView.text = "z:".plus(z)

                //データ追加
                if(csvAdd) watchAcceleration.queueAdd(time,x,y,z)
            }
            "gyroscope" -> {
                // 加速度センサ
                val data = messageEvent.data.toString(Charsets.UTF_8) //文字列に変換
                //受け取ったデータmsgはコンマ区切りのcsv形式なので、value[]にそれぞれ格納します。
                val value = data.split(",").dropLastWhile { it.isEmpty() }.toTypedArray()
                val time = value[0].toLong()
                val x = value[1].toFloat()
                val y = value[2].toFloat()
                val z = value[3].toFloat()

                //数値を表示
                //binding.sAccXView.text = "x:".plus(x)
                //binding.sAccYView.text = "y:".plus(y)
                //binding.sAccZView.text = "z:".plus(z)

                //データ追加
                if(csvAdd) watchGyroscope.queueAdd(time,x,y,z)
            }
            "heartRate" ->{
                // 心拍センサ
                val data = messageEvent.data.toString(Charsets.UTF_8) //文字列に変換
                //受け取ったデータmsgはコンマ区切りのcsv形式なので、value[]にそれぞれ格納します。
                val value = data.split(",").dropLastWhile { it.isEmpty() }.toTypedArray()
                val time = value[0].toLong()
                val heartRateData = value[1].toFloat()

                //数値を表示
                binding.heartView.text = heartRateData.toString()

                //データ追加
                if(csvAdd) heartRate.queueAdd(time,heartRateData)
            }
        }
    }

    private fun csvWrite(){
        Log.d("csvWrite","write")
        // csvを出力
        acceleration.csvWriter(fileName,app.csvFolderPath).let { if(!it) Log.d("csvWrite","defeat") }
        gyroscope.csvWriter(fileName,app.csvFolderPath).let { if(!it) Log.d("csvWrite","defeat") }
        watchAcceleration.csvWriter(fileName,app.csvFolderPath).let { if(!it) Log.d("csvWrite","defeat") }
        watchGyroscope.csvWriter(fileName,app.csvFolderPath).let { if(!it) Log.d("csvWrite","defeat") }
        heartRate.csvWriter(fileName,app.csvFolderPath).let { if(!it) Log.d("csvWrite","defeat") }
    }
}