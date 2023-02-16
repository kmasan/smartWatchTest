package com.b22706.smartwatch_test

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import java.io.FileWriter
import java.io.IOException
import java.util.*
import java.util.stream.IntStream.range
import kotlin.math.sqrt

class Acceleration {
    var queue: LinkedList<AccelerationData> = LinkedList() //キュー
    var deque:ArrayDeque<AccelerationData> = ArrayDeque(listOf())
    private val hz: Int = 50

    var csvRun = false

    data class AccelerationData(
        val time: Long,
        val x: Float,
        val y: Float,
        val z: Float
    )

    fun queueAdd(time: Long, x: Float,y: Float,z: Float){
        //キューへのデータ追加
        val norm = sqrt(x*x + y*y + z*z)
        queue.add(AccelerationData(time, x, y, z))
        deque.add(AccelerationData(time, x, y, z))
    }

    fun queueReset(){
        queue = LinkedList()
        deque.clear()
    }

    private fun rolling(inList:MutableList<Float>):MutableList<Float>{
        //移動平均フィルタ
        val window = 10
        val list:MutableList<Float> = mutableListOf()

        for(i in range(0,inList.size)){
            if(i < window) continue

            var sum = 0F
            for(j in range(i-window,i)){
                sum += inList[j]
            }
            list.add(sum/window)
        }
        return list
    }

    fun csvWriter(path: String, fileName: String): Boolean {
        //CSVファイルの書き出し
        try{
            //書込み先指定
            val writer = FileWriter("${path}/${fileName}-acc.csv")

            //書き込み準備
            val csvPrinter = CSVPrinter(
                writer, CSVFormat.DEFAULT
                    .withHeader(
                        "time",
                        "x",
                        "y",
                        "z"
                    )
            )
            //書き込み開始
            for(data in queue){
                //データ保存
                csvPrinter.printRecord(
                    data.time.toString(),
                    data.x.toString(),
                    data.y.toString(),
                    data.z.toString()
                )
            }
            //データ保存の終了処理
            csvPrinter.flush()
            csvPrinter.close()
            return true
        }catch (e: IOException){
            //エラー処理d
            Log.d("csvWrite", "${e}:${e.message!!}")
            return false
        }
    }

    fun csvWriterDeque(path: String, fileName: String): Boolean {
        //CSVファイルの書き出し
        try{
            //書込み先指定
            val writer = FileWriter("${path}/${fileName}-acc.csv")

            //書き込み準備
            val csvPrinter = CSVPrinter(
                writer, CSVFormat.DEFAULT
                    .withHeader(
                        "time",
                        "x",
                        "y",
                        "z"
                    )
            )
            val hnd = Handler(Looper.getMainLooper())
            deque.clear()
            csvRun = true
            // こいつ(rnb0) が何回も呼ばれる
            val rnb = object : Runnable {
                override fun run() {
                    val dequeClone = deque.clone()
                    //書き込み開始
                    for(data in dequeClone){
                        //データ保存
                        csvPrinter.printRecord(
                            data.time.toString(),
                            data.x.toString(),
                            data.y.toString(),
                            data.z.toString()
                        )
                    }
                    deque.clear()

                    // stop用のフラグ
                    when(csvRun) {
                        true -> {
                            // 指定時間後に自分自身を呼ぶ
                            hnd.postDelayed(this, 1000)
                        }
                        false -> {
                            //データ保存の終了処理
                            csvPrinter.flush()
                            csvPrinter.close()
                        }
                    }
                }
            }
            // 初回の呼び出し
            hnd.post(rnb)
            return true
        }catch (e: IOException){
            //エラー処理d
            Log.d("csvWrite", "${e}:${e.message!!}")
            return false
        }
    }
}

