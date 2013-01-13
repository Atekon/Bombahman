package pt.cagojati.bombahman.multiplayer;

import java.io.IOException;
import java.net.Socket;

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

import pt.cagojati.bombahman.Bomb;
import pt.cagojati.bombahman.GameActivity;
import pt.cagojati.bombahman.Player;
import pt.cagojati.bombahman.multiplayer.messages.AddBombServerMessage;
import pt.cagojati.bombahman.multiplayer.messages.AddPlayerServerMessage;
import pt.cagojati.bombahman.multiplayer.messages.AddPowerupServerMessage;
import pt.cagojati.bombahman.multiplayer.messages.ConnectionCloseServerMessage;
import pt.cagojati.bombahman.multiplayer.messages.ExplodeBombServerMessage;
import pt.cagojati.bombahman.multiplayer.messages.JoinedServerServerMessage;
import pt.cagojati.bombahman.multiplayer.messages.KillPlayerServerMessage;
import pt.cagojati.bombahman.multiplayer.messages.MessageFlags;
import pt.cagojati.bombahman.multiplayer.messages.MovePlayerServerMessage;
import android.util.Log;

public class WiFiConnector implements IMultiplayerConnector  {
	private static final int SERVER_PORT = 4444;
	
	MessagePool<IMessage> mMessagePool = new MessagePool<IMessage>();
	private ServerConnector<SocketConnection> mServerConnector;
	private String mServerIP;
	private GameActivity mGameActivity;
	
	public WiFiConnector(String ip) {
		MessageFlags.initMessagePool(mMessagePool);
		mServerIP = ip;
	}
	
	public void setActivity(GameActivity game){
		mGameActivity = game;
	}
	
	public void initClient()
	{
		try{
			this.mServerConnector = new SocketConnectionServerConnector(new SocketConnection(new Socket(this.mServerIP, SERVER_PORT)), new ExampleServerConnectorListener());
	
			this.mServerConnector.registerServerMessage(MessageFlags.FLAG_MESSAGE_SERVER_CONNECTION_CLOSE, ConnectionCloseServerMessage.class, new IServerMessageHandler<SocketConnection>() {
				@Override
				public void onHandleMessage(final ServerConnector<SocketConnection> pServerConnector, final IServerMessage pServerMessage) throws IOException {
					mGameActivity.finish();
				}
			});
	
			this.mServerConnector.registerServerMessage(MessageFlags.FLAG_MESSAGE_SERVER_ADD_BOMB, AddBombServerMessage.class, new IServerMessageHandler<SocketConnection>() {
				@Override
				public void onHandleMessage(final ServerConnector<SocketConnection> pServerConnector, final IServerMessage pServerMessage) throws IOException {
					final AddBombServerMessage addBombServerMessage = (AddBombServerMessage)pServerMessage;
					Player player = GameActivity.getPlayer(addBombServerMessage.getPlayerId());
					player.dropBomb(addBombServerMessage.getX(),addBombServerMessage.getY(), addBombServerMessage.getBombId());
				}
			});
			
			this.mServerConnector.registerServerMessage(MessageFlags.FLAG_MESSAGE_SERVER_ADD_PLAYER, AddPlayerServerMessage.class, new IServerMessageHandler<SocketConnection>() {
				@Override
				public void onHandleMessage(final ServerConnector<SocketConnection> pServerConnector, final IServerMessage pServerMessage) throws IOException {
					final AddPlayerServerMessage addPlayerServerMessage = (AddPlayerServerMessage) pServerMessage;
					mGameActivity.addPlayer();
					if(addPlayerServerMessage.isPlayer()){
						mGameActivity.setCurrentPlayerServerMessage();
					}
				}
			});
			
			this.mServerConnector.registerServerMessage(MessageFlags.FLAG_MESSAGE_SERVER_JOINED_SERVER, JoinedServerServerMessage.class, new IServerMessageHandler<SocketConnection>() {
				@Override
				public void onHandleMessage(final ServerConnector<SocketConnection> pServerConnector, final IServerMessage pServerMessage) throws IOException {
					final JoinedServerServerMessage joinedServerServerMessage = (JoinedServerServerMessage) pServerMessage;
					for(int i = 0; i <joinedServerServerMessage.getNumPlayers()+1; i++){
						mGameActivity.addPlayer();
					}
					if(joinedServerServerMessage.isPlayer()){
						mGameActivity.setCurrentPlayerServerMessage();
					}
				}
			});
			
			this.mServerConnector.registerServerMessage(MessageFlags.FLAG_MESSAGE_SERVER_EXPLODE_BOMB, ExplodeBombServerMessage.class, new IServerMessageHandler<SocketConnection>() {
				@Override
				public void onHandleMessage(final ServerConnector<SocketConnection> pServerConnector, final IServerMessage pServerMessage) throws IOException {
					final ExplodeBombServerMessage explodeBombServerMessage = (ExplodeBombServerMessage) pServerMessage;
					Bomb bomb = GameActivity.getBombPool().getBomb(explodeBombServerMessage.getBombId());
					if(bomb !=null){
						bomb.unregisterTimerHandler();
						bomb.explode();
					}
				}
			});
			
			this.mServerConnector.registerServerMessage(MessageFlags.FLAG_MESSAGE_SERVER_KILL_PLAYER, KillPlayerServerMessage.class, new IServerMessageHandler<SocketConnection>() {
				@Override
				public void onHandleMessage(final ServerConnector<SocketConnection> pServerConnector, final IServerMessage pServerMessage) throws IOException {
					final KillPlayerServerMessage killPlayerServerMessage = (KillPlayerServerMessage) pServerMessage;
					mGameActivity.killPlayer(GameActivity.getPlayer(killPlayerServerMessage.getPlayerId()));
				}
			});
			
			this.mServerConnector.registerServerMessage(MessageFlags.FLAG_MESSAGE_SERVER_MOVE_PLAYER, MovePlayerServerMessage.class, new IServerMessageHandler<SocketConnection>() {
				@Override
				public void onHandleMessage(final ServerConnector<SocketConnection> pServerConnector, final IServerMessage pServerMessage) throws IOException {
					final MovePlayerServerMessage movePlayerServerMessage = (MovePlayerServerMessage) pServerMessage;
					Player player = GameActivity.getPlayer(movePlayerServerMessage.getPlayerId());
					DeadReckoningClient.moveRemotePlayer(movePlayerServerMessage.getX(), movePlayerServerMessage.getY(),movePlayerServerMessage.getVX(),movePlayerServerMessage.getVY(), player);
				}
			});
			
			this.mServerConnector.registerServerMessage(MessageFlags.FLAG_MESSAGE_SERVER_ADD_POWERUPS, AddPowerupServerMessage.class, new IServerMessageHandler<SocketConnection>() {

				@Override
				public void onHandleMessage(ServerConnector<SocketConnection> pServerConnector,IServerMessage pServerMessage) throws IOException {
					final AddPowerupServerMessage addPowerupServerMessage = (AddPowerupServerMessage) pServerMessage;
					mGameActivity.addPowerups(addPowerupServerMessage.getPowerUpList());
				}
			});
	
			this.mServerConnector.getConnection().start();
		}catch (final Throwable t) {
			Log.d("oteste", t.getMessage());
			Debug.e(t);
			mGameActivity.finish();
		}
	}
	
	private class ExampleServerConnectorListener implements ISocketConnectionServerConnectorListener {
		@Override
		public void onStarted(final ServerConnector<SocketConnection> pConnector) {
		}

		@Override
		public void onTerminated(final ServerConnector<SocketConnection> pConnector) {
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void terminate() {
		if(this.mServerConnector!=null){
			this.mServerConnector.terminate();
		}
	}
}
