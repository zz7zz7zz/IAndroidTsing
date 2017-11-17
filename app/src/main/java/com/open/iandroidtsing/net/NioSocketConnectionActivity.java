package com.open.iandroidtsing.net;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

import com.open.iandroidtsing.R;
import com.open.iandroidtsing.net.listener.IConnectReceiveListener;
import com.open.iandroidtsing.net.data.Message;
import com.open.iandroidtsing.net.client.NioClient;
import com.open.iandroidtsing.net.data.TcpAddress;

public class NioSocketConnectionActivity extends Activity {

	private NioClient mConnection =null;
	private EditText ip,port,sendContent,recContent;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.net_socket_connection);
		initView();
	}
	 
	
	private void initView()
	{
		findViewById(R.id.open).setOnClickListener(listener);
		findViewById(R.id.close).setOnClickListener(listener);
		findViewById(R.id.reconn).setOnClickListener(listener);
		findViewById(R.id.send).setOnClickListener(listener);
		findViewById(R.id.clear).setOnClickListener(listener);
		
		ip=(EditText) findViewById(R.id.ip);
		port=(EditText) findViewById(R.id.port);
		sendContent=(EditText) findViewById(R.id.sendContent);
		recContent=(EditText) findViewById(R.id.recContent);
		
		ip.setText("192.168.123.1");
		port.setText("9999");

		mConnection = new NioClient(new TcpAddress[]{new TcpAddress(ip.getText().toString(), Integer.valueOf(port.getText().toString()))},socketListener);
	}

	private OnClickListener listener=new OnClickListener() {

		@Override
		public void onClick(View v) {
			mConnection.setConnectAddress(new TcpAddress[]{new TcpAddress(ip.getText().toString(), Integer.valueOf(port.getText().toString()))});
			switch(v.getId())
			{
				case R.id.open:
					mConnection.connect();
					break;
					
				case R.id.close:
					mConnection.disconnect();
					break;
					
				case R.id.reconn:
					mConnection.reconnect();
					break;
					
				case R.id.send:
					Message packet=new Message();
					packet.pack(sendContent.getText().toString());
					mConnection.sendMessage(packet);
					sendContent.setText("");
					break;
					
				case R.id.clear:
					recContent.setText("");
					break;
			}
		}
	};

	private IConnectReceiveListener socketListener=new IConnectReceiveListener() {

		@Override
		public void onConnectionReceive(final String txt) {
			runOnUiThread(new Runnable() {
				public void run() {
					recContent.getText().append(txt).append("\r\n");
				}
			});
		}
	};

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		mConnection.disconnect();
	}
}
