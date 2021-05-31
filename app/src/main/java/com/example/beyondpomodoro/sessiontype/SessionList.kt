package com.example.beyondpomodoro.sessiontype

import java.util.*

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 */
class SessionList(sessions: List<SessionType?>) {

    /**
     * An array of sample (placeholder) items.
     */
    val items: MutableList<SessionType> = ArrayList()

    /**
     * A map of sample (placeholder) items, by ID.
     */
    val itemMap: MutableMap<UInt, SessionType> = HashMap()

    private val COUNT = 25

    init {
        // Add some sample items.
        sessions.forEach {session ->
            session?.let {
                addItem(it)
            }
        }
    }

    private fun addItem(item: SessionType) {
        items.add(item)
        itemMap[item.id] = item
    }

    private fun createSessionType(
        position: Int,
        title: String,
        onTime: UInt,
        offTime: UInt,
        tags: String
    ): SessionType {
        return SessionType(position.toUInt(), title, onTime, offTime, tags)
    }

    private fun makeDetails(position: Int): String {
        val builder = StringBuilder()
        builder.append("Details about Item: ").append(position)
        for (i in 0..position - 1) {
            builder.append("\nMore details information here.")
        }
        return builder.toString()
    }
}

/**
 * A placeholder item representing a piece of content.
 */
data class SessionType(
    val id: UInt,
    val title: String,
    val onTime: UInt,
    val offTime: UInt,
    val tags: String
) {
    override fun toString(): String =
        "$id, $title, $onTime, $offTime. $tags"
}