package com.sefaozgur.haydisahayaap.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.sefaozgur.haydisahayaap.Model.Chat;
import com.sefaozgur.haydisahayaap.R;

import java.util.List;

public class MessageAdapterRecycler extends RecyclerView.Adapter<MessageAdapterRecycler.myViewHolder> {

    private Context context;
    private List<Chat> mChat;

    //Firebase
    FirebaseUser firebaseUser;

    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;

    //Constructor
    public MessageAdapterRecycler(Context context, List<Chat> mChat) {
        this.context = context;
        this.mChat = mChat;
    }

    @NonNull
    @Override
    public myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view;
        if(viewType == MSG_TYPE_RIGHT){
            view = LayoutInflater.from(context).inflate(R.layout.recycler_chat_message_right, parent, false);
        }else {
            view = LayoutInflater.from(context).inflate(R.layout.recycler_chat_message_left, parent, false);
        }
        return new myViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull myViewHolder holder, int position) {

        Chat chat = mChat.get(position);

        holder.showMessage.setText(chat.getMessage());

    }

    @Override
    public int getItemCount() {
        return mChat.size();
    }


    public class myViewHolder extends RecyclerView.ViewHolder{

        //tek bir text view tanımladık
        public TextView showMessage;
        //tek bir seen txt tanımladık(görüldü)
        //public TextView  text_seen;

        public myViewHolder(@NonNull View itemView) {
            super(itemView);
            //her iki recycler view row u içinde text view'a aynı id'yi verdik
            showMessage = itemView.findViewById(R.id.recycler_chat_message_text_view);
            //bu yüzden sadece bir tane showMessage text view'mız var

        }
    }

    @Override
    public int getItemViewType(int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(mChat.get(position).getSender().equals(firebaseUser.getUid())){
            //yani gönderilen mesajları sağa yasla
            return MSG_TYPE_RIGHT;
        }else{
            //alınan mesajları sola yasla
            return MSG_TYPE_LEFT;
        }
    }


}


