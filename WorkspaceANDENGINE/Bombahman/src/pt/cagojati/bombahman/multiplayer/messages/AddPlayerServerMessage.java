package pt.cagojati.bombahman.multiplayer.messages;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.andengine.extension.multiplayer.protocol.adt.message.server.ServerMessage;

import pt.cagojati.bombahman.Player;

public class AddPlayerServerMessage extends ServerMessage {
	
	private boolean mIsPlayer;

	public AddPlayerServerMessage() {

	}

	@Override
	public short getFlag() {
		return MessageFlags.FLAG_MESSAGE_SERVER_ADD_PLAYER;
	}

	@Override
	protected void onReadTransmissionData(final DataInputStream pDataInputStream) throws IOException {
		this.mIsPlayer = pDataInputStream.readBoolean();
	}

	@Override
	protected void onWriteTransmissionData(final DataOutputStream pDataOutputStream) throws IOException {
		pDataOutputStream.writeBoolean(this.mIsPlayer);
	}

	public boolean isPlayer() {
		return mIsPlayer;
	}

	public void setIsPlayer(boolean mIsPlayer) {
		this.mIsPlayer = mIsPlayer;
	}
}

