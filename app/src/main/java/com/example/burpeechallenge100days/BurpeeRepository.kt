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
        private val LAST_RECORDED_DATE = longPreferencesKey("last_recorded_date")
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

    suspend fun getLastRecordedDate(): Long {
        return context.dataStore.data.map { preferences ->
            preferences[LAST_RECORDED_DATE] ?: 0L
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

    suspend fun saveLastRecordedDate(date: Long) {
        context.dataStore.edit { preferences ->
            preferences[LAST_RECORDED_DATE] = date
        }
    }
}
