package com.qmk.musicmanager.model

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Model for defining a rule to apply to a title, in order to filter out or format some strings.")
data class NamingRule(
    @field:Schema(
        description = "Id of the naming rule in the database.",
        type = "String"
    )
    val id: String? = null,
    @field:Schema(
        description = "The String that has to be replaced.",
        example = " [Monstercat Release]",
        type = "String"
    )
    val replace: String,
    @field:Schema(
        description = "What the String has to be replaced by.",
        example = "",
        type = "String"
    )
    val replaceBy: String = "",
    @field:Schema(
        description = "A priority indicator to set if some replace must be done before or after others.",
        example = "2",
        type = "Int"
    )
    val priority: Int = 2
    )