package com.b22706.smartwatch_test

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.b22706.smartwatch_test.fileUpload.ApiResult
import com.b22706.smartwatch_test.fileUpload.PostData
import java.io.File

class CsvListAdapter(val appContext: Context, private val csvFolderPath: String) : ListAdapter<String,CsvListAdapter.ViewHolder>(WordsComparator()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_block, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when(position){
            0->{
                val item = getItem(position)
                holder.button.text = item
                holder.button.setOnClickListener {
                    File(csvFolderPath).list()?.forEach {
                        File("$csvFolderPath/$it").delete().let{ bool ->
                            Log.d("onBindViewHolder",bool.toString())
                        }
                    }
                    File(csvFolderPath).list()?.let {
                        submitList(it.toList())
                    }

                }
            }
            else->{
                val item = getItem(position)
                holder.button.text = item
                holder.button.setOnClickListener {
                    val pushFile = File("$csvFolderPath/$item")
                    dataPost(holder, pushFile)
                }
            }
        }
        //Log.d("onBindViewHolder", item)
    }

    private fun dataPost(holder: ViewHolder, pushFile: File){
        val folderName = "ayato"
        val postData = PostData(pushFile.name, pushFile.path, folderName)
        postData.run(object : ApiResult {

            // アップロード完了時の処理
            @SuppressLint("SetTextI18n")
            override fun onSuccess(res: String) {
                holder.button.text = "success"
                holder.button.isClickable = false
                Log.d("onFileSelect.onSuccess",res)
            }

            // アップロード失敗時の処理
            @SuppressLint("SetTextI18n")
            override fun onError(res: String?) {
                holder.button.text = "defeat"
                holder.button.isClickable = false
                Log.d("onFileSelect.onError",res!!)
            }
        })
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val button: Button = view.findViewById(R.id.csvNameButton)

        override fun toString(): String {
            return super.toString() + " '" + button.text + "'"
        }
    }

    class WordsComparator : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }
    }
}