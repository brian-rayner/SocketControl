package uk.co.brayner.socketcontrol.ui.home;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import uk.co.brayner.socketcontrol.R;

public class HomeFragment extends Fragment
{

  private HomeViewModel homeViewModel;

  ImageView ivLeft;
  ImageView ivRight;

  public View onCreateView(@NonNull LayoutInflater inflater,
                           ViewGroup container, Bundle savedInstanceState)
  {
    homeViewModel =
            new ViewModelProvider(this).get(HomeViewModel.class);
    View root = inflater.inflate(R.layout.fragment_home, container, false);

    ivLeft = root.findViewById(R.id.ivLeft);
    ivRight = root.findViewById(R.id.ivRight);

    ivLeft.setOnClickListener(view -> { onClickLeft (); });
    ivRight.setOnClickListener(view -> { onClickRight (); });

    // Add observers for any changes to the underlying viewmodel

    homeViewModel.getLeft().observe(getViewLifecycleOwner(), new Observer<Boolean>()
    {
      @Override
      public void onChanged(@Nullable Boolean on)
      {
        if (on)
          ivLeft.setImageResource(R.drawable.power_on);
        else
          ivLeft.setImageResource(R.drawable.power_off);
      }
    });

    homeViewModel.getRight().observe(getViewLifecycleOwner(), new Observer<Boolean>()
    {
      @Override
      public void onChanged(@Nullable Boolean on)
      {
        if (on)
          ivRight.setImageResource(R.drawable.power_on);
        else
          ivRight.setImageResource(R.drawable.power_off);
      }
    });

    return root;
  }

  private void onClickLeft ()
  {
    homeViewModel.toggleLeft();
  }

  private void onClickRight ()
  {
    homeViewModel.toggleRight();
  }
}