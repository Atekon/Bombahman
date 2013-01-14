package pt.cagojati.bombahman.multiplayer;

import java.io.IOException;

import org.andengine.extension.multiplayer.protocol.adt.message.IMessage;
import org.andengine.extension.multiplayer.protocol.adt.message.client.IClientMessage;
import org.andengine.extension.multiplayer.protocol.adt.message.server.IServerMessage;
import org.andengine.extension.multiplayer.protocol.client.IServerMessageHandler;
import org.andengine.extension.multiplayer.protocol.client.connector.BluetoothSocketConnectionServerConnector;
import org.andengine.extension.multiplayer.protocol.client.connector.BluetoothSocketConnectionServerConnector.IBluetoothSocketConnectionServerConnectorListener;
import org.andengine.extension.multiplayer.protocol.client.connector.ServerConnector;
import org.andengine.extension.multiplayer.protocol.shared.BluetoothSocketConnection;
import org.andengine.extension.multiplayer.protocol.shared.SocketConnection;
import org.andengine.extension.multiplayer.protocol.util.MessagePool;
import org.andengine.util.debug.Debug;

import pt.cagojati.bombahman.GameActivity;
import pt.cagojati.bombahman.LobbyActivity;
import pt.cagojati.bombahman.multiplayer.messages.AddPlayerServerMessage;
import pt.cagojati.bombahman.multiplayer.messages.ConnectionCloseServerMessage;
import pt.cagojati.bombahman.multiplayer.messages.CurrentMapServerMessage;
import pt.cagojati.bombahman.multiplayer.messages.CurrentTimeServerMessage;
import pt.cagojati.bombahman.multiplayer.messages.JoinServerMessage;
import pt.cagojati.bombahman.multiplayer.messages.JoinedLobbyServerMessage;
import pt.cagojati.bombahman.multiplayer.messages.MessageFlags;
import pt.cagojati.bombahman.multiplayer.messages.PlayerReadyServerMessage;
import pt.cagojati.bombahman.multiplayer.messages.RemovePlayerServerMessage;
import pt.cagojati.bombahman.multiplayer.messages.SetPowerupsServerMessage;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class BluetoothLobbyConnector implements ILobbyConnector  {
	
	private static final String LOBBY_UUID = "6D2DF50E-06EF-C21C-7DB0-345099A5F64E";
	
	MessagePool<IMessage> mMessagePool = new MessagePool<IMessage>();
	private ServerConnector<BluetoothSocketConnection> mServerConnector;
	private LobbyActivity mLobbyActivity;
	private String mServerAddress;
	private int mPlayerCount=0;
	private BluetoothAdapter mBluetoothAdapter;
	
	public BluetoothLobbyConnector(String serverAddress) {
		MessageFlags.initMessagePool(mMessagePool);
		mServerAddress = serverAddress;
		this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	}
	
	public void setActivity(LobbyActivity game){
		mLobbyActivity = game;
	}
	
	public void initClient()
	{
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				try{
					BluetoothLobbyConnector.this.mServerConnector = new BluetoothSocketConnectionServerConnector(new BluetoothSocketConnection(BluetoothLobbyConnector.this.mBluetoothAdapter,BluetoothLobbyConnector.this.mServerAddress, BluetoothLobbyConnector.LOBBY_UUID), new ExampleServerConnectorListener());
			
					BluetoothLobbyConnector.this.mServerConnector.registerServerMessage(MessageFlags.FLAG_MESSAGE_SERVER_CONNECTION_CLOSE, ConnectionCloseServerMessage.class, new IServerMessageHandler<BluetoothSocketConnection>() {
						@Override
						public void onHandleMessage(final ServerConnector<BluetoothSocketConnection> pServerConnector, final IServerMessage pServerMessage) throws IOException {
							mLobbyActivity.finish();
						}
					});
					
					BluetoothLobbyConnector.this.mServerConnector.registerServerMessage(MessageFlags.FLAG_MESSAGE_SERVER_PLAYER_READY, PlayerReadyServerMessage.class, new IServerMessageHandler<BluetoothSocketConnection>() {
						@Override
						public void onHandleMessage(final ServerConnector<BluetoothSocketConnection> pServerConnector, final IServerMessage pServerMessage) throws IOException {
							final PlayerReadyServerMessage playerReadyServerMessage = (PlayerReadyServerMessage)pServerMessage;
							mLobbyActivity.setPlayerReady(playerReadyServerMessage.getPlayerId(), playerReadyServerMessage.getIsReady());
						}
					});
					
					BluetoothLobbyConnector.this.mServerConnector.registerServerMessage(MessageFlags.FLAG_MESSAGE_SERVER_LOBBY_JOINED, JoinedLobbyServerMessage.class, new IServerMessageHandler<BluetoothSocketConnection>() {
						@Override
						public void onHandleMessage(final ServerConnector<BluetoothSocketConnection> pServerConnector, final IServerMessage pServerMessage) throws IOException {
							final JoinedLobbyServerMessage joinedLobbyServerMessage = (JoinedLobbyServerMessage)pServerMessage;
							mLobbyActivity.setNumOfPlayer(joinedLobbyServerMessage.getNumPlayers());
							mLobbyActivity.addPlayer(joinedLobbyServerMessage.getNumPlayers());
							mLobbyActivity.setPlayerId(joinedLobbyServerMessage.getNumPlayers());
							
							mLobbyActivity.setPastReadyPlayers(joinedLobbyServerMessage.getPlayersReady());
							mLobbyActivity.setCurrentMap(joinedLobbyServerMessage.getCurrentMap());
							mLobbyActivity.setCurrentTime(joinedLobbyServerMessage.getTime());
							mLobbyActivity.setPowerups(joinedLobbyServerMessage.isPowerupsEnable());
							
							setPlayerCount(joinedLobbyServerMessage.getNumPlayers());
						}
					});
					
					BluetoothLobbyConnector.this.mServerConnector.registerServerMessage(MessageFlags.FLAG_MESSAGE_SERVER_ADD_PLAYER, AddPlayerServerMessage.class, new IServerMessageHandler<BluetoothSocketConnection>() {
						@Override
						public void onHandleMessage(final ServerConnector<BluetoothSocketConnection> pServerConnector, final IServerMessage pServerMessage) throws IOException {
							final AddPlayerServerMessage addPlayerServerMessage = (AddPlayerServerMessage)pServerMessage;
							setPlayerCount(getPlayerCount() + 1);
							mLobbyActivity.addPlayer(getPlayerCount());
						}
					});
					
					BluetoothLobbyConnector.this.mServerConnector.registerServerMessage(MessageFlags.FLAG_MESSAGE_SERVER_CURRENT_MAP, CurrentMapServerMessage.class, new IServerMessageHandler<BluetoothSocketConnection>() {
						@Override
						public void onHandleMessage(final ServerConnector<BluetoothSocketConnection> pServerConnector, final IServerMessage pServerMessage) throws IOException {
							final CurrentMapServerMessage currentMapServerMessage = (CurrentMapServerMessage)pServerMessage;
							mLobbyActivity.setCurrentMap(currentMapServerMessage.getCurrentMap());
						}
					});
					
					BluetoothLobbyConnector.this.mServerConnector.registerServerMessage(MessageFlags.FLAG_MESSAGE_SERVER_CURRENT_TIME, CurrentTimeServerMessage.class, new IServerMessageHandler<BluetoothSocketConnection>() {
						@Override
						public void onHandleMessage(final ServerConnector<BluetoothSocketConnection> pServerConnector, final IServerMessage pServerMessage) throws IOException {
							final CurrentTimeServerMessage currentTimeServerMessage = (CurrentTimeServerMessage)pServerMessage;
							mLobbyActivity.setCurrentTime(currentTimeServerMessage.getCurrentTime());
						}
					});
					
					BluetoothLobbyConnector.this.mServerConnector.registerServerMessage(MessageFlags.FLAG_MESSAGE_SERVER_SET_POWERUPS, SetPowerupsServerMessage.class, new IServerMessageHandler<BluetoothSocketConnection>() {
						@Override
						public void onHandleMessage(final ServerConnector<BluetoothSocketConnection> pServerConnector, final IServerMessage pServerMessage) throws IOException {
							final SetPowerupsServerMessage powerupsServerMessage = (SetPowerupsServerMessage)pServerMessage;
							mLobbyActivity.setPowerups(powerupsServerMessage.hasPowerups());
						}
					});
					
					BluetoothLobbyConnector.this.mServerConnector.registerServerMessage(MessageFlags.FLAG_MESSAGE_SERVER_REMOVE_PLAYER, RemovePlayerServerMessage.class, new IServerMessageHandler<BluetoothSocketConnection>() {
						@Override
						public void onHandleMessage(final ServerConnector<BluetoothSocketConnection> pServerConnector, final IServerMessage pServerMessage) throws IOException {
							final RemovePlayerServerMessage removePlayerServerMessage = (RemovePlayerServerMessage)pServerMessage;
							mLobbyActivity.removePlayer(removePlayerServerMessage.getPlayerId());
							setPlayerCount(getPlayerCount() - 1);
						}
					});
					
					BluetoothLobbyConnector.this.mServerConnector.registerServerMessage(MessageFlags.FLAG_MESSAGE_SERVER_JOIN, JoinServerMessage.class, new IServerMessageHandler<BluetoothSocketConnection>() {
						@Override
						public void onHandleMessage(final ServerConnector<BluetoothSocketConnection> pServerConnector, final IServerMessage pServerMessage) throws IOException {
							Intent intent = new Intent(mLobbyActivity, GameActivity.class);
							Bundle bundle = new Bundle();
				            bundle.putBoolean("isWiFi", false);
				            bundle.putString("ip", mServerAddress);
				            bundle.putInt("time", mLobbyActivity.getCurrentTime());
				    		bundle.putBoolean("powerupsEnabled", mLobbyActivity.isCurrentPowerups());
				    		bundle.putString("map", mLobbyActivity.getCurrentMapName());
				            intent.putExtras(bundle);
							mLobbyActivity.startActivity(intent);
						}
					});
			
					BluetoothLobbyConnector.this.mServerConnector.getConnection().start();
				}catch (final Throwable t) {
					Log.d("oteste", t.getMessage());
					Debug.e(t);
					mLobbyActivity.finish();
				}
				
			}
		}).start();
		
	}
	
	private class ExampleServerConnectorListener implements IBluetoothSocketConnectionServerConnectorListener {
		@Override
		public void onStarted(final ServerConnector<BluetoothSocketConnection> pConnector) {
		}

		@Override
		public void onTerminated(final ServerConnector<BluetoothSocketConnection> pConnector) {
//			MultiplayerExample.this.toast("CLIENT: Disconnected from Server...");
//			MultiplayerExample.this.finish();
		}
	}

	@Override
	public MessagePool<IMessage> getMessagePool() {
		return mMessagePool;
	}

	@Override
	public void sendClientMessage(IClientMessage msg) {
		try {
			this.mServerConnector.sendClientMessage(msg);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void terminate() {
		if(this.mServerConnector!=null){
			this.mServerConnector.terminate();
		}
	}

	public int getPlayerCount() {
		return mPlayerCount;
	}

	public void setPlayerCount(int mPlayerCount) {
		this.mPlayerCount = mPlayerCount;
	}
}
