package com.bharath.homeforge.data

import com.bharath.homeforge.domain.LevelProgress
import com.bharath.homeforge.domain.LevelStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/** The level in use right now: the one you chose, or the one your completed workouts have earned. */
fun WorkoutDao.observeLevelStatus(prefs: Flow<UserPrefs>): Flow<LevelStatus> =
    combine(prefs, observeWorkoutCount()) { p, workouts -> LevelProgress.status(p.level, p.autoLevel, workouts) }
