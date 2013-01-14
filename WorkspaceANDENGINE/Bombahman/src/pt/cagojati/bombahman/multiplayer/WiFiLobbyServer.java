package pt.cagojati.bombahman.multiplayer;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Hashtable;

import org.andengine.engine.handler.timer.ITimerCallback;
import org.andengine.engine.handler.timer.TimerHandler;
import org.andengine.extension.multiplayer.protocol.adt.message.IMessage;
import org.andengine.extension.multiplayer.protocol.adt.message.client.IClientMessage;
import org.andengine.extension.multiplayer.protocol.adt.message.server.IServerMessage;
import org.andengine.extension.multiplayer.protocol.client.IServerMessageHandler;
import org.andengine.extension.multiplayer.protocol.client.connector.ServerConnector;
import org.andengine.extension.multiplayer.protocol.server.IClientMessageHandler;
import org.andengine.extension.multiplayer.protocol.server.Server;
import org.andengine.extension.multiplayer.protocol.server.SocketServer;
import org.andengine.extension.multiplayer.protocol.server.SocketServer.ISocketServerListener;
import org.andengine.extension.multiplayer.protocol.server.connector.ClientConnector;
import org.andengine.extension.multiplayer.protocol.server.connector.SocketConnectionClientConnector;
import org.andengine.extension.multiplayer.protocol.server.connector.SocketConnectionClientConnector.ISocketConnectionClientConnectorListener;
import org.andengine.extension.multiplayer.protocol.shared.SocketConnection;
import org.andengine.extension.multiplayer.protocol.util.MessagePool;

import pt.cagojati.bombahman.GameActivity;
import pt.cagojati.bombahman.LobbyActivity;
import pt.cagojati.bombahman.multiplayer.messages.AddBombClientMessage;
import pt.cagojati.bombahman.multiplayer.messages.AddBombServerMessage;
import pt.cagojati.bombahman.multiplayer.messages.AddPlayerServerMessage;
import pt.cagojati.bombahman.multiplayer.messages.ConnectionCloseServerMessage;
import pt.cagojati.bombahman.multiplayer.messages.ExplodeBombServerMessage;
import pt.cagojati.bombahman.multiplayer.messages.JoinedLobbyServerMessage;
import pt.cagojati.bombahman.multiplayer.messages.JoinedServerServerMessage;
import pt.cagojati.bombahman.multiplayer.messages.MessageFlags;
import pt.cagojati.bombahman.multiplayer.messages.MovePlayerClientMessage;
import pt.cagojati.bombahman.multiplayer.messages.MovePlayerServerMessage;
import pt.cagojati.bombahman.multiplayer.messages.PlayerReadyClientMessage;
import pt.cagojati.bombahman.multiplayer.messages.PlayerReadyServerMessage;
import pt.cagojati.bombahman.multiplayer.messages.RemovePlayerServerMessage;
import android.util.Log;

public class WiFiLobbyServer implements ILobbyServer {
	
	private static final int SERVER_PORT = 4444;
	
	MessagePool<IMessage> mMessagePool = new MessagePool<IMessage>();
	private SocketServer<SocketConnectionClientConnector> mSocketServer;
	private static WiFiLobbyServer instance = null;
	private int mPlayerCount = 0;
	private boolean[] mPlayersReady = {false, false, false, false};
	private ArrayList<InetAddress> clientList;

	private WiFiLobbyServer() {
		MessageFlags.initMessagePool(mMessagePool);
		clientList = new ArrayList<InetAddress>();
	}
	
	public static synchronized WiFiLobbyServer getSingletonObject() {
		if (instance == null) {
			instance = new WiFiLobbyServer();
		}
		return instance;
	}
	
	public static boolean isInitialized(){
		if(instance==null)
			return false;
		return true;
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public Server getServerSocket() {
		return mSocketServer;
	}

	@Override
	public void initServer() {
		this.mSocketServer = new SocketServer<SocketConnectionClientConnector>(SERVER_PORT, new ExampleClientConnectorListener(), new ExampleServerStateListener()) {
			@Override
			protected SocketConnectionClientConnector newClientConnector(final SocketConnection pSocketConnection) throws IOException {
				final SocketConnectionClientConnector clientConnector = new SocketConnectionClientConnector(pSocketConnection);
				
				clientConnector.registerClientMessage(MessageFlags.FLAG_MESSAGE_CLIENT_PLAYER_READY, PlayerReadyClientMessage.class, new IClientMessageHandler<SocketConnection>() {

					@Override
					public void onHandleMessage(ClientConnector<SocketConnection> pClientConnector,IClientMessage pClientMessage) throws IOException {
						
						final PlayerReadyClientMessage playerReadyClientMessage = (PlayerReadyClientMessage) pClientMessage;
						
						final PlayerReadyServerMessage playerReadyServerMessage = (PlayerReadyServerMessage) WiFiLobbyServer.this.mMessagePool.obtainMessage(MessageFlags.FLAG_MESSAGE_SERVER_PLAYER_READY);
						playerReadyServerMessage.set(playerReadyClientMessage.getPlayerId(), playerReadyClientMessage.getIsReady());
						
						mPlayersReady[playerReadyClientMessage.getPlayerId()] = playerReadyClientMessage.getIsReady();
						
						WiFiLobbyServer.this.mSocketServer.sendBroadcastServerMessage(playerReadyServerMessage);
						
						WiFiLobbyServer.this.mMessagePool.recycleMessage(playerReadyServerMessage);
					}
				});
				

				return clientConnector;
			}
		};
		this.mSocketServer.start();
	}
	
	private class ExampleClientConnectorListener implements ISocketConnectionClientConnectorListener {
		@Override
		public void onStarted(final ClientConnector<SocketConnection> pConnector) {
			clientList.add(pConnector.getConnection().getSocket().getInetAddress());
			try {
				AddPlayerServerMessage addPlayerServerMessage = (AddPlayerServerMessage) WiFiLobbyServer.this.mMessagePool.obtainMessage(MessageFlags.FLAG_MESSAGE_SERVER_ADD_PLAYER);
				WiFiLobbyServer.this.mSocketServer.sendBroadcastServerMessage(addPlayerServerMessage);
				WiFiLobbyServer.this.mMessagePool.recycleMessage(addPlayerServerMessage);
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				JoinedLobbyServerMessage joinedLobbyServerMessage = (JoinedLobbyServerMessage) WiFiLobbyServer.this.mMessagePool.obtainMessage(MessageFlags.FLAG_MESSAGE_SERVER_LOBBY_JOINED);
				joinedLobbyServerMessage.setIsPlayer(true);
				joinedLobbyServerMessage.setNumPlayers(mPlayerCount);
				joinedLobbyServerMessage.setPlayersReady(mPlayersReady);
				joinedLobbyServerMessage.setTime(LobbyActivity.getCurrentTime());
				joinedLobbyServerMessage.setCurrentMap(LobbyActivity.getCurrentMap());
				joinedLobbyServerMessage.setPowerupsEnable(LobbyActivity.isCurrentPowerups());
				pConnector.sendServerMessage(joinedLobbyServerMessage);
				
				WiFiLobbyServer.this.mMessagePool.recycleMessage(joinedLobbyServerMessage);
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			mPlayerCount++;
			
		}

		@Override
		public void onTerminated(final ClientConnector<SocketConnection> pConnector) {
			mPlayerCount--;
			int clientExited = clientList.indexOf(pConnector.getConnection().getSocket().getInetAddress());

			try {
				clientList.remove(pConnector.getConnection().getSocket().getInetAddress());
				RemovePlayerServerMessage removePlayer_msg = (RemovePlayerServerMessage) WiFiLobbyServer.this.mMessagePool.obtainMessage(MessageFlags.FLAG_MESSAGE_SERVER_REMOVE_PLAYER);
				removePlayer_msg.setPlayerId(clientExited);
				WiFiLobbyServer.this.mSocketServer.sendBroadcastServerMessage(removePlayer_msg);
				WiFiLobbyServer.this.mMessagePool.recycleMessage(removePlayer_msg);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
		}
	}
	
	private class ExampleServerStateListener implements ISocketServerListener<SocketConnectionClientConnector> {
		@Override
		public void onStarted(final SocketServer<SocketConnectionClientConnector> pSocketServer) {
			Log.d("oteste","servidor comecou");
		}

		@Override
		public void onTerminated(final SocketServer<SocketConnectionClientConnector> pSocketServer) {
			Log.d("oteste","servidor terminou");
		}

		@Override
		public void onException(final SocketServer<SocketConnectionClientConnector> pSocketServer, final Throwable pThrowable) {
			Log.d("oteste","servidor crashou" + pThrowable.getMessage());
		}

	}

	@Override
	public void terminate() {
		this.mSocketServer.terminate();
		instance = null;
	}

	@Override
	public void sendBroadcastServerMessage(IServerMessage msg) throws IOException {
		this.mSocketServer.sendBroadcastServerMessage(msg);
	}

	@Override
	public MessagePool<IMessage> getMessagePool() {
		return this.mMessagePool;
	}
	
	public InetAddress[] getClientList()
	{
		InetAddress[] arr = new InetAddress[clientList.size()];
		for(int i = 0; i<clientList.size();i++)
		{
			arr[i] = clientList.get(i);
		}
		return arr;
	}
	
}
