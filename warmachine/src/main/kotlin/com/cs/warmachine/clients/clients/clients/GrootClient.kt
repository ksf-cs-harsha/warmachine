

package com.cs.warmachine.clients

import com.cs.beam.context.TaskContext
import com.cs.beam.core.json.JsonUtils
import com.cs.beam.restclient.APIResponse
import com.cs.beam.restclient.DefaultHttpClient
import com.cs.warmachine.clients.constant.Constant.NSDL_ENABLED_PARTNERS_LIST
import com.fasterxml.jackson.core.type.TypeReference
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Service

@Service
class GrootClient(val httpClient: DefaultHttpClient) {

    private val logger = LoggerFactory.getLogger(GrootClient::class.java)

    @Value("\${groot.api.url}")
    private val grootUrl: String? = null

    private val getLpcConfig = "/loanProduct/productCode/{lpc}"

    private var NSDL_KYC_VERICATION_ENABLED_PARTNERS: Set<String> = java.util.HashSet()

    private var httpHeaders: HttpHeaders = HttpHeaders()

    init {
        httpHeaders.contentType = MediaType.APPLICATION_JSON
    }

    suspend fun getLpcConfig(taskContext: TaskContext?, lpc: String): APIResponse {
        var grootLpcConfigUrl = grootUrl + getLpcConfig
        grootLpcConfigUrl = grootLpcConfigUrl.replace("{lpc}", lpc)

        logger.info("Going to get lpc config from groot : {}", grootLpcConfigUrl)
        return httpClient.getAPI(taskContext, grootLpcConfigUrl, httpHeaders)
    }

    suspend fun getNsdlKycConfigList(taskContext: TaskContext?, nsdlKycConfig: String?): Set<String> {
        logger.info("Status: {} | Entering getNsdlKycConfigList method", taskContext?.getProcessId())
        val apiCallResp: APIResponse = getApiCallResp(taskContext)
        if (!apiCallResp.httpStatus.is2xxSuccessful) {
            logger.error("Error in fetch nsdlkycconfig list from groot {}", apiCallResp.httpStatus)
            return NSDL_KYC_VERICATION_ENABLED_PARTNERS
        }
        val typeReference: TypeReference<List<String?>?> = object : TypeReference<List<String?>?>() {}
        logger.info("Return NSDL_KYC_VERICATION_ENABLED_PARTNERS")
        val nsdlEnabledPartnersList: List<String> = JsonUtils.readValue(apiCallResp.body!!, typeReference) as List<String>
        NSDL_KYC_VERICATION_ENABLED_PARTNERS = HashSet<String>(nsdlEnabledPartnersList)
        logger.info("In Groot Wrapper, NSDL_KYC_VERICATION_ENABLED_PARTNERS obtained")
        return NSDL_KYC_VERICATION_ENABLED_PARTNERS
    }

    private suspend fun getApiCallResp(taskContext: TaskContext?): APIResponse {
        val urlBuilder = StringBuilder()
        urlBuilder.append(grootUrl)
            .append(NSDL_ENABLED_PARTNERS_LIST)
        return httpClient.getAPI(taskContext, urlBuilder.toString(), httpHeaders)
    }
}


