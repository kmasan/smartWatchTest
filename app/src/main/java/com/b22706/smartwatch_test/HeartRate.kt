package com.b22706.smartwatch_test

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import java.io.FileWriter
import java.io.IOException
import java.util.*
import java.util.stream.IntStream.range
import kotlin.math.sqrt

class HeartRate {
    var queue: LinkedList<HeartData> = LinkedList() //キュー

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
}

