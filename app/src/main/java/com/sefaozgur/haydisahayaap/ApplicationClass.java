package com.sefaozgur.haydisahayaap;


import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;

import com.onesignal.OSNotification;
import com.onesignal.OSNotificationOpenedResult;
import com.onesignal.OSNotificationReceivedEvent;
import com.onesignal.OneSignal;

import org.json.JSONException;
import org.json.JSONObject;

public class ApplicationClass extends Application {

    private static final String ONESIGNAL_APP_ID = "edd630aa-e0ff-4da2-94e5-fce7eef6b8e2";
    //eski-> 4f1c5c6f-35f5-44be-9675-e013bfb777b7
    //yeni-> d912d2bb-977e-4492-8410-d4232d9cab68
    //en yeni-> 92f52848-b186-44b5-aec1-5476e772fedc

    @Override
    public void onCreate() {
        super.onCreate();

        //bu satır çalışmazsa yani application class o zaman bildirimlerde problem çıkabiliyor
        //ama aplication class çalışırsa problem çıkmıyor yani shared prefde sıkıntı yok
        System.out.println("application class çalışıyor");

        // Enable verbose OneSignal logging to debug issues if needed.
        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE);

        // OneSignal Initialization
        OneSignal.initWithContext(this);
        OneSignal.setAppId(ONESIGNAL_APP_ID);

        //---------------bildirim kontrol başlangıç-------------

        //SharedPreferences preferences = getSharedPreferences("PREFS",MODE_PRIVATE);
        //String test = preferences.getString("himID","none");
        //System.out.println("id: "+test);

        //bildirim gelince tetiklenir
        OneSignal.setNotificationWillShowInForegroundHandler(new OneSignal.OSNotificationWillShowInForegroundHandler() {
            @Override
            public void notificationWillShowInForeground(OSNotificationReceivedEvent notificationReceivedEvent) {

                System.out.println("showInforegroun çalışıyor");

                SharedPreferences preferences = getSharedPreferences("PREFS",MODE_PRIVATE);
                String test = preferences.getString("himID","none");
                System.out.println("id: "+test);

                OSNotification osNotification = notificationReceivedEvent.getNotification();
                JSONObject data = osNotification.getAdditionalData();
                try {
                    String himUserId1 = data.getString("key");

                    if(!test.equals(himUserId1)){
                        //konuştuğumuz kişi ile bildirim  gönderen kişi aynı değilse
                        notificationReceivedEvent.complete(osNotification);
                    }else {
                        //aynı kişiyse
                        System.out.println("aynı kişi");
                        notificationReceivedEvent.complete(null);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        //--------------bildirim kontrol bitiş----------------

        //Bildirime tıklanınca tetiklenir
        OneSignal.setNotificationOpenedHandler(new OneSignal.OSNotificationOpenedHandler() {
            @Override
            public void notificationOpened(OSNotificationOpenedResult result) {
                OSNotification notification = result.getNotification();
                JSONObject jsonObject = notification.getAdditionalData();
                try {
                    String himUserId = jsonObject.getString("key");
                    //System.out.println(himUserId);
                    //intent to message activity
                    Intent intent = new Intent(getApplicationContext(),MessageActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("him_id",himUserId);
                    startActivity(intent);

                } catch (JSONException e) {
                    e.printStackTrace();
                    System.out.println(e.getLocalizedMessage());
                }
            }
        });
    }

}

