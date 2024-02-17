package com.sefaozgur.haydisahayaap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;


public class RegisterActivity extends AppCompatActivity {
    //widgets
    private TextInputEditText registerUserName,editTextPhone,emailText,passwordText;
    private ImageView registerProfileIcon;
    private ProgressBar progressBar;
    private Button button;
    //firebase
    private FirebaseAuth firebaseAuth;
    private DatabaseReference myRef;
    //default phone
    private String phone;
    private String text;
    private TextView privacyPolicyTxtVw;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        getSupportActionBar().hide();

        //btn
        button = findViewById(R.id.buttonRegister);
        //initialize progress bar
        progressBar = findViewById(R.id.registerProgressBar);
        progressBar.setVisibility(View.GONE);

        registerProfileIcon = findViewById(R.id.register_profile_icon);
        registerUserName = findViewById(R.id.register_user_name);
        editTextPhone = findViewById(R.id.register_phone_number);
        emailText = findViewById(R.id.register_email);
        passwordText = findViewById(R.id.register_user_password);

        //firebase
        firebaseAuth = FirebaseAuth.getInstance();

        //privacy policy
        privacyPolicyTxtVw = findViewById(R.id.privacyPolicyTextView);
        text = "Kabul Et ve Kayıt ol'a tıklayarak, Gizlilik Politikasını kabul etmiş olursunuz.";
        SpannableString ss = new SpannableString(text);

        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                //Gizlilik sözleşmesinin bulunduğu sayfaya yönledirme yapıyor
                openWebPage("https://haydisahayaa.blogspot.com/p/privacy-policy.html");
                //https://haydisahayaa.blogspot.com/p/privacy-policy.html
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(Color.BLUE);
            }
        };

        ss.setSpan(clickableSpan,35,56, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        privacyPolicyTxtVw.setText(ss);
        privacyPolicyTxtVw.setMovementMethod(LinkMovementMethod.getInstance());
    }

    public void openWebPage(String url) {
        try {
            Uri webPage = Uri.parse(url);
            Intent myIntent = new Intent(Intent.ACTION_VIEW, webPage);
            startActivity(myIntent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Lütfen bir web tarayıcısı kurun veya URL'nizi kontrol edin.",  Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    //Register user
    public void register(View view){

        String userName = registerUserName.getText().toString();
        String userPhoneNumber = editTextPhone.getText().toString();
        String userEmail = emailText.getText().toString();
        String userPassword = passwordText.getText().toString();

        if(userName.isEmpty()){
            registerUserName.setError("Lütfen isminizi giriniz!");
            registerUserName.requestFocus();
        }
        else if (userEmail.isEmpty()){
            emailText.setError("Lütfen e-posta adresi giriniz!");
            emailText.requestFocus();
        }
        else if (userPassword.isEmpty()){
            passwordText.setError("Lütfen şifre giriniz");
            passwordText.requestFocus();
        }
        else if (userPassword.length() < 6){
            passwordText.setError("Şifre 6 haneden az olamaz!");
            passwordText.requestFocus();
        }
        else if (!(userName.isEmpty() && userEmail.isEmpty() && userPassword.isEmpty())){
            //tüm alanlar boş değilse yani tüm alanlar dolu ise

            //check phone text
            if (userPhoneNumber.isEmpty()){
                phone = "123456789"; //default phone
            }else {
                phone = userPhoneNumber;
            }
            //progress bar start
            progressBar.setVisibility(View.VISIBLE);

            firebaseAuth.createUserWithEmailAndPassword(userEmail,userPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    System.out.println("step1");
                    if(task.isSuccessful()){
                        System.out.println("step2");
                        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                        String userUUID = firebaseUser.getUid();

                        myRef = FirebaseDatabase.getInstance().getReference("MyUsers").child(userUUID);

                        //HashMaps
                        HashMap<String,String> hashMap = new HashMap<>();
                        hashMap.put("id",userUUID);
                        hashMap.put("username",userName);
                        hashMap.put("imageURL","default");
                        hashMap.put("status","offline");
                        hashMap.put("userPhone",phone);
                        System.out.println("step3");
                        myRef.setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                System.out.println("step4");//çalışmayan satır
                                Toast.makeText(RegisterActivity.this,"Kaydınız oluşturulmuştur.",Toast.LENGTH_SHORT).show();
                                //intent
                                Intent intent = new Intent(RegisterActivity.this,MainActivity.class);
                                startActivity(intent);
                                finish();
                                progressBar.setVisibility(View.GONE); //progress bar finish
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(RegisterActivity.this,e.getLocalizedMessage(),Toast.LENGTH_LONG).show();
                                progressBar.setVisibility(View.GONE); //progress bar finish
                            }
                        });

                    } else {
                        Toast.makeText(RegisterActivity.this,"Beklenmedik bir hata oluştu, lütfen daha sonra tekrar deneyiniz",Toast.LENGTH_LONG).show();
                        progressBar.setVisibility(View.GONE); //progress bar finish
                    }

                }
            });

        }
        else {
            Toast.makeText(RegisterActivity.this,"Lütfen boş alan bırakmayınız!",Toast.LENGTH_LONG).show();
        }


    }
}