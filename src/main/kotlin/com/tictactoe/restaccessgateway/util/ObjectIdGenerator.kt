package com.tictactoe.restaccessgateway.util

import org.springframework.stereotype.Component

@Component
class ObjectIdGenerator {

    final fun createLong() : Long {
        return System.currentTimeMillis()
    }
}