package com.tictactoe.restaccessgateway.usecases

import com.tictactoe.restaccessgateway.domain.Cell

interface SaveToPersistentStorage {
    fun daveToDatabase(cell: Cell): String
}