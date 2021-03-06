package pt.cagojati.bombahman;

import java.net.UnknownHostException;

import org.andengine.extension.multiplayer.protocol.util.WifiUtils;
import org.andengine.util.debug.Debug;

import pt.cagojati.bombahman.multiplayer.IMultiplayerServer;
import pt.cagojati.bombahman.multiplayer.WiFiConnector;
import pt.cagojati.bombahman.multiplayer.WiFiServer;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		Button btnJoin = (Button) this.findViewById(R.id.JoinWiFiButton);
		btnJoin.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//Asks Server Ip and Joins
				//testing purposes only
				final EditText input = new EditText(MainActivity.this);
				new AlertDialog.Builder(MainActivity.this)
			    .setTitle("Join")
			    .setMessage("Insert Ip")
			    .setView(input)
			    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			        public void onClick(DialogInterface dialog, int whichButton) {
			            Editable value = input.getText(); 
			            
			            Intent intent = new Intent(MainActivity.this, GameActivity.class);
						Bundle bundle = new Bundle();
			            bundle.putBoolean("isWiFi", true);
			            bundle.putString("ip", value.toString());
			            intent.putExtras(bundle);
						startActivity(intent);
			        }
			    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			        public void onClick(DialogInterface dialog, int whichButton) {
			            // Do nothing.
			        }
			    }).show();
			}
		});
		
		Button btnCreate = (Button) this.findViewById(R.id.CreateWifi);
		btnCreate.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//Creates Server and then proceeds to join it
				
				IMultiplayerServer server = WiFiServer.getSingletonObject();
				server.initServer();
				MainActivity.this.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						try {
							Toast.makeText(MainActivity.this, WifiUtils.getWifiIPv4Address(MainActivity.this), Toast.LENGTH_SHORT).show();
						} catch (UnknownHostException e) {
							e.printStackTrace();
						}
					}
				});
				
				try {
					Thread.sleep(500);
				} catch (final Throwable t) {
					Debug.e(t);
				}
				Intent intent = new Intent(MainActivity.this, GameActivity.class);
				Bundle bundle = new Bundle();
	            bundle.putBoolean("isWiFi", true);
	            bundle.putString("ip", "127.0.0.1");
	            intent.putExtras(bundle);
				startActivity(intent);
				
			}
		});
	}

}
