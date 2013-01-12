package pt.cagojati.bombahman.multiplayer.messages;

import org.andengine.extension.multiplayer.protocol.adt.message.IMessage;
import org.andengine.extension.multiplayer.protocol.util.MessagePool;

public class MessageFlags {
	
	public static final short FLAG_MESSAGE_SERVER_CONNECTION_CLOSE = Short.MIN_VALUE;
	public static final short FLAG_MESSAGE_SERVER_LOBBY_JOINED = 0;
	public static final short FLAG_MESSAGE_SERVER_ADD_BOMB = 1;
	public static final short FLAG_MESSAGE_CLIENT_ADD_BOMB = 2;
	public static final short FLAG_MESSAGE_SERVER_ADD_PLAYER = 3;
	public static final short FLAG_MESSAGE_CLIENT_ADD_PLAYER = 4;
	public static final short FLAG_MESSAGE_SERVER_JOINED_SERVER = 5;
	public static final short FLAG_MESSAGE_SERVER_EXPLODE_BOMB = 6;
	public static final short FLAG_MESSAGE_SERVER_MOVE_PLAYER = 7;
	public static final short FLAG_MESSAGE_CLIENT_MOVE_PLAYER = 8;
	public static final short FLAG_MESSAGE_SERVER_KILL_PLAYER = 9;
			
	public static void initMessagePool(MessagePool<IMessage> messagePool) {
		messagePool.registerMessage(FLAG_MESSAGE_SERVER_LOBBY_JOINED, JoinedLobbyServerMessage.class);
		messagePool.registerMessage(FLAG_MESSAGE_SERVER_ADD_BOMB, AddBombServerMessage.class);
		messagePool.registerMessage(FLAG_MESSAGE_CLIENT_ADD_BOMB, AddBombClientMessage.class);
		messagePool.registerMessage(FLAG_MESSAGE_SERVER_ADD_PLAYER, AddPlayerServerMessage.class);
		messagePool.registerMessage(FLAG_MESSAGE_CLIENT_ADD_PLAYER, AddPlayerClientMessage.class);
		messagePool.registerMessage(FLAG_MESSAGE_SERVER_JOINED_SERVER, JoinedServerServerMessage.class);
		messagePool.registerMessage(FLAG_MESSAGE_SERVER_EXPLODE_BOMB, ExplodeBombServerMessage.class);
		messagePool.registerMessage(FLAG_MESSAGE_SERVER_MOVE_PLAYER, MovePlayerServerMessage.class);
		messagePool.registerMessage(FLAG_MESSAGE_CLIENT_MOVE_PLAYER, MovePlayerClientMessage.class);
		messagePool.registerMessage(FLAG_MESSAGE_SERVER_KILL_PLAYER, KillPlayerServerMessage.class);
	}
}
