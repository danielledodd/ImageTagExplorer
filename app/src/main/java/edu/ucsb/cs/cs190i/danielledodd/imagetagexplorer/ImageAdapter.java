package edu.ucsb.cs.cs190i.danielledodd.imagetagexplorer;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;

/**
 * Created by Danielle on 5/12/2017.
 */
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by holl on 5/5/17.
 */

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder>{

    ArrayList<String> data = new ArrayList<>();

    ViewHolder holder;
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ImageTagDatabaseHelper.GetInstance().Subscribe(new ImageTagDatabaseHelper.OnDatabaseChangeListener() {
            @Override
            public void OnDatabaseChange() {

                notifyDataSetChanged();
            }
        });
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_item, parent, false);
        holder = new ViewHolder(view);
        return holder;
    }

    public void clear(){
        data = new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setData(ArrayList<String> newData){
        data = newData;
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

       Uri uri = Uri.parse(data.get(position));

        Picasso.with(holder.imageView.getContext()).load(uri).into(holder.imageView);
        final int i = position;
        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("camera fragment uri", data.get(i));
                FragmentManager fm = ((MainActivity)holder.imageView.getContext()).getSupportFragmentManager();
                ImageFragment fragment = new ImageFragment();
                // Show DialogFragment
                Bundle b = new Bundle();
                b.putString("file name", data.get(i));
                fragment.setArguments(b);
                fragment.show(fm, "Dialog Fragment");

            }
        });
    }


    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        private int id = 0;
        public ViewHolder(View view) {
            super(view);
            imageView = (ImageView) view.findViewById(R.id.recycler_single_image);
            id += 1;
        }

        public String toString() {
            return "" + id;
        }

    }
}