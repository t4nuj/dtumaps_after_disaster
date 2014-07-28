package dtu.dtumaps;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


import com.makemyandroidapp.googleformuploader.GoogleFormUploader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;






public class NewPlace extends Fragment {

    int screenHeight;
    int screenWidth;

    private EditText titleBox,subtitleBox,floorBox,nameBox,emailBox;
    private Button submitButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_newplace, container, false);

        titleBox = (EditText)view.findViewById(R.id.editText2);
        subtitleBox = (EditText)view.findViewById(R.id.editText3);
        floorBox = (EditText)view.findViewById(R.id.editText4);
        nameBox = (EditText)view.findViewById(R.id.editText5);
        emailBox = (EditText)view.findViewById(R.id.editText6);
        submitButton = (Button)view.findViewById(R.id.submitButton);

        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        screenHeight = metrics.heightPixels;
        screenWidth = metrics.widthPixels;

        titleBox.setY(0);
        subtitleBox.setY( (screenHeight/20));
        floorBox.setY((screenHeight/20)*2);
        nameBox.setY( (screenHeight/20) *3);
        emailBox.setY((screenHeight/20)*4);

        titleBox.setHint("Title");
        subtitleBox.setHint("Subtitle");
        floorBox.setHint("Floor");
        nameBox.setHint("Your Name");
        emailBox.setHint("Your e-mail Id");
        submitButton.setText("Add Place");


        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                writeToFile(titleBox.getText().toString(), subtitleBox.getText().toString(),floorBox.getText().toString(),nameBox.getText().toString(),emailBox.getText().toString());

            }
        });


        return view;
    }

    void writeToFile(String title,String subtitle,String floor, String name,String email) {



        Bundle args = getArguments();

        GoogleFormUploader uploader = new GoogleFormUploader("13dmvLGAKUAy-GfcZiIovrp5lEg9zgh2-mgsxP6uq5tw");
        uploader.addEntry("1500039523",title);
        uploader.addEntry("1993349306",subtitle);
        uploader.addEntry("274955233",floor);
        uploader.addEntry("2075683479",args.getString("lat"));
        uploader.addEntry("2077764936",args.getString("lon"));
        uploader.addEntry("296289044",name);
        uploader.addEntry("1561815213",email);
        uploader.upload();
        getFragmentManager().beginTransaction().replace(R.id.fragmentSpace,new Mapper()).commit();

        };
    }


