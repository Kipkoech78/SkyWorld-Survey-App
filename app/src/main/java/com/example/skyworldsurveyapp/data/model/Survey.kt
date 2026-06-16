package com.skyworld.surveyapp.data.model

import org.simpleframework.xml.Root


import org.simpleframework.xml.Attribute
import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList


/**
 * Maps to:
 * <survey id="1">
 *   <name>...</name>
 *   <description>...</description>
 * </survey>
 */
@Root(name = "survey", strict = false)
data class Survey(

    @field:Attribute(name = "id", required = false)
    var id: Long = 0L,

    @field:Element(name = "name")
    var name: String = "",

    @field:Element(name = "description", required = false, data = true)
    var description: String? = null
) {
    // Simple-XML requires a no-arg constructor to exist for deserialization;
    // the default values above already provide that.
}

/**
 * Maps to the wrapping <surveys>...</surveys> returned by GET /api/surveys
 */
@Root(name = "surveys", strict = false)
data class SurveyListResponse(

    @field:ElementList(entry = "survey", inline = true, required = false)
    var surveys: MutableList<Survey> = mutableListOf()
)
