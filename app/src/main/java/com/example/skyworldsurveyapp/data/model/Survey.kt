package com.skyworld.surveyapp.data.model

import org.simpleframework.xml.Root


import org.simpleframework.xml.Attribute
import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList


@Root(name = "survey", strict = false)
data class Survey(

    @field:Attribute(name = "id", required = false)
    var id: Long = 0L,

    @field:Element(name = "name")
    var name: String = "",

    @field:Element(name = "description", required = false, data = true)
    var description: String? = null
) {
   //null constructor
}
@Root(name = "surveys", strict = false)
data class SurveyListResponse(

    @field:ElementList(entry = "survey", inline = true, required = false)
    var surveys: MutableList<Survey> = mutableListOf()
)
