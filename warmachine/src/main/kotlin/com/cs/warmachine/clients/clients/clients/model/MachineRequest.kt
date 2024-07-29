package com.cs.warmachine.clients.model

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.JsonNode

data class MachinePreprocessorRequest(
    val id: String,
    val partnerId: String,
    val loanProduct: String,
    val partnerLoanId: String,
    val appFormId: String,
    val sourceService: String? = null,
    val resumeTaskName: String? = null,
    val applicantId: String? = null,
    val workFlowId: String? = null,
    val flowType: String? = null,
    val inputType: String? = null,
    val sourceType: String? = null,
    val thirdPartyResponse: String? = null,
    val inputData: MachineInputData
)

data class InputData(
    var appForm: JsonNode? = null,
    var applicantId: String? = null,
    var selfie: HashMap<String, Any>? = null,
    var perfiosReport: String? = null,
    var sherlockReport: String? = null,
    var cibilReport: String? = null,
    var aadharReport: String? = null,
    var KarzaPanProfileReport: String? = null,
    var hypervergeReport: String? = null,
    @get:JsonProperty("green_channel") var greenChannel: HashMap<String, Any>? = null,
    @get:JsonProperty("karza_employment") var karzaEmployment: HashMap<String, Any>? = null,
    var payload: String? = null,
    var panKarzaReport: String? = null,
    var pennyTestingReport: String? = null,
    @get:JsonProperty("attempt_count") var attemptCount: Int? = null,
    var dataSource: String? = null,
    var selectedOffer: List<JsonNode>? = null
)

@JsonFormat(shape = JsonFormat.Shape.STRING)
enum class InputType {
    bureau,
    hard_bureau,
    sherlock,
    aadhar,
    karza_pan_profile,
    hyperverge,
    pan_nsdl,
    overallkyc_inference,
    penny_testing,
    penny_drop_check,
    postapproval_inference
}

@JsonFormat(shape = JsonFormat.Shape.STRING)
enum class MachineSourceType(val type: String) {
    REPORT("report"),
    DATA("data")
}

@JsonFormat(shape = JsonFormat.Shape.STRING)
enum class MachineSourceService(val type: String) {
    PLEX("plex")
}

@JsonFormat(shape = JsonFormat.Shape.STRING)
enum class MachineResumeTaskName(val type: String) {
    WAIT_FOR_KYC_MACHINE_DECISION("WaitForKycMachineDecision"),
    WAIT_FOR_BUREAU_DECISION("WaitForBureauDecision")
}


data class MachineInputData(
    val appForm: JsonNode,
    val dataSource: String? = null,
    val bureauType: String? = null,
    val report: String? = null,
    val data: JsonNode? = null,
    val attemptCount: Int? = null,
    val selectedOffer: List<JsonNode>? = null
)

data class MachineInferenceRequest(
    val id: String,
    val workFlowId: String,
    val sourceService: String? = null,
    val resumeTaskName: String? = null,
    val partnerId: String,
    val loanProduct: String,
    val inputType: String,
    val partnerLoanId: String,
    val appFormId: String,
    val inputData: MachineInputData
)

data class NSDLPanResponse(
    val result: Result,
    val requestId: String,
    val statusCode: String? = null
)

data class Result(
    val name: String
)