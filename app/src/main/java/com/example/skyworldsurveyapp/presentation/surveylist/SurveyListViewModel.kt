package com.skyworld.surveyapp.presentation.surveylist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skyworld.surveyapp.data.model.Survey
import com.skyworld.surveyapp.data.repository.SurveyRepository
import com.skyworld.surveyapp.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SurveyListViewModel @Inject constructor(
    private val repository: SurveyRepository
) : ViewModel() {

    private val _state = MutableStateFlow<Resource<List<Survey>>>(Resource.Loading)
    val state: StateFlow<Resource<List<Survey>>> = _state.asStateFlow()

    init {
        loadSurveys()
    }

    fun loadSurveys() {
        viewModelScope.launch {
            _state.value = Resource.Loading
            _state.value = repository.getSurveys()
        }
    }
}
