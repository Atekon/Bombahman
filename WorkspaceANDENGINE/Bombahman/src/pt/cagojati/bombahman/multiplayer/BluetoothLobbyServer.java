package pt.cagojati.bombahman.multiplayer;

import java.io.IOException;
import java.util.ArrayList;

import org.andengine.extension.multiplayer.protocol.adt.message.IMessage;
import org.andengine.extension.multiplayer.protocol.adt.message.client.IClientMessage;
import org.andengine.extension.multiplayer.protocol.adt.message.server.IServerMessage;
import org.andengine.extension.multiplayer.protocol.exception.BluetoothException;
import org.andengine.extension.multiplayer.protocol.server.BluetoothSocketServer;
import org.andengine.extension.multiplayer.protocol.server.BluetoothSocketServer.IBluetoothSocketServerListener;
import org.andengine.extension.multiplayer.protocol.server.IClientMessageHandler;
import org.andengine.extension.multiplayer.protocol.server.Server;
import org.andengine.extension.multiplayer.protocol.server.SocketServer;
import org.andengine.extension.multiplayer.protocol.server.connector.BluetoothSocketConnectionClientConnector;
import org.andengine.extension.multiplayer.protocol.server.connector.BluetoothSocketConnectionClientConnector.IBluetoothSocketConnectionClientConnectorListener;
import org.andengine.extension.multiplayer.protocol.server.connector.ClientConnector;
import org.andengine.extension.multiplayer.protocol.server.connector.SocketConnectionClientConnector;
import org.andengine.extension.multiplayer.protocol.shared.BluetoothSocketConnection;
import org.andengine.extension.multiplayer.protocol.shared.SocketConnection;
import org.andengine.extension.multiplayer.protocol.util.MessagePool;

import pt.cagojati.bombahman.LobbyActivity;
import pt.cagojati.bombahman.multiplayer.messages.AddPlayerServerMessage;
import pt.cagojati.bombahman.multiplayer.messages.JoinedLobbyServerMessage;
import pt.cagojati.bombahman.multiplayer.messages.MessageFlags;
import pt.cagojati.bombahman.multiplayer.messages.PlayerReadyClientMessage;
import pt.cagojati.bombahman.multiplayer.messages.PlayerReadyServerMessage;
import pt.cagojati.bombahman.multiplayer.messages.RemovePlayerServerMessage;
import android.util.Log;

public class BluetoothLobbyServer implements ILobbyServer {
		
	private static final String LOBBY_UUID = "6D2DF50E-06EF-C21C-7DB0-345099A5F64E";

	MessagePool<IMessage> mMessagePool = new MessagePool<IMessage>();
	private BluetoothSocketServer<BluetoothSocketConnectionClientConnector> mSocketServer;
	private static BluetoothLobbyServer instance = null;
	private int mPlayerCount = 0;
	private boolean[] mPlayersReady = {false, false, false, false};
	private ArrayList<String> clientList;

	private BluetoothLobbyServer() {
		MessageFlags.initMessagePool(mMessagePool);
		clientList = new ArrayList<String>();
	}
	
	public static synchronized BluetoothLobbyServer getSingletonObject() {
		if (instance == null) {
			instance = new BluetoothLobbyServer();
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
		try {
			this.mSocketServer = new BluetoothSocketServer<BluetoothSocketConnectionClientConnector>(LOBBY_UUID, new ExampleClientConnectorListener(), new ExampleServerStateListener()) {
				@Override
				protected BluetoothSocketConnectionClientConnector newClientConnector(final BluetoothSocketConnection pSocketConnection) throws IOException {
					BluetoothSocketConnectionClientConnector clientConnector;
					try {
						clientConnector = new BluetoothSocketConnectionClientConnector(pSocketConnection);
					
						clientConnector.registerClientMessage(MessageFlags.FLAG_MESSAGE_CLIENT_PLAYER_READY, PlayerReadyClientMessage.class, new IClientMessageHandler<BluetoothSocketConnection>() {
	
							@Override
							public void onHandleMessage(ClientConnector<BluetoothSocketConnection> pClientConnector,IClientMessage pClientMessage) throws IOException {
								
								final PlayerReadyClientMessage playerReadyClientMessage = (PlayerReadyClientMessage) pClientMessage;
								
								final PlayerReadyServerMessage playerReadyServerMessage = (PlayerReadyServerMessage) BluetoothLobbyServer.this.mMessagePool.obtainMessage(MessageFlags.FLAG_MESSAGE_SERVER_PLAYER_READY);
								playerReadyServerMessage.set(playerReadyClientMessage.getPlayerId(), playerReadyClientMessage.getIsReady());
								
								mPlayersReady[playerReadyClientMessage.getPlayerId()] = playerReadyClientMessage.getIsReady();
								
								BluetoothLobbyServer.this.mSocketServer.sendBroadcastServerMessage(playerReadyServerMessage);
								
								BluetoothLobbyServer.this.mMessagePool.recycleMessage(playerReadyServerMessage);
							}
						});
	
						return clientConnector;
					} catch (BluetoothException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					return null;
				}
			};
		} catch (BluetoothException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.mSocketServer.start();
	}
	
	private class ExampleClientConnectorListener implements IBluetoothSocketConnectionClientConnectorListener {
		@Override
		public void onStarted(final ClientConnector<BluetoothSocketConnection> pConnector) {
			clientList.add(pConnector.getConnection().getBluetoothSocket().getRemoteDevice().getAddress());
			try {
				AddPlayerServerMessage addPlayerServerMessage = (AddPlayerServerMessage) BluetoothLobbyServer.this.mMessagePool.obtainMessage(MessageFlags.FLAG_MESSAGE_SERVER_ADD_PLAYER);
				BluetoothLobbyServer.this.mSocketServer.sendBroadcastServerMessage(addPlayerServerMessage);
				BluetoothLobbyServer.this.mMessagePool.recycleMessage(addPlayerServerMessage);
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				JoinedLobbyServerMessage joinedLobbyServerMessage = (JoinedLobbyServerMessage) BluetoothLobbyServer.this.mMessagePool.obtainMessage(MessageFlags.FLAG_MESSAGE_SERVER_LOBBY_JOINED);
				joinedLobbyServerMessage.setIsPlayer(true);
				joinedLobbyServerMessage.setNumPlayers(mPlayerCount);
				joinedLobbyServerMessage.setPlayersReady(mPlayersReady);
				joinedLobbyServerMessage.setTime(LobbyActivity.getCurrentTime());
				joinedLobbyServerMessage.setCurrentMap(LobbyActivity.getCurrentMap());
				joinedLobbyServerMessage.setPowerupsEnable(LobbyActivity.isCurrentPowerups());
				pConnector.sendServerMessage(joinedLobbyServerMessage);
				
				BluetoothLobbyServer.this.mMessagePool.recycleMessage(joinedLobbyServerMessage);
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			mPlayerCount++;
			
		}

		@Override
		public void onTerminated(final ClientConnector<BluetoothSocketConnection> pConnector) {
			mPlayerCount--;
			int clientExited = clientList.indexOf(pConnector.getConnection().getBluetoothSocket().getRemoteDevice().getAddress());

			try {
				clientList.remove(pConnector.getConnection().getBluetoothSocket().getRemoteDevice().getAddress());
				RemovePlayerServerMessage removePlayer_msg = (RemovePlayerServerMessage) BluetoothLobbyServer.this.mMessagePool.obtainMessage(MessageFlags.FLAG_MESSAGE_SERVER_REMOVE_PLAYER);
				removePlayer_msg.setPlayerId(clientExited);
				BluetoothLobbyServer.this.mSocketServer.sendBroadcastServerMessage(removePlayer_msg);
				BluetoothLobbyServer.this.mMessagePool.recycleMessage(removePlayer_msg);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
		}
	}
	
	private class ExampleServerStateListener implements IBluetoothSocketServerListener<BluetoothSocketConnectionClientConnector> {
		@Override
		public void onStarted(final BluetoothSocketServer<BluetoothSocketConnectionClientConnector> pSocketServer) {
			Log.d("oteste","servidor comecou");
		}

		@Override
		public void onTerminated(final BluetoothSocketServer<BluetoothSocketConnectionClientConnector> pSocketServer) {
			Log.d("oteste","servidor terminou");
		}

		@Override
		public void onException(final BluetoothSocketServer<BluetoothSocketConnectionClientConnector> pSocketServer, final Throwable pThrowable) {
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
	
	public String[] getClientList()
	{
		String[] arr = new String[clientList.size()];
		for(int i = 0; i<clientList.size();i++)
		{
			arr[i] = clientList.get(i);
		}
		return arr;
	}
	
}
