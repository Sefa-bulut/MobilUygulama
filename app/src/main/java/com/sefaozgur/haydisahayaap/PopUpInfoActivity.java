package com.sefaozgur.haydisahayaap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sefaozgur.haydisahayaap.Model.CheckIn;
import com.sefaozgur.haydisahayaap.Model.Rating;

import java.text.SimpleDateFormat;
import java.util.Date;

public class PopUpInfoActivity extends AppCompatActivity {

    private TextView nameTxt,phoneTxt,dateTxt,positionTxt;
    private Button phoneButton,messageButton;
    private RatingBar ratingBar;
    //firebase
    private FirebaseUser firebaseUser;
    String id;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pop_up_info);

        //--------Set popup view start--------
        /*
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        getWindow().setLayout((int)(width*.8),(int)(height*.6)); //burdaki katsayıları oynıyarak boyut ayarlanabilir
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.gravity = Gravity.CENTER;
        params.x = 0;
        params.y = -20;
        getWindow().setAttributes(params);
         */
        //--------Set popup view end--------

        //firebase işlemleri
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        //Map sayfasından id bilgisini alma
        Intent intent = getIntent();
        id = intent.getStringExtra("id");
        //System.out.println(id);

        //Widgets initialize
        nameTxt = findViewById(R.id.txtViewUserNameInfo);
        dateTxt = findViewById(R.id.txtViewDateInfo);
        positionTxt = findViewById(R.id.txtViewPositionInfo);
        phoneTxt = findViewById(R.id.txtViewUserPhoneInfo);
        phoneButton = findViewById(R.id.call_btn_pop_up_info);
        messageButton = findViewById(R.id.message_btn_pop_up_info);
        ratingBar = findViewById(R.id.rating_bar_pop_up_info);
        ratingBar.setIsIndicator(true);

        //Get Data From Firebase
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("CheckIn").child(id);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                CheckIn checkIn = snapshot.getValue(CheckIn.class);

                nameTxt.setText(checkIn.getName());
                //------------date format start-------
                long lng = checkIn.getDate(); //long olarak kaydettik o yüzden long olarak çekiyoruz
                //timeStamp formatından normal tarihe çevirme(yani longdan normal tarihe çevirme oldu)
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");//İstenilen format buradan ayarlanabilir
                Date u_date = new Date(lng);//Date'in içine long bir değer verdik
                String appointmentDate =  simpleDateFormat.format(u_date);
                //-------------date format end-------
                dateTxt.setText(appointmentDate);
                positionTxt.setText(checkIn.getPosition());
                phoneTxt.setText(checkIn.getPhone());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //phone call
        phoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //format şu->tel:5225552332 yoksa çalışmıyor
                String telPhone = phoneTxt.getText().toString();
                Intent intentToPhone = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:+90" + telPhone));
                startActivity(intentToPhone);
            }
        });

        //intent to message activity
        messageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //aldığımız id yi message activitye paslıyoruz
                Intent intent = new Intent(PopUpInfoActivity.this,MessageActivity.class);
                intent.putExtra("him_id",id);
                startActivity(intent);
                finish();
            }
        });

        //get rating from firebase
        getUserRating();

    }

    private void getUserRating() {
        //okuma
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Rating");
        ref.child(id).child("rate").addValueEventListener(new ValueEventListener() {
            int count=0;
            int sum=0;
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    Rating rating = dataSnapshot.getValue(Rating.class);
                    sum+=Integer.parseInt(rating.getRateValue());
                    count++;
                }

                if(count!=0)
                {
                    int average= sum/count;
                    ratingBar.setRating(average);
                    System.out.println("ortalaması: "+average);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PopUpInfoActivity.this,error.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });


    }
}