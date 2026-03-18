package com.github.wrager.sbguserscripts.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.github.wrager.sbguserscripts.BuildConfig
import com.github.wrager.sbguserscripts.R

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        findPreference<Preference>("app_version")?.summary =
            getString(R.string.settings_version_value, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE)

        findPreference<Preference>("report_bug")?.setOnPreferenceClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(ISSUES_URL)))
            true
        }
    }

    companion object {
        private const val ISSUES_URL = "https://github.com/wrager/sbg-userscripts-apk/issues"
    }
}
