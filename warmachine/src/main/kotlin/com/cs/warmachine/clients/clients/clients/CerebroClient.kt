package com.cs.warmachine.clients.clients.clients

import com.cs.beam.context.TaskContext
import com.cs.beam.core.json.JsonUtils
import com.cs.beam.restclient.APIResponse
import com.cs.beam.restclient.DefaultHttpClient
import com.cs.warmachine.clients.model.CreditPullDetailsResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Service

@Service
class CerebroClient(val httpClient: DefaultHttpClient) {

    private val logger = LoggerFactory.getLogger(CerebroClient::class.java)

    @Value("\${cerebro.api.url}")
    private val bureauServiceUrl: String? = null

    private val creditDataEndpoint = "/appForm/{replaceAppFormId}/creditDataStatus?bureauName={replaceBureauName}"

    private val appFormEndpoint = "/appFormId/"
    private val applicantEndpoint = "/applicantId/"
    private val parseAndBureauNameEndpoint = "/parse?bureauName="

    private var httpHeaders: HttpHeaders = HttpHeaders()

    init {
        httpHeaders.contentType = MediaType.APPLICATION_JSON
    }

    suspend fun appFormCreditData(
        taskContext: TaskContext?,
        appFormId: String,
        bureauName: String
    ): APIResponse {
        var appformCreditDataUrl = bureauServiceUrl + creditDataEndpoint
        appformCreditDataUrl = appformCreditDataUrl.replace("{replaceAppFormId}", appFormId)
        appformCreditDataUrl = appformCreditDataUrl.replace("{replaceBureauName}", bureauName)

        logger.info(
            "Going to call credit data  for appForm: {} bureauName: {} from cerebro : {}",
            appFormId,
            bureauName,
            appformCreditDataUrl
        )
        return httpClient.getAPI(taskContext, appformCreditDataUrl, httpHeaders)
    }

    suspend fun parseBureauCreditData(apiResponse: APIResponse, taskContext: TaskContext): CreditPullDetailsResponse? {
        logger.info(
            "Process Id : {} | Status : Bureau Credit Data Response from Cerebro : {} ",
            taskContext.getProcessId(),
            apiResponse.body
        )
        if (!apiResponse.httpStatus.is2xxSuccessful) {
            logger.warn(
                "Process Id : {} | Status :Failed to fetch bureau Credit Data Status from Cerebro  {} ",
                taskContext.getProcessId(),
                apiResponse.httpStatus
            )
            return null
        }
        var creditDataResponse: CreditPullDetailsResponse? = null
        try {
            creditDataResponse = JsonUtils.fromJson(apiResponse.body, CreditPullDetailsResponse::class.java)
        } catch (ignored: Exception) {
            logger.error(
                "Process Id : {} | Status :Error while parsing Bureau Score Data : ApiCallResp {}",
                taskContext.getProcessId(),
                apiResponse
            )
        }
        return creditDataResponse
    }

    suspend fun parseBureau(
        taskContext: TaskContext,
        appFormId: String,
        applicantId: String,
        bureauName: String
    ): APIResponse {
        val url =
            "$bureauServiceUrl$appFormEndpoint$appFormId$applicantEndpoint$applicantId$parseAndBureauNameEndpoint$bureauName"
        logger.info("Process Id : {} | Status : Going to parse Bureau File | url {} ", taskContext.getProcessId(), url)
        return httpClient.postAPI(taskContext, url, "", httpHeaders)
    }

    suspend fun parseBureauFile(
        taskContext: TaskContext,
        appFormId: String,
        applicantId: String,
        bureauName: String
    ): APIResponse {
        val url =
            "$bureauServiceUrl$appFormEndpoint$appFormId$applicantEndpoint$applicantId$parseAndBureauNameEndpoint$bureauName"
        logger.info("Process Id : {} | Status : Going to parse Bureau File | url {} ", taskContext.getProcessId(), url)
        return httpClient.postAPI(taskContext, url, "", httpHeaders)
    }
}
