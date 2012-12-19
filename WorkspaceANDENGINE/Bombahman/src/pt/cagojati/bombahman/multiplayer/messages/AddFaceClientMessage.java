package pt.cagojati.bombahman.multiplayer.messages;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.andengine.extension.multiplayer.protocol.adt.message.client.ClientMessage;

public class AddFaceClientMessage extends ClientMessage {
	private float mX;
	private float mY;

	public AddFaceClientMessage() {

	}

	public AddFaceClientMessage(final float pX, final float pY) {
		this.setX(pX);
		this.setY(pY);
	}

	public void set(final float pX, final float pY) {
		this.setX(pX);
		this.setY(pY);
	}

	@Override
	public short getFlag() {
		return MessageFlags.FLAG_MESSAGE_CLIENT_ADD_FACE;
	}

	@Override
	protected void onReadTransmissionData(final DataInputStream pDataInputStream) throws IOException {
		this.setX(pDataInputStream.readFloat());
		this.setY(pDataInputStream.readFloat());
	}

	@Override
	protected void onWriteTransmissionData(final DataOutputStream pDataOutputStream) throws IOException {
		pDataOutputStream.writeFloat(this.getX());
		pDataOutputStream.writeFloat(this.getY());
	}

	public float getY() {
		return mY;
	}

	public void setY(float mY) {
		this.mY = mY;
	}

	public float getX() {
		return mX;
	}

	public void setX(float mX) {
		this.mX = mX;
	}
}