package uk.co.brayner.socketcontrol;

import android.os.Bundle;
import android.util.Log;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

public class MainActivity extends AppCompatActivity
{
  static String TAG = "MainActivity";

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    BottomNavigationView navView = findViewById(R.id.nav_view);
    // Passing each menu ID as a set of Ids because each
    // menu should be considered as top level destinations.
    AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
            R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
            .build();
    NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
    NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
    NavigationUI.setupWithNavController(navView, navController);

    try
    {
      Python python = Python.getInstance();
      PyObject broadlink = python.getModule("broadlink");

      // Just to check we are talking to the Python module OK

      PyObject obj = broadlink.callAttr("test");
      Log.d(TAG, "test result: " + obj.toString());

      // This is it: retrieve list of compatible devices on network

      obj = broadlink.callAttr("discover");
      Log.d(TAG, "discover result: " + obj.toString());

      // TODO - retrieve details of the BG800 / BG900 smart wall socket
    }
    catch (Exception ex)
    {
      Log.d(TAG, "Something went wrong: " + ex.toString());
    }
  }

}