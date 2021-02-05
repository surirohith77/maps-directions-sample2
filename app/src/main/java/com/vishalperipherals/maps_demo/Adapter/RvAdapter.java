package com.vishalperipherals.maps_demo.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.vishalperipherals.maps_demo.R;
import com.vishalperipherals.maps_demo.models.Fhandler;

import java.util.List;

public class RvAdapter extends RecyclerView.Adapter<RvAdapter.MYVIEWHOLDER> {

    List<Fhandler> list;

    public RvAdapter(List<Fhandler> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public MYVIEWHOLDER onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_design,parent,false);
       return new MYVIEWHOLDER(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MYVIEWHOLDER holder, int position) {

        Fhandler fhandler = list.get(position);
        Picasso.get().load(fhandler.getImage()).into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class MYVIEWHOLDER extends RecyclerView.ViewHolder {
        ImageView imageView ;
        public MYVIEWHOLDER(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.imageview);
        }
    }


}
