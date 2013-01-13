package pt.cagojati.bombahman.multiplayer.messages;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.andengine.extension.multiplayer.protocol.adt.message.server.ServerMessage;

import pt.cagojati.bombahman.Player;

public class StartSuddenDeathServerMessage extends ServerMessage {
	
	public StartSuddenDeathServerMessage() {

	}
	

	@Override
	public short getFlag() {
		return MessageFlags.FLAG_MESSAGE_SERVER_START_SUDDEN_DEATH;
	}

	@Override
	protected void onReadTransmissionData(final DataInputStream pDataInputStream) throws IOException {
	}

	@Override
	protected void onWriteTransmissionData(final DataOutputStream pDataOutputStream) throws IOException {
	}

}

