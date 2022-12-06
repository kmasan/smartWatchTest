package com.b22706.smartwatch_test.fileUpload

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.b22706.smartwatch_test.databinding.FragmentFileUploadBinding
import com.b22706.smartwatch_test.fileUpload.UriToFileUtil.toFile
import java.io.File

class FileSelector(private val appContext: Context, val binding: FragmentFileUploadBinding) : Activity(),FileSelectionDialog.OnFileSelectListener {
    companion object{
        private const val MENU_ID_FILE = 0     // オプションメニューID
    }
    private lateinit var m_strInitialDir: File       // 初期フォルダ

    // ファイルを選択するダイアログを表示する
    fun selectFile() {
        //外部ストレージ　ルートフォルダパス取得
        val externalFilesDirs =
            appContext.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)!!.path.split("/")
        var externalFilesDirsPath = ""
        var i = 0
        externalFilesDirs.forEach { externalFilesDirsPeart ->
            if (i > 3) {
                return@forEach
            }
            if (externalFilesDirsPeart != "") {
                externalFilesDirsPath = "$externalFilesDirsPath/$externalFilesDirsPeart"
            }

            i++
        }

        //外部ストレージ　ダウンロードフォルダパスを初期フォルダとして変数に保存
        m_strInitialDir = File(externalFilesDirsPath)

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            // ファイル選択ダイアログ表示　Android 9.0　(API 28)　以下の場合の処理
            // アプリが minSdkVersion 26　なのでそれ以下の端末処置は考慮していない
            val dlg = FileSelectionDialog(appContext, this)
            dlg.show(m_strInitialDir)
        } else {
            // ファイル選択Activity表示　Android 9.0　(API 28)　を超えるの場合の処理
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.type = "*/*"
            appContext.startActivity(intent)
        }
    }


    // Android 9.0　(API 28) 以下でファイルが選択されたときに呼び出される関数
    @SuppressLint("SetTextI18n")
    override fun onFileSelect(file: File?) {
        Toast.makeText(
            appContext,
            "API:" + Build.VERSION.SDK_INT + " ファイルが選択されました。\n : " + file!!.path,
            Toast.LENGTH_LONG
        ).show()

        // ファイルパスを指定してアップロードを実行
        binding.responseView.text = "Uploading..."
        val folderName = "ayato"//folderName.text.toString()
        val postData = PostData(file.name, file.path, folderName)
        postData.run(callback = object : ApiResult {

            // アップロード完了時の処理
            override fun onSuccess(res: String) {
                binding.responseView.text = res
                Log.d("onFileSelect.onSuccess",res)
            }

            // アップロード失敗時の処理
            override fun onError(res: String?) {
                binding.responseView.text = res
                Log.d("onFileSelect.onError",res!!)
            }
        })
    }

    //Android 9.0　(API 28)　を超える端末でファイルが選択されたときに呼び出される関数
    @SuppressLint("SetTextI18n")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == AppCompatActivity.RESULT_OK) {
            when (requestCode) {
                MENU_ID_FILE -> {
                    // ファイル名を取得
                    var selectFileName = "ファイル名を取得できませんでした"
                    // ファイルURIを取得
                    val uri: Uri = data?.data!!
                    // ファイルURIをファイルパスに変換
                    val filePath: String = toFile(this, uri).path
                    data.data?.let { selectFileUri ->
                        contentResolver.query(selectFileUri, null, null, null, null)
                    }?.use { cursor ->
                        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        //val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                        cursor.moveToFirst()
                        selectFileName = cursor.getString(nameIndex)
                    }

                    // トーストの表示
                    Toast.makeText(
                        this,
                        "API:" + Build.VERSION.SDK_INT + " ファイルが選択されました。\n : " + selectFileName,
                        Toast.LENGTH_LONG
                    ).show()

                    // ファイルパスを指定してアップロードを実行
                    binding.responseView.text = "Uploading..."
                    val folderName = "ayato"//binding.folderName.text.toString()
                    val postData = PostData(selectFileName, filePath, folderName)
                    postData.run(object : ApiResult {

                        // アップロード完了時の処理
                        override fun onSuccess(res: String) {
                            binding.responseView.text = res
                            Log.d("onFileSelect.onSuccess",res)
                        }

                        // アップロード失敗時の処理
                        override fun onError(res: String?) {
                            binding.responseView.text = res
                            Log.d("onFileSelect.onError",res!!)
                        }
                    })
                }
                else -> {/*何もしない*/
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}