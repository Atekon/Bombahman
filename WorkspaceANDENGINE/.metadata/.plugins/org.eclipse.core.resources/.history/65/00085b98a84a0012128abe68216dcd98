package pt.cagojati.bombahman.multiplayer;

import org.andengine.extension.multiplayer.protocol.adt.message.IMessage;
import org.andengine.extension.multiplayer.protocol.adt.message.client.IClientMessage;
import org.andengine.extension.multiplayer.protocol.util.MessagePool;

import pt.cagojati.bombahman.GameActivity;

public interface IMultiplayerConnector {
	
	public MessagePool<IMessage> getMessagePool();

	public void sendClientMessage(IClientMessage msg);
	public void setActivity(GameActivity game);
}
