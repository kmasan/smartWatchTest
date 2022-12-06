package com.b22706.smartwatch_test.fileUpload

import android.app.AlertDialog
import android.app.AlertDialog.Builder
import android.content.Context
import android.graphics.Color
import android.os.Environment
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import com.b22706.smartwatch_test.R
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

class FileSelectionDialog(context: Context, listener: OnFileSelectListener) : OnItemClickListener {
    private val m_parent: Context // 呼び出し元
    private val m_listener: OnFileSelectListener // 結果受取先
    private var m_dlg: AlertDialog? = null // ダイアログ
    private var m_fileinfoarrayadapter: FileInfoArrayAdapter? = null // ファイル情報配列アダプタ

    //ダイアログの作成と表示　Android 9.0　(API 28)　以下用　minSdkVersion 26　なのでそれ以下の端末処置は考慮していない
    fun show(fileDirectory: File) {
        //外部ストレージのパブリックフォルダールートパスを取得
        var externalFilesDirs =
            m_parent.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)!!.path.split("/")
        var publicDirectoryRootPath = ""
        var i = 0
        externalFilesDirs.forEach { externalFilesDirsPeart ->
            if (i > 3) {
                return@forEach
            }
            if (externalFilesDirsPeart != "") {
                publicDirectoryRootPath = publicDirectoryRootPath + "/" + externalFilesDirsPeart
            }

            i++
        }

        // タイトル
        var strTitle: String = fileDirectory.getAbsolutePath()

        // リストビュー
        val listview = ListView(m_parent)
        listview.setScrollingCacheEnabled(false)
        listview.setOnItemClickListener(this)

        val aFile: Array<File>
        try {
            // ファイルリスト
            aFile = fileDirectory.listFiles()!!
        } catch (e: Exception) {
            return
        }

        val listFileInfo: MutableList<FileInfo> =
            ArrayList()
        if (null != aFile) {
            for (fileTemp in aFile) {
                listFileInfo.add(FileInfo(fileTemp.getName(), fileTemp))
            }
            Collections.sort(listFileInfo)
        }
        //親フォルダに戻るパスの追加　rootパスの場合は親フォルダーに移動できないので追加しない
        if (null != fileDirectory.getParent() && strTitle != publicDirectoryRootPath) {
            listFileInfo.add(0, FileInfo("..", File(fileDirectory.getParent())))
        }
        m_fileinfoarrayadapter = FileInfoArrayAdapter(m_parent, listFileInfo)
        listview.setAdapter(m_fileinfoarrayadapter)

        val builder = Builder(m_parent)
        builder.setTitle(strTitle)
        builder.setPositiveButton("Cancel", null)
        builder.setView(listview)
        builder.setIcon(R.mipmap.ic_launcher)
        m_dlg = builder.show()
    }

    //ListView内の項目をクリックしたときの処理
    override fun onItemClick(l: AdapterView<*>?, v: View?, position: Int, id: Long) {
        if (null != m_dlg) {
            m_dlg = m_dlg as AlertDialog
            m_dlg!!.dismiss()
            m_dlg = null
        }
        val fileinfo =
            m_fileinfoarrayadapter!!.getItem(position)
        if (true == fileinfo.file.isDirectory) {
            show(fileinfo.file)
        } else {
            // ファイルが選ばれた：リスナーのハンドラを呼び出す
            m_listener.onFileSelect(fileinfo.file)
        }
    }

    //選択したファイルの情報を取り出すためのリスナーインターフェース
    interface OnFileSelectListener {
        // ファイルが選択されたときに呼び出される関数
        fun onFileSelect(file: File?)
    }

    //コンストラクタ
    init {
        m_parent = context
        m_listener = listener
    }

    //ファイル選択ダイアログに表示すｔるファイル情報保持するクラス
    inner class FileInfo(val name: String, file: File) : Comparable<FileInfo?> {
        private val m_file: File // ファイルオブジェクト

        val file: File
            get() = m_file

        //表示並び順設定
        override fun compareTo(another: FileInfo?): Int {
            // ディレクトリ < ファイル の順
            if (true == m_file.isDirectory() && false == another?.file!!.isDirectory()) {
                return -1
            }
            return if (false == m_file.isDirectory() && true == another!!.file.isDirectory()) {
                1
            } else m_file.getName().toLowerCase().compareTo(another!!.file.getName().toLowerCase())

            // ファイル同士、ディレクトリ同士の場合は、ファイル名（ディレクトリ名）の大文字小文字区別しない辞書順
        }

        // コンストラクタ
        init {
            m_file = file
        }
    }

    //ListView用のアダプターをinner classとして定義
    inner class FileInfoArrayAdapter(
        context: Context?,
        private val m_listFileInfo: List<FileInfo>
    ) : ArrayAdapter<FileInfo>(context!!, -1, m_listFileInfo) {
        // m_listFileInfoの一要素の取得
        override fun getItem(position: Int): FileInfo {
            return m_listFileInfo[position]
        }

        // 一要素のビューの生成
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            // レイアウトの生成
            var convertView: View? = convertView
            if (null == convertView) {
                val context: Context = context
                // レイアウト
                val layout = LinearLayout(context)
                layout.setPadding(10, 10, 10, 10)
                layout.setBackgroundColor(Color.WHITE)
                convertView = layout
                // テキスト
                val textview = TextView(context)
                textview.tag = "text"
                textview.setTextColor(Color.BLACK)
                textview.setPadding(10, 10, 10, 10)
                layout.addView(textview)
            }

            // 値の指定
            val fileinfo =
                m_listFileInfo[position]
            val textview = convertView.findViewWithTag("text") as TextView
            if (fileinfo.file.isDirectory) { // ディレクトリの場合は、名前の後ろに「/」を付ける
                textview.text = fileinfo.name + "/"
            } else {
                textview.text = fileinfo.name
            }
            return convertView
        }
    }
}