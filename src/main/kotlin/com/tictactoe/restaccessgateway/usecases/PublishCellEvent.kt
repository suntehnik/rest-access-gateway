package com.tictactoe.restaccessgateway.usecases

import com.tictactoe.restaccessgateway.domain.Cell

interface PublishCellEvent {
    fun publishNewCellEvent(cell: Cell, cellId: String): String
}