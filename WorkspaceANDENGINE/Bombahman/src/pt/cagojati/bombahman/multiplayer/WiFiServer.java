package pt.cagojati.bombahman.multiplayer;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Hashtable;

import org.andengine.engine.handler.timer.ITimerCallback;
import org.andengine.engine.handler.timer.TimerHandler;
import org.andengine.extension.multiplayer.protocol.adt.message.IMessage;
import org.andengine.extension.multiplayer.protocol.adt.message.client.IClientMessage;
import org.andengine.extension.multiplayer.protocol.adt.message.server.IServerMessage;
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
import pt.cagojati.bombahman.Player;
import pt.cagojati.bombahman.multiplayer.messages.AddBombClientMessage;
import pt.cagojati.bombahman.multiplayer.messages.AddBombServerMessage;
import pt.cagojati.bombahman.multiplayer.messages.AddPlayerServerMessage;
import pt.cagojati.bombahman.multiplayer.messages.AddPowerupServerMessage;
import pt.cagojati.bombahman.multiplayer.messages.AllReadyServerMessage;
import pt.cagojati.bombahman.multiplayer.messages.ExplodeBombServerMessage;
import pt.cagojati.bombahman.multiplayer.messages.JoinedServerServerMessage;
import pt.cagojati.bombahman.multiplayer.messages.KillPlayerServerMessage;
import pt.cagojati.bombahman.multiplayer.messages.MessageFlags;
import pt.cagojati.bombahman.multiplayer.messages.MovePlayerClientMessage;
import pt.cagojati.bombahman.multiplayer.messages.MovePlayerServerMessage;
import android.util.Log;

public class WiFiServer implements IMultiplayerServer {
	
	private static final int SERVER_PORT = 4445;
	
	MessagePool<IMessage> mMessagePool = new MessagePool<IMessage>();
	private SocketServer<SocketConnectionClientConnector> mSocketServer;
	private static WiFiServer instance = null;
	private int mPlayerCount = 0;
	private Hashtable<InetAddress, Integer> clientList;
	private InetAddress[] hostsAdresses;
	private int mMaxPlayers;
	private boolean[] clientHasJoined;

	private WiFiServer() {
		MessageFlags.initMessagePool(mMessagePool);
		clientList = new Hashtable<InetAddress, Integer>();
	}
	
	public static synchronized WiFiServer getSingletonObject() {
		if (instance == null) {
			instance = new WiFiServer();
		}
		return instance;
	}
	
	public static boolean isInitialized(){
		if(instance==null)
			return false;
		return true;
	}
	
	public MessagePool<IMessage> getMessagePool()
	{
		return mMessagePool;
	}
	
	public void setHostsAddreses(InetAddress[] hostsAddresses)
	{
		this.hostsAdresses = hostsAddresses;
		clientHasJoined = new boolean[hostsAddresses.length];
		for(int i =0; i< hostsAddresses.length;i++)
		{
			clientHasJoined[i] = false;
		}
	}
	
	public void setMaxPlayers(int maxPlayers){
		this.mMaxPlayers = maxPlayers;
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
				
				clientConnector.registerClientMessage(MessageFlags.FLAG_MESSAGE_CLIENT_ADD_BOMB, AddBombClientMessage.class, new IClientMessageHandler<SocketConnection>() {
					@Override
					public void onHandleMessage(final ClientConnector<SocketConnection> pClientConnector, final IClientMessage pClientMessage) throws IOException {
						final AddBombClientMessage addBombClientMessage = (AddBombClientMessage) pClientMessage;
						
						final AddBombServerMessage addBombServerMessage = (AddBombServerMessage) WiFiServer.this.mMessagePool.obtainMessage(MessageFlags.FLAG_MESSAGE_SERVER_ADD_BOMB);
						addBombServerMessage.set(addBombClientMessage.getX(), addBombClientMessage.getY(),addBombClientMessage.getPlayerId(), addBombClientMessage.getBombId());

						ArrayList<SocketConnectionClientConnector> clientBlackList = new ArrayList<SocketConnectionClientConnector>();
						clientBlackList.add(clientConnector);
						WiFiServer.this.mSocketServer.sendAlmostBroadcastServerMessage(addBombServerMessage, clientBlackList);
						//WiFiServer.this.mSocketServer.sendBroadcastServerMessage(addBombServerMessage);

						WiFiServer.this.mMessagePool.recycleMessage(addBombServerMessage);
					}
				});
				
				clientConnector.registerClientMessage(MessageFlags.FLAG_MESSAGE_CLIENT_MOVE_PLAYER, MovePlayerClientMessage.class, new IClientMessageHandler<SocketConnection>() {
					@Override
					public void onHandleMessage(final ClientConnector<SocketConnection> pClientConnector, final IClientMessage pClientMessage) throws IOException {
						final MovePlayerClientMessage movePlayerClientMessage = (MovePlayerClientMessage) pClientMessage;
						
						if(DeadReckoningServer.validateMessage(movePlayerClientMessage.getPlayerId())){
							if(movePlayerClientMessage.getPlayerId()!=0)
							Log.d("oteste", "no lag on player" + movePlayerClientMessage.getPlayerId());

							final MovePlayerServerMessage movePlayerServerMessage = (MovePlayerServerMessage) WiFiServer.this.mMessagePool.obtainMessage(MessageFlags.FLAG_MESSAGE_SERVER_MOVE_PLAYER);
							movePlayerServerMessage.set(movePlayerClientMessage.getX(), movePlayerClientMessage.getY(),movePlayerClientMessage.getVX(),movePlayerClientMessage.getVY(),movePlayerClientMessage.getPlayerId());
	
							ArrayList<SocketConnectionClientConnector> clientBlackList = new ArrayList<SocketConnectionClientConnector>();
							clientBlackList.add(clientConnector);
							WiFiServer.this.mSocketServer.sendAlmostBroadcastServerMessage(movePlayerServerMessage, clientBlackList);
	
							WiFiServer.this.mMessagePool.recycleMessage(movePlayerServerMessage);
						}else{
							//player laggedout
//							Log.d("oteste", "lag on player" + movePlayerClientMessage.getPlayerId());
//							final MovePlayerServerMessage movePlayerServerMessage = (MovePlayerServerMessage) WiFiServer.this.mMessagePool.obtainMessage(MessageFlags.FLAG_MESSAGE_SERVER_MOVE_PLAYER);
//							movePlayerServerMessage.set(movePlayerClientMessage.getX(), movePlayerClientMessage.getY(),movePlayerClientMessage.getVX(),movePlayerClientMessage.getVY(),movePlayerClientMessage.getPlayerId());
//							
//							pClientConnector.sendServerMessage(movePlayerServerMessage);
//							
//							WiFiServer.this.mMessagePool.recycleMessage(movePlayerServerMessage);
						}
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
			if(clientList.containsKey(pConnector.getConnection().getSocket().getInetAddress()))
			{
				return;
			}
			InetAddress address = pConnector.getConnection().getSocket().getInetAddress();
			int pos = -1;
			for(int i =0; i<mMaxPlayers;i++)
			{
				if(address == hostsAdresses[i])
				{
					pos = i;
					clientHasJoined[pos] = true;
				}
			}
			clientList.put(pConnector.getConnection().getSocket().getInetAddress(), pos);
			//first player should be server right?
			if(pos==0){
				DeadReckoningServer.startTimer();
			}
			try {
				AddPlayerServerMessage addPlayerServerMessage = (AddPlayerServerMessage) WiFiServer.this.mMessagePool.obtainMessage(MessageFlags.FLAG_MESSAGE_SERVER_ADD_PLAYER);
				addPlayerServerMessage.setPlayerId(pos);
				WiFiServer.this.mSocketServer.sendBroadcastServerMessage(addPlayerServerMessage);
				WiFiServer.this.mMessagePool.recycleMessage(addPlayerServerMessage);
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				JoinedServerServerMessage joinedServerServerMessage = (JoinedServerServerMessage) WiFiServer.this.mMessagePool.obtainMessage(MessageFlags.FLAG_MESSAGE_SERVER_JOINED_SERVER);
				joinedServerServerMessage.setNumPlayers(mPlayerCount);
				joinedServerServerMessage.setPlayerId(pos);
				int[] players = new int[mPlayerCount];
				int j = 0;
				for(int i = 0; i<WiFiServer.this.mMaxPlayers; i++){
					if(WiFiServer.this.clientHasJoined[i] == true)
					{
						if(pos!=i)
						{
							players[j] = i;
							j++;
						}
					}
				}
				joinedServerServerMessage.setPlayersToAdd(players);
				pConnector.sendServerMessage(joinedServerServerMessage);
				WiFiServer.this.mMessagePool.recycleMessage(joinedServerServerMessage);
				
				if(GameActivity.isPowerupEnabled()){
					AddPowerupServerMessage message = (AddPowerupServerMessage) mMessagePool.obtainMessage(MessageFlags.FLAG_MESSAGE_SERVER_ADD_POWERUPS);
					message.set(GameActivity.getPowerUpList());
					pConnector.sendServerMessage(message);
					mMessagePool.recycleMessage(message);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			mPlayerCount++;
			if(mPlayerCount == WiFiServer.this.mMaxPlayers)
			{
				AllReadyServerMessage message = (AllReadyServerMessage) mMessagePool.obtainMessage(MessageFlags.FLAG_MESSAGE_SERVER_ALLREADY);
				try {
					WiFiServer.this.mSocketServer.sendBroadcastServerMessage(message);
				} catch (IOException e) {
					e.printStackTrace();
				}
				mMessagePool.recycleMessage(message);

			}
		}

		@Override
		public void onTerminated(final ClientConnector<SocketConnection> pConnector) {
			KillPlayerServerMessage killPlayerServerMessage = (KillPlayerServerMessage) WiFiServer.this.mMessagePool.obtainMessage(MessageFlags.FLAG_MESSAGE_SERVER_KILL_PLAYER);
			killPlayerServerMessage.setPlayerId(clientList.get(pConnector.getConnection().getSocket().getInetAddress()));
			try {
				WiFiServer.this.mSocketServer.sendBroadcastServerMessage(killPlayerServerMessage);
			} catch (IOException e) {
				e.printStackTrace();
			}
			WiFiServer.this.mMessagePool.recycleMessage(killPlayerServerMessage);
			
			mPlayerCount--;
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
	
}
