package com.sefaozgur.haydisahayaap.View;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sefaozgur.haydisahayaap.Adapter.UserAdapterRecycler;
import com.sefaozgur.haydisahayaap.Model.ChatList;
import com.sefaozgur.haydisahayaap.Model.Users;
import com.sefaozgur.haydisahayaap.R;

import java.util.ArrayList;
import java.util.List;


public class ChatsFragment extends Fragment {

    //--------Userların Arasından Sadece Konuştuklarımızın Bulunduğu Fragment(Yani sohbet kısmı)---------

    private RecyclerView recyclerView;
    private UserAdapterRecycler userAdapterRecycler;
    private List<Users> mUsers;
    private List<ChatList> userChatLists;
    //firebase
    private FirebaseUser firebaseUser;
    private DatabaseReference databaseReference;

    public ChatsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        //one signal
        //OneSignal.disablePush(false);
        return inflater.inflate(R.layout.fragment_chats, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recycler_view_chat_fragment);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        userChatLists = new ArrayList<>();

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference("ChatList").child(firebaseUser.getUid());
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userChatLists.clear();
                //loop for all users
                for (DataSnapshot snaps : snapshot.getChildren()){
                    //burada konuştuğunuz kişileri listeliyoruz
                    ChatList chatList = snaps.getValue(ChatList.class);
                    userChatLists.add(chatList);
                }
                //bununla da o kişilerin bilgilerini çekerek satır satır gösteriyoruz
                getChatList();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(),error.getMessage(),Toast.LENGTH_LONG).show();
            }
        });


    }


    public void getChatList(){
        //getting all  recent chats
        mUsers = new ArrayList<>();
        databaseReference = FirebaseDatabase.getInstance().getReference("MyUsers");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mUsers.clear();
                for (DataSnapshot snapshot1 : snapshot.getChildren()){
                    Users users = snapshot1.getValue(Users.class);
                    for (ChatList usrChtLst: userChatLists){
                        //MyUsers tablosundaki tüm userları değilde
                        //konuştuklarımızın bulunduğu userları user listesine ekliyoruz
                        if(users.getId().equals(usrChtLst.getId())){
                            mUsers.add(users);
                        }

                    }
                }
                //yeni bir adapter yerine
                //rehber kısmında kullandığımız adapterin aynısını kullandık
                userAdapterRecycler = new UserAdapterRecycler(getContext(),mUsers,true);
                recyclerView.setAdapter(userAdapterRecycler);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //Toast.makeText(getContext(),error.getMessage(),Toast.LENGTH_LONG).show();
                Toast.makeText(getActivity(),error.getMessage(),Toast.LENGTH_LONG).show();
            }
        });

    }

}