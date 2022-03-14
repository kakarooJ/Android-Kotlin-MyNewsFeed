package com.kakaroo.mynewsfeed

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.*

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            //카테고리 초기값(enable/disable)설정
            val serviceStartPref: SwitchPreferenceCompat? = findPreference("service_key")
            serviceStartPref?.isChecked?.let { setPreferenceEnableDisable(!it) }

            serviceStartPref?.setOnPreferenceClickListener {
                if (serviceStartPref?.isChecked) {
                    setPreferenceEnableDisable(false)
                } else {
                    setPreferenceEnableDisable(true)
                }
                true
            }

            val keywordPref: EditTextPreference? = findPreference("keyword_key")
            keywordPref?.setOnPreferenceChangeListener{ preference, newValue ->
                if(newValue == "") {
                    val pref = PreferenceManager.getDefaultSharedPreferences(this.context)
                    pref.edit().putString("keyword_key", "").apply()
                    Log.i(Common.MY_TAG, "키워드 입력값이 없습니다.")
                }
                true
            }

            val urlKeyPref: EditTextPreference? = findPreference("url_key")
            urlKeyPref?.setOnPreferenceChangeListener{ preference, newValue ->
                if(newValue == "") {
                    val pref = PreferenceManager.getDefaultSharedPreferences(this.context)
                    pref.edit().putString("url_key", Common.DEFAULT_URL).apply()
                    Log.i(Common.MY_TAG, "Default URL로 채워집니다")
                }
                true
            }
        }

        private fun setPreferenceEnableDisable(enabled: Boolean) {
            val keywordCategoryPref: PreferenceCategory? = findPreference("keyword_category_key")
            if (keywordCategoryPref != null) {
                keywordCategoryPref.isEnabled = enabled
            }
            val requestCategoryPref: PreferenceCategory? = findPreference("request_category_key")
            if (requestCategoryPref != null) {
                requestCategoryPref.isEnabled = enabled
            }
            val urlKeyPref: EditTextPreference? = findPreference("url_key")
            if (urlKeyPref != null) {
                urlKeyPref.isEnabled = enabled
            }
        }
    }
}