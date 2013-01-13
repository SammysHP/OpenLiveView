package nl.rnplus.olv.data;

/**
 * LiveViewDbHelper2.java
 * @Author Renze Nicolai
 * Replacement for LiveViewDbHelper.java
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.util.Log;

public class LiveViewDbHelper2 {
  
 private static final String SCRIPT_CREATE_TABLE_ALERT_ITEMS =
  "create table " + LiveViewDbConstants.TABLE_ALERT_ITEMS + " ("
  + LiveViewDbConstants.COLUMN_ALERT_ITEMS_ID + " integer primary key, " 
  + LiveViewDbConstants.COLUMN_ALERT_ITEMS_TITLE + " text, "
  + LiveViewDbConstants.COLUMN_ALERT_ITEMS_CONTENT + " text not null, "
  + LiveViewDbConstants.COLUMN_ALERT_ITEMS_TYPE + " integer, "
  + LiveViewDbConstants.COLUMN_ALERT_ITEMS_TIMESTAMP + " integer, "
  +");";
 
 private SQLiteHelper sqLiteHelper;
 private SQLiteDatabase sqLiteDatabase;

 private Context context;
 
 public LiveViewDbHelper2(Context c){
  context = c;
 }
 
 public LiveViewDbHelper2 openToRead() throws android.database.SQLException {
  sqLiteHelper = new SQLiteHelper(context, LiveViewDbConstants.DATABASE_NAME, null, LiveViewDbConstants.DATABASE_VERSION);
  sqLiteDatabase = sqLiteHelper.getReadableDatabase();
  return this; 
 }
 
 public LiveViewDbHelper2 openToWrite() throws android.database.SQLException {
  sqLiteHelper = new SQLiteHelper(context, LiveViewDbConstants.DATABASE_NAME, null, LiveViewDbConstants.DATABASE_VERSION);
  sqLiteDatabase = sqLiteHelper.getWritableDatabase();
  return this; 
 }
 
 public void close(){
  sqLiteHelper.close();
 }
 
 public long insertAlert(String title, String content, int type, long timestamp){
  ContentValues contentValues = new ContentValues();
  contentValues.put(LiveViewDbConstants.COLUMN_ALERT_ITEMS_CONTENT, content);
  contentValues.put(LiveViewDbConstants.COLUMN_ALERT_ITEMS_TITLE, title);
  contentValues.put(LiveViewDbConstants.COLUMN_ALERT_ITEMS_TYPE, type);
  contentValues.put(LiveViewDbConstants.COLUMN_ALERT_ITEMS_TIMESTAMP, timestamp);
  return sqLiteDatabase.insert(LiveViewDbConstants.TABLE_ALERT_ITEMS, null, contentValues);
 }
 
 public int deleteAllAlerts(){
  return sqLiteDatabase.delete(LiveViewDbConstants.TABLE_ALERT_ITEMS, null, null);
 }
 
 public String queueAllAlerts(){
  String[] columns = new String[]{LiveViewDbConstants.COLUMN_ALERT_ITEMS_CONTENT, LiveViewDbConstants.COLUMN_ALERT_ITEMS_TITLE};
  Cursor cursor = sqLiteDatabase.query(LiveViewDbConstants.TABLE_ALERT_ITEMS, columns, 
    null, null, null, null, null);
  String result = "";
  
  int index_CONTENT = cursor.getColumnIndex(LiveViewDbConstants.COLUMN_ALERT_ITEMS_CONTENT);
  for(cursor.moveToFirst(); !(cursor.isAfterLast()); cursor.moveToNext()){
   result = result + cursor.getString(index_CONTENT) + "\n";
  }
 
  return result;
 }
 
 public class SQLiteHelper extends SQLiteOpenHelper {

  public SQLiteHelper(Context context, String name,
    CursorFactory factory, int version) {
   super(context, name, factory, version);
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
   db.execSQL(SCRIPT_CREATE_TABLE_ALERT_ITEMS);
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
      if (oldVersion != newVersion)
      {
      	try {
	            db.execSQL("DROP TABLE " + LiveViewDbConstants.TABLE_ALERT_ITEMS);
      	} catch(Exception e) {
      		Log.e("LiveViewDbHelper", "Could not delete all tables. "+e.toString());
      	}
          onCreate(db);
      }
  } 
 }
}