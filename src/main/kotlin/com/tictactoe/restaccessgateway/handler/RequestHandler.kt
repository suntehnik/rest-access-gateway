package com.tictactoe.restaccessgateway.handler

import com.google.protobuf.util.JsonFormat
import com.tictactoe.proto.TicTacToeProto
import com.tictactoe.restaccessgateway.handler.CommandQueueProvider.Companion.CALLBACK_QUEUE_NAME
import io.reactivex.subjects.SingleSubject
import org.springframework.amqp.core.Queue
import org.springframework.amqp.rabbit.annotation.RabbitHandler
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import java.util.logging.Level
import java.util.logging.Logger


@Component
class RequestHandler {

    @Autowired
    private lateinit var sender: Sender

    @Autowired
    private lateinit var responseSubjectProvider: ResponseSubjectProvider

    fun handleCommand(cmdNewCell: TicTacToeProto.cmdNewCell): SingleSubject<TicTacToeProto.respNewCell> {
        val singleSubject = responseSubjectProvider.createAndRegister(cmdNewCell.clientRequestId)
        sender.send(cmdNewCell.toByteArray())
        return singleSubject
    }
}

@Component
class ResponseSubjectProvider {
    private var requestsMap = HashMap<Long, SingleSubject<TicTacToeProto.respNewCell>>()

    fun createAndRegister(clientRequestId: Long): SingleSubject<TicTacToeProto.respNewCell> {
        val singleSubject = SingleSubject.create<TicTacToeProto.respNewCell>()
        requestsMap[clientRequestId] = singleSubject
        return singleSubject
    }

    fun notifyAndDelete(newCellResponse: TicTacToeProto.respNewCell) {
        val clientRequestId = newCellResponse.clientRequestId
        val singleSubject = requestsMap[clientRequestId]
        singleSubject?.let {
            it.onSuccess(newCellResponse)
            requestsMap.remove(clientRequestId)
        }
    }
}

@Component
class CommandQueueProvider {

    @Value(COMMAND_QUEUE_NAME)
    lateinit var commandQueueName: String

    @Value(CALLBACK_QUEUE_NAME)
    lateinit var callbackQueueName: String

    @Bean
    fun commandQueue() = Queue(commandQueueName, true)

    @Bean
    fun callBackQueue() = Queue(callbackQueueName, true)

    companion object {
        const val COMMAND_QUEUE_NAME = "#{'\${com.tictactoe.cmdNewCellQueueName}'}"
        const val CALLBACK_QUEUE_NAME = "$COMMAND_QUEUE_NAME-cb"
    }
}

@Component
class Sender {

    @Autowired
    lateinit var template: RabbitTemplate

    @Autowired
    lateinit var commandQueueProvider: CommandQueueProvider

    init {
        System.out.println("Init sender")
    }

    fun send(obj: Any) {
        template.convertAndSend(commandQueueProvider.commandQueueName, obj)
    }

    fun send(message: String) {
        template.convertAndSend(commandQueueProvider.commandQueueName, message)
    }

    fun send(message: ByteArray) {
        template.convertAndSend(commandQueueProvider.commandQueueName, message)
    }
}

@Service
class RestAccessGatewayService {
    @Bean
    fun receiver() = RabbitReceiver()
}

@RabbitListener(queues = [CALLBACK_QUEUE_NAME])
class RabbitReceiver {

    @Autowired
    private lateinit var responseSubjectProvider: ResponseSubjectProvider

    @RabbitHandler
    fun receive(messageIn: String) {
        Logger.getLogger("RabbitReceiver").log(Level.INFO, "[x] Received '$messageIn'")
        val responseMessageBuilder = TicTacToeProto.respNewCell.newBuilder()
        JsonFormat.parser().merge(messageIn, responseMessageBuilder)
        if (responseMessageBuilder.isInitialized) {
            val responseMessage = responseMessageBuilder.build()
            responseSubjectProvider.notifyAndDelete(responseMessage)
        } else {
            Logger.getLogger("RabbitReceiver").log(Level.WARNING, "Unknown message format")
        }
    }

    @RabbitHandler
    fun receive(bytes: ByteArray) {
        Logger.getLogger("RabbitReceiver").log(Level.INFO, "[x] Received byte array")
        val responseMessage = TicTacToeProto.respNewCell.parseFrom(bytes)
        if (responseMessage.isInitialized) {
            responseSubjectProvider.notifyAndDelete(responseMessage)
        } else {
            Logger.getLogger("RabbitReceiver").log(Level.WARNING, "Unknown message format")
        }
    }
}
