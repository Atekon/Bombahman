package pt.cagojati.bombahman.multiplayer;

import org.andengine.engine.handler.timer.ITimerCallback;
import org.andengine.engine.handler.timer.TimerHandler;

import pt.cagojati.bombahman.GameActivity;

public class DeadReckoningServer {
	
	private static float[] lastMessage = {0,0,0,0};
	private static float elapsedTime=0;
	private static float compareTime = 0.6f;
	
	public static void startTimer()
	{
		final float deltaTime = 0.025f;
		GameActivity.getScene().registerUpdateHandler(new TimerHandler(deltaTime, true, new ITimerCallback() {
			@Override
			public void onTimePassed(TimerHandler pTimerHandler) {
				elapsedTime+=deltaTime;
			}
		}));
	}
	
	/**
	 * Validates whether if a player lagged out or not (also sets the newest time)
	 * @param playerId
	 * @return
	 */
	public static boolean validateMessage(int playerId)
	{
		boolean isValid = !(lastMessage[playerId]!=0 && elapsedTime > lastMessage[playerId]+compareTime);
		lastMessage[playerId] = elapsedTime;
		return isValid;
	}
}
