package com.skyworld.surveyapp.presentation.surveyform

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.skyworld.surveyapp.data.model.Question
import com.skyworld.surveyapp.data.model.QuestionType

/**
 * Single entry point the form screen calls — this is the "dynamic rendering"
 * piece: it doesn't know in advance what questions exist, it just switches
 * on whatever "type" the API sent back.
 */
@Composable
fun QuestionInput(
    question: Question,
    currentAnswer: String,
    onAnswerChange: (String) -> Unit,
    onMultiChoiceToggle: (optionValue: String, checked: Boolean) -> Unit,
    certificateUris: List<Uri>,
    onAddCertificate: (Uri) -> Unit,
    onRemoveCertificate: (Uri) -> Unit
) {
    when (question.type) {
        QuestionType.SHORT_TEXT -> ShortTextInput(currentAnswer, onAnswerChange)
        QuestionType.LONG_TEXT -> LongTextInput(currentAnswer, onAnswerChange)
        QuestionType.EMAIL -> EmailInput(currentAnswer, onAnswerChange)
        QuestionType.CHOICE -> {
            val opts = question.options
            if (opts?.isMultiSelect == true) {
                MultipleChoiceInput(opts.options, currentAnswer, onMultiChoiceToggle)
            } else {
                SingleChoiceInput(opts?.options.orEmpty(), currentAnswer, onAnswerChange)
            }
        }
        QuestionType.FILE -> FileUploadInput(
            allowsMultiple = question.fileProperties?.allowsMultiple ?: false,
            uris = certificateUris,
            onAdd = onAddCertificate,
            onRemove = onRemoveCertificate
        )
        else -> Text("Unsupported question type: ${question.type}")
    }
}

@Composable
private fun ShortTextInput(value: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        label = { Text("Your answer") }
    )
}

@Composable
private fun LongTextInput(value: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp),
        label = { Text("Your answer") }
    )
}

@Composable
private fun EmailInput(value: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        label = { Text("you@example.com") },
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Email)
    )
}

@Composable
private fun SingleChoiceInput(
    options: List<com.skyworld.surveyapp.data.model.SurveyOption>,
    selectedValue: String,
    onSelect: (String) -> Unit
) {
    Column {
        options.forEach { option ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = (option.value == selectedValue),
                        onClick = { onSelect(option.value) }
                    )
                    .padding(vertical = 4.dp)
            ) {
                RadioButton(selected = (option.value == selectedValue), onClick = { onSelect(option.value) })
                Spacer(Modifier.width(8.dp))
                Text(option.label.trim())
            }
        }
    }
}

@Composable
private fun MultipleChoiceInput(
    options: List<com.skyworld.surveyapp.data.model.SurveyOption>,
    selectedCsv: String,
    onToggle: (optionValue: String, checked: Boolean) -> Unit
) {
    val selected = selectedCsv.split(",").filter { it.isNotBlank() }.toSet()
    Column {
        options.forEach { option ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            ) {
                Checkbox(
                    checked = option.value in selected,
                    onCheckedChange = { checked -> onToggle(option.value, checked) }
                )
                Spacer(Modifier.width(8.dp))
                Text(option.label.trim())
            }
        }
    }
}

@Composable
private fun FileUploadInput(
    allowsMultiple: Boolean,
    uris: List<Uri>,
    onAdd: (Uri) -> Unit,
    onRemove: (Uri) -> Unit
) {
    val pickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let(onAdd) }

    Column {
        OutlinedButton(onClick = { pickerLauncher.launch(arrayOf("application/pdf")) }) {
            Icon(Icons.Filled.AttachFile, contentDescription = null)

            Spacer(Modifier.width(8.dp))
            Text(if (allowsMultiple) "Add PDF" else "Choose PDF")
        }

        if (uris.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            LazyColumn(modifier = Modifier.heightIn(max = 180.dp)) {
                items(uris) { uri ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Text(
                            uri.lastPathSegment ?: "selected.pdf",
                            modifier = Modifier.weight(1f),
                            maxLines = 1
                        )
                        IconButton(onClick = { onRemove(uri) }) {
                            Icon(Icons.Filled.Close, contentDescription = "Remove")
                        }
                    }
                }
            }
        }
    }
}
