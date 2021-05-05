package uk.co.brayner.socketcontrol;

import android.content.Context;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Logger
{
  public static void log (Context context, String text)
  {
    try
    {
      File logFile = new File(context.getFilesDir(), "log.txt");
      if (!logFile.exists())
        logFile.createNewFile();

      Date d = Calendar.getInstance().getTime();
      DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.UK);

      Log.d ("Logger", df.format(d) + ":  " + text);

      //BufferedWriter for performance, true to set append to file flag
      BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
      buf.append(df.format(d) + ":  ");
      buf.append(text);
      buf.newLine();
      buf.close();
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
  }
}
