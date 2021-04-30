package uk.co.brayner.socketcontrol;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.preference.PreferenceManager;

public class MainActivity extends AppCompatActivity
{
  static String TAG = "MainActivity";

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    SharedPreferences prefs;

    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    BottomNavigationView navView = findViewById(R.id.nav_view);
    // Passing each menu ID as a set of Ids because each
    // menu should be considered as top level destinations.
    AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
            R.id.navigation_home, R.id.navigation_schedule, R.id.navigation_settings)
            .build();
    NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
    NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
    NavigationUI.setupWithNavController(navView, navController);

    prefs = PreferenceManager.getDefaultSharedPreferences(this);
    String host = prefs.getString("ip", "Diddly squat");

    WallSocket wallSocket = WallSocket.getInstance();
    if (wallSocket.connect(host))
      Log.d(TAG, "We're in!");
    else
      Log.d(TAG, "Oops, something went wrong");
  }
}