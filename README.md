# A.N.D. Grepolis — Android Notification Dispatcher

> **Android only** — this application requires an Android device and is not available for iOS.

A.N.D. Grepolis is an Android app that listens for incoming Grepolis notifications on your device and automatically forwards them to a destination of your choice (e.g. a Discord webhook). Never miss an attack, report, or alliance message while you're away from the game.

---

## Features

- Listens for Grepolis notifications in real time using Android's `NotificationListenerService`
- Forwards notifications automatically in the background, even when your screen is off
- Survives device reboots — the service restarts automatically on boot
- Runs as a foreground service to stay alive on battery-optimised devices
- Network state awareness: queues or retries forwarding when connectivity is lost

---

## Requirements

| Requirement | Minimum |
|---|---|
| Android version | 7.0 Nougat (API 24) |
| Target SDK | Android 14 (API 34) |
| Internet connection | Required for forwarding |
| Grepolis app | Must be installed and receiving notifications |

---

## Installation

1. Download the `app-release.apk` file to your Android device.
2. In your device settings, enable **Install from unknown sources** (or "Install unknown apps") for your browser or file manager.
3. Open the APK file and tap **Install**.
4. Launch **A.N.D. Grepolis** from your app drawer.

---

## Setup & Permissions

The app will prompt you to grant several permissions required for it to function:

| Permission | Purpose |
|---|---|
| Notification Listener | Read incoming Grepolis notifications |
| Post Notifications | Show the persistent foreground service notification |
| Run at Startup | Restart the service automatically after a reboot |
| Ignore Battery Optimisation | Prevent Android from killing the background service |
| Internet & Network Access | Forward notifications to your webhook/endpoint |
| Display over other apps | Show response alerts if configured |

> ⚠️ The **Notification Listener** permission is the most critical one. Without it, the app cannot read any notifications. Grant it via **Settings → Apps → Special app access → Notification access**.

---

## Version

| Field | Value |
|---|---|
| Version name | 1.1.2.0 |
| Version code | 4 |
| Package | `com.example.grepolisnotificationforwarder` |

---

## Notes

- This app is **not available on the Google Play Store** and must be sideloaded.
- It is designed specifically for the Grepolis mobile app and will only forward notifications originating from it.
- iOS is not supported. Android's `NotificationListenerService` API has no equivalent on iOS due to platform restrictions.

---

## Source

Source code is available on GitHub: [github.com/GFRCnlvng](https://github.com/GFRCnlvng)
