package com.cs.warmachine.clients.model

import com.fasterxml.jackson.annotation.JsonInclude

/**
 * @author krishna
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class PolicyRun(
    var policyID: Int? = null,
    var policyName: String? = null,
    var totalRules: Int? = null,
    var totalPassed: Int? = null,
    var overallStatus: String? = null,
    var rulesExecuted: Map<String, Boolean>? = null
)