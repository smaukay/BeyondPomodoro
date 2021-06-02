package com.example.beyondpomodoro

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
import androidx.recyclerview.widget.RecyclerView
import com.example.beyondpomodoro.sessiontype.SessionList
import com.example.beyondpomodoro.sessiontype.SessionType

/**
 * A fragment representing a list of Items.
 */
class SessionInfoFragment : Fragment() {

    private var columnCount = 1

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

                // get the last activity type on activity creation and store in sharedData
                activity?.getPreferences(Context.MODE_PRIVATE)?.let { prefs ->
                    val settingsSet = if (prefs.contains("sessionList")) {
                        val default: Set<String> = setOf()
                        prefs.getString("sessionList", "")?.let {
                            val sessionNames = it.split("<SESNAME>")
                            sessionNames.map { sname ->
                                prefs.getString(sname, sname).orEmpty()
                            }
                        }?: run {
                            listOf()
                        }
                    } else {
                        listOf()
                    }

                    println("DEBUG: $settingsSet")

                    // now collect the info for this session id
                    val sessions = settingsSet.mapIndexed { num, sessionId ->
                        val title = prefs.getString("$sessionId", "")
                        val onTime = prefs.getInt("pomodoroTimeFor$sessionId", 25).toUInt()
                        val offTime = prefs.getInt("breakTimeFor$sessionId", 5).toUInt()
                        val tags = prefs.getString("tagsFor$sessionId", "")
                        println("DEBUG: $sessionId, $onTime, $offTime")
                        title?.let {
                            tags?.let {
                                SessionType(num.toUInt(), sessionId, title, onTime, offTime, tags)
                            }
                        }
                    }

                    adapter = MySessionInfoRecyclerViewAdapter(SessionList(sessions).items) {
                        context.toast("${it.id}, ${it.title}")

                        // save the title to sharedprefs
                        prefs.edit().apply {
                            putString(it.id, it.title)
                            apply()
                        }
                    };

                }

            }
        }
        return view
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