package com.skyworld.surveyapp.util

/**
 * Wraps the outcome of a repository call so ViewModels/Composables can
 * render loading, success, and error states uniformly.
 */
sealed class Resource<out T> {
    data class Success<out T>(val data: T) : Resource<T>()
    data class Error(val message: String) : Resource<Nothing>()
    data object Loading : Resource<Nothing>()
}
