package com.bharath.homeforge.domain

data class ExerciseGuide(
    val steps: List<String>,
    val cues: List<String>,
    val mistakes: List<String>,
)

object ExerciseGuides {

    fun forExercise(name: String): ExerciseGuide? = guides[name]

    private fun guide(steps: List<String>, cues: List<String>, mistakes: List<String>) =
        ExerciseGuide(steps, cues, mistakes)

    private val guides: Map<String, ExerciseGuide> = mapOf(
        "Goblet squat" to guide(
            steps = listOf(
                "Hold one dumbbell upright against your chest with both hands under the top plate.",
                "Stand with your feet a little wider than your shoulders, toes turned slightly out.",
                "Breathe in, push your hips back and bend your knees to lower into a deep squat.",
                "Keep your elbows inside your knees at the bottom, then drive through your whole foot to stand.",
            ),
            cues = listOf("Chest tall, elbows pointing down.", "Knees follow the line of your toes.", "Go as deep as you can with a flat back."),
            mistakes = listOf("Knees caving inward.", "Heels lifting off the floor.", "Rounding your back at the bottom."),
        ),
        "Barbell back squat" to guide(
            steps = listOf(
                "Rest the bar across your upper back, below the neck, hands gripping wider than your shoulders.",
                "Take a step back and set your feet about shoulder width apart, toes slightly out.",
                "Breathe in and brace your core, then sit back and down until your thighs are at least parallel to the floor.",
                "Drive up through your whole foot, keeping your chest up, and breathe out near the top.",
            ),
            cues = listOf("Brace like you are about to be punched.", "Push your knees out over your toes.", "Keep the bar over the middle of your foot."),
            mistakes = listOf("Letting your chest fall forward.", "Knees collapsing inward.", "Lifting your hips before your shoulders on the way up."),
        ),
        "Barbell front squat" to guide(
            steps = listOf(
                "Rest the bar on the front of your shoulders, with elbows high and fingers lightly under the bar.",
                "Stand with your feet shoulder width apart and brace your core.",
                "Sit straight down between your hips, keeping your elbows up.",
                "Stand up by driving through your feet, keeping the bar close to your neck.",
            ),
            cues = listOf("Elbows high the whole time.", "Torso upright.", "Use a lighter weight than your back squat."),
            mistakes = listOf("Elbows dropping, so the bar rolls forward.", "Leaning forward.", "Bar sitting on your wrists instead of your shoulders."),
        ),
        "Dumbbell squat" to guide(
            steps = listOf(
                "Hold a dumbbell in each hand at your sides, palms facing in.",
                "Stand with your feet shoulder width apart.",
                "Push your hips back and bend your knees to lower until your thighs are about parallel to the floor.",
                "Press through your feet to stand, keeping your arms relaxed.",
            ),
            cues = listOf("Chest up and eyes forward.", "Weight stays over mid-foot.", "Control the way down."),
            mistakes = listOf("Rounding your lower back.", "Knees caving in.", "Bouncing out of the bottom."),
        ),
        "Romanian deadlift" to guide(
            steps = listOf(
                "Hold the bar in front of your thighs with an overhand grip, feet hip width apart, knees slightly bent.",
                "Push your hips straight back, letting the bar slide down your legs.",
                "Lower until you feel a strong stretch in your hamstrings, usually just below the knee.",
                "Squeeze your glutes and drive your hips forward to stand tall.",
            ),
            cues = listOf("Back flat the whole time.", "Bar stays close to your legs.", "Knees stay softly bent, not squatting."),
            mistakes = listOf("Rounding your back.", "Bending your knees too much so it becomes a squat.", "Letting the bar drift away from your body."),
        ),
        "Dumbbell Romanian deadlift" to guide(
            steps = listOf(
                "Hold a dumbbell in each hand in front of your thighs, feet hip width apart, knees slightly bent.",
                "Push your hips back and slide the dumbbells down the front of your legs.",
                "Stop when you feel a stretch in your hamstrings, with your back still flat.",
                "Drive your hips forward to return to standing.",
            ),
            cues = listOf("Hinge at the hips, not the waist.", "Shoulders back, neck neutral.", "Dumbbells stay close to your legs."),
            mistakes = listOf("Rounding your back.", "Dropping the weight too far forward.", "Overextending at the top."),
        ),
        "Barbell deadlift" to guide(
            steps = listOf(
                "Stand with the bar over the middle of your feet, feet hip width apart.",
                "Bend at the hips and knees and grip the bar just outside your legs.",
                "Flatten your back, brace your core and pull the slack out of the bar.",
                "Push the floor away and stand up with the bar close to your body, then lock out your hips.",
                "Lower the bar the same way, hips back first, and set it down under control.",
            ),
            cues = listOf("Back flat, chest up.", "Bar stays in contact with your legs.", "Push the floor away instead of yanking."),
            mistakes = listOf("Rounding your lower back.", "Starting with the bar too far from your shins.", "Jerking the bar off the floor."),
        ),
        "Barbell glute bridge" to guide(
            steps = listOf(
                "Sit on the floor with your upper back against a low, stable support such as a sofa, and roll the bar over your hips.",
                "Plant your feet flat, hip width apart, knees bent.",
                "Drive through your heels and lift your hips until your body forms a straight line from shoulders to knees.",
                "Squeeze your glutes at the top, then lower with control.",
            ),
            cues = listOf("Chin tucked, ribs down.", "Push through your heels.", "Pause and squeeze at the top."),
            mistakes = listOf("Arching your lower back at the top.", "Pushing through your toes.", "Using a bar that rolls or is not padded."),
        ),
        "Dumbbell reverse lunge" to guide(
            steps = listOf(
                "Stand tall holding a dumbbell in each hand at your sides.",
                "Step one foot back and lower until both knees are bent at about 90 degrees.",
                "Keep your front shin roughly vertical and your torso upright.",
                "Push through your front foot to return to standing, then repeat on the other side.",
            ),
            cues = listOf("Long step back.", "Weight on your front heel.", "Chest tall."),
            mistakes = listOf("Front knee collapsing inward.", "Leaning forward.", "Taking too short a step."),
        ),
        "Split squat" to guide(
            steps = listOf(
                "Hold a dumbbell in each hand. Stand in a staggered stance with one foot forward and the back foot resting on a bench or sturdy chair behind you.",
                "Lower your back knee toward the floor, keeping your torso upright.",
                "Stop when your front thigh is about parallel to the floor.",
                "Drive through your front foot to stand. Finish all reps, then switch legs.",
            ),
            cues = listOf("Front foot far enough forward.", "Most of your weight on the front leg.", "Stay upright."),
            mistakes = listOf("Front foot too close to the support.", "Leaning your torso far forward.", "Pushing off the back foot."),
        ),
        "Goblet reverse lunge" to guide(
            steps = listOf(
                "Hold one dumbbell upright against your chest with both hands.",
                "Step one foot back and lower until both knees are bent at about 90 degrees.",
                "Keep your chest tall and your front heel planted.",
                "Push through your front foot to stand, then alternate legs.",
            ),
            cues = listOf("Elbows tucked in.", "Slow and controlled.", "Back knee hovers just above the floor."),
            mistakes = listOf("Letting your torso tip forward.", "Front knee drifting inward.", "Rushing the reps."),
        ),
        "Dumbbell floor press" to guide(
            steps = listOf(
                "Lie on the floor with your knees bent and a dumbbell in each hand over your chest.",
                "Lower the dumbbells until your upper arms touch the floor, elbows at about 45 degrees from your body.",
                "Pause briefly, then press the dumbbells straight up until your arms are straight.",
            ),
            cues = listOf("Wrists stacked over elbows.", "Shoulder blades pulled back and down.", "Control the descent."),
            mistakes = listOf("Flaring your elbows straight out to the sides.", "Bouncing your arms off the floor.", "Letting the dumbbells drift apart."),
        ),
        "Barbell floor press" to guide(
            steps = listOf(
                "Lie on the floor with your knees bent. Hold the bar over your chest with your hands a little wider than your shoulders.",
                "Lower the bar until your upper arms touch the floor.",
                "Pause, then press the bar straight up until your arms are straight.",
            ),
            cues = listOf("Elbows about 45 degrees from your body.", "Squeeze the bar.", "Keep your feet planted."),
            mistakes = listOf("Letting the bar drift toward your face.", "Bouncing off the floor.", "Uneven pressing with one arm."),
        ),
        "Push-up" to guide(
            steps = listOf(
                "Place your hands on the floor slightly wider than your shoulders, body in a straight line from head to heels.",
                "Lower your chest toward the floor, elbows at about 45 degrees from your body.",
                "Stop just above the floor, then press back up until your arms are straight.",
            ),
            cues = listOf("Tighten your glutes and core.", "Look at a spot just ahead of your hands.", "Make it easier by lifting your hips less and kneeling."),
            mistakes = listOf("Sagging hips.", "Elbows flaring straight out.", "Only moving halfway down."),
        ),
        "Close-grip push-up" to guide(
            steps = listOf(
                "Start in a push-up position with your hands directly under your shoulders or slightly closer.",
                "Lower your chest while keeping your elbows tucked close to your sides.",
                "Press back up until your arms are straight.",
            ),
            cues = listOf("Elbows brush your ribs.", "Body stays in one straight line.", "Slow on the way down."),
            mistakes = listOf("Hands too close, which strains your wrists.", "Sagging hips.", "Elbows flaring out."),
        ),
        "Barbell overhead press" to guide(
            steps = listOf(
                "Hold the bar at shoulder height with hands just wider than your shoulders, elbows slightly in front of the bar.",
                "Stand with your feet hip width apart, glutes and core tight.",
                "Press the bar straight up, moving your head slightly back, then forward again once the bar passes your forehead.",
                "Finish with the bar over the middle of your feet and your arms straight, then lower to your shoulders.",
            ),
            cues = listOf("Squeeze your glutes to protect your back.", "Bar travels in a straight line.", "Ribs down, do not lean back."),
            mistakes = listOf("Leaning back and arching your lower back.", "Pressing the bar out in front of you.", "Flaring your ribs."),
        ),
        "Dumbbell shoulder press" to guide(
            steps = listOf(
                "Hold a dumbbell in each hand at shoulder height, palms facing forward, elbows slightly in front of you.",
                "Brace your core and press both dumbbells up until your arms are straight above your shoulders.",
                "Lower them back to shoulder height under control.",
            ),
            cues = listOf("Wrists over elbows.", "Core tight.", "Dumbbells move slightly inward at the top."),
            mistakes = listOf("Arching your back.", "Letting the dumbbells drift forward.", "Shrugging your shoulders to your ears."),
        ),
        "Pike push-up" to guide(
            steps = listOf(
                "Start in a push-up position, then walk your feet in and lift your hips high so your body forms an upside-down V.",
                "Bend your elbows to lower the top of your head toward the floor between your hands.",
                "Press back up until your arms are straight.",
            ),
            cues = listOf("Hips high.", "Elbows at about 45 degrees.", "Move slowly and stay in control."),
            mistakes = listOf("Dropping your hips and turning it into a normal push-up.", "Flaring your elbows.", "Hitting your head on the floor."),
        ),
        "Barbell bent-over row" to guide(
            steps = listOf(
                "Hold the bar with an overhand grip, hands just wider than your shoulders.",
                "Hinge forward at the hips until your torso is roughly 45 degrees to the floor, back flat, knees slightly bent.",
                "Pull the bar toward your lower ribs, leading with your elbows.",
                "Squeeze your shoulder blades together, then lower the bar slowly.",
            ),
            cues = listOf("Keep your back flat.", "Pull your elbows back, not up.", "Do not stand up as you pull."),
            mistakes = listOf("Rounding your back.", "Using momentum from your hips.", "Pulling the bar to your chest."),
        ),
        "One-arm dumbbell row" to guide(
            steps = listOf(
                "Place one hand and the same-side knee on a bench or sturdy chair, back flat, holding a dumbbell in your free hand.",
                "Let the dumbbell hang with your arm straight.",
                "Pull the dumbbell toward your hip, keeping your elbow close to your body.",
                "Lower slowly and finish all reps before switching sides.",
            ),
            cues = listOf("Pull with your elbow, not your hand.", "Keep your shoulders level.", "Back stays flat."),
            mistakes = listOf("Twisting your torso to lift the weight.", "Shrugging your shoulder.", "Rushing the lowering phase."),
        ),
        "Dumbbell bent-over row" to guide(
            steps = listOf(
                "Hold a dumbbell in each hand, hinge forward at the hips until your torso is about 45 degrees to the floor, back flat.",
                "Let the dumbbells hang below your shoulders.",
                "Row both dumbbells toward your hips, elbows close to your body.",
                "Squeeze your shoulder blades together, then lower under control.",
            ),
            cues = listOf("Neck in line with your spine.", "Elbows drive back.", "Hold the hinge position still."),
            mistakes = listOf("Rounding your back.", "Standing up as you pull.", "Letting the dumbbells swing."),
        ),
        "Barbell upright row" to guide(
            steps = listOf(
                "Hold the bar in front of your thighs with an overhand grip, hands shoulder width apart.",
                "Pull the bar straight up along your body, leading with your elbows, to about chest height.",
                "Lower slowly to the start.",
            ),
            cues = listOf("Elbows stay higher than your wrists.", "Bar stays close to your body.", "Stop at chest height."),
            mistakes = listOf("Pulling the bar above your chest, which can pinch your shoulders.", "Using a very narrow grip.", "Swinging your body to help."),
        ),
        "Dumbbell curl" to guide(
            steps = listOf(
                "Stand tall holding a dumbbell in each hand with your palms facing forward and elbows at your sides.",
                "Curl the dumbbells up toward your shoulders without moving your elbows.",
                "Squeeze your biceps at the top, then lower slowly until your arms are straight.",
            ),
            cues = listOf("Elbows pinned to your sides.", "Slow on the way down.", "Wrists straight."),
            mistakes = listOf("Swinging your body.", "Letting your elbows drift forward.", "Dropping the weight quickly."),
        ),
        "Barbell curl" to guide(
            steps = listOf(
                "Stand holding the bar with an underhand grip, hands shoulder width apart, arms straight.",
                "Curl the bar up toward your chest without moving your elbows.",
                "Squeeze at the top, then lower slowly to the start.",
            ),
            cues = listOf("Stand tall, glutes tight.", "Elbows stay at your sides.", "Control the lowering."),
            mistakes = listOf("Leaning back to lift the bar.", "Using momentum.", "Only lifting halfway."),
        ),
        "Overhead triceps extension" to guide(
            steps = listOf(
                "Hold one dumbbell with both hands under the top plate and press it overhead, arms straight.",
                "Keeping your upper arms still and close to your head, bend your elbows to lower the dumbbell behind your head.",
                "Straighten your arms to return to the top.",
            ),
            cues = listOf("Elbows point forward, not out.", "Upper arms stay still.", "Core braced, ribs down."),
            mistakes = listOf("Elbows flaring wide.", "Arching your back.", "Using a weight that is too heavy to control."),
        ),
        "Close-grip floor press" to guide(
            steps = listOf(
                "Lie on the floor with your knees bent. Hold the bar over your chest with your hands about shoulder width apart.",
                "Lower the bar slowly, keeping your elbows tucked close to your sides, until your upper arms touch the floor.",
                "Press the bar straight up until your arms are straight.",
            ),
            cues = listOf("Elbows tucked.", "Wrists straight and stacked.", "Smooth, controlled reps."),
            mistakes = listOf("Hands too close together, which strains your wrists.", "Flaring your elbows.", "Bouncing off the floor."),
        ),
        "Triceps kickback" to guide(
            steps = listOf(
                "Hold a dumbbell in each hand and hinge forward at the hips with a flat back, upper arms parallel to the floor and elbows bent.",
                "Straighten your elbows, pushing the dumbbells back until your arms are straight.",
                "Squeeze your triceps, then bend your elbows to return.",
            ),
            cues = listOf("Upper arms stay still.", "Squeeze at the end of the movement.", "Use a light weight."),
            mistakes = listOf("Swinging the weights.", "Dropping your elbows.", "Rounding your back."),
        ),
        "Lateral raise" to guide(
            steps = listOf(
                "Stand holding a dumbbell in each hand at your sides, with a slight bend in your elbows.",
                "Raise your arms out to the sides until they reach shoulder height.",
                "Pause briefly, then lower slowly.",
            ),
            cues = listOf("Lead with your elbows, not your hands.", "Stop at shoulder height.", "Light weight and slow reps."),
            mistakes = listOf("Swinging the weights up.", "Shrugging your shoulders.", "Lifting above shoulder height."),
        ),
        "Rear delt fly" to guide(
            steps = listOf(
                "Hold a dumbbell in each hand and hinge forward at the hips with a flat back, arms hanging below your shoulders.",
                "With a slight bend in your elbows, raise your arms out to the sides until they are in line with your back.",
                "Squeeze your shoulder blades together, then lower slowly.",
            ),
            cues = listOf("Think about moving your elbows, not your hands.", "Neck stays neutral.", "Very light weights work best."),
            mistakes = listOf("Using momentum.", "Standing up to lift the weights.", "Shrugging your shoulders."),
        ),
        "Plank" to guide(
            steps = listOf(
                "Rest on your forearms and toes, elbows directly under your shoulders.",
                "Keep your body in a straight line from your head to your heels.",
                "Tighten your abs and glutes and hold for the target time, breathing steadily.",
            ),
            cues = listOf("Squeeze your glutes.", "Pull your belly button toward your spine.", "Look at the floor, neck neutral."),
            mistakes = listOf("Sagging hips.", "Hips piked high.", "Holding your breath."),
        ),
        "Russian twist" to guide(
            steps = listOf(
                "Sit on the floor with your knees bent, lean back slightly and keep your back straight. Hold a dumbbell in both hands in front of your chest.",
                "Lift your feet slightly off the floor, or keep them down to make it easier.",
                "Rotate your torso to one side, then the other, tapping the dumbbell near the floor on each side.",
            ),
            cues = listOf("Turn from your ribs, not just your arms.", "Chest tall.", "Move under control."),
            mistakes = listOf("Rounding your back.", "Swinging the weight with only your arms.", "Rushing the reps."),
        ),
        "Dead bug" to guide(
            steps = listOf(
                "Lie on your back with your arms pointing at the ceiling and your knees bent at 90 degrees above your hips.",
                "Press your lower back gently into the floor.",
                "Slowly lower one arm overhead and the opposite leg toward the floor, stopping before your back arches.",
                "Return to the start and switch sides.",
            ),
            cues = listOf("Lower back stays on the floor.", "Slow and controlled.", "Breathe out as you extend."),
            mistakes = listOf("Letting your lower back arch.", "Moving too fast.", "Holding your breath."),
        ),
        "Side plank" to guide(
            steps = listOf(
                "Lie on your side with your forearm on the floor, elbow under your shoulder and legs stacked.",
                "Lift your hips so your body forms a straight line from head to feet.",
                "Hold for the target time, then switch sides.",
            ),
            cues = listOf("Push the floor away with your forearm.", "Hips high.", "Breathe steadily."),
            mistakes = listOf("Hips sagging toward the floor.", "Rolling forward or backward.", "Shoulder not over your elbow."),
        ),
        "Leg raise" to guide(
            steps = listOf(
                "Lie on your back with your legs straight and your hands under your hips for support.",
                "Press your lower back into the floor and raise your legs until they point at the ceiling.",
                "Lower them slowly, stopping just above the floor before your back arches.",
            ),
            cues = listOf("Lower back stays down.", "Slow lowering.", "Bend your knees slightly to make it easier."),
            mistakes = listOf("Arching your lower back.", "Swinging your legs with momentum.", "Letting your feet touch the floor between reps."),
        ),
        "Bicycle crunch" to guide(
            steps = listOf(
                "Lie on your back with your hands lightly behind your head and your knees lifted.",
                "Bring one knee toward your chest while rotating your opposite elbow toward it.",
                "Extend the other leg straight, then switch sides in a pedaling motion.",
            ),
            cues = listOf("Rotate through your torso.", "Do not pull on your neck.", "Slow reps are harder than fast ones."),
            mistakes = listOf("Pulling on your head.", "Moving too fast.", "Not rotating your shoulders."),
        ),
        "Weighted crunch" to guide(
            steps = listOf(
                "Lie on your back with your knees bent and feet flat, holding a dumbbell against your chest.",
                "Curl your shoulders off the floor by contracting your abs.",
                "Pause at the top, then lower slowly.",
            ),
            cues = listOf("Lift with your abs, not your neck.", "Chin slightly tucked.", "Exhale as you crunch."),
            mistakes = listOf("Yanking your neck forward.", "Using too much weight.", "Lowering too fast."),
        ),
        "Barbell rollout" to guide(
            steps = listOf(
                "Kneel on a mat with the loaded bar on the floor in front of you and grip it with both hands.",
                "Brace your abs and slowly roll the bar forward, extending your body as far as you can control.",
                "Pull the bar back toward your knees using your abs.",
            ),
            cues = listOf("Keep your lower back from sagging.", "Only go as far as you can control.", "Move slowly."),
            mistakes = listOf("Arching your lower back.", "Rolling out too far too soon.", "Using your arms instead of your abs to come back."),
        ),
        "Mountain climber" to guide(
            steps = listOf(
                "Start in a push-up position with your hands under your shoulders and your body in a straight line.",
                "Drive one knee toward your chest, then quickly switch legs.",
                "Keep alternating at a steady pace for the target time.",
            ),
            cues = listOf("Hips level.", "Core tight.", "Breathe steadily."),
            mistakes = listOf("Bouncing your hips up.", "Letting your lower back sag.", "Going so fast your form breaks down."),
        ),
        "Dumbbell calf raise" to guide(
            steps = listOf(
                "Stand holding a dumbbell in each hand at your sides, with the balls of your feet on a step or a plate if you like.",
                "Rise up onto your toes as high as you can.",
                "Pause at the top, then lower your heels slowly below the step if possible.",
            ),
            cues = listOf("Full range, up and down.", "Pause at the top.", "Hold something for balance if needed."),
            mistakes = listOf("Bouncing quickly.", "Not going all the way up.", "Bending your knees."),
        ),
        "Bodyweight calf raise" to guide(
            steps = listOf(
                "Stand tall with your feet hip width apart, holding a wall or chair for balance if needed.",
                "Rise onto your toes as high as you can.",
                "Pause, then lower your heels slowly back to the floor.",
            ),
            cues = listOf("Squeeze at the top.", "Slow lowering.", "Make it harder by using one leg at a time."),
            mistakes = listOf("Bouncing.", "Rolling your ankles outward.", "Rushing the reps."),
        ),
        "Bodyweight squat" to guide(
            steps = listOf(
                "Stand with your feet a little wider than your shoulders, toes turned slightly out, arms out in front of you.",
                "Push your hips back and bend your knees to lower until your thighs are about parallel to the floor.",
                "Keep your chest up and your heels on the floor.",
                "Press through your whole foot to stand back up.",
            ),
            cues = listOf("Knees follow your toes.", "Weight stays over mid-foot.", "Go only as deep as you can with a flat back."),
            mistakes = listOf("Knees caving inward.", "Heels lifting.", "Rounding your lower back."),
        ),
        "Wall sit" to guide(
            steps = listOf(
                "Stand with your back against a wall and walk your feet forward about two feet.",
                "Slide down until your thighs are parallel to the floor and your knees are over your ankles.",
                "Hold the position for the target time, breathing steadily.",
            ),
            cues = listOf("Back flat against the wall.", "Weight in your heels.", "Rest your hands on your thighs, not on your knees."),
            mistakes = listOf("Sliding too low and straining your knees.", "Holding your breath.", "Letting your feet creep in."),
        ),
        "Jump squat" to guide(
            steps = listOf(
                "Stand with your feet shoulder width apart and lower into a squat.",
                "Drive through your feet and jump up as high as you can.",
                "Land softly on the middle of your feet, bending your knees to absorb the impact, and go straight into the next rep.",
            ),
            cues = listOf("Land quietly.", "Swing your arms to help you jump.", "Reset your balance if you tire."),
            mistakes = listOf("Landing with straight, stiff legs.", "Letting your knees cave in on landing.", "Jumping when your form has broken down."),
        ),
        "Assisted pistol squat" to guide(
            steps = listOf(
                "Stand on one leg next to a sturdy chair or doorframe and hold it lightly for balance, with the other leg out in front.",
                "Bend your standing knee and lower as far as you can control, keeping your heel flat.",
                "Press through your standing foot to rise, using your hand only as much as you need.",
                "Finish all reps, then switch legs.",
            ),
            cues = listOf("Use less help from your hand over time.", "Keep your standing knee in line with your toes.", "Slow and controlled."),
            mistakes = listOf("Dropping quickly into the bottom.", "Your heel lifting.", "Pulling hard with your arm."),
        ),
        "Glute bridge" to guide(
            steps = listOf(
                "Lie on your back with your knees bent and feet flat on the floor, hip width apart.",
                "Press through your heels and lift your hips until your body forms a straight line from shoulders to knees.",
                "Squeeze your glutes at the top, then lower with control.",
            ),
            cues = listOf("Ribs down, do not arch your back.", "Push through your heels.", "Pause at the top."),
            mistakes = listOf("Arching your lower back.", "Pushing through your toes.", "Rushing the reps."),
        ),
        "Good morning" to guide(
            steps = listOf(
                "Stand with your feet shoulder width apart, knees slightly bent, hands lightly behind your head.",
                "Push your hips back and hinge forward with a flat back until you feel a stretch in your hamstrings.",
                "Squeeze your glutes and drive your hips forward to stand tall.",
            ),
            cues = listOf("Hinge at the hips.", "Back stays flat.", "Move slowly."),
            mistakes = listOf("Rounding your back.", "Bending your knees too much so it turns into a squat.", "Pulling on your neck."),
        ),
        "Single-leg glute bridge" to guide(
            steps = listOf(
                "Lie on your back with one foot flat on the floor and the other leg straight up or extended.",
                "Press through your planted heel and lift your hips until your body is in a straight line.",
                "Keep your hips level, then lower with control. Finish all reps and switch legs.",
            ),
            cues = listOf("Keep your hips level.", "Squeeze your glute at the top.", "Slow lowering."),
            mistakes = listOf("Letting one hip drop.", "Arching your lower back.", "Pushing off the other foot."),
        ),
        "Single-leg Romanian deadlift" to guide(
            steps = listOf(
                "Stand on one leg with a soft bend in your knee and your arms hanging in front of you.",
                "Hinge forward at the hip, lifting your other leg behind you as your torso lowers toward parallel with the floor.",
                "Keep your back flat and your hips square to the floor.",
                "Squeeze your glute to return to standing, finish all reps, then switch legs.",
            ),
            cues = listOf("Think of a see-saw.", "Hips stay square.", "Hold a wall lightly if you wobble."),
            mistakes = listOf("Rounding your back.", "Opening your hips toward the ceiling.", "Locking your standing knee."),
        ),
        "Bodyweight reverse lunge" to guide(
            steps = listOf(
                "Stand tall with your hands on your hips.",
                "Step one foot back and lower until both knees are bent at about 90 degrees.",
                "Push through your front foot to return to standing, then switch legs.",
            ),
            cues = listOf("Long step back.", "Chest tall.", "Weight on your front heel."),
            mistakes = listOf("Front knee collapsing inward.", "Taking too short a step.", "Leaning forward."),
        ),
        "Step-up" to guide(
            steps = listOf(
                "Stand in front of a sturdy chair or step that is about knee height.",
                "Place one whole foot on it and press through that heel to stand up fully.",
                "Lower back down with control, then repeat. Finish all reps and switch legs.",
            ),
            cues = listOf("Drive with the front leg, not the back foot.", "Stand tall at the top.", "Slow lowering."),
            mistakes = listOf("Pushing off the floor with your back foot.", "Using an unstable chair.", "Letting your knee cave in."),
        ),
        "Rear-foot elevated split squat" to guide(
            steps = listOf(
                "Stand in front of a sturdy chair with your back to it, and rest the top of one foot on the seat behind you.",
                "Lower your back knee toward the floor, keeping your torso upright.",
                "Stop when your front thigh is about parallel to the floor, then press through your front foot to stand.",
                "Finish all reps, then switch legs.",
            ),
            cues = listOf("Front foot far enough forward.", "Most of your weight on the front leg.", "Stay upright."),
            mistakes = listOf("Front foot too close to the chair.", "Leaning far forward.", "Pushing off the back foot."),
        ),
        "Jumping lunge" to guide(
            steps = listOf(
                "Start in a lunge with one foot forward and both knees bent.",
                "Jump up and switch your legs in the air.",
                "Land softly in a lunge with the other foot forward and go straight into the next jump.",
            ),
            cues = listOf("Land softly.", "Chest tall.", "Stop if your landing gets sloppy."),
            mistakes = listOf("Landing hard on straight legs.", "Knees caving in.", "Leaning forward."),
        ),
        "Incline push-up" to guide(
            steps = listOf(
                "Place your hands on a sturdy table or counter, a little wider than your shoulders, and walk your feet back until your body is in a straight line.",
                "Bend your elbows to lower your chest toward the edge.",
                "Press back up until your arms are straight.",
            ),
            cues = listOf("Body in one straight line.", "Elbows at about 45 degrees.", "The lower the surface, the harder it gets."),
            mistakes = listOf("Sagging hips.", "Flaring your elbows.", "Using a surface that can slide."),
        ),
        "Knee push-up" to guide(
            steps = listOf(
                "Start on your hands and knees with your hands a little wider than your shoulders, body in a straight line from head to knees.",
                "Lower your chest toward the floor, elbows at about 45 degrees.",
                "Press back up until your arms are straight.",
            ),
            cues = listOf("Straight line from your head to your knees.", "Tighten your core.", "Move on to full push-ups when this feels easy."),
            mistakes = listOf("Sticking your hips up.", "Only moving halfway down.", "Flaring your elbows."),
        ),
        "Diamond push-up" to guide(
            steps = listOf(
                "Start in a push-up position with your hands together under your chest, thumbs and index fingers forming a diamond.",
                "Lower your chest toward your hands, keeping your elbows close to your body.",
                "Press back up until your arms are straight.",
            ),
            cues = listOf("Elbows brush your ribs.", "Body stays straight.", "Drop to your knees if you cannot keep good form."),
            mistakes = listOf("Elbows flaring out.", "Sagging hips.", "Hands too far forward, which strains your wrists."),
        ),
        "Decline push-up" to guide(
            steps = listOf(
                "Place your feet on a sturdy chair and your hands on the floor, a little wider than your shoulders.",
                "Keep your body in a straight line and lower your chest toward the floor.",
                "Press back up until your arms are straight.",
            ),
            cues = listOf("Tight core and glutes.", "Look slightly ahead of your hands.", "The higher your feet, the harder it is."),
            mistakes = listOf("Letting your hips sag or pike.", "Using a chair that can slide.", "Flaring your elbows."),
        ),
        "Archer push-up" to guide(
            steps = listOf(
                "Start in a wide push-up position with your hands well outside your shoulders.",
                "Shift your weight to one side, bending that elbow while the other arm stays straight.",
                "Press back to the centre and repeat on the other side.",
            ),
            cues = listOf("The straight arm is a support only.", "Move slowly.", "Keep your body in a straight line."),
            mistakes = listOf("Letting your hips twist.", "Bending both arms equally.", "Going too fast."),
        ),
        "Hands-elevated pike push-up" to guide(
            steps = listOf(
                "Place your hands on a sturdy chair seat and walk your feet back and in, lifting your hips high so your body forms an upside-down V.",
                "Bend your elbows to lower your head toward the chair.",
                "Press back up until your arms are straight.",
            ),
            cues = listOf("Hips high.", "Elbows at about 45 degrees.", "Stay in control."),
            mistakes = listOf("Dropping your hips.", "Flaring your elbows.", "Using a chair that can slip."),
        ),
        "Pike hold" to guide(
            steps = listOf(
                "Start in a push-up position, then walk your feet in and lift your hips high so your body forms an upside-down V.",
                "Press the floor away and hold, with your head between your arms.",
                "Breathe steadily for the target time.",
            ),
            cues = listOf("Push the floor away.", "Hips high.", "Relax your neck."),
            mistakes = listOf("Holding your breath.", "Letting your shoulders sag toward your ears.", "Bending your elbows."),
        ),
        "Feet-elevated pike push-up" to guide(
            steps = listOf(
                "Place your feet on a sturdy chair and your hands on the floor, with your hips lifted high.",
                "Bend your elbows to lower the top of your head toward the floor.",
                "Press back up until your arms are straight.",
            ),
            cues = listOf("The more upright your body, the more it trains your shoulders.", "Elbows at about 45 degrees.", "Move slowly."),
            mistakes = listOf("Flaring your elbows.", "Dropping your hips.", "Hitting your head on the floor."),
        ),
        "Superman" to guide(
            steps = listOf(
                "Lie face down with your arms stretched out in front of you and your legs straight.",
                "Lift your arms, chest and legs a few centimetres off the floor at the same time.",
                "Hold for a second, squeezing your back and glutes, then lower slowly.",
            ),
            cues = listOf("Look at the floor, neck neutral.", "Squeeze your glutes.", "Lift only as high as is comfortable."),
            mistakes = listOf("Craning your neck up.", "Swinging with momentum.", "Lifting too high and straining your lower back."),
        ),
        "Reverse snow angel" to guide(
            steps = listOf(
                "Lie face down with your arms by your sides, palms down, and lift your chest slightly.",
                "Sweep your arms in a wide arc overhead, keeping them off the floor.",
                "Sweep them back to your sides, squeezing your shoulder blades together.",
            ),
            cues = listOf("Slow and smooth.", "Squeeze your shoulder blades.", "Neck stays neutral."),
            mistakes = listOf("Letting your arms touch the floor.", "Rushing the movement.", "Lifting your head too high."),
        ),
        "Table inverted row" to guide(
            steps = listOf(
                "Lie under a very sturdy table and grip the edge with both hands, a little wider than your shoulders.",
                "Keep your body straight from head to heels, with your heels on the floor.",
                "Pull your chest up toward the table edge, squeezing your shoulder blades together.",
                "Lower slowly until your arms are straight.",
            ),
            cues = listOf("Body stays in one straight line.", "Pull your elbows back.", "Bend your knees to make it easier."),
            mistakes = listOf("Using a table that might tip or slide.", "Letting your hips sag.", "Only pulling halfway."),
        ),
        "Feet-elevated inverted row" to guide(
            steps = listOf(
                "Lie under a very sturdy table and grip the edge, with your heels resting on a chair so your body is level.",
                "Keep your body in a straight line and pull your chest up to the table edge.",
                "Lower slowly until your arms are straight.",
            ),
            cues = listOf("Tight core and glutes.", "Squeeze your shoulder blades.", "Control the lowering."),
            mistakes = listOf("Using furniture that can move.", "Sagging hips.", "Jerking your body up."),
        ),
        "Towel curl" to guide(
            steps = listOf(
                "Sit or stand and loop a towel under one foot, holding the ends with the same-side hand.",
                "Curl your hand toward your shoulder while pressing your foot down into the towel for resistance.",
                "Slowly let your arm straighten, keeping the resistance on. Finish all reps, then switch sides.",
            ),
            cues = listOf("Press down with your foot to make it harder.", "Elbow stays at your side.", "Slow and steady."),
            mistakes = listOf("Letting the towel go slack.", "Swinging your body.", "Letting your elbow drift forward."),
        ),
        "Underhand inverted row" to guide(
            steps = listOf(
                "Lie under a very sturdy table and grip the edge with an underhand grip, hands shoulder width apart.",
                "Keep your body in a straight line and pull your chest up, bending your elbows.",
                "Lower slowly until your arms are straight.",
            ),
            cues = listOf("Palms face you to work your biceps more.", "Keep your elbows close.", "Bend your knees to make it easier."),
            mistakes = listOf("Using a table that can tip.", "Sagging hips.", "Rushing the lowering."),
        ),
        "Chair dip" to guide(
            steps = listOf(
                "Sit on the edge of a sturdy chair with your hands next to your hips, then slide your hips off the seat with your knees bent.",
                "Bend your elbows straight back to lower your body until your upper arms are about parallel to the floor.",
                "Press back up until your arms are straight.",
            ),
            cues = listOf("Elbows point straight back.", "Keep your hips close to the chair.", "Bend your knees to make it easier."),
            mistakes = listOf("Flaring your elbows.", "Going so deep it strains your shoulders.", "Using a chair that can slide."),
        ),
        "Close-grip incline push-up" to guide(
            steps = listOf(
                "Place your hands on a sturdy table or counter, close together under your chest, and walk your feet back until your body is straight.",
                "Lower your chest toward your hands, keeping your elbows tucked.",
                "Press back up until your arms are straight.",
            ),
            cues = listOf("Elbows tucked close to your body.", "Straight line from head to heels.", "Lower the surface to make it harder."),
            mistakes = listOf("Flaring your elbows.", "Sagging hips.", "Using a surface that can slide."),
        ),
        "Table triceps extension" to guide(
            steps = listOf(
                "Place your hands on the edge of a very sturdy table, about shoulder width apart, and walk your feet back until your body is at a slant.",
                "Bend your elbows to lower your head toward the table edge, keeping your upper arms still.",
                "Straighten your arms to push back to the start.",
            ),
            cues = listOf("Only your elbows move.", "Keep your body in a straight line.", "Walk your feet back to make it harder."),
            mistakes = listOf("Flaring your elbows.", "Sagging hips.", "Using a table that can slide."),
        ),
        "Feet-elevated chair dip" to guide(
            steps = listOf(
                "Sit on the edge of one sturdy chair with your hands next to your hips and rest your heels on a second chair in front of you.",
                "Slide your hips off the seat and bend your elbows straight back to lower your body.",
                "Press back up until your arms are straight.",
            ),
            cues = listOf("Elbows point straight back.", "Keep your hips close to the chair.", "Stop if your shoulders feel pinched."),
            mistakes = listOf("Using chairs that can slide.", "Flaring your elbows.", "Going too deep."),
        ),
        "Plank shoulder tap" to guide(
            steps = listOf(
                "Start in a high plank with your hands under your shoulders and your feet a little wider than hip width.",
                "Lift one hand and tap the opposite shoulder, keeping your hips as still as you can.",
                "Return that hand and tap with the other, alternating.",
            ),
            cues = listOf("Wider feet make it easier.", "Hips stay level.", "Tighten your core."),
            mistakes = listOf("Rocking your hips side to side.", "Sagging in your lower back.", "Rushing the taps."),
        ),
        "Arm circles" to guide(
            steps = listOf(
                "Stand tall with your arms stretched out to the sides at shoulder height.",
                "Make small, controlled circles with your arms.",
                "After the target reps, reverse the direction.",
            ),
            cues = listOf("Keep your shoulders down.", "Arms stay at shoulder height.", "Keep it slow and smooth."),
            mistakes = listOf("Shrugging your shoulders.", "Letting your arms drop.", "Making big, swinging circles."),
        ),
        "Pike shoulder tap" to guide(
            steps = listOf(
                "Start in a pike position, hips high, with your hands and feet on the floor.",
                "Lift one hand and tap the opposite shoulder, keeping your hips high and still.",
                "Return that hand and alternate sides.",
            ),
            cues = listOf("Feet wider for more balance.", "Hips stay high.", "Move slowly."),
            mistakes = listOf("Dropping your hips.", "Twisting your body.", "Rushing."),
        ),
        "Crunch" to guide(
            steps = listOf(
                "Lie on your back with your knees bent and feet flat, hands lightly behind your head or across your chest.",
                "Curl your shoulders off the floor by contracting your abs.",
                "Pause at the top, then lower slowly.",
            ),
            cues = listOf("Lift with your abs, not your neck.", "Chin slightly tucked.", "Breathe out as you crunch."),
            mistakes = listOf("Pulling on your neck.", "Using momentum.", "Lowering too fast."),
        ),
        "Bird dog" to guide(
            steps = listOf(
                "Start on your hands and knees with your hands under your shoulders and your knees under your hips.",
                "Reach one arm forward and the opposite leg back until they are in line with your body.",
                "Hold for a second, return, and switch sides.",
            ),
            cues = listOf("Keep your hips level.", "Do not arch your lower back.", "Move slowly."),
            mistakes = listOf("Letting your hips twist.", "Lifting your leg too high.", "Rushing."),
        ),
        "Flutter kicks" to guide(
            steps = listOf(
                "Lie on your back with your legs straight and your hands under your hips.",
                "Press your lower back into the floor and lift your feet a few centimetres off the ground.",
                "Kick your legs up and down in small, quick alternating motions for the target time.",
            ),
            cues = listOf("Lower back stays down.", "Small, steady kicks.", "Keep your neck relaxed."),
            mistakes = listOf("Arching your lower back.", "Letting your feet touch the floor.", "Holding your breath."),
        ),
        "V-up" to guide(
            steps = listOf(
                "Lie on your back with your arms stretched overhead and your legs straight.",
                "At the same time, lift your arms and legs and reach your hands toward your toes, forming a V.",
                "Lower slowly with control and repeat.",
            ),
            cues = listOf("Use your abs, not momentum.", "Keep your legs as straight as you can.", "Bend your knees to make it easier."),
            mistakes = listOf("Swinging your arms to jerk yourself up.", "Dropping quickly on the way down.", "Holding your breath."),
        ),
        "Hollow hold" to guide(
            steps = listOf(
                "Lie on your back with your arms stretched overhead and your legs straight.",
                "Press your lower back into the floor, then lift your shoulders and legs off the ground.",
                "Hold the shape for the target time, breathing steadily.",
            ),
            cues = listOf("Lower back stays on the floor.", "Raise your legs higher to make it easier.", "Keep your ribs pulled down."),
            mistakes = listOf("Letting your lower back arch.", "Holding your breath.", "Lifting your head and straining your neck."),
        ),
        "Single-leg calf raise" to guide(
            steps = listOf(
                "Stand on one foot, holding a wall or chair for balance.",
                "Rise onto your toes as high as you can.",
                "Pause, then lower your heel slowly. Finish all reps and switch legs.",
            ),
            cues = listOf("Full range, up and down.", "Slow lowering.", "Use your hand only for balance."),
            mistakes = listOf("Bouncing.", "Rolling your ankle outward.", "Rushing the reps."),
        ),

        // General warm-up moves. These aren't in ExerciseLibrary, so they never appear in a routine
        // on their own, but the warm-up checklist links to this same guide screen for each one.
        "March or jog in place" to guide(
            steps = listOf(
                "Stand tall with your feet hip width apart.",
                "Lift your knees and swing your arms like an easy jog, without traveling anywhere.",
                "Keep a steady, comfortable pace for the full time.",
            ),
            cues = listOf("Land softly on the balls of your feet.", "Breathe normally.", "Pick up the pace only if you're still cold after a minute."),
            mistakes = listOf("Starting so fast you're out of breath by the end.", "Stomping flat-footed.", "Holding your arms stiff at your sides."),
        ),
        "Leg swings" to guide(
            steps = listOf(
                "Hold a wall or chair for balance and stand on one leg.",
                "Swing the other leg forward and back in a controlled arc, like a pendulum.",
                "Finish all reps, then face the other way and switch legs.",
            ),
            cues = listOf("Let the swing grow a little taller each rep.", "Keep your standing leg soft, not locked.", "Keep your hips facing forward."),
            mistakes = listOf("Swinging so hard you lose balance.", "Rounding your lower back to gain height.", "Rushing through without warming up."),
        ),
        "Hip circles" to guide(
            steps = listOf(
                "Stand with your feet a little wider than your hips, hands on your hips.",
                "Draw a slow, large circle with your hips, like you're hooping.",
                "Do all reps one way, then reverse direction.",
            ),
            cues = listOf("Keep your knees soft.", "Make the circle as big as feels comfortable.", "Breathe steadily through the motion."),
            mistakes = listOf("Only moving your upper body.", "Rushing the circles.", "Locking your knees straight."),
        ),
        "Cat-cow" to guide(
            steps = listOf(
                "Get on your hands and knees, hands under your shoulders and knees under your hips.",
                "Drop your belly and lift your chest and tailbone, looking slightly up (cow).",
                "Then round your spine toward the ceiling, tucking your chin and tailbone (cat).",
                "Flow between the two shapes with your breath for all reps.",
            ),
            cues = listOf("Inhale into the cow, exhale into the cat.", "Move slowly through your whole spine.", "Keep your arms steady under your shoulders."),
            mistakes = listOf("Moving only your neck instead of your whole spine.", "Rushing the breathing.", "Letting your hips drift behind your knees."),
        ),
        "Shoulder rolls" to guide(
            steps = listOf(
                "Stand tall with your arms relaxed at your sides.",
                "Roll both shoulders up, back and down in a slow circle.",
                "Do all reps, then reverse the direction.",
            ),
            cues = listOf("Make the circle as big as you can.", "Keep your arms loose, not swinging.", "Breathe normally throughout."),
            mistakes = listOf("Small, lazy circles that don't open the joint.", "Hunching your neck forward.", "Rushing through both directions."),
        ),
        "Reverse snow angel" to guide(
            steps = listOf(
                "Lie face down, or hinge forward from standing, with your arms by your sides, palms down.",
                "Sweep your arms out and up toward overhead, squeezing your shoulder blades together.",
                "Reverse the path back down with control and repeat.",
            ),
            cues = listOf("Lead with your thumbs turning up as you lift.", "Squeeze your shoulder blades at the top.", "Keep the movement slow on the way down."),
            mistakes = listOf("Shrugging your shoulders up toward your ears.", "Using momentum instead of control.", "Arching your lower back to gain range."),
        ),
    )
}
