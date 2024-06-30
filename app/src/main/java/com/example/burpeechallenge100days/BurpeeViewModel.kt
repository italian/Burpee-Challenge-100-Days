package com.example.burpeechallenge100days

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@RequiresApi(Build.VERSION_CODES.O)
class BurpeeViewModel(private val repository: BurpeeRepository) : ViewModel() {

    private val _shouldShowResetDialog = MutableLiveData<Boolean>()
    val shouldShowResetDialog: LiveData<Boolean> = _shouldShowResetDialog

    private val _lastRecordedDate = MutableLiveData<LocalDate>()

    private val _totalDays = MutableLiveData<Int>()

    private val _currentDay = MutableLiveData<Int>()
    val currentDay: LiveData<Int> = _currentDay

    private val _totalBurpeesToday = MutableLiveData<Int>()
    val totalBurpeesToday: LiveData<Int> = _totalBurpeesToday

    private val _burpeesDone = MutableLiveData<Int>()
    val burpeesDone: LiveData<Int> = _burpeesDone

    private val _burpeesLeftBeforeYesterday = MutableLiveData<Int>()

    private val _burpeesLeft = MutableLiveData<Int>()
    val burpeesLeft: LiveData<Int> = _burpeesLeft

    init {
        viewModelScope.launch {
            loadData()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun loadData() {

        if (repository.isFirstRun()) {
            repository.setFirstRun(false)
            resetProgress()
            return
        }

        loadSavedData()

        val daysDifference = ChronoUnit.DAYS.between(_lastRecordedDate.value, LocalDate.now())

        when {
//            _lastRecordedDate.value == LocalDate.MIN -> {
//                // First run of the application
//                Unit
//            }
            daysDifference > 2L -> {
                // It's been over two days
                _shouldShowResetDialog.value = true
                resetProgress()
            }
            daysDifference == 1L -> {
                // It's been 1 day
                handleOneDayPassed()
            }
            daysDifference == 2L -> {
                // 2 days have passed
                handleTwoDaysPassed()
            }
            else -> {
                // loadSavedData()
                Unit
            }
        }
    }

    private suspend fun loadSavedData() {
        _lastRecordedDate.value = repository.getLastRecordedDate()
        _totalDays.value = repository.getTotalDays()
        _currentDay.value = repository.getCurrentDay()
        _totalBurpeesToday.value = repository.getTotalBurpeesToday()
        _burpeesDone.value = repository.getBurpeesDone()
        _burpeesLeftBeforeYesterday.value = repository.getBurpeesLeftBeforeYesterday()
        _burpeesLeft.value = repository.getBurpeesLeft()
    }

    private fun resetProgress() {
        viewModelScope.launch {
            _totalDays.value = 100
            _currentDay.value = 1
            _totalBurpeesToday.value = 1
            _burpeesDone.value = 0
            _burpeesLeftBeforeYesterday.value = 0
            _burpeesLeft.value = 1

            saveProgress()
        }
    }

    private suspend fun saveProgress() {
        repository.saveLastRecordedDate(LocalDate.now())
        repository.saveTotalDays(_totalDays.value ?: 100)
        repository.saveCurrentDay(_currentDay.value ?: 1)
        repository.saveTotalBurpeesToday(_totalBurpeesToday.value ?: 1)
        repository.saveBurpeesDone(_burpeesDone.value ?: 0)
        repository.saveBurpeesLeftBeforeYesterday(_burpeesLeftBeforeYesterday.value ?: 0)
        repository.saveBurpeesLeft(_burpeesLeft.value ?: 1)
    }

    private suspend fun handleOneDayPassed() {
        // loadSavedData()

        if (_burpeesLeft.value == 0) {
            _currentDay.value = _currentDay.value?.plus(1)
            _totalBurpeesToday.value = _totalBurpeesToday.value?.plus(1)
            _burpeesDone.value = 0
            _burpeesLeftBeforeYesterday.value = _burpeesLeft.value
            _burpeesLeft.value = _totalBurpeesToday.value
        } else if (_burpeesLeftBeforeYesterday.value == 0) {
            _totalDays.value = _totalDays.value?.plus(1)
            _currentDay.value = _currentDay.value?.plus(1)
            _burpeesDone.value = 0
            _burpeesLeftBeforeYesterday.value = _burpeesLeft.value
            _burpeesLeft.value = _totalBurpeesToday.value
        } else {
            _shouldShowResetDialog.value = true
            resetProgress()
        }
        saveProgress()
    }

    private suspend fun handleTwoDaysPassed() {
        // loadSavedData()

        if (_burpeesLeft.value == 0) {
            _totalDays.value = _totalDays.value?.plus(2)
            _currentDay.value = _currentDay.value?.plus(2)
            _totalBurpeesToday.value = _totalBurpeesToday.value?.plus(1)
            _burpeesDone.value = 0
            _burpeesLeftBeforeYesterday.value = _totalBurpeesToday.value
            _burpeesLeft.value = _totalBurpeesToday.value
        } else {
            _shouldShowResetDialog.value = true
            resetProgress()
        }
        saveProgress()
    }

    fun addBurpees(count: Int) {
        _burpeesDone.value = _burpeesDone.value?.plus(count)
        _burpeesLeft.value = _totalBurpeesToday.value?.minus(_burpeesDone.value!!)

        if (_burpeesLeft.value!! < 0) {
            _burpeesLeft.value = 0
        }

        viewModelScope.launch {
            repository.saveBurpeesDone(_burpeesDone.value!!)
            repository.saveBurpeesLeft(_burpeesLeft.value!!)
            repository.saveLastRecordedDate(LocalDate.now())
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
}
