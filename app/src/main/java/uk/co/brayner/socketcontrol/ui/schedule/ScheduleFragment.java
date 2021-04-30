package uk.co.brayner.socketcontrol.ui.schedule;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;
import uk.co.brayner.socketcontrol.R;

public class ScheduleFragment extends Fragment
{
  static String TAG = "ScheduleFragment";

  private ScheduleViewModel scheduleViewModel;
  private SharedPreferences prefs;

  TextView twState;
  TextView twAdd;
  EditText etAdd;
  TextView twUnits;
  TextView twPrice;
  EditText etPrice;
  TextView twPriceUnits;
  TextView twChargerOn;
  TextView twUntil;

  RadioButton rbAddMiles;
  RadioButton rbAddCharge;
  RadioButton rbToFull;
  RadioButton rbPrice;
  RadioButton rbAnyTime;

  Button btnStart;

  public View onCreateView(@NonNull LayoutInflater inflater,
                           ViewGroup container, Bundle savedInstanceState)
  {
    scheduleViewModel = new ViewModelProvider(this).get(ScheduleViewModel.class);
    prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

    View root = inflater.inflate(R.layout.fragment_schedule, container, false);

    // Get references to all significant controls on the page

    twState = root.findViewById(R.id.twState);
    twAdd = root.findViewById(R.id.twAdd);
    etAdd = root.findViewById(R.id.etAdd);
    twUnits = root.findViewById(R.id.twUnits);
    twPrice = root.findViewById(R.id.twPrice);
    etPrice = root.findViewById(R.id.etPrice);
    twPriceUnits = root.findViewById(R.id.twPriceUnits);
    twChargerOn = root.findViewById(R.id.twChargerOn);
    twUntil = root.findViewById(R.id.twUntil);

    rbAddMiles = root.findViewById(R.id.rbAddMiles);
    rbAddCharge = root.findViewById(R.id.rbAddCharge);
    rbToFull = root.findViewById(R.id.rbToFull);
    rbPrice = root.findViewById(R.id.rbPrice);
    rbAnyTime = root.findViewById(R.id.rbAnyTime);

    btnStart = root.findViewById(R.id.btnStart);

    // Set the initial states according to viewmodel

    switch (scheduleViewModel.getChargeFor().getValue())
    {
      case TO_FULL:     rbToFull.setChecked(true);      break;
      case ADD_MILES:   rbAddMiles.setChecked(true);    break;
      case ADD_CHARGE:  rbAddCharge.setChecked(true);   break;
    }

    switch (scheduleViewModel.getChargeWhen().getValue())
    {
      case ANY_TIME:  rbAnyTime.setChecked(true);   break;
      case MAX_PRICE: rbPrice.setChecked(true);     break;
    }

    if (rbAddMiles.isChecked())
      etAdd.setText(scheduleViewModel.getMilesToAdd().getValue().toString());
    else if (rbAddCharge.isChecked())
      etAdd.setText(scheduleViewModel.getChargeToAdd().getValue().toString());
    etPrice.setText(scheduleViewModel.getPriceLimit().getValue().toString());

    setChargeOptions();

    // Add onClick handlers for all buttons on the page

    rbAddMiles.setOnClickListener(view -> { onClickAddMiles (); });
    rbAddCharge.setOnClickListener(view -> { onClickAddCharge (); });
    rbToFull.setOnClickListener(view -> { onClickToFull (); });
    rbPrice.setOnClickListener(view -> { onClickPrice (); });
    rbAnyTime.setOnClickListener(view -> { onClickAnyTime (); });
    btnStart.setOnClickListener(view -> { onClickStart (); });

    // Add observers for any changes to the underlying viewmodel

    scheduleViewModel.getCharging().observe(getViewLifecycleOwner(), new Observer<Boolean>()
    {
      @Override
      public void onChanged(@Nullable Boolean charging)
      {
        // Called when charging state changed.  Update display accordingly.

        if (charging)
        {
          twState.setText(R.string.charging);
          twState.setBackgroundColor(Color.parseColor("#8BC34A"));
          btnStart.setText(R.string.stop);

        }
        else
        {
          twState.setText(R.string.not_charging);
          twState.setBackgroundColor(Color.parseColor("#FF9800"));
          btnStart.setText(R.string.start);
          twUntil.setText("");
        }
      }
    });

    scheduleViewModel.getChargerOn().observe(getViewLifecycleOwner(), new Observer<Boolean>()
    {
      @Override
      public void onChanged(@Nullable Boolean chargerOn)
      {
        // Called when charger turns on or off.  Update display accordingly.

        if (chargerOn)
        {
          twChargerOn.setText(R.string.on);
          twChargerOn.setBackgroundColor(Color.parseColor("#8BC34A"));
        }
        else
        {
          twChargerOn.setText(R.string.off);
          twChargerOn.setBackgroundColor(Color.parseColor("#FF9800"));
        }
      }
    });

    scheduleViewModel.getChargeFor().observe(getViewLifecycleOwner(), new Observer<ScheduleViewModel.ChargeFor>()
    {
      @Override
      public void onChanged(@Nullable ScheduleViewModel.ChargeFor chargeFor)
      {
        // Called when the 'charge for' option changes.  Update display accordingly.

        switch (chargeFor)
        {
          case TO_FULL:
            twAdd.setVisibility(View.INVISIBLE);
            etAdd.setVisibility(View.INVISIBLE);
            twUnits.setVisibility(View.INVISIBLE);
            break;

          case ADD_MILES:
            twAdd.setVisibility(View.VISIBLE);
            etAdd.setVisibility(View.VISIBLE);
            twUnits.setVisibility(View.VISIBLE);
            twUnits.setText(R.string.miles);
            break;

          case ADD_CHARGE:
            twAdd.setVisibility(View.VISIBLE);
            etAdd.setVisibility(View.VISIBLE);
            twUnits.setVisibility(View.VISIBLE);
            twUnits.setText(R.string.kwh);
            break;
        }
      }
    });

    scheduleViewModel.getChargeWhen().observe(getViewLifecycleOwner(), new Observer<ScheduleViewModel.ChargeWhen>()
    {
      @Override
      public void onChanged(@Nullable ScheduleViewModel.ChargeWhen chargeWhen)
      {
        // Called when the 'charge when' option changes.  Update display accordingly.

        switch (chargeWhen)
        {
          case MAX_PRICE:
            twPrice.setVisibility(View.VISIBLE);
            etPrice.setVisibility(View.VISIBLE);
            twPriceUnits.setVisibility(View.VISIBLE);
            break;

          case ANY_TIME:
            twPrice.setVisibility(View.INVISIBLE);
            etPrice.setVisibility(View.INVISIBLE);
            twPriceUnits.setVisibility(View.INVISIBLE);
            break;
        }
      }
    });

    scheduleViewModel.getNextTime().observe(getViewLifecycleOwner(), new Observer<Date>()
    {
      @Override
      public void onChanged(@Nullable Date nextTime)
      {
        // Called when the next event time changes.  Update display accordingly.

        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.UK);
        twUntil.setText(getText(R.string.until) + " " + df.format(nextTime));
      }
    });

    return root;
  }

  // onClick handlers; inform the viewmodel of the event.

  private void onClickAddMiles ()
  {
    scheduleViewModel.setChargeFor(ScheduleViewModel.ChargeFor.ADD_MILES);
    etAdd.setText(scheduleViewModel.getMilesToAdd().getValue().toString());
  }

  private void onClickAddCharge ()
  {
    scheduleViewModel.setChargeFor(ScheduleViewModel.ChargeFor.ADD_CHARGE);
    etAdd.setText(scheduleViewModel.getChargeToAdd().getValue().toString());
  }

  private void onClickToFull ()
  {
    scheduleViewModel.setChargeFor(ScheduleViewModel.ChargeFor.TO_FULL);
  }

  private void onClickPrice ()
  {
    scheduleViewModel.setChargeWhen(ScheduleViewModel.ChargeWhen.MAX_PRICE);
  }

  private void onClickAnyTime ()
  {
    scheduleViewModel.setChargeWhen(ScheduleViewModel.ChargeWhen.ANY_TIME);
  }

  private void onClickStart ()
  {
    scheduleViewModel.setToAdd(Integer.parseInt(etAdd.getText().toString()));
    scheduleViewModel.setPriceLimit(Integer.parseInt(etPrice.getText().toString()));
    scheduleViewModel.startStop ();
  }

  private void setChargeOptions()
  {
    String tariff = prefs.getString("tariff", "");
    if (tariff.equals("go"))
    {
      rbPrice.setText("GO low rate");
      rbPrice.setVisibility(View.VISIBLE);
    }
    else if (tariff.equals("agile"))
    {
      rbPrice.setText("Price below");
      rbPrice.setVisibility(View.VISIBLE);
    }
    else
    {
      rbPrice.setVisibility(View.INVISIBLE);
    }
  }
}