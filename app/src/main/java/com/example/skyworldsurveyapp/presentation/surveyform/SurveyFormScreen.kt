package com.skyworld.surveyapp.presentation.surveyform

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.skyworld.surveyapp.data.model.Question
import com.skyworld.surveyapp.data.model.QuestionType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurveyFormScreen(
    surveyName: String,
    onDone: () -> Unit,
    onBack: () -> Unit,
    viewModel: SurveyFormViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.submitSuccess) {
        if (state.submitSuccess) onDone()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(surveyName) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))

                state.loadError != null -> Text(
                    state.loadError ?: "",
                    modifier = Modifier.align(Alignment.Center).padding(24.dp),
                    color = MaterialTheme.colorScheme.error
                )

                else -> Column(modifier = Modifier.fillMaxSize()) {
                    LinearProgressIndicator(
                        progress = { (state.currentStep + 1f) / state.totalSteps },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Box(modifier = Modifier.weight(1f).padding(24.dp)) {
                        if (state.isReviewStep) {
                            ReviewContent(
                                questions = state.questions,
                                answers = state.answers,
                                certificateUris = state.certificateUris,
                                onEditStep = viewModel::editStep
                            )
                        } else {
                            state.currentQuestion?.let { question ->
                                QuestionStepContent(
                                    question = question,
                                    answer = state.answers[question.name].orEmpty(),
                                    certificateUris = state.certificateUris[question.name].orEmpty(),
                                    validationError = state.validationError,
                                    fileError = state.fileTooLargeError,
                                    onAnswerChange = { viewModel.updateAnswer(question.name, it) },
                                    onMultiChoiceToggle = { value, checked ->
                                        viewModel.toggleMultiChoiceOption(question.name, value, checked)
                                    },
                                    onAddCertificate = { viewModel.addCertificateUri(question.name, it) },
                                    onRemoveCertificate = { viewModel.removeCertificateUri(question.name, it) }
                                )
                            }
                        }
                    }

                    state.submitError?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(horizontal = 24.dp))
                        Spacer(Modifier.height(8.dp))
                    }

                    FormNavigationBar(
                        showPrevious = state.currentStep > 0,
                        isReviewStep = state.isReviewStep,
                        isSubmitting = state.isSubmitting,
                        onPrevious = viewModel::goPrevious,
                        onNext = viewModel::goNext,
                        onSubmit = viewModel::submit
                    )
                }
            }
        }
    }
}

@Composable
private fun QuestionStepContent(
    question: Question,
    answer: String,
    certificateUris: List<android.net.Uri>,
    validationError: String?,
    fileError: String?,
    onAnswerChange: (String) -> Unit,
    onMultiChoiceToggle: (String, Boolean) -> Unit,
    onAddCertificate: (android.net.Uri) -> Unit,
    onRemoveCertificate: (android.net.Uri) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(question.text, style = MaterialTheme.typography.headlineSmall)
        question.description?.trim()?.takeIf { it.isNotEmpty() }?.let {
            Spacer(Modifier.height(4.dp))
            Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.height(20.dp))

        QuestionInput(
            question = question,
            currentAnswer = answer,
            onAnswerChange = onAnswerChange,
            onMultiChoiceToggle = onMultiChoiceToggle,
            certificateUris = certificateUris,
            onAddCertificate = onAddCertificate,
            onRemoveCertificate = onRemoveCertificate
        )

        validationError?.let {
            Spacer(Modifier.height(12.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }
        fileError?.let {
            Spacer(Modifier.height(12.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
private fun ReviewContent(
    questions: List<Question>,
    answers: Map<String, String>,
    certificateUris: Map<String, List<android.net.Uri>>,
    onEditStep: (Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text("Review your answers", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(questions.size) { index ->
                val question = questions[index]
                val displayValue = when (question.type) {
                    QuestionType.FILE -> certificateUris[question.name]
                        ?.joinToString(", ") { it.lastPathSegment ?: "file.pdf" }
                        ?.takeIf { it.isNotEmpty() } ?: "—"
                    else -> answersDisplayLabel(question, answers[question.name])
                }

                Card(onClick = { onEditStep(index) }, modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text(question.text, style = MaterialTheme.typography.titleSmall)
                        Spacer(Modifier.height(4.dp))
                        Text(displayValue, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}

/** Turns stored option codes (e.g. "REACT,VUE") back into readable labels for the review screen. */
private fun answersDisplayLabel(question: Question, rawValue: String?): String {
    if (rawValue.isNullOrBlank()) return "—"
    val options = question.options?.options ?: return rawValue
    val labelByValue = options.associate { it.value to it.label.trim() }
    return rawValue.split(",").filter { it.isNotBlank() }
        .joinToString(", ") { labelByValue[it] ?: it }
}

@Composable
private fun FormNavigationBar(
    showPrevious: Boolean,
    isReviewStep: Boolean,
    isSubmitting: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSubmit: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(24.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        if (showPrevious) {
            OutlinedButton(onClick = onPrevious, enabled = !isSubmitting) { Text("Previous") }
        } else {
            Spacer(Modifier)
        }

        if (isReviewStep) {
            Button(onClick = onSubmit, enabled = !isSubmitting) {
                if (isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                } else {
                    Text("Submit")
                }
            }
        } else {
            Button(onClick = onNext) { Text("Next") }
        }
    }
}
