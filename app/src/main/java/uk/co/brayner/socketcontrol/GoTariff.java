package uk.co.brayner.socketcontrol;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Calendar;
import java.util.List;
import java.util.function.BiConsumer;

import androidx.preference.PreferenceManager;
import uk.co.brayner.socketcontrol.octopus.Rates;

import static java.util.Calendar.DATE;
import static java.util.Calendar.HOUR;

public class GoTariff implements Tariff
{
  private Context context;
  private SharedPreferences prefs;

  //---------------------------------------------------------------------------

  public GoTariff (Context context)
  {
    this.context = context;
    prefs = PreferenceManager.getDefaultSharedPreferences(context);
  }

  //---------------------------------------------------------------------------

  @Override
  public void getRates (Calendar from, BiConsumer<Context, List<Rates.Result>> callback)
  {
    callback.accept(context, null);
  }

  //---------------------------------------------------------------------------

  @Override
  public boolean isLowRate (Calendar thisTime, int price, List<Rates.Result> rates)
  {
    // Get today's GO low rate starting time

    String goStart = prefs.getString("go_start_time", "00:30");
    String[] parts = goStart.split(":");
    int hour = Integer.parseInt(parts[0]);
    int minute = Integer.parseInt(parts[1]);

    // Set a calendar to this exact time

    Calendar startTime = (Calendar) thisTime.clone();
    startTime.set(Calendar.HOUR_OF_DAY, hour);
    startTime.set(Calendar.MINUTE, minute);
    startTime.set(Calendar.SECOND, 0);
    startTime.set(Calendar.MILLISECOND, 0);

    // If we are before today's start time, try yesterday's

    if (thisTime.before(startTime))
      startTime.add(DATE, -1);

    // Now we're definitely on or after the last start time
    // Find end time following the start time

    String goDuration = prefs.getString("go_duration", "4");
    int duration = Integer.parseInt(goDuration);
    Calendar endTime = (Calendar) startTime.clone();
    endTime.add(HOUR, duration);

    // If we're before the end time, we're in the low rate

    return thisTime.before(endTime);
  }
}
