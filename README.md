# 345_Project

Starter scaffold for a SOEN 345 ticket reservation system.

## Tech Stack

- Java 17
- Gradle
- JUnit 5
- Android app module for emulator testing

## Current Structure

The project follows a modular monolith layout with clear boundaries:

- `presentation`: entry points and request/response DTOs
- `application`: use cases and validation logic
- `domain`: business models and repository interfaces
- `infrastructure`: concrete adapters such as in-memory repositories and password hashing

The repo also includes:

- `app`: Android UI module for emulator demos
- `core`: Gradle Java module that reuses the current business logic under `src/main/java`

## TDD Workflow

1. Write a failing test in `src/test/java`
2. Add the smallest implementation needed to pass
3. Refactor while keeping tests green

## Run Tests

```bash
gradle :core:test
```

## Android Emulator Demo

Open the project in Android Studio and run the `app` configuration on an emulator.

Demo accounts:

- Customer: `customer@site.com` / `secret123`
- Admin: `admin@site.com` / `adminpass`

The Android app currently uses fake in-memory users so the UI flow works before SQL is implemented.

## Android Studio Sync

If Android Studio still shows red files after these changes:

1. Close the project.
2. Delete the project's `.idea` folder.
3. Re-open the root folder as a Gradle project.
4. Let Android Studio finish the Gradle sync.

## Send Confirmation Emails

For the application to be able to send emails it needs a sender email.
In this case it will be whichever email is set in the local.properties file.

1. Add the GOOGLE_APP_PASSWORD SENDER_EMAIL variables to local.properties
2. Get an App Password within your google account so our app bypasses double auth
3. set the variables on local.properties to your google email and the App Password
