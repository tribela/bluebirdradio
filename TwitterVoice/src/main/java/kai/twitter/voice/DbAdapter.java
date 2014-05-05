package kai.twitter.voice;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import twitter4j.auth.AccessToken;

/**
 * Created by kjwon15 on 2014. 4. 6..
 */
public class DbAdapter {

    private static final String TABLE_NAME = "accounts";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_ACCESS_SECRET = "access_secret";
    private Context context;
    private SQLiteDatabase db;

    public DbAdapter(Context context) {
        this.context = context;
        this.open();
    }

    private void open() {
        db = new DbHelper(context).getWritableDatabase();
    }

    public boolean insertAccount(AccessToken token) {
        try {
            ContentValues values = new ContentValues();
            values.put(KEY_ACCESS_TOKEN, token.getToken());
            values.put(KEY_ACCESS_SECRET, token.getTokenSecret());
            db.insert(TABLE_NAME, null, values);
            return true;
        } catch (SQLiteException e) {
            Log.e("SQLite", e.getMessage());
            return false;
        }
    }

    public boolean deleteAccount(AccessToken token) {
        try {
            db.delete(TABLE_NAME,
                    String.format(" %s = ? and %s = ?", KEY_ACCESS_TOKEN, KEY_ACCESS_SECRET),
                    new String[]{token.getToken(), token.getTokenSecret()});
            return true;
        } catch (SQLiteException e) {
            Log.e("SQLite", e.getMessage());
            return false;
        }
    }

    public boolean deleteAccount(String accessToken) {
        try {
            db.delete(TABLE_NAME,
                    KEY_ACCESS_TOKEN + " = ?",
                    new String[]{accessToken});
            return true;
        } catch (SQLiteException e) {
            Log.e("SQLite", e.getMessage());
            return false;
        }
    }

    public List<AccessToken> getAccounts() {
        List<AccessToken> accounts = new ArrayList<AccessToken>();
        Cursor cursor = db.query(TABLE_NAME,
                new String[]{KEY_ACCESS_TOKEN, KEY_ACCESS_SECRET},
                null, null, null, null, null);
        if (cursor.moveToFirst()) {
            int indexToken = cursor.getColumnIndex(KEY_ACCESS_TOKEN);
            int indexSecret = cursor.getColumnIndex(KEY_ACCESS_SECRET);

            do {
                String token = cursor.getString(indexToken);
                String secret = cursor.getString(indexSecret);
                AccessToken accessToken = new AccessToken(token, secret);
                accounts.add(accessToken);
            } while (cursor.moveToNext());
        }

        return accounts;
    }

    private class DbHelper extends SQLiteOpenHelper {
        private static final String DBNAME = "accounts.db";
        private static final int DATABASE_VERSION = 1;

        public DbHelper(Context context) {
            super(context, DBNAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            String query = String.format("create table %s(%s text unique, %s text);",
                    TABLE_NAME, KEY_ACCESS_TOKEN, KEY_ACCESS_SECRET);
            db.execSQL(query);

            Log.i("DB", "Database created");
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {
            Log.i("DB", "Database upgraded");

        }
    }
}
