package pt.cagojati.bombahman.multiplayer;

import java.net.InetAddress;

import org.andengine.extension.multiplayer.protocol.adt.message.IMessage;
import org.andengine.extension.multiplayer.protocol.adt.message.server.IServerMessage;
import org.andengine.extension.multiplayer.protocol.server.Server;
import org.andengine.extension.multiplayer.protocol.util.MessagePool;

public class BluetoothServer implements IMultiplayerServer {

	@Override
	public Server getServerSocket() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void initServer() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void terminate() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sendBroadcastServerMessage(IServerMessage msg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public MessagePool<IMessage> getMessagePool() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setHostsAddreses(InetAddress[] hostsAddresses) {
		// TODO Auto-generated method stub
		
	}

}
