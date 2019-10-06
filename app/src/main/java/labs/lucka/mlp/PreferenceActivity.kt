package labs.lucka.mlp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.MenuItem
import androidx.annotation.Keep
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat

class PreferenceActivity :
    AppCompatActivity(), PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

    class PreferenceFragmentMain : PreferenceFragmentCompat() {

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.preference_main, rootKey)
        }

    }

    @Keep
    class PreferenceFragmentAbout : PreferenceFragmentCompat() {

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.preference_about, rootKey)

            findPreference<Preference>(getString(R.string.pref_about_summary_version_key))
                ?.summary =
                getString(
                    R.string.pref_about_summary_version_summary,
                    BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE
                )

        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_preference)
        if (savedInstanceState == null) {
            val preferenceFragment = PreferenceFragmentMain()
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.preference_frame, preferenceFragment)
                .commit()
        }
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

    override fun onPreferenceStartFragment(
        caller: PreferenceFragmentCompat?, pref: Preference?
    ): Boolean {
        if (caller == null || pref == null) return false

        supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                android.R.animator.fade_in, android.R.animator.fade_out,
                android.R.animator.fade_in, android.R.animator.fade_out
            )
            .replace(
                R.id.preference_frame,
                supportFragmentManager.fragmentFactory.instantiate(classLoader, pref.fragment)
                    .apply {
                        arguments = pref.extras
                        setTargetFragment(caller, 0)
                    }
            )
            .addToBackStack(null)
            .commit()

        return true
    }

}