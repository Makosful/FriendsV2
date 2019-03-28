package com.github.makosful.friendsv2.dal;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.github.makosful.friendsv2.Common;
import com.github.makosful.friendsv2.be.Friend;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SQLiteFriends implements IStorage<Friend>
{
    private static final String TAG = "SQLiteDatabase";

    private static final String TABLE_NAME = "friends";
    private static final String FIELD_ID = "ID";
    private static final String FIELD_NAME = "name";
    private static final String FIELD_ADDRESS = "address";
    private static final String FIELD_LATITUDE = "latitude";
    private static final String FIELD_LONGITUDE = "longitude";
    private static final String FIELD_PHONE = "phone";
    private static final String FIELD_EMAIL = "email";
    private static final String FIELD_WEBSITE = "website";
    private static final String FIELD_BIRTHDAY = "birthday";
    private static final String FIELD_PICTURE = "picture";

    private static String INSERT = "INSERT INTO " + TABLE_NAME +
                                   "(name, address, latitude, longitue, phone, email, website, birthday, picture) " +
                                   "values (?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private SQLiteDatabase database;
    private SQLiteStatement insertStatement;

    public SQLiteFriends(Context context)
    {
        OpenHelper openHelper = new OpenHelper(context);
        this.database = openHelper.getWritableDatabase();
        this.insertStatement = this.database.compileStatement(INSERT);
    }

    private static void log(String message)
    {
        Log.d(TAG, message);
    }

    @Override
    public boolean create(Friend item)
    {
        log("Attempting to store Friend in database");

        try
        {
            log("Converts bitmap to byte array");
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            item.getPicture().compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] bytes = stream.toByteArray();
            log("Conversion successfully");

            log("Attempts to close conversion stream");
            stream.close();
            log("stream closed");

            int i = 0;
            this.insertStatement.bindString(++i, item.getName()); // 1
            this.insertStatement.bindString(++i, item.getAdress()); // 2
            this.insertStatement.bindDouble(++i, item.getLatitude()); // 3
            this.insertStatement.bindDouble(++i, item.getLongitude()); // 4
            this.insertStatement.bindString(++i, item.getPhone()); // 5
            this.insertStatement.bindString(++i, item.getEmail()); // 6
            this.insertStatement.bindString(++i, item.getWebsite()); // 7
            this.insertStatement.bindLong(++i, item.getBirthDate().getTime()); // 8
            this.insertStatement.bindBlob(++i, bytes); // 9

            log("Attempts to execute INSERT statement for Friend");
            long result = this.insertStatement.executeInsert();

            return result >= 0;
        }
        catch (IOException e)
        {
            log("Stream failed to close. IOException");
            log("Exception: " + e.getMessage());
            return false;
        }
        finally
        {
            log("Recycles the bitmap");
            item.getPicture().recycle();
        }
    }

    @Override
    public Friend readById(int id)
    {
        log("Attempts to read Friend with ID " + id);
        Cursor cursor = this.database.query(TABLE_NAME,
                                           new String[] {FIELD_ID, FIELD_NAME, FIELD_ADDRESS,
                                                         FIELD_LATITUDE, FIELD_LONGITUDE,
                                                         FIELD_PHONE, FIELD_EMAIL, FIELD_WEBSITE,
                                                         FIELD_BIRTHDAY, FIELD_PICTURE}, "id = ?",
                                           new String[] {"" + id}, null, null, FIELD_NAME);

        log("Query sent. Result received");

        if (cursor.moveToFirst())
        {
            log("Assembles Friend from the database");
            int i = -1;
            Friend friend = new Friend();
            friend.setId(cursor.getInt(++i));
            friend.setName(cursor.getString(++i));
            friend.setAdress(cursor.getString(++i));
            friend.setLatitude(cursor.getLong(++i));
            friend.setLongitude(cursor.getLong(++i));
            friend.setPhone(cursor.getString(++i));
            friend.setEmail(cursor.getString(++i));
            friend.setWebsite(cursor.getString(++i));
            friend.setBirthDate(new Date(cursor.getLong(++i)));

            log("Attempts to convert the byte array into a bitmap");
            byte[] blob = cursor.getBlob(++i);
            Bitmap bitmap = BitmapFactory
                    .decodeByteArray(blob, 0, blob.length, new BitmapFactory.Options());
            friend.setPicture(bitmap);

            cursor.close();
            return friend;
        }

        log("No friend with the ID " + id + " was found");
        cursor.close();
        return null;
    }

    @Override
    public List<Friend> readAll()
    {
        log("Attempts to read all Friends from the database");

        List<Friend> friends = new ArrayList<>();

        Cursor cursor = this.database.query(TABLE_NAME,
                                          new String[] {FIELD_ID, FIELD_NAME, FIELD_ADDRESS,
                                                        FIELD_LATITUDE, FIELD_LONGITUDE,
                                                        FIELD_PHONE, FIELD_EMAIL, FIELD_WEBSITE,
                                                        FIELD_BIRTHDAY, FIELD_PICTURE}, null, null,
                                          null, null, FIELD_NAME);

        log("Query sent. Results received");

        if (cursor.moveToFirst())
        {
            log("Starts looping through all friends from the database");
            do
            {
                int i = -1;
                Friend friend = new Friend();
                log("Gets the Friend's ID");
                friend.setId(cursor.getInt(++i));

                log("Gets the name of Friend(" + friend.getId() + ")");
                friend.setName(cursor.getString(++i));

                log("Gets the address of Friend(" + friend.getId() + ")");
                friend.setAdress(cursor.getString(++i));

                log("Gets the location of Friend(" + friend.getId() + ")");
                friend.setLatitude(cursor.getLong(++i));
                friend.setLongitude(cursor.getLong(++i));

                log("Gets the phone number of Friend(" + friend.getId() + ")");
                friend.setPhone(cursor.getString(++i));

                log("Gets the email of Friend(" + friend.getId() + ")");
                friend.setEmail(cursor.getString(++i));

                log("Gets the website of Friend(" + friend.getId() + ")");
                friend.setWebsite(cursor.getString(++i));

                log("Gets the birthday of Friend(" + friend.getId() + ")");
                friend.setBirthDate(new Date(cursor.getLong(++i)));

                log("Attempts to convert friend(" + friend.getId() + ")'s byte array into a bitmap");
                byte[] blob = cursor.getBlob(++i);
                Bitmap bitmap = BitmapFactory
                        .decodeByteArray(blob, 0, blob.length, new BitmapFactory.Options());
                friend.setPicture(bitmap);

                log("Add friend(" + friend.getId() + ") to the list of friends");
                friends.add(friend);
            }
            while (cursor.moveToNext());
            log("Finished looping through all the friends from the database");
        }

        if (!cursor.isClosed())
            cursor.close();

        return friends;
    }

    @Override
    public boolean update(Friend item)
    {
        log("Attempts to update Friend " + item.getName());
        try
        {
            log("Attempts to convert bitmap into byte array");
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            item.getPicture().compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] bytes = stream.toByteArray();
            log("Conversion successful");

            log("Attempts to close the stream");
            stream.close();
            log("Stream closed successfully");

            ContentValues cv = new ContentValues();
            cv.put(FIELD_NAME, item.getName());
            cv.put(FIELD_NAME, item.getAdress());
            cv.put(FIELD_NAME, item.getLatitude());
            cv.put(FIELD_NAME, item.getLongitude());
            cv.put(FIELD_NAME, item.getPhone());
            cv.put(FIELD_NAME, item.getEmail());
            cv.put(FIELD_NAME, item.getWebsite());
            cv.put(FIELD_NAME, item.getBirthDate().getTime());
            cv.put(FIELD_NAME, bytes);

            log("Fires off the UPDATE statement");
            int result = this.database
                    .update(TABLE_NAME, cv, "id = ?", new String[] {"" + item.getId()});

            return result >= 1;
        }
        catch (IOException e)
        {
            log("Stream failed to close. IOException");
            log("Exception: " + e.getMessage());
            return false;
        }
        finally
        {
            log("Recycles the bitmap");
            item.getPicture().recycle();
        }
    }

    @Override
    public boolean deleteAll()
    {
        log("Attempts to clear the table of Friends");
        int result = this.database.delete(TABLE_NAME, null, null);

        return result >= 1;
    }

    @Override
    public boolean deleteById(int id)
    {
        log("Attempts to delete the friend with ID " + id);
        int result = this.database.delete(TABLE_NAME, "id = ?", new String[] {"" + id});

        return result >= 1;
    }

    private static class OpenHelper extends SQLiteOpenHelper
    {

        OpenHelper(Context context)
        {
            super(context, Common.DATABASE_NAME, null, Common.DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase)
        {
            sqLiteDatabase.execSQL("CREATE TABLE " + TABLE_NAME + " (" +
                                   FIELD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                   FIELD_NAME + " TEXT, " +
                                   FIELD_ADDRESS + " TEXT, " +
                                   FIELD_LATITUDE + "NUM, " +
                                   FIELD_LONGITUDE + "NUM, " +
                                   FIELD_PHONE + " TEXT, " +
                                   FIELD_EMAIL + " TEXT, " +
                                   FIELD_WEBSITE + " TEXT, " +
                                   FIELD_BIRTHDAY + " INTEGER, " +
                                   FIELD_PICTURE + " BLOB" +
                                   ")");
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1)
        {
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        }
    }
}
