package uk.co.brayner.socketcontrol;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;

public class Socket
{
  private static Socket theInstance = null;
  private Python python;
  private PyObject broadlink;
  private PyObject device;
  private boolean connected = false;

  private Socket ()
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

  public static Socket getInstance ()
  {
    if (theInstance == null)
      theInstance = new Socket();
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
}
