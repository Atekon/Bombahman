package pt.cagojati.bombahman;

import java.io.IOException;
import java.util.Iterator;

import org.andengine.extension.multiplayer.protocol.adt.message.IMessage;
import org.andengine.extension.multiplayer.protocol.util.MessagePool;
import org.andengine.opengl.vbo.LowMemoryVertexBufferObject;
import org.andengine.util.debug.Debug;

import com.badlogic.gdx.physics.box2d.Body;

import pt.cagojati.bombahman.multiplayer.BluetoothListDevicesActivity;
import pt.cagojati.bombahman.multiplayer.BluetoothLobbyConnector;
import pt.cagojati.bombahman.multiplayer.BluetoothLobbyServer;
import pt.cagojati.bombahman.multiplayer.BluetoothRequestCodes;
import pt.cagojati.bombahman.multiplayer.ILobbyConnector;
import pt.cagojati.bombahman.multiplayer.ILobbyServer;
import pt.cagojati.bombahman.multiplayer.IMultiplayerServer;
import pt.cagojati.bombahman.multiplayer.WiFiConnector;
import pt.cagojati.bombahman.multiplayer.WiFiLobbyConnector;
import pt.cagojati.bombahman.multiplayer.WiFiLobbyServer;
import pt.cagojati.bombahman.multiplayer.WiFiServer;
import pt.cagojati.bombahman.multiplayer.messages.AddPlayerClientMessage;
import pt.cagojati.bombahman.multiplayer.messages.ConnectionCloseServerMessage;
import pt.cagojati.bombahman.multiplayer.messages.CurrentMapServerMessage;
import pt.cagojati.bombahman.multiplayer.messages.CurrentTimeServerMessage;
import pt.cagojati.bombahman.multiplayer.messages.JoinServerMessage;
import pt.cagojati.bombahman.multiplayer.messages.MessageFlags;
import pt.cagojati.bombahman.multiplayer.messages.PlayerReadyClientMessage;
import pt.cagojati.bombahman.multiplayer.messages.PlayerReadyServerMessage;
import pt.cagojati.bombahman.multiplayer.messages.SetPowerupsServerMessage;

import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class LobbyActivity extends Activity {

	private ILobbyConnector mConnector;
	private static int mPlayerId;
	private static boolean mIsReady = false;
	BluetoothAdapter mBluetoothAdapter;

	
	//Just for INDES
	private static String[] mapNames = {"map", "map2", "map3", "map4"};
	private static int[] maps = {R.drawable.map, R.drawable.map2, R.drawable.map3, R.drawable.map4};
	private static int currentMap =0;
	private int currentTime = 0;
	private boolean currentPowerups = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_lobby);
		
		//set views click listeners and resize them
		Button btn_setTime = (Button) this.findViewById(R.id.SetTime_Btn);
		btn_setTime.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				//create horizontal linearLayout with 2 number pickers
				LinearLayout timeLayout = new LinearLayout(LobbyActivity.this);
				timeLayout.setOrientation(LinearLayout.HORIZONTAL);
				timeLayout.setGravity(Gravity.CENTER);
				final NumberPicker time_minutes = new NumberPicker(LobbyActivity.this);
				time_minutes.setRange(1, 3);
				//disable keyboard in numberpicker
				time_minutes.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
				time_minutes.setLayoutParams(new NumberPicker.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0.5f));
				final NumberPicker time_seconds = new NumberPicker(LobbyActivity.this);
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
			        	//get time and send it
			        	int timeInSeconds=0;
			        	timeInSeconds = time_minutes.mCurrent*60 + time_seconds.mCurrent;
			        	LobbyActivity.this.setCurrentTime(timeInSeconds);
			        	MessagePool<IMessage> messagePool = mConnector.getMessagePool();
						CurrentTimeServerMessage currentTime_msg = (CurrentTimeServerMessage) messagePool.obtainMessage(MessageFlags.FLAG_MESSAGE_SERVER_CURRENT_TIME);
						currentTime_msg.setCurrentTime(timeInSeconds);
						LobbyActivity.this.currentTime = timeInSeconds;
						try {
							WiFiLobbyServer.getSingletonObject().sendBroadcastServerMessage(currentTime_msg);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						messagePool.recycleMessage(currentTime_msg);
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
		//set default map
		mapImage.setImageResource(maps[currentMap]);
		//set default map's name
		TextView mapName = (TextView) this.findViewById(R.id.MapName);
		mapName.setText(mapNames[currentMap]);
		
		Button ready_btn = (Button) this.findViewById(R.id.ReadyBtn);
		ready_btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if(mPlayerId!=0){
					mIsReady = !mIsReady;
					MessagePool<IMessage> messagePool = mConnector.getMessagePool();
					PlayerReadyClientMessage ready_msg = (PlayerReadyClientMessage) messagePool.obtainMessage(MessageFlags.FLAG_MESSAGE_CLIENT_PLAYER_READY);
					ready_msg.set(mPlayerId, mIsReady);
					Button ready_btn = (Button) LobbyActivity.this.findViewById(R.id.ReadyBtn);
					if(mIsReady)
					{
						if(mPlayerId!=0)
							ready_btn.setText("Unready");
					}
					else
					{
						if(mPlayerId!=0)
							ready_btn.setText("Ready");
					}
					mConnector.sendClientMessage(ready_msg);
				messagePool.recycleMessage(ready_msg);
				}else{
					if(LobbyActivity.this.mConnector.getPlayerCount() != 0){
						WiFiLobbyServer lserver = WiFiLobbyServer.getSingletonObject();
						IMultiplayerServer server = WiFiServer.getSingletonObject();
						server.setHostsAddreses(lserver.getClientList());
						server.initServer();
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						MessagePool<IMessage> messagePool = mConnector.getMessagePool();
						JoinServerMessage ready_msg = (JoinServerMessage) messagePool.obtainMessage(MessageFlags.FLAG_MESSAGE_SERVER_JOIN);
						try {
							lserver.sendBroadcastServerMessage(ready_msg);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						messagePool.recycleMessage(ready_msg);
					}
				}
			}
		});
		
		//set map selection buttons listeners
		Button nextMap = (Button) this.findViewById(R.id.NextMap_Btn);
		nextMap.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if(mPlayerId==0)
				{
					if(currentMap==3)
					{
						currentMap=0;
					}
					else
					{
						currentMap++;
					}
					MessagePool<IMessage> messagePool = mConnector.getMessagePool();
					CurrentMapServerMessage currentMap_msg = (CurrentMapServerMessage) messagePool.obtainMessage(MessageFlags.FLAG_MESSAGE_SERVER_CURRENT_MAP);
					currentMap_msg.setCurrentMap(currentMap);
					try {
						WiFiLobbyServer.getSingletonObject().sendBroadcastServerMessage(currentMap_msg);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					messagePool.recycleMessage(currentMap_msg);
				}
			}
		});
		
		Button previousMap = (Button) this.findViewById(R.id.PreviousMap_Btn);
		previousMap.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if(mPlayerId==0)
				{
					if(currentMap==0)
					{
						currentMap=3;
					}
					else
					{
						currentMap--;
					}
					MessagePool<IMessage> messagePool = mConnector.getMessagePool();
					CurrentMapServerMessage currentMap_msg = (CurrentMapServerMessage) messagePool.obtainMessage(MessageFlags.FLAG_MESSAGE_SERVER_CURRENT_MAP);
					currentMap_msg.setCurrentMap(currentMap);
					try {
						WiFiLobbyServer.getSingletonObject().sendBroadcastServerMessage(currentMap_msg);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					messagePool.recycleMessage(currentMap_msg);
				}
			}
		});
		
		//set powerup checkbox handler
		final CheckBox powerups = (CheckBox) this.findViewById(R.id.Powerups);
		powerups.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if(mPlayerId==0){
					LobbyActivity.this.setCurrentPowerups(powerups.isChecked());
					MessagePool<IMessage> messagePool = mConnector.getMessagePool();
					SetPowerupsServerMessage setPowerups_msg = (SetPowerupsServerMessage) messagePool.obtainMessage(MessageFlags.FLAG_MESSAGE_SERVER_SET_POWERUPS);
					setPowerups_msg.setPowerups(powerups.isChecked());
					try {
						WiFiLobbyServer.getSingletonObject().sendBroadcastServerMessage(setPowerups_msg);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					messagePool.recycleMessage(setPowerups_msg);
				}
			}
			
		});
		
		Bundle bundle = getIntent().getExtras();
		if(bundle.getBoolean("isWiFi")){
			mConnector = new WiFiLobbyConnector(bundle.getString("ip"));
			mConnector.setActivity(LobbyActivity.this);
			mConnector.initClient();
		}else{
			mConnector = new BluetoothLobbyConnector(bundle.getString("ip"));
			mConnector.setActivity(LobbyActivity.this);
			mConnector.initClient();
		}
	}
	
	public void setPlayerReady(final int playerId, final boolean isReady)
	{
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				TextView txt = null;
				switch(playerId)
				{
					case 0:
						txt = (TextView) LobbyActivity.this.findViewById(R.id.Player1Name);
						break;
					case 1:
						txt = (TextView) LobbyActivity.this.findViewById(R.id.Player2Name);
						break;
					case 2:
						txt = (TextView) LobbyActivity.this.findViewById(R.id.Player3Name);
						break;
					case 3:
						txt = (TextView) LobbyActivity.this.findViewById(R.id.Player4Name);
						break;	
				}
				int color;
				if(isReady)
				{
					txt.setTextColor(Color.GREEN);
				}
				else
				{
					txt.setTextColor(Color.BLACK);
				}
			}
		});
	}
	
	public void addPlayer(final int playerId)
	{
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				ImageView img = null;
				int playerImage =0;
				switch(playerId)
				{
					case 0:
						img = (ImageView) LobbyActivity.this.findViewById(R.id.ImgPlayer1);
						playerImage = R.drawable.player1lobby;
						break;
					case 1:
						img = (ImageView) LobbyActivity.this.findViewById(R.id.ImgPlayer2);
						playerImage = R.drawable.player2lobby;
						break;
					case 2:
						img = (ImageView) LobbyActivity.this.findViewById(R.id.ImgPlayer3);
						playerImage = R.drawable.player3lobby;
						break;
					case 3:
						img = (ImageView) LobbyActivity.this.findViewById(R.id.ImgPlayer4);
						playerImage = R.drawable.player4lobby;
						break;	
				}
				img.setImageDrawable(getResources().getDrawable(playerImage));
			}
		});
		
	}
	
	public void setPlayerId(int playerId)
	{
		mPlayerId = playerId;
		if(mPlayerId == 0)
		{
			runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					Button ready_btn = (Button) LobbyActivity.this.findViewById(R.id.ReadyBtn);
					ready_btn.setText("Start");
				}
			});
		}
		else
		{
			runOnUiThread(new Runnable() {
							
				@Override
				public void run() {
					//disable options
					Button previousMap_btn = (Button) LobbyActivity.this.findViewById(R.id.PreviousMap_Btn);
					previousMap_btn.setEnabled(false);
					Button nextMap_btn = (Button) LobbyActivity.this.findViewById(R.id.NextMap_Btn);
					nextMap_btn.setEnabled(false);
					Button setTime_btn = (Button) LobbyActivity.this.findViewById(R.id.SetTime_Btn);
					setTime_btn.setEnabled(false);
					CheckBox powerups_check = (CheckBox) LobbyActivity.this.findViewById(R.id.Powerups);
					powerups_check.setEnabled(false);
				}
			});
		}
	}

	public void setNumOfPlayer(int numPlayers) {
		for(int i =0; i<numPlayers; i++)
		{
			addPlayer(i);
		}
	}
	
	public void setPastReadyPlayers(boolean[] mReadyPlayers)
	{
		for(int i=0; i<mReadyPlayers.length; i++)
		{
			if(mReadyPlayers[i]==true)
			{
				setPlayerReady(i, mReadyPlayers[i]);
			}
		}
	}
	
	public void setCurrentMap(int currentMap)
	{
		this.currentMap = currentMap;
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				ImageView mapImage = (ImageView) LobbyActivity.this.findViewById(R.id.MapImg);
				mapImage.setImageResource(maps[LobbyActivity.this.currentMap]);
				//set default map's name
				TextView mapName = (TextView) LobbyActivity.this.findViewById(R.id.MapName);
				mapName.setText(mapNames[LobbyActivity.this.currentMap]);
				
			}
		});
	}

	public void setCurrentTime(final int time) {
		if(mPlayerId!=0)
		{
			//convert time to minutes and seconds
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					int minutes = (int) Math.floor(time/60);
					int seconds = time-minutes*60;
					Button time_btn = (Button) LobbyActivity.this.findViewById(R.id.SetTime_Btn);
					time_btn.setText("Time: "+minutes+":"+seconds);
				}
			});
		}
		
	}

	public void setPowerups(final boolean powerupsEnable) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				CheckBox powerups = (CheckBox) LobbyActivity.this.findViewById(R.id.Powerups);
				powerups.setChecked(powerupsEnable);
				
			}
		});
	}

	public void removePlayer(final int playerId) {
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				ImageView img = null;
				TextView txt = null;
				switch(playerId)
				{
					case 0:
						img = (ImageView) LobbyActivity.this.findViewById(R.id.ImgPlayer1);
						txt = (TextView) LobbyActivity.this.findViewById(R.id.Player1Name);
						break;
					case 1:
						img = (ImageView) LobbyActivity.this.findViewById(R.id.ImgPlayer2);
						txt = (TextView) LobbyActivity.this.findViewById(R.id.Player2Name);
						break;
					case 2:
						img = (ImageView) LobbyActivity.this.findViewById(R.id.ImgPlayer3);
						txt = (TextView) LobbyActivity.this.findViewById(R.id.Player3Name);
						break;
					case 3:
						img = (ImageView) LobbyActivity.this.findViewById(R.id.ImgPlayer4);
						txt = (TextView) LobbyActivity.this.findViewById(R.id.Player4Name);
						break;	
				}
				img.setImageDrawable(getResources().getDrawable(R.drawable.playerunknown));
				txt.setTextColor(Color.BLACK);
			}
		});
		
	}
	
	@Override
	protected void onDestroy() {

		if(WiFiLobbyServer.isInitialized()) {
			WiFiLobbyServer server = WiFiLobbyServer.getSingletonObject();

			try {
				server.sendBroadcastServerMessage(new ConnectionCloseServerMessage());
			} catch (final IOException e) {
				Debug.e(e);
			}
			server.terminate();
		}
		
		if(BluetoothLobbyServer.isInitialized()) {
			BluetoothLobbyServer server = BluetoothLobbyServer.getSingletonObject();

			try {
				server.sendBroadcastServerMessage(new ConnectionCloseServerMessage());
			} catch (final IOException e) {
				Debug.e(e);
			}
			server.terminate();
		}
		

		if(LobbyActivity.this.mConnector != null) {
			LobbyActivity.this.mConnector.terminate();
		}

		super.onDestroy();
	}

	public boolean isCurrentPowerups() {
		return currentPowerups;
	}

	public void setCurrentPowerups(boolean currentPowerups) {
		this.currentPowerups = currentPowerups;
	}

	public int getCurrentTime() {
		return currentTime;
	}
	
	public String getCurrentMapName(){
		return LobbyActivity.mapNames[currentMap];
	}

}
