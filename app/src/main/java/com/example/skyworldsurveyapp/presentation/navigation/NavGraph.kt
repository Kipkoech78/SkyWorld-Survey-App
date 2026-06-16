package com.skyworld.surveyapp.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.skyworld.surveyapp.presentation.surveyform.SurveyFormScreen
import com.skyworld.surveyapp.presentation.surveylist.SurveyListScreen

@Composable
fun SurveyNavGraph(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Screen.SurveyList.route) {

        composable(Screen.SurveyList.route) {
            SurveyListScreen(
                onSurveySelected = { survey ->
                    navController.navigate(Screen.SurveyForm.createRoute(survey.id, survey.name))
                }
            )
        }

        composable(
            route = Screen.SurveyForm.route,
            arguments = listOf(
                navArgument("surveyId") { type = NavType.StringType },
                navArgument("surveyName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val surveyName = backStackEntry.arguments?.getString("surveyName").orEmpty()
            SurveyFormScreen(
                surveyName = surveyName,
                onDone = {
                    navController.popBackStack(Screen.SurveyList.route, inclusive = false)
                },
                onBack = { navController.popBackStack() }
            )
        }
    }
}
