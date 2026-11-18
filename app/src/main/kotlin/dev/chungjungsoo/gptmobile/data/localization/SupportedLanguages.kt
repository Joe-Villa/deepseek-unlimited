package dev.chungjungsoo.gptmobile.data.localization

data class AppLanguage(
    val tag: String,
    val nativeName: String,
    val englishName: String
)

object SupportedLanguages {
    val system = AppLanguage("system", "System", "System default")

    val all: List<AppLanguage> = listOf(
        system,
        AppLanguage("en", "English", "English"),
        AppLanguage("zh-CN", "简体中文", "Chinese (Simplified)"),
        AppLanguage("zh-TW", "繁體中文", "Chinese (Traditional)"),
        AppLanguage("ja", "日本語", "Japanese"),
        AppLanguage("ko", "한국어", "Korean"),
        AppLanguage("fr", "Français", "French"),
        AppLanguage("de", "Deutsch", "German"),
        AppLanguage("es", "Español", "Spanish"),
        AppLanguage("pt-BR", "Português (Brasil)", "Portuguese (Brazil)"),
        AppLanguage("ru", "Русский", "Russian"),
        AppLanguage("ar", "العربية", "Arabic"),
        AppLanguage("hi", "हिन्दी", "Hindi"),
        AppLanguage("id", "Bahasa Indonesia", "Indonesian"),
        AppLanguage("it", "Italiano", "Italian"),
        AppLanguage("th", "ไทย", "Thai"),
        AppLanguage("vi", "Tiếng Việt", "Vietnamese"),
        AppLanguage("tr", "Türkçe", "Turkish"),
        AppLanguage("nl", "Nederlands", "Dutch"),
        AppLanguage("pl", "Polski", "Polish"),
        AppLanguage("uk", "Українська", "Ukrainian"),
        AppLanguage("fa", "فارسی", "Persian")
    )

    fun byTag(tag: String): AppLanguage = all.firstOrNull { it.tag == tag } ?: system
}
