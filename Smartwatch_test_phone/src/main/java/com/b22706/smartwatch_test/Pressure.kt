package com.b22706.smartwatch_test

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import java.io.FileWriter
import java.io.IOException
import java.util.*
import java.util.stream.IntStream.range
import kotlin.math.sqrt

class Pressure {
    private var queue: LinkedList<PressureData> = LinkedList() //キュー
    private val hz: Int = 50

    data class PressureData(
        val time: Long,
        val pressure: Float
    )

    fun queueAdd(time: Long, data: Float){
        //キューへのデータ追加
        queue.add(PressureData(time, data))
    }

    fun queueReset(){
        queue = LinkedList()
    }

    fun csvWriter(fileName: String, path: String): Boolean {
        //CSVファイルの書き出し
        try{
            //書込み先指定
            val writer = FileWriter(path.plus("/").plus(fileName).plus("_pressure.csv"))

            //書き込み準備
            val csvPrinter = CSVPrinter(
                writer, CSVFormat.DEFAULT
                    .withHeader(
                        "time",
                        "heartRate"
                    )
            )
            //書き込み開始
            for(data in queue){
                //データ保存
                csvPrinter.printRecord(
                    data.time,
                    data.pressure
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

