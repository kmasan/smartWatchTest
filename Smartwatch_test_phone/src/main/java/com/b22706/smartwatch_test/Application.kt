package com.b22706.smartwatch_test

import android.app.Application
import android.os.Build
import android.os.Environment
import java.io.File

class Application: Application() {
    var internalPath = ""
    var externalPath = ""
    var csvFolderPath = ""

    override fun onCreate() {
        super.onCreate()
        internalPath = internalStoragePath()
        externalPath = externalStoragePath()
        val folder = File(externalPath, "csv")
        folder.mkdirs()
        csvFolderPath = folder.path
    }

    private fun internalStoragePath(): String {
        return  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){//Android 10　以上か
            //内部ストレージ(/data/user/0/パッケージ名/files)
            this.applicationContext.filesDir.path
        }else{
            //（/storage/emulated/0）
            Environment.getExternalStorageDirectory().absolutePath
        }
    }

    private fun externalStoragePath(): String{
        return  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){//Android 10　以上か
            //（/storage/emulated/0/Android/data/パッケージ名/files）
            //this.getExternalFilesDir(null).toString()
            //（/storage/emulated/0/Android/data/パッケージ名/files/Documents）
            this.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).toString()
        }else{
            //（/storage/emulated/0）
            Environment.getExternalStorageDirectory().absolutePath
        }
    }
}