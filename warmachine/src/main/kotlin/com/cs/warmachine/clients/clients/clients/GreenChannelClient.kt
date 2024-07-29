package com.cs.warmachine.clients

import com.cs.beam.context.TaskContext
import com.cs.beam.restclient.APIResponse
import com.cs.beam.restclient.DefaultHttpClient
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Service
import org.springframework.web.util.UriComponentsBuilder

@Service
class GreenChannelClient(val httpClient: DefaultHttpClient) {

    private val logger = LoggerFactory.getLogger(GreenChannelClient::class.java)

    @Value("\${greenChannel.api.url}")
    private val greenChannelUrl: String? = null

    private var httpHeaders: HttpHeaders = HttpHeaders()

    private val greenChannelStatus = "/appForm/greenChannel/status"

    suspend fun getGCStatusForAppForm(processId: String, taskContext: TaskContext, appFormId: String?, applicantId: String?): APIResponse {
        val apiUrl = greenChannelUrl + greenChannelStatus
        logger.info(
            "Process Id : {} | Status : Going to hit GreenChannel matchAppForm API URL {} - with appFormId - {}",
            processId,
            apiUrl,
            appFormId
        )
        val builder = UriComponentsBuilder.fromUriString(apiUrl)
            .queryParam("appFormId", appFormId)
            .queryParam("applicantId", applicantId)

        val url = builder.build(false).toUriString()
        return httpClient.getAPI(taskContext, url, httpHeaders)
    }
}