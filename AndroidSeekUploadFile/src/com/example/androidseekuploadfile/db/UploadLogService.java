package com.example.androidseekuploadfile.db;

import java.io.File;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * 操作数据库
 * @author miaowei
 *
 */
public class UploadLogService {

	private DBOpenHelper dbOpenHelper;  
    
    public UploadLogService(Context context){  
        this.dbOpenHelper = new DBOpenHelper(context);  
    }  
     /**
      * 保存上传文件断点数据 
      * @param sourceid 标识ID
      * @param uploadFile 文件
      */
    public void save(String sourceid, File uploadFile){  
        SQLiteDatabase db = dbOpenHelper.getWritableDatabase();  
        db.execSQL("insert into uploadlog(uploadfilepath, sourceid) values(?,?)",  
                new Object[]{uploadFile.getAbsolutePath(),sourceid});  
    }  
      
    /**
     * 文件上传完成，删除上传文件断点数据
     * @param uploadFile
     */
    public void delete(File uploadFile){  
        SQLiteDatabase db = dbOpenHelper.getWritableDatabase();  
        db.execSQL("delete from uploadlog where uploadfilepath=?", new Object[]{uploadFile.getAbsolutePath()});  
    }  
      
    /**
     * 根据文件的上传路径得到绑定的id
     * @param uploadFile
     * @return
     */
    public String getBindId(File uploadFile){  
        SQLiteDatabase db = dbOpenHelper.getReadableDatabase();  
        Cursor cursor = db.rawQuery("select sourceid from uploadlog where uploadfilepath=?",   
                new String[]{uploadFile.getAbsolutePath()});  
        if(cursor.moveToFirst()){  
            return cursor.getString(0);  
        }  
        return null;  
    } 
	
}
