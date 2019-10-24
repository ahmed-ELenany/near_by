package com.example.nearby.listViewSearch;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.nearby.R;
import com.squareup.picasso.Picasso;


public class CustomListViewAdapter extends RecyclerView.Adapter<CustomListViewAdapter.viewHolder> {

    private final Context context;
    private final DataList datalist;
    String layoutType;

    public CustomListViewAdapter(Context c, DataList datalist) {
        this.datalist = datalist;
        this.layoutType = layoutType;
        this.context = c;

    }


    @Override
    public viewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;
        itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.venues_data_row, parent, false);
        return new viewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final viewHolder holder, final int position) {
        holder.TvName.setText(datalist.name.get(position));
        holder.TvAddress.setText(datalist.address.get(position));
//        Log.d("qawsedr",datalist.photo.get(position)+"");
        //***=
        if(datalist.photo.size()>0){
            Picasso.with(context).load(datalist.photo.get(position)).placeholder(R.drawable.picture).fit().into(holder.image);
        }
        //****

    }

    //**********************************************************************************************************
    @Override
    public int getItemCount() {
        return datalist.id.size();
    }

    public class viewHolder extends RecyclerView.ViewHolder {
        public final TextView TvName;
        public final TextView TvAddress;
        public final ImageView image;
        public viewHolder(View itemView) {
            super(itemView);
            TvName = itemView.findViewById(R.id.TvName);
            TvAddress = itemView.findViewById(R.id.TvAddress);
            image = itemView.findViewById(R.id.photo);

        }
    }
}
