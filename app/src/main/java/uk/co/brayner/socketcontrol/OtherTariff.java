package uk.co.brayner.socketcontrol;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Calendar;
import java.util.List;
import java.util.function.BiConsumer;

import androidx.preference.PreferenceManager;
import uk.co.brayner.socketcontrol.octopus.Rates;

import static java.util.Calendar.HOUR;
import static java.util.Calendar.YEAR;

public class OtherTariff implements Tariff
{
  private Context context;

  //---------------------------------------------------------------------------

  public OtherTariff (Context context)
  {
    this.context = context;
  }

  //---------------------------------------------------------------------------

  @Override
  public void getRates (Calendar from, BiConsumer<Context, List<Rates.Result>> callback)
  {
    callback.accept(context, null);
  }

  //---------------------------------------------------------------------------

  @Override
  public boolean isLowRate(Calendar startTime, int price, List<Rates.Result> rates)
  {
    // There is no other rate!

    return true;
  }
}
