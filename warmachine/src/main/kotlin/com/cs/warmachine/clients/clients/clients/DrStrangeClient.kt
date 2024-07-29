package com.cs.warmachine.clients.clients.clients

import com.cs.beam.context.TaskContext
import com.cs.beam.restclient.APIResponse
import com.cs.beam.restclient.DefaultHttpClient
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.FileSystemResource
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.util.UriComponentsBuilder
import java.io.File


@Service
class DrStrangeClient(
    val httpClient: DefaultHttpClient,
    @Value("\${cloud.aws.s3.unorganizedBucket}")
    private val rawDataBucket: String
) {

    private val logger = LoggerFactory.getLogger(ShieldClient::class.java)

    private val appFormEndpoint = "/appForm"

    private val validateAadhaarApi = "/aadhaarData"

    @Value("\${drStrange.api.url}")
    private val drStrangeUrl: String? = null

    private val appFormDocEndpoint = "/appForm/{appFormId}/appFormDoc/{id}/link"


    private var httpHeaders: HttpHeaders = HttpHeaders()

    suspend fun validateAadhaar(taskContext: TaskContext?, requestBody: String, aadhaarType: String): APIResponse? {
        logger.info("In validate aadhaar")
        httpHeaders.contentType = MediaType.APPLICATION_JSON

        val apiUrl = "$drStrangeUrl$validateAadhaarApi?aadhaarType=$aadhaarType"
        logger.info("Status: API URL = {} with headers = {}", apiUrl, httpHeaders)

        logger.info("Return from matchAppForm")
        return httpClient.postAPI(taskContext, apiUrl, requestBody, httpHeaders)
    }
    suspend fun uploadDocs(taskContext: TaskContext?, requestBody: String, partnerLoanId: String): APIResponse {
        val url = "$drStrangeUrl/partnerLoan/$partnerLoanId/dms/upload"
        return httpClient.postAPI(taskContext, url, requestBody, httpHeaders)
    }

    suspend fun tagDocs(taskContext: TaskContext?, partnerLoanId: String): APIResponse {
        logger.info("Calling drstrange Doc tag API")
        val docsTaggingUrl = "$drStrangeUrl/partnerLoan/$partnerLoanId/dms/tagging"
        return httpClient.postAPI(taskContext, docsTaggingUrl, "{}", httpHeaders)
    }

    suspend fun getAllDocType(taskContext: TaskContext, processInstanceId: String): APIResponse {
        val url = drStrangeUrl + "/docType"
        logger.info("Process Id : {} | Status : Calling docType api to fetch all docs list from drstrange", processInstanceId)
        return httpClient.getAPI(taskContext, url, httpHeaders)

    }

    suspend fun tagDocument(taskContext: TaskContext, id: String, body: String, appFormId: String): APIResponse {
        var url = (drStrangeUrl + appFormDocEndpoint).replace("{id}", id)
        url = url.replace("{appFormId}", appFormId)
        return httpClient.putAPI(taskContext, url, body, httpHeaders)
    }

    suspend fun checkDmsCompleteStatus(taskContext: TaskContext, appFormId: String): APIResponse {
        val docsInfoUrl = "${drStrangeUrl}/appForm/${appFormId}/dms/complete"
        return httpClient.getAPI(taskContext, docsInfoUrl, httpHeaders)
    }


    suspend fun updateDMSComplete(taskContext: TaskContext, appFormId: String, loanProductCode: String): APIResponse {
        val url = "${drStrangeUrl}/appForm/$appFormId/dms/complete?loanProductCode=$loanProductCode"
        return httpClient.postAPI(taskContext, url, "{}", httpHeaders)
    }

    suspend fun getDocsByLPC(taskContext: TaskContext?, appFormId: String, lpc: String): APIResponse {
        val url =
            "$drStrangeUrl$appFormEndpoint/$appFormId/docsBySection?loanProductCode=$lpc"
        return httpClient.getAPI(taskContext, url, httpHeaders)
    }

    suspend fun uploadPartnerDocs(taskContext: TaskContext, file: File, appFormId: String): APIResponse {
        val url = "$drStrangeUrl/bucket/upload"
        val httpHeaders = HttpHeaders()
        httpHeaders.contentType = MediaType.MULTIPART_FORM_DATA
        val builder = UriComponentsBuilder.fromUriString(url)
            .queryParam("uploadKey", appFormId)
            .queryParam("bucketName", rawDataBucket)
        val body: MultiValueMap<String, HttpEntity<*>> = LinkedMultiValueMap()
        body.add("file", HttpEntity(FileSystemResource(file)))
        return httpClient.postMultiPartFile(
            taskContext,
            builder.build(false).toUriString(),
            body,
            httpHeaders
        )
    }

    suspend fun transformDocs(taskContext: TaskContext, body: String, partnerLoanId: String): APIResponse {
        val url = "$drStrangeUrl/partnerLoan/$partnerLoanId/dms/upload"
        return httpClient.postAPI(taskContext, url, body, httpHeaders)
    }
}