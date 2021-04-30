package uk.co.brayner.socketcontrol;

import android.util.Log;

import com.chaquo.python.Kwarg;
import com.chaquo.python.PyObject;
import com.chaquo.python.Python;


public class WallSocket
{
  static String TAG = "Socket";

  private static WallSocket theInstance = null;
  private Python python;
  private PyObject broadlink;
  private PyObject device;
  private boolean connected = false;

  public enum ID
  {
    BOTH,
    LEFT,
    RIGHT
  }

  public class State
  {
    public boolean powerBoth;
    public boolean powerLeft;
    public boolean powerRight;
    int maxWorkTimeBoth;
    int maxWorkTimeLeft;
    int maxWorkTimeRight;
    int brightness;
  }

  private WallSocket()
  {
    try
    {
      // Create instance of Python and get reference to Broadlink module

      python = Python.getInstance();
      broadlink = python.getModule("broadlink");
    }
    catch (Exception ex)
    {
    }
  }

  public static WallSocket getInstance ()
  {
    if (theInstance == null)
      theInstance = new WallSocket();
    return theInstance;
  }

  public boolean connect (String host)
  {
    if (!connected)
    {
      try
      {
        // Get reference to our device using the host IP address

        device = broadlink.callAttr("hello", host);

        // Request authorization.  If successful, records a token
        // to be used in subsequent commands

        device.callAttr("auth");
        connected = true;
      } catch (Exception ex)
      {
        connected = false;
      }
    }
    return true;
  }

  public State powerOn (ID port, boolean on)
  {
    String portString = null;
    int onInt;

    if (connected)
    {
      switch (port)
      {
        case BOTH:
          portString = "pwr";
          break;
        case LEFT:
          portString = "pwr1";
          break;
        case RIGHT:
          portString = "pwr2";
          break;
      }

      if (on)
        onInt = 1;
      else
        onInt = 0;

      PyObject map = device.callAttr("set_state", new Kwarg(portString, onInt));
      return unpackState(map);
    }
    return null;
  }

  public State setMaxWorkTime (ID port, int time)
  {
    String portString = null;

    if (connected)
    {
      switch (port)
      {
        case BOTH:
          portString = "maxworktime";
          break;
        case LEFT:
          portString = "maxworktime1";
          break;
        case RIGHT:
          portString = "maxworktime2";
          break;
      }

      PyObject map = device.callAttr("set_state", new Kwarg(portString, time));
      return unpackState(map);
    }
    return null;
  }

  public void setBrightness (int level)
  {
    if (connected)
      device.callAttr("set_state", new Kwarg ("idcbrightness", level));
  }

  public State getState ()
  {
    if (connected)
    {
      try
      {
        PyObject map = device.callAttr("get_state");
        return unpackState(map);
      } catch (Exception ex)
      {
        Log.d(TAG, ex.toString());
      }
    }
    return null;
  }

  private State unpackState (PyObject map)
  {
    State state = new State ();

    state.powerBoth = (map.asMap().get("pwr").toInt() == 1);
    state.powerLeft = (map.asMap().get("pwr1").toInt() == 1);
    state.powerRight = (map.asMap().get("pwr2").toInt() == 1);
    state.maxWorkTimeBoth = map.asMap().get("maxworktime").toInt();
    state.maxWorkTimeLeft = map.asMap().get("maxworktime1").toInt();
    state.maxWorkTimeRight = map.asMap().get("maxworktime2").toInt();
    state.brightness = map.asMap().get("idcbrightness").toInt();
    return state;
  }
}
