package com.example.bizcardandroid;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder>{

    private static final String TAG = "RecyclerViewAdapter";

    private Context context;
    private ArrayList<Contact> contacts;

    RecyclerViewAdapter(Context context, ArrayList<Contact> contacts) {
        this.context = context;
        this.contacts = contacts;
    }


    @NonNull
    @Override
    public RecyclerViewAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.contact_layout,
                viewGroup,false);
        MyViewHolder vh = new MyViewHolder(view);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {
        Log.d(TAG,"onBindViewHolder: called");

        if(!contacts.isEmpty()) {
            Picasso.with(context).load(contacts.get(i).getImgURL()).noFade().into(myViewHolder.image);
            myViewHolder.textViewName.setText(contacts.get(i).getName());
            myViewHolder.textViewCompany.setText(contacts.get(i).getCompany());
        }

        myViewHolder.relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Clicked");
            }
        });
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {

        CircleImageView image;
        TextView textViewName;
        TextView textViewCompany;
        RelativeLayout relativeLayout;

        MyViewHolder(View v) {

            super(v);
            image = v.findViewById(R.id.profile_image);
            textViewName = v.findViewById(R.id.name_text);
            textViewCompany = v.findViewById(R.id.company_text);
            relativeLayout = v.findViewById(R.id.contactRelativeLayout);

        }
    }
}
