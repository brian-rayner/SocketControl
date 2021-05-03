//---------------------------------------------------------------------------
//
// ScheduleViewModel
//
// Holds the logic for managing a charging schedule.
//
// Once a charging schedule is started, it is managed by a series of
// alarms set to trigger when the charger is to be turned on or off.
// The alarms are handled by a broadcast receiver (defined in Scheduler.java)
// and can be triggered even when the app is closed and the Android
// device is asleep.
//
// This class contains several static methods that can be invoked by
// the broadcast receiver in the absence of an active user interface.
// However, if the UI is present, it is updated to reflect the progress
// of the charging schedule.
//
//---------------------------------------------------------------------------

package uk.co.brayner.socketcontrol.ui.schedule;

import android.app.AlarmManager;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.preference.PreferenceManager;
import uk.co.brayner.socketcontrol.Logger;
import uk.co.brayner.socketcontrol.R;
import uk.co.brayner.socketcontrol.Scheduler;
import uk.co.brayner.socketcontrol.WallSocket;

import static java.util.Calendar.DATE;
import static java.util.Calendar.HOUR;
import static java.util.Calendar.MINUTE;

public class ScheduleViewModel extends AndroidViewModel
{
  static String TAG = "HomeViewModel";
  static Float SUPPLY_VOLTAGE = 230.0F;
  static int MILLISECONDS_IN_MINUTE = 60000;
  static int TIME_MARGIN = 2;
  static String CHANNEL_ID = "charging_complete_notification";

  public enum State
  {
    NOT_STARTED,
    CHARGING,
    CHARGING_COMPLETE
  }

  public enum ChargeFor
  {
    TO_FULL,
    ADD_MILES,
    ADD_CHARGE
  }

  public enum ChargeWhen
  {
    ANY_TIME,
    MAX_PRICE,
    OFF_PEAK
  }

  enum Action
  {
    CHARGER_FINISH,
    CHARGER_ON,
    CHARGER_OFF
  }

  private final SharedPreferences prefs;
  private final MutableLiveData<State> state;
  private final MutableLiveData<ChargeFor> chargeFor;
  private final MutableLiveData<ChargeWhen> chargeWhen;
  private final MutableLiveData<Boolean> chargerOn;
  private final MutableLiveData<Integer> milesToAdd;
  private final MutableLiveData<Integer> chargeToAdd;
  private final MutableLiveData<Integer> priceLimit;
  private final MutableLiveData<Date> nextTime;
  private static ScheduleViewModel theInstance = null;

  //---------------------------------------------------------------------------

  public ScheduleViewModel(@NonNull Application application)
  {
    super(application);
    theInstance = this;
    prefs = PreferenceManager.getDefaultSharedPreferences(application);

    // Set up all observable values and initialize from preferences

    state = new MutableLiveData<>();
    state.setValue(State.values()[prefs.getInt("State", 0)]);

    chargerOn = new MutableLiveData<>();
    chargerOn.setValue(prefs.getBoolean("ChargerOn", false));

    chargeFor = new MutableLiveData<>();
    chargeFor.setValue(ChargeFor.values()[prefs.getInt("ChargeFor", 0)]);

    chargeWhen = new MutableLiveData<>();
    chargeWhen.setValue(ChargeWhen.values()[prefs.getInt("ChargeWhen", 0)]);

    milesToAdd = new MutableLiveData<>();
    milesToAdd.setValue(prefs.getInt("MilesToAdd", 20));

    chargeToAdd = new MutableLiveData<>();
    chargeToAdd.setValue(prefs.getInt("ChargeToAdd", 10));

    priceLimit = new MutableLiveData<>();
    priceLimit.setValue(prefs.getInt("PriceLimit", 5));

    nextTime = new MutableLiveData<>();
    String sTime = prefs.getString("NextTime", "");
    DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.UK);
    Date d = null;
    try
    {
      d = df.parse (sTime);
    } catch (ParseException ignored) { }
    nextTime.setValue(d);

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
    {
      CharSequence name = application.getString(R.string.app_name);
      String description = application.getString(R.string.charging_complete);
      int importance = NotificationManager.IMPORTANCE_DEFAULT;
      NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
      channel.setDescription(description);
      // Register the channel with the system
      NotificationManager notificationManager = application.getSystemService(NotificationManager.class);
      notificationManager.createNotificationChannel(channel);
    }
  }

  //---------------------------------------------------------------------------

  // Functions for accessing observable values

  public LiveData<State> getState()  { return state;  }
  public LiveData<Boolean> getChargerOn()  { return chargerOn;  }
  public LiveData<ChargeFor> getChargeFor() { return chargeFor; }
  public LiveData<ChargeWhen> getChargeWhen() { return chargeWhen; }
  public LiveData<Integer> getMilesToAdd()  { return milesToAdd;  }
  public LiveData<Integer> getChargeToAdd()  { return chargeToAdd;  }
  public LiveData<Integer> getPriceLimit()  { return priceLimit;  }
  public LiveData<Date> getNextTime()  { return nextTime;  }

  //---------------------------------------------------------------------------

  public void setChargeFor (ChargeFor chargeFor)
  {
    // Updates the "charge for" option

    this.chargeFor.setValue(chargeFor);
    SharedPreferences.Editor editor = prefs.edit();
    editor.putInt("ChargeFor", chargeFor.ordinal());
    editor.apply();
  }

  //---------------------------------------------------------------------------

  public void setChargeWhen (ChargeWhen chargeWhen)
  {
    // Updates the "charge when" option

    this.chargeWhen.setValue(chargeWhen);
    SharedPreferences.Editor editor = prefs.edit();
    editor.putInt("ChargeWhen", chargeWhen.ordinal());
    editor.apply();
  }

  //---------------------------------------------------------------------------

  public void setToAdd (Integer toAdd)
  {
    // Updates the miles or charge to add

    SharedPreferences.Editor editor = prefs.edit();
    switch (chargeFor.getValue())
    {
      case ADD_MILES:
        milesToAdd.setValue(toAdd);
        editor.putInt("MilesToAdd", toAdd);
        break;

      case ADD_CHARGE:
        chargeToAdd.setValue(toAdd);
        editor.putInt("ChargeToAdd", toAdd);
        break;

      default:
        break;
    }
    editor.apply();
  }

  //---------------------------------------------------------------------------

  public void setPriceLimit (Integer limit)
  {
    // Updates the price limit for Agile tariff

    if (chargeWhen.getValue() == ChargeWhen.MAX_PRICE)
    {
      priceLimit.setValue(limit);
      SharedPreferences.Editor editor = prefs.edit();
      editor.putInt("PriceLimit", limit);
      editor.apply();
    }
  }

  //---------------------------------------------------------------------------

  public void startStop ()
  {
    // Toggles start/stop charging

    if (state.getValue() == State.CHARGING)
      stopCharging (getApplication());
    else
      startCharging ();
  }

  //---------------------------------------------------------------------------

  private static void chargerOn (Context context, Boolean on)
  {
    // Find which outlet is being used

    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
    String sOutlet = prefs.getString("outlet", "Left");
    WallSocket.ID outlet = sOutlet.equals("Right") ? WallSocket.ID.RIGHT : WallSocket.ID.LEFT;

    // Turn the charger on or off

    String host = prefs.getString("ip", "");
    WallSocket wallSocket = WallSocket.getInstance(host);
    WallSocket.State state = wallSocket.powerOn(outlet, on);
    if (theInstance != null)
      theInstance.chargerOn.setValue(sOutlet.equals("Right") ? state.powerRight : state.powerLeft);

    // Persist the change in state

    SharedPreferences.Editor editor = prefs.edit();
    editor.putBoolean("ChargerOn", on);
    editor.apply();

    Logger.log(context,"Charger " + (on ? "ON" : "OFF"));
  }

  //---------------------------------------------------------------------------

  private void startCharging ()
  {
    // Starts the charging schedule

    float chargeToAdd = 0.0F;   // How much charge to add
    float power;                // Power supplied by charger

    Logger.log(getApplication(),"--------------------------------------------------");

    try
    {
      // First work out how much charge to add

      String sMpkwh = prefs.getString("mpkwh", "2");
      String sCurrent = prefs.getString("current", "10");
      String sCapacity = prefs.getString("capacity", "60");

      switch (chargeFor.getValue())
      {
        case TO_FULL:

          chargeToAdd = Float.parseFloat(sCapacity);
          break;

        case ADD_CHARGE:

          chargeToAdd = Float.valueOf(this.chargeToAdd.getValue());
          break;

        case ADD_MILES:

          chargeToAdd = this.milesToAdd.getValue() / Float.parseFloat(sMpkwh);
          break;
      }

      if (chargeToAdd <= 0)
        return;

      // Calculate time needed to add charge (in minutes)

      power = Float.parseFloat(sCurrent) * SUPPLY_VOLTAGE / 1000.0F;
      int remainingChargeTime = (int) (60 * chargeToAdd / power);

      switch (chargeWhen.getValue())
      {
        case ANY_TIME:

          // OK, we just go flat out regardless of tariff rates

          chargerOn(getApplication(), true);
          Calendar finishTime = Calendar.getInstance();
          finishTime.add(MINUTE, remainingChargeTime);
          startTimer(getApplication(), finishTime, Action.CHARGER_FINISH, 0);
          break;

        case OFF_PEAK:

          // Get current time and start time of today's off-peak period

          Calendar currentTime = Calendar.getInstance();
          Calendar startTime = findOffPeakStartTime(getApplication());

          if (currentTime.before(startTime))
          {
            // We are currently before the start of off-peak: Wait for start time.

            startTimer (getApplication(), startTime, Action.CHARGER_ON, remainingChargeTime);
          }
          else
          {
            // We are after start time; calculate off-peak end time

            String goDuration = prefs.getString("go_duration", "4");
            int duration = Integer.parseInt(goDuration);
            Calendar endTime = (Calendar) startTime.clone();
            endTime.add(HOUR, duration);

            if (currentTime.before(endTime))
            {
              // We're within the off-peak period.  Can start charging immediately.
              // Find time to end of off-peak

              int remainingOffPeakTime = (int) (endTime.getTimeInMillis() - currentTime.getTimeInMillis()) / MILLISECONDS_IN_MINUTE;
              if (remainingChargeTime < remainingOffPeakTime)
              {
                // We can complete the charge during this off-peak period

                finishTime = startTime;
                finishTime.add(MINUTE, remainingChargeTime);
                startTimer(getApplication(), finishTime, Action.CHARGER_FINISH, 0);
              }
              else
              {
                // Need to charge for the whole off-peak period (less margin for schedule delays)

                remainingChargeTime -= remainingOffPeakTime;
                endTime.add(MINUTE, -TIME_MARGIN);
                startTimer(getApplication(), endTime, Action.CHARGER_OFF, remainingChargeTime);
              }

              // Either way we can start charging now

              chargerOn(getApplication(), true);
            }
            else
            {
              // We're after the off-peak period for today.  Need to wait for the
              // start of the next period

              startTime.add(DATE, 1);
              startTimer(getApplication(), startTime, Action.CHARGER_ON, remainingChargeTime);
            }
          }
          break;

        default:

          Toast.makeText(getApplication(), "Agile tariff not yet supported", Toast.LENGTH_LONG).show();
          return;
      }

      // Update state and persist

      state.setValue(State.CHARGING);
      SharedPreferences.Editor editor = prefs.edit();
      editor.putInt("State", State.CHARGING.ordinal());
      editor.apply();

      Logger.log(getApplication(),"Charging started");
    }
    catch (Exception ex)
    {
      Toast.makeText(getApplication(), "Error in settings value.  Please check all entries on Settings page", Toast.LENGTH_LONG).show();
    }
  }

  //---------------------------------------------------------------------------

  private static void stopCharging (Context context)
  {
    // Stops all charging activity and turns off the charger

    chargerOn(context, false);
    if (theInstance != null)
      theInstance.state.setValue(State.CHARGING_COMPLETE);

    // Persist the change in state

    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
    SharedPreferences.Editor editor = prefs.edit();
    editor.putInt("State", State.CHARGING_COMPLETE.ordinal());
    editor.apply();

    // Cancel pending alarms

    AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    Intent intent = new Intent(context, Scheduler.class);
    am.cancel(PendingIntent.getBroadcast(context, 0, intent, 0));

    Logger.log(context,"Charging complete");
  }

  //---------------------------------------------------------------------------

  static void startTimer (Context context, Calendar expiryTime, Action action, int remainingChargeTime)
  {
    // Start timer for the given duration in minutes

    DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.UK);

    // Get time of next event

    Date d = expiryTime.getTime();

    // Update UI if present

    if (theInstance != null)
      theInstance.nextTime.setValue(d);

    // Persist the next event details

    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
    SharedPreferences.Editor editor = prefs.edit();
    editor.putString("NextTime", df.format(d));
    editor.putInt("Action", action.ordinal());
    editor.putInt("RemainingChargeTime", remainingChargeTime);
    editor.apply();

    // Start an alarm to wake the app when next scheduled event is due

    AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    Intent intent = new Intent(context, Scheduler.class);
    PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
    am.set(AlarmManager.RTC_WAKEUP, expiryTime.getTimeInMillis(), pendingIntent);

    // Log to file

    Logger.log(context,"Starting timer set for " + df.format(d));
  }

  //---------------------------------------------------------------------------

  public static void onTimer(Context context)
  {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

    int remainingChargeTime = prefs.getInt("RemainingChargeTime", 0);
    int ordinal = prefs.getInt("Action", 0);
    Action action = Action.values()[ordinal];

    String goDuration = prefs.getString("go_duration", "4");
    int duration = Integer.parseInt(goDuration);
    Date d = Calendar.getInstance().getTime();
    DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.UK);
    Logger.log(context, "Timer fired at " + df.format(d));
    Logger.log(context, "Remaining charge time = " + remainingChargeTime);

    Calendar startTime = findOffPeakStartTime(context);

    switch (action)
    {
      case CHARGER_FINISH:

        stopCharging(context);

        // Send a notification to indicate that charging is complete

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_charge_complete)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(context.getString(R.string.charging_complete))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(0, builder.build());

        break;

      case CHARGER_ON:

        int remainingOffPeakTime = duration * 60;
        if (remainingChargeTime < remainingOffPeakTime)
        {
          // We can complete the charge during this off-peak period

          startTime.add(MINUTE, remainingChargeTime);
          startTimer(context, startTime, Action.CHARGER_FINISH, 0);
        }
        else
        {
          // Need to charge for the whole off-peak period

          startTime.add(MINUTE, remainingOffPeakTime);
          remainingChargeTime -= remainingOffPeakTime;
          startTimer(context, startTime, Action.CHARGER_OFF, remainingChargeTime);
        }
        // Either way we can start charging now

        chargerOn(context, true);
        break;

      case CHARGER_OFF:

        // Turn charger off and wait till next off-peak period

        Calendar now = Calendar.getInstance();
        if (now.after(startTime))
          startTime.add(DATE, 1);
        startTimer(context, startTime, Action.CHARGER_ON, remainingChargeTime);
        chargerOn(context, false);
        break;
    }
  }

  //---------------------------------------------------------------------------

  static Calendar findOffPeakStartTime (Context context)
  {
    // Get today's start time

    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
    String goStart = prefs.getString("go_start_time", "00:30");
    String[] parts = goStart.split(":");
    int hour = Integer.parseInt(parts[0]);
    int minute = Integer.parseInt(parts[1]);

    Calendar startTime = Calendar.getInstance();
    startTime.set(Calendar.HOUR_OF_DAY, hour);
    startTime.set(Calendar.MINUTE, minute);
    return startTime;
  }
}