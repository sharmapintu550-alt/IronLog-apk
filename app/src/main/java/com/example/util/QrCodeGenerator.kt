package com.example.util

import android.graphics.Bitmap
import android.util.Base64
import com.example.data.entity.ExerciseEntity
import com.example.data.entity.RoutineEntity
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.net.URLDecoder
import java.net.URLEncoder
import java.util.UUID
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

data class SharedExerciseSet(
    val setNumber: Int = 1,
    val reps: Int = 10,
    val weightKg: Double = 0.0,
    val setType: String = "NORMAL"
)

data class SharedRoutineExercise(
    val id: String = "",
    val name: String,
    val exerciseName: String = name,
    val muscle: String = "Full Body",
    val targetMuscle: String = muscle,
    val equipment: String = "Barbell",
    val defaultSets: Int = 3,
    val targetReps: String = "8-12",
    val notes: String = "",
    val sets: List<SharedExerciseSet> = emptyList()
)

data class SharedRoutinePayload(
    val version: Int = 2,
    val routineName: String,
    val name: String = routineName,
    val description: String = "",
    val notes: String = description,
    val exercisesSummary: String = "",
    val defaultSets: Int = 3,
    val targetReps: String = "8-12",
    val exerciseIds: List<String> = emptyList(),
    val exercises: List<SharedRoutineExercise> = emptyList()
)

object QrCodeGenerator {

    private const val AMOLED_BG = 0xFF121212.toInt()
    private const val ELECTRIC_BLUE = 0xFF1877F2.toInt()
    private const val MATRIX_WHITE = 0xFFFFFFFF.toInt()
    private const val MATRIX_BLACK = 0xFF000000.toInt()

    /**
     * Generates a crisp standard QR code Bitmap:
     * Pure white background, pitch-black foreground modules,
     * and UTF-8 encoding with quiet zone margins.
     */
    fun generateStandardQrBitmap(
        content: String,
        targetSizePx: Int = 512
    ): Bitmap {
        val hints = mapOf(
            EncodeHintType.CHARACTER_SET to "UTF-8",
            EncodeHintType.MARGIN to 1,
            EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M
        )

        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, 0, 0, hints)
        val matrixWidth = bitMatrix.width
        val matrixHeight = bitMatrix.height

        val scale = (targetSizePx / matrixWidth).coerceAtLeast(4)
        val outputWidth = matrixWidth * scale
        val outputHeight = matrixHeight * scale

        val bitmap = Bitmap.createBitmap(outputWidth, outputHeight, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(outputWidth * outputHeight)

        for (my in 0 until matrixHeight) {
            for (mx in 0 until matrixWidth) {
                val isModuleDark = bitMatrix.get(mx, my)
                val moduleColor = if (isModuleDark) MATRIX_BLACK else MATRIX_WHITE

                for (sy in 0 until scale) {
                    val pixelY = my * scale + sy
                    val rowOffset = pixelY * outputWidth
                    for (sx in 0 until scale) {
                        val pixelX = mx * scale + sx
                        pixels[rowOffset + pixelX] = moduleColor
                    }
                }
            }
        }

        bitmap.setPixels(pixels, 0, outputWidth, 0, 0, outputWidth, outputHeight)
        return bitmap
    }

    /**
     * Generates a high-contrast inverted QR code Bitmap:
     * AMOLED #121212 dark background, pure white #FFFFFF data modules,
     * and Electric Blue #1877F2 corner finder eyes.
     */
    fun generateAmoledQrBitmap(
        content: String,
        targetSizePx: Int = 512
    ): Bitmap {
        val hints = mapOf(
            EncodeHintType.CHARACTER_SET to "UTF-8",
            EncodeHintType.MARGIN to 1,
            EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M
        )

        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, 0, 0, hints)
        val matrixWidth = bitMatrix.width
        val matrixHeight = bitMatrix.height

        // Determine integer scale factor to maintain sharp square modules
        val scale = (targetSizePx / matrixWidth).coerceAtLeast(4)
        val outputWidth = matrixWidth * scale
        val outputHeight = matrixHeight * scale

        val bitmap = Bitmap.createBitmap(outputWidth, outputHeight, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(outputWidth * outputHeight)

        for (my in 0 until matrixHeight) {
            for (mx in 0 until matrixWidth) {
                val isModuleDark = bitMatrix.get(mx, my)

                // Finder patterns are 7x7 modules at 3 corners:
                // (0..6, 0..6), (matrixWidth-7..matrixWidth-1, 0..6), (0..6, matrixHeight-7..matrixHeight-1)
                val isTopLeftFinder = mx < 7 && my < 7
                val isTopRightFinder = mx >= (matrixWidth - 7) && my < 7
                val isBottomLeftFinder = mx < 7 && my >= (matrixHeight - 7)
                val isFinderEye = isTopLeftFinder || isTopRightFinder || isBottomLeftFinder

                val moduleColor = when {
                    !isModuleDark -> AMOLED_BG
                    isFinderEye -> ELECTRIC_BLUE
                    else -> MATRIX_WHITE
                }

                // Fill the scaled block for this module
                for (sy in 0 until scale) {
                    val pixelY = my * scale + sy
                    val rowOffset = pixelY * outputWidth
                    for (sx in 0 until scale) {
                        val pixelX = mx * scale + sx
                        pixels[rowOffset + pixelX] = moduleColor
                    }
                }
            }
        }

        bitmap.setPixels(pixels, 0, outputWidth, 0, 0, outputWidth, outputHeight)
        return bitmap
    }

    /**
     * Builds a compact JSON payload from a RoutineEntity and resolved database exercises.
     */
    fun buildRoutinePayload(
        routine: RoutineEntity,
        allDbExercises: List<ExerciseEntity>
    ): SharedRoutinePayload {
        val names = routine.exercisesSummary.split(",")
            .map { it.trim() }
            .filter { it.isNotBlank() }

        val exerciseList = mutableListOf<SharedRoutineExercise>()
        val exerciseIds = mutableListOf<String>()

        names.forEachIndexed { index, name ->
            val matchedDb = allDbExercises.find { it.name.equals(name, ignoreCase = true) }
            val exId = matchedDb?.id ?: "ex_${index}_${name.lowercase().replace(" ", "_")}"
            val muscle = matchedDb?.primaryMuscle?.takeIf { it.isNotBlank() }
                ?: matchedDb?.muscleGroup?.takeIf { it.isNotBlank() }
                ?: "Full Body"
            val equipment = matchedDb?.equipmentType?.takeIf { it.isNotBlank() }
                ?: matchedDb?.equipment?.takeIf { it.isNotBlank() }
                ?: "Barbell"

            exerciseIds.add(exId)
            val defaultSetsList = listOf(
                SharedExerciseSet(setNumber = 1, reps = 10, weightKg = 0.0, setType = "NORMAL"),
                SharedExerciseSet(setNumber = 2, reps = 10, weightKg = 0.0, setType = "NORMAL"),
                SharedExerciseSet(setNumber = 3, reps = 10, weightKg = 0.0, setType = "NORMAL")
            )

            exerciseList.add(
                SharedRoutineExercise(
                    id = exId,
                    name = name,
                    exerciseName = name,
                    muscle = muscle,
                    targetMuscle = muscle,
                    equipment = equipment,
                    defaultSets = 3,
                    targetReps = "8-12",
                    notes = "",
                    sets = defaultSetsList
                )
            )
        }

        return SharedRoutinePayload(
            version = 2,
            routineName = routine.title,
            name = routine.title,
            description = routine.description,
            notes = routine.description,
            exercisesSummary = routine.exercisesSummary,
            defaultSets = 3,
            targetReps = "8-12",
            exerciseIds = exerciseIds,
            exercises = exerciseList
        )
    }

    /**
     * Serializes payload to lightweight JSON string with compact field names.
     */
    fun serializeToJson(payload: SharedRoutinePayload): String {
        val obj = JSONObject()
        obj.put("v", payload.version)
        obj.put("name", payload.routineName)
        obj.put("routine_name", payload.routineName)
        obj.put("notes", payload.description)
        obj.put("description", payload.description)
        obj.put("exercises_summary", payload.exercisesSummary)
        obj.put("default_sets", payload.defaultSets)
        obj.put("target_reps", payload.targetReps)

        val idArray = JSONArray()
        payload.exerciseIds.forEach { idArray.put(it) }
        obj.put("exercise_ids", idArray)

        val exArray = JSONArray()
        payload.exercises.forEach { ex ->
            val eObj = JSONObject()
            eObj.put("id", ex.id)
            eObj.put("name", ex.name)
            eObj.put("exerciseName", ex.name)
            eObj.put("muscle", ex.muscle)
            eObj.put("targetMuscle", ex.muscle)
            eObj.put("equipment", ex.equipment)
            eObj.put("default_sets", ex.defaultSets)
            eObj.put("target_reps", ex.targetReps)
            eObj.put("notes", ex.notes)

            val setsArray = JSONArray()
            val setsToUse = if (ex.sets.isNotEmpty()) {
                ex.sets
            } else {
                (1..ex.defaultSets).map { SharedExerciseSet(setNumber = it, reps = 10, weightKg = 0.0, setType = "NORMAL") }
            }
            setsToUse.forEach { s ->
                val sObj = JSONObject()
                sObj.put("setNumber", s.setNumber)
                sObj.put("reps", s.reps)
                sObj.put("weightKg", s.weightKg)
                sObj.put("setType", s.setType)
                setsArray.put(sObj)
            }
            eObj.put("sets", setsArray)
            exArray.put(eObj)
        }
        obj.put("exercises", exArray)

        return obj.toString()
    }

    /**
     * Compresses the input string using GZIP and encodes it to a URL-safe Base64 string.
     */
    fun compressAndEncode(jsonString: String): String {
        return try {
            val bos = ByteArrayOutputStream()
            GZIPOutputStream(bos).use { gzip ->
                gzip.write(jsonString.toByteArray(Charsets.UTF_8))
            }
            Base64.encodeToString(bos.toByteArray(), Base64.NO_WRAP or Base64.URL_SAFE)
        } catch (e: Exception) {
            encodeToBase64(jsonString)
        }
    }

    /**
     * Decodes Base64 and decompresses GZIP data if present.
     */
    fun decodeAndDecompress(input: String): String? {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return null

        if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
            return trimmed
        }

        return try {
            val decodedBytes = Base64.decode(trimmed, Base64.DEFAULT or Base64.URL_SAFE)
            // Check for GZIP magic numbers: 0x1f (31) and 0x8b (-117)
            if (decodedBytes.size >= 2 && decodedBytes[0] == 0x1f.toByte() && decodedBytes[1] == 0x8b.toByte()) {
                GZIPInputStream(ByteArrayInputStream(decodedBytes)).bufferedReader(Charsets.UTF_8).use { it.readText() }
            } else {
                String(decodedBytes, Charsets.UTF_8)
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Fallback standard Base64 encoding.
     */
    fun encodeToBase64(jsonString: String): String {
        return Base64.encodeToString(jsonString.toByteArray(Charsets.UTF_8), Base64.NO_WRAP or Base64.URL_SAFE)
    }

    /**
     * Creates an app deep link: ironlog://routine?data=<BASE64_GZIP_OR_URL_SAFE_JSON>
     */
    fun createDeepLink(payload: SharedRoutinePayload): String {
        val json = serializeToJson(payload)
        val compressed = compressAndEncode(json)
        val encodedParam = URLEncoder.encode(compressed, "UTF-8")
        return "ironlog://routine?data=$encodedParam"
    }

    /**
     * Decodes routine payload from raw input (Deep link URI, Base64 GZIP, or direct JSON).
     */
    fun parseInput(rawInput: String): SharedRoutinePayload? {
        val trimmed = rawInput.trim()
        if (trimmed.isEmpty()) return null

        try {
            // Check if it's a deep link URI: ironlog://routine?data=... or ironlog://routine/share?data=...
            val payloadParam = if (trimmed.startsWith("ironlog://", ignoreCase = true) || trimmed.contains("routine", ignoreCase = true)) {
                val dataParam = if (trimmed.contains("data=")) {
                    trimmed.substringAfter("data=").substringBefore("&")
                } else {
                    trimmed.substringAfterLast("/")
                }
                try {
                    URLDecoder.decode(dataParam, "UTF-8")
                } catch (e: Exception) {
                    dataParam
                }
            } else {
                trimmed
            }

            val jsonString = decodeAndDecompress(payloadParam) ?: payloadParam
            if (jsonString.startsWith("{") && jsonString.endsWith("}")) {
                return parseJsonString(jsonString)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private fun parseJsonString(jsonString: String): SharedRoutinePayload? {
        return try {
            val obj = JSONObject(jsonString)
            val version = obj.optInt("v", obj.optInt("version", 2))
            val routineName = obj.optString("name", obj.optString("routine_name", obj.optString("title", "Imported Routine")))
            val description = obj.optString("notes", obj.optString("description", ""))
            var exercisesSummary = obj.optString("exercises_summary", "")
            val defaultSets = obj.optInt("default_sets", 3)
            val targetReps = obj.optString("target_reps", "8-12")
            val notes = obj.optString("notes", description)

            val exerciseIds = mutableListOf<String>()
            val idsArray = obj.optJSONArray("exercise_ids")
            if (idsArray != null) {
                for (i in 0 until idsArray.length()) {
                    exerciseIds.add(idsArray.getString(i))
                }
            }

            val exercises = mutableListOf<SharedRoutineExercise>()
            val exArray = obj.optJSONArray("exercises")
            if (exArray != null && exArray.length() > 0) {
                for (i in 0 until exArray.length()) {
                    val eObj = exArray.getJSONObject(i)
                    val exName = eObj.optString("exerciseName", eObj.optString("name", "Exercise ${i + 1}"))
                    val exMuscle = eObj.optString("targetMuscle", eObj.optString("muscle", "Full Body"))
                    val exEquipment = eObj.optString("equipment", "Barbell")
                    val exDefaultSets = eObj.optInt("default_sets", defaultSets)
                    val exTargetReps = eObj.optString("target_reps", targetReps)
                    val exNotes = eObj.optString("notes", "")

                    val setsList = mutableListOf<SharedExerciseSet>()
                    val setsArray = eObj.optJSONArray("sets")
                    if (setsArray != null && setsArray.length() > 0) {
                        for (sIdx in 0 until setsArray.length()) {
                            val sObj = setsArray.getJSONObject(sIdx)
                            setsList.add(
                                SharedExerciseSet(
                                    setNumber = sObj.optInt("setNumber", sObj.optInt("set_number", sIdx + 1)),
                                    reps = sObj.optInt("reps", 10),
                                    weightKg = sObj.optDouble("weightKg", sObj.optDouble("weight_kg", sObj.optDouble("weight", 0.0))),
                                    setType = sObj.optString("setType", sObj.optString("set_type", "NORMAL"))
                                )
                            )
                        }
                    } else {
                        for (sIdx in 1..exDefaultSets) {
                            setsList.add(SharedExerciseSet(setNumber = sIdx, reps = 10, weightKg = 0.0, setType = "NORMAL"))
                        }
                    }

                    exercises.add(
                        SharedRoutineExercise(
                            id = eObj.optString("id", "ex_$i"),
                            name = exName,
                            exerciseName = exName,
                            muscle = exMuscle,
                            targetMuscle = exMuscle,
                            equipment = exEquipment,
                            defaultSets = exDefaultSets,
                            targetReps = exTargetReps,
                            notes = exNotes,
                            sets = setsList
                        )
                    )
                }
            }

            // Fallback: If exercises array was empty, parse from exercisesSummary
            if (exercises.isEmpty() && exercisesSummary.isNotBlank()) {
                val names = exercisesSummary.split(",").map { it.trim() }.filter { it.isNotBlank() }
                names.forEachIndexed { i, name ->
                    val exId = if (i < exerciseIds.size) exerciseIds[i] else "ex_${UUID.randomUUID().toString().take(6)}"
                    exercises.add(
                        SharedRoutineExercise(
                            id = exId,
                            name = name,
                            exerciseName = name,
                            muscle = "Full Body",
                            targetMuscle = "Full Body",
                            equipment = "Barbell",
                            defaultSets = defaultSets,
                            targetReps = targetReps,
                            notes = notes,
                            sets = (1..defaultSets).map { SharedExerciseSet(setNumber = it, reps = 10, weightKg = 0.0, setType = "NORMAL") }
                        )
                    )
                }
            }

            if (exercisesSummary.isBlank() && exercises.isNotEmpty()) {
                exercisesSummary = exercises.joinToString(", ") { it.name }
            }

            SharedRoutinePayload(
                version = version,
                routineName = routineName,
                name = routineName,
                description = description,
                notes = notes,
                exercisesSummary = exercisesSummary,
                defaultSets = defaultSets,
                targetReps = targetReps,
                exerciseIds = exerciseIds,
                exercises = exercises
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
