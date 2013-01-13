package pt.cagojati.bombahman.multiplayer.messages;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.andengine.extension.multiplayer.protocol.adt.message.server.ServerMessage;

public class CurrentMapServerMessage extends ServerMessage{

	private int mCurrentMap;
	
	public CurrentMapServerMessage(){
		
	}
	
	public CurrentMapServerMessage(int currentMap){
		this.setCurrentMap(currentMap);
	}
	
	@Override
	public short getFlag() {
		return MessageFlags.FLAG_MESSAGE_SERVER_CURRENT_MAP;
	}

	@Override
	protected void onReadTransmissionData(DataInputStream pDataInputStream)
			throws IOException {
		this.setCurrentMap(pDataInputStream.readInt());
		
	}

	@Override
	protected void onWriteTransmissionData(DataOutputStream pDataOutputStream)
			throws IOException {
		pDataOutputStream.writeInt(this.getCurrentMap());
		
	}

	public int getCurrentMap() {
		return mCurrentMap;
	}

	public void setCurrentMap(int currentMap) {
		this.mCurrentMap = currentMap;
	}
	
}
