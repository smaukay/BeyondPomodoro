package com.example.beyondpomodoro

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
import androidx.recyclerview.widget.RecyclerView
import com.example.beyondpomodoro.sessiontype.SessionList
import com.example.beyondpomodoro.sessiontype.SessionType
import kotlinx.coroutines.launch


/**
 * A fragment representing a list of Items.
 */
class SessionInfoFragment : Fragment() {

    private var columnCount = 1
    private var sessions: List<SessionType>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            columnCount = it.getInt(ARG_COLUMN_COUNT)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_session_info_list, container, false)

        // Set the adapter
        if (view is RecyclerView) {
            with(view) {

                addItemDecoration(DividerItemDecoration(context, VERTICAL))

                layoutManager = when {
                    columnCount <= 1 -> LinearLayoutManager(context)
                    else -> GridLayoutManager(context, columnCount)
                }

                // fetch sessions from database
                lifecycleScope.launch {
                    sessions = (activity as MainActivity).db.sessionDao().getSessions().mapIndexed {idx, e ->
                        SessionType(idx.toUInt(),
                            e.sid.toString(),
                            "",
                            e.sessionTime?.toUInt()?.div(60u)?: run {25u},
                            e.breakTime?.toUInt()?.div(60u)?: run {5u},
                            e.tags?.toList()?: run{ listOf<String>()})
                    }

                    adapter = MySessionInfoRecyclerViewAdapter(SessionList(sessions!!).items) {
                        context.toast("${it.id}, ${it.title}")
                    };
                }
            }
        }
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        println("DEBUG: destorying recycler view... ")
        println("DEBUG: $sessions")
    }

    companion object {

        // TODO: Customize parameter argument names
        const val ARG_COLUMN_COUNT = "column-count"

        // TODO: Customize parameter initialization
        @JvmStatic
        fun newInstance(columnCount: Int) =
            SessionInfoFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_COLUMN_COUNT, columnCount)
                }
            }
    }
}