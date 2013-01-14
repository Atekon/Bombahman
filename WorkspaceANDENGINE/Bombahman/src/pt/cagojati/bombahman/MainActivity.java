package pt.cagojati.bombahman;

import java.net.UnknownHostException;

import org.andengine.extension.multiplayer.protocol.util.WifiUtils;
import org.andengine.util.debug.Debug;

import pt.cagojati.bombahman.multiplayer.BluetoothListDevicesActivity;
import pt.cagojati.bombahman.multiplayer.BluetoothLobbyServer;
import pt.cagojati.bombahman.multiplayer.BluetoothRequestCodes;
import pt.cagojati.bombahman.multiplayer.ILobbyServer;
import pt.cagojati.bombahman.multiplayer.WiFiLobbyServer;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
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
	public static boolean isWifi = true;
	
	BluetoothAdapter mBluetoothAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		Bundle bundle = getIntent().getExtras();
		isWifi = bundle.getBoolean("isWifi");
		
		Button btnLobby = (Button) this.findViewById(R.id.CreateLobbyButton);
		btnLobby.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(isWifi)
				{
					ILobbyServer lobbyServer = WiFiLobbyServer.getSingletonObject();
					lobbyServer.initServer();
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
				}else{
					ILobbyServer lobbyServer = BluetoothLobbyServer.getSingletonObject();
					lobbyServer.initServer();
					
				}
				
				try {
					Thread.sleep(500);
				} catch (final Throwable t) {
					Debug.e(t);
				}
				Intent intent = new Intent(MainActivity.this, LobbyActivity.class);
				Bundle bundle = new Bundle();
	            bundle.putBoolean("isWiFi", isWifi);
	            if(isWifi == true){
	            	bundle.putString("ip", "127.0.0.1");
	            }else{
	            	mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	            	bundle.putString("ip",mBluetoothAdapter.getAddress());
	            }
	            intent.putExtras(bundle);
				startActivity(intent);
			}
		});
		
		Button btnJoinLobby = (Button) this.findViewById(R.id.JoinLobbyButton);
		btnJoinLobby.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//Asks Server Ip and Joins
				//testing purposes only
				final EditText input = new EditText(MainActivity.this);
				if(isWifi){
					new AlertDialog.Builder(MainActivity.this)
				    .setTitle("Join")
				    .setMessage("Insert Ip")
				    .setView(input)
				    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
				        public void onClick(DialogInterface dialog, int whichButton) {
				            Editable value = input.getText(); 
				            
				            Intent intent = new Intent(MainActivity.this, LobbyActivity.class);
							Bundle bundle = new Bundle();
				            bundle.putBoolean("isWiFi", isWifi);
				            bundle.putString("ip", value.toString());
				            intent.putExtras(bundle);
							startActivity(intent);
				        }
				    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				        public void onClick(DialogInterface dialog, int whichButton) {
				            // Do nothing.
				        }
				    }).show();
				}else{
					Intent intent = new Intent(MainActivity.this, BluetoothListDevicesActivity.class);
					MainActivity.this.startActivityForResult(intent, BluetoothRequestCodes.REQUESTCODE_BLUETOOTH_CONNECT);
				}
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		switch(requestCode)
		{
		case BluetoothRequestCodes.REQUESTCODE_BLUETOOTH_CONNECT:
			Intent intent = new Intent(MainActivity.this, LobbyActivity.class);
			Bundle bundle = new Bundle();
	        bundle.putBoolean("isWiFi", isWifi);
	        bundle.putString("ip", data.getExtras().getString(BluetoothListDevicesActivity.EXTRA_DEVICE_ADDRESS));
	        intent.putExtras(bundle);
			startActivity(intent);
			break;
		default:
			super.onActivityResult(requestCode, resultCode, data);
		}
	}
	
	
}
