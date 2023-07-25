package com.rosario.naviversity;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ActivitiesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ActivitiesFragment extends Fragment {

    ArrayList<Ride> listRides = new ArrayList<>();

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";

    public ActivitiesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_activities, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.mRecyclerView);

        Place start = new Place();
        Car car = new Car("Fiat Panda", "XXXXXXX", "Arancione");

        start.setName("Dipartimento di Economia e Impresa");
        Ride r1 = new Ride(start, start, "ciao", "ciao", "ciao");
        r1.setCar(car);
        r1.setOwner("4/5");
        Ride r2 = new Ride(start, start, "ciao", "ciao", "ciao");
        r2.setOwner("5/5");
        r2.setCar(car);
        r1.setDate("21/12/2023");
        r2.setDate("21/12/2023");
        r1.setTime("12:00");
        r2.setTime("15:30");
        listRides.add(r1);
        listRides.add(r2);

        RideRecyclerViewAdapter adapter = new RideRecyclerViewAdapter(getContext(), listRides);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        return view;
    }
}