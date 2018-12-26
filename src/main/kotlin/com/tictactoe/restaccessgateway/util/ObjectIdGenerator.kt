package com.tictactoe.restaccessgateway.util

import org.springframework.stereotype.Component

@Component
class ObjectIdGenerator {
    /**
     * @return new object ID for specifyed class
     */
    final inline fun <reified T> create() : String {
        val millis = System.currentTimeMillis()
        return String.format("%s-%d", T::class.java.simpleName, millis)
    }

    final inline fun createLong() : Long {
        return System.currentTimeMillis();
    }
}