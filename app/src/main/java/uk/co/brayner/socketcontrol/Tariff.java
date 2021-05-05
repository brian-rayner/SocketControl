package uk.co.brayner.socketcontrol;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Calendar;
import java.util.List;
import java.util.function.BiConsumer;

import androidx.preference.PreferenceManager;
import uk.co.brayner.socketcontrol.octopus.Rates;

public interface Tariff
{
  public enum Type
  {
    AGILE,
    GO,
    OTHER
  }

  //---------------------------------------------------------------------------

  static Type getType (Context context)
  {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
    String tariff = prefs.getString("tariff", "");

    switch (tariff)
    {
      case "agile":   return Type.AGILE;
      case "go":      return Type.GO;
      default:        return Type.OTHER;
    }
  }

  //---------------------------------------------------------------------------

  static Tariff getTariff (Context context)
  {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
    String tariff = prefs.getString("tariff", "");

    switch (tariff)
    {
      case "agile":   return new AgileTariff(context);
      case "go":      return new GoTariff(context);
      default:        return null;
    }
  }

  //---------------------------------------------------------------------------

  void getRates (Calendar from, BiConsumer<Context, List<Rates.Result>> callback);

  //---------------------------------------------------------------------------

  boolean isLowRate (Calendar startTime, int price, List<Rates.Result> rates);
}
