package com.example.visionapp.communiates

import com.example.visionapp.CommunicateConfig
import java.util.PriorityQueue
import kotlinx.coroutines.*

object CommunicateQueue {

    private val queue: PriorityQueue<Communicate> = PriorityQueue()

    private val cleanerJob = CoroutineScope(Dispatchers.Default).launch {
        while (true) {
            delay(CommunicateConfig.CLEANUP_CHECK_INTERVAL_MS)
            removeOldMessages()
        }
    }

    fun add(communicate: Communicate, removeCommunicatesWithSameCategory: Boolean = false) {
        if(removeCommunicatesWithSameCategory){
            removeByCategory(communicate.communicateType.category)
        }
        queue.add(communicate)
    }

    fun poll(): Communicate? {
        return queue.poll()
    }

    fun peek(): Communicate? {
        return queue.peek()
    }

    fun size(): Int {
        return queue.size
    }

    fun allElements(): List<Communicate> {
        return queue.toList()
    }

    fun filterByCategory(category: CommunicateCategory): List<Communicate> {
        return queue.filter { it.communicateType.category == category }
    }


    fun stopCleaner() {
        cleanerJob.cancel()
    }

    private fun removeOldMessages() {
        if(size() == 0){
            return
        }
        val currentTime = System.currentTimeMillis()
        queue.removeIf { currentTime - it.timestamp > CommunicateConfig.COMMUNICATE_MAX_AGE_MS }
    }

    private fun removeByCategory(category: CommunicateCategory) {
        queue.removeIf { it.communicateType.category == category }
    }
}
