package com.example.ui.components

/**
 * Provides CDN illustration URLs from the open-source free-exercise-db repository.
 * Formats: https://cdn.jsdelivr.net/gh/yuhonas/free-exercise-db@main/exercises/{Exercise_Folder}/0.jpg
 */
object ExerciseImageProvider {

    private const val CDN_BASE = "https://cdn.jsdelivr.net/gh/yuhonas/free-exercise-db@main/exercises"

    /**
     * Map of exact exercise names to their corresponding CDN illustration URLs.
     */
    val EXACT_URLS: Map<String, String> = mapOf(
        // Prompt specific patterns
        "Barbell Back Squat" to "$CDN_BASE/Barbell_Squat/0.jpg",
        "Barbell Squat" to "$CDN_BASE/Barbell_Squat/0.jpg",
        "Romanian Deadlift" to "$CDN_BASE/Romanian_Deadlift/0.jpg",
        "Leg Extension" to "$CDN_BASE/Leg_Extensions/0.jpg",
        "Leg Extensions" to "$CDN_BASE/Leg_Extensions/0.jpg",
        "Lying Leg Curl" to "$CDN_BASE/Lying_Leg_Curls/0.jpg",
        "Lying Leg Curls" to "$CDN_BASE/Lying_Leg_Curls/0.jpg",
        "Seated Leg Curl" to "$CDN_BASE/Seated_Leg_Curl/0.jpg",
        "Seated Machine Shoulder Press" to "$CDN_BASE/Machine_Shoulder_Press/0.jpg",
        "Dumbbell Lateral Raise" to "$CDN_BASE/Side_Lateral_Raise/0.jpg",
        "Lateral Raise" to "$CDN_BASE/Side_Lateral_Raise/0.jpg",
        "Rear Delt Reverse Pec Deck" to "$CDN_BASE/Rear_Delt_Machine/0.jpg",
        "Cable Pushdown" to "$CDN_BASE/Triceps_Pushdown/0.jpg",
        "Barbell Bench Press" to "$CDN_BASE/Barbell_Bench_Press/0.jpg",
        "Flat Barbell Bench Press" to "$CDN_BASE/Barbell_Bench_Press_-_Medium_Grip/0.jpg",

        // Chest
        "Incline Dumbbell Press" to "$CDN_BASE/Incline_Dumbbell_Press/0.jpg",
        "Incline Barbell Bench Press" to "$CDN_BASE/Barbell_Incline_Bench_Press_-_Medium_Grip/0.jpg",
        "Incline Barbell Bench Press - 30°" to "$CDN_BASE/Barbell_Incline_Bench_Press_-_Medium_Grip/0.jpg",
        "Incline Barbell Bench Press - 45°" to "$CDN_BASE/Barbell_Incline_Bench_Press_-_Medium_Grip/0.jpg",
        "Decline Barbell Bench Press" to "$CDN_BASE/Decline_Barbell_Bench_Press/0.jpg",
        "Dumbbell Bench Press" to "$CDN_BASE/Dumbbell_Bench_Press/0.jpg",
        "Cable Chest Fly" to "$CDN_BASE/Cable_Crossover/0.jpg",
        "Cable Crossover" to "$CDN_BASE/Cable_Crossover/0.jpg",
        "Dips" to "$CDN_BASE/Chest_Dip/0.jpg",
        "Chest Dips" to "$CDN_BASE/Chest_Dip/0.jpg",
        "Push-ups" to "$CDN_BASE/Pushups/0.jpg",
        "Machine Chest Press" to "$CDN_BASE/Machine_Bench_Press/0.jpg",
        "Pec Deck Fly" to "$CDN_BASE/Butterfly/0.jpg",

        // Back
        "Conventional Deadlift" to "$CDN_BASE/Barbell_Deadlift/0.jpg",
        "Barbell Deadlift" to "$CDN_BASE/Barbell_Deadlift/0.jpg",
        "Deadlift" to "$CDN_BASE/Barbell_Deadlift/0.jpg",
        "Barbell Bent-Over Row" to "$CDN_BASE/Bent_Over_Barbell_Row/0.jpg",
        "Bent Over Row" to "$CDN_BASE/Bent_Over_Barbell_Row/0.jpg",
        "Lat Pulldown" to "$CDN_BASE/Wide-Grip_Lat_Pulldown/0.jpg",
        "Pull-up" to "$CDN_BASE/Pullups/0.jpg",
        "Pull-ups" to "$CDN_BASE/Pullups/0.jpg",
        "Chin-up" to "$CDN_BASE/Chin-Up/0.jpg",
        "Seated Cable Row" to "$CDN_BASE/Seated_Cable_Rows/0.jpg",
        "T-Bar Row" to "$CDN_BASE/T-Bar_Row_with_Handle/0.jpg",
        "One-Arm Dumbbell Row" to "$CDN_BASE/One-Arm_Dumbbell_Row/0.jpg",

        // Legs
        "Leg Press" to "$CDN_BASE/Leg_Press/0.jpg",
        "Bulgarian Split Squat" to "$CDN_BASE/Split_Squats/0.jpg",
        "Standing Calf Raise" to "$CDN_BASE/Standing_Calf_Raises/0.jpg",
        "Seated Calf Raise" to "$CDN_BASE/Seated_Calf_Raise/0.jpg",
        "Hack Squat" to "$CDN_BASE/Barbell_Hack_Squat/0.jpg",
        "Goblet Squat" to "$CDN_BASE/Goblet_Squat/0.jpg",
        "Front Squat" to "$CDN_BASE/Front_Barbell_Squat/0.jpg",

        // Shoulders
        "Barbell Overhead Press" to "$CDN_BASE/Overhead_Press/0.jpg",
        "Overhead Press" to "$CDN_BASE/Overhead_Press/0.jpg",
        "Standing Military Press" to "$CDN_BASE/Standing_Military_Press/0.jpg",
        "Dumbbell Shoulder Press" to "$CDN_BASE/Dumbbell_Shoulder_Press/0.jpg",
        "Face Pull" to "$CDN_BASE/Face_Pull/0.jpg",
        "Rear Delt Fly" to "$CDN_BASE/Cable_Rear_Delt_Fly/0.jpg",
        "Arnold Press" to "$CDN_BASE/Arnold_Dumbbell_Press/0.jpg",
        "Upright Row" to "$CDN_BASE/Upright_Barbell_Row/0.jpg",

        // Arms - Biceps
        "Incline Dumbbell Curl" to "$CDN_BASE/Incline_Dumbbell_Curl/0.jpg",
        "Barbell Bicep Curl" to "$CDN_BASE/Barbell_Curl/0.jpg",
        "Barbell Curl" to "$CDN_BASE/Barbell_Curl/0.jpg",
        "Bicep Curl" to "$CDN_BASE/Barbell_Curl/0.jpg",
        "Dumbbell Bicep Curl" to "$CDN_BASE/Dumbbell_Bicep_Curl/0.jpg",
        "Hammer Curl" to "$CDN_BASE/Hammer_Curls/0.jpg",
        "Hammer Curls" to "$CDN_BASE/Hammer_Curls/0.jpg",
        "Preacher Curl" to "$CDN_BASE/Preacher_Curl/0.jpg",
        "Concentration Curl" to "$CDN_BASE/Concentration_Curls/0.jpg",

        // Arms - Triceps
        "Tricep Rope Pushdown" to "$CDN_BASE/Triceps_Pushdown/0.jpg",
        "Triceps Pushdown" to "$CDN_BASE/Triceps_Pushdown/0.jpg",
        "Skull Crushers" to "$CDN_BASE/EZ-Bar_Skullcrusher/0.jpg",
        "Overhead Tricep Extension" to "$CDN_BASE/Standing_Dumbbell_Triceps_Extension/0.jpg",
        "Close-Grip Bench Press" to "$CDN_BASE/Close-Grip_Barbell_Bench_Press/0.jpg",
        "Tricep Dips" to "$CDN_BASE/Bench_Dips/0.jpg",

        // Core
        "Hanging Leg Raise" to "$CDN_BASE/Hanging_Leg_Raise/0.jpg",
        "Ab Wheel Rollout" to "$CDN_BASE/Ab_Roller/0.jpg",
        "Plank" to "$CDN_BASE/Plank/0.jpg",
        "Cable Crunch" to "$CDN_BASE/Cable_Crunch/0.jpg",
        "Russian Twist" to "$CDN_BASE/Russian_Twist/0.jpg"
    )

    /**
     * Resolves a CDN URL for an exercise name, checking exact match first,
     * followed by keyword-based matching.
     */
    fun getImageUrl(exerciseName: String): String? {
        val trimmed = exerciseName.trim()
        EXACT_URLS[trimmed]?.let { return it }

        // Case-insensitive exact lookup
        for ((name, url) in EXACT_URLS) {
            if (name.equals(trimmed, ignoreCase = true)) {
                return url
            }
        }

        val lower = trimmed.lowercase()

        // Keyword based fallback
        return when {
            "squat" in lower && "front" in lower -> "$CDN_BASE/Front_Barbell_Squat/0.jpg"
            "squat" in lower && ("goblet" in lower || "dumbbell" in lower) -> "$CDN_BASE/Goblet_Squat/0.jpg"
            "squat" in lower && "hack" in lower -> "$CDN_BASE/Barbell_Hack_Squat/0.jpg"
            "squat" in lower && "split" in lower -> "$CDN_BASE/Split_Squats/0.jpg"
            "squat" in lower -> "$CDN_BASE/Barbell_Squat/0.jpg"
            "deadlift" in lower && "romanian" in lower -> "$CDN_BASE/Romanian_Deadlift/0.jpg"
            "deadlift" in lower && "rdl" in lower -> "$CDN_BASE/Romanian_Deadlift/0.jpg"
            "deadlift" in lower -> "$CDN_BASE/Barbell_Deadlift/0.jpg"
            "bench press" in lower && "incline" in lower -> "$CDN_BASE/Barbell_Incline_Bench_Press_-_Medium_Grip/0.jpg"
            "bench press" in lower && "decline" in lower -> "$CDN_BASE/Decline_Barbell_Bench_Press/0.jpg"
            "bench press" in lower && "close" in lower -> "$CDN_BASE/Close-Grip_Barbell_Bench_Press/0.jpg"
            "bench press" in lower && "dumbbell" in lower -> "$CDN_BASE/Dumbbell_Bench_Press/0.jpg"
            "bench press" in lower -> "$CDN_BASE/Barbell_Bench_Press/0.jpg"
            "chest press" in lower -> "$CDN_BASE/Machine_Bench_Press/0.jpg"
            "pushdown" in lower -> "$CDN_BASE/Triceps_Pushdown/0.jpg"
            "skull crusher" in lower || "skullcrusher" in lower -> "$CDN_BASE/EZ-Bar_Skullcrusher/0.jpg"
            "leg extension" in lower -> "$CDN_BASE/Leg_Extensions/0.jpg"
            "leg curl" in lower && "seated" in lower -> "$CDN_BASE/Seated_Leg_Curl/0.jpg"
            "leg curl" in lower && "lying" in lower -> "$CDN_BASE/Lying_Leg_Curls/0.jpg"
            "leg curl" in lower -> "$CDN_BASE/Seated_Leg_Curl/0.jpg"
            "leg press" in lower -> "$CDN_BASE/Leg_Press/0.jpg"
            "calf raise" in lower -> "$CDN_BASE/Standing_Calf_Raises/0.jpg"
            "lateral raise" in lower || "side raise" in lower -> "$CDN_BASE/Side_Lateral_Raise/0.jpg"
            "shoulder press" in lower && "machine" in lower -> "$CDN_BASE/Machine_Shoulder_Press/0.jpg"
            "shoulder press" in lower && "dumbbell" in lower -> "$CDN_BASE/Dumbbell_Shoulder_Press/0.jpg"
            "shoulder press" in lower || "overhead press" in lower || "military press" in lower -> "$CDN_BASE/Overhead_Press/0.jpg"
            "face pull" in lower -> "$CDN_BASE/Face_Pull/0.jpg"
            "rear delt" in lower || "reverse pec deck" in lower -> "$CDN_BASE/Rear_Delt_Machine/0.jpg"
            "lat pulldown" in lower -> "$CDN_BASE/Wide-Grip_Lat_Pulldown/0.jpg"
            "pull up" in lower || "pull-up" in lower || "pullup" in lower -> "$CDN_BASE/Pullups/0.jpg"
            "chin up" in lower || "chin-up" in lower || "chinup" in lower -> "$CDN_BASE/Chin-Up/0.jpg"
            "cable row" in lower || "seated row" in lower -> "$CDN_BASE/Seated_Cable_Rows/0.jpg"
            "bent over row" in lower || "barbell row" in lower -> "$CDN_BASE/Bent_Over_Barbell_Row/0.jpg"
            "hammer curl" in lower -> "$CDN_BASE/Hammer_Curls/0.jpg"
            "bicep curl" in lower || "barbell curl" in lower -> "$CDN_BASE/Barbell_Curl/0.jpg"
            "incline curl" in lower -> "$CDN_BASE/Incline_Dumbbell_Curl/0.jpg"
            "curl" in lower && "arm" in lower -> "$CDN_BASE/Barbell_Curl/0.jpg"
            "dip" in lower -> "$CDN_BASE/Chest_Dip/0.jpg"
            "pushup" in lower || "push-up" in lower || "push up" in lower -> "$CDN_BASE/Pushups/0.jpg"
            "fly" in lower || "crossover" in lower -> "$CDN_BASE/Cable_Crossover/0.jpg"
            "hanging leg raise" in lower -> "$CDN_BASE/Hanging_Leg_Raise/0.jpg"
            "ab wheel" in lower || "ab roller" in lower -> "$CDN_BASE/Ab_Roller/0.jpg"
            "plank" in lower -> "$CDN_BASE/Plank/0.jpg"
            else -> null
        }
    }
}
