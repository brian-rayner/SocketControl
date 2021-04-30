package uk.co.brayner.socketcontrol.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import uk.co.brayner.socketcontrol.WallSocket;

public class HomeViewModel extends ViewModel
{
  WallSocket wallSocket = WallSocket.getInstance();
  WallSocket.State state;

  private MutableLiveData<Boolean> left;
  private MutableLiveData<Boolean> right;

  public HomeViewModel()
  {
    left = new MutableLiveData<>();
    right = new MutableLiveData<>();

    state = wallSocket.getState();
    left.setValue(state.powerLeft);
    right.setValue(state.powerRight);
  }

  public LiveData<Boolean> getLeft()
  {
    return left;
  }
  public LiveData<Boolean> getRight()
  {
    return right;
  }

  public void toggleLeft ()
  {
    state = wallSocket.powerOn(WallSocket.ID.LEFT, !left.getValue());
    left.setValue(state.powerLeft);
    right.setValue(state.powerRight);
  }

  public void toggleRight ()
  {
    state = wallSocket.powerOn(WallSocket.ID.RIGHT, !right.getValue());
    left.setValue(state.powerLeft);
    right.setValue(state.powerRight);
  }
}