package com.tictactoe.restaccessgateway.handler

import com.google.protobuf.util.JsonFormat
import com.tictactoe.proto.TicTacToeProto
import com.tictactoe.restaccessgateway.handler.RabbitQueue.Companion.QUEUE_NAME
import io.reactivex.subjects.SingleSubject
import org.springframework.amqp.core.Queue
import org.springframework.amqp.rabbit.annotation.RabbitHandler
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import java.util.logging.Level
import java.util.logging.Logger

@Component
class RequestHandler {

    @Autowired
    private lateinit var rabbitQueue: RabbitQueue

    private var requestsMap: HashMap<Long, SingleSubject<TicTacToeProto.respNewCell>> = HashMap()

    fun handleCommand(cmdNewCell: TicTacToeProto.cmdNewCell): SingleSubject<TicTacToeProto.respNewCell> {
        val singleSubject = SingleSubject.create<TicTacToeProto.respNewCell>()
        requestsMap[cmdNewCell.clientRequestId] = singleSubject
        rabbitQueue.sender().send(JsonFormat.printer().print(cmdNewCell))
        return singleSubject
    }
}

@Component
class RabbitQueue {

    @Bean
    fun queue() = Queue(QUEUE_NAME, true)

    @Bean
    fun receiver() = RabbitReceiver()

    @Bean
    fun sender() = Sender()


    companion object {
        const val QUEUE_NAME = "spring-queue"
    }
}

class Sender {

    @Autowired
    lateinit var template: RabbitTemplate

    @Autowired
    lateinit var queue: Queue

    init {
        System.out.println("Init sender")
    }

    fun send(message: String) {
        template.convertAndSend(queue.name, message)
    }

    fun send(message: ByteArray) {
        template.convertAndSend(queue.name, message)
    }

    fun send() {
        val message = "Hello World!"
        template.convertAndSend(queue.name, message)
        println("[x] Sent '$message'")
    }
}


@RabbitListener(queues = [QUEUE_NAME])
class RabbitReceiver {

    init {
        System.out.println("Init receiver")
    }

    @RabbitHandler
    fun receive(msgByte: Array<Byte>) {
        println("Byte received")
    }

    @RabbitHandler
    fun receive(messageIn: String) = Logger.getLogger("RabbitReceiver").log(Level.INFO, "[x] Received '$messageIn'")
}
