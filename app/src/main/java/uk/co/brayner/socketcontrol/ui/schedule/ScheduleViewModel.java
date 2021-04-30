package uk.co.brayner.socketcontrol.ui.schedule;

import android.app.Application;
import android.content.SharedPreferences;
import android.os.Handler;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Date;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.preference.PreferenceManager;
import uk.co.brayner.socketcontrol.WallSocket;

public class ScheduleViewModel extends AndroidViewModel
{
  static String TAG = "HomeViewModel";
  static Float SUPPLY_VOLTAGE = 230.0F;

  public enum ChargeFor
  {
    TO_FULL,
    ADD_MILES,
    ADD_CHARGE
  }

  public enum ChargeWhen
  {
    ANY_TIME,
    MAX_PRICE
  }

  private SharedPreferences prefs;
  private MutableLiveData<ChargeFor> chargeFor;
  private MutableLiveData<ChargeWhen> chargeWhen;
  private MutableLiveData<Boolean> charging;
  private MutableLiveData<Boolean> chargerOn;
  private MutableLiveData<Integer> milesToAdd;
  private MutableLiveData<Integer> chargeToAdd;
  private MutableLiveData<Integer> priceLimit;
  private MutableLiveData<Date> nextTime;

  Float remainingTime = 0.0F;

  Handler timerHandler = new Handler();
  Runnable timerRunnable = new Runnable()
  {
    @Override
    public void run()
    {
      stopCharging();
    }
  };

  public ScheduleViewModel(@NonNull Application application)
  {
    super(application);
    prefs = PreferenceManager.getDefaultSharedPreferences(application);

    charging = new MutableLiveData<>();
    charging.setValue(false);

    chargerOn = new MutableLiveData<>();
    chargerOn.setValue(false);

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
  }

  public LiveData<Boolean> getCharging()  { return charging;  }
  public LiveData<Boolean> getChargerOn()  { return chargerOn;  }
  public LiveData<ChargeFor> getChargeFor() { return chargeFor; }
  public LiveData<ChargeWhen> getChargeWhen() { return chargeWhen; }
  public LiveData<Integer> getMilesToAdd()  { return milesToAdd;  }
  public LiveData<Integer> getChargeToAdd()  { return chargeToAdd;  }
  public LiveData<Integer> getPriceLimit()  { return priceLimit;  }
  public LiveData<Date> getNextTime()  { return nextTime;  }

  public void setChargeFor (ChargeFor chargeFor)
  {
    this.chargeFor.setValue(chargeFor);
    SharedPreferences.Editor editor = prefs.edit();
    editor.putInt("ChargeFor", chargeFor.ordinal());
    editor.apply();
  }

  public void setChargeWhen (ChargeWhen chargeWhen)
  {
    this.chargeWhen.setValue(chargeWhen);
    SharedPreferences.Editor editor = prefs.edit();
    editor.putInt("ChargeWhen", chargeWhen.ordinal());
    editor.apply();
  }

  public void setToAdd (Integer toAdd)
  {
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

  public void setPriceLimit (Integer limit)
  {
    if (chargeWhen.getValue() == ChargeWhen.MAX_PRICE)
    {
      priceLimit.setValue(limit);
      SharedPreferences.Editor editor = prefs.edit();
      editor.putInt("PriceLimit", limit);
      editor.apply();
    }
  }

  public void startStop ()
  {
    if (charging.getValue())
      stopCharging();
    else
      startCharging ();
  }

  private void stopCharging ()
  {
    WallSocket wallSocket = WallSocket.getInstance();
    WallSocket.State state;

    state = wallSocket.powerOn(WallSocket.ID.LEFT, false);
    chargerOn.setValue(state.powerLeft);
    charging.setValue(false);
    timerHandler.removeCallbacks(timerRunnable);
  }

  private void startCharging ()
  {
    Float chargeToAdd = 0.0F;   // How much charge to add
    Float power;                // Power supplied by charger
    WallSocket wallSocket = WallSocket.getInstance();
    WallSocket.State state;

    try
    {
      String sMpkwh = prefs.getString("mpkwh", "2");
      String sCurrent = prefs.getString("current", "10");
      String sCapacity = prefs.getString("capacity", "60");

      switch (chargeFor.getValue())
      {
        case TO_FULL:

          chargeToAdd = Float.valueOf(sCapacity);
          break;

        case ADD_CHARGE:

          chargeToAdd = Float.valueOf(this.chargeToAdd.getValue());
          break;

        case ADD_MILES:

          chargeToAdd = Float.valueOf(this.milesToAdd.getValue() / Float.valueOf(sMpkwh));
          break;
      }

      power = Float.valueOf(sCurrent) * SUPPLY_VOLTAGE / 1000.0F;
      remainingTime = chargeToAdd / power;

      state = wallSocket.powerOn(WallSocket.ID.LEFT, true);
      chargerOn.setValue(state.powerLeft);
      charging.setValue(true);

      long delay = (long) (remainingTime * 3600000);
      timerHandler.postDelayed(timerRunnable, delay);
    }
    catch (Exception ex)
    {
      Toast.makeText(getApplication(), "Error in settings value.  Please check all entries on Settings page", Toast.LENGTH_LONG);
    }

    if (chargeWhen.getValue() == ChargeWhen.ANY_TIME)
    {
      Calendar endTime = Calendar.getInstance();
      endTime.add(Calendar.MINUTE, Math.round(remainingTime * 60.0F));
      Date d = endTime.getTime();
      nextTime.setValue(d);
    }

  }
}