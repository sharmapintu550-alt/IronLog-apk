package com.example.util

import android.util.Log
import androidx.compose.runtime.toMutableStateList
import com.example.ui.screens.ActiveSetType
import com.example.ui.screens.ActiveWorkoutExercise
import com.example.ui.screens.ActiveWorkoutSet
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

object ActiveWorkoutSerializer {

    fun serialize(exercises: List<ActiveWorkoutExercise>): String {
        return try {
            val arr = JSONArray()
            for (ex in exercises) {
                val exObj = JSONObject()
                exObj.put("id", ex.id)
                exObj.put("name", ex.name)
                exObj.put("primaryMuscle", ex.primaryMuscle)
                exObj.put("equipmentType", ex.equipmentType)
                exObj.put("note", ex.note)
                exObj.put("barbellWeightKg", ex.barbellWeightKg)
                exObj.put("isCompleted", ex.isCompleted)
                if (ex.setupMediaUri != null) exObj.put("setupMediaUri", ex.setupMediaUri)
                if (ex.machineCalibration != null) exObj.put("machineCalibration", ex.machineCalibration)

                val setsArr = JSONArray()
                for (s in ex.sets) {
                    val sObj = JSONObject()
                    sObj.put("id", s.id)
                    sObj.put("setNumber", s.setNumber)
                    sObj.put("setType", s.setType.name)
                    sObj.put("previous", s.previous)
                    sObj.put("weight", s.weight)
                    sObj.put("reps", s.reps)
                    sObj.put("isCompleted", s.isCompleted)
                    setsArr.put(sObj)
                }
                exObj.put("sets", setsArr)
                arr.put(exObj)
            }
            arr.toString()
        } catch (e: Exception) {
            Log.e("ActiveWorkoutSerializer", "Serialization error: ${e.message}", e)
            ""
        }
    }

    fun deserialize(json: String): List<ActiveWorkoutExercise> {
        val list = mutableListOf<ActiveWorkoutExercise>()
        if (json.isBlank()) return list
        try {
            val arr = JSONArray(json)
            for (i in 0 until arr.length()) {
                val exObj = arr.optJSONObject(i) ?: continue
                val id = exObj.optString("id", UUID.randomUUID().toString())
                val rawName = exObj.optString("name", "Exercise")
                val name = rawName.substringBefore("(").trim().ifBlank { "Exercise" }
                val primaryMuscle = exObj.optString("primaryMuscle", "Chest")
                val equipmentType = exObj.optString("equipmentType", "Barbell")
                val note = exObj.optString("note", "")
                val barbellWeightKg = exObj.optDouble("barbellWeightKg", 20.0)
                val isCompleted = exObj.optBoolean("isCompleted", false)
                val setupMediaUri = if (exObj.has("setupMediaUri") && !exObj.isNull("setupMediaUri")) exObj.optString("setupMediaUri") else null
                val machineCalibration = if (exObj.has("machineCalibration") && !exObj.isNull("machineCalibration")) exObj.optString("machineCalibration") else null

                val setsList = mutableListOf<ActiveWorkoutSet>()
                val setsArr = exObj.optJSONArray("sets")
                if (setsArr != null) {
                    for (j in 0 until setsArr.length()) {
                        val sObj = setsArr.optJSONObject(j) ?: continue
                        val sId = sObj.optString("id", UUID.randomUUID().toString())
                        val setNumber = sObj.optInt("setNumber", j + 1)
                        val setTypeName = sObj.optString("setType", "NORMAL")
                        val setType = try {
                            ActiveSetType.valueOf(setTypeName)
                        } catch (e: Exception) {
                            ActiveSetType.NORMAL
                        }
                        val previous = sObj.optString("previous", "")
                        val weight = sObj.optString("weight", "")
                        val reps = sObj.optString("reps", "")
                        val isSetCompleted = sObj.optBoolean("isCompleted", false)

                        setsList.add(
                            ActiveWorkoutSet(
                                id = sId,
                                setNumber = setNumber,
                                setType = setType,
                                previous = previous,
                                weight = weight,
                                reps = reps,
                                isCompleted = isSetCompleted
                            )
                        )
                    }
                }

                if (setsList.isEmpty()) {
                    setsList.add(
                        ActiveWorkoutSet(
                            id = UUID.randomUUID().toString(),
                            setNumber = 1,
                            setType = ActiveSetType.NORMAL,
                            previous = "—",
                            weight = "",
                            reps = "",
                            isCompleted = false
                        )
                    )
                }

                list.add(
                    ActiveWorkoutExercise(
                        id = id,
                        name = name,
                        primaryMuscle = primaryMuscle,
                        equipmentType = equipmentType,
                        note = note,
                        sets = setsList.toMutableStateList(),
                        initialBarbellWeightKg = barbellWeightKg,
                        initialSetupMediaUri = setupMediaUri,
                        initialMachineCalibration = machineCalibration,
                        initialIsCompleted = isCompleted
                    )
                )
            }
        } catch (e: Exception) {
            Log.e("ActiveWorkoutSerializer", "Deserialization error: ${e.message}", e)
        }
        return list
    }
}
