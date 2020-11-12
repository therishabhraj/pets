package com.example.pets.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Created by Rishabh on 04,November,2020
 */
public class PetProvider extends ContentProvider {
    public static final int PETS = 100;
    public static final int PET_ID = 101;
    public static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY, PetContract.PATH_PETS, PETS);
        sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY, PetContract.PATH_PETS+"/#", PET_ID);
    }
    private PetDbHelper mDbHelper;


    @Override
    public boolean onCreate() {
        mDbHelper = new PetDbHelper(getContext());
        return false;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase database = mDbHelper.getReadableDatabase();
        Cursor cursor;
        int match = sUriMatcher.match(uri);
        switch (match){
            case PETS:
                cursor = database.query(PetContract.PetEntry.TABLE_NAME, projection, selection,
                        selectionArgs, null,null, sortOrder);
                break;
            case PET_ID:
                selection = PetContract.PetEntry._ID+"=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(PetContract.PetEntry.TABLE_NAME, projection, selection,
                        selectionArgs, null,null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot Query unknown URI" + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {

        int match = sUriMatcher.match(uri);
        switch (match){
            case PETS:
                return PetContract.PetEntry.CONTENT_LIST_TYPE;
            case PET_ID:
                return PetContract.PetEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        int match = sUriMatcher.match(uri);
        switch (match){
            case PETS:
                return insertPet(uri, values);
            default:
                throw new IllegalArgumentException("Insertion not supported for this URI "+uri);
        }
    }

    private Uri insertPet(Uri uri, ContentValues values) {
        String name = values.getAsString(PetContract.PetEntry.COLUMN_PET_NAME);
        if (name == null){
            throw new IllegalArgumentException("Pet requires a name");
        }

        int gender = values.getAsInteger(PetContract.PetEntry.COLUMN_PET_GENDER);
        if (!PetContract.PetEntry.isValidGender(gender)){
            throw new IllegalArgumentException("Pet requires a valid gender");
        }

        int weight = values.getAsInteger(PetContract.PetEntry.COLUMN_PET_WEIGHT);
        if (weight < 0){
            throw new IllegalArgumentException("Pet requires a valid weight");
        }

        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        long id = database.insert(PetContract.PetEntry.TABLE_NAME, null, values);
        if (id == -1){
            Log.e("PetProvider","Failed to insert row for " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);
        int rowsDeleted;
        switch (match){
            case PETS:
                rowsDeleted = database.delete(PetContract.PetEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case PET_ID:
                selection = PetContract.PetEntry._ID+"=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(PetContract.PetEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
        if (rowsDeleted != 0){
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;

    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        int match = sUriMatcher.match(uri);
        switch (match){
            case PETS:
                return updatePet(uri, values, selection, selectionArgs);
            case PET_ID:
                selection = PetContract.PetEntry._ID +"=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updatePet(uri, values, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    private int updatePet(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        if (values.containsKey(PetContract.PetEntry.COLUMN_PET_NAME)){
            String name = values.getAsString(PetContract.PetEntry.COLUMN_PET_NAME);
            if (name == null){
                throw new IllegalArgumentException("Pet requires a name");
            }
        }

        if (values.containsKey(PetContract.PetEntry.COLUMN_PET_GENDER)){
            int gender = values.getAsInteger(PetContract.PetEntry.COLUMN_PET_GENDER);
            if (!PetContract.PetEntry.isValidGender(gender)){
                throw new IllegalArgumentException("Pet requires a valid gender");
            }
        }


        if (values.containsKey(PetContract.PetEntry.COLUMN_PET_WEIGHT)){
            int weight = values.getAsInteger(PetContract.PetEntry.COLUMN_PET_WEIGHT);
            if (weight < 0){
                throw new IllegalArgumentException("Pet requires a valid weight");
            }
        }

        if (values.size()==0){
            return 0;
        }

        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        int rowsUpdated =  database.update(PetContract.PetEntry.TABLE_NAME, values, selection, selectionArgs);
        if (rowsUpdated != 0){
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }
}
