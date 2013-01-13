package pt.cagojati.bombahman;

import java.io.IOException;

import org.andengine.engine.handler.timer.ITimerCallback;
import org.andengine.engine.handler.timer.TimerHandler;
import org.andengine.extension.tmx.TMXProperties;
import org.andengine.extension.tmx.TMXTile;
import org.andengine.extension.tmx.TMXTileProperty;

import pt.cagojati.bombahman.multiplayer.IMultiplayerServer;
import pt.cagojati.bombahman.multiplayer.messages.MessageFlags;
import pt.cagojati.bombahman.multiplayer.messages.StartSuddenDeathServerMessage;

import android.util.Log;

public class Clock {
	
	private int mTime;
	private float mDeltaTime = 0.5f;
	private GameActivity mGameActivity;
	
	public Clock(int time, GameActivity mGameActivity)
	{
		mTime = time;
		this.mGameActivity = mGameActivity;
	}

	public int getTime() {
		return mTime;
	}

	public void setTime(int mTime) {
		this.mTime = mTime;
	}

	public void startTimer()
	{
		GameActivity.getScene().registerUpdateHandler(new TimerHandler(mTime-mDeltaTime, new ITimerCallback() {
			
			@Override
			public void onTimePassed(TimerHandler pTimerHandler) {
				IMultiplayerServer server = mGameActivity.getServer();
				StartSuddenDeathServerMessage msg = (StartSuddenDeathServerMessage) server.getMessagePool().obtainMessage(MessageFlags.FLAG_MESSAGE_SERVER_START_SUDDEN_DEATH);
				try {
					server.sendBroadcastServerMessage(msg);
				} catch (IOException e) {
					e.printStackTrace();
				}
				server.getMessagePool().recycleMessage(msg);
//				dropWalls();
			}
		}));
	}
	
	public void dropWalls()
	{	
		GameActivity.getScene().registerUpdateHandler(new TimerHandler(mDeltaTime,true, new ITimerCallback() {
			int x = 1;
			int y = 33;
			@Override
			public void onTimePassed(TimerHandler pTimerHandler) {
				Log.d("oteste", "timer");
				x+=32;
				if(x>GameActivity.CAMERA_WIDTH-32)
				{
					x=32;
					y+=32;
					if(y>GameActivity.CAMERA_HEIGHT-32)
					{
						pTimerHandler.setAutoReset(false);
						return;
					}
				}
				TMXTile tile = GameActivity.getMap().getTMXTileAt(x, y);
				//check if there is a player in the spot
				
				TMXProperties<TMXTileProperty> property = tile.getTMXTileProperties(GameActivity.getMap().getTMXTiledMap());
				if(property!=null && property.containsTMXProperty("wall", "true"))
				{
					return;
				}else{
					if(property!=null && property.containsTMXProperty("brick", "true"))
					{
						Brick brick = (Brick) tile.getUserData();
						brick.explode();
					}
					//replace Tile with Wall
					tile.setGlobalTileID(GameActivity.getMap().getTMXTiledMap(), 2, 0);
					tile.setTextureRegion(GameActivity.getMap().getTMXTiledMap().getTMXTileSets().get(0).getTextureRegionFromGlobalTileID(2));
					Wall wall = new Wall();
					wall.createBody(tile, GameActivity.getPhysicsWorld(), GameActivity.getScene(), GameActivity.getVertexBufferManager());
					for(int k=0; k<GameActivity.getTotalPlayers(); k++){
						TMXTile playerTile = GameActivity.getPlayer(k).getTMXTile();
						if(tile.getTileX() == playerTile.getTileX() && tile.getTileY() == playerTile.getTileY())
						{
							mGameActivity.killPlayer(GameActivity.getPlayer(k));
						}
					}
				}
			}
		}));
	}
}
