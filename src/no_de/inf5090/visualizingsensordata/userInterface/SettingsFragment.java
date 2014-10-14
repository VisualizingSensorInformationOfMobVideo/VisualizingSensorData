package no_de.inf5090.visualizingsensordata.userInterface;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import no_de.inf5090.visualizingsensordata.R;

/**
 * Preferences for our application
 */
public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // load preferences from XML
        addPreferencesFromResource(R.xml.preferences);
    }
}
