package com.rosario.naviversity;

import android.location.Location;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SearchRideFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SearchRideFragment extends Fragment {
    //GoogleMap googleMap;
    private static final String[] DEPARTMENTS = new String[] {
            "Facoltà di Economia", "Facoltà di Matematica e Informatica"
    };
    private static final String[] POI = new String[] {
            "Facoltà di Economia", "Facoltà di Matematica e Informatica"
    };
    Button btnSearch;
    AutoCompleteTextView autoCompleteTextView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(requireContext(), android.R.layout.simple_list_item_1, DEPARTMENTS);

        View view = inflater.inflate(R.layout.fragment_search_ride, container, false);
        btnSearch = view.findViewById(R.id.btnSearch);
        autoCompleteTextView = view.findViewById(R.id.primo);

        autoCompleteTextView.setAdapter(adapter);

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                float[] results = new float[1];
                Location.distanceBetween(37.56366662350805, 15.066447526196836, 37.56364111001874, 15.06649044153912, results);
                System.out.println("CIAOCIAO" + results[0]);

                Fragment mapsFragment = new MapsFragment();
                Bundle result = new Bundle();
                result.putString("ciao", "via gravina 85 tremestieri etneo");
                getParentFragmentManager().setFragmentResult("dataFromSearch", result);
                Toast.makeText(getContext(), "messaggio mandato", Toast.LENGTH_SHORT).show();

                FragmentTransaction transaction = getParentFragmentManager().beginTransaction().replace(R.id.frameLayout, mapsFragment);
                transaction.commit();

            }
        });
        return view;
    }
}