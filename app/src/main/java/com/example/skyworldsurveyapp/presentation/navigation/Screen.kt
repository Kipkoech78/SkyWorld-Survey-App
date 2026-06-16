package com.skyworld.surveyapp.presentation.navigation

import android.net.Uri

sealed class Screen(val route: String) {

    data object SurveyList : Screen("survey_list")

    data object SurveyForm : Screen("survey_form/{surveyId}/{surveyName}") {
        fun createRoute(surveyId: Long, surveyName: String) =
            "survey_form/$surveyId/${Uri.encode(surveyName)}"
    }
}
