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

public class WiFiLobbyServer implements ILobbyServer {
	
	private static final int SERVER_PORT = 4444;
	
	MessagePool<IMessage> mMessagePool = new MessagePool<IMessage>();
	private SocketServer<SocketConnectionClientConnector> mSocketServer;
	private static WiFiLobbyServer instance = null;
	private int mPlayerCount = 0;

	private WiFiLobbyServer() {
		MessageFlags.initMessagePool(mMessagePool);
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
				
				

				return clientConnector;
			}
		};
		this.mSocketServer.start();
	}
	
	private class ExampleClientConnectorListener implements ISocketConnectionClientConnectorListener {
		@Override
		public void onStarted(final ClientConnector<SocketConnection> pConnector) {	
			
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