package com.sefaozgur.haydisahayaap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.onesignal.OSDeviceState;
import com.onesignal.OneSignal;
import com.sefaozgur.haydisahayaap.Adapter.ViewPagerAdapter;
import com.sefaozgur.haydisahayaap.View.ChatsFragment;
import com.sefaozgur.haydisahayaap.View.MapFragment;
import com.sefaozgur.haydisahayaap.View.ProfileFragment;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    //firebase
    private FirebaseUser firebaseUser;
    private DatabaseReference myRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //firebase
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        //--------------one signal start-------------
        // get the data from SP
        SharedPreferences prefs = getSharedPreferences("NotificationSetting", Context.MODE_PRIVATE);
        String loadedString = prefs.getString("NotifyKey","on");
        //System.out.println(loadedString);

        if (loadedString.matches("on")){
            //System.out.println("if bloğu:"+loadedString);
            OneSignal.disablePush(false);
        }

        //One signal - user device id (yani token diyebiliriz)
        OSDeviceState deviceState = OneSignal.getDeviceState();
        if (deviceState != null){
            String one_signal_my_token = deviceState.getUserId();
            oneSignalMyTokenAddFB(one_signal_my_token);
            System.out.println("noObserver: "+one_signal_my_token);
        }
        //--------------one signal end----------------

        //Tab layout and viewPager
        TabLayout tabLayout = findViewById(R.id.tab_layout);
        ViewPager viewPager = findViewById(R.id.view_pager);

        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());

        viewPagerAdapter.addFragment(new MapFragment(),"Harita");
        viewPagerAdapter.addFragment(new ChatsFragment(),"Sohbet");
        //viewPagerAdapter.addFragment(new UsersFragment(),"Users");
        viewPagerAdapter.addFragment(new ProfileFragment(),"Profil");


        viewPager.setAdapter(viewPagerAdapter);

        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.option_menu,menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(item.getItemId()==R.id.sign_out){
            //one signal
            OneSignal.disablePush(true);
            FirebaseAuth.getInstance().signOut();
            //finish yapmadık onun yerine flag clear top yaptık(finish yapsaydık uygulama çökebilirdi)
            Intent intent = new Intent(MainActivity.this,SignActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        changeStatus("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        changeStatus("offline");
    }

    private void changeStatus(String status){
        myRef = FirebaseDatabase.getInstance().getReference("MyUsers").child(firebaseUser.getUid());
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("status",status);
        myRef.updateChildren(hashMap);
    }

    //Token'i firebase'e ekleyen method(update token)
    private void oneSignalMyTokenAddFB(String token){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("MyUsers");
        reference.child(firebaseUser.getUid()).child("token").setValue(token);
    }

}