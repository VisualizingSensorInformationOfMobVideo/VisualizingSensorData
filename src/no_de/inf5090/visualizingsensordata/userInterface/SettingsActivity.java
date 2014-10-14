package no_de.inf5090.visualizingsensordata.userInterface;

import android.app.Activity;
import android.os.Bundle;
import no_de.inf5090.visualizingsensordata.userInterface.SettingsFragment;

/**
 *
 */
public class SettingsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // show the fragment
        getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
    }
}
