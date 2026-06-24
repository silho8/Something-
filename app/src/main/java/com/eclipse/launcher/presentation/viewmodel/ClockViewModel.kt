package com.eclipse.launcher.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class ClockViewModel @Inject constructor() : ViewModel() {

    private val _timeString = MutableStateFlow("")
    val timeString: StateFlow<String> = _timeString.asStateFlow()

    private val _dateString = MutableStateFlow("")
    val dateString: StateFlow<String> = _dateString.asStateFlow()

    init {
        startClock()
    }

    private fun startClock() {
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val dateFormat = SimpleDateFormat("EEE, MMM d", Locale.getDefault())

        viewModelScope.launch {
            while (isActive) {
                val now = Calendar.getInstance().time
                _timeString.value = timeFormat.format(now)
                _dateString.value = dateFormat.format(now)

                // Calculate delay until the next exact minute
                val currentSecond = Calendar.getInstance().get(Calendar.SECOND)
                val currentMillisecond = Calendar.getInstance().get(Calendar.MILLISECOND)
                val millisUntilNextMinute = ((60 - currentSecond) * 1000L) - currentMillisecond

                delay(millisUntilNextMinute + 50) // Small buffer to ensure crossing the minute threshold
            }
        }
    }
}
