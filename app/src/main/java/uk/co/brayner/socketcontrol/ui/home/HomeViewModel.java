package uk.co.brayner.socketcontrol.ui.home;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;
import uk.co.brayner.socketcontrol.MainActivity;
import uk.co.brayner.socketcontrol.R;
import uk.co.brayner.socketcontrol.Socket;

public class HomeViewModel extends AndroidViewModel
{
  public enum ChargeFor
  {
    TO_FULL,
    ADD_MILES,
    ADD_KWH
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

  public HomeViewModel(@NonNull Application application)
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
  }

  public LiveData<Boolean> getCharging()  { return charging;  }
  public LiveData<Boolean> getChargerOn()  { return chargerOn;  }
  public LiveData<ChargeFor> getChargeFor() { return chargeFor; }
  public LiveData<ChargeWhen> getChargeWhen() { return chargeWhen; }

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

  public void startStop ()
  {
    Socket socket = Socket.getInstance();
    Socket.State state;

    if (charging.getValue())
    {
      state = socket.powerOn(Socket.ID.LEFT, false);
      chargerOn.setValue(state.powerLeft);
      charging.setValue(false);
    }
    else
    {
      state = socket.powerOn(Socket.ID.LEFT, true);
      chargerOn.setValue(state.powerLeft);
      charging.setValue(true);
    }
  }
}