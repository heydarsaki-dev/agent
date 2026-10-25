package com.past9.phoneaos.ui.i18n

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import java.util.Locale

/**
 * App language, one place. Persian ("fa") is the default; "en" switches back.
 * Sourced from the same SharedPreferences the settings screen writes, so it is readable
 * before the graph exists (attachBaseContext) and inside composables alike.
 */
object I18n {
    const val DEFAULT_LANGUAGE = "fa"

    fun language(context: Context): String =
        context.getSharedPreferences("settings", Context.MODE_PRIVATE)
            .getString("language", DEFAULT_LANGUAGE)?.takeIf { it == "en" || it == "fa" } ?: DEFAULT_LANGUAGE

    fun isPersian(lang: String): Boolean = lang != "en"

    /** The Persian dictionary, assembled from the per-screen maps in the fa/ folder. */
    val fa: Map<String, String> by lazy { faParts.flatMap { it().toList() }.toMap() }

    /** Translate an exact English UI string. Unknown strings fall back to the English itself. */
    fun tr(s: String, language: String): String = if (isPersian(language)) fa[s] ?: s else s

    /**
     * Translate a static template with positional args ("%1$s asked to %2$s"). In Persian the
     * placeholders may sit in a different order than English; use the fa-IR locale so numbers
     * come out in Persian digits. Unknown templates fall back to the English wording.
     */
    fun trArgs(template: String, language: String, vararg args: Any?): String {
        if (!isPersian(language)) return String.format(java.util.Locale.US, template, *args)
        val t = fa[template] ?: template
        // %s alone never localises digits: "5" stays Latin even under fa-IR. Counts and
        // durations are passed as Numbers, so spell those in Persian digits here; names,
        // emails and urls arrive as Strings and must stay exactly as the user typed them.
        // Templates with %d (or %1$d etc.) let Java's formatter localise digits natively —
        // converting the arg to a String would crash. Detect %d and only convert Numbers
        // when the template has no decimal conversion.
        val hasDecimal = java.util.regex.Pattern.compile("%\\d*\\$?d").matcher(t).find()
        val local = java.util.Arrays.copyOf(args, args.size).map {
            if (it is Number && !hasDecimal) persianDigits(it.toString()) else it
        }
        return String.format(PERSIAN_LOCALE, t, *local.toTypedArray())
    }

    /** The ten Indodecimal digits, indexed by the Latin digit they replace. */
    private val FA_DIGITS = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')

    /** Rewrite the ASCII digits of [s] as Persian ones, leaving everything else untouched. */
    fun persianDigits(s: String): String {
        if (s.none { it in '0'..'9' }) return s
        val out = StringBuilder(s.length)
        for (c in s) out.append(if (c in '0'..'9') FA_DIGITS[c - '0'] else c)
        return out.toString()
    }

    val PERSIAN_LOCALE: Locale get() = Locale.forLanguageTag("fa-IR")
}

/** The per-screen Persian maps, in one registry so I18n can merge them. */
internal val faParts: List<() -> Map<String, String>> = listOf(
    { com.past9.phoneaos.ui.i18n.fa.faUi },
)

/** The current language, provided once around the app's root. Defaults to English so previews and tests stay stable. */
val LocalLanguage = staticCompositionLocalOf { "en" }

/** Translate a user-visible English string into the current language. Composable-friendly. */
@Composable
fun tr(s: String): String = I18n.tr(s, LocalLanguage.current)

/** Translate a positional-arg template into the current language. Composable-friendly. */
@Composable
fun trArgs(template: String, vararg args: Any?): String = I18n.trArgs(template, LocalLanguage.current, *args)

/** Same translation, for non-composable code (services, the runtime, notifications). */
fun trAny(s: String, language: String): String = I18n.tr(s, language)

/**
 * Persian needs a Persian locale for the app's layout to be right-to-left and for anything
 * that reads the configuration (date formats, IME, platform pickers) to follow it.
 */
fun Context.withAppLanguage(): Context {
    val lang = I18n.language(this)
    if (!I18n.isPersian(lang)) {
        // English: keep the system locale only when it is already English, otherwise pin en-US.
        val system = resources.configuration.locales[0]
        if (system?.language == "en") return this
        val locale = Locale.forLanguageTag("en-US")
        Locale.setDefault(locale)
        val config = android.content.res.Configuration(resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        return createConfigurationContext(config)
    }
    val locale = Locale.forLanguageTag("fa-IR")
    Locale.setDefault(locale)
    val config = android.content.res.Configuration(resources.configuration)
    config.setLocale(locale)
    config.setLayoutDirection(locale)
    return createConfigurationContext(config)
}
