package application.minseong.capstone;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper{
    //DBMS 용도로 사용 : Database management system 을 의미

    public static final int DATABASE_VERSION = 1;

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS "+ DBContract.contract.TABLE_NAME;


    public DBHelper(Context context,String name, SQLiteDatabase.CursorFactory factory,int version){
        super(context,name,factory,version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.i("makeDB","db 생성 , db 없을때만 최초로 생성");
        createTable(db);
    }
    // 생성 후 최초 한번 호출 (테이블 생성 목적)

    public void createTable(SQLiteDatabase db) {
        String sql = "CREATE TABLE " + DBContract.contract.TABLE_NAME + "(email text, name text, password text)";
        try {
            db.execSQL(sql);
        } catch (SQLException e) {
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (newVersion == DATABASE_VERSION) {
            try {
                db.execSQL(SQL_DELETE_ENTRIES);
                onCreate(db);
            } catch (Exception e) {
                Log.e("Upgrade", "DB 업그레이드 오류");
            }
        }
    }
    // DB 버전 업그레이드 될때 호출 (스키마 변경 목적)

    public boolean insertUser(SQLiteDatabase db, String user_id, String user_name, String user_pw) {
        db.beginTransaction();
        try {
            String sql = "INSERT INTO " + DBContract.contract.TABLE_NAME + "(email,name,password)" + "VALUES('" + user_id + "', '" + user_name + "', '" + user_pw + "')";
            db.execSQL(sql);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            db.setTransactionSuccessful();
            db.endTransaction();
        }
    }

    public boolean selectUser(SQLiteDatabase db,String email){
        Log.i("tag","검색할때 실행");
        db.beginTransaction();
        try{
            String sql = "SELECT email FROM " + DBContract.contract.TABLE_NAME + " WHERE email = '" + email + "'";
            Cursor cursor = db.rawQuery(sql,null);

            if(cursor.getCount() != 0){
                cursor.close();
                return true; // 해당 이메일을 사용하는 유저가 있을때
            }else
                cursor.close();
                return false; // 해당 이메일을 사용하는 유저가 없을때

        }catch(Exception e){
            e.printStackTrace();
            return false;
        }finally {
            db.endTransaction();
        }
    }


    public boolean getPasswordFromId(SQLiteDatabase db,String email,String password){
        Log.i("tag","password validation");
        db.beginTransaction();

        try{
            String sql = "SELECT password FROM "+ DBContract.contract.TABLE_NAME + " WHERE email = '" + email + "'";
            Cursor cursor = db.rawQuery(sql,null);

            if(cursor.getCount() != 0){
                cursor.moveToFirst();
                if(password.equals(cursor.getString(0))){
                    cursor.close();
                    return true;
                }else{
                    cursor.close();
                    return false;
                }
            }else{
                Log.i("tag","cursurNull");
                return false;
            }
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }finally{
            db.setTransactionSuccessful();
            db.endTransaction();
        }
    }


    public int CountUser(SQLiteDatabase db){
        int userCount = 0;
        db.beginTransaction();
        try {
            String sql = "SELECT COUNT(*) FROM " + DBContract.contract.TABLE_NAME;
            Cursor cursor = db.rawQuery(sql,null);
            if(cursor.getCount() > 0){
                cursor.moveToFirst();
                userCount = cursor.getInt(0);
                cursor.close();
                return userCount;
            }else{
                cursor.close();
                return 0;
            }
        }catch (Exception e){
            e.printStackTrace();
            return 0;
        }finally {
            db.endTransaction();
        }
    }
}
