package com.example.burpeechallenge100days

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.TimeUnit

class BurpeeViewModel(private val repository: BurpeeRepository) : ViewModel() {

    private val _currentDay = MutableLiveData<Int>()
    val currentDay: LiveData<Int> = _currentDay

    private val _totalBurpeesToday = MutableLiveData<Int>()
    val totalBurpeesToday: LiveData<Int> = _totalBurpeesToday

    private val _burpeesLeft = MutableLiveData<Int>()
    val burpeesLeft: LiveData<Int> = _burpeesLeft

    private val _burpeesDone = MutableLiveData<Int>()
    val burpeesDone: LiveData<Int> = _burpeesDone

    private var totalDays = 100
    private var lastRecordedDate: Long = 0

    init {
        viewModelScope.launch {
            loadData()
        }
    }

    private suspend fun loadData() {
        val currentDate = Calendar.getInstance().timeInMillis
        lastRecordedDate = repository.getLastRecordedDate()

        val daysDifference = TimeUnit.MILLISECONDS.toDays(currentDate - lastRecordedDate)

        if (lastRecordedDate == 0L) {
            // Первый запуск приложения
            _currentDay.value = 1
            _totalBurpeesToday.value = 1
            _burpeesDone.value = 0
            _burpeesLeft.value = 1

            repository.saveCurrentDay(1)
            repository.saveBurpeesDoneToday(0)
            repository.saveLastRecordedDate(currentDate)
        } else if (daysDifference >= 1) {
            // Если прошел хотя бы один день, обновляем день
            val nextDay = (repository.getCurrentDay() + daysDifference).coerceAtMost(totalDays.toLong()).toInt()
            _currentDay.value = nextDay
            _totalBurpeesToday.value = nextDay
            _burpeesDone.value = 0
            _burpeesLeft.value = nextDay

            repository.saveCurrentDay(nextDay)
            repository.saveBurpeesDoneToday(0)
            repository.saveLastRecordedDate(currentDate)
        } else {
            // Если это тот же день, загружаем сохраненные данные
            _currentDay.value = repository.getCurrentDay()
            _totalBurpeesToday.value = _currentDay.value
            _burpeesDone.value = repository.getBurpeesDoneToday()
            _burpeesLeft.value = (_totalBurpeesToday.value ?: 0) - (_burpeesDone.value ?: 0)
        }
    }

    fun addBurpees(count: Int) {
        val currentDone = _burpeesDone.value ?: 0
        val newDone = currentDone + count
        _burpeesDone.value = newDone
        _burpeesLeft.value = (_totalBurpeesToday.value ?: 1) - newDone

        viewModelScope.launch {
            repository.saveBurpeesDoneToday(newDone)
            repository.saveLastRecordedDate(Calendar.getInstance().timeInMillis)
        }
    }

    fun setDay(day: Int) {
        _currentDay.value = day
        _totalBurpeesToday.value = day
        _burpeesDone.value = 0
        _burpeesLeft.value = day

        viewModelScope.launch {
            repository.saveCurrentDay(day)
            repository.saveBurpeesDoneToday(0)
            repository.saveLastRecordedDate(Calendar.getInstance().timeInMillis)
        }
    }

    fun resetProgress() {
        viewModelScope.launch {
            _currentDay.value = 1
            _totalBurpeesToday.value = 1
            _burpeesDone.value = 0
            _burpeesLeft.value = 1

            repository.saveCurrentDay(1)
            repository.saveBurpeesDoneToday(0)
            repository.saveLastRecordedDate(Calendar.getInstance().timeInMillis)
        }
    }
}
