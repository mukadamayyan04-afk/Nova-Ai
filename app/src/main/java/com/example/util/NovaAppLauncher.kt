package com.example.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import android.provider.Settings

data class AppCommandResult(
    val handled: Boolean,
    val spokenResponse: String,
    val commandLabel: String? = null
)

object NovaAppLauncher {

    fun executeIfCommand(context: Context, input: String): AppCommandResult? {
        val clean = input.trim().lowercase()

        // Clean out leading wake words or filler
        val text = clean
            .replace("nova", "")
            .replace("hey", "")
            .replace("please", "")
            .replace("kripya", "")
            .replace("karo", "")
            .trim()

        // 1. YouTube
        if (text.contains("youtube") && (text.contains("open") || text.contains("kholo") || text.contains("play") || text.contains("chalao") || text.contains("start"))) {
            return launchYouTube(context)
        }

        // 2. Chrome
        if (text.contains("chrome") && (text.contains("open") || text.contains("kholo") || text.contains("browser") || text.contains("chalao"))) {
            return launchChrome(context)
        }

        // 3. Google Search / App
        if ((text.contains("google") || text.contains("search on google")) && (text.contains("open") || text.contains("kholo") || text.contains("chalao") || text.contains("search"))) {
            val query = text.replace("open", "").replace("google", "").replace("search", "").replace("for", "").trim()
            return launchGoogle(context, query)
        }

        // 4. WhatsApp
        if (text.contains("whatsapp") && (text.contains("open") || text.contains("kholo") || text.contains("chalao"))) {
            return launchWhatsApp(context)
        }

        // 5. Camera
        if (text.contains("camera") && (text.contains("open") || text.contains("kholo") || text.contains("chalao") || text.contains("photo") || text.contains("pic"))) {
            return launchCamera(context)
        }

        // 6. Settings
        if (text.contains("settings") && (text.contains("open") || text.contains("kholo") || text.contains("phone"))) {
            return launchSettings(context)
        }

        // 7. Phone / Dialer
        if (text.startsWith("call") || text.startsWith("dial") || text.contains("phone dialer") || text.contains("call lagao")) {
            val target = text.replace("call", "").replace("dial", "").replace("lagao", "").replace("ko", "").trim()
            return launchDialer(context, target)
        }

        // 8. Open website directly e.g. "open github.com" or "open website wikipedia.org"
        val websiteMatch = Regex("(?:open|kholo|browse|visit)\\s+(?:website\\s+)?([a-zA-Z0-9-]+\\.[a-zA-Z]{2,}(?:/[^\\s]*)?)").find(text)
        if (websiteMatch != null) {
            val domain = websiteMatch.groupValues[1]
            return openUrl(context, if (domain.startsWith("http")) domain else "https://$domain", domain)
        }

        return null
    }

    fun launchYouTube(context: Context): AppCommandResult {
        return try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:")).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            if (isIntentAvailable(context, intent)) {
                context.startActivity(intent)
                AppCommandResult(true, "Opening YouTube for you now.", "YouTube")
            } else {
                openUrl(context, "https://www.youtube.com", "YouTube Web")
            }
        } catch (e: Exception) {
            openUrl(context, "https://www.youtube.com", "YouTube Web")
        }
    }

    fun launchChrome(context: Context): AppCommandResult {
        return try {
            val intent = context.packageManager.getLaunchIntentForPackage("com.android.chrome")
            if (intent != null) {
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
                AppCommandResult(true, "Launching Google Chrome, Boss.", "Chrome")
            } else {
                openUrl(context, "https://www.google.com", "Browser")
            }
        } catch (e: Exception) {
            openUrl(context, "https://www.google.com", "Browser")
        }
    }

    fun launchGoogle(context: Context, query: String = ""): AppCommandResult {
        return try {
            if (query.isNotEmpty()) {
                val intent = Intent(Intent.ACTION_WEB_SEARCH).apply {
                    putExtra("query", query)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
                AppCommandResult(true, "Searching Google for $query.", "Google Search")
            } else {
                val intent = context.packageManager.getLaunchIntentForPackage("com.google.android.googlequicksearchbox")
                if (intent != null) {
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    context.startActivity(intent)
                    AppCommandResult(true, "Opening Google now.", "Google")
                } else {
                    openUrl(context, "https://www.google.com", "Google Web")
                }
            }
        } catch (e: Exception) {
            openUrl(context, "https://www.google.com", "Google Web")
        }
    }

    fun launchWhatsApp(context: Context): AppCommandResult {
        return try {
            val intent = context.packageManager.getLaunchIntentForPackage("com.whatsapp")
            if (intent != null) {
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
                AppCommandResult(true, "Opening WhatsApp for you.", "WhatsApp")
            } else {
                AppCommandResult(false, "WhatsApp is not installed on this device. Would you like to check Google Play Store?", "WhatsApp Not Found")
            }
        } catch (e: Exception) {
            AppCommandResult(false, "Could not open WhatsApp: ${e.message}", "Error")
        }
    }

    fun launchCamera(context: Context): AppCommandResult {
        return try {
            val intent = Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            AppCommandResult(true, "Opening camera.", "Camera")
        } catch (e: Exception) {
            AppCommandResult(false, "Unable to launch camera app.", "Camera Error")
        }
    }

    fun launchSettings(context: Context): AppCommandResult {
        return try {
            val intent = Intent(Settings.ACTION_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            AppCommandResult(true, "Opening device settings.", "Settings")
        } catch (e: Exception) {
            AppCommandResult(false, "Could not open settings.", "Settings Error")
        }
    }

    fun launchDialer(context: Context, number: String = ""): AppCommandResult {
        return try {
            val uri = if (number.matches(Regex("[0-9+\\-#*]+"))) {
                Uri.parse("tel:$number")
            } else {
                Uri.parse("tel:")
            }
            val intent = Intent(Intent.ACTION_DIAL, uri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            AppCommandResult(true, "Preparing dialer.", "Phone Dialer")
        } catch (e: Exception) {
            AppCommandResult(false, "Failed to launch phone dialer.", "Dialer Error")
        }
    }

    fun openUrl(context: Context, url: String, label: String = "Website"): AppCommandResult {
        return try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            AppCommandResult(true, "Opening $label in your browser.", label)
        } catch (e: Exception) {
            AppCommandResult(false, "Failed to open $url: ${e.message}", "Browser Error")
        }
    }

    private fun isIntentAvailable(context: Context, intent: Intent): Boolean {
        val activities = context.packageManager.queryIntentActivities(
            intent,
            PackageManager.MATCH_DEFAULT_ONLY
        )
        return activities.isNotEmpty()
    }
}
