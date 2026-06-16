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

/**
 * Only covers the mobile-relevant endpoints (survey discovery + completion).
 * Survey/question/response management is web-only per the spec.
 */
interface SurveyApiService {

    @GET("surveys")
    suspend fun getSurveys(): SurveyListResponse

    @GET("surveys/{surveyId}/questions")
    suspend fun getSurveyQuestions(@Path("surveyId") surveyId: Long): QuestionListResponse

    /**
     * Submission is multipart/form-data (not raw XML) so the API can accept
     * file parts alongside the answer fields. Each non-file answer is sent
     * as a text field keyed by the question's "name" (e.g. "full_name"),
     * multi-select choice answers are comma-joined values (matches the doc's
     * "REACT,VUE" example), and every uploaded file is a part named
     * "certificates".
     */
    @Multipart
    @POST("surveys/{surveyId}/responses")
    suspend fun submitSurveyResponse(
        @Path("surveyId") surveyId: Long,
        @PartMap fields: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part certificates: List<MultipartBody.Part>
    ): Response<Unit>
}
