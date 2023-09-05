package com.rosario.naviversity;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.material.textfield.TextInputLayout;

public class AddPlaceFragment extends Fragment {

    Button btnAdd;
    EditText placeNameTxt, placeLatTxt, placeLongTxt;
    AutoCompleteTextView placeTypeTxt;
    TextInputLayout placeNameLayout, placeLatLayout, placeLongLayout, placeTypeLayout;
    String[] definisci array di tipologie


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_place, container, false);
        btnAdd = view.findViewById(R.id.btn_add);
        placeNameTxt = view.findViewById(R.id.place_name);
        placeLatTxt = view.findViewById(R.id.place_lat);
        placeLongTxt = view.findViewById(R.id.place_long);
        placeTypeTxt = view.findViewById(R.id.place_type);
        placeNameLayout = view.findViewById(R.id.place_name_input_layout);
        placeLatLayout = view.findViewById(R.id.place_lat_input_layout);
        placeLongLayout = view.findViewById(R.id.place_long_input_layout);
        placeTypeLayout = view.findViewById(R.id.place_type_input_layout);


        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkFilledPlaceValues()){
                    //writeNewPlace();
                }
            }
        });
        return view;
    }

    public boolean checkFilledPlaceValues(){
        String name, lat, lon, type;

        name = String.valueOf(placeNameTxt.getText());
        lat = String.valueOf(placeLatTxt.getText());
        lon = String.valueOf(placeLongTxt.getText());
        type = String.valueOf(placeTypeTxt.getText());

        if(TextUtils.isEmpty(name)){
            placeNameLayout.setError("Inserire un nome");
            return false;
        }else{
            if(placeNameLayout.getError() != null){
                placeNameLayout.setErrorEnabled(false);
            }
        }
        if(TextUtils.isEmpty(lat)){
            placeLatLayout.setError("Inserire una latitudine");
            return false;
        }else{
            placeLatLayout.setErrorEnabled(false);
        }
        if(TextUtils.isEmpty(lon)){
            placeLongLayout.setError("Inserire una longitudine");
            return false;
        }else{
            placeLongLayout.setErrorEnabled(false);
        }
        if(TextUtils.isEmpty(type)){
            placeTypeLayout.setError("Selezionare una tipologia");
            return false;
        }else{
            placeTypeLayout.setErrorEnabled(false);
        }
        return true;
    }
}