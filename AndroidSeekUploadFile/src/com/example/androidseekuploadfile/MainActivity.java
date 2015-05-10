package com.example.androidseekuploadfile;

import java.io.File;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.io.RandomAccessFile;
import java.net.Socket;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.androidseekuploadfile.db.UploadLogService;
import com.example.androidseekuploadfile.tools.StreamTool;
/**
 * android实现断点上传文件
 * @author miaowei
 *
 */
public class MainActivity extends Activity {
	/**
	 * 文件名
	 */
	private EditText filenameEditText;
	/**
	 * 上传结果 
	 */
	private TextView resultView;
	/**
	 * 等待框
	 */
	private ProgressBar uploadBar;
	/**
	 * 上传服务
	 */
	private UploadLogService logService;
	/**
	 * 是否开启上传
	 */
	private boolean start = true;
	/**
	 * 上传
	 */
	private Button btn_upload;
	/**
	 * 暂停
	 */
	private Button btn_stop;
	/**
	 * 本地测试
	 */
	String pathString = Environment.getExternalStorageDirectory().getAbsolutePath();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		logService = new UploadLogService(this);    
		filenameEditText = (EditText)this.findViewById(R.id.filename);    
		uploadBar = (ProgressBar) this.findViewById(R.id.uploadbar);    
		resultView = (TextView)this.findViewById(R.id.result);    
		btn_upload =(Button)this.findViewById(R.id.btn_upload);    
		btn_stop =(Button)this.findViewById(R.id.btn_stop);   
		
		btn_upload.setOnClickListener(onClickListener);
		btn_stop.setOnClickListener(onClickListener);
	}
	/**
	 * 事件处理
	 */
	private OnClickListener onClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.btn_upload://上传
				start = true;
				String filename = filenameEditText.getText().toString(); 
				//判断SDCard是否存在
                if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){    
                    //取得SDCard的目录
                	//File uploadFile = new File(Environment.getExternalStorageDirectory(), filename); 
                	//本地测试使用
                	File uploadFile = new File(pathString+"/Android/data/com.mapbar.info.collection/files/cache.zip");
                    
                	if(uploadFile.exists()){
                    	//开始上传文件
                        uploadFile(uploadFile);
                        
                    }else{ 
                    	
                        Toast.makeText(MainActivity.this,"文件不存在",Toast.LENGTH_SHORT).show();    
                    }    
                }else{ 
                	
                    Toast.makeText(MainActivity.this,"未检测到SD卡", Toast.LENGTH_SHORT).show();    
                }   
				break;
			case R.id.btn_stop://暂停
				
				start = false;
				
				break;
			default:
				break;
			}
			
		}
	};
	
	/**  
     * 上传文件
     * 启动一个线程，使用Handler来避免UI线程ANR错误   
     * @param uploadFile  
     */    
    private void uploadFile(final File uploadFile) {    
        new Thread(new Runnable() {             
            @Override    
            public void run() {    
                try {
                	//设置长传文件的最大刻度  
                	uploadBar.setMax((int)uploadFile.length()); 
                	//判断文件是否已有上传记录
                    String souceid = logService.getBindId(uploadFile); 
                    //构造拼接协议 
                    String head = "Content-Length="+ uploadFile.length() + ";filename="+ uploadFile.getName() + ";sourceid="+    
                        (souceid==null? "" : souceid)+"\r\n"; 
                    //通过Socket取得输出流
                    //测试使用，具体自配
                    Socket socket = new Socket("192.168.1.10",8080);    
                    OutputStream outStream = socket.getOutputStream();    
                    outStream.write(head.getBytes());    
                        
                    PushbackInputStream inStream = new PushbackInputStream(socket.getInputStream());        
                    //获取到字符流的id与位置
                    String response = StreamTool.readLine(inStream);    
                    String[] items = response.split(";");    
                    String responseid = items[0].substring(items[0].indexOf("=")+1);    
                    String position = items[1].substring(items[1].indexOf("=")+1); 
                    //代表原来没有上传过此文件，往数据库添加一条绑定记录 
                    if(souceid==null){
                        logService.save(responseid, uploadFile);    
                    }    
                    RandomAccessFile fileOutStream = new RandomAccessFile(uploadFile, "r");    
                    fileOutStream.seek(Integer.valueOf(position));    
                    byte[] buffer = new byte[1024];    
                    int len = -1; 
                    //初始化长传的数据长度 
                    int length = Integer.valueOf(position);    
                    while(start&&(len = fileOutStream.read(buffer)) != -1){    
                        outStream.write(buffer, 0, len); 
                        //设置长传数据长度 
                        length += len;    
                        Message msg = new Message();    
                        msg.getData().putInt("size", length);    
                        mHandler.sendMessage(msg);    
                    }    
                    fileOutStream.close();    
                    outStream.close();    
                    inStream.close();    
                    socket.close(); 
                    //判断上传完则删除数据
                    if(length==uploadFile.length()){
                    	
                    	logService.delete(uploadFile);
                    } 
                    	    
                } catch (Exception e) {    
                    e.printStackTrace();    
                }    
            }    
        }).start();    
    } 
	
	/**
	 * 使用Handler给创建他的线程发送消息
	 * UI更新
	 */
	private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			//获得上传长度的进度 
			int length = msg.getData().getInt("size");    
			uploadBar.setProgress(length);    
            float num = (float)uploadBar.getProgress()/(float)uploadBar.getMax();    
            //设置显示结果  
            int result = (int)(num * 100);    
            resultView.setText(result+ "%"); 
            //上传成功
            if(uploadBar.getProgress()==uploadBar.getMax()){ 
            	
                Toast.makeText(MainActivity.this, "上传成功", Toast.LENGTH_SHORT).show();    
            } 
		}
		
	};

}
