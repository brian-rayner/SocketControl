package uk.co.brayner.socketcontrol.ui.schedule;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
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
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;
import uk.co.brayner.socketcontrol.R;
import uk.co.brayner.socketcontrol.Tariff;

public class ScheduleFragment extends Fragment
{
  static String TAG = "ScheduleFragment";

  private ScheduleViewModel scheduleViewModel;

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
  RadioButton rbOffPeak;
  RadioButton rbAnyTime;

  Button btnStart;

  //---------------------------------------------------------------------------

  public View onCreateView(@NonNull LayoutInflater inflater,
                           ViewGroup container, Bundle savedInstanceState)
  {
    scheduleViewModel = new ViewModelProvider(this).get(ScheduleViewModel.class);

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
    rbOffPeak = root.findViewById(R.id.rbOffPeak);
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
      case OFF_PEAK:  rbOffPeak.setChecked(true);   break;
    }

    if (rbAddMiles.isChecked())
      etAdd.setText(String.format(Locale.ENGLISH, "%d", scheduleViewModel.getMilesToAdd().getValue()));
    else if (rbAddCharge.isChecked())
      etAdd.setText(String.format(Locale.ENGLISH, "%d", scheduleViewModel.getChargeToAdd().getValue()));
    etPrice.setText(String.format(Locale.ENGLISH, "%d", scheduleViewModel.getPriceLimit().getValue()));

    setChargeOptions();
    displayState (scheduleViewModel.getState().getValue());
    displayChargerOn (scheduleViewModel.getChargerOn().getValue());
    displayNextEventTime (scheduleViewModel.getNextTime().getValue());

    // Add onClick handlers for all buttons on the page

    rbAddMiles.setOnClickListener(view -> onClickAddMiles ());
    rbAddCharge.setOnClickListener(view -> onClickAddCharge ());
    rbToFull.setOnClickListener(view -> onClickToFull ());
    rbPrice.setOnClickListener(view -> onClickPrice ());
    rbOffPeak.setOnClickListener(view -> onClickOffPeak ());
    rbAnyTime.setOnClickListener(view -> onClickAnyTime ());
    btnStart.setOnClickListener(view -> onClickStart ());

    // Add observers for any changes to the underlying viewmodel

    scheduleViewModel.getState().observe(getViewLifecycleOwner(), this::displayState);

    scheduleViewModel.getChargerOn().observe(getViewLifecycleOwner(), this::displayChargerOn);

    scheduleViewModel.getChargeFor().observe(getViewLifecycleOwner(), chargeFor ->
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
    });

    scheduleViewModel.getChargeWhen().observe(getViewLifecycleOwner(), chargeWhen ->
    {
      // Called when the 'charge when' option changes.  Update display accordingly.

      Tariff.Type type = Tariff.getType(getContext());
      if (chargeWhen == ScheduleViewModel.ChargeWhen.MAX_PRICE && type == Tariff.Type.AGILE)
      {
        twPrice.setVisibility(View.VISIBLE);
        etPrice.setVisibility(View.VISIBLE);
        twPriceUnits.setVisibility(View.VISIBLE);
      }
      else
      {
        twPrice.setVisibility(View.INVISIBLE);
        etPrice.setVisibility(View.INVISIBLE);
        twPriceUnits.setVisibility(View.INVISIBLE);
      }
    });

    scheduleViewModel.getNextTime().observe(getViewLifecycleOwner(), this::displayNextEventTime);

    return root;
  }

  //---------------------------------------------------------------------------

  // onClick handlers; inform the viewmodel of the event.

  private void onClickAddMiles ()
  {
    scheduleViewModel.setChargeFor(ScheduleViewModel.ChargeFor.ADD_MILES);
    etAdd.setText(String.format(Locale.ENGLISH, "%d", scheduleViewModel.getMilesToAdd().getValue()));
  }

  //---------------------------------------------------------------------------

  private void onClickAddCharge ()
  {
    scheduleViewModel.setChargeFor(ScheduleViewModel.ChargeFor.ADD_CHARGE);
    etAdd.setText(String.format(Locale.ENGLISH, "%d", scheduleViewModel.getChargeToAdd().getValue()));
  }

  //---------------------------------------------------------------------------

  private void onClickToFull ()
  {
    scheduleViewModel.setChargeFor(ScheduleViewModel.ChargeFor.TO_FULL);
  }

  //---------------------------------------------------------------------------

  private void onClickPrice ()
  {
    scheduleViewModel.setChargeWhen(ScheduleViewModel.ChargeWhen.MAX_PRICE);
  }

  //---------------------------------------------------------------------------

  private void onClickOffPeak ()
  {
    scheduleViewModel.setChargeWhen(ScheduleViewModel.ChargeWhen.OFF_PEAK);
  }

  //---------------------------------------------------------------------------

  private void onClickAnyTime ()
  {
    scheduleViewModel.setChargeWhen(ScheduleViewModel.ChargeWhen.ANY_TIME);
  }

  //---------------------------------------------------------------------------

  private void onClickStart ()
  {
    if (scheduleViewModel.getChargeFor().getValue() != ScheduleViewModel.ChargeFor.TO_FULL)
      scheduleViewModel.setToAdd(Integer.parseInt(etAdd.getText().toString()));
    if (scheduleViewModel.getChargeWhen().getValue() == ScheduleViewModel.ChargeWhen.MAX_PRICE)
      scheduleViewModel.setPriceLimit(Integer.parseInt(etPrice.getText().toString()));
    scheduleViewModel.startStop ();
  }

  //---------------------------------------------------------------------------

  private void setChargeOptions()
  {
    Tariff.Type type = Tariff.getType(getContext());
    switch (type)
    {
      case GO:
        rbPrice.setVisibility(View.GONE);
        rbOffPeak.setVisibility(View.VISIBLE);
        break;

      case AGILE:
        rbPrice.setVisibility(View.VISIBLE);
        rbOffPeak.setVisibility(View.GONE);
        break;

      default:
        rbPrice.setVisibility(View.GONE);
        rbOffPeak.setVisibility(View.GONE);
        break;
    }
  }

  //---------------------------------------------------------------------------

  void displayState (ScheduleViewModel.State state)
  {
    // Update display to reflect the given charging state

    switch (state)
    {
      case NOT_STARTED:
        twState.setText(R.string.not_charging);
        twState.setBackgroundColor(Color.parseColor("#FF9800"));
        btnStart.setText(R.string.start);
        twUntil.setVisibility(View.INVISIBLE);
        break;

      case CHARGING:
        twState.setText(R.string.charging);
        twState.setBackgroundColor(Color.parseColor("#8BC34A"));
        btnStart.setText(R.string.stop);
        twUntil.setVisibility(View.VISIBLE);
        break;

      case CHARGING_COMPLETE:
        twState.setText(R.string.charging_complete);
        twState.setBackgroundColor(Color.parseColor("#FF9800"));
        btnStart.setText(R.string.start);
        twUntil.setVisibility(View.INVISIBLE);
    }
  }

  //---------------------------------------------------------------------------

  void displayChargerOn (Boolean chargerOn)
  {
    // Update display to reflect given charger state

    if (chargerOn)
    {
      twChargerOn.setText(R.string.on);
      twChargerOn.setBackgroundColor(Color.parseColor("#8BC34A"));
    } else
    {
      twChargerOn.setText(R.string.off);
      twChargerOn.setBackgroundColor(Color.parseColor("#FF9800"));
    }
  }

  //---------------------------------------------------------------------------

  void displayNextEventTime (Date nextTime)
  {
    // Called when the next event time changes.  Update display accordingly.

    if (nextTime != null)
    {
      DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.UK);
      twUntil.setText(String.format(getString(R.string.until), df.format(nextTime)));
    }
  }
}