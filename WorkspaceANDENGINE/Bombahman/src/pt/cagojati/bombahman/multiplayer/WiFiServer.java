package pt.cagojati.bombahman.multiplayer;

import java.io.IOException;
import java.util.ArrayList;

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
import pt.cagojati.bombahman.multiplayer.messages.AddBombClientMessage;
import pt.cagojati.bombahman.multiplayer.messages.AddBombServerMessage;
import pt.cagojati.bombahman.multiplayer.messages.AddPlayerServerMessage;
import pt.cagojati.bombahman.multiplayer.messages.ExplodeBombServerMessage;
import pt.cagojati.bombahman.multiplayer.messages.JoinedServerServerMessage;
import pt.cagojati.bombahman.multiplayer.messages.MessageFlags;
import pt.cagojati.bombahman.multiplayer.messages.MovePlayerClientMessage;
import pt.cagojati.bombahman.multiplayer.messages.MovePlayerServerMessage;
import android.util.Log;

public class WiFiServer implements IMultiplayerServer {
	
	private static final int SERVER_PORT = 4444;
	
	MessagePool<IMessage> mMessagePool = new MessagePool<IMessage>();
	private SocketServer<SocketConnectionClientConnector> mSocketServer;
	private static WiFiServer instance = null;
	private int mPlayerCount = 0;

	private WiFiServer() {
		MessageFlags.initMessagePool(mMessagePool);
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
						final String bombId = addBombClientMessage.getBombId();
						GameActivity.getScene().registerUpdateHandler(new TimerHandler(3, new ITimerCallback() {
							
							@Override
							public void onTimePassed(TimerHandler pTimerHandler) {
								final ExplodeBombServerMessage explodeBombServerMessage = (ExplodeBombServerMessage) WiFiServer.this.mMessagePool.obtainMessage(MessageFlags.FLAG_MESSAGE_SERVER_EXPLODE_BOMB);
								explodeBombServerMessage.set(bombId);
								try {
									WiFiServer.this.mSocketServer.sendBroadcastServerMessage(explodeBombServerMessage);
								} catch (IOException e) {
									e.printStackTrace();
								}
								
								WiFiServer.this.mMessagePool.recycleMessage(explodeBombServerMessage);
							}
						}));
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
							Log.d("oteste", "lag on player" + movePlayerClientMessage.getPlayerId());
							final MovePlayerServerMessage movePlayerServerMessage = (MovePlayerServerMessage) WiFiServer.this.mMessagePool.obtainMessage(MessageFlags.FLAG_MESSAGE_SERVER_MOVE_PLAYER);
							movePlayerServerMessage.set(movePlayerClientMessage.getX(), movePlayerClientMessage.getY(),movePlayerClientMessage.getVX(),movePlayerClientMessage.getVY(),movePlayerClientMessage.getPlayerId());
							
							pClientConnector.sendServerMessage(movePlayerServerMessage);
							
							WiFiServer.this.mMessagePool.recycleMessage(movePlayerServerMessage);
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
			//first player should be server right?
			if(mPlayerCount==0){
				DeadReckoningServer.startTimer();
			}
			try {
				AddPlayerServerMessage addPlayerServerMessage = (AddPlayerServerMessage) WiFiServer.this.mMessagePool.obtainMessage(MessageFlags.FLAG_MESSAGE_SERVER_ADD_PLAYER);
				addPlayerServerMessage.setIsPlayer(false);
				WiFiServer.this.mSocketServer.sendBroadcastServerMessage(addPlayerServerMessage);
				WiFiServer.this.mMessagePool.recycleMessage(addPlayerServerMessage);
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				JoinedServerServerMessage joinedServerServerMessage = (JoinedServerServerMessage) WiFiServer.this.mMessagePool.obtainMessage(MessageFlags.FLAG_MESSAGE_SERVER_JOINED_SERVER);
				joinedServerServerMessage.setIsPlayer(true);
				joinedServerServerMessage.setNumPlayers(mPlayerCount);
				pConnector.sendServerMessage(joinedServerServerMessage);
				
				WiFiServer.this.mMessagePool.recycleMessage(joinedServerServerMessage);
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			mPlayerCount++;
		}

		@Override
		public void onTerminated(final ClientConnector<SocketConnection> pConnector) {
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
	}

	@Override
	public void sendBroadcastServerMessage(IServerMessage msg) throws IOException {
		this.mSocketServer.sendBroadcastServerMessage(msg);
	}
	
}
