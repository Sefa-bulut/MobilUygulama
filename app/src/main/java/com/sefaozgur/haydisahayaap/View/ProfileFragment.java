package com.sefaozgur.haydisahayaap.View;


import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.onesignal.OneSignal;
import com.sefaozgur.haydisahayaap.Model.Users;
import com.sefaozgur.haydisahayaap.R;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

public class ProfileFragment extends Fragment {

    //Widgets
    private TextView userNameTextView,notifyText;
    private ImageView userImageView;
    private Button uploadButton;
    private Switch notificationSwitch;
    //Firebase
    private DatabaseReference reference;
    private FirebaseUser firebaseUser;

    //Profile image
    private Bitmap selectedImage;
    private Uri imageUri;
    private StorageReference storageReference;

    //notification on/off
    String MY_PREFS_NAME = "NotificationSetting";
    String saveKey = "NotifyKey";
    String stringToSave;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //widgets
        userNameTextView = view.findViewById(R.id.user_name_text_view_profile_fragment);
        userImageView = view.findViewById(R.id.image_view_profile_fragment);
        uploadButton = view.findViewById(R.id.upload_button_profile_fragment);
        notificationSwitch = view.findViewById(R.id.notification_switch);
        notifyText = view.findViewById(R.id.notification_text_view);

        //firebase
        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("MyUsers").child(firebaseUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                Users users = snapshot.getValue(Users.class);
                userNameTextView.setText(users.getUsername());

                if(users.getImageURL().equals("default")){
                    if(imageUri==null){
                        userImageView.setImageResource(R.mipmap.ic_launcher);
                    }
                }else {
                    if (imageUri==null){
                        //Picasso.get().load(users.getImageURL()).into(userImageView);
                        Picasso.get().load(users.getImageURL()).centerCrop().fit().into(userImageView);
                    }
                }

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(),error.getMessage(),Toast.LENGTH_LONG).show();
            }
        });

        //select image
        userImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage(v);
            }
        });

        //upload image
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage();
            }
        });

        //--------------------------Notification Settings-------------------

        // To save data to SP
        SharedPreferences.Editor editor = getContext().getSharedPreferences(MY_PREFS_NAME,Context.MODE_PRIVATE).edit();
        //editor.putString(saveKey, stringToSave);
        //editor.apply();

        // To load the data at a later time
        SharedPreferences prefs = getContext().getSharedPreferences(MY_PREFS_NAME,Context.MODE_PRIVATE);
        String loadedString = prefs.getString(saveKey,"on");

        if (loadedString.matches("on")){
            notifyText.setText("Bildirimler Açık");
            notificationSwitch.setChecked(true);
        }else {
            notifyText.setText("Bildirimler Kapalı");
            notificationSwitch.setChecked(false);
        }
        //Notification switch on/off
        notificationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    stringToSave = "on";
                    notifyText.setText("Bildirimler Açık");
                    OneSignal.disablePush(false);//bildirime aç
                    Toast.makeText(getContext(),"açık",Toast.LENGTH_SHORT).show();
                }else {
                    stringToSave = "off";
                    notifyText.setText("Bildirimler Kapalı");
                    OneSignal.disablePush(true); //bildirime kapat
                    Toast.makeText(getContext(),"kapalı",Toast.LENGTH_SHORT).show();
                }
                editor.putString(saveKey, stringToSave);
                editor.apply();

            }
        });

    }

    public void uploadImage(){
        //progress dialog
        ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Resim Yükleniyor...");
        progressDialog.show();
        //image name ayarlama her resim aynı adla kaydolmasın diye
        UUID uuid = UUID.randomUUID();
        String imageName = "images/"+uuid+".jpg";

        if (imageUri != null){
            //reduce the size of image
            StorageReference childRef2 = FirebaseStorage.getInstance().getReference(imageName);
            Bitmap bmp = null;
            try {
                bmp = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.JPEG, 5, baos);
            byte[] data = baos.toByteArray();
            //uploading the image
            UploadTask uploadTask2 = childRef2.putBytes(data);
            uploadTask2.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    //download url (kaydedilen resmin adresini bulmak için)
                    StorageReference newStorageReference = FirebaseStorage.getInstance().getReference(imageName);
                    newStorageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String downloadURL = uri.toString();
                            //image url save to profile
                            DatabaseReference reference2 = FirebaseDatabase.getInstance().getReference("MyUsers").child(firebaseUser.getUid());
                            HashMap<String,Object> map = new HashMap<>();
                            map.put("imageURL",downloadURL);
                            reference2.updateChildren(map);
                            //progress dialog dismiss
                            imageUri = null;
                            Toast.makeText(getContext(),"Resim yüklendi!",Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getContext(),e.getLocalizedMessage(),Toast.LENGTH_LONG).show();
                            progressDialog.dismiss();
                        }
                    });

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getContext(),e.getLocalizedMessage(),Toast.LENGTH_LONG).show();
                    progressDialog.dismiss();
                }
            });
        }
    }

    public void selectImage(View view){
        //permission
        if(ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
        }else{
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent,2);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {
            if (ContextCompat.checkSelfPermission(getContext(),Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent,2);
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 2 && resultCode == getActivity().RESULT_OK && data != null) {
            imageUri = data.getData();
            try {
                if (Build.VERSION.SDK_INT >= 28) {
                    ImageDecoder.Source source = ImageDecoder.createSource(getActivity().getContentResolver(),imageUri);
                    selectedImage = ImageDecoder.decodeBitmap(source);
                } else {
                    selectedImage = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(),imageUri);
                }
                //userImageView.setImageBitmap(selectedImage);
                Picasso.get().load(imageUri).centerCrop().fit().into(userImageView);

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        super.onActivityResult(requestCode, resultCode, data);
    }


}