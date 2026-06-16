package com.skyworld.surveyapp.data.model

import org.simpleframework.xml.Attribute
import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root

/** Question type constants — mirrors the "type" attribute values in the API doc. */
object QuestionType {
    const val SHORT_TEXT = "short_text"
    const val LONG_TEXT = "long_text"
    const val EMAIL = "email"
    const val CHOICE = "choice"
    const val FILE = "file"
}

/**
 * Maps to:
 * <file_properties format=".pdf" max_file_size="1" max_file_size_unit="mb" multiple="yes" />
 */
@Root(name = "file_properties", strict = false)
data class FileProperties(

    @field:Attribute(name = "format", required = false)
    var format: String = "",

    @field:Attribute(name = "max_file_size", required = false)
    var maxFileSize: String = "",

    @field:Attribute(name = "max_file_size_unit", required = false)
    var maxFileSizeUnit: String = "",

    @field:Attribute(name = "multiple", required = false)
    var multiple: String = "no"
) {
    val allowsMultiple: Boolean get() = multiple.equals("yes", ignoreCase = true)
}

/**
 * Maps to a single <question> element as returned by
 * GET /api/surveys/{surveyId}/questions
 */
@Root(name = "question", strict = false)
data class Question(

    @field:Attribute(name = "id", required = false)
    var id: Long = 0L,

    @field:Attribute(name = "name")
    var name: String = "",

    @field:Attribute(name = "type")
    var type: String = "",

    @field:Attribute(name = "required", required = false)
    var required: String = "no",

    @field:Element(name = "text")
    var text: String = "",

    @field:Element(name = "description", required = false, data = true)
    var description: String? = null,

    @field:Element(name = "options", required = false)
    var options: OptionsWrapper? = null,

    @field:Element(name = "file_properties", required = false)
    var fileProperties: FileProperties? = null
) {
    val isRequired: Boolean get() = required.equals("yes", ignoreCase = true)
}

/** Maps to the wrapping <questions>...</questions> element. */
@Root(name = "questions", strict = false)
data class QuestionListResponse(

    @field:ElementList(entry = "question", inline = true, required = false)
    var questions: MutableList<Question> = mutableListOf()
)
