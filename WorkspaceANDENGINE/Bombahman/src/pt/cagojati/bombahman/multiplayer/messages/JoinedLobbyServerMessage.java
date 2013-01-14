package pt.cagojati.bombahman.multiplayer.messages;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.andengine.extension.multiplayer.protocol.adt.message.server.ServerMessage;

import pt.cagojati.bombahman.Player;

public class JoinedLobbyServerMessage extends ServerMessage {
	
	private boolean mIsPlayer;
	private int mNumPlayers;
	private boolean[] mPlayersReady = new boolean[4];
	private int mCurrentMap;
	private int mTime;
	private boolean mPowerupsEnable;

	public JoinedLobbyServerMessage() {

	}

	@Override
	public short getFlag() {
		return MessageFlags.FLAG_MESSAGE_SERVER_LOBBY_JOINED;
	}

	@Override
	protected void onReadTransmissionData(final DataInputStream pDataInputStream) throws IOException {
		this.mIsPlayer = pDataInputStream.readBoolean();
		this.mNumPlayers = pDataInputStream.readInt();
		this.mCurrentMap = pDataInputStream.readInt();
		this.mTime = pDataInputStream.readInt();
		this.mPowerupsEnable = pDataInputStream.readBoolean();
		for(int i=0; i<4; i++)
		{
			this.getPlayersReady()[i] = pDataInputStream.readBoolean();
		}
		
	}

	@Override
	protected void onWriteTransmissionData(final DataOutputStream pDataOutputStream) throws IOException {
		pDataOutputStream.writeBoolean(this.mIsPlayer);
		pDataOutputStream.writeInt(this.mNumPlayers);
		pDataOutputStream.writeInt(this.mCurrentMap);
		pDataOutputStream.writeInt(this.mTime);
		pDataOutputStream.writeBoolean(this.mPowerupsEnable);
		for(int i=0; i<4; i++)
		{
			pDataOutputStream.writeBoolean(this.getPlayersReady()[i]);
		}
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

	public boolean[] getPlayersReady() {
		return mPlayersReady;
	}

	public void setPlayersReady(boolean[] mPlayersReady) {
		this.mPlayersReady = mPlayersReady;
	}

	public int getCurrentMap() {
		return mCurrentMap;
	}

	public void setCurrentMap(int mCurrentMap) {
		this.mCurrentMap = mCurrentMap;
	}

	public int getTime() {
		return mTime;
	}

	public void setTime(int mTime) {
		this.mTime = mTime;
	}

	public boolean isPowerupsEnable() {
		return mPowerupsEnable;
	}

	public void setPowerupsEnable(boolean mPowerupsEnable) {
		this.mPowerupsEnable = mPowerupsEnable;
	}
}

