package com.b22706.smartwatch_test

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.b22706.smartwatch_test.databinding.FragmentFileUploadBinding
import com.b22706.smartwatch_test.fileUpload.ApiResult
import com.b22706.smartwatch_test.fileUpload.FileSelector
import com.b22706.smartwatch_test.fileUpload.PostData
import java.io.File

class FileUploadFragment: Fragment() {
    companion object {
        @JvmStatic
        fun newInstance(path: String) =
            FileUploadFragment().apply {
                arguments = Bundle().apply {
                    csvFolderPath = path
                }
            }
    }
    private var _binding: FragmentFileUploadBinding? = null
    private val binding get() = _binding!!
    lateinit var fileSelector: FileSelector
    private var csvFolderPath = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFileUploadBinding.inflate(inflater, container, false)
        fileSelector = FileSelector(requireContext(), binding)
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val folder = File(requireActivity().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).toString(), "csv")
        csvFolderPath = folder.path

        // 権限の取得ボタンが押された時の処理
        binding.getPermissionButton.setOnClickListener {
            val intent = Intent("android.settings.MANAGE_ALL_FILES_ACCESS_PERMISSION")
            startActivity(intent)
        }

        // UPLOADボタンが押された時の処理
        binding.ulButton.setOnClickListener {
            //fileSelector.selectFile()
            val pushFileName = ""//binding.sendFileNameText.text
            val pushFile = File("$csvFolderPath/$pushFileName.csv")

            binding.responseView.text = "Uploading..."
            //dataPost(pushFile)

            File(csvFolderPath).walk().forEach {
                if(it.isFile){
                    Log.d("dataPost",it.path)
                    dataPost(it)
                }
            }

            binding.responseView.text = "finish"
        }
    }

    private fun dataPost(pushFile: File){
        val folderName = "ayato"//binding.folderName.text.toString()
        val postData = PostData(pushFile.name, pushFile.path, folderName)
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
}