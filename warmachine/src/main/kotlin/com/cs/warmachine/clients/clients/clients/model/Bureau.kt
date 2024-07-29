package com.cs.warmachine.clients.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties


// to fetch existing bureau credit data
@JsonIgnoreProperties(ignoreUnknown = true)
data class CreditPullDetailsResponse(
    var appFormId: String? = null,
    var applicantCreditDataList: List<ApplicantCreditData>? = null
)
@JsonIgnoreProperties(ignoreUnknown = true)
data class ApplicantCreditData(
    var applicantId: Int? = null,
    var detailsList: List<CreditPullData>? = null
)
@JsonIgnoreProperties(ignoreUnknown = true)
data class CreditPullData(
    var status: String? = null,
    var downloadUrlResponse: String? = null,
    var manualUpload: Boolean? = null
)
