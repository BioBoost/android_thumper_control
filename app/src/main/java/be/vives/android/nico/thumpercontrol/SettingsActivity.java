package be.vives.android.nico.thumpercontrol;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

public class SettingsActivity extends PreferenceActivity {

    public static final String KEY_PREF_NODE_IP = "pref_key_node_server_ip";
    public static final String KEY_PREF_NODE_PORT = "pref_key_node_server_port";
    public static final String KEY_PREF_NEO_STRINGID = "pref_key_neopixel_controller_stringid";
    public static final String KEY_PREF_TREX_REFRESH_TIME = "pref_key_trex_controller_refresh";
    public static final String KEY_PREF_TREX_MAX_SPEED = "pref_key_trex_controller_max_speed";
    public static final String KEY_PREF_TREX_BATTERY_THRESHOLD = "pref_key_trex_controller_battery_voltage_threshold";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_settings);

        // Load the preferences from the xml file
        addPreferencesFromResource(R.xml.app_preferences);
    }

}
