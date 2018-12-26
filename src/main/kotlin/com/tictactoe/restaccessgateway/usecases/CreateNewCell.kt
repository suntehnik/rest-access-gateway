package com.tictactoe.restaccessgateway.usecases

import com.tictactoe.restaccessgateway.domain.Cell
import com.tictactoe.restaccessgateway.domain.CellKind

interface CreateNewCell {
    fun createCell(kind: CellKind, x: Int, y: Int): Cell
}