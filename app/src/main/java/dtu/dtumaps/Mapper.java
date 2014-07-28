package dtu.dtumaps;

import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.InflateException;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by home on 7/3/14.
 */


public class Mapper extends Fragment {

    View view = null;
    int screenHeight,screenWidth;
    FrameLayout fl;

    private EditText searchBox;
    private String searchresult  = "";

    private GoogleMap dMap;
    LatLng coords = new LatLng(28.750072, 77.117730);

    location[] place;
    //Parsed info should go into this array



    Button addplaces;
    LinearLayout topBox;
    Button addButton;
    String pLat,pLon;
    MarkerOptions cmark;

    Marker drag;

    //buttons for floormarkers

    Button ground_floor;
    Button first_floor;
    Button second_floor;
    Button third_floor;
    Button clear;
    Button all;
    Button findWR,findWC;
    boolean isContributing;



    private CameraPosition collegeCenter = new CameraPosition.Builder().target(coords).zoom(17).build();



    @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (view != null) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null)
                parent.removeView(view);
        }

        try {
            view = inflater.inflate(R.layout.fragment_map, container, false);
        } catch (InflateException e) {
        /* map is already there, just return view as it is */
            return view;
        }

        isContributing = new Boolean(false);
        fl = (FrameLayout)view.findViewById(R.id.fl);
        topBox = (LinearLayout)view.findViewById(R.id.topBox);
//        coverBottom = (LinearLayout)view.findViewById(R.id.coverBottom);

        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        screenHeight = metrics.heightPixels;
        screenWidth = metrics.widthPixels;

        dMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        dMap.moveCamera(CameraUpdateFactory.newCameraPosition(collegeCenter));
        dMap.getUiSettings().setZoomControlsEnabled(false);

        searchBox = (EditText) view.findViewById(R.id.searchBox);
        searchBox.setWidth((int) (4.5* screenWidth / 7));
        searchBox.setHint("Search for a Place");
//        searchBox.setPadding(((Double)(0.5*screenWidth/7)).intValue(),0, ((Double) (0.5*screenWidth/7)).intValue(), 0);


        ground_floor = (Button) view.findViewById(R.id.GroundFloor);
        first_floor = (Button) view.findViewById(R.id.FirstFloor);
        second_floor = (Button) view.findViewById(R.id.SecondFloor);
        third_floor = (Button) view.findViewById(R.id.ThirdFloor);
        clear = (Button) view.findViewById(R.id.Clear);
        all = (Button) view.findViewById(R.id.All);
        findWC = (Button) view.findViewById(R.id.water);
        findWR = (Button) view.findViewById(R.id.wash);
        findWC.setWidth((int) (0.6*screenWidth/7));
        findWR.setWidth((int) (0.6*screenWidth/7));
//        findWC.setPadding(((Double)(0.5*screenWidth/7)).intValue(),0, ((Double) (0.5*screenWidth/7)).intValue(), 0);
//        findWR.setPadding(((Double)(0.5*screenWidth/7)).intValue(),0, ((Double) (0.5*screenWidth/7)).intValue(), 0);

        addplaces = (Button)view.findViewById(R.id.addplaces);
        addButton = new Button(getActivity());

        cmark = new MarkerOptions();


        //parses json data and transfers to array
        createDataset();

        //listens for enter press on keyboard
        searchBox.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_DONE) {

                    String input,lowerTitle,lowerSubtitle;
                    input = searchBox.getText().toString().toLowerCase();
                    removeAllMarkers();
                    searchresult = new String(input.toString());

                    for (int i=0; i<place.length; i++)
                    {
                        lowerTitle = place[i].title.toLowerCase();
                        lowerSubtitle = place[i].subtitle.toLowerCase();
                        if ((lowerTitle).contains(input) )
                        {
                            addMarker(i);
                        }
                        else if (lowerSubtitle.contains(input))
                        {
                            addMarker(i);
                        }
                    }

                    InputMethodManager in = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

                    // NOTE: In the author's example, he uses an identifier
                    // called searchBar. If setting this code on your EditText
                    // then use v.getWindowToken() as a reference to your
                    // EditText is passed into this callback as a TextView

                    in.hideSoftInputFromWindow(searchBox
                                    .getApplicationWindowToken(),
                            InputMethodManager.HIDE_NOT_ALWAYS);
//                    userValidateEntry();

                    handled = true;

                }
                return handled;
            }
        });



    addplaces.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            removeAllMarkers();
            isContributing = true;
            cmark = new MarkerOptions().position(coords).title("Your New Marker");
            drag = dMap.addMarker(new MarkerOptions().position(coords).title("Your New Marker").draggable(true));

            Toast toast = Toast.makeText(getActivity().getApplicationContext(),"Long press to reposition marker",Toast.LENGTH_SHORT);
            toast.show();


            addButton.setLayoutParams(addplaces.getLayoutParams());
            addButton.setBackgroundColor(Color.parseColor("#000000"));
            addButton.setTextColor(Color.parseColor("#ffffff"));
            addButton.setText("Add Properties");
            fl.addView(addButton);
        }
    });

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fl.removeView(addButton);

                pLat = String.valueOf(drag.getPosition().latitude);
                pLon = String.valueOf(drag.getPosition().longitude);

                Bundle args = new Bundle();
                args.putString("lat",pLat);
                args.putString("lon",pLon);

                Fragment f = (Fragment)getFragmentManager().findFragmentById(R.id.map);

                Fragment newOne = new NewPlace();
                newOne.setArguments(args);

                getFragmentManager().beginTransaction().remove(f).commit();
                getFragmentManager().beginTransaction().replace(R.id.fragmentSpace, newOne).commit();
            }
        });

    //set an on click listener for the floormakers.
    //On clicking, previous markers will be removed
    // Will show only those present on that floor

    View.OnClickListener floormakers = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeAllMarkers();
                String input = new String(searchresult.toString());
                switch(v.getId()) {
                    case R.id.GroundFloor: {
                        for (int i = 0; i < place.length; i++) {
                            if(place[i].floor != null && place[i].floor.contentEquals("Ground"))
                            {
                                if(input != null && input != "" && (place[i].title.toLowerCase().contains(input) || place[i].subtitle.toLowerCase().contains(input)))addMarker(i);
                                else if (input == null || input == "") addMarker(i);
                            }
                        }
                        break;
                    }
                    case R.id.FirstFloor: {
                        for (int i = 0; i < place.length; i++) {

                            if(place[i].floor != null && input != "" && place[i].floor.contentEquals("First"))
                            {
                                if(input != null && (place[i].title.toLowerCase().contains(input) || place[i].subtitle.toLowerCase().contains(input)))addMarker(i);
                                else if (input == null || input == "")addMarker(i);
                            }
                        }
                        break;
                    }
                    case R.id.SecondFloor: {
                        for (int i = 0; i < place.length; i++) {

                            if(place[i].floor != null &&input != "" &&  place[i].floor.contentEquals("Second"))
                            {
                                if(input != null && input != "" &&  (place[i].title.toLowerCase().contains(input) || place[i].subtitle.toLowerCase().contains(input))) addMarker(i);
                                else if (input == null || input == "")addMarker(i);
                             }
                        }
                        break;
                    }
                    case R.id.ThirdFloor: {
                        for (int i = 0; i < place.length; i++) {

                            if (place[i].floor != null && input != "" && place[i].floor.contentEquals("Third"))
                            {
                                if(input != null && (place[i].title.toLowerCase().contains(input) || place[i].subtitle.toLowerCase().contains(input))) addMarker(i);
                                else if (input == null || input == "")addMarker(i);
                            }
                        }
                        break;
                    }
                    case R.id.Clear: {
                        if (input != "") 
                        {
                            removeAllMarkers();
                            searchBox.setText("");
                            searchresult = new String("");
                        }
                        break;
                    }
                    case R.id.All: {
                        for(int i = 0;i < place.length;i++)
                        {
                            if(input != null && input != "" && (place[i].title.toLowerCase().contains(input) || place[i].subtitle.toLowerCase().contains(input))) addMarker(i);
                            else if (input == null || input == "") addMarker(i);
                        }
                        break;
                    }


                }
            }
        };

    ground_floor.setOnClickListener(floormakers);
    first_floor.setOnClickListener(floormakers);
    second_floor.setOnClickListener(floormakers);
    third_floor.setOnClickListener(floormakers);
    clear.setOnClickListener(floormakers);
    all.setOnClickListener(floormakers);

    findWC.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            dMap.clear();
            for(int i = 0; i < place.length; i++)
            {
                if(place[i].title.toLowerCase().contains("water cooler"))
                {
                    addMarker(i);
                }
            }
            searchresult = new String("water cooler");
        }
    });

    findWR.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            dMap.clear();
            for(int i = 0; i < place.length; i++)
            {
                if(place[i].title.toLowerCase().contains("washroom"))
                {
                    addMarker(i);


                }
            }

            searchresult = new String("washroom");
        }
    });
    view.setFocusableInTouchMode(true);
    view.setOnKeyListener(new View.OnKeyListener() {
        @Override
        public boolean onKey(View view, int i, KeyEvent keyEvent) {

            if (i == KeyEvent.KEYCODE_BACK) {
                if (isContributing) {
                    addplaces = new Button(getActivity());
                    addplaces.setLayoutParams(addButton.getLayoutParams());
                    addplaces.setBackgroundColor(Color.parseColor("#e02828"));
                    addplaces.setTextColor(Color.parseColor("#ffffff"));
                    addplaces.setText(R.string.contribute_places);
                    addplaces.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            removeAllMarkers();
                            isContributing = true;
                            cmark = new MarkerOptions().position(coords).title("Your New Marker");
                            drag = dMap.addMarker(new MarkerOptions().position(coords).title("Your New Marker").draggable(true));

                            Toast toast = Toast.makeText(getActivity().getApplicationContext(),"Long press to reposition marker",Toast.LENGTH_SHORT);
                            toast.show();


                            addButton.setLayoutParams(addplaces.getLayoutParams());
                            addButton.setBackgroundColor(Color.parseColor("#000000"));
                            addButton.setTextColor(Color.parseColor("#ffffff"));
                            addButton.setText("Add Properties");
                            fl.addView(addButton);
                        }
                    });
                    fl.addView(addplaces);
                    fl.removeView(addButton);
                    dMap.clear();
                    isContributing = false;
                    return true;
                }

            }
            return false;
        }
    });

        return view;
    }



    public void addMarker(int i)
    {
//        MarkerOptions m2 = new MarkerOptions().position(new LatLng(place[i].latitude,place[i].longitude)).title(place[i].title).snippet(String.format("%s,\r%s Floor\r", place[i].subtitle, place[i].floor));
        MarkerOptions m = new MarkerOptions().position(new LatLng(place[i].latitude,place[i].longitude)).title(place[i].title).snippet(String.format("%s,\r%s Floor\r", place[i].subtitle, place[i].floor));
        if(place[i].title.toLowerCase().contains("washroom"))
        {
            if (place[i].title.toLowerCase().contains("gents"))
            m.icon(BitmapDescriptorFactory.fromResource(R.drawable.toilets));

            else m.icon(BitmapDescriptorFactory.fromResource(R.drawable.toilets2));


        }
        if(place[i].title.toLowerCase().contains("water"))
        {
            m.icon(BitmapDescriptorFactory.fromResource(R.drawable.drinkingwater));
        }
        dMap.addMarker(m);
    }

    //parses json data and transfers to array
    public void createDataset()
    {

        JSONObject object = null;
        JSONObject iObject;
        JSONArray jsonArray = new JSONArray();

        try {
            object = new JSONObject(loadJSONFromAsset());
            iObject = new JSONObject();
            jsonArray = object.getJSONArray("map");
            boolean hasMap = object.has("map");


        } catch (JSONException e) {
            e.printStackTrace();
        }

//        Log.e("JsonArray Length",Integer.toString(jsonArray.length()));
          place = new location[jsonArray.length()];
        for (int i = 0; i < jsonArray.length(); i++) {

            place[i] = new location();
            try {
                iObject = jsonArray.getJSONObject(i);
                place[i] = new location();

                place[i].title = iObject.getString("title");
                place[i].subtitle = iObject.getString("subtitle");
                place[i].floor = iObject.getString("floor");
                place[i].latitude = Double.parseDouble(iObject.getString("latitude"));
                place[i].longitude = Double.parseDouble(iObject.getString("longitude"));
                addMarker(i);
//                dMap.addMarker(new MarkerOptions().position(new LatLng(place[i].latitude,place[i].longitude)).title(place[i].title).snippet(String.format("%s,\n%s Floor\n",place[i].subtitle,place[i].floor)));

            } catch (JSONException e) {
                e.printStackTrace();
                Log.e("Not","possible");
            }

        }
    }


    public String loadJSONFromAsset() {
        String json = null;
        try {

            InputStream is = getActivity().getAssets().open("mapdata2.json");

            int size = is.available();

            byte[] buffer = new byte[size];

            is.read(buffer);

            is.close();

            json = new String(buffer, "UTF-8");


        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;

    }

    public void removeAllMarkers()
    {
        dMap.clear();
    }






}

