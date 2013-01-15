package pt.cagojati.bombahman.multiplayer;

import java.io.IOException;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

import org.andengine.extension.multiplayer.protocol.adt.message.IMessage;
import org.andengine.extension.multiplayer.protocol.adt.message.client.IClientMessage;
import org.andengine.extension.multiplayer.protocol.adt.message.server.IServerMessage;
import org.andengine.extension.multiplayer.protocol.client.IServerMessageHandler;
import org.andengine.extension.multiplayer.protocol.client.connector.ServerConnector;
import org.andengine.extension.multiplayer.protocol.client.connector.SocketConnectionServerConnector;
import org.andengine.extension.multiplayer.protocol.client.connector.SocketConnectionServerConnector.ISocketConnectionServerConnectorListener;
import org.andengine.extension.multiplayer.protocol.shared.SocketConnection;
import org.andengine.extension.multiplayer.protocol.util.MessagePool;
import org.andengine.util.debug.Debug;

import pt.cagojati.bombahman.GameActivity;
import pt.cagojati.bombahman.LobbyActivity;
import pt.cagojati.bombahman.MainActivity;
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
import pt.cagojati.bombahman.multiplayer.messages.StartReadyTimerServerMessage;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

public class WiFiLobbyConnector implements ILobbyConnector  {
	private static final int SERVER_PORT = 4444;
	
	MessagePool<IMessage> mMessagePool = new MessagePool<IMessage>();
	private ServerConnector<SocketConnection> mServerConnector;
	private String mServerIP;
	private LobbyActivity mLobbyActivity;
	private int mPlayerCount=0;
	
	public WiFiLobbyConnector(String ip) {
		MessageFlags.initMessagePool(mMessagePool);
		mServerIP = ip;
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
					WiFiLobbyConnector.this.mServerConnector = new SocketConnectionServerConnector(new SocketConnection(new Socket(WiFiLobbyConnector.this.mServerIP, SERVER_PORT)), new ExampleServerConnectorListener());
			
					WiFiLobbyConnector.this.mServerConnector.registerServerMessage(MessageFlags.FLAG_MESSAGE_SERVER_CONNECTION_CLOSE, ConnectionCloseServerMessage.class, new IServerMessageHandler<SocketConnection>() {
						@Override
						public void onHandleMessage(final ServerConnector<SocketConnection> pServerConnector, final IServerMessage pServerMessage) throws IOException {
							mLobbyActivity.finish();
						}
					});
					
					WiFiLobbyConnector.this.mServerConnector.registerServerMessage(MessageFlags.FLAG_MESSAGE_SERVER_PLAYER_READY, PlayerReadyServerMessage.class, new IServerMessageHandler<SocketConnection>() {
						@Override
						public void onHandleMessage(final ServerConnector<SocketConnection> pServerConnector, final IServerMessage pServerMessage) throws IOException {
							final PlayerReadyServerMessage playerReadyServerMessage = (PlayerReadyServerMessage)pServerMessage;
							mLobbyActivity.setPlayerReady(playerReadyServerMessage.getPlayerId(), playerReadyServerMessage.getIsReady());
						}
					});
					
					WiFiLobbyConnector.this.mServerConnector.registerServerMessage(MessageFlags.FLAG_MESSAGE_SERVER_LOBBY_JOINED, JoinedLobbyServerMessage.class, new IServerMessageHandler<SocketConnection>() {
						@Override
						public void onHandleMessage(final ServerConnector<SocketConnection> pServerConnector, final IServerMessage pServerMessage) throws IOException {
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
					
					WiFiLobbyConnector.this.mServerConnector.registerServerMessage(MessageFlags.FLAG_MESSAGE_SERVER_ADD_PLAYER, AddPlayerServerMessage.class, new IServerMessageHandler<SocketConnection>() {
						@Override
						public void onHandleMessage(final ServerConnector<SocketConnection> pServerConnector, final IServerMessage pServerMessage) throws IOException {
							final AddPlayerServerMessage addPlayerServerMessage = (AddPlayerServerMessage)pServerMessage;
							setPlayerCount(getPlayerCount() + 1);
							mLobbyActivity.addPlayer(getPlayerCount());
						}
					});
					
					WiFiLobbyConnector.this.mServerConnector.registerServerMessage(MessageFlags.FLAG_MESSAGE_SERVER_CURRENT_MAP, CurrentMapServerMessage.class, new IServerMessageHandler<SocketConnection>() {
						@Override
						public void onHandleMessage(final ServerConnector<SocketConnection> pServerConnector, final IServerMessage pServerMessage) throws IOException {
							final CurrentMapServerMessage currentMapServerMessage = (CurrentMapServerMessage)pServerMessage;
							mLobbyActivity.setCurrentMap(currentMapServerMessage.getCurrentMap());
						}
					});
					
					WiFiLobbyConnector.this.mServerConnector.registerServerMessage(MessageFlags.FLAG_MESSAGE_SERVER_CURRENT_TIME, CurrentTimeServerMessage.class, new IServerMessageHandler<SocketConnection>() {
						@Override
						public void onHandleMessage(final ServerConnector<SocketConnection> pServerConnector, final IServerMessage pServerMessage) throws IOException {
							final CurrentTimeServerMessage currentTimeServerMessage = (CurrentTimeServerMessage)pServerMessage;
							mLobbyActivity.setCurrentTime(currentTimeServerMessage.getCurrentTime());
						}
					});
					
					WiFiLobbyConnector.this.mServerConnector.registerServerMessage(MessageFlags.FLAG_MESSAGE_SERVER_SET_POWERUPS, SetPowerupsServerMessage.class, new IServerMessageHandler<SocketConnection>() {
						@Override
						public void onHandleMessage(final ServerConnector<SocketConnection> pServerConnector, final IServerMessage pServerMessage) throws IOException {
							final SetPowerupsServerMessage powerupsServerMessage = (SetPowerupsServerMessage)pServerMessage;
							mLobbyActivity.setPowerups(powerupsServerMessage.hasPowerups());
						}
					});
					
					WiFiLobbyConnector.this.mServerConnector.registerServerMessage(MessageFlags.FLAG_MESSAGE_SERVER_REMOVE_PLAYER, RemovePlayerServerMessage.class, new IServerMessageHandler<SocketConnection>() {
						@Override
						public void onHandleMessage(final ServerConnector<SocketConnection> pServerConnector, final IServerMessage pServerMessage) throws IOException {
							final RemovePlayerServerMessage removePlayerServerMessage = (RemovePlayerServerMessage)pServerMessage;
							mLobbyActivity.removePlayer(removePlayerServerMessage.getPlayerId());
							setPlayerCount(getPlayerCount() - 1);
						}
					});
					
					WiFiLobbyConnector.this.mServerConnector.registerServerMessage(MessageFlags.FLAG_MESSAGE_SERVER_JOIN, JoinServerMessage.class, new IServerMessageHandler<SocketConnection>() {
						@Override
						public void onHandleMessage(final ServerConnector<SocketConnection> pServerConnector, final IServerMessage pServerMessage) throws IOException {
							Intent intent = new Intent(mLobbyActivity, GameActivity.class);
							Bundle bundle = new Bundle();
				            bundle.putBoolean("isWiFi", true);
				            bundle.putString("ip", mServerIP);
				            bundle.putInt("time", LobbyActivity.getCurrentTime());
				    		bundle.putBoolean("powerupsEnabled", LobbyActivity.isCurrentPowerups());
				    		bundle.putString("map", LobbyActivity.getCurrentMapName());
				            intent.putExtras(bundle);
							mLobbyActivity.startActivity(intent);
							mLobbyActivity.finish();
						}
					});
					
					WiFiLobbyConnector.this.mServerConnector.registerServerMessage(MessageFlags.FLAG_MESSAGE_SERVER_START_READY_TIMER, StartReadyTimerServerMessage.class, new IServerMessageHandler<SocketConnection>() {
						
						@Override
						public void onHandleMessage(ServerConnector<SocketConnection> pServerConnector,IServerMessage pServerMessage) throws IOException {
							mLobbyActivity.startTimer();
						}
					});
			
					WiFiLobbyConnector.this.mServerConnector.getConnection().start();
				}catch (final Throwable t) {
					Log.d("oteste", t.getMessage());
					Debug.e(t);
					mLobbyActivity.finish();
				}
				
			}
		}).start();
		
	}
	
	private class ExampleServerConnectorListener implements ISocketConnectionServerConnectorListener {
		@Override
		public void onStarted(final ServerConnector<SocketConnection> pConnector) {
		}

		@Override
		public void onTerminated(final ServerConnector<SocketConnection> pConnector) {
//			MultiplayerExample.this.toast("CLIENT: Disconnected from Server...");
			mLobbyActivity.finish();
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
