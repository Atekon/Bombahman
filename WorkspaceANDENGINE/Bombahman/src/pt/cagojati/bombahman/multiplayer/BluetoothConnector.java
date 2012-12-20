package pt.cagojati.bombahman.multiplayer;

import org.andengine.extension.multiplayer.protocol.adt.message.IMessage;
import org.andengine.extension.multiplayer.protocol.adt.message.client.IClientMessage;
import org.andengine.extension.multiplayer.protocol.util.MessagePool;

import pt.cagojati.bombahman.GameActivity;

public class BluetoothConnector implements IMultiplayerConnector {

	@Override
	public MessagePool<IMessage> getMessagePool() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void sendClientMessage(IClientMessage msg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setActivity(GameActivity game) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void initClient() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void terminate() {
		// TODO Auto-generated method stub
		
	}

}
