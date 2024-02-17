package com.sefaozgur.haydisahayaap.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sefaozgur.haydisahayaap.MessageActivity;
import com.sefaozgur.haydisahayaap.Model.Users;
import com.sefaozgur.haydisahayaap.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class UserAdapterRecycler extends RecyclerView.Adapter<UserAdapterRecycler.ViewHolder> {

    //----Bu Adapter user_list_recyclerview'in adapter'i-----

    private Context context;
    private List<Users> mUsers;

    private boolean isChat;

    //Constructor
    public UserAdapterRecycler(Context context, List<Users> mUsers, boolean isChat) {
        this.context = context;
        this.mUsers = mUsers;
        this.isChat = isChat;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.recycler_user_list_row,parent,false);

        return new UserAdapterRecycler.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Users users = mUsers.get(position);

        holder.username.setText(users.getUsername());

        if(users.getImageURL().equals("default")){
            holder.imageView.setImageResource(R.mipmap.ic_launcher);
        }else {
            //Picasso.get().load(users.getImageURL()).into(holder.imageView);
            Picasso.get().load(users.getImageURL()).centerCrop().fit().into(holder.imageView);
        }

        //change status image (online->sarı,offline->gri)
        if(isChat){
            if(users.getStatus().equals("online")){
                holder.img_ON.setVisibility(View.VISIBLE);
                holder.img_OFF.setVisibility(View.GONE);
            } else {
                holder.img_OFF.setVisibility(View.VISIBLE);
                holder.img_ON.setVisibility(View.GONE);
            }
        } else {
            holder.img_ON.setVisibility(View.GONE);
            holder.img_OFF.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, MessageActivity.class);
                intent.putExtra("him_id",users.getId());
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public TextView username;
        public ImageView imageView;
        public ImageView img_ON;
        public ImageView img_OFF;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.recycler_user_list_text_view);
            imageView = itemView.findViewById(R.id.recycler_user_list_image_view);
            img_ON = itemView.findViewById(R.id.status_on_image_view_user_list_row);
            img_OFF = itemView.findViewById(R.id.status_off_image_view_user_list_row);

        }
    }
}


