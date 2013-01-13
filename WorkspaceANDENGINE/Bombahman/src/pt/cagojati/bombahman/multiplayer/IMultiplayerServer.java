package pt.cagojati.bombahman.multiplayer;

import java.io.IOException;

import org.andengine.extension.multiplayer.protocol.adt.message.IMessage;
import org.andengine.extension.multiplayer.protocol.adt.message.server.IServerMessage;
import org.andengine.extension.multiplayer.protocol.server.Server;
import org.andengine.extension.multiplayer.protocol.server.connector.ClientConnector;
import org.andengine.extension.multiplayer.protocol.shared.Connection;
import org.andengine.extension.multiplayer.protocol.util.MessagePool;

import pt.cagojati.bombahman.multiplayer.messages.KillPlayerServerMessage;

public interface IMultiplayerServer {

	@SuppressWarnings("rawtypes")
	public Server getServerSocket();
	
	public void initServer();
	public void terminate();
	
	public void sendBroadcastServerMessage(IServerMessage msg) throws IOException;

	public MessagePool<IMessage> getMessagePool();
}
