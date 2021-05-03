package uk.co.brayner.socketcontrol;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

import uk.co.brayner.socketcontrol.ui.schedule.ScheduleViewModel;

public class Scheduler extends BroadcastReceiver
{
  @Override
  public void onReceive(Context context, Intent intent)
  {
    PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
    PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "socketcontrol:Scheduler");

    wl.acquire(10000L);     // Apply 10 second wake lock to keep CPU alive

    Logger.log(context, "Broadcast receiver called");
    ScheduleViewModel.onTimer(context);

    wl.release();

  }
}
