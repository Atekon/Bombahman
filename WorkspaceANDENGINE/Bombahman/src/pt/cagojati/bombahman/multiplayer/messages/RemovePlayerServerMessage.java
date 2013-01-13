package pt.cagojati.bombahman.multiplayer.messages;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.andengine.extension.multiplayer.protocol.adt.message.server.ServerMessage;

public class RemovePlayerServerMessage extends ServerMessage{

	private int mPlayerId;
	
	@Override
	public short getFlag() {
		return MessageFlags.FLAG_MESSAGE_SERVER_REMOVE_PLAYER;
	}

	@Override
	protected void onReadTransmissionData(DataInputStream pDataInputStream)
			throws IOException {
		this.setPlayerId(pDataInputStream.readInt());
		
	}

	@Override
	protected void onWriteTransmissionData(DataOutputStream pDataOutputStream)
			throws IOException {
		pDataOutputStream.writeInt(this.getPlayerId());
		
	}

	public int getPlayerId() {
		return mPlayerId;
	}

	public void setPlayerId(int mPlayerId) {
		this.mPlayerId = mPlayerId;
	}

}
