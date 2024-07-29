package com.cs.warmachine.clients.clients.clients

import com.cs.beam.context.TaskContext
import com.cs.beam.core.json.JsonUtils
import com.cs.beam.restclient.APIResponse
import com.cs.beam.restclient.DefaultHttpClient
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.util.UriComponentsBuilder
import sun.tools.jconsole.Messages.DETAILS
import javax.swing.text.html.parser.DTDConstants.SYSTEM


@Service
class ShieldClient(
    val httpClient: DefaultHttpClient,
    val LINKED_INDIVIDUAL: MutableCollection<*>?,
    val APPLICANT_TYPE: String
) {

    private val logger = LoggerFactory.getLogger(ShieldClient::class.java)

    @Value("\${shield.api.url}")
    private val shieldUrl: String? = null

    private val appFormAPI = "/appForm"

    private val getAppFormApi = "/appForm/{replaceAppFormId}"

    private val appFormStatusApi = "/appForm/{appFormId}/status"


    private val getBasicAppFormEndpointByPartnerLoanId =
        "/partnerLoanId/{replacePartnerLoanId}/selective?details=appFormBasic"

    private val applicantAppFormStatusApi = "/customer/{replaceCustomerId}/applicantAppformStatus"

    private val greenChannelWorkflowAPI = "https://tars.uat.creditsaison.xyz/api/v1/generic/start-process"

    private val selectiveAppFormPath = "/{entityType}/{entityId}"

    private val appFormEditApi =
        "/appForm/{appFormId}?reRunCpcChecks=false&validationRequired={replaceValidationRequired}"

    private val updateStageAndStatusApi = "/appForm/{replaceAppFormId}/stageStatus"

    private val getAppFormByPartnerLoanIdEndpoint = "/appForm/findByPartnerLoanId"

    private val updateStageStatusEndpoint = "/appForm/{replaceAppFormId}/stageStatus"

    private val updateStageAndStatusBody = """{
            "stage": "{replaceStage}",
            "status": "{replaceStatus}"
        }"""

    private val applicantEndpoint = "/applicant/"
    private val appFormEndpoint = "/appForm/"
    private val statusEndpoint = "/status"
    private val customerEndpoint = "customerId"
    private val partnerLoanIdEndpoint = "/partnerLoanId/"
    private val greenChannelEndpoint = "/greenChannel"
    private val exclusiveUpdateTrueParams = "?exclusiveUpdate=true"
    private val appFormPatchUpdateApi = "/patch/appForm/{appFormId}?validationRequired={validationRequired}"

    private var httpHeaders: HttpHeaders = HttpHeaders()

    init {
        httpHeaders.contentType = MediaType.APPLICATION_JSON
    }

    suspend fun postAppForm(taskContext: TaskContext?, appFormData: String): APIResponse {
        logger.info("In postAppForm")

        val apiUrl = "$shieldUrl$appFormAPI"
        if (taskContext != null) {
            logger.info(
                "Status: Task postAppForm = {}, API URL = {} with headers = {}",
                taskContext.getProcessId(),
                apiUrl,
                httpHeaders
            )
        }

        logger.info("Return from postAppForm")
        return httpClient.postAPI(taskContext, apiUrl, appFormData, httpHeaders)
    }

    suspend fun startGreenChannelWorkflow(taskContext: TaskContext, appFormData: String): APIResponse {
        logger.info("In startGreenChannelWorkflow")

        logger.info(
            "Status: Task startGreenChannelWorkflow = {}, API URL = {} with headers = {}",
            taskContext.getProcessId(),
            greenChannelWorkflowAPI,
            httpHeaders
        )

        logger.info("Return from startGreenChannelWorkflow")
        return httpClient.postAPI(taskContext, greenChannelWorkflowAPI, appFormData, httpHeaders)
    }

    suspend fun changeAppFormStatus(taskContext: TaskContext, appFormId: String, statusBody: String): APIResponse {
        logger.info(
            "ProcessId: {} | Going to change status for appForm for id: {} with status body: {}",
            taskContext.getProcessId(), appFormId, statusBody
        )
        var apiUrl = "$shieldUrl$appFormStatusApi"
        apiUrl = apiUrl.replace("{appFormId}", appFormId)
        logger.info(
            "Status: Task changeAppFormStatus = {}, API URL = {} with headers = {}",
            taskContext.getProcessId(),
            apiUrl,
            httpHeaders
        )
        logger.info("ProcessId: {} | return from preApproveAppForm", taskContext.getProcessId())
        return httpClient.postAPI(taskContext, apiUrl, statusBody, httpHeaders)
    }


    suspend fun getSelectiveAppFormDetails(
        taskContext: TaskContext, entityId: String,
        entityType: String, vararg params: String
    ): APIResponse {
        var getAppFormUrl = "$shieldUrl$selectiveAppFormPath"
        getAppFormUrl = getAppFormUrl.replace("{entityType}", entityType).replace("{entityId}", entityId)
        val url = UriComponentsBuilder.fromUriString(getAppFormUrl).path("/selective")
            .queryParam(APPLICANT_TYPE, LINKED_INDIVIDUAL).queryParam(
                DETAILS, params.joinToString(",")
            ).build().toUriString()
        logger.info(
            "Status: ProcessId = {} Going to call Shield to get Selective App Form Details : {}",
            taskContext.getProcessId(),
            url
        )
        return httpClient.getAPI(taskContext, url, httpHeaders)
    }



    suspend fun getApplicantAppFormStatus(taskContext: TaskContext, customerId: String): APIResponse {
        var getApplicantAppFormStatusUrl = "$shieldUrl$applicantAppFormStatusApi"
        getApplicantAppFormStatusUrl = getApplicantAppFormStatusUrl.replace("{replaceCustomerId}", customerId)

        logger.info("Going to get Applicant AppForm Status from Shield : {}", getApplicantAppFormStatusUrl)
        return httpClient.getAPI(taskContext, getApplicantAppFormStatusUrl, httpHeaders)
    }

    suspend fun getAppForm(taskContext: TaskContext?, appFormId: String): APIResponse {
        var getAppFormUrl = "$shieldUrl$getAppFormApi"
        getAppFormUrl = getAppFormUrl.replace("{replaceAppFormId}", appFormId)

        logger.info(
            "Status: Process Id : {} Going to call Shield to get App Form : {}",
            taskContext?.getProcessId(),
            getAppFormUrl
        )
        return httpClient.getAPI(taskContext, getAppFormUrl, httpHeaders)
    }

    suspend fun editAppForm(
        taskContext: TaskContext, userId: String, appFormId: String, body: String, validationRequired: String
    ): APIResponse {
        logger.info("Updating appform for id: {} with status body: {}", appFormId, body)
        var apiUrl = "$shieldUrl$appFormEditApi"
        apiUrl = apiUrl.replace("{appFormId}", appFormId)
        apiUrl = apiUrl.replace("{replaceValidationRequired}", validationRequired)
        logger.info(
            "Status: Task = {}, API URL = {} with headers = {}", taskContext.getProcessId(), apiUrl, httpHeaders
        )
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON

        if (userId.isNotBlank()) {
            headers["requestingSub"] = userId
        }
        return httpClient.patchAPI(taskContext, apiUrl, body, headers)
    }

    //  Don't Use this api | Need change on shield
    suspend fun editAppForm(taskContext: TaskContext, appFormId: String, body: String): APIResponse {
        val apiUrl = "$shieldUrl$appFormEndpoint$appFormId$exclusiveUpdateTrueParams"
        logger.info(
            "ProcessId: {} | Updating appForm details | appFormId: {} | Url: {}",
            taskContext.getProcessId(), appFormId, apiUrl
        )
        httpHeaders["requestingSub"] = SYSTEM.toString()
        return httpClient.patchAPI(taskContext, apiUrl, body, httpHeaders)
    }


    suspend fun updateStageAndStatus(
        taskContext: TaskContext,
        appFormId: String,
        stage: String,
        status: String
    ): APIResponse {
        var updateStageAndStatusUrl = shieldUrl + updateStageAndStatusApi
        updateStageAndStatusUrl = updateStageAndStatusUrl.replace("{replaceAppFormId}", appFormId)

        var bodyContent: String = updateStageAndStatusBody
        bodyContent = bodyContent.replace("{replaceStage}", stage)
        bodyContent = bodyContent.replace("{replaceStatus}", status)
        return httpClient.putAPI(taskContext, updateStageAndStatusUrl, bodyContent, httpHeaders)
    }

    suspend fun getAppFormBasicDetailsByPartnerLoanId(taskContext: TaskContext, partnerLoanId: String): APIResponse {
        var getAppFormUrl = "$shieldUrl$getBasicAppFormEndpointByPartnerLoanId"
        getAppFormUrl = getAppFormUrl.replace("{replacePartnerLoanId}", partnerLoanId)

        logger.info(
            "Process Id : {} Going to call Shield to get appForm basic details : {}",
            taskContext.getProcessId(),
            getAppFormUrl
        )
        return httpClient.getAPI(taskContext, getAppFormUrl, httpHeaders)
    }

   suspend fun updateStatusAndStageInAppForm(taskContext: TaskContext, appFormId: String, status: String, stage: String) {
        val data = HashMap<String, String>()
        data["stage"] = stage
        data["status"] = status

        logger.info(
            "Process Id : {} | Status : Updating status {} and stage {} for appFormId {}.",
            taskContext.getProcessId(),
            status,
            stage,
            appFormId
        )
        val apiCallResp: APIResponse = updateStatusAndStage(taskContext, appFormId, JsonUtils.toJson(data))
        if (!apiCallResp.httpStatus.is2xxSuccessful) {
            logger.info(
                "Process Id : {} | Status : Error {} in updating status {} and stage {} for appFormId {}.",
                taskContext.getProcessId(),
                apiCallResp.body,
                status,
                stage,
                appFormId
            )
        } else {
            logger.info(
                "Process Id : {} | Status : Successful in updating status {} and stage {} for appFormId {}.",
                taskContext.getProcessId(),
                status,
                stage,
                appFormId
            )
        }
    }

    suspend fun emitStatusChangeAndUpdateStatus(taskContext: TaskContext,
        status: String,
        stage: String,
        appFormId: String,
        partnerLoanId: String,
        partnerId: String
    ) {
        val processInstanceId = taskContext.getProcessId()
        logger.info(
            "Process Id : {} | Status : Emitting event with status {} for appFormId {}.",
            processInstanceId,
            status,
            appFormId
        )
        val data = HashMap<String, Any>()
        data["stage"] = stage
        logger.info(
            "Process Id : {} | Status : Emitted event with status {} for appFormId {} is successful.",
            processInstanceId,
            status,
            appFormId
        )
        updateStatusAndStageInAppForm(taskContext, appFormId, status, stage)
    }

    suspend fun getAppFormByPartnerLoanId(taskContext: TaskContext?, partnerLoanId: String): APIResponse {
        val url = "$shieldUrl$getAppFormByPartnerLoanIdEndpoint/$partnerLoanId"
        return httpClient.getAPI(taskContext, url, httpHeaders)
    }

    suspend fun updateStatusAndStage(taskContext: TaskContext?, appFormId: String, requestBody: String): APIResponse {
        var url = "$shieldUrl$updateStageStatusEndpoint"
        url = url.replace("{replaceAppFormId}", appFormId)
        return httpClient.putAPI(taskContext, url, requestBody, httpHeaders)
    }

    suspend fun getApplicantData(taskContext: TaskContext, applicantId: String): APIResponse {
        val url = "$shieldUrl$applicantEndpoint$applicantId"
        return httpClient.getAPI(taskContext, url, httpHeaders)
    }

    suspend fun updateDedupeStatus(
        taskContext: TaskContext,
        applicantId: String,
        status: String,
        reason: String
    ): APIResponse {
        val url = "$shieldUrl$applicantEndpoint$applicantId"
        val applicantData: MutableMap<String, String> = HashMap()
        applicantData["dedupeStatus"] = status
        applicantData["dedupeReason"] = reason
        val data = JsonUtils.toJson(applicantData)
        return httpClient.patchAPI(taskContext, url, data, httpHeaders)
    }

    suspend fun updateAppFormStatus(taskContext: TaskContext, appFormId: String, requestBody: String): APIResponse {
        val url = "$shieldUrl$appFormEndpoint$appFormId$statusEndpoint"
        return httpClient.postAPI(taskContext, url, requestBody, httpHeaders)
    }

    suspend fun findDistinctCustomerIdsByApplicantIds(taskContext: TaskContext, applicantIds: MutableList<Int?>?): APIResponse {
        val commaSepList = applicantIds?.filterNotNull()?.joinToString(",")
        val url = "$shieldUrl$applicantEndpoint$customerEndpoint?applicantIds=$commaSepList"
        return httpClient.getAPI(taskContext, url, httpHeaders)
    }

    suspend fun updateGreenChannelStatus(taskContext: TaskContext?, partnerLoanId: String, status: String?): APIResponse {
        val url = "$shieldUrl$appFormEndpoint$partnerLoanIdEndpoint$partnerLoanId$greenChannelEndpoint?status=$status"
        return httpClient.putAPI(taskContext, url, "", httpHeaders)
    }

}