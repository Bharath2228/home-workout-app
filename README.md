# HomeForge

An Android workout planner and tracker built around the plates and rods you actually own. Everything is stored on
the phone, so it works offline.

## Features

### Your equipment
- Enter your plates (weight and count) and the weight of one rod. Add, remove or change them any time.
- Every suggested weight is one you can really build. Three set-ups are handled: a single dumbbell, a pair of
  dumbbells (both loaded the same) and the two rods joined into one barbell (plates mirrored on both sides).
- A plate guide shows which plates to load, using the fewest plates, for example "Per side: 5 + 1.5".
- Warm-up sets for the first heavy lift of a workout, built from loads you can make.
- A general mobility warm-up checklist shown before every training day, and logged with the workout.

### Plans, calendar and levels
- Three plans, chosen by how many days a week you can train: Full body (2 to 3 days), Push / Pull / Legs
  (3 to 4 days) and 6-day Push / Pull / Legs (5 to 6 days). Each has a plain-words description.
- Readable day names (Day 1, Push 1, Legs 2) and a line saying which muscles the day trains.
- An optional Core day for a rest day. Every other training day already ends with core work.
- A calendar with a start date and your training weekdays. A missed workout stays next and later dates slide back,
  so no part of the plan is skipped. Workouts on rest days count as extras. Weeks, done and missed counts are shown.
- Three levels: Beginner, Intermediate, Advanced. Level changes the exercises, the number of sets and where new
  exercises start. You move up automatically after 24 completed workouts (Intermediate) and 72 (Advanced), or you
  can pick your level yourself.
- A no-equipment mode that builds workouts from bodyweight exercises only.
- Training goals (strength, muscle gain, weight loss) that set rep ranges and rest times.
- Exercise variations rotate every 4 weeks. You can also swap any exercise yourself.

### Exercises
- 79 exercises across squat, hinge, lunge, push, pull, biceps, triceps, shoulders, core and calves.
- Step-by-step instructions, form cues and common mistakes for every exercise.
- Pictures for 22 exercises, with credits (see below), and a button that opens a video search for any exercise.

### Logging a workout
- Log weight and reps for each set, with last time's result shown.
- Add, remove and reorder exercises and sets during a workout.
- A rest timer starts when you tick a set, with vibration and a beep when it ends.
- Weight, sets and reps adapt to how your last sessions went, and each change shows a short reason: weight up,
  hold, one step lighter, a deload after three stalled sessions, or an extra set once you reach your heaviest load.
- Edit or delete past workouts and sets.
- A running timer shows how long the workout has taken. The length is saved with each workout and shown in your
  history, and the start and end times go to Health Connect.

### Progress
- Charts of your top weight (or total reps) per exercise, and your best set.
- Personal-record messages when you finish a workout, and a weekly streak.
- Body weight and measurements (waist, chest, upper arm, thigh) over time.
- Warnings for six training days in a row, a sharp jump in weekly sets and long breaks.

### Health Connect (optional)
- Sends each workout to Health Connect as a strength-training session, with the exercises and reps.
- Adds an estimated calorie burn (from your body weight and workout length) and your body weight.
- Deleting or editing a workout updates Health Connect. Needs a real phone with Health Connect.

### Everything else
- Reminders on your training days.
- Backup: export your data to a file and import it again.
- A welcome screen that sets your level, plan and goal. Light and dark themes.

## Picture credits

The exercise pictures in `app/src/main/res/drawable-nodpi/` come from the [wger](https://wger.de) exercise
database and are used under Creative Commons Attribution-ShareAlike licenses (CC BY-SA 3.0 and 4.0). They were
cropped and resized. Authors and source pages for each picture are listed in the app under
Settings > Picture credits, and in `app/src/main/java/com/bharath/homeforge/domain/ExerciseMedia.kt`.

## Limits

- Calories are a rough estimate, not a measurement. Heart rate comes only from a wearable.
- Reminders can arrive a few minutes late and don't check whether you already trained.
- Some exercises have no picture yet, only written instructions.
- Data lives on the phone. Use Settings > Backup to keep a copy.

## Tech

Kotlin, Jetpack Compose and Material 3, Room for storage, Health Connect client, minimum Android 8.0 (API 26).
Open the project in Android Studio to build and run it. The unit tests cover the weight, plan, schedule, level,
adaptation and backup logic and run with the Gradle `test` task.
