package edu.ucsb.cs.cs190i.danielledodd.imagetagexplorer;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {
    static final int RESULT_LOAD_GALLERY = 1;
    static final int RESULT_LOAD_CAMERA = 2;
    ImageAdapter imageAdapter;
    RecyclerView view;
    ImageTagDatabaseHelper db;
    public Boolean mShowDialog = false;
    Uri selectedImageUri;
    String mCurrentPhotoPath;
    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageTagDatabaseHelper.Initialize(this);

        db = ImageTagDatabaseHelper.GetInstance();
        view = (RecyclerView)findViewById(R.id.recycler_view_images);
        view.setLayoutManager(new GridLayoutManager(this, 2));

        imageAdapter = new ImageAdapter();
        view.setAdapter(imageAdapter);
        imageAdapter.setData(db.getAllImageURIs());


        final Button button = (Button) findViewById(R.id.button);
        adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line,new ArrayList<String>());
        final AutoCompleteTextView autoCompleteTextView = (AutoCompleteTextView) findViewById(R.id.editText);

        autoCompleteTextView.setAdapter(adapter);
        autoCompleteTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                adapter.clear();
                adapter.addAll(db.getAllTags());
                adapter.notifyDataSetChanged();
            }
        });
        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                button.setVisibility(View.VISIBLE);
                button.setText(adapter.getItem(i));
                imageAdapter.setData(db.getAllImagesForTags(adapter.getItem(i)));
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        button.setVisibility(View.GONE);
                        autoCompleteTextView.setText("");
                        imageAdapter.setData(db.getAllImageURIs());
                    }
                });
            }
        });





        FloatingActionButton camera = (FloatingActionButton) findViewById(R.id.camera);
        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //https://developer.android.com/training/camera/photobasics.html
                dispatchTakePictureIntent();
            }
        });
        FloatingActionButton gallery = (FloatingActionButton) findViewById(R.id.gallery);
        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    Intent imgPickingIntent = new Intent();
                    imgPickingIntent.setType("image/*");
                    imgPickingIntent.setAction(Intent.ACTION_OPEN_DOCUMENT);
                    startActivityForResult(imgPickingIntent, RESULT_LOAD_GALLERY);
                }
            });
        Toolbar myToolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(myToolbar);
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String imageFileName = "Test" + System.currentTimeMillis();
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {

            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "edu.ucsb.cs.cs190i.danielledodd.imagetagexplorer",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, RESULT_LOAD_CAMERA);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RESULT_LOAD_GALLERY && resultCode == RESULT_OK) {
            mShowDialog = true;
            selectedImageUri = data.getData();
        }

        if (requestCode == RESULT_LOAD_CAMERA && resultCode == RESULT_OK) {
            mShowDialog = true;
            File f = new File(mCurrentPhotoPath);
            Uri contentUri = Uri.fromFile(f);
            selectedImageUri = contentUri;

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //ImageTagDatabaseHelper.GetInstance().close();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        // Show your dialog here (this is called right after onActivityResult)
        if(mShowDialog) {
            mShowDialog = false;
            db.addImageURI(selectedImageUri.toString());
            imageAdapter.setData(db.getAllImageURIs());
            FragmentManager fm = getSupportFragmentManager();
            ImageFragment fragment = new ImageFragment();
            // Show DialogFragment
            Bundle b = new Bundle();
            b.putString("file name", selectedImageUri.toString());
            fragment.setArguments(b);
            fragment.show(fm, "Dialog Fragment");
        }
    }
    @Override
    protected void onResume() {
        Log.d("on resume", "On resume!");
        super.onResume();
        adapter.clear();
        adapter.addAll(db.getAllTags());
        adapter.notifyDataSetChanged();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu_main; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.populate:
                TaggedImageRetriever.getNumImages(new TaggedImageRetriever.ImageNumResultListener() {
                    @Override
                    public void onImageNum(int num) {
                      for (int i = 0; i < num; i++) {
                           TaggedImageRetriever.getTaggedImageByIndex(i, new TaggedImageRetriever.TaggedImageResultListener() {
                                @Override
                                public void onTaggedImage(TaggedImageRetriever.TaggedImage image) {
                                    if (image != null) {
                                        String fname = "Test"+ System.currentTimeMillis() + ".jpg";
                                        try (FileOutputStream stream = openFileOutput(fname, Context.MODE_PRIVATE)) {
                                            image.image.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                                            image.image.recycle();
                                        } catch (IOException e) {
                                        }
                                        db.addImageURI(Uri.fromFile(getFileStreamPath(fname)).toString());
                                        imageAdapter.setData(db.getAllImageURIs());
                                        StringBuilder tagList = new StringBuilder();
                                        for (String p : image.tags) {
                                            db.addTag(p);
                                            db.addLink(Uri.fromFile(getFileStreamPath(fname)).toString(), p);

                                        }
                                    }
                                }
                            });
                        }

                        //Log.d("database: ", db.getAllImageURIs().toString());

                    }
                });



                break;
            case R.id.clear:
                db.clear();
                imageAdapter.clear();
                break;
        }
        return super.onOptionsItemSelected(item);

    }

}