package com.example.data

import com.example.model.Exercise
import com.example.model.WorkoutDay

object PreloadedWorkoutPlan {

    fun getDefaultDays(): List<WorkoutDay> = listOf(
        WorkoutDay(
            id = "day_1",
            dayNumber = 1,
            title = "DAY 1: Upper Body (Upper Chest & Delts)",
            shortTitle = "Upper Chest & Delts",
            focusBadge = "Clavicular Pec Shelf & Thoracic Extension",
            anatomicalGoal = "Builds upper clavicular chest thickness and anterior/lateral deltoids to lift the chest line and reduce pseudogynecomastia appearance, paired with thoracic and hip flexor opening.",
            exercises = listOf(
                Exercise(
                    id = "d1_ex1",
                    name = "Hip Flexor Couch Stretch",
                    isWarmup = true,
                    sets = 2,
                    targetRepsOrHold = "60s/side",
                    isTimedHold = true,
                    holdDurationSeconds = 60,
                    isPerSideHold = true,
                    restSeconds = 30,
                    searchQuery = "couch stretch anterior pelvic tilt form",
                    formCues = "Squeeze the glute of your back leg and tuck your pelvis under (posterior tilt) BEFORE leaning forward. Avoid arching your lower back to fake a deeper stretch."
                ),
                Exercise(
                    id = "d1_ex2",
                    name = "Thoracic Wall Slides",
                    isWarmup = true,
                    sets = 2,
                    targetRepsOrHold = "10 reps",
                    isTimedHold = false,
                    holdDurationSeconds = 0,
                    isPerSideHold = false,
                    restSeconds = 30,
                    searchQuery = "thoracic wall slides posture tutorial",
                    formCues = "Keep lower back, upper back, elbows, and wrists pressed flat against the wall as you slide arms up. Avoid shrugging shoulders toward ears."
                ),
                Exercise(
                    id = "d1_ex3",
                    name = "Decline Push-Ups (Feet Elevated)",
                    isWarmup = false,
                    sets = 4,
                    targetRepsOrHold = "8–12 reps",
                    isTimedHold = false,
                    holdDurationSeconds = 0,
                    isPerSideHold = false,
                    restSeconds = 90,
                    searchQuery = "decline push up proper form upper chest",
                    formCues = "Elevate feet 12–18 inches (bed/chair), brace abs, tuck elbows at 45 degrees, lower upper chest toward floor, and push shoulder blades forward at the top. Avoid flaring elbows to 90 degrees or sagging hips."
                ),
                Exercise(
                    id = "d1_ex4",
                    name = "Pike Push-Ups",
                    isWarmup = false,
                    sets = 3,
                    targetRepsOrHold = "8–12 reps",
                    isTimedHold = false,
                    holdDurationSeconds = 0,
                    isPerSideHold = false,
                    restSeconds = 90,
                    searchQuery = "pike push up progression calisthenics form",
                    formCues = "Hinge at hips into an inverted V, shift head forward past your hands as you lower (forming a tripod), and press straight back up. Avoid dropping head straight down between hands."
                ),
                Exercise(
                    id = "d1_ex5",
                    name = "Wide-Grip Push-Ups (Slow 3s Eccentric)",
                    isWarmup = false,
                    sets = 3,
                    targetRepsOrHold = "10–15 reps",
                    isTimedHold = false,
                    holdDurationSeconds = 0,
                    isPerSideHold = false,
                    restSeconds = 75,
                    searchQuery = "wide grip push up chest activation form",
                    formCues = "Place hands ~1.5x shoulder width, rotate fingers slightly outward, take a full 3 seconds to lower until chest hovers 1 inch above floor, and squeeze pecs hard at top. Avoid bouncing at the bottom."
                ),
                Exercise(
                    id = "d1_ex6",
                    name = "Pseudo Planche Push-Ups",
                    isWarmup = false,
                    sets = 3,
                    targetRepsOrHold = "8–10 reps",
                    isTimedHold = false,
                    holdDurationSeconds = 0,
                    isPerSideHold = false,
                    restSeconds = 75,
                    searchQuery = "pseudo planche push up beginner form",
                    formCues = "Turn hands outward ~45 degrees, protract upper back slightly, and lean shoulders forward past wrists throughout the entire rep. Avoid letting shoulders drift backward as you push up."
                ),
                Exercise(
                    id = "d1_ex7",
                    name = "Prone Y-T-W Raises (Floor)",
                    isWarmup = false,
                    sets = 3,
                    targetRepsOrHold = "12 reps",
                    isTimedHold = false,
                    holdDurationSeconds = 0,
                    isPerSideHold = false,
                    restSeconds = 60,
                    searchQuery = "prone ytw raises floor posture form",
                    formCues = "Lie face down with forehead resting on a folded towel, thumbs pointing up, and lift arms using mid/upper back—not momentum. Avoid lifting chest off floor or shrugging traps."
                )
            )
        ),
        WorkoutDay(
            id = "day_2",
            dayNumber = 2,
            title = "DAY 2: Lower Body & Core (Glutes, Hamstrings & APT Fix)",
            shortTitle = "Glutes, Hamstrings & APT Fix",
            focusBadge = "Posterior Pelvic Tilt & Deep Abdominals",
            anatomicalGoal = "Directly corrects Anterior Pelvic Tilt by lengthening tight hip flexors while strengthening the glute max, hamstrings, and deep transverse abdominis to hold a neutral pelvis.",
            exercises = listOf(
                Exercise(
                    id = "d2_ex1",
                    name = "Kneeling Hip Flexor Stretch",
                    isWarmup = true,
                    sets = 2,
                    targetRepsOrHold = "60s/side",
                    isTimedHold = true,
                    holdDurationSeconds = 60,
                    isPerSideHold = true,
                    restSeconds = 30,
                    searchQuery = "half kneeling hip flexor stretch posterior pelvic tilt",
                    formCues = "Keep torso upright, tuck tailbone under, and squeeze back glute until you feel a stretch at the front of the hip. Avoid leaning far forward with an arched lower back."
                ),
                Exercise(
                    id = "d2_ex2",
                    name = "Glute Bridges (2s Peak Squeeze)",
                    isWarmup = false,
                    sets = 4,
                    targetRepsOrHold = "15 reps",
                    isTimedHold = false,
                    holdDurationSeconds = 0,
                    isPerSideHold = false,
                    restSeconds = 60,
                    searchQuery = "glute bridge posterior pelvic tilt proper form",
                    formCues = "Flatten lower back into floor FIRST (posterior tilt), drive through heels, and hold a hard 2-second glute squeeze at top without hyperextending spine. Avoid pushing hips too high by arching lower back."
                ),
                Exercise(
                    id = "d2_ex3",
                    name = "Single-Leg Glute Bridges",
                    isWarmup = false,
                    sets = 3,
                    targetRepsOrHold = "10–12 reps/leg",
                    isTimedHold = false,
                    holdDurationSeconds = 0,
                    isPerSideHold = false,
                    restSeconds = 60,
                    searchQuery = "single leg glute bridge proper form",
                    formCues = "Pull non-working knee slightly toward chest to lock pelvis in a neutral/posterior tilt, and drive through planted heel. Avoid letting the unsupported hip drop to one side."
                ),
                Exercise(
                    id = "d2_ex4",
                    name = "Dead Bugs",
                    isWarmup = false,
                    sets = 3,
                    targetRepsOrHold = "10 reps/side",
                    isTimedHold = false,
                    holdDurationSeconds = 0,
                    isPerSideHold = false,
                    restSeconds = 60,
                    searchQuery = "dead bug exercise lower back flat form",
                    formCues = "Press lower back firmly into floor so zero space exists under your spine. Exhale fully as you extend opposite arm and leg slowly. Avoid moving fast or letting lower back arch off the floor."
                ),
                Exercise(
                    id = "d2_ex5",
                    name = "Hollow Body Hold",
                    isWarmup = false,
                    sets = 3,
                    targetRepsOrHold = "30s hold",
                    isTimedHold = true,
                    holdDurationSeconds = 30,
                    isPerSideHold = false,
                    restSeconds = 60,
                    searchQuery = "hollow body hold gymnastics progression",
                    formCues = "Curl shoulders and shoulder blades off floor, glue lower back to ground, and point toes. Bend knees slightly if lower back lifts. Avoid holding breath or letting lower back peel off floor."
                ),
                Exercise(
                    id = "d2_ex6",
                    name = "Bodyweight Bulgarian Split Squats",
                    isWarmup = false,
                    sets = 3,
                    targetRepsOrHold = "10 reps/leg",
                    isTimedHold = false,
                    holdDurationSeconds = 0,
                    isPerSideHold = false,
                    restSeconds = 75,
                    searchQuery = "bodyweight bulgarian split squat upright torso",
                    formCues = "Keep torso upright and pelvis tucked slightly under so the back hip gets an active stretch while front glute and quad work. Avoid leaning heavily forward or letting front knee cave inward."
                )
            )
        ),
        WorkoutDay(
            id = "day_3",
            dayNumber = 3,
            title = "DAY 3: Upper Body (Chest Volume, Rear Delts & Posture)",
            shortTitle = "Chest Volume, Rear Delts & Posture",
            focusBadge = "Inner/Upper Chest & Scapular Retraction",
            anatomicalGoal = "High-hypertrophy upper and inner chest volume paired with pec minor opening, serratus anterior activation, and RKC posterior-tilt core tension.",
            exercises = listOf(
                Exercise(
                    id = "d3_ex1",
                    name = "Doorway Chest Opener",
                    isWarmup = true,
                    sets = 2,
                    targetRepsOrHold = "45s/side",
                    isTimedHold = true,
                    holdDurationSeconds = 45,
                    isPerSideHold = true,
                    restSeconds = 30,
                    searchQuery = "doorway pec minor stretch proper form",
                    formCues = "Place forearm on doorframe at 90 degrees (and slightly higher for pec minor), step through gently, and keep ribs down. Avoid pushing so hard that shoulder joint pinches forward."
                ),
                Exercise(
                    id = "d3_ex2",
                    name = "Scapular Push-Ups",
                    isWarmup = true,
                    sets = 2,
                    targetRepsOrHold = "12 reps",
                    isTimedHold = false,
                    holdDurationSeconds = 0,
                    isPerSideHold = false,
                    restSeconds = 30,
                    searchQuery = "scapular push ups serratus anterior form",
                    formCues = "Keep elbows locked straight; sink chest down by pinching shoulder blades together, then push floor away to spread them apart. Avoid bending elbows or sagging hips."
                ),
                Exercise(
                    id = "d3_ex3",
                    name = "Decline Push-Ups (Feet Elevated)",
                    isWarmup = false,
                    sets = 4,
                    targetRepsOrHold = "10–15 reps",
                    isTimedHold = false,
                    holdDurationSeconds = 0,
                    isPerSideHold = false,
                    restSeconds = 90,
                    searchQuery = "decline push up proper form upper chest",
                    formCues = "Feet elevated 12–18 inches, elbows tucked at 45 degrees, control the descent, and protract shoulder blades at lockout. Avoid flaring elbows or letting lower back sag."
                ),
                Exercise(
                    id = "d3_ex4",
                    name = "Diamond / Close-Grip Push-Ups",
                    isWarmup = false,
                    sets = 3,
                    targetRepsOrHold = "8–12 reps",
                    isTimedHold = false,
                    holdDurationSeconds = 0,
                    isPerSideHold = false,
                    restSeconds = 75,
                    searchQuery = "diamond push up proper form elbows",
                    formCues = "Place hands close together under upper/mid chest, keep elbows tucked close to sides, and squeeze inner/upper chest at top. Avoid flaring elbows outward."
                ),
                Exercise(
                    id = "d3_ex5",
                    name = "Elevated Pike Push-Ups",
                    isWarmup = false,
                    sets = 3,
                    targetRepsOrHold = "6–10 reps",
                    isTimedHold = false,
                    holdDurationSeconds = 0,
                    isPerSideHold = false,
                    restSeconds = 90,
                    searchQuery = "feet elevated pike push up form",
                    formCues = "Place feet on a low bed or chair, stack hips directly over shoulders, and lower crown of head forward toward floor. Avoid letting hips drift backward away from shoulders."
                ),
                Exercise(
                    id = "d3_ex6",
                    name = "Reverse Snow Angels (Floor)",
                    isWarmup = false,
                    sets = 3,
                    targetRepsOrHold = "12–15 reps",
                    isTimedHold = false,
                    holdDurationSeconds = 0,
                    isPerSideHold = false,
                    restSeconds = 60,
                    searchQuery = "prone reverse snow angels rear delts form",
                    formCues = "Lie face down with glutes squeezed, lift arms 2–3 inches off floor, and sweep slowly from overhead down to hips with palms rotating down. Avoid letting hands touch floor mid-rep."
                ),
                Exercise(
                    id = "d3_ex7",
                    name = "RKC Plank (Posterior Tilt Plank)",
                    isWarmup = false,
                    sets = 3,
                    targetRepsOrHold = "30s hold",
                    isTimedHold = true,
                    holdDurationSeconds = 30,
                    isPerSideHold = false,
                    restSeconds = 60,
                    searchQuery = "rkc plank posterior pelvic tilt tutorial",
                    formCues = "Tuck pelvis under hard, squeeze glutes and quads at 100% tension, and pull elbows toward toes isometrically. Avoid relaxing into a passive lower-back sag."
                )
            )
        ),
        WorkoutDay(
            id = "day_4",
            dayNumber = 4,
            title = "DAY 4: Lower Body & Core (Posterior Chain & Deep Core)",
            shortTitle = "Posterior Chain & Deep Core",
            focusBadge = "Hamstring Pull & Lateral Pelvic Stability",
            anatomicalGoal = "Reinforces posterior pelvic tilt under load via unilateral hip extension, eccentric hamstring curls, and anti-extension/anti-rotation deep core bracing.",
            exercises = listOf(
                Exercise(
                    id = "d4_ex1",
                    name = "Half-Kneeling Hip Flexor Stretch",
                    isWarmup = true,
                    sets = 2,
                    targetRepsOrHold = "60s/side",
                    isTimedHold = true,
                    holdDurationSeconds = 60,
                    isPerSideHold = true,
                    restSeconds = 30,
                    searchQuery = "half kneeling hip flexor stretch posterior pelvic tilt",
                    formCues = "Torso upright, tailbone tucked under, squeeze back glute firmly. Avoid arching lower back."
                ),
                Exercise(
                    id = "d4_ex2",
                    name = "Glute Bridge March",
                    isWarmup = true,
                    sets = 2,
                    targetRepsOrHold = "12 reps",
                    isTimedHold = false,
                    holdDurationSeconds = 0,
                    isPerSideHold = false,
                    restSeconds = 30,
                    searchQuery = "glute bridge march stability exercise",
                    formCues = "Hold top of glute bridge with pelvis tucked, slowly lift one knee toward chest without letting hips drop or twist. Avoid rushing marches."
                ),
                Exercise(
                    id = "d4_ex3",
                    name = "Single-Leg Hip Thrusts",
                    isWarmup = false,
                    sets = 4,
                    targetRepsOrHold = "10–12 reps/leg",
                    isTimedHold = false,
                    holdDurationSeconds = 0,
                    isPerSideHold = false,
                    restSeconds = 75,
                    searchQuery = "single leg hip thrust couch bench form",
                    formCues = "Rest upper back (below shoulder blades) on couch/bed, keep chin tucked forward, drive through heel until shin is vertical at top. Avoid throwing head back or arching ribs upward."
                ),
                Exercise(
                    id = "d4_ex4",
                    name = "Sliding Hamstring Curls",
                    isWarmup = false,
                    sets = 3,
                    targetRepsOrHold = "8–12 reps",
                    isTimedHold = false,
                    holdDurationSeconds = 0,
                    isPerSideHold = false,
                    restSeconds = 75,
                    searchQuery = "sliding floor hamstring curls towel socks",
                    formCues = "Wear socks on smooth floor (or towel under heels), lift hips into bridge, slide heels out slowly over 3 seconds, and pull back in keeping hips high. Avoid letting hips drop to floor during slide."
                ),
                Exercise(
                    id = "d4_ex5",
                    name = "Reverse Plank",
                    isWarmup = false,
                    sets = 3,
                    targetRepsOrHold = "35s hold",
                    isTimedHold = true,
                    holdDurationSeconds = 35,
                    isPerSideHold = false,
                    restSeconds = 60,
                    searchQuery = "reverse plank proper form posture",
                    formCues = "Hands behind hips (fingers pointing toward feet or slightly out), push chest up, squeeze glutes, lift hips into straight line from heels to shoulders. Avoid dropping head back or sagging hips."
                ),
                Exercise(
                    id = "d4_ex6",
                    name = "Bird-Dogs",
                    isWarmup = false,
                    sets = 3,
                    targetRepsOrHold = "10 reps/side",
                    isTimedHold = false,
                    holdDurationSeconds = 0,
                    isPerSideHold = false,
                    restSeconds = 45,
                    searchQuery = "bird dog exercise squat university form",
                    formCues = "Keep neutral spine (imagine balancing a cup of water on lower back), kick heel straight back—not up—and hold 2 seconds per rep. Avoid kicking leg high enough to arch lower back."
                ),
                Exercise(
                    id = "d4_ex7",
                    name = "Side Planks",
                    isWarmup = false,
                    sets = 3,
                    targetRepsOrHold = "30s/side",
                    isTimedHold = true,
                    holdDurationSeconds = 30,
                    isPerSideHold = true,
                    restSeconds = 45,
                    searchQuery = "side plank proper form oblique glute medius",
                    formCues = "Stack elbow directly under shoulder, push hips forward so body forms a straight line from head to feet, and brace abs. Avoid letting top hip roll forward or sag."
                )
            )
        )
    )

    val groundedMotivationalCues = listOf(
        "Clavicular Chest Architecture" to "Building the upper pec shelf and anterior delts creates an upward structural frame across the collarbone, visibly tightening the lower chest contour.",
        "Pelvic Neutrality Is Built Daily" to "Anterior Pelvic Tilt isn't fixed by stretching alone—active glute lockouts and ribs-down deep core tension train your nervous system to hold neutral alignment.",
        "Slow Eccentrics Over Ego Reps" to "Bodyweight hypertrophy thrives on mechanical tension. Own every 3-second lowering phase and protract hard at lockout.",
        "Ribcage-to-Pelvis Stacking" to "Exhale fully on Dead Bugs, Hollow Holds, and RKC Planks to bring your lower ribs down. When ribs and pelvis stack, your lower back decompresses and your waistline tightens."
    )
}
