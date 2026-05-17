package app.task2.services.interfaces

import app.task2.entities.History

interface HistoryInterface {
    fun save(history: History): History
}

