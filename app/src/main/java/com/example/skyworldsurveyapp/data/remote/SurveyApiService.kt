package com.skyworld.surveyapp.data.remote

import com.skyworld.surveyapp.data.model.QuestionListResponse
import com.skyworld.surveyapp.data.model.SurveyListResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.PartMap
import retrofit2.http.Path


interface SurveyApiService {

    @GET("surveys")
    suspend fun getSurveys(): SurveyListResponse

    @GET("surveys/{surveyId}/questions")
    suspend fun getSurveyQuestions(@Path("surveyId") surveyId: Long): QuestionListResponse

    @Multipart
    @POST("surveys/{surveyId}/responses")
    suspend fun submitSurveyResponse(
        @Path("surveyId") surveyId: Long,
        @PartMap fields: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part certificates: List<MultipartBody.Part>
    ): Response<Unit>
}
