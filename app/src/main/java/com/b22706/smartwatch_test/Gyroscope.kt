package com.b22706.smartwatch_test

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import java.io.FileWriter
import java.io.IOException
import java.util.*
import java.util.stream.IntStream.range
import kotlin.math.sqrt

class Gyroscope {
    var queue: LinkedList<AccelerationData> = LinkedList() //キュー

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
    }

    fun queueReset(){
        queue = LinkedList()
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
            val writer = FileWriter("$path/$fileName-gyro.csv")

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
            //エラー処理
            return false
        }
    }
}

