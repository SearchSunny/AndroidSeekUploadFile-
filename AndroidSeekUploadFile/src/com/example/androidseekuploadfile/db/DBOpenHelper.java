package com.example.androidseekuploadfile.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 数据库帮助类
 * @author miaowei
 *
 */
public class DBOpenHelper extends SQLiteOpenHelper {

	/**
	 * 文件名
	 */
	private String uploadfilepath;
	/**
	 * 记录文件标识
	 */
	private String sourceid;
	
	public DBOpenHelper(Context context) {  
        super(context, "upload.db", null, 1);  
    }  
  
	/**
	 * 创建数据库
	 */
    @Override  
    public void onCreate(SQLiteDatabase db) {  
        db.execSQL("CREATE TABLE uploadlog (_id integer primary key autoincrement, uploadfilepath varchar(100), sourceid varchar(10))");  
    }  
  
    @Override  
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {  
        db.execSQL("DROP TABLE IF EXISTS uploadlog");  
        onCreate(db);         
    }  
	
}
