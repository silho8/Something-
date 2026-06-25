# Something. Launcher - Release Instructions

Follow this checklist to generate a production-ready APK for "Something." Launcher.

## 1. Build Checklist
- [ ] Ensure all mock debug routes are verified.
- [ ] Confirm `applicationId` matches your intended Play Store package name in `app/build.gradle.kts`.
- [ ] Confirm `versionCode` is incremented.
- [ ] Confirm `versionName` is accurate (e.g., `1.0.0`).
- [ ] Ensure `minifyEnabled` and `shrinkResources` are set to `true` in the release block of `build.gradle.kts` (Already configured in this milestone).

## 2. Signing Checklist
- [ ] Open Android Studio.
- [ ] Navigate to **Build > Generate Signed Bundle / APK...**
- [ ] Select **APK** (or Android App Bundle for Play Store).
- [ ] If you don't have a Keystore, click **Create new...**
    - Choose a secure path.
    - Set a strong Keystore password.
    - Create a Key Alias and Key Password.
    - Fill out the Certificate details (First and Last Name, Organization, etc.).
- [ ] If you have an existing Keystore, select it and enter your credentials.
- [ ] Click **Next**.

## 3. APK Generation
- [ ] Select the **release** build variant.
- [ ] Select V1 (Jar Signature) and V2 (Full APK Signature) if generating an APK.
- [ ] Click **Finish**.
- [ ] Wait for Gradle to assemble the Release APK.
- [ ] The generated APK will be located in `app/release/app-release.apk`.

## 4. Play Store Preparation Checklist
- [ ] Generate high-resolution promotional screenshots.
- [ ] Ensure the "Something." minimalist AMOLED logo meets the 512x512 Play Store icon requirement.
- [ ] Prepare an engaging description highlighting the Dynamic Theme Engine, Glass UI, and NDot typography.
- [ ] Submit the AAB/APK to the Play Console.
- [ ] Fill out the Data Safety forms (The app does not collect remote data, but requires `QUERY_ALL_PACKAGES` permission disclosure).
