package pt.cagojati.bombahman.multiplayer;

import java.io.IOException;

import org.andengine.extension.multiplayer.protocol.adt.message.IMessage;
import org.andengine.extension.multiplayer.protocol.adt.message.client.IClientMessage;
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

import pt.cagojati.bombahman.multiplayer.messages.AddFaceClientMessage;
import pt.cagojati.bombahman.multiplayer.messages.AddFaceServerMessage;
import pt.cagojati.bombahman.multiplayer.messages.MessageFlags;

public class WiFiServer implements IMultiplayerServer {
	
	private static final int SERVER_PORT = 4444;
	
	MessagePool<IMessage> mMessagePool = new MessagePool<IMessage>();
	private SocketServer<SocketConnectionClientConnector> mSocketServer;

	public WiFiServer() {
		MessageFlags.initMessagePool(mMessagePool);
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
				
				clientConnector.registerClientMessage(MessageFlags.FLAG_MESSAGE_CLIENT_ADD_FACE, AddFaceClientMessage.class, new IClientMessageHandler<SocketConnection>() {
					@Override
					public void onHandleMessage(final ClientConnector<SocketConnection> pClientConnector, final IClientMessage pClientMessage) throws IOException {
						final AddFaceClientMessage addFaceClientMessage = (AddFaceClientMessage) pClientMessage;
						
						final AddFaceServerMessage addFaceServerMessage = (AddFaceServerMessage) WiFiServer.this.mMessagePool.obtainMessage(MessageFlags.FLAG_MESSAGE_SERVER_ADD_FACE);
						addFaceServerMessage.set(addFaceClientMessage.getX(), addFaceClientMessage.getY());

						WiFiServer.this.mSocketServer.sendBroadcastServerMessage(addFaceServerMessage);

						WiFiServer.this.mMessagePool.recycleMessage(addFaceServerMessage);
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
			Log.d("oteste","servidor crashou");

		}

	}
	
}
