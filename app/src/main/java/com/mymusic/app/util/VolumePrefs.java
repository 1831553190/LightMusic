package com.mymusic.app.util;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;

/**
 *  Configuration for the volume dialog + related policy.
 */
public class VolumePrefs {

    public static final String PREF_ENABLE_PROTOTYPE = "pref_enable_prototype";  // not persistent
    public static final String PREF_SHOW_ALARMS = "pref_show_alarms";
    public static final String PREF_SHOW_SYSTEM = "pref_show_system";
    public static final String PREF_SHOW_HEADERS = "pref_show_headers";
    public static final String PREF_SHOW_FAKE_REMOTE_1 = "pref_show_fake_remote_1";
    public static final String PREF_SHOW_FAKE_REMOTE_2 = "pref_show_fake_remote_2";
    public static final String PREF_ENABLE_AUTOMUTE = "pref_enable_automute";
    public static final String PREF_ENABLE_SILENT_MODE = "pref_enable_silent_mode";
    public static final String PREF_DEBUG_LOGGING = "pref_debug_logging";
    public static final String PREF_SEND_LOGS = "pref_send_logs";
    public static final String PREF_ADJUST_SYSTEM = "pref_adjust_system";
    public static final String PREF_ADJUST_VOICE_CALLS = "pref_adjust_voice_calls";
    public static final String PREF_ADJUST_BLUETOOTH_SCO = "pref_adjust_bluetooth_sco";
    public static final String PREF_ADJUST_MEDIA = "pref_adjust_media";
    public static final String PREF_ADJUST_ALARMS = "pref_adjust_alarms";
    public static final String PREF_ADJUST_NOTIFICATION = "pref_adjust_notification";

    public static final boolean DEFAULT_SHOW_HEADERS = true;
    public static final boolean DEFAULT_ENABLE_AUTOMUTE = true;
    public static final boolean DEFAULT_ENABLE_SILENT_MODE = true;

    public static void unregisterCallbacks(Context c, OnSharedPreferenceChangeListener listener) {
        prefs(c).unregisterOnSharedPreferenceChangeListener(listener);
    }

    public static void registerCallbacks(Context c, OnSharedPreferenceChangeListener listener) {
        prefs(c).registerOnSharedPreferenceChangeListener(listener);
    }

    private static SharedPreferences prefs(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static boolean get(Context context, String key, boolean def) {
        return prefs(context).getBoolean(key, def);
    }
}