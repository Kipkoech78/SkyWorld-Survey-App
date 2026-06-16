package com.skyworld.surveyapp.data.repository

import com.skyworld.surveyapp.data.model.Question
import com.skyworld.surveyapp.data.model.Survey
import com.skyworld.surveyapp.util.Resource
import okhttp3.MultipartBody

interface SurveyRepository {

    suspend fun getSurveys(): Resource<List<Survey>>

    suspend fun getSurveyQuestions(surveyId: Long): Resource<List<Question>>
    suspend fun submitSurveyResponse(
        surveyId: Long,
        answers: Map<String, String>,
        certificates: List<MultipartBody.Part>
    ): Resource<Unit>
}
