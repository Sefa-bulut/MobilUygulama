package com.sefaozgur.haydisahayaap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignActivity extends AppCompatActivity {

    //------------------Launcher activity-------------------

    private FirebaseAuth firebaseAuth;
    private TextInputEditText emailText,passwordText;
    private TextView textViewRegister,logoName;
    private String text;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign);

        getSupportActionBar().hide();

        //initialize progress bar
        progressBar = findViewById(R.id.signInprogressBar);
        progressBar.setVisibility(View.GONE);

        logoName = findViewById(R.id.logo_name);
        firebaseAuth = FirebaseAuth.getInstance();
        emailText = findViewById(R.id.userEmail);
        passwordText = findViewById(R.id.userPassword);
        textViewRegister = findViewById(R.id.textViewRegister);
        text = "Hesabın yok mu? Kaydol";

        SpannableString ss = new SpannableString(text);

        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                //intent
                Intent intentToMain = new Intent(SignActivity.this,RegisterActivity.class);
                startActivity(intentToMain);
                finish();
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(Color.BLUE);
            }
        };

        ss.setSpan(clickableSpan,16,22, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        textViewRegister.setText(ss);
        textViewRegister.setMovementMethod(LinkMovementMethod.getInstance());

    }


    @Override
    protected void onStart() {
        //İçerde kullanıcı olup olmadığını kontrol ediyoruz
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        //check signed user
        if(currentUser!=null){
            //intent
            Intent intent = new Intent(SignActivity.this,MainActivity.class);
            startActivity(intent);
            finish();
        }
        super.onStart();
    }

    public void signIn(View view){

        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();

        if(email.isEmpty()){
            emailText.setError("Lütfen E-posta adresi giriniz!");
            emailText.requestFocus();
        }
        else if (password.isEmpty()){
            passwordText.setError("Lütfen şifre giriniz!");
            passwordText.requestFocus();
        }
        else if (!(email.isEmpty() && password.isEmpty())){
            //tüm alanlar dolu ise
            progressBar.setVisibility(View.VISIBLE); //start progress bar
            firebaseAuth.signInWithEmailAndPassword(email,password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                @Override
                public void onSuccess(AuthResult authResult) {
                    Intent intent1 = new Intent(SignActivity.this, MainActivity.class);
                    startActivity(intent1);
                    finish();
                    progressBar.setVisibility(View.GONE); //finish progress bar
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(SignActivity.this,e.getLocalizedMessage(),Toast.LENGTH_LONG).show();
                    progressBar.setVisibility(View.GONE); //finish progress bar
                }
            });

        }
        else {
            Toast.makeText(SignActivity.this,"Bir hata oluştu!",Toast.LENGTH_LONG).show();
        }


    }

}