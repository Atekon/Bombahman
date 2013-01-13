package pt.cagojati.bombahman.multiplayer.messages;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.andengine.extension.multiplayer.protocol.adt.message.server.ServerMessage;

import pt.cagojati.bombahman.BombPowerup;
import pt.cagojati.bombahman.Brick;
import pt.cagojati.bombahman.FirePowerup;
import pt.cagojati.bombahman.IPowerUp;
import pt.cagojati.bombahman.Player;

public class AddPowerupServerMessage extends ServerMessage {
	private IPowerUp[] powerUpList;

	public AddPowerupServerMessage() {
		
	}

	public AddPowerupServerMessage(IPowerUp[] vec) {
		set(vec);
	}

	public void set(IPowerUp[] vec) {
		setPowerUpList(vec);
	}

	@Override
	public short getFlag() {
		return MessageFlags.FLAG_MESSAGE_SERVER_ADD_POWERUPS;
	}

	@Override
	protected void onReadTransmissionData(final DataInputStream pDataInputStream) throws IOException {
		powerUpList = new IPowerUp[14];
		for(int j = 0; j<14; j++){
			int type = pDataInputStream.readInt();
			switch(type){
			case 0: 
				FirePowerup fire = new FirePowerup();
				fire.setX(pDataInputStream.readFloat());
				fire.setY(pDataInputStream.readFloat());
				powerUpList[j] = fire;
				break;
			case 1:
				BombPowerup bomb = new BombPowerup();
				bomb.setX(pDataInputStream.readFloat());
				bomb.setY(pDataInputStream.readFloat());
				powerUpList[j] = bomb;
				break;
			}
		}
	}

	@Override
	protected void onWriteTransmissionData(final DataOutputStream pDataOutputStream) throws IOException {
		for(int i = 0; i<getPowerUpList().length; i++){
			pDataOutputStream.writeInt(getPowerUpList()[i].getType());
			pDataOutputStream.writeFloat(getPowerUpList()[i].getX());
			pDataOutputStream.writeFloat(getPowerUpList()[i].getY());
		}
	}

	public IPowerUp[] getPowerUpList() {
		return powerUpList;
	}

	public void setPowerUpList(IPowerUp[] powerUpList) {
		this.powerUpList = powerUpList;
	}
	
	
}

