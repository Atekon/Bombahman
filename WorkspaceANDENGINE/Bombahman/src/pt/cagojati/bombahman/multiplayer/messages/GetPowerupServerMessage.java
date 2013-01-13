package pt.cagojati.bombahman.multiplayer.messages;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.andengine.extension.multiplayer.protocol.adt.message.server.ServerMessage;

import pt.cagojati.bombahman.Player;

public class GetPowerupServerMessage extends ServerMessage {
	private int mPlayerId;
	private float mX;
	private float mY;

	public GetPowerupServerMessage() {

	}

	public GetPowerupServerMessage(final int playerId, final float mX, final float mY) {
		this.setPlayerId(playerId);
		this.setX(mX);
		this.setY(mY);
	}

	public void set(final int playerId, final float mX, final float mY) {
		this.setPlayerId(playerId);
		this.setX(mX);
		this.setY(mY);
	}

	@Override
	public short getFlag() {
		return MessageFlags.FLAG_MESSAGE_SERVER_GET_POWERUP;
	}

	@Override
	protected void onReadTransmissionData(final DataInputStream pDataInputStream) throws IOException {
		this.setPlayerId(pDataInputStream.readInt());
		this.setX(pDataInputStream.readFloat());
		this.setY(pDataInputStream.readFloat());
	}

	@Override
	protected void onWriteTransmissionData(final DataOutputStream pDataOutputStream) throws IOException {
		pDataOutputStream.writeInt(getPlayerId());
		pDataOutputStream.writeFloat(this.getX());
		pDataOutputStream.writeFloat(this.getY());
	}
	
	public int getPlayerId() {
		return mPlayerId;
	}

	private void setPlayerId(int mPlayerId) {
		this.mPlayerId = mPlayerId;
	}

	private float getX() {
		return mX;
	}

	private void setX(float mX) {
		this.mX = mX;
	}

	private float getY() {
		return mY;
	}

	private void setY(float mY) {
		this.mY = mY;
	}
	
}

