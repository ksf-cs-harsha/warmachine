package com.cs.warmachine.clients


import com.cs.beam.context.TaskContext
import com.cs.beam.restclient.APIResponse
import com.cs.beam.restclient.DefaultHttpClient
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Service

@Service
class VisionClient(val httpClient: DefaultHttpClient) {

    private val logger = LoggerFactory.getLogger(VisionClient::class.java)

    @Value("\${vision.api.url}")
    private val visionClientUrl: String? = null

    private val dedupeExposureEndpoint = "/appForm/{replaceAppFormId}/exposure/rule?stage=QC"
    private val applicantEndpoint = "/applicant/"
    private val appFormEndpoint = "/appForm/"
    private val dedupeEndpoint = "/dedupe"
    private val detailedEndpoint = "/detailed"
    private val mergeEndpoint = "/merge"

    private var httpHeaders: HttpHeaders = HttpHeaders()

    init {
        httpHeaders.contentType = MediaType.APPLICATION_JSON
    }

    suspend fun dedupeExposurePolicy(taskContext: TaskContext, appFormId: String): APIResponse {
        var apiUrl = "$visionClientUrl$dedupeExposureEndpoint"
        apiUrl = apiUrl.replace("{replaceAppFormId}", appFormId)
        logger.info(
            "Process Id: {} | Hitting vision to execute exposure policy: {}",
            taskContext.getProcessId(),
            apiUrl
        )
        return httpClient.postAPI(taskContext, apiUrl, "", httpHeaders)
    }

    suspend fun dedupeApplicant(taskContext: TaskContext, applicantId: String): APIResponse {
        val url = "$visionClientUrl$applicantEndpoint$applicantId$dedupeEndpoint"
        return httpClient.postAPI(taskContext, url, "", httpHeaders)
    }

    suspend fun getAppFormDedupeStatus(taskContext: TaskContext, appFormId: String): APIResponse {
        val url = "$visionClientUrl$appFormEndpoint$appFormId$dedupeEndpoint"
        return httpClient.getAPI(taskContext, url, httpHeaders)
    }

    suspend fun getAllDeduplication(taskContext: TaskContext, appFormId: String): APIResponse {
        val url = "$visionClientUrl$appFormEndpoint$appFormId$dedupeEndpoint$detailedEndpoint"
        return httpClient.getAPI(taskContext, url, httpHeaders)
    }

    suspend fun mergeApplicant(taskContext: TaskContext, requestBody: String, applicantId: String): APIResponse {
        val url = "$visionClientUrl$applicantEndpoint$applicantId$mergeEndpoint"
        return httpClient.postAPI(taskContext, url, requestBody, httpHeaders)
    }
}