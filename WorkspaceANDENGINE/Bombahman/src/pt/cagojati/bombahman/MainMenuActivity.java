package pt.cagojati.bombahman;

import pt.cagojati.bombahman.multiplayer.BluetoothListDevicesActivity;
import pt.cagojati.bombahman.multiplayer.BluetoothLobbyConnector;
import pt.cagojati.bombahman.multiplayer.BluetoothRequestCodes;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainMenuActivity extends Activity {
	
	BluetoothAdapter mBluetoothAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_menu);
		
		Button btnWifi = (Button) this.findViewById(R.id.WifiBtn);
		btnWifi.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainMenuActivity.this, MainActivity.class);
				Bundle bundle = new Bundle();
	            bundle.putBoolean("isWifi", true);
	            intent.putExtras(bundle);
				startActivity(intent);
			}
		});
		
		Button btnBluetooth = (Button) this.findViewById(R.id.BluetoothBtn);
		btnBluetooth.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				MainMenuActivity.this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
				if (!mBluetoothAdapter.isEnabled()) {
				    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				    startActivityForResult(enableBtIntent, BluetoothRequestCodes.REQUESTCODE_BLUETOOTH_ENABLE);
				}else{
					Intent intent = new Intent(MainMenuActivity.this, MainActivity.class);
					Bundle bundle = new Bundle();
		            bundle.putBoolean("isWifi", false);
		            intent.putExtras(bundle);
					startActivity(intent);
				}
			}
		});
	}
	
	@Override
	protected void onActivityResult(final int pRequestCode, final int pResultCode, final Intent pData){
		switch (pRequestCode) {
		case BluetoothRequestCodes.REQUESTCODE_BLUETOOTH_ENABLE:
			Intent intent = new Intent(MainMenuActivity.this, MainActivity.class);
			Bundle bundle = new Bundle();
            bundle.putBoolean("isWifi", false);
            intent.putExtras(bundle);
			startActivity(intent);
			break;
		case BluetoothRequestCodes.REQUESTCODE_BLUETOOTH_CONNECT:
			break;
		default:
			super.onActivityResult(pRequestCode, pRequestCode, pData);
		}
	}

}
