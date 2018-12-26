package com.tictactoe.restaccessgateway.controller

import com.google.protobuf.util.JsonFormat
import com.tictactoe.proto.TicTacToeProto
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@RunWith(SpringRunner::class)
@SpringBootTest
@AutoConfigureMockMvc
class RestAccessGatewayTest {
    @Autowired
    private val mockMvc: MockMvc? = null

    @Before
    fun setUp() {
    }

    @After
    fun tearDown() {
    }

    @Test
    fun getFieldConfiguration() {
        assertNotNull("Mock MVC is not null", mockMvc)
        val result = createAsyncResult(mockMvc!!, get("/field"))

        assertNotNull("Mocked result is null", result)
        this.mockMvc
                .perform(asyncDispatch(result))
                .andDo(print())
                .andExpect { }
                .andExpect(status().`is`(200))
                .andExpect(jsonPath("$.code")
                        .value(404))
                .andExpect(jsonPath("$.error.message")
                        .value("Not implemented yet"))
    }

    @Test
    fun postNewObject() {
        assertNotNull("Mock MVC set up", mockMvc)
        val postFieldConfiguration = TicTacToeProto.cmdPostFieldConfiguration.newBuilder()
                .setX(1)
                .setY(1)
                .setKind(TicTacToeProto.cmdPostFieldConfiguration.Kind.X)
                .build()
        val fieldConfigStr = JsonFormat.printer().print(postFieldConfiguration)
        mockMvc?.let {
            val postResult = it.perform(post("/field")
                    .content(fieldConfigStr)
                    .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
            )
                    .andExpect(request().asyncStarted())
                    .andReturn()
            it
                    .perform(asyncDispatch(postResult))
                    .andDo(print())
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.code")
                            .value(200))
                    .andExpect {
                        jsonPath("$.error")
                                .isNotEmpty
                    }
                    .andExpect(jsonPath("$.fieldConfiguration.cells")
                            .exists())
        }
    }

    @Test
    fun invalidRequestInvocationForDeleteRequest() {
        assertNotNull("Mock MVC set up", mockMvc)
        this.mockMvc
                ?.perform(delete("/field"))
                ?.andDo(print())
                ?.andExpect(status().`is`(200))
                ?.andExpect(jsonPath("$.code")
                        .value(500))
                ?.andExpect(jsonPath("$.error.message")
                        .value("Invalid request"))
    }

    @Test
    fun invalidRequestInvocationForPutRequest() {
        assertNotNull("Mock MVC set up", mockMvc)
        this.mockMvc
                ?.perform(put("/field"))
                ?.andDo(print())
                ?.andExpect(status().`is`(200))
                ?.andExpect(jsonPath("$.code")
                        .value(500))
                ?.andExpect(jsonPath("$.error.message")
                        .value("Invalid request"))
    }

    @Test
    fun invalidRequestInvocationForPatchRequest() {
        assertNotNull("Mock MVC set up", mockMvc)
        this.mockMvc
                ?.perform(patch("/field"))
                ?.andDo(print())
                ?.andExpect(status().`is`(200))
                ?.andExpect(jsonPath("$.code")
                        .value(500))
                ?.andExpect(jsonPath("$.error.message")
                        .value("Invalid request"))
    }

    companion object {
        fun createAsyncResult(mvc: MockMvc, operation: MockHttpServletRequestBuilder): MvcResult {
            val result = mvc
                    .perform(operation)
//                    .andExpect(request().asyncResult(notNullValue()))
//                    .andExpect(request().asyncStarted())
                    .andReturn()
            assertNotNull("Async request result is not null", result)
            return result
        }
    }
}