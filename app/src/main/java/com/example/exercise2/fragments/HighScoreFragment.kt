package com.example.exercise2.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.exercise2.R
import com.example.exercise2.interfaces.Callback_HighScoreClicked
import com.example.exercise2.ui.RecordAdapter
import com.example.exercise2.utilities.RecordsManager
import androidx.recyclerview.widget.DividerItemDecoration

class HighScoreFragment : Fragment() {

    var callbackHighScoreClicked: Callback_HighScoreClicked? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_high_score, container, false)

        val rv = view.findViewById<RecyclerView>(R.id.highscore_RV_records)
        rv.layoutManager = LinearLayoutManager(context)
        rv.addItemDecoration(DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL))
        val records = RecordsManager.getTop10()

        rv.adapter = RecordAdapter(records) { record ->
            callbackHighScoreClicked?.zoom(record.lat, record.lon)
        }

        return view
    }
}
