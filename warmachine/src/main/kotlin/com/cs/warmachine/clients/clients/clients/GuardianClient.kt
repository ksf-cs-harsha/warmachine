package com.cs.warmachine.clients

import com.cs.beam.context.TaskContext
import com.cs.beam.core.json.JsonUtils
import com.cs.beam.restclient.APIResponse
import com.cs.beam.restclient.DefaultHttpClient
import com.cs.warmachine.clients.model.PolicyRun
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.util.UriComponentsBuilder


@Service
class GuardianClient(val httpClient: DefaultHttpClient) {

    private val logger = LoggerFactory.getLogger(GuardianClient::class.java)

    @Value("\${guardian.api.url}")
    private val guardianUrl: String? = null

    private val executePolicy = "/policy/{policyName}/run"

    private val bureauCheckApi = "/rules/bureauPull"

    private val inferencePolicyRunApi = "/inference"

    private var httpHeaders: HttpHeaders = HttpHeaders()

    init {
        httpHeaders.contentType = MediaType.APPLICATION_JSON
    }

    //    "creditPolicy", "appForm", "appFormId", "loan.loanProduct", ""
    suspend fun executePolicy(
        taskContext: TaskContext,
        policyName: String, entityType: String, entityId: String, lpc: String, version: String? = null,
        body: String
    ): APIResponse {
        var executePolicyUrl = guardianUrl + executePolicy
        executePolicyUrl = executePolicyUrl.replace("{policyName}", policyName)

        val builder = UriComponentsBuilder.fromUriString(executePolicyUrl)
            .queryParam("entityType", entityType)
            .queryParam("entityId", entityId)
            .queryParam("lpc", lpc)
            .queryParam("version", version)
        val url = builder.build(false).toUriString()
        logger.info("Going to execute Policy in guardian url {} , body {} ", url, body)

        return httpClient.postAPI(taskContext, url, body, httpHeaders)
    }

    fun parsePolicyResponse(taskContext: TaskContext, apiCallResp: APIResponse): PolicyRun {
        val policyResult = PolicyRun()
        logger.info("Process Id : {} | Status : Policy Response : {} ", taskContext.getProcessId(), apiCallResp.body)
        if (!apiCallResp.httpStatus.is2xxSuccessful) {
            logger.error(
                "Process Id : {} | Status : Something went wrong in Guardian | Response:{}",
                taskContext.getProcessId(), apiCallResp.body
            )
            return policyResult
        }

        return JsonUtils.fromJson(apiCallResp.body, PolicyRun::class.java)
    }

    suspend fun initiateBureauPullCheck(taskContext: TaskContext, bureauPull: String): APIResponse {
        val bureauPullUrl = "$guardianUrl$bureauCheckApi"
        logger.info("Going to initiate Bureau Pull Check in guardian : {}", bureauPullUrl)
        return httpClient.postAPI(taskContext, bureauPullUrl, bureauPull, httpHeaders)
    }

    suspend fun runMatrixPolicy(taskContext: TaskContext, requestBody: String): APIResponse {
        logger.info("Process Id: {} Going to run Inference Policy in guardian", taskContext.getProcessId())
        val matrixEndpoint = "$guardianUrl$inferencePolicyRunApi"
        val apiResponse = httpClient.postAPI(taskContext, matrixEndpoint, requestBody, httpHeaders)
        logger.info("Process Id: {} Response after to executing Inference Policy", taskContext.getProcessId())
        return apiResponse
    }
}