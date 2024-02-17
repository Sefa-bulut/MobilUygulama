package com.sefaozgur.haydisahayaap.Notification;

import com.onesignal.OneSignal;

import org.json.JSONException;
import org.json.JSONObject;

public class SendNotification {

    private static final String TAG = "error" ;

    //burdaki notification key dediğimiz göndereceğimiz kişinin tokeni
    // myId dedidğimiz kendi idmiz
    //myId'yı datanın içindeki key'ye atıyoruz (yani key'imiz key, value'muz myId)

    //constructor
    public SendNotification(String message, String heading, String notificationKey, String myId) {
        try {
            OneSignal.postNotification(new JSONObject("{'include_player_ids': ['" + notificationKey + "']," +
                    "'headings': {'en': '" + heading + "'}," +
                    "'contents': {'en': '" + message + "'}," +
                    "'data': {'key': '" + myId + "'}," +
                    "'android_sound': 'nil'}"), null);
        } catch (JSONException e) {
            e.printStackTrace();
            System.out.println("sendNotificationError: "+e.getLocalizedMessage());
        }

    }

}
