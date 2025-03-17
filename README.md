# R-Auto Update Library (An Android library for auto update app from the latest release.)

[![Release](https://jitpack.io/v/rajeev-k47/R-AUL.svg)](https://jitpack.io/#rajeev-k47/R-AUL)
![Monthly download statistics](https://jitpack.io/v/rajeev-k47/R-AUL/month.svg)

## Overview
R-aul is an Android library that monitors the latest releases of a specified GitHub repository and notifies users when a new version is available. It allows seamless update checks and provides an option to download and install the latest version directly.
## Features
- Listens for new releases from a given GitHub repository
- Notifies users when an update is available
- Supports automatic APK downloads
- Provides an easy way to install updates.

## Prerequisites

Add this in your root `settings.gradle.kts` For (**`Kotlin DSL`**):

```gradle
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        ...
        maven { url = uri("https://jitpack.io") }
    }
}
```

## Dependency

This library is available on [Jitpack](https://jitpack.io)

Add this to your module's `build.gradle` file (make sure to follow Prerequisites first):

```gradle
dependencies {
	...
     implementation("com.github.rajeev-k47:R-AUL:v2.0")
}
```
## Usage

⭐ To install the new version successfully, the APK must be signed with the same key as the currently installed version. Otherwise, the installation will fail due to package conflicts.

⭐ For the R-AUL to listen from github releases you should create a tag of every release in a proper regex ``"v$versionName$versionCode"`` e.g. ``v1.2.6`` and put apk build file in that release.

Initialise the object in onCreate method -
``` kotlin
Raul.init("GITHUB-USERNAME","REPOSITORY")
Raul.listen(context)
```
This will listen for the latest release from the provided repository and notify the user whenever a new release is published.

