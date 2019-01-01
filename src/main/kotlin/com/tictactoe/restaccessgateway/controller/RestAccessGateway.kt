package com.tictactoe.restaccessgateway.controller

import com.tictactoe.proto.TicTacToeProto
import com.tictactoe.restaccessgateway.domain.*
import com.tictactoe.restaccessgateway.handler.RequestHandler
import com.tictactoe.restaccessgateway.util.ObjectIdGenerator
import io.reactivex.Single
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.http.converter.protobuf.ProtobufHttpMessageConverter
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod.*
import org.springframework.web.bind.annotation.RestController
import java.util.concurrent.TimeUnit

@RestController
class RestAccessGateway {

    @Autowired
    lateinit var objectIdGenerator: ObjectIdGenerator

    @Autowired
    lateinit var commandObjectFactory: CommandObjectFactory

    @Autowired
    lateinit var requestHandler: RequestHandler

    @RequestMapping(value = ["/field"], method = [GET])
    fun getFieldConfiguration(): Single<TicTacToeResponse> {
        val error = TicTacToeError("Not implemented yet", 404)
        return Single.just(TicTacToeResponse("", error.code, error, null))
    }

    @RequestMapping(value = ["/field"], method = [POST])
    fun postNewObjectObservable(@RequestBody newCellCommand: TicTacToeProto.cmdNewCell): Single<TicTacToeResponse> {
        val cmd = newCellCommand.toBuilder().setClientRequestId(objectIdGenerator.createLong()).build()
        return requestHandler.handleCommand(cmd)
                .timeout(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS)
                .onErrorReturn {
                    TicTacToeProto.respNewCell.newBuilder()
                            .setStatus(TicTacToeProto.Status.fail)
                            .build()
                }
                .map { s ->
                    when (s.status) {
                        TicTacToeProto.Status.success -> {
                            TicTacToeResponse("", 200, null, commandObjectFactory.createFieldConfiguration(s.cellsList))
                        }
                        TicTacToeProto.Status.fail -> {
                            val error = TicTacToeError("Request timed out", 408)
                            TicTacToeResponse("", error.code, error, null)
                        }
                        TicTacToeProto.Status.UNRECOGNIZED -> {
                            val error = TicTacToeError("Unrecognized response status", 503)
                            TicTacToeResponse("", error.code, error, null)
                        }
                        null -> {
                            val error = TicTacToeError("Unknown response status", 503)
                            TicTacToeResponse("", error.code, error, null)
                        }
                    }

                }
    }

    @Bean
    fun protobufHttpMessageConverter() = ProtobufHttpMessageConverter()

    @RequestMapping(value = ["/field"], method = [DELETE, PUT, PATCH])
    fun invalidRequestInvocation(): TicTacToeResponse {
        val error = TicTacToeError("Invalid request", 500)
        return TicTacToeResponse("", error.code, error, null)
    }

    companion object {
        const val REQUEST_TIMEOUT = 3L
    }
}

@Component
class CommandObjectFactory {

    fun createObject(x: Long, y: Long, kind: String): TicTacToeProto.cmdNewCell {
        return TicTacToeProto.cmdNewCell.newBuilder()
                .setKind(TicTacToeProto.Kind.valueOf(kind))
                .setX(x)
                .setY(y)
                .build()
    }

    fun createFieldConfiguration(cellsList: List<TicTacToeProto.docFieldCell>): FieldConfiguration {
        val fieldConfiguration = FieldConfiguration()
        for (cell in cellsList) {
            fieldConfiguration.cells.add(Cell(CellKind.valueOf(cell.kind), cell.x, cell.y))
        }
        return fieldConfiguration
    }
}