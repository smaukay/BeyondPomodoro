package com.example.beyondpomodoro.placeholder

import java.util.*

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 *
 * TODO: Replace all uses of this class before publishing your app.
 */
object SessionType {

    /**
     * An array of sample (placeholder) items.
     */
    val ITEMS: MutableList<SessionType> = ArrayList()

    /**
     * A map of sample (placeholder) items, by ID.
     */
    val ITEM_MAP: MutableMap<UInt, SessionType> = HashMap()

    private val COUNT = 25

    init {
        // Add some sample items.
        for (i in 1..COUNT) {
            addItem(createSessionType(i))
        }
    }

    private fun addItem(item: SessionType) {
        ITEMS.add(item)
        ITEM_MAP.put(item.id, item)
    }

    private fun createSessionType(position: Int): SessionType {
        return SessionType(position.toUInt(), position.toString(), (20u..25u).random(), (5u..10u).random(), "tag1, tag2, tag3")
    }

    private fun makeDetails(position: Int): String {
        val builder = StringBuilder()
        builder.append("Details about Item: ").append(position)
        for (i in 0..position - 1) {
            builder.append("\nMore details information here.")
        }
        return builder.toString()
    }

    /**
     * A placeholder item representing a piece of content.
     */
    data class SessionType(val id: UInt, val title: String, val onTime: UInt, val offTime: UInt, val tags: String) {
        override fun toString(): String = "${id.toString()}, $title, ${onTime.toString()}, ${offTime.toString()}. $tags"
    }
}