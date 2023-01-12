package com.b22706.smartwatch_test

import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.b22706.smartwatch_test.databinding.CsvListBinding
import java.io.File

class CsvListFragment: Fragment() {
    private lateinit var binding: CsvListBinding
    private lateinit var csvAdapter: CsvListAdapter

    private var columnCount = 1
    private var csvFolderPath = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val folder = File(requireActivity().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).toString(), "csv")
        csvFolderPath = folder.path
        csvAdapter = CsvListAdapter(requireContext(), csvFolderPath)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        //val view = inflater.inflate(R.layout.csv_list, container, false)
        binding = CsvListBinding.inflate(inflater, container, false)


        //Log.d("onCreateView", "check RecycleView")
        // Set the csvAdapter
        val recyclerView = binding.csvListView
        //Log.d("onCreateView", recyclerView.toString())
        with(recyclerView) {
            layoutManager = when {
                columnCount <= 1 -> LinearLayoutManager(context)
                else -> GridLayoutManager(context, columnCount)
            }
            adapter = csvAdapter
        }

        binding.reloadButton.setOnClickListener {
            //Log.d("CsvListFragment", "isClick")
            File(csvFolderPath).list()?.let {
                submitList(it.toList())
                //Log.d("CsvListFragment", it.toList().toString())
            }
        }

        return binding.root
    }

    private fun submitList(list: List<String>){
        csvAdapter.submitList(mutableListOf("csv削除").plus(list))
    }
}