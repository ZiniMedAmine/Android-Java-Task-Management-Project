# My Task Management Project

A native Android to-do / task manager written in **Java**. Users sign up with email and password (Firebase Authentication), then create, track and complete their own tasks. Each task can have a deadline and a photo from the camera or gallery. Tasks are stored **locally on the device** in SQLite; user profiles live in **Firebase Realtime Database**.

> Built as a university project for the *Native Mobile Development* course (2nd year, Engineering).

![Platform](https://img.shields.io/badge/platform-Android-3DDC84?logo=android&logoColor=white)
![Language](https://img.shields.io/badge/language-Java-007396?logo=openjdk&logoColor=white)
![Min SDK](https://img.shields.io/badge/minSdk-26-blue)
![Target SDK](https://img.shields.io/badge/targetSdk-36-blue)
![Firebase](https://img.shields.io/badge/backend-Firebase-FFCA28?logo=firebase&logoColor=black)

---

## Features

**Authentication**
- Sign up with full name, email and password
- Sign in / sign out with Firebase Authentication
- Stays signed in between launches (skips the login screen when a session exists)

**Tasks**
- Create a task with a title, description, optional deadline (date and time picker) and optional image
- Attach an image from the **camera** or the **gallery**; images are compressed and saved to the app's private storage
- Task list with a live countdown ("3 days, 4 hours left", "Overdue!")
- Mark a task complete straight from the list
- Details screen to edit the title, description, deadline and completion status, or delete the task
- Each user only sees their own tasks (tasks are scoped by Firebase UID)

**Profile**
- View and edit your full name (synced to Firebase Realtime Database)
- Change your password (at least 6 characters, with confirmation)

<!--
## Screenshots

Add screenshots to docs/screenshots/ and uncomment this section.

| Sign in | Tasks | Add task | Details | Profile |
|:---:|:---:|:---:|:---:|:---:|
| <img src="docs/screenshots/sign_in.png" width="180"/> | <img src="docs/screenshots/tasks.png" width="180"/> | <img src="docs/screenshots/add_task.png" width="180"/> | <img src="docs/screenshots/details.png" width="180"/> | <img src="docs/screenshots/profile.png" width="180"/> |
-->

## Tech stack

| Area | Technology |
|---|---|
| Language | Java 11 |
| UI | XML layouts, Fragments, Material Components, ConstraintLayout |
| Authentication | Firebase Authentication (email/password) |
| Remote data | Firebase Realtime Database (user profiles) |
| Local data | SQLite via `SQLiteOpenHelper` (tasks) |
| Images | Camera/gallery intents, `FileProvider`, internal app storage |
| Build | Gradle (Kotlin DSL), Android Gradle Plugin 8.13, version catalog |
| SDK | `minSdk 26` · `targetSdk 36` · `compileSdk 36` |

## Architecture

```
MainActivity  (auth host)
├── SignInFragment
└── SignUpFragment
        │  on successful login
        ▼
HomeActivity  (main host + bottom navigation)
├── TasksFragment    → ListView + TasksAdapter
├── AddTaskFragment  → camera / gallery / date-time picker
└── ProfileFragment  → Firebase Realtime Database
        │  tap a task
        ▼
Details  (edit / update / delete a task)
```

### Project structure

```
app/src/main/java/com/example/mytaskmanagementproject/
├── MainActivity.java       # Entry point; redirects signed-in users to HomeActivity
├── SignInFragment.java     # Email/password sign in
├── SignUpFragment.java     # Account creation + stores profile in Realtime DB
├── HomeActivity.java       # Bottom navigation (Tasks, Add, Profile, Logout)
├── TasksFragment.java      # Lists the current user's tasks
├── TasksAdapter.java       # List item rendering, time-left countdown, "Complete" button
├── AddTaskFragment.java    # New task form with image and deadline
├── Details.java            # View / edit / delete a single task
├── ProfileFragment.java    # Edit name, change password
├── DatabaseHandler.java    # SQLite helper (CRUD for tasks)
├── ImageUtils.java         # Save / load / delete task images
└── Task.java               # Task model
```

### Data model

**SQLite:** database `taskManager`, table `tasks`

| Column | Type | Notes |
|---|---|---|
| `id` | INTEGER | Primary key, autoincrement |
| `title` | TEXT | |
| `description` | TEXT | |
| `deadline` | INTEGER | Epoch millis, nullable |
| `status` | INTEGER | `0` = incomplete, `1` = completed |
| `user_id` | TEXT | Firebase UID of the owner |
| `image_path` | TEXT | Absolute path in app-private storage, nullable |

**Firebase Realtime Database**

```
users/
  <uid>/
    fullName: "..."
    email: "..."
```

## Getting started

### Prerequisites

- [Android Studio](https://developer.android.com/studio) (a recent stable version that supports AGP 8.13)
- JDK 11 or newer (the one bundled with Android Studio works)
- An Android device or emulator running **Android 8.0 (API 26)** or newer
- A [Firebase](https://console.firebase.google.com/) account

### 1. Clone the repository

```bash
git clone https://github.com/<your-username>/MyTaskManagementProject.git
```

### 2. Set up Firebase

The app needs its own Firebase project. The `google-services.json` file is **not** committed to this repository.

1. Create a project in the [Firebase console](https://console.firebase.google.com/).
2. Add an **Android app** with the package name `com.example.mytaskmanagementproject`.
3. Download `google-services.json` and place it in the `app/` folder.
4. Under **Authentication → Sign-in method**, enable **Email/Password**.
5. Create a **Realtime Database**, then copy its URL (for example `https://<project-id>-default-rtdb.<region>.firebasedatabase.app/`).
6. Replace the hard-coded database URL with yours in these files:
   - `SignUpFragment.java`
   - `HomeActivity.java`
   - `ProfileFragment.java`
7. Set database rules so each user can only read and write their own profile:

   ```json
   {
     "rules": {
       "users": {
         "$uid": {
           ".read": "$uid === auth.uid",
           ".write": "$uid === auth.uid"
         }
       }
     }
   }
   ```

### 3. Build and run

Open the project in Android Studio, let Gradle sync, then press **Run ▶**.

Or from the command line:

```bash
./gradlew assembleDebug
```

The APK is written to `app/build/outputs/apk/debug/`.

## Permissions

| Permission | Why |
|---|---|
| `CAMERA` | Take a photo to attach to a task (requested at runtime) |
| `READ_EXTERNAL_STORAGE` / `WRITE_EXTERNAL_STORAGE` | Pick an image from the gallery on older Android versions |
| `ACCESS_MEDIA_LOCATION` | Media access when reading gallery images |

## Known limitations

- Tasks are stored **only on the device**. They don't sync between devices and are lost if the app is uninstalled.
- The task list shows the raw `Date.toString()` format for deadlines.
- There are no notifications or reminders when a deadline gets close.

## Possible improvements

- Sync tasks to Firestore or Realtime Database for multi-device support
- Deadline reminders with `WorkManager` + notifications
- Replace `ListView` with `RecyclerView`, and SQLite with Room
- Filter and sort tasks (by deadline, status)
- Dark theme polish and localization

## Author

**Zini**: Engineering student, Native Mobile Development course.
