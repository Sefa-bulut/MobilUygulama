package com.sefaozgur.haydisahayaap.View;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.sefaozgur.haydisahayaap.Model.CheckIn;
import com.sefaozgur.haydisahayaap.PopUpCheckInActivity;
import com.sefaozgur.haydisahayaap.PopUpInfoActivity;
import com.sefaozgur.haydisahayaap.R;

import java.util.Calendar;


public class MapFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LocationManager locationManager;
    private LocationListener locationListener;

    private static final int PERMISSION_FINE_LOCATION_REQUEST_CODE = 1;

    //enlem boylam
    private double latitude_send = 0.0;
    private double longitude_send = 0.0;

    //firebase
    private DatabaseReference databaseReference;

    public MapFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //initialize map fragment
        SupportMapFragment supportMapFragment =(SupportMapFragment)
                getChildFragmentManager().findFragmentById(R.id.google_map);
        supportMapFragment.getMapAsync(this::onMapReady);

        //image button
        ImageButton checkInButton = view.findViewById(R.id.check_in_btn_map_fragment);
        checkInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkIn();
            }
        });
    }

    //check-in button
    private void checkIn() {
        if (latitude_send == 0.0 || longitude_send == 0.0){
            Toast.makeText(getActivity(),"Konum bilginize şuanda erişilemiyor.",Toast.LENGTH_LONG).show();
        }else {
            Intent intent = new Intent(getActivity(), PopUpCheckInActivity.class);
            intent.putExtra("latitude",latitude_send);
            intent.putExtra("longitude",longitude_send);
            startActivity(intent);
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //get location data from firebase
        getDataFromFB();

        //marker click listener
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                String id = marker.getTitle();
                //intent to info popup activity
                Intent intentInfo = new Intent(getActivity(), PopUpInfoActivity.class);
                intentInfo.putExtra("id",id);
                startActivity(intentInfo);

                return false;
            }
        });


        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {

                if (latitude_send == 0.0 || longitude_send == 0.0){
                    System.out.println("enlem boylam boşsa");
                    //get user location
                    latitude_send = location.getLatitude();
                    longitude_send = location.getLongitude();
                    myMoveCamera(location.getLatitude(),location.getLongitude());
                }else {
                    //konum bilgileri boş değilse
                    System.out.println("enlem boylam boş değilse");
                    locationManager.removeUpdates(locationListener);//mapda bi çökme olursa bu satırı kaldırıp dene
                }

            }

            @Override
            public void onProviderEnabled(@NonNull String provider) {

            }

            @Override
            public void onProviderDisabled(@NonNull String provider) {

                AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                alert.setTitle("Konum Servisiniz Kapalı");
                alert.setMessage("Konum servisini açmak istermisiniz?");
                alert.setNegativeButton("Hayır", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getActivity(),"Konum servisini açmazsanız yer bildiriminde bulunamazsınız.",Toast.LENGTH_LONG).show();
                    }
                });
                alert.setPositiveButton("Evet", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //GPS TURN ON
                        Intent intent1 = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(intent1);
                    }
                });
                alert.show();
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }
        };

        updateGPS();

    }

    //get location data from firebase
    private void getDataFromFB() {
        //get current date
        Calendar currentCalendar = Calendar.getInstance();
        currentCalendar.add(Calendar.DATE,0);
        currentCalendar.set(Calendar.HOUR_OF_DAY,0);
        currentCalendar.set(Calendar.MINUTE,0);

        long lng = currentCalendar.getTimeInMillis();
        double currentDate = (double) lng;
        //-------------
        databaseReference = FirebaseDatabase.getInstance().getReference("CheckIn");
        //startat sadece double kabul edıyor bu yüzden longu double yaptık ve ona göre çekiyoruz
        Query lastDate = databaseReference.orderByChild("date").startAt(currentDate); //1617408060450.0
        lastDate.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                mMap.clear();
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    CheckIn checkIn = dataSnapshot.getValue(CheckIn.class);
                    //System.out.println("firebaseden çekilen tarih: " + checkIn.getDate());
                    double latitude = checkIn.getLatitude();
                    double longitude = checkIn.getLongitude();
                    String id = checkIn.getId();

                    LatLng latLng = new LatLng(latitude,longitude);
                    mMap.addMarker(new MarkerOptions().position(latLng).title(id));
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void myMoveCamera(double latitude,double longitude) {
        mMap.clear();
        LatLng userLocation = new LatLng(latitude,longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation,15));
        //mMap.addMarker(new MarkerOptions().position(userLocation).title("location listener"));
    }

    private void updateGPS(){
        //get permission
        if(ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            //user provided the permission (izin verildi)
            mMap.setMyLocationEnabled(true);//haritada konumumuza geri getiren düğme
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
            Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if(lastKnownLocation != null){
                //update location values
                latitude_send = lastKnownLocation.getLatitude();
                longitude_send = lastKnownLocation.getLongitude();
                mMap.clear();
                LatLng lastUserLocation = new LatLng(lastKnownLocation.getLatitude(),lastKnownLocation.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation,15));
                //mMap.addMarker(new MarkerOptions().position(lastUserLocation).title("lastKnownLocation"));
            }else{
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,30000,0,locationListener);//konum servisinden lokasyonu istiyoruz sonra bunu yukarıdaki location listenerda işliyoruz.
            }

        } else {
            //permission granted not yet (izin henüz verilmedi)
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},PERMISSION_FINE_LOCATION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode){
            case PERMISSION_FINE_LOCATION_REQUEST_CODE:
                if (grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    updateGPS();
                }else {
                    Toast.makeText(getActivity(),"Bu uygulamanın düzgün çalışması için izin verilmesi gerekiyor!",Toast.LENGTH_LONG).show();
                    getActivity().finish();
                }
                break;
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

}