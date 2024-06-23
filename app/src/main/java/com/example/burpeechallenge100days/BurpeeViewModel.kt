package com.example.burpeechallenge100days

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

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

    init {
        viewModelScope.launch {
            loadData()
        }
    }

    private suspend fun loadData() {
        _currentDay.value = repository.getCurrentDay()
        _totalBurpeesToday.value = _currentDay.value ?: 1
        _burpeesDone.value = repository.getBurpeesDoneToday()
        _burpeesLeft.value = (_totalBurpeesToday.value ?: 1) - (_burpeesDone.value ?: 0)
    }

    fun addBurpees(count: Int) {
        val currentDone = _burpeesDone.value ?: 0
        val newDone = currentDone + count
        _burpeesDone.value = newDone
        _burpeesLeft.value = (_totalBurpeesToday.value ?: 1) - newDone

        if (newDone >= (_totalBurpeesToday.value ?: 1)) {
            completeDay()
        }

        viewModelScope.launch {
            repository.saveBurpeesDoneToday(newDone)
        }
    }

    private fun completeDay() {
        viewModelScope.launch {
            val nextDay = (_currentDay.value ?: 1) + 1
            _currentDay.value = nextDay
            _totalBurpeesToday.value = nextDay
            _burpeesDone.value = 0
            _burpeesLeft.value = nextDay

            repository.saveCurrentDay(nextDay)
            repository.saveBurpeesDoneToday(0)

            if (nextDay > totalDays) {
                // Челлендж завершен, можно добавить логику для показа поздравления
            }
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
        }
    }
}