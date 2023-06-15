package com.rosario.naviversity;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SearchRideFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SearchRideFragment extends Fragment {
    Button btnSearch;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_ride, container, false);
        btnSearch = view.findViewById(R.id.btnSearch);

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment mapsFragment = new MapsFragment();
                Bundle result = new Bundle();
                result.putString("ciao", "testo da visualizzar");
                getParentFragmentManager().setFragmentResult("dataFromSearch", result);
                Toast.makeText(getContext(), "messaggio mandato", Toast.LENGTH_SHORT).show();

                FragmentTransaction transaction = getParentFragmentManager().beginTransaction().replace(R.id.frameLayout, mapsFragment);
                transaction.commit();

            }
        });
        return view;
    }
}