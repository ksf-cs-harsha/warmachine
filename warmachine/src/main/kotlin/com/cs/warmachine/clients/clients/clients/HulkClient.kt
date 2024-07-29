package com.cs.warmachine.clients

import ch.qos.logback.core.CoreConstants.EMPTY_STRING
import com.cs.beam.context.TaskContext
import com.cs.beam.core.constants.ServiceException
import com.cs.beam.restclient.APIResponse
import com.cs.beam.restclient.DefaultHttpClient
import com.cs.warmachine.clients.constant.Constant.CONDITION_VARIABLE_STATUS
import com.cs.warmachine.clients.constant.Constant.KARZA_NO_RESPONSE_STATUS
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.coyote.BadRequestException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import java.io.IOException
import java.util.*

@Service
class HulkClient(private val httpClient: DefaultHttpClient, private val grootClient: GrootClient) {

    private val logger = LoggerFactory.getLogger(HulkClient::class.java)

    private val objectMapper = ObjectMapper()

    @Value("\${hulk.api.url}")
    private var hulkUrl: String? = null

    private val applicantEndpoint = "/applicant"
    private val verifyNSDLEndpoint = "/verify?vendor=nsdl"
    private val verifyEndpoint = "/verify"
    private val nsdlCacheName = "NSDL_CACHE_KEY"
    private val kycEndpoint = "/kyc/"
    private val nsdlVendorName = "nsdl"

    private var httpHeaders: HttpHeaders = HttpHeaders()

    //TODO logp attern Process Id : {} | message
    suspend fun getApplicantVf(taskContext: TaskContext, partnerId: String, applicantId: String): APIResponse {
        val checkApplicantVfUrl: String?
        var nsdlKycConfigSetFirstElement = "NULL"
        val nsdlKycConfigList: Set<String>
        try {
            nsdlKycConfigList = grootClient.getNsdlKycConfigList(taskContext, nsdlCacheName)
            checkApplicantVfUrl = if (nsdlKycConfigList.contains(partnerId)) {
                "$hulkUrl$applicantEndpoint/$applicantId$verifyNSDLEndpoint"
            } else {
                "$hulkUrl$applicantEndpoint/$applicantId$verifyEndpoint"
            }
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
        if (nsdlKycConfigList.isNotEmpty()) {
            nsdlKycConfigSetFirstElement = nsdlKycConfigList.iterator().next()
        }
        logger.info(
            "Process Id: {} | partnerId:{},nsdlKycConfigSetFirstElement:{}",
            taskContext.getProcessId(),
            partnerId,
            nsdlKycConfigSetFirstElement
        )
        logger.error(
            "Process Id : {} | Status : calling calling hulk api {}",
            taskContext.getProcessId(),
            checkApplicantVfUrl
        )
        val response:APIResponse  =  httpClient.getAPI(taskContext, checkApplicantVfUrl, httpHeaders)
        if (response.httpStatus.is5xxServerError) {
            throw ServiceException("Hulk is Down", response.httpStatus as HttpStatus);
        } else if (response.httpStatus.is4xxClientError) {
            throw BadRequestException(response.body)
        } else {
            return response
        }
    }

    suspend fun verifyApplicant(taskContext: TaskContext?,
                                partnerId: String?,
                                applicantId: String,
                                vendor: String?
    ): APIResponse {
        val verifyApplicantUrl: String
        val APIResponse: APIResponse
        var nsdlKycConfigSetFirstElement = "NULL"
        var nsdlKycConfigList: Set<String?> = HashSet()
        try {
            if (vendor == null) {
                nsdlKycConfigList = grootClient.getNsdlKycConfigList(taskContext, nsdlCacheName)
                if (nsdlKycConfigList.contains(partnerId)) {
                    logger.info("Status: {} Hitting hulk with nsdl api", taskContext?.getProcessId())
                    verifyApplicantUrl = "$hulkUrl$applicantEndpoint/$applicantId$verifyNSDLEndpoint"
                    APIResponse = httpClient.postAPI(taskContext, verifyApplicantUrl, EMPTY_STRING, httpHeaders)
                } else {
                    logger.info("Status: {} Hitting hulk with karza api", taskContext?.getProcessId())
                    verifyApplicantUrl = "$hulkUrl$applicantEndpoint/$applicantId$verifyEndpoint"
                    APIResponse = httpClient.postAPI(taskContext, verifyApplicantUrl, EMPTY_STRING, httpHeaders)
                    if (APIResponse.httpStatus.is2xxSuccessful) {
                        val body: String? = APIResponse.body
                        val node: JsonNode?
                        if (body != null) {
                            var status: String? = null
                            node = try {
                                objectMapper.readTree(body)
                            } catch (e: IOException) {
                                null
                            }
                            if (!Objects.isNull(node) && node!!.has(CONDITION_VARIABLE_STATUS)) {
                                status = node.path(CONDITION_VARIABLE_STATUS).asText()
                            }
                            if (KARZA_NO_RESPONSE_STATUS != status) {
                                logger.info(
                                    "Status: {} | Applicant verification successful for {}",
                                    taskContext?.getProcessId(),
                                    applicantId
                                )
                            } else {
                                logger.info(
                                    "Status: {} | Retrying verification KYC for applicant Id {}",
                                    taskContext?.getProcessId(),
                                    applicantId
                                )
                            }
                        }
                    }
                }
            } else {
                if (nsdlVendorName.equals(vendor, ignoreCase = true)) {
                    logger.info("Status: {} | Hitting hulk with nsdl api", taskContext?.getProcessId())
                    verifyApplicantUrl = "$hulkUrl$applicantEndpoint/$applicantId$verifyNSDLEndpoint"
                    APIResponse = httpClient.postAPI(taskContext, verifyApplicantUrl, EMPTY_STRING, httpHeaders)
                } else {
                    logger.info("Status: {} | Hitting hulk with karza api", taskContext?.getProcessId())
                    verifyApplicantUrl = "$hulkUrl$applicantEndpoint/$applicantId$verifyEndpoint"
                    APIResponse = httpClient.postAPI(taskContext, verifyApplicantUrl, EMPTY_STRING, httpHeaders)
                }
            }
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
        if (nsdlKycConfigList.isNotEmpty()) {
            nsdlKycConfigSetFirstElement = nsdlKycConfigList.iterator().next().toString()
        }
        logger.info(
            "Status: {} | partnerId:{}, nsdlKycConfigSetFirstElement:{}",
            taskContext?.getProcessId(),
            partnerId,
            nsdlKycConfigSetFirstElement
        )
        return APIResponse
    }

    suspend fun editKycDetails(taskContext: TaskContext?, applicantId: String, kycId: String, kycData: String): APIResponse {
        val url = "$hulkUrl$applicantEndpoint/$applicantId$kycEndpoint$kycId"
        httpHeaders.contentType = MediaType.APPLICATION_JSON
        val apiResponse: APIResponse = httpClient.putAPI(taskContext, url, kycData, httpHeaders)
        if (apiResponse.httpStatus.is2xxSuccessful) {
            logger.info("Process Id: {} | Edit Kyc Details successful for {}", taskContext?.getProcessId(), applicantId)
        } else {
            logger.info(
                "Process Id: {} | Retrying Edit Kyc Details for applicant Id {}",
                taskContext?.getProcessId(),
                applicantId
            )
        }
        return apiResponse
    }
}