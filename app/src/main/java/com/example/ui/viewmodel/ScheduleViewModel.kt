package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.entity.WeeklySplitEntity
import com.example.data.repository.WorkoutRepository
import com.example.util.StreakCalculator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate

/**
 * Dedicated ViewModel for 7-day recurring split allocation and rest-day safe streak evaluation.
 */
class ScheduleViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = WorkoutRepository(AppDatabase.getDatabase(application))

    val weeklySplits: StateFlow<List<WeeklySplitEntity>> = repository.allWeeklySplits
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val completedEpochDays: StateFlow<Set<Long>> = repository.completedDates
        .map { it.toSet() }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    // Rest-Day Safe Active Streak: rest days do NOT penalize or break the streak
    val activeStreak: StateFlow<Int> = combine(completedEpochDays, weeklySplits) { epochDays, splits ->
        val logDates = epochDays.map { LocalDate.ofEpochDay(it) }.toSet()
        val splitMap = splits.associateBy { DayOfWeek.of(it.dayOfWeek) }
        StreakCalculator.calculateActiveStreak(logDates, splitMap)
    }.flowOn(Dispatchers.Default)
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun saveWeeklySplit(splits: List<WeeklySplitEntity>) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveWeeklySplit(splits)
        }
    }
}
