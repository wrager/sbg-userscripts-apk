package com.github.wrager.sbgscout.script.model

@JvmInline
value class ScriptVersion(val value: String) : Comparable<ScriptVersion> {

    override fun compareTo(other: ScriptVersion): Int {
        val thisSegments = value.split(".").map { it.toIntOrNull() ?: 0 }
        val otherSegments = other.value.split(".").map { it.toIntOrNull() ?: 0 }
        val maxLength = maxOf(thisSegments.size, otherSegments.size)

        for (index in 0 until maxLength) {
            val thisSegment = thisSegments.getOrElse(index) { 0 }
            val otherSegment = otherSegments.getOrElse(index) { 0 }
            if (thisSegment != otherSegment) return thisSegment.compareTo(otherSegment)
        }

        return 0
    }
}
