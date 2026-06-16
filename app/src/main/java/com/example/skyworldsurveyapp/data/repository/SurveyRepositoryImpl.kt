package com.skyworld.surveyapp.data.repository

import com.skyworld.surveyapp.data.model.Question
import com.skyworld.surveyapp.data.model.Survey
import com.skyworld.surveyapp.data.remote.SurveyApiService
import com.skyworld.surveyapp.util.Resource
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import javax.inject.Inject

class SurveyRepositoryImpl @Inject constructor(
    private val api: SurveyApiService
) : SurveyRepository {

    override suspend fun getSurveys(): Resource<List<Survey>> = safeCall {
        api.getSurveys().surveys
    }

    override suspend fun getSurveyQuestions(surveyId: Long): Resource<List<Question>> = safeCall {
        api.getSurveyQuestions(surveyId).questions
    }

    override suspend fun submitSurveyResponse(
        surveyId: Long,
        answers: Map<String, String>,
        certificates: List<MultipartBody.Part>
    ): Resource<Unit> = safeCall {
        val fields = answers.mapValues { (_, value) ->
            value.toRequestBody("text/plain".toMediaTypeOrNull())
        }
        val response = api.submitSurveyResponse(surveyId, fields, certificates)
        if (!response.isSuccessful) {
            throw IOException("Server responded with ${response.code()}")
        }
    }

    private suspend inline fun <T> safeCall(block: suspend () -> T): Resource<T> {
        return try {
            Resource.Success(block())
        } catch (e: IOException) {
            Resource.Error(e.message ?: "Couldn't reach the server. Check your connection and that the API is running.")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Something went wrong.")
        }
    }
}
