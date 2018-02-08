package edu.ucsb.cs.cs190i.danielledodd.imagetagexplorer;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

/**
 * Created by Danielle on 5/15/2017.
 */

public class ImageFragment extends DialogFragment {
    TagAdapter tagAdapter;
    String uri;
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ImageTagDatabaseHelper.Initialize(getContext());

        final ImageTagDatabaseHelper db = ImageTagDatabaseHelper.GetInstance();

        View v = inflater.inflate(R.layout.image_fragment, container, false);
        final ImageView imageView = (ImageView) v.findViewById(R.id.dialog_image);
        uri = getArguments().getString("file name");
        Picasso.with(getContext()).load(Uri.parse(uri)).into(imageView);
        Log.d("getting the photo id" , db.getImageIdByUri(uri));

        RecyclerView recyclerView = (RecyclerView) v.findViewById(R.id.recycler_view_tags);
        LinearLayoutManager layoutManager
                = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);
        tagAdapter = new TagAdapter(this);

        recyclerView.setAdapter(tagAdapter);
        tagAdapter.setData(db.getAllTagsForImage(uri));


        final AutoCompleteTextView editText = (AutoCompleteTextView) v.findViewById(R.id.editText2);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_dropdown_item_1line, db.getAllTags());
        editText.setAdapter(adapter);
        editText.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                db.addTag(editText.getText().toString());
                db.addLink(uri, editText.getText().toString());
                tagAdapter.setData(db.getAllTagsForImage(uri));
                editText.setText("");
            }
        });



        editText.setOnKeyListener(new View.OnKeyListener() {

                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if ((event.getAction() == KeyEvent.ACTION_UP) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                        if (!editText.getText().toString().equals("")) {
                            if(!tagAdapter.tags.contains(editText.getText().toString())) {
                                db.addTag(editText.getText().toString());
                                db.addLink(uri, editText.getText().toString());
                                db.addLink(uri, editText.getText().toString());
                                tagAdapter.setData(db.getAllTagsForImage(uri));
                                editText.setText("");
                            }else{
                                Toast.makeText(getActivity(), "The tag you have entered already exists!",
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                    return true;
                }});




                    return v;



    }

}
