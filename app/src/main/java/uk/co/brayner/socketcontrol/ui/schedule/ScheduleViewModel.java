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
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.preference.PreferenceManager;
import uk.co.brayner.socketcontrol.Logger;
import uk.co.brayner.socketcontrol.MainActivity;
import uk.co.brayner.socketcontrol.R;
import uk.co.brayner.socketcontrol.Scheduler;
import uk.co.brayner.socketcontrol.Tariff;
import uk.co.brayner.socketcontrol.WallSocket;
import uk.co.brayner.socketcontrol.octopus.Rates;

import static java.util.Calendar.MILLISECOND;
import static java.util.Calendar.MINUTE;
import static java.util.Calendar.SECOND;

public class ScheduleViewModel extends AndroidViewModel
{
  static String TAG = "HomeViewModel";
  static Float SUPPLY_VOLTAGE = 230.0F;
  static int MILLISECONDS_IN_MINUTE = 60000;

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

  Tariff tariff;

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
    // Starts the charging schedule.  Start by retrieving the current rates
    // if necessary, then passing control to handleCharging

    tariff = Tariff.getTariff(getApplication());
    assert tariff != null;
    tariff.getRates(Calendar.getInstance(), this::handleCharging);
  }

  //---------------------------------------------------------------------------

  private void handleCharging(Context context, List<Rates.Result> rates)
  {
    float chargeToAdd = 0.0F;   // How much charge to add
    float power;                // Power supplied by charger

    Logger.log(context,"--------------------------------------------------");

    try
    {
      // First work out how much charge to add

      String sMpkwh = prefs.getString("mpkwh", "2");
      String sCurrent = prefs.getString("current", "10");
      String sCapacity = prefs.getString("capacity", "60");
      int duration;

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

      Calendar currentTime = Calendar.getInstance();
      Calendar finishTime;

      if (chargeWhen.getValue() == ChargeWhen.ANY_TIME)
      {
        // OK, we just go flat out regardless of tariff rates

        chargerOn(getApplication(), true);
        finishTime = (Calendar) currentTime.clone();
        finishTime.add(MINUTE, remainingChargeTime);
        remainingChargeTime = 0;
      }
      else
      {
        int price = prefs.getInt("PriceLimit", 5);

        // Find start and end time in current half-hour timeslot

        Calendar startTime = (Calendar) currentTime.clone();
        startTime.set(SECOND, 0);
        startTime.set(MILLISECOND, 0);
        if (startTime.get(MINUTE) > 30)
          startTime.set(MINUTE, 30);
        else
          startTime.set(MINUTE, 0);
        Calendar endTime = (Calendar) startTime.clone();
        endTime.add(MINUTE, 30);

        int remainingTimeslotTime = (int) (endTime.getTimeInMillis() - currentTime.getTimeInMillis()) / MILLISECONDS_IN_MINUTE;

        if (tariff.isLowRate(startTime, price, rates))
        {
          if (remainingChargeTime > remainingTimeslotTime)
          {
            finishTime = endTime;
            remainingChargeTime -= remainingTimeslotTime;
          }
          else
          {
            finishTime = (Calendar) currentTime.clone();
            finishTime.add(MINUTE, remainingChargeTime);
            remainingChargeTime = 0;
          }
          chargerOn(getApplication(), true);
        }
        else
        {
          finishTime = endTime;
        }
      }

      state.setValue(State.CHARGING);
      startTimer (getApplication(), finishTime, remainingChargeTime);

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

  static void startTimer (Context context, Calendar expiryTime, int remainingChargeTime)
  {
    // Start timer for the given duration in minutes

    // Update UI if present

    if (theInstance != null)
      theInstance.nextTime.setValue(expiryTime.getTime());

    // Persist the next event details

    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
    SharedPreferences.Editor editor = prefs.edit();
    editor.putInt("State", State.CHARGING.ordinal());
    editor.putInt("RemainingChargeTime", remainingChargeTime);
    editor.putLong("ExpiryTime", expiryTime.getTimeInMillis());
    editor.apply();

    // Start an alarm to wake the app at given time

    AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    Intent intent = new Intent(context, Scheduler.class);
    PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
    am.setExact(AlarmManager.RTC_WAKEUP, expiryTime.getTimeInMillis(), pendingIntent);
  }

  //---------------------------------------------------------------------------

  public static void onTimer(Context context)
  {
    // Progresses the charging schedule.  Start by retrieving the current rates
    // if necessary, then passing control to handleTimer

    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
    long ms = prefs.getLong("ExpiryTime", 0);
    Calendar timeslotTime = Calendar.getInstance();
    timeslotTime.setTimeInMillis(ms);

    Tariff tariff = Tariff.getTariff(context);
    assert tariff != null;
    tariff.getRates(timeslotTime, ScheduleViewModel::handleTimer);
  }

  //---------------------------------------------------------------------------

  public static void handleTimer(Context context, List<Rates.Result> rates)
  {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

    int remainingChargeTime = prefs.getInt("RemainingChargeTime", 0);
    long ms = prefs.getLong("ExpiryTime", 0);

    Date d = Calendar.getInstance().getTime();
    DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.UK);
    Logger.log(context, "Timer fired at " + df.format(d));
    Logger.log(context, "Remaining charge time = " + remainingChargeTime);

    Calendar timeslotTime = Calendar.getInstance();
    timeslotTime.setTimeInMillis(ms);

    if (remainingChargeTime > 0)
    {
      Tariff tariff = Tariff.getTariff(context);
      assert tariff != null;
      int price = prefs.getInt("PriceLimit", 5);

      if (tariff.isLowRate(timeslotTime, price, rates))
      {
        if (remainingChargeTime > 30)
        {
          timeslotTime.add(MINUTE, 30);
          remainingChargeTime -= 30;
        }
        else
        {
          timeslotTime.add(MINUTE, remainingChargeTime);
          remainingChargeTime = 0;
        }
        chargerOn(context, true);
      }
      else
      {
        timeslotTime.add(MINUTE, 30);
        chargerOn(context, false);
      }

      startTimer (context, timeslotTime, remainingChargeTime);
    }
    else
    {
      stopCharging(context);

      // Send a notification to indicate that charging is complete

      Intent intent = new Intent(context, MainActivity.class);
      intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
      PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
      NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
              .setSmallIcon(R.drawable.ic_charge_complete)
              .setContentTitle(context.getString(R.string.app_name))
              .setContentText(context.getString(R.string.charging_complete))
              .setPriority(NotificationCompat.PRIORITY_DEFAULT)
              .setContentIntent(pendingIntent)
              .setAutoCancel(true);

      NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
      notificationManager.notify(0, builder.build());
    }
  }
}