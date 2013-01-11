package pt.cagojati.bombahman.multiplayer;

import org.andengine.engine.handler.timer.ITimerCallback;
import org.andengine.engine.handler.timer.TimerHandler;

import pt.cagojati.bombahman.GameActivity;
import pt.cagojati.bombahman.Player;
import pt.cagojati.bombahman.multiplayer.messages.MovePlayerClientMessage;
import android.util.Log;

public class DeadReckoning {
	private static Player mPlayer;
	private static TimerHandler mTimer;
	private static float mTime = 0.5f;
	private static float oldPosX, oldPosY;
	
	public static void setPlayer(Player player)
	{
		mPlayer = player;
	}
	
	public static void startTimer()
	{
		mTimer = new TimerHandler(mTime,true, new ITimerCallback() {
			
			@Override
			public void onTimePassed(TimerHandler pTimerHandler) {
				DeadReckoning.sendMoveMessage(DeadReckoning.mPlayer.getVelX(),DeadReckoning.mPlayer.getVelY());
			};
		});
		GameActivity.getScene().registerUpdateHandler(mTimer);
	}
	
	public static void sendMoveMessage(float vX, float vY)
	{
		mPlayer.getDeadBoundBox().setPosition(mPlayer.getPosX()-mPlayer.getDeadBoundBox().getWidthScaled()/2, mPlayer.getPosY()-mPlayer.getDeadBoundBox().getHeightScaled()/2);
		MovePlayerClientMessage message = new MovePlayerClientMessage((int)mPlayer.getPosX(), (int)mPlayer.getPosY(), vX,vY, mPlayer.getId());
		GameActivity.getConnector().sendClientMessage(message);
		mTimer.reset();
	}
	
	public static void moveRemotePlayer(final float posX, final float posY,final float vX, final float vY, Player player){	
		float difPosX = posX - oldPosX;
		float difPosY = posY - oldPosY;
		
		if(Math.abs(difPosX)<1)
		{
			difPosX = 0;
		}
		if(Math.abs(difPosY)<1)
		{
			difPosY = 0;
		}
		
		float max = Math.max(Math.abs(difPosX), Math.abs(difPosY));
		player.setPos(posX, posY);
		if(max != 0){
			float velX = difPosX/max;
			float velY = difPosY/max;
			
			player.move(vX,vY);
			Log.d("oteste","velX"+velX);
			Log.d("oteste","velY"+velY);
		}else{
			player.move(0,0);
		}
		player.animate(difPosX, difPosY);
		
		oldPosX = posX;
		oldPosY = posY;
	}
}
