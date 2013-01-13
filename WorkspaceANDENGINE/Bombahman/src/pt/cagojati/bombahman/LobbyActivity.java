package pt.cagojati.bombahman;

import java.io.IOException;

import org.andengine.extension.multiplayer.protocol.adt.message.IMessage;
import org.andengine.extension.multiplayer.protocol.util.MessagePool;
import org.andengine.opengl.vbo.LowMemoryVertexBufferObject;

import pt.cagojati.bombahman.multiplayer.ILobbyConnector;
import pt.cagojati.bombahman.multiplayer.ILobbyServer;
import pt.cagojati.bombahman.multiplayer.IMultiplayerServer;
import pt.cagojati.bombahman.multiplayer.WiFiConnector;
import pt.cagojati.bombahman.multiplayer.WiFiLobbyConnector;
import pt.cagojati.bombahman.multiplayer.WiFiLobbyServer;
import pt.cagojati.bombahman.multiplayer.WiFiServer;
import pt.cagojati.bombahman.multiplayer.messages.AddPlayerClientMessage;
import pt.cagojati.bombahman.multiplayer.messages.MessageFlags;
import pt.cagojati.bombahman.multiplayer.messages.PlayerReadyClientMessage;
import pt.cagojati.bombahman.multiplayer.messages.PlayerReadyServerMessage;

import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.view.Display;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class LobbyActivity extends Activity {

	private ILobbyConnector mConnector;
	private static int mPlayerId;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_lobby);
		Bundle bundle = getIntent().getExtras();
		if(bundle.getBoolean("isWiFi")){
			mConnector = new WiFiLobbyConnector(bundle.getString("ip"));
		}
		mConnector.setActivity(LobbyActivity.this);
		mConnector.initClient();
		
		//set views click listeners and resize them
		Button btn_setTime = (Button) this.findViewById(R.id.SetTime_Btn);
		btn_setTime.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				//create horizontal linearLayout with 2 number pickers
				LinearLayout timeLayout = new LinearLayout(LobbyActivity.this);
				timeLayout.setOrientation(LinearLayout.HORIZONTAL);
				timeLayout.setGravity(Gravity.CENTER);
				NumberPicker time_minutes = new NumberPicker(LobbyActivity.this);
				time_minutes.setRange(1, 3);
				//disable keyboard in numberpicker
				time_minutes.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
				time_minutes.setLayoutParams(new NumberPicker.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0.5f));
				NumberPicker time_seconds = new NumberPicker(LobbyActivity.this);
				time_seconds.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
				time_seconds.setLayoutParams(new NumberPicker.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0.5f));
				time_seconds.setRange(0, 59);
				timeLayout.addView(time_minutes);
				timeLayout.addView(time_seconds);
				
				//create and show layout
				new AlertDialog.Builder(LobbyActivity.this)
			    .setTitle("Set Game Time")
			    .setView(timeLayout)
			    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			        public void onClick(DialogInterface dialog, int whichButton) {
			            
			        }
			    }).show();
								
			}
		});
		
		//get screen size
		Display display = getWindowManager().getDefaultDisplay(); 
		int screenWidth = display.getWidth();  // deprecated
		int screenHeight = display.getHeight();
		
		//set height of map layout (image + buttons + map name)
		LinearLayout mapSelection = (LinearLayout) this.findViewById(R.id.Map_selection);
		LayoutParams params = mapSelection.getLayoutParams();
		params.height =(int) (screenHeight * 0.4); 
		mapSelection.setLayoutParams(params);
		
		//set height of options layout
		LinearLayout options = (LinearLayout) this.findViewById(R.id.OptionsVertical);
		params = options.getLayoutParams();
		params.height =(int) (screenHeight * 0.4);
		options.setLayoutParams(params);
		
//		Button ready_btn = (Button) this.findViewById(R.id.ReadyBtn);
//		ready_btn.setHeight((int)(screenHeight*0.1));
//		ready_btn.setLayoutParams(params);
		
		//Set height of the map's image
		ImageView mapImage = (ImageView) this.findViewById(R.id.MapImg);
		params = mapImage.getLayoutParams();
		params.height = (int) (screenHeight*0.33);
		mapImage.setLayoutParams(params);
		
		Button ready_btn = (Button) this.findViewById(R.id.ReadyBtn);
		ready_btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				MessagePool<IMessage> messagePool = mConnector.getMessagePool();
				PlayerReadyClientMessage ready_msg = (PlayerReadyClientMessage) messagePool.obtainMessage(MessageFlags.FLAG_MESSAGE_CLIENT_PLAYER_READY);
				ready_msg.set(mPlayerId, true);
				mConnector.sendClientMessage(ready_msg);
			}
		});
	}
	
	public void setPlayerReady(int playerId)
	{
		switch(playerId)
		{
			case 0:
				TextView player1_name = (TextView) this.findViewById(R.id.Player1Name);
				player1_name.setTextColor(Color.GREEN);
				break;
			case 1:
				TextView player2_name = (TextView) this.findViewById(R.id.Player2Name);
				player2_name.setTextColor(Color.GREEN);
				break;
			case 2:
				TextView player3_name = (TextView) this.findViewById(R.id.Player3Name);
				player3_name.setTextColor(Color.GREEN);
				break;
			case 3:
				TextView player4_name = (TextView) this.findViewById(R.id.Player4Name);
				player4_name.setTextColor(Color.GREEN);
				break;	
		}
	}
	
	public void setPlayerImage(int playerId)
	{
		switch(playerId)
		{
			case 0:
				ImageView player1_name = (ImageView) this.findViewById(R.id.ImgPlayer1);
				player1_name.setImageDrawable(getResources().getDrawable(R.drawable.ic_launcher));
				break;
			case 1:
				ImageView player2_name = (ImageView) this.findViewById(R.id.ImgPlayer2);
				//Set player's image
				break;
			case 2:
				ImageView player3_name = (ImageView) this.findViewById(R.id.ImgPlayer3);
				//Set player's image
				break;
			case 3:
				ImageView player4_name = (ImageView) this.findViewById(R.id.ImgPlayer4);
				//Set player's image
				break;	
		}
	}
	
	public void setPlayerId(int playerId)
	{
		mPlayerId = playerId;
	}

}
