package pt.cagojati.bombahman.multiplayer.messages;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.andengine.extension.multiplayer.protocol.adt.message.server.ServerMessage;

public class CurrentTimeServerMessage extends ServerMessage{
private int mCurrentTime;
	
	public CurrentTimeServerMessage(){
		
	}
	
	public CurrentTimeServerMessage(int currentMap){
		this.setCurrentTime(currentMap);
	}
	
	@Override
	public short getFlag() {
		return MessageFlags.FLAG_MESSAGE_SERVER_CURRENT_TIME;
	}

	@Override
	protected void onReadTransmissionData(DataInputStream pDataInputStream)
			throws IOException {
		this.setCurrentTime(pDataInputStream.readInt());
		
	}

	@Override
	protected void onWriteTransmissionData(DataOutputStream pDataOutputStream)
			throws IOException {
		pDataOutputStream.writeInt(this.getCurrentTime());
		
	}

	public int getCurrentTime() {
		return mCurrentTime;
	}

	public void setCurrentTime(int currentTime) {
		this.mCurrentTime = currentTime;
	}
}
