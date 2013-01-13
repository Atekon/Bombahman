package pt.cagojati.bombahman.multiplayer.messages;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.andengine.extension.multiplayer.protocol.adt.message.server.ServerMessage;

public class SetPowerupsServerMessage extends ServerMessage{
private boolean mHasPowerups;
	
	public SetPowerupsServerMessage(){
		
	}
	
	public SetPowerupsServerMessage(boolean hasPowerups){
		this.setPowerups(hasPowerups);
	}
	
	@Override
	public short getFlag() {
		return MessageFlags.FLAG_MESSAGE_SERVER_SET_POWERUPS;
	}

	@Override
	protected void onReadTransmissionData(DataInputStream pDataInputStream)
			throws IOException {
		this.setPowerups(pDataInputStream.readBoolean());
		
	}

	@Override
	protected void onWriteTransmissionData(DataOutputStream pDataOutputStream)
			throws IOException {
		pDataOutputStream.writeBoolean(this.hasPowerups());
		
	}

	public boolean hasPowerups() {
		return mHasPowerups;
	}

	public void setPowerups(boolean hasPowerups) {
		this.mHasPowerups = hasPowerups;
	}
}
