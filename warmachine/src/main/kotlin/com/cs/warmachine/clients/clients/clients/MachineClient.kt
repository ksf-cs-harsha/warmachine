package com.cs.warmachine.clients.clients.clients

import com.cs.beam.context.TaskContext
import com.cs.beam.core.json.JsonUtils
import com.cs.beam.restclient.APIResponse
import com.cs.beam.restclient.DefaultHttpClient
import com.cs.warmachine.clients.model.MachinePreprocessorRequest
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.env.Environment
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Service

@Service
class MachineClient(private val httpClient: DefaultHttpClient, env: Environment) {

    private val logger: Logger = LoggerFactory.getLogger(MachineClient::class.java)

    @Value("\${machine.url.protected.preprocessor}")
    private val preProcessorMachineUrl: String? = null

    private val apiKey: String = env.getProperty("ds.matrix.api.key") as String

    private var httpHeaders: HttpHeaders = HttpHeaders()

    init {
        httpHeaders.contentType = MediaType.APPLICATION_JSON
        httpHeaders["x-api-key"] = apiKey
        logger.info("the api-key is {}", apiKey)
    }

    suspend fun hitMachinePreprocessor(
        taskContext: TaskContext,
        preProcessorRequest: MachinePreprocessorRequest
    ): APIResponse {
        var url = preProcessorMachineUrl!!
        val lpcSmall = preProcessorRequest.loanProduct.lowercase()
        url = url.replace("{replaceInputType}", preProcessorRequest.inputType.toString())
            .replace("{replaceLPC}", lpcSmall)

        val requestBody = JsonUtils.toJson(preProcessorRequest)
        logger.info(
            "Process Id : {} | Hitting the Machine Preprocessor Endpoint | Url: {} | Request Body: {}",
            taskContext.getProcessId(),
            url,
            requestBody
        )
        return httpClient.postAPI(taskContext, url, requestBody, httpHeaders)
    }
}

