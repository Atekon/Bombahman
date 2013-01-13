package pt.cagojati.bombahman.multiplayer.messages;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.andengine.extension.multiplayer.protocol.adt.message.server.ServerMessage;

import pt.cagojati.bombahman.Player;

public class JoinedServerServerMessage extends ServerMessage {
	
	private int mNumPlayers;
	private int mPlayerId;
	private int[] playersToAdd;

	public JoinedServerServerMessage() {
		
	}

	@Override
	public short getFlag() {
		return MessageFlags.FLAG_MESSAGE_SERVER_JOINED_SERVER;
	}

	@Override
	protected void onReadTransmissionData(final DataInputStream pDataInputStream) throws IOException {
		setNumPlayers(pDataInputStream.readInt());
		this.mPlayerId = pDataInputStream.readInt();
		for(int i =0; i <this.mNumPlayers;i++){
			this.getPlayersToAdd()[i] = pDataInputStream.readInt();
		}
	}

	@Override
	protected void onWriteTransmissionData(final DataOutputStream pDataOutputStream) throws IOException {
		pDataOutputStream.writeInt(this.mNumPlayers);
		pDataOutputStream.writeInt(this.mPlayerId);
		for(int i =0; i <this.mNumPlayers;i++){
			pDataOutputStream.writeInt(this.getPlayersToAdd()[i]);
		}
	}

	public int getNumPlayers() {
		return mNumPlayers;
	}

	public void setNumPlayers(int numPlayers) {
		this.mNumPlayers = numPlayers;
		this.setPlayersToAdd(new int[numPlayers]);
	}
	
	public int getPlayerId() {
		return mPlayerId;
	}

	public void setPlayerId(int playerId) {
		this.mPlayerId = playerId;
	}

	public int[] getPlayersToAdd() {
		return playersToAdd;
	}

	public void setPlayersToAdd(int[] playersToAdd) {
		this.playersToAdd = playersToAdd;
	}
}

