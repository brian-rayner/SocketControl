package uk.co.brayner.socketcontrol.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import uk.co.brayner.socketcontrol.R;

public class HomeFragment extends Fragment
{
  private HomeViewModel homeViewModel;

  ImageView ivLeft;
  ImageView ivRight;

  //---------------------------------------------------------------------------

  public View onCreateView(@NonNull LayoutInflater inflater,
                           ViewGroup container, Bundle savedInstanceState)
  {
    homeViewModel =
            new ViewModelProvider(this).get(HomeViewModel.class);
    View root = inflater.inflate(R.layout.fragment_home, container, false);

    ivLeft = root.findViewById(R.id.ivLeft);
    ivRight = root.findViewById(R.id.ivRight);

    ivLeft.setOnClickListener(view -> onClickLeft ());
    ivRight.setOnClickListener(view -> onClickRight ());

    // Set initial state of indicators

    if (homeViewModel.getLeft().getValue())
      ivLeft.setImageResource(R.drawable.power_on);
    else
      ivLeft.setImageResource(R.drawable.power_off);

    if (homeViewModel.getRight().getValue())
      ivRight.setImageResource(R.drawable.power_on);
    else
      ivRight.setImageResource(R.drawable.power_off);

    // Add observers for any changes to the underlying viewmodel

    homeViewModel.getLeft().observe(getViewLifecycleOwner(), on ->
    {
      if (on)
        ivLeft.setImageResource(R.drawable.power_on);
      else
        ivLeft.setImageResource(R.drawable.power_off);
    });

    homeViewModel.getRight().observe(getViewLifecycleOwner(), on ->
    {
      if (on)
        ivRight.setImageResource(R.drawable.power_on);
      else
        ivRight.setImageResource(R.drawable.power_off);
    });

    return root;
  }

  //---------------------------------------------------------------------------

  private void onClickLeft ()
  {
    homeViewModel.toggleLeft();
  }

  //---------------------------------------------------------------------------

  private void onClickRight ()
  {
    homeViewModel.toggleRight();
  }
}