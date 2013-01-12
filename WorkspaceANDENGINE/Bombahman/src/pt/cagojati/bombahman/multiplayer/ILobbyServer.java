package pt.cagojati.bombahman.multiplayer;

import java.io.IOException;

import org.andengine.extension.multiplayer.protocol.adt.message.server.IServerMessage;
import org.andengine.extension.multiplayer.protocol.server.Server;
import org.andengine.extension.multiplayer.protocol.server.connector.ClientConnector;
import org.andengine.extension.multiplayer.protocol.shared.Connection;

public interface ILobbyServer {

	@SuppressWarnings("rawtypes")
	public Server getServerSocket();
	
	public void initServer();
	public void terminate();
	
	public void sendBroadcastServerMessage(IServerMessage msg) throws IOException;
}
