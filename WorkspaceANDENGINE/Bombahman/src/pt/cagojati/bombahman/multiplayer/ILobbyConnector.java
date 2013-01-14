package pt.cagojati.bombahman.multiplayer;

import org.andengine.extension.multiplayer.protocol.adt.message.IMessage;
import org.andengine.extension.multiplayer.protocol.adt.message.client.IClientMessage;
import org.andengine.extension.multiplayer.protocol.util.MessagePool;

import pt.cagojati.bombahman.LobbyActivity;

public interface ILobbyConnector {
	
	public MessagePool<IMessage> getMessagePool();

	public void sendClientMessage(IClientMessage msg);
	public void setActivity(LobbyActivity game);
	public void initClient();
	
	public void terminate();
	public int getPlayerCount();

}