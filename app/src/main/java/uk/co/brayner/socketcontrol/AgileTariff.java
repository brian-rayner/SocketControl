package uk.co.brayner.socketcontrol;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.function.BiConsumer;

import androidx.core.util.Consumer;
import androidx.preference.PreferenceManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import uk.co.brayner.socketcontrol.octopus.OctopusApi;
import uk.co.brayner.socketcontrol.octopus.Rates;

public class AgileTariff implements Tariff
{
  private Context context;

  //---------------------------------------------------------------------------

  public AgileTariff (Context context)
  {
    this.context = context;
  }

  //---------------------------------------------------------------------------

  @Override
  public boolean isLowRate(Calendar startTime, int price, List<Rates.Result> rates)
  {
    Calendar cal = Calendar.getInstance();

    if (rates == null)
      return false;

    // Work backwards through rates list to find period which includes
    // the current time

    int index;

    for (index=0; index < rates.size(); index++)
    {
      cal.setTime(rates.get(index).validFrom);
      if (!cal.after(startTime))
        break;
    }

    return (rates.get(index).valueIncVat < price);
  }

  //---------------------------------------------------------------------------

  @Override
  public void getRates (Calendar from, BiConsumer<Context, List<Rates.Result>> callback)
  {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
    Retrofit retrofit;
    OctopusApi octopus;

    Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
            .create();

    retrofit = new Retrofit.Builder()
            .baseUrl("https://api.octopus.energy/")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build();

    octopus = retrofit.create(OctopusApi.class);

    String productCode = "AGILE-18-02-21";
    String tariffCode = "E-1R-" + productCode + "-" + prefs.getString("region", "J");

    try
    {
      DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
      dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
      String sFrom = dateFormat.format(from.getTime());

      from.add(Calendar.MINUTE, 30);
      String sTo = dateFormat.format(from.getTime());

      Call<Rates> call = octopus.getRates(productCode, tariffCode, sFrom, sTo);
      call.enqueue(new Callback<Rates>()
      {
        @Override
        public void onResponse(Call<Rates> call, Response<Rates> response)
        {
          callback.accept (context, response.body().results);
        }

        @Override
        public void onFailure(Call<Rates> call, Throwable t)
        {
          Toast.makeText(context, "Unable to retrieve current Agile rates.  Try again later.", Toast.LENGTH_LONG).show();
          callback.accept (context, null);
        }
      });
    }
    catch (Exception ex)
    {
      Toast.makeText(context, "Unable to retrieve current Agile rates.  Try again later.", Toast.LENGTH_LONG).show();
      callback.accept (context, null);
    }
  }
}
