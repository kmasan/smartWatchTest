package com.b22706.smartwatch_test

import android.os.Handler
import android.os.Looper
import android.util.Log
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import java.io.FileWriter
import java.io.IOException
import java.util.*
import java.util.stream.IntStream.range
import kotlin.math.sqrt

class HeartRate {
    var queue: LinkedList<HeartData> = LinkedList() //キュー
    var deque:ArrayDeque<HeartData> = ArrayDeque(listOf())

    var csvRun = false

    data class HeartData(
        val time: Long,
        val heart: Float
    )

    fun queueAdd(time: Long, heart: Float){
        //キューへのデータ追加
        queue.add(HeartData(time, heart))
    }

    fun queueReset(){
        queue = LinkedList()
    }

    fun csvWriter(path: String, fileName: String): Boolean {
        //CSVファイルの書き出し
        try{
            //書込み先指定
            val writer = FileWriter("$path/$fileName-heart.csv")

            //書き込み準備
            val csvPrinter = CSVPrinter(
                writer, CSVFormat.DEFAULT
                    .withHeader(
                        "time",
                        "heart"
                    )
            )
            //書き込み開始
            for(data in queue){
                //データ保存
                csvPrinter.printRecord(
                    data.time.toString(),
                    data.heart.toString()
                )
            }
            //データ保存の終了処理
            csvPrinter.flush()
            csvPrinter.close()
            return true
        }catch (e: IOException){
            //エラー処理
            return false
        }
    }

    fun csvWriterDeque(path: String, fileName: String): Boolean {
        //CSVファイルの書き出し
        try{
            //書込み先指定
            val writer = FileWriter("${path}/${fileName}-heart.csv")

            //書き込み準備
            val csvPrinter = CSVPrinter(
                writer, CSVFormat.DEFAULT
                    .withHeader(
                        "time",
                        "heart"
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
                            data.heart.toString()
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

