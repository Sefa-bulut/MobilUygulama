package com.sefaozgur.haydisahayaap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sefaozgur.haydisahayaap.Adapter.MessageAdapterRecycler;
import com.sefaozgur.haydisahayaap.Model.Chat;
import com.sefaozgur.haydisahayaap.Model.Rating;
import com.sefaozgur.haydisahayaap.Model.Users;
import com.sefaozgur.haydisahayaap.Notification.SendNotification;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class MessageActivity extends AppCompatActivity {

    //dialog
    Dialog dialog;

    private TextView username;
    private ImageView userImageView;
    private EditText msgEditTxt;
    private ImageButton sendMsg,ratingBtn;
    //firebase
    private FirebaseUser firebaseUser;
    private DatabaseReference databaseReference;
    private String him_id;
    //Recycler view
    private RecyclerView recyclerView;
    private MessageAdapterRecycler messageAdapterRecycler;
    private List<Chat> chats;
    //send message methodunda da bilgiye erişebilmek için global tanımladık
    private Users users;
    String MyName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        //action barı gizledik şimdi yerine kendi dizayn ettiğimiz tool bar gözüküyor.
        getSupportActionBar().hide(); //null gelebilirmiş çökme olursa burayı kontrol et

        //Widget initialize
        username = findViewById(R.id.text_view_message_activity);
        userImageView = findViewById(R.id.image_view_message_activity);
        sendMsg = findViewById(R.id.image_btn_send_message_activity);
        msgEditTxt = findViewById(R.id.edit_text_message_activity);
        //rating button
        ratingBtn = findViewById(R.id.star_image_btn_message_activity);
        //dialog initialize
        dialog = new Dialog(this);

        //Recyclerview
        recyclerView = findViewById(R.id.recycler_view_message_activity);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        //tıkladığımız kişinin id'sini put extra ile aldık
        Intent intent = getIntent();
        him_id = intent.getStringExtra("him_id");

        //firebase işlemleri
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        //mevcut kullanıcı bilgilerini bir kez almak
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("MyUsers").child(firebaseUser.getUid()).child("username");
        databaseRef.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    System.out.println(task.getException().toString());
                }
                else {
                    //myUser = (Users) task.getResult().getValue();
                    MyName = (String) task.getResult().getValue();
                }
            }
        });

        databaseReference = FirebaseDatabase.getInstance().getReference("MyUsers").child(him_id);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //tıkladığımız kişinin id'sini put extra ile aldık
                //şimdi onun bilgilerini çekip tool bar'a yansıtıcaz
                users = snapshot.getValue(Users.class);
                assert users != null;
                username.setText(users.getUsername());
                //profile resim kontrolu
                if(users.getImageURL().equals("default")){
                    userImageView.setImageResource(R.mipmap.ic_launcher);
                }else {
                    //Picasso.get().load(users.getImageURL()).into(userImageView);
                    Picasso.get().load(users.getImageURL()).centerCrop().fit().into(userImageView);
                }

                //read messages
                readMessages(firebaseUser.getUid(), him_id);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MessageActivity.this,error.getMessage(),Toast.LENGTH_LONG).show();
            }

        });

        //send button click
        sendMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = msgEditTxt.getText().toString();
                if(!msg.equals("")){
                    //send message
                    sendMessage(firebaseUser.getUid(), him_id, msg);
                    //user null mı kontrol edilebilir
                    System.out.println("message acctivity token: " + users.getToken());
                    //bizim msgmız-bizim ismimiz-bizim idmiz-göndereceğimiz kişinin tokeni
                    new SendNotification(msg,MyName,users.getToken(),firebaseUser.getUid());
                } else {
                    Toast.makeText(MessageActivity.this,"Mesaj boş olamaz!",Toast.LENGTH_LONG).show();
                }

                msgEditTxt.setText("");
            }
        });

        //Rating Users
        ratingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //rating method
                openRatingDialog();
            }
        });

    }

    private void openRatingDialog() {
        dialog.setContentView(R.layout.layout_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        //widgets
        Button submit = dialog.findViewById(R.id.submit_button);
        RatingBar ratingBar = dialog.findViewById(R.id.rating_bar_custom_dialog);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int rate = Math.round(ratingBar.getRating());
                String ratingValue = String.valueOf(rate);
                //ekleme
                Rating rating = new Rating(firebaseUser.getUid(),ratingValue);
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Rating").child(him_id);
                reference.child("rate").child(firebaseUser.getUid()).setValue(rating);
                Toast.makeText(MessageActivity.this,ratingValue+" yıldız verdiniz.",Toast.LENGTH_LONG).show();
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    //Sender->şuan ki kullanıcı id'si,Receiver-> intentle gelen kullanıcı idsi, Message-> edittext e yazılan yazı
    public void sendMessage(String sender, String receiver, String message){

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("sender",sender);
        hashMap.put("receiver",receiver);
        hashMap.put("message",message);
        //hashMap.put("isSeen",false);

        reference.child("Chats").push().setValue(hashMap);

        //adding users to chat fragment: latest chats with contacts
        //son konuştuğumuz kişileri chat fragmentına eklemek için yeni bir tablo oluşturuyoruz
        DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("ChatList").child(firebaseUser.getUid()).child(him_id);
        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(!snapshot.exists()){
                    //yoksa
                    chatRef.child("id").setValue(him_id);

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MessageActivity.this,error.getMessage(),Toast.LENGTH_LONG).show();
            }
        });


        //(konuştuğumuz kişide de chat list oluşması için onun adına ekliyoruz)
        //yani birinden mesaj geldiği zaman chatlistemizde gözüküyor
        //bunu eklemeseydik sadece bizim mesaj attıklarımız chat listemizde gözükecekti bize mesaj atanlar gözükmeyecekti
        DatabaseReference test = FirebaseDatabase.getInstance().getReference("ChatList").child(him_id).child(firebaseUser.getUid());
        test.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                test.child("id").setValue(firebaseUser.getUid());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }


    public void readMessages(String myID, String userID){

        chats = new ArrayList<>();

        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("Chats");
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chats.clear();
                for (DataSnapshot dataSnapshot: snapshot.getChildren()){

                    Chat chat = dataSnapshot.getValue(Chat.class);

                    if(chat.getReceiver().equals(myID) && chat.getSender().equals(userID) ||
                            chat.getReceiver().equals(userID) && chat.getSender().equals(myID)){

                        chats.add(chat);
                    }

                    messageAdapterRecycler = new MessageAdapterRecycler(MessageActivity.this,chats);
                    recyclerView.setAdapter(messageAdapterRecycler);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MessageActivity.this,error.getMessage(),Toast.LENGTH_LONG).show();
            }
        });


    }

    private void changeStatus(String status){
        databaseReference = FirebaseDatabase.getInstance().getReference("MyUsers").child(firebaseUser.getUid());

        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("status",status);

        databaseReference.updateChildren(hashMap);
    }

    //one signal bildirim kontrolu
    //şuan konuştuğum kişinin id sini sharedPreferences a kayıt etme
    private void currentPerson(String himID){
        SharedPreferences.Editor editor = getSharedPreferences("PREFS",MODE_PRIVATE).edit();
        editor.putString("himID",himID);
        //editor.apply(); asekron gerçekleşiyor
        editor.commit(); //sekron gerçekleşiyor

    }

    @Override
    protected void onResume() {
        super.onResume();
        changeStatus("online");
        currentPerson(him_id);
    }

    @Override
    protected void onPause() {
        super.onPause();
        changeStatus("offline");
        currentPerson("none");
    }


}