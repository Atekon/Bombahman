package pt.cagojati.bombahman.multiplayer.messages;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.andengine.extension.multiplayer.protocol.adt.message.client.ClientMessage;

import pt.cagojati.bombahman.Player;

public class MovePlayerClientMessage extends ClientMessage {
	private int mX;
	private int mY;
	private int mPlayerId;

	public MovePlayerClientMessage() {

	}

	public MovePlayerClientMessage(final int pX, final int pY, final int playerId) {
		this.setX(pX);
		this.setY(pY);
		this.setPlayerId(playerId);
	}

	public void set(final int pX, final int pY, int playerid, String bombId ) {
		this.setX(pX);
		this.setY(pY);
		this.setPlayerId(playerid);
	}

	@Override
	public short getFlag() {
		return MessageFlags.FLAG_MESSAGE_CLIENT_MOVE_PLAYER;
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

	public int getY() {
		return mY;
	}

	public void setY(int mY) {
		this.mY = mY;
	}

	public int getX() {
		return mX;
	}

	public void setX(int mX) {
		this.mX = mX;
	}

	public int getPlayerId() {
		return mPlayerId;
	}

	private void setPlayerId(int playerId) {
		this.mPlayerId = playerId;
	}
}