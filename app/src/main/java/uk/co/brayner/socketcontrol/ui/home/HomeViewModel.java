package uk.co.brayner.socketcontrol.ui.home;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.preference.PreferenceManager;
import uk.co.brayner.socketcontrol.WallSocket;

public class HomeViewModel extends AndroidViewModel
{
  WallSocket wallSocket;
  WallSocket.State state;

  private final MutableLiveData<Boolean> left;
  private final MutableLiveData<Boolean> right;

  //---------------------------------------------------------------------------

  public HomeViewModel(@NonNull Application application)
  {
    super(application);

    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(application);
    String host = prefs.getString("ip", "");

    wallSocket = WallSocket.getInstance(host);
    left = new MutableLiveData<>();
    right = new MutableLiveData<>();

    state = wallSocket.getState();
    left.setValue(state.powerLeft);
    right.setValue(state.powerRight);
  }

  //---------------------------------------------------------------------------

  public LiveData<Boolean> getLeft()
  {
    return left;
  }
  public LiveData<Boolean> getRight()
  {
    return right;
  }

  //---------------------------------------------------------------------------

  public void toggleLeft ()
  {
    state = wallSocket.powerOn(WallSocket.ID.LEFT, !left.getValue());
    left.setValue(state.powerLeft);
    right.setValue(state.powerRight);
  }

  //---------------------------------------------------------------------------

  public void toggleRight ()
  {
    state = wallSocket.powerOn(WallSocket.ID.RIGHT, !right.getValue());
    left.setValue(state.powerLeft);
    right.setValue(state.powerRight);
  }
}