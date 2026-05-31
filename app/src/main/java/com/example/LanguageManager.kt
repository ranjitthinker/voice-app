package com.example

import android.content.Context
import androidx.compose.runtime.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

enum class AppLanguage(val code: String, val displayName: String) {
    ENGLISH("en", "English"),
    HINDI("hi", "हिन्दी"),
    PUNJABI("pa", "ਪੰਜਾਬੀ")
}

object LanguageManager {
    private val _currentLanguage = MutableStateFlow(AppLanguage.ENGLISH)
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage

    fun init(context: Context) {
        val prefs = context.getSharedPreferences("voice_vault_prefs", Context.MODE_PRIVATE)
        val langStr = prefs.getString("app_language", AppLanguage.ENGLISH.name) ?: AppLanguage.ENGLISH.name
        _currentLanguage.value = AppLanguage.values().firstOrNull { it.name == langStr } ?: AppLanguage.ENGLISH
    }

    fun setLanguage(context: Context, lang: AppLanguage) {
        _currentLanguage.value = lang
        val prefs = context.getSharedPreferences("voice_vault_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("app_language", lang.name).apply()
    }

    private val translations = mapOf(
        AppLanguage.ENGLISH to mapOf(
            "app_title" to "VoiceVault",
            "dashboard_mic_prompt" to "Press the mic icon inside bottom bar to initiate your first recording session securely.",
            "search_placeholder" to "Search recordings...",
            "home" to "Home",
            "record" to "Record",
            "favorites" to "Favorites",
            "search" to "Search",
            "settings" to "Settings",
            "quick_actions" to "Quick Actions",
            "action_new" to "New",
            "action_favs" to "Favs",
            "action_recent" to "Recent",
            "action_shared" to "Shared",
            "stats_title" to "Vault Statistics",
            "stat_total_recordings" to "Recordings",
            "stat_total_time" to "Duration (Hr)",
            "dialog_rename_title" to "Rename Session Vault",
            "dialog_rename_field" to "Vault Title",
            "dialog_btn_rename" to "Rename",
            "dialog_btn_cancel" to "Cancel",
            "play_speed" to "SPEED",
            "play_share" to "SHARE",
            "play_favorite" to "FAVORITE",
            "play_copy" to "COPY",
            "play_delete" to "DELETE",
            "play_repeat_mode" to "Repeat Mode",
            "play_repeat_off" to "Repeat Off",
            "play_repeat_one" to "Repeat One",
            "play_repeat_starred" to "Repeat Starred",
            "play_repeat_all" to "Repeat All",
            "rec_status_active" to "SECURE RECORDING ACTIVE",
            "rec_status_paused" to "RECORDING PAUSED",
            "rec_save_title_label" to "Custom Title (Optional)",
            "rec_save_tag_label" to "Category Tag",
            "rec_btn_save" to "Save Session",
            "rec_btn_cancel" to "Discard",
            "rec_btn_pause" to "Pause",
            "rec_btn_resume" to "Resume",
            "bookmark_toast" to "Bookmark added at current timestamp!",
            "copylink_toast" to "Secure link prepared. Launching native direct share options...",
            "settings_language" to "App Language",
            "tag_meeting" to "MEETING",
            "tag_interview" to "INTERVIEW",
            "tag_memo" to "MEMO"
        ),
        AppLanguage.HINDI to mapOf(
            "app_title" to "वॉइसवॉल्ट",
            "dashboard_mic_prompt" to "सुरक्षित रूप से अपना पहला रिकॉर्डिंग सत्र शुरू करने के लिए नीचे दिए गए माइक आइकन को दबाएं।",
            "search_placeholder" to "रिकॉर्डिंग खोजें...",
            "home" to "होम",
            "record" to "रिकॉर्ड",
            "favorites" to "पसंदीदा",
            "search" to "खोजें",
            "settings" to "सेटिंग्स",
            "quick_actions" to "त्वरित क्रियाएं",
            "action_new" to "नया",
            "action_favs" to "पसंदीदा",
            "action_recent" to "हालिया",
            "action_shared" to "साझा",
            "stats_title" to "वॉल्ट सांख्यिकी",
            "stat_total_recordings" to "कुल रिकॉर्डिंग्स",
            "stat_total_time" to "अवधि (घंटे)",
            "dialog_rename_title" to "सत्र का नाम बदलें",
            "dialog_rename_field" to "शीर्षक",
            "dialog_btn_rename" to "नाम बदलें",
            "dialog_btn_cancel" to "रद्द करें",
            "play_speed" to "गति",
            "play_share" to "साझा करें",
            "play_favorite" to "पसंदीदा",
            "play_copy" to "कॉपी करें",
            "play_delete" to "हटाएं",
            "play_repeat_mode" to "दोहराएँ मोड",
            "play_repeat_off" to "बंद",
            "play_repeat_one" to "एक दोहराएं",
            "play_repeat_starred" to "पसंदीदा दोहराएं",
            "play_repeat_all" to "सभी दोहराएं",
            "rec_status_active" to "सुरक्षित रिकॉर्डिंग सक्रिय है",
            "rec_status_paused" to "रिकॉर्डिंग रुकी हुई है",
            "rec_save_title_label" to "कस्टम शीर्षक (वैकल्पिक)",
            "rec_save_tag_label" to "श्रेणी टैग",
            "rec_btn_save" to "सत्र सहेजें",
            "rec_btn_cancel" to "खारिज करें",
            "rec_btn_pause" to "रोकें",
            "rec_btn_resume" to "शुरू करें",
            "bookmark_toast" to "वर्तमान समय पर बुकमार्क जोड़ा गया!",
            "copylink_toast" to "सुरक्षित लिंक तैयार किया गया। शेयर विकल्प खोल रहा है...",
            "settings_language" to "ऐप की भाषा",
            "tag_meeting" to "बैठक",
            "tag_interview" to "साक्षात्कार",
            "tag_memo" to "मेमो"
        ),
        AppLanguage.PUNJABI to mapOf(
            "app_title" to "ਵੌਇਸਵੌਲਟ",
            "dashboard_mic_prompt" to "ਸੁਰੱਖਿਅਤ ਰੂਪ ਨਾਲ ਆਪਣਾ ਪਹਿਲਾ ਰਿਕਾਰਡਿੰਗ ਸੈਸ਼ਨ ਸ਼ੁਰੂ ਕਰਨ ਲਈ ਹੇਠਾਂ ਦਿੱਤੇ ਮਾਈਕ ਆਈਕਨ ਨੂੰ ਦਬਾਓ।",
            "search_placeholder" to "ਰਿਕਾਰਡਿੰਗਜ਼ ਖੋਜੋ...",
            "home" to "ਹੋਮ",
            "record" to "ਰਿਕਾਰਡ",
            "favorites" to "ਮਨਪਸੰਦ",
            "search" to "ਖੋਜੋ",
            "settings" to "ਸੈਟਿੰਗਜ਼",
            "quick_actions" to "ਤੁਰੰਤ ਕਾਰਵਾਈਆਂ",
            "action_new" to "ਨਵਾਂ",
            "action_favs" to "ਮਨਪਸੰਦ",
            "action_recent" to "ਹਾਲੀਆ",
            "action_shared" to "ਸਾਂਝਾ",
            "stats_title" to "ਵੌਲਟ ਅੰਕੜੇ",
            "stat_total_recordings" to "ਕੁੱਲ ਰਿਕਾਰਡਿੰਗਜ਼",
            "stat_total_time" to "ਸਮਾਂ (ਘੰਟੇ)",
            "dialog_rename_title" to "ਸੈਸ਼ਨ ਦਾ ਨਾਮ ਬਦਲੋ",
            "dialog_rename_field" to "ਸਿਰਲੇਖ",
            "dialog_btn_rename" to "ਨਾਮ ਬਦਲੋ",
            "dialog_btn_cancel" to "ਰੱਦ ਕਰੋ",
            "play_speed" to "ਗਤੀ",
            "play_share" to "ਸਾਂਝਾ ਕਰੋ",
            "play_favorite" to "ਮਨਪਸੰਦ",
            "play_copy" to "ਕਾਪੀ ਕਰੋ",
            "play_delete" to "ਮਿਟਾਓ",
            "play_repeat_mode" to "ਦੁਹਰਾਓ ਮੋਡ",
            "play_repeat_off" to "ਬੰਦ",
            "play_repeat_one" to "ਇੱਕ ਦੁਹਰਾਓ",
            "play_repeat_starred" to "ਮਨਪਸੰਦ ਦੁਹਰਾਓ",
            "play_repeat_all" to "ਸਾਰੇ ਦੁਹਰਾਓ",
            "rec_status_active" to "ਸੁਰੱਖਿਅਤ ਰਿਕਾਰਡਿੰਗ ਚਾਲੂ ਹੈ",
            "rec_status_paused" to "ਰਿਕਾਰਡਿੰਗ ਰੁਕੀ ਹੋਈ ਹੈ",
            "rec_save_title_label" to "ਕਸਟਮ ਸਿਰਲੇਖ (ਵਿਕਲਪਿਕ)",
            "rec_save_tag_label" to "ਸ਼੍ਰੇਣੀ ਟੈਗ",
            "rec_btn_save" to "ਸੈਸ਼ਨ ਸੁਰੱਖਿਅਤ ਕਰੋ",
            "rec_btn_cancel" to "ਰੱਦ ਕਰੋ",
            "rec_btn_pause" to "ਰੋਕੋ",
            "rec_btn_resume" to "ਸ਼ੁਰੂ ਕਰੋ",
            "bookmark_toast" to "ਮੌਜੂਦਾ ਸਮੇਂ 'ਤੇ ਬੁੱਕਮਾਰਕ ਜੋੜਿਆ ਗਿਆ!",
            "copylink_toast" to "ਸੁਰੱਖਿਅਤ ਲਿੰਕ ਤਿਆਰ ਕੀਤਾ ਗਿਆ। ਸ਼ੇਅਰ ਵਿਕਲਪ ਖੋਲ੍ਹ ਰਿਹਾ ਹੈ...",
            "settings_language" to "ਐਪ ਭਾਸ਼ਾ",
            "tag_meeting" to "ਮੀਟਿੰਗ",
            "tag_interview" to "ਇੰਟਰਵਿਊ",
            "tag_memo" to "ਮੈਮੋ"
        )
    )

    fun translate(key: String): String {
        return translations[_currentLanguage.value]?.get(key) ?: key
    }
}

@Composable
fun translate(key: String): String {
    val lang by LanguageManager.currentLanguage.collectAsState()
    return remember(lang, key) { LanguageManager.translate(key) }
}
