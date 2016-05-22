package com.example.wifisignalstrength;

import android.util.Log;
import android.view.View.OnClickListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;




import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener,Runnable{

	static String filename = "SignalStrength.xls";
	static int index = 0;
	static String filename1 = "SignalStrength1.xls";
	static int rowNumber = 1;
	static int colNumber = 0;
	volatile String Server_Ip;
	int click_num=0;
	protected static final long TIME_DELAY = 1000;
	int clickedhere = 0;
	private Socket socket;
	private PrintWriter pw;
	private BufferedReader br;
	private Handler handler;

	static TextView mTextView;
	static TextView showMsg;
	EditText editx;
	EditText edity;
	EditText server_ip;
	Button xplusbutton;
	Button xminusbutton;
	Button yplusbutton;
	Button yminusbutton;
	Button start;
	Button send;
	Button makesure;
	private WifiManager wifiManager;
	int count = 0;
	String data = "";
	double xcord = 0;
	double ycord = 0;
	int xyCount = 0;
	ArrayList<String> arr = new ArrayList<String>();;
	String[] tempArray;
	ArrayList<String> temparr = new ArrayList<String>();
	ArrayList<String> arrTransfer = new ArrayList<String>();
	// String[] arrTotal = new String[307];
	double[] arrTotal = new double[253];
	String[] strValue = new String[250];
	private static final IntentFilter FILTER = new IntentFilter(
			WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
	
	  StringBuilder sb;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (android.provider.Settings.System.getInt(getContentResolver(),
				Settings.System.ACCELEROMETER_ROTATION, 0) == 1) {
			android.provider.Settings.System.putInt(getContentResolver(),
					Settings.System.ACCELEROMETER_ROTATION, 0);
		}
		setContentView(R.layout.activity_main);
		xplusbutton = (Button) findViewById(R.id.button1);
		xminusbutton = (Button) findViewById(R.id.button2);
		yplusbutton = (Button) findViewById(R.id.button3);
		yminusbutton = (Button) findViewById(R.id.button4);
		start =(Button) findViewById(R.id.button_refresh);
		makesure =(Button) findViewById(R.id.makesure);
		
		send = (Button) findViewById(R.id.send);
		send.setEnabled(false);
		start.setEnabled(false);
		
		

		xplusbutton.setOnClickListener(this);
		xminusbutton.setOnClickListener(this);
		yplusbutton.setOnClickListener(this);
		yminusbutton.setOnClickListener(this);
		send.setOnClickListener(this);
		makesure.setOnClickListener(this);

		editx = (EditText) findViewById(R.id.editText1);
		edity = (EditText) findViewById(R.id.editText2);
		server_ip = (EditText) findViewById(R.id.Ip_edit);
		showMsg =(TextView) findViewById(R.id.textView3);
		
		
		 handler=new Handler();

		mTextView = (TextView) findViewById(R.id.text_id);
	
		
		
		
	}

	@Override
	public void onStart() {
		super.onStart();
		// Register the scan receiver
		
	}

	int xcounter = 0;
	int ycounter = 0;

	public void onClick(View v) {
		colNumber++;
		rowNumber = 1;
		if (android.provider.Settings.System.getInt(getContentResolver(),
				Settings.System.ACCELEROMETER_ROTATION, 0) == 1) {
			android.provider.Settings.System.putInt(getContentResolver(),
					Settings.System.ACCELEROMETER_ROTATION, 0);
		}

		if (v == xplusbutton) {
			xcounter++;
			editx.setText(Integer.toString(xcounter));
		}
		if (v == xminusbutton) {
			xcounter--;
			editx.setText(Integer.toString(xcounter));
		}

		if (v == yplusbutton) {
			ycounter++;
			edity.setText(Integer.toString(ycounter));
		}
		if (v == yminusbutton) {
			ycounter--;
			edity.setText(Integer.toString(ycounter));
		}
		if (v == send) {
			makesure.setEnabled(true);
			send.setEnabled(false);
			editx.setEnabled(true);
			edity.setEnabled(true);
			xminusbutton.setEnabled(true);
		    xplusbutton.setEnabled(true);
			yplusbutton.setEnabled(true);
			yminusbutton.setEnabled(true);
			editx.setText("");
			edity.setText("");
			xcounter=0;
			ycounter=0;
			pw.println(sb.toString());
			pw.flush();
			sb=null;
			
		}
		if (v == makesure) {
			Server_Ip=server_ip.getText().toString();
			if(Server_Ip==null)return;
			if(Server_Ip!=null&&click_num==0){
			   makesure.setText("确认坐标之后开始");
			   server_ip.setEnabled(false);
			   click_num++;
			   //只启动一次socket避免开启过多线程
				new Thread(this).start();
			}
			makesure.setEnabled(false);
			start.setEnabled(true);
			editx.setEnabled(false);
			edity.setEnabled(false);
			xminusbutton.setEnabled(false);
		    xplusbutton.setEnabled(false);
			yplusbutton.setEnabled(false);
			yminusbutton.setEnabled(false);
			
		}
	}

	@Override
	public void onStop() {
		super.onStop();
		// Unregister the receiver
		unregisterReceiver(scanReceiver);
	}

	// The Refresh Button
	// As you press this button, the signal strength will be started to calculate again
	//已在layout中设置
	public void onClickRefresh(View v) {
		count = 0;
		index = 0;
		wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
		registerReceiver(scanReceiver, FILTER);
		sb=new StringBuilder();
		arrTotal = new double[13];
		strValue = new String[13];
		mTextView.setText("开始");
		arr = new ArrayList<String>();
		wifiManager.startScan();
		start.setEnabled(false);
	
		
		
	}

	BroadcastReceiver scanReceiver = new BroadcastReceiver() {
		List<ScanResult> wifiScanList;
		int k=3;
		int sum=0;
		@Override
		public void onReceive(Context context, Intent intent) {
			// Neglect first 5 signal strength
			if (count < 5) {
				wifiScanList = wifiManager.getScanResults();
				for (ScanResult result : wifiScanList) {
				}
				mTextView.setText("忽略"+(count+1)+"次");
				count++;
				wifiManager.startScan();
			}
			else if (count < 15) {
				int SignalR1 = 0, SignalR2 = 0, SignalR3 = 0, SignalR4 = 0, SignalR5 = 0;
				int SignalR6 = 0, SignalR7 = 0, SignalR8 = 0, SignalR9 = 0, SignalR10 = 0;
				wifiScanList = wifiManager.getScanResults();
				
				for (ScanResult result : wifiScanList) {

					if (result.SSID.equals("book1")) {
						SignalR1 = result.level * (-1);
					}
					//test
					if (result.SSID.equals("book2")) {
						SignalR2 =result.level * (-1);
					}
					if (result.SSID.equals("book3")) {
						SignalR3 = result.level * (-1);
					}
					//test
					if (result.SSID.equals("book4")) {
						SignalR4 =result.level * (-1) ;
					}
					if (result.SSID.equals("book5")) {
						SignalR5 = result.level * (-1);
					}
					//test
					if (result.SSID.equals("book6")) {
						SignalR6 =result.level * (-1);
					}
					if (result.SSID.equals("book7")) {
						SignalR7 = result.level * (-1);
					}
					//test
					if (result.SSID.equals("book8")) {
						SignalR8 =result.level * (-1) ;
					}
					if (result.SSID.equals("book9")) {
						SignalR9 = result.level * (-1);
					}
					//test
					if (result.SSID.equals("book10")) {
						SignalR10 =result.level * (-1) ;
					}
				}
//				if (k == 3) {
					sum=(SignalR1 + SignalR2 + SignalR3
							+ SignalR4 + SignalR5 + SignalR6 + SignalR7 + SignalR8 + SignalR9 + SignalR10);
					arrTotal[index] = sum;
					
					strValue[index] = "("+SignalR1+","+ SignalR2+","+SignalR3+","+SignalR4+","+SignalR5+","+ SignalR6+","+SignalR7+ ","+ SignalR8+","+SignalR9+ "," +SignalR10+")"+">SUM-";
					
					mTextView.setText("第" + (count - 4) + "次测试" + "  ||| "
							+ "dBm值:" + arrTotal[index]);
					index++;
					count++;
//					k = 0;
//				} else {
//					k++;
//					mTextView.setText("忽略" + k + "次");
//				}
				wifiManager.startScan();
			//测试15次后计算 开始计算5到15次测试的结果
			} else if (count == 15) {
		       
		      
				int sum = 0;
				double avg = 0;
				for(int i=0;i<10;i++){
					sum+=arrTotal[i];
					sb.append(strValue[i]);
					sb.append(arrTotal[i]+":");
				}
				//得出平均值
				avg = sum /10.0;
				mTextView.setText("测试结束,"+"坐标"+"("+xcounter+","+ycounter+")"+"平均dBm:"+avg);
				sb.append(avg+":");
				sb.append(xcounter+":");
				//防止数据粘包
				sb.append(ycounter+":");
//				MainActivity.showMsg.setText(sb);
				send.setEnabled(true);
				
			}

		}
	};
	public void run() {
		 try {
				socket=new Socket(Server_Ip,5648); 

			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.e("socket","unknown host");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.e("socket","io execption");
			}
		if (socket == null) {
			unregisterReceiver(scanReceiver);
			mTextView.setText("未获取服务器连接");

			Log.e("socket", "null");
		}
	        else
	        	try {
				pw=new PrintWriter(socket.getOutputStream());
				br=new BufferedReader(new InputStreamReader(socket.getInputStream()));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		try {
			String str;
			while((str=br.readLine())!=null){
				final String s=str;
				handler.post(new Runnable(){

					public void run() {
						Toast.makeText(MainActivity.this, s, Toast.LENGTH_LONG).show();
						
					}});
				
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
