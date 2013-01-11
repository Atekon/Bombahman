package pt.cagojati.bombahman.multiplayer.messages;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.andengine.extension.multiplayer.protocol.adt.message.server.ServerMessage;

import pt.cagojati.bombahman.Player;

public class KillPlayerServerMessage extends ServerMessage {
	
	private int mPlayerId;

	public KillPlayerServerMessage() {

	}
	

	@Override
	public short getFlag() {
		return MessageFlags.FLAG_MESSAGE_SERVER_KILL_PLAYER;
	}

	@Override
	protected void onReadTransmissionData(final DataInputStream pDataInputStream) throws IOException {
		this.mPlayerId = pDataInputStream.readInt();
	}

	@Override
	protected void onWriteTransmissionData(final DataOutputStream pDataOutputStream) throws IOException {
		pDataOutputStream.writeInt(this.getPlayerId());
	}

	public int getPlayerId() {
		return this.mPlayerId;
	}

	public void setPlayerId(int playerId) {
		this.mPlayerId = playerId;
	}
}

