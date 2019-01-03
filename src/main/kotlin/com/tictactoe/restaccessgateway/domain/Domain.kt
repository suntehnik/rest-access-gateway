@file:Suppress("unused")

package com.tictactoe.restaccessgateway.domain

import com.tictactoe.proto.TicTacToeProto

class TicTacToeResponse(val id: String, val code: Int, val error: TicTacToeError?, val fieldConfiguration: FieldConfiguration?)

class Cell(val kind: CellKind, val x: Long, val y: Long)

class FieldConfiguration {
    val cells: MutableList<Cell> = mutableListOf()
}

class TicTacToeError(val message: String, val code: Int)

enum class CellKind(val kind: Int) {
    X(0), O(1);

    companion object {
        fun valueOf(kind: Int): CellKind {
            return when (kind) {
                0 -> O
                1 -> X
                else -> throw IllegalArgumentException("Unsupported cell kind")
            }
        }

        fun valueOf(value: TicTacToeProto.Kind): CellKind {
            return when (value) {
                TicTacToeProto.Kind.O -> O
                TicTacToeProto.Kind.X -> X
                else -> throw IllegalArgumentException("Unsupported cell kind")
            }
        }
    }
}
