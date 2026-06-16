package com.skyworld.surveyapp.data.model

import org.simpleframework.xml.Attribute
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root
import org.simpleframework.xml.Text

/**
 * Maps to:
 * <option value="REACT">React JS</option>
 *
 * The display label is the element's inline text content, the code/value
 * sent back to the API is the "value" attribute.
 */
@Root(name = "option", strict = false)
data class SurveyOption(

    @field:Attribute(name = "value")
    var value: String = "",

    @field:Text(required = false)
    var label: String = ""
)
@Root(name = "options", strict = false)
data class OptionsWrapper(

    @field:Attribute(name = "multiple", required = false)
    var multiple: Int = 1,

    @field:ElementList(entry = "option", inline = true, required = false)
    var options: MutableList<SurveyOption> = mutableListOf()
) {
    val isMultiSelect: Boolean get() = multiple == 1
}
