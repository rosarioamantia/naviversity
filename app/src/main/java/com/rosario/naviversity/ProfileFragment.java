package com.rosario.naviversity;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {

    FirebaseAuth mAuth;
    FirebaseUser fUser;
    SwitchMaterial carSwitch;
    User currentUser;
    FirebaseDatabase mDatabase;
    DatabaseReference dbReference;
    TextView userName, userSurname, userEmail;
    RatingBar ratingBar;
    ConstraintLayout carConstraintLayout;
    String[] carColors, carModels;
    AutoCompleteTextView carColorTxt, carModelTxt;


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        carSwitch = view.findViewById(R.id.car_switch);
        mAuth = FirebaseAuth.getInstance();
        fUser = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance();
        dbReference = mDatabase.getReference();
        userName = view.findViewById(R.id.user_name);
        userSurname = view.findViewById(R.id.user_surname);
        userEmail = view.findViewById(R.id.user_mail);
        ratingBar = view.findViewById(R.id.rating_bar);
        carConstraintLayout = view.findViewById(R.id.car_constratint_layout);
        carColors = getResources().getStringArray(R.array.car_colors);
        carModels = getResources().getStringArray(R.array.car_models);

        setUIData(view);

        return view;
    }

    public void setUIData(View view){
        dbReference.child("user").child(fUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                currentUser = snapshot.getValue(User.class);
                currentUser.setId(snapshot.getKey());
                userName.setText(currentUser.getName());
                userSurname.setText(currentUser.getSurname());
                userEmail.setText(fUser.getEmail());
                ratingBar.setRating(4.0f);


                if(currentUser.isCarOwner()){
                    carConstraintLayout.setVisibility(View.VISIBLE);
                    ArrayAdapter<String> carColorsAdapter = new ArrayAdapter<>(getContext(),
                            androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
                            carColors);
                    ArrayAdapter<String> carModelsAdapter = new ArrayAdapter<>(getContext(),
                            androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
                            carModels);
                    carModelTxt = view.findViewById(R.id.car_model_txt);
                    carColorTxt = view.findViewById(R.id.car_color_txt);
                    carColorTxt.setText(currentUser.getCar().getColor());
                }else{
                    carConstraintLayout.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}