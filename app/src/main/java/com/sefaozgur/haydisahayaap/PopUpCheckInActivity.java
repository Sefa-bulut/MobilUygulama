package com.sefaozgur.haydisahayaap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sefaozgur.haydisahayaap.Model.CheckIn;
import com.sefaozgur.haydisahayaap.Model.Users;

import java.util.ArrayList;
import java.util.Calendar;

public class PopUpCheckInActivity extends AppCompatActivity {

    //widgets
    private TextView userNameTxt,showDateTxt;
    private Button pickDateBtn,cancel,checkIn;
    private ProgressBar progressBar;
    private Spinner spinner;
    //date time picker
    private DatePickerDialog datePickerDialog;
    private int gun,yil,ay;
    private long ts; //appointment date
    //firebase
    private FirebaseUser firebaseUser;
    private DatabaseReference databaseReference;
    //values
    private String name,phone,userPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pop_up_check_in);

        //firebase initialize
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference("MyUsers").child(firebaseUser.getUid());

        //get intent
        Intent intent = getIntent();
        double latitude = intent.getDoubleExtra("latitude",0.0);
        double longitude = intent.getDoubleExtra("longitude",0.0);

        //widgets initialize
        progressBar = findViewById(R.id.pop_up_progress_bar);
        progressBar.setVisibility(View.GONE);
        userNameTxt = findViewById(R.id.user_name_txt_pop_up_check_in);
        showDateTxt = findViewById(R.id.show_date_txt_view);
        cancel = findViewById(R.id.cancel_btn_pop_up_check_in);
        checkIn = findViewById(R.id.check_in_btn_pop_up_check_in);
        checkIn.setVisibility(View.INVISIBLE); //To set invisible (butonu görünmez yap)
        //Spinner
        spinner = findViewById(R.id.spinner);
        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add("Farketmez");
        arrayList.add("Kaleci");
        arrayList.add("Defans");
        arrayList.add("Orta saha");
        arrayList.add("Forvet");
        arrayList.add("Sağ açık");
        arrayList.add("Sol açık");
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,arrayList);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(arrayAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                userPosition = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //date time picker
        pickDateBtn = findViewById(R.id.pick_date);
        pickDateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final Calendar c = Calendar.getInstance();
                int mYear = c.get(Calendar.YEAR); //current year
                int mMonth = c.get(Calendar.MONTH); //current month
                int mDay = c.get(Calendar.DAY_OF_MONTH); //current day

                datePickerDialog = new DatePickerDialog(PopUpCheckInActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        //istenilen tarih formatı burada ayarlanır
                        showDateTxt.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
                        //initialize
                        gun = dayOfMonth;
                        ay = month;
                        yil = year;
                        //tarihi seçilen tarihe göre ayarlıyoruz
                        Calendar selected_date = Calendar.getInstance();
                        selected_date.set(Calendar.DAY_OF_MONTH,gun);
                        selected_date.set(Calendar.MONTH,ay);
                        selected_date.set(Calendar.YEAR,yil);
                        selected_date.set(Calendar.HOUR_OF_DAY,0);
                        selected_date.set(Calendar.MINUTE,1);
                        selected_date.set(Calendar.SECOND,0);

                        //seçilen tarihi getTimeInMillis diyerek long biçiminde aldık ve bunu global long değişkenimize attık
                        System.out.println("seçilen tarih: " + selected_date.getTimeInMillis());//bu değer long
                        ts = selected_date.getTimeInMillis();

                        //ayarlanan tarihi timestamp formatına çeviriyoruz
                        //time_stamp = new Timestamp(selected_date.getTime());
                    }
                }, mYear, mMonth, mDay);
                datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000); //disable previous dates
                datePickerDialog.show();

            }
        });

        //get data
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Users users = snapshot.getValue(Users.class);
                name = users.getUsername();
                phone = users.getUserPhone();
                userNameTxt.setText(name);
                checkIn.setVisibility(View.VISIBLE); //To set visible (butonu görünür yap)
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PopUpCheckInActivity.this,error.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });

        //save to firebase button
        checkIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String appointmentDate = showDateTxt.getText().toString();
                if (appointmentDate.matches("")){
                    Toast.makeText(PopUpCheckInActivity.this,"Lütfen bir tarih seçin!",Toast.LENGTH_LONG).show();
                }else {
                    saveFirebase(ts,latitude,longitude);
                }

            }
        });

        //cancel button
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    private void saveFirebase(long date,double lat,double lon) {
        progressBar.setVisibility(View.VISIBLE); //progress bar set visible

        CheckIn checkIn = new CheckIn(firebaseUser.getUid(),name,phone,userPosition,date,lat,lon);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("CheckIn");
        reference.child(firebaseUser.getUid()).setValue(checkIn).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                //BAŞARILIYSA
                Toast.makeText(PopUpCheckInActivity.this,"Check-in oluşturuldu.Unutma belirlediğin gün geçtiğinde oluşturduğun check-in kaybolacak.",Toast.LENGTH_LONG).show();
                progressBar.setVisibility(View.GONE); //progress bar
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //BAŞARISIZ OLDUYSA
                Toast.makeText(PopUpCheckInActivity.this,e.getLocalizedMessage(),Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE); //progress bar
            }
        });

    }


}