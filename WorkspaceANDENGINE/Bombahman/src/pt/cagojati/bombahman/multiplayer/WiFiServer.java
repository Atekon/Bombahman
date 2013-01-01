package pt.cagojati.bombahman.multiplayer;

import java.io.IOException;

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

import android.util.Log;

import pt.cagojati.bombahman.multiplayer.messages.AddBombClientMessage;
import pt.cagojati.bombahman.multiplayer.messages.AddBombServerMessage;
import pt.cagojati.bombahman.multiplayer.messages.MessageFlags;

public class WiFiServer implements IMultiplayerServer {
	
	private static final int SERVER_PORT = 4444;
	
	MessagePool<IMessage> mMessagePool = new MessagePool<IMessage>();
	private SocketServer<SocketConnectionClientConnector> mSocketServer;
	private static WiFiServer instance = null;

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
						addBombServerMessage.set(addBombClientMessage.getX(), addBombClientMessage.getY(),addBombClientMessage.getPlayerId());

						WiFiServer.this.mSocketServer.sendBroadcastServerMessage(addBombServerMessage);

						WiFiServer.this.mMessagePool.recycleMessage(addBombServerMessage);
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
			Log.d("oteste",pConnector.getConnection().getSocket().getInetAddress().getHostAddress());
		}

		@Override
		public void onTerminated(final ClientConnector<SocketConnection> pConnector) {
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
