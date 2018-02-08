package edu.ucsb.cs.cs190i.danielledodd.imagetagexplorer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Samuel on 5/2/2017.
 */

public class ImageTagDatabaseHelper extends SQLiteOpenHelper {
    private static final String CreateImageTable = "CREATE TABLE Image (Id integer PRIMARY KEY AUTOINCREMENT, Uri text NOT NULL UNIQUE);";
    private static final String CreateTagTable = "CREATE TABLE Tag (Id integer PRIMARY KEY AUTOINCREMENT, Text text NOT NULL UNIQUE);";
    private static final String CreateLinkTable =
            "CREATE TABLE Link (ImageId integer, TagId integer, PRIMARY KEY (ImageId, TagId), " +
                    "FOREIGN KEY (ImageId) REFERENCES Image (Id) ON DELETE CASCADE ON UPDATE NO ACTION, " +
                    "FOREIGN KEY (TagId) REFERENCES Tag (Id) ON DELETE CASCADE ON UPDATE NO ACTION);";
    private static final String DatabaseName = "ImageTagDatabase.db";
    private static ImageTagDatabaseHelper Instance;
    private List<OnDatabaseChangeListener> Listeners;



    private ImageTagDatabaseHelper(Context context) {
        super(context, DatabaseName, null, 1);
        Listeners = new ArrayList<>();
    }

    public static void Initialize(Context context) {
        Instance = new ImageTagDatabaseHelper(context);
    }

    public static ImageTagDatabaseHelper GetInstance() {
        return Instance;
    }

    public void Subscribe(OnDatabaseChangeListener listener) {
        Listeners.add(listener);
    }

    private boolean TryUpdate(Cursor cursor) {
        try {
            cursor.moveToFirst();
        } catch (SQLiteConstraintException exception) {
            return false;
        } finally {
            cursor.close();
        }
        NotifyListeners();
        return true;
    }


    public void clear(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("Image",null,null);
        db.delete("Tag",null,null);
        db.delete("Link", null, null);
        db.close();
    }

    public void deleteTagByImageId(String uri, String text){
        String imageId = getImageIdByUri(uri);
        String tagId = getTagIdByText(text);
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("Link", "TagId=? AND ImageId=?", new String[] {tagId, imageId} );
        db.close();
    }


    public void addImageURI(String URI) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("Uri", URI);
        db.insert("Image", null, values);
        db.close();
        NotifyListeners();
    }

    public void addTag(String tagText) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues tagvalues = new ContentValues();
        tagvalues.put("Text", tagText);
        db.insert("Tag", null, tagvalues);
        db.close();
        NotifyListeners();
    }

    public void addLink(String imageUri, String tagText){
        String imageId = getImageIdByUri(imageUri);
        String tagId = getTagIdByText(tagText);
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("ImageId", imageId);
        values.put("TagId", tagId);
        db.insert("Link", null, values);
        db.close();
        NotifyListeners();
    }

    public String getImageIdByUri(String uri){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        String imageId = "";

        try {
            cursor = db.rawQuery("SELECT Id FROM Image WHERE Uri=?", new String[] {uri + ""});
            if(cursor.getCount() > 0) {
                cursor.moveToFirst();
                imageId = cursor.getString(cursor.getColumnIndex("Id"));
            }
            return imageId;
        }finally {
            cursor.close();
            db.close();
        }

    }
    public String getUriById(String id){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        String uri = "";

        try {
            cursor = db.rawQuery("SELECT Uri FROM Image WHERE Id=?", new String[] {id + ""});
            if(cursor.getCount() > 0) {
                cursor.moveToFirst();
                uri = cursor.getString(cursor.getColumnIndex("Uri"));
            }
            return uri;
        }finally {
            cursor.close();
            db.close();
        }
    }

    public String getTagIdByText(String text){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        String tagId = "";

        try {
            cursor = db.rawQuery("SELECT Id FROM Tag WHERE Text=?", new String[] {text + ""});
            if(cursor.getCount() > 0) {
                cursor.moveToFirst();
                tagId = cursor.getString(cursor.getColumnIndex("Id"));
            }
            return tagId;
        }finally {
            cursor.close();
            db.close();
        }
    }

    public String getTextById(String id){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        String text = "";

        try {
            cursor = db.rawQuery("SELECT Text FROM Tag WHERE Id=?", new String[] {id + ""});
            if(cursor.getCount() > 0) {
                cursor.moveToFirst();
                text = cursor.getString(cursor.getColumnIndex("Text"));
            }
            return text;
        }finally {
            cursor.close();
            db.close();
        }
    }

    public ArrayList<String> getAllImageURIs() {
        ArrayList<String> uri_list = new ArrayList<String>();
        String selectQuery = "SELECT * FROM Image";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                uri_list.add(cursor.getString(1));
            } while (cursor.moveToNext());
        }

        // return contact list
        db.close();
        return uri_list;
    }
    public ArrayList<String> getAllTags() {
        ArrayList<String> uri_list = new ArrayList<String>();
        String selectQuery = "SELECT * FROM Tag";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                uri_list.add(cursor.getString(1));
            } while (cursor.moveToNext());
        }

        // return contact list
        db.close();
        return uri_list;
    }

    public ArrayList<String> getAllTagsForImage(String uri){
        String imageId = getImageIdByUri(uri);

        ArrayList<String> tag_id_list = new ArrayList<String>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM Link WHERE ImageId=?", new String[] {imageId + ""});

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                tag_id_list.add(cursor.getString(1));
            } while (cursor.moveToNext());
        }
        ArrayList<String> tag_list = new ArrayList<String>();

        for(int i = 0; i<tag_id_list.size(); i++){
            tag_list.add(getTextById(tag_id_list.get(i)));
        }

        db.close();
        return tag_list;
    }

    public ArrayList<String> getAllImagesForTags(String text){
        String tagId = getTagIdByText(text);

        ArrayList<String> image_id_list = new ArrayList<String>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT ImageId FROM Link WHERE TagId=?", new String[] {tagId + ""});

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                image_id_list.add(cursor.getString(0));
                //Log.d("cursor", cursor.getString(0));
            } while (cursor.moveToNext());
        }
        ArrayList<String> image_list = new ArrayList<String>();

        for(int i = 0; i<image_id_list.size(); i++){
            image_list.add(getUriById(image_id_list.get(i)));
        }

        // return contact list
        db.close();
        //Log.d("tag id associated image", uri_list.toString());
        return image_list;
    }



    private void NotifyListeners() {
        for (OnDatabaseChangeListener listener : Listeners) {
            listener.OnDatabaseChange();
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CreateImageTable);
        db.execSQL(CreateTagTable);
        db.execSQL(CreateLinkTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public interface OnDatabaseChangeListener {
        void OnDatabaseChange();
    }
}