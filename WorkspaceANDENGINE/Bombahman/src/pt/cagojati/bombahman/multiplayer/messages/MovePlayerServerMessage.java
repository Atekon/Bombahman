package pt.cagojati.bombahman.multiplayer.messages;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.andengine.extension.multiplayer.protocol.adt.message.server.ServerMessage;

import pt.cagojati.bombahman.Player;

public class MovePlayerServerMessage extends ServerMessage {
	private int mX;
	private int mY;
	private int mPlayerId;

	public MovePlayerServerMessage() {

	}

	public MovePlayerServerMessage(final int pX, final int pY, final int playerid) {
		this.setX(pX);
		this.setY(pY);
		this.setPlayerId(playerid);
	}

	public void set(final int pX, final int pY, final int playerId) {
		this.setX(pX);
		this.setY(pY);
		this.setPlayerId(playerId);
	}

	@Override
	public short getFlag() {
		return MessageFlags.FLAG_MESSAGE_SERVER_MOVE_PLAYER;
	}

	@Override
	protected void onReadTransmissionData(final DataInputStream pDataInputStream) throws IOException {
		this.setX(pDataInputStream.readInt());
		this.setY(pDataInputStream.readInt());
		this.setPlayerId(pDataInputStream.readInt());
	}

	@Override
	protected void onWriteTransmissionData(final DataOutputStream pDataOutputStream) throws IOException {
		pDataOutputStream.writeInt(this.getX());
		pDataOutputStream.writeInt(this.getY());
		pDataOutputStream.writeInt(this.getPlayerId());
	}

	public int getX() {
		return mX;
	}

	public void setX(int mX) {
		this.mX = mX;
	}

	public int getY() {
		return mY;
	}

	public void setY(int mY) {
		this.mY = mY;
	}

	public int getPlayerId() {
		return mPlayerId;
	}

	private void setPlayerId(int playerId) {
		this.mPlayerId = playerId;
	}
}

