package com.skyworld.surveyapp.presentation.surveyform

import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skyworld.surveyapp.data.model.Question
import com.skyworld.surveyapp.data.model.QuestionType
import com.skyworld.surveyapp.data.repository.SurveyRepository
import com.skyworld.surveyapp.util.FileUtils
import com.skyworld.surveyapp.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SurveyFormUiState(
    val isLoading: Boolean = true,
    val loadError: String? = null,
    val questions: List<Question> = emptyList(),
    val currentStep: Int = 0,

    val answers: Map<String, String> = emptyMap(),
    val certificateUris: Map<String, List<Uri>> = emptyMap(),
    val fileTooLargeError: String? = null,
    val validationError: String? = null,
    val isSubmitting: Boolean = false,
    val submitError: String? = null,
    val submitSuccess: Boolean = false
) {
    val totalSteps: Int get() = questions.size + 1 // +1 for the review step
    val isReviewStep: Boolean get() = currentStep >= questions.size
    val currentQuestion: Question? get() = questions.getOrNull(currentStep)
}

@HiltViewModel
class SurveyFormViewModel @Inject constructor(
    private val repository: SurveyRepository,
    @ApplicationContext private val appContext: Context,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val surveyId: Long = savedStateHandle.get<String>("surveyId")?.toLongOrNull() ?: -1L

    private val _uiState = MutableStateFlow(SurveyFormUiState())
    val uiState: StateFlow<SurveyFormUiState> = _uiState.asStateFlow()

    init {
        loadQuestions()
    }

    private fun loadQuestions() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, loadError = null) }
            when (val result = repository.getSurveyQuestions(surveyId)) {
                is Resource.Success -> _uiState.update { it.copy(isLoading = false, questions = result.data) }
                is Resource.Error -> _uiState.update { it.copy(isLoading = false, loadError = result.message) }
                Resource.Loading -> Unit
            }
        }
    }

    fun updateAnswer(questionName: String, value: String) {
        _uiState.update { it.copy(answers = it.answers + (questionName to value), validationError = null) }
    }

    fun toggleMultiChoiceOption(questionName: String, optionValue: String, checked: Boolean) {
        _uiState.update { state ->
            val current = state.answers[questionName]?.split(",")?.filter { it.isNotBlank() }?.toMutableList() ?: mutableListOf()
            if (checked) {
                if (optionValue !in current) current.add(optionValue)
            } else {
                current.remove(optionValue)
            }
            state.copy(answers = state.answers + (questionName to current.joinToString(",")), validationError = null)
        }
    }

    fun addCertificateUri(questionName: String, uri: Uri) {
        val result = FileUtils.uriToMultipart(appContext, uri)
        when (result) {
            is FileUtils.UriToPartResult.TooLarge -> {
                _uiState.update { it.copy(fileTooLargeError = "${result.fileName} is over the 1MB limit") }
            }
            FileUtils.UriToPartResult.ReadFailed -> {
                _uiState.update { it.copy(fileTooLargeError = "Couldn't read that file, please try another") }
            }
            is FileUtils.UriToPartResult.Success -> {
                _uiState.update { state ->
                    val current = state.certificateUris[questionName].orEmpty()
                    state.copy(
                        certificateUris = state.certificateUris + (questionName to (current + uri)),
                        fileTooLargeError = null,
                        validationError = null
                    )
                }
            }
        }
    }

    fun removeCertificateUri(questionName: String, uri: Uri) {
        _uiState.update { state ->
            val current = state.certificateUris[questionName].orEmpty()
            state.copy(certificateUris = state.certificateUris + (questionName to (current - uri)))
        }
    }

    fun goNext() {
        val state = _uiState.value
        val question = state.currentQuestion ?: return
        if (!isAnswered(question, state)) {
            _uiState.update { it.copy(validationError = "This question is required before you can continue") }
            return
        }
        _uiState.update { it.copy(currentStep = it.currentStep + 1, validationError = null) }
    }

    fun goPrevious() {
        _uiState.update { it.copy(currentStep = (it.currentStep - 1).coerceAtLeast(0), validationError = null) }
    }

    // Jump back to a specific step from the review screen to edit an answer.
    fun editStep(stepIndex: Int) {
        _uiState.update { it.copy(currentStep = stepIndex.coerceIn(0, it.questions.size), validationError = null) }
    }

    private fun isAnswered(question: Question, state: SurveyFormUiState): Boolean {
        if (!question.isRequired) return true
        return when (question.type) {
            QuestionType.FILE -> state.certificateUris[question.name]?.isNotEmpty() == true
            else -> state.answers[question.name]?.isNotBlank() == true
        }
    }

    fun submit() {
        val state = _uiState.value
        // Final guard in case the review step was reached without every
        // required question answered (shouldn't happen given goNext's
        // validation, but defensive here since this is the point of no return).
        val firstUnanswered = state.questions.indexOfFirst { !isAnswered(it, state) }
        if (firstUnanswered != -1) {
            _uiState.update { it.copy(currentStep = firstUnanswered, validationError = "This question is required before you can continue") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, submitError = null) }

            val certificateParts = state.certificateUris.values.flatten().mapNotNull { uri ->
                (FileUtils.uriToMultipart(appContext, uri) as? FileUtils.UriToPartResult.Success)?.part
            }

            when (val result = repository.submitSurveyResponse(surveyId, state.answers, certificateParts)) {
                is Resource.Success -> _uiState.update { it.copy(isSubmitting = false, submitSuccess = true) }
                is Resource.Error -> _uiState.update { it.copy(isSubmitting = false, submitError = result.message) }
                Resource.Loading -> Unit
            }
        }
    }
}
