package pt.cagojati.bombahman.multiplayer.messages;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.andengine.extension.multiplayer.protocol.adt.message.server.ServerMessage;

import pt.cagojati.bombahman.Player;

public class ExplodeBombServerMessage extends ServerMessage {
	private String mBombId;

	public ExplodeBombServerMessage() {

	}

	public ExplodeBombServerMessage(final String bombId) {
		this.setBombId(bombId);
	}

	public void set(final String bombId) {
		this.setBombId(bombId);
	}

	@Override
	public short getFlag() {
		return MessageFlags.FLAG_MESSAGE_SERVER_EXPLODE_BOMB;
	}

	@Override
	protected void onReadTransmissionData(final DataInputStream pDataInputStream) throws IOException {
		this.setBombId(pDataInputStream.readUTF());
	}

	@Override
	protected void onWriteTransmissionData(final DataOutputStream pDataOutputStream) throws IOException {
		pDataOutputStream.writeUTF(getBombId());
	}
	
	public String getBombId() {
		return mBombId;
	}

	private void setBombId(String mBombId) {
		this.mBombId = mBombId;
	}
	
}

