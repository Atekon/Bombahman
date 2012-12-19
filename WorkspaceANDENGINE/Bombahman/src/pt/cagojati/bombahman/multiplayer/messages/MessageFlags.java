package pt.cagojati.bombahman.multiplayer.messages;

import org.andengine.extension.multiplayer.protocol.adt.message.IMessage;
import org.andengine.extension.multiplayer.protocol.util.MessagePool;

public class MessageFlags {
	
	public static final short FLAG_MESSAGE_SERVER_CONNECTION_CLOSE = Short.MIN_VALUE;
	public static final short FLAG_MESSAGE_SERVER_ADD_FACE = 1;
	public static final short FLAG_MESSAGE_CLIENT_ADD_FACE = 2;
	
	public static void initMessagePool(MessagePool<IMessage> messagePool) {
		messagePool.registerMessage(FLAG_MESSAGE_SERVER_ADD_FACE, AddFaceServerMessage.class);
		messagePool.registerMessage(FLAG_MESSAGE_CLIENT_ADD_FACE, AddFaceClientMessage.class);
	}
}
