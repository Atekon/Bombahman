package pt.cagojati.bombahman.multiplayer.messages;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.andengine.extension.multiplayer.protocol.adt.message.server.ServerMessage;

import pt.cagojati.bombahman.Player;

public class JoinedServerServerMessage extends ServerMessage {
	
	private boolean mIsPlayer;
	private int mNumPlayers;

	public JoinedServerServerMessage() {

	}

	@Override
	public short getFlag() {
		return MessageFlags.FLAG_MESSAGE_SERVER_JOINED_SERVER;
	}

	@Override
	protected void onReadTransmissionData(final DataInputStream pDataInputStream) throws IOException {
		this.mIsPlayer = pDataInputStream.readBoolean();
		this.mNumPlayers = pDataInputStream.readInt();
	}

	@Override
	protected void onWriteTransmissionData(final DataOutputStream pDataOutputStream) throws IOException {
		pDataOutputStream.writeBoolean(this.mIsPlayer);
		pDataOutputStream.writeInt(this.mNumPlayers);
	}

	public boolean isPlayer() {
		return mIsPlayer;
	}

	public void setIsPlayer(boolean mIsPlayer) {
		this.mIsPlayer = mIsPlayer;
	}

	public int getNumPlayers() {
		return mNumPlayers;
	}

	public void setNumPlayers(int numPlayers) {
		this.mNumPlayers = numPlayers;
	}
}

