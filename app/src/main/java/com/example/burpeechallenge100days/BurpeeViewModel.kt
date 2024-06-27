package com.example.burpeechallenge100days

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.util.Calendar

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
        val currentDate = Calendar.getInstance()
        val lastRecordedDateCalendar = Calendar.getInstance()
        lastRecordedDate = repository.getLastRecordedDate()
        totalDays = repository.getTotalDays()

        lastRecordedDateCalendar.timeInMillis = lastRecordedDate

        val daysDifference = daysBetween(lastRecordedDateCalendar, currentDate)

        when {
            lastRecordedDate == 0L -> {
                // Первый запуск приложения
                resetProgress()
            }
            daysDifference == 1 -> {
                // Если прошел один день
                handleOneDaysPassed()
            }
            daysDifference == 2 -> {
                // Если прошло два дня
                handleTwoDaysPassed()
            }
            daysDifference > 2 -> {
                // Если прошло больше двух дней
                resetProgress()
            }
            else -> {
                // Если это тот же день, загружаем сохраненные данные
                loadSavedData()
            }
        }
    }

    private suspend fun handleOneDaysPassed() {
        val dayBeforeYesterdayBurpeesLeft = repository.getBurpeesLeftBeforeYesterday()
        val burpeesLeft = repository.getBurpeesLeft()

        if (dayBeforeYesterdayBurpeesLeft > 0) {
            resetProgress()
        } else if (burpeesLeft == 0) {
            loadSavedData()
            _currentDay.value = (_currentDay.value ?: 1) + 1
            _totalBurpeesToday.value = _currentDay.value
            _burpeesDone.value = 0
            _burpeesLeft.value = _totalBurpeesToday.value
            saveProgress()
        } else {
            incrementDay()
        }
    }

    private suspend fun handleTwoDaysPassed() {
        val dayBeforeYesterdayBurpeesLeft = repository.getBurpeesLeftBeforeYesterday()
        val burpeesLeft = repository.getBurpeesLeft()

        if (dayBeforeYesterdayBurpeesLeft > 0) {
            resetProgress()
        } else if (burpeesLeft == 0) {
            loadSavedData()
            _currentDay.value = (_currentDay.value ?: 1) + 2
            _totalBurpeesToday.value = (_currentDay.value ?: 1) - 1
            _burpeesDone.value = 0
            _burpeesLeft.value = _totalBurpeesToday.value
            saveProgress()
        } else {
            resetProgress()
        }
    }

    private suspend fun loadSavedData() {
        _currentDay.value = repository.getCurrentDay()
        _totalBurpeesToday.value = repository.getTotalBurpeesToday()
        _burpeesDone.value = repository.getBurpeesDoneToday()
        _burpeesLeft.value = repository.getBurpeesLeft()
    }

    private fun daysBetween(startDate: Calendar, endDate: Calendar): Int {
        val startDay = startDate.get(Calendar.DAY_OF_YEAR)
        val endDay = endDate.get(Calendar.DAY_OF_YEAR)
        val startYear = startDate.get(Calendar.YEAR)
        val endYear = endDate.get(Calendar.YEAR)

        return if (startYear == endYear) {
            endDay - startDay
        } else {
            var days = endDay - startDay
            for (year in startYear until endYear) {
                val daysInYear = if (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) {
                    366
                } else {
                    365
                }
                days += daysInYear
            }
            days
        }
    }

    fun addBurpees(count: Int) {
        val currentDone = _burpeesDone.value ?: 0
        val newDone = currentDone + count
        _burpeesDone.value = newDone
        _burpeesLeft.value = (_totalBurpeesToday.value ?: 1) - newDone
        val newLeft = _burpeesLeft.value ?: 0

        viewModelScope.launch {
            repository.saveBurpeesDoneToday(newDone)
            repository.saveBurpeesLeft(newLeft)
            repository.saveLastRecordedDate(Calendar.getInstance().timeInMillis)
        }
    }

    fun setDay(day: Int) {
        _currentDay.value = day
        _totalBurpeesToday.value = day
        _burpeesDone.value = 0
        _burpeesLeft.value = day

        viewModelScope.launch {
            saveProgress()
        }
    }

    private fun resetProgress() {
        viewModelScope.launch {
            _currentDay.value = 1
            _totalBurpeesToday.value = 1
            _burpeesDone.value = 0
            _burpeesLeft.value = 1

            saveProgress()
        }
    }

    private suspend fun saveProgress() {
        repository.saveCurrentDay(_currentDay.value ?: 1)
        repository.saveBurpeesDoneToday(_burpeesDone.value ?: 0)
        repository.saveBurpeesLeft(_burpeesLeft.value ?: 0)
        repository.saveLastRecordedDate(Calendar.getInstance().timeInMillis)
        repository.saveTotalDays(totalDays)
        repository.saveTotalBurpeesToday(_totalBurpeesToday.value ?: 0)
    }

    private suspend fun incrementDay() {
        loadSavedData()
        _currentDay.value = (_currentDay.value ?: 1) + 1
        _burpeesDone.value = 0
        _burpeesLeft.value = _totalBurpeesToday.value
        saveProgress()
    }
}
