package com.example.burpeechallenge100days

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "burpee_prefs")

class BurpeeRepository(private val context: Context) {

    companion object {
        private val FIRST_RUN = booleanPreferencesKey("first_run")
        private val LAST_RECORDED_DATE = longPreferencesKey("last_recorded_date")
        private val TOTAL_DAYS = intPreferencesKey("total_days")
        private val CURRENT_DAY = intPreferencesKey("current_day")
        private val TOTAL_BURPEES_TODAY = intPreferencesKey("total_burpees_today")
        private val BURPEES_DONE = intPreferencesKey("BURPEES_DONE")
        private val BURPEES_LEFT_BEFORE_YESTERDAY = intPreferencesKey("burpees_left_before_yesterday")
        private val BURPEES_LEFT = intPreferencesKey("burpees_left")
    }

    suspend fun isFirstRun(): Boolean {
        return context.dataStore.data.map { preferences ->
            preferences[FIRST_RUN] ?: true
        }.first()
    }

    suspend fun setFirstRun(isFirstRun: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[FIRST_RUN] = isFirstRun
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getLastRecordedDate(): LocalDate {
        return context.dataStore.data.map { preferences ->
            val millis = preferences[LAST_RECORDED_DATE]?: -1L // Using -1L as the default value
            if (millis == -1L) null
            else Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
        }.firstOrNull()?: LocalDate.MIN
    }

    suspend fun getTotalDays(): Int {
        return context.dataStore.data.map { preferences ->
            preferences[TOTAL_DAYS] ?: 100
        }.first()
    }

    suspend fun getCurrentDay(): Int {
        return context.dataStore.data.map { preferences ->
            preferences[CURRENT_DAY] ?: 1
        }.first()
    }

    suspend fun getTotalBurpeesToday(): Int {
        return context.dataStore.data.map { preferences ->
            preferences[TOTAL_BURPEES_TODAY] ?: 1
        }.first()
    }

    suspend fun getBurpeesDone(): Int {
        return context.dataStore.data.map { preferences ->
            preferences[BURPEES_DONE] ?: 0
        }.first()
    }

    suspend fun getBurpeesLeftBeforeYesterday(): Int {
        return context.dataStore.data.map { preferences ->
            preferences[BURPEES_LEFT_BEFORE_YESTERDAY] ?: 0
        }.first()
    }

    suspend fun getBurpeesLeft(): Int {
        return context.dataStore.data.map { preferences ->
            preferences[BURPEES_LEFT] ?: 0
        }.first()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun saveLastRecordedDate(date: LocalDate) {
        val millis = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        context.dataStore.edit { preferences ->
            preferences[LAST_RECORDED_DATE] = millis
        }
    }

    suspend fun saveTotalDays(days: Int) {
        context.dataStore.edit { preferences ->
            preferences[TOTAL_DAYS] = days
        }
    }

    suspend fun saveCurrentDay(day: Int) {
        context.dataStore.edit { preferences ->
            preferences[CURRENT_DAY] = day
        }
    }

    suspend fun saveTotalBurpeesToday(count: Int) {
        context.dataStore.edit { preferences ->
            preferences[TOTAL_BURPEES_TODAY] = count
        }
    }

    suspend fun saveBurpeesDone(count: Int) {
        context.dataStore.edit { preferences ->
            preferences[BURPEES_DONE] = count
        }
    }

    suspend fun saveBurpeesLeftBeforeYesterday(count: Int) {
        context.dataStore.edit { preferences ->
            preferences[BURPEES_LEFT_BEFORE_YESTERDAY] = count
        }
    }

    suspend fun saveBurpeesLeft(count: Int) {
        context.dataStore.edit { preferences ->
            preferences[BURPEES_LEFT] = count
        }
    }
}
