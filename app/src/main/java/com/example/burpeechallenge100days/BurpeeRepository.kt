package com.example.burpeechallenge100days

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "burpee_prefs")

class BurpeeRepository(private val context: Context) {

    companion object {
        private val CURRENT_DAY = intPreferencesKey("current_day")
        private val BURPEES_DONE_TODAY = intPreferencesKey("burpees_done_today")
        private val BURPEES_LEFT = intPreferencesKey("burpees_left")
        private val LAST_RECORDED_DATE = longPreferencesKey("last_recorded_date")
        private val BURPEES_LEFT_BEFORE_YESTERDAY = intPreferencesKey("burpees_left_before_yesterday")
        private val TOTAL_DAYS = intPreferencesKey("total_days")
        private val TOTAL_BURPEES_TODAY = intPreferencesKey("total_burpees_today")
    }

    suspend fun getCurrentDay(): Int {
        return context.dataStore.data.map { preferences ->
            preferences[CURRENT_DAY] ?: 1
        }.first()
    }

    suspend fun getBurpeesDoneToday(): Int {
        return context.dataStore.data.map { preferences ->
            preferences[BURPEES_DONE_TODAY] ?: 0
        }.first()
    }

    suspend fun getBurpeesLeft(): Int {
        return context.dataStore.data.map { preferences ->
            preferences[BURPEES_LEFT] ?: 0
        }.first()
    }

    suspend fun getLastRecordedDate(): Long {
        return context.dataStore.data.map { preferences ->
            preferences[LAST_RECORDED_DATE] ?: 0L
        }.first()
    }

    suspend fun getBurpeesLeftBeforeYesterday(): Int {
        return context.dataStore.data.map { preferences ->
            preferences[BURPEES_LEFT_BEFORE_YESTERDAY] ?: 0
        }.first()
    }

    suspend fun getTotalDays(): Int {
        return context.dataStore.data.map { preferences ->
            preferences[TOTAL_DAYS] ?: 100
        }.first()
    }

    suspend fun getTotalBurpeesToday(): Int {
        return context.dataStore.data.map { preferences ->
            preferences[TOTAL_BURPEES_TODAY] ?: 1
        }.first()
    }

    suspend fun saveCurrentDay(day: Int) {
        context.dataStore.edit { preferences ->
            preferences[CURRENT_DAY] = day
        }
    }

    suspend fun saveBurpeesDoneToday(count: Int) {
        context.dataStore.edit { preferences ->
            preferences[BURPEES_DONE_TODAY] = count
        }
    }

    suspend fun saveBurpeesLeft(count: Int) {
        context.dataStore.edit { preferences ->
            preferences[BURPEES_LEFT] = count
        }
    }

    suspend fun saveLastRecordedDate(date: Long) {
        context.dataStore.edit { preferences ->
            preferences[LAST_RECORDED_DATE] = date
        }
    }

//    suspend fun saveBurpeesLeftBeforeYesterday(count: Int) {
//        context.dataStore.edit { preferences ->
//            preferences[BURPEES_LEFT_BEFORE_YESTERDAY] = count
//        }
//    }

    suspend fun saveTotalDays(days: Int) {
        context.dataStore.edit { preferences ->
            preferences[TOTAL_DAYS] = days
        }
    }

    suspend fun saveTotalBurpeesToday(count: Int) {
        context.dataStore.edit { preferences ->
            preferences[TOTAL_BURPEES_TODAY] = count
        }
    }
}
