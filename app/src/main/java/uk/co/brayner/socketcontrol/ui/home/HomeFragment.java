package uk.co.brayner.socketcontrol.ui.home;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import uk.co.brayner.socketcontrol.R;

public class HomeFragment extends Fragment
{
  private HomeViewModel homeViewModel;

  public View onCreateView(@NonNull LayoutInflater inflater,
                           ViewGroup container, Bundle savedInstanceState)
  {
    homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
    View root = inflater.inflate(R.layout.fragment_home, container, false);

    TextView twState = root.findViewById(R.id.twState);
    TextView twAdd = root.findViewById(R.id.twAdd);
    EditText etAdd = root.findViewById(R.id.etAdd);
    TextView twUnits = root.findViewById(R.id.twUnits);
    TextView twPrice = root.findViewById(R.id.twPrice);
    EditText etPrice = root.findViewById(R.id.etPrice);
    TextView twPriceUnits = root.findViewById(R.id.twPriceUnits);
    TextView twChargerOn = root.findViewById(R.id.twChargerOn);

    RadioButton rbAddMiles = root.findViewById(R.id.rbAddMiles);
    RadioButton rbAddKWH = root.findViewById(R.id.rbAddKWH);
    RadioButton rbToFull = root.findViewById(R.id.rbToFull);
    RadioButton rbPrice = root.findViewById(R.id.rbPrice);
    RadioButton rbAlways = root.findViewById(R.id.rbAlways);

    Button btnStart = root.findViewById(R.id.btnStart);

    switch (homeViewModel.getChargeFor().getValue())
    {
      case TO_FULL:   rbToFull.setChecked(true);    break;
      case ADD_MILES: rbAddMiles.setChecked(true);  break;
      case ADD_KWH:   rbAddKWH.setChecked(true);    break;
    }

    switch (homeViewModel.getChargeWhen().getValue())
    {
      case ANY_TIME:  rbAlways.setChecked(true);  break;
      case MAX_PRICE: rbPrice.setChecked(true);   break;
    }

    rbAddMiles.setOnClickListener(view -> { onClickAddMiles (); });
    rbAddKWH.setOnClickListener(view -> { onClickAddKWH (); });
    rbToFull.setOnClickListener(view -> { onClickToFull (); });
    rbPrice.setOnClickListener(view -> { onClickPrice (); });
    rbAlways.setOnClickListener(view -> { onClickAnyTime (); });
    btnStart.setOnClickListener(view -> { onClickStart (); });

    homeViewModel.getCharging().observe(getViewLifecycleOwner(), new Observer<Boolean>()
    {
      @Override
      public void onChanged(@Nullable Boolean charging)
      {
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
        }
      }
    });

    homeViewModel.getChargerOn().observe(getViewLifecycleOwner(), new Observer<Boolean>()
    {
      @Override
      public void onChanged(@Nullable Boolean chargerOn)
      {
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

    homeViewModel.getChargeFor().observe(getViewLifecycleOwner(), new Observer<HomeViewModel.ChargeFor>()
    {
      @Override
      public void onChanged(@Nullable HomeViewModel.ChargeFor chargeFor)
      {
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

          case ADD_KWH:
            twAdd.setVisibility(View.VISIBLE);
            etAdd.setVisibility(View.VISIBLE);
            twUnits.setVisibility(View.VISIBLE);
            twUnits.setText(R.string.kwh);
            break;
        }
      }
    });

    homeViewModel.getChargeWhen().observe(getViewLifecycleOwner(), new Observer<HomeViewModel.ChargeWhen>()
    {
      @Override
      public void onChanged(@Nullable HomeViewModel.ChargeWhen chargeWhen)
      {
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

    return root;
  }

  private void onClickAddMiles ()
  {
    homeViewModel.setChargeFor(HomeViewModel.ChargeFor.ADD_MILES);
  }

  private void onClickAddKWH ()
  {
    homeViewModel.setChargeFor(HomeViewModel.ChargeFor.ADD_KWH);
  }

  private void onClickToFull ()
  {
    homeViewModel.setChargeFor(HomeViewModel.ChargeFor.TO_FULL);
  }

  private void onClickPrice ()
  {
    homeViewModel.setChargeWhen(HomeViewModel.ChargeWhen.MAX_PRICE);
  }

  private void onClickAnyTime ()
  {
    homeViewModel.setChargeWhen(HomeViewModel.ChargeWhen.ANY_TIME);
  }

  private void onClickStart ()
  {
    homeViewModel.startStop ();
  }
}