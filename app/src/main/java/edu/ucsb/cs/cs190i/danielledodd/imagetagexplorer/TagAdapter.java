package edu.ucsb.cs.cs190i.danielledodd.imagetagexplorer;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.ArrayList;

/**
 * Created by Danielle on 5/15/2017.
 */

public class TagAdapter extends RecyclerView.Adapter<TagAdapter.ViewHolder> {
    ArrayList<String> tags = new ArrayList<String>();;
    ViewHolder holder;
    ImageFragment fragment;
    @Override
    public TagAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ImageTagDatabaseHelper.GetInstance().Subscribe(new ImageTagDatabaseHelper.OnDatabaseChangeListener() {
            @Override
            public void OnDatabaseChange() {
                notifyDataSetChanged();
            }
        });
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_tag_item, parent, false);
        holder = new TagAdapter.ViewHolder(view);

        return holder;
    }
    public TagAdapter(ImageFragment i){
        fragment = i;
    }
    public void setData(ArrayList<String> newData){
        tags = newData;
        notifyDataSetChanged();
    }


    public void onBindViewHolder(final TagAdapter.ViewHolder holder, int position) {
        holder.button.setText(tags.get(position));
        final int i = position;
        holder.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImageTagDatabaseHelper.GetInstance().deleteTagByImageId(fragment.uri, holder.button.getText().toString());
                tags.remove(i);
                notifyDataSetChanged();
            }
        });
    }

    public int getItemCount(){
        return tags.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        Button button;

        public ViewHolder(View view) {
            super(view);
            button = (Button) view.findViewById(R.id.single_tag_button);

        }


    }
}
