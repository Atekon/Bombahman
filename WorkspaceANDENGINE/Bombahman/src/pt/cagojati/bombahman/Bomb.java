package pt.cagojati.bombahman;

import java.io.IOException;
import java.util.UUID;

import org.andengine.engine.handler.timer.ITimerCallback;
import org.andengine.engine.handler.timer.TimerHandler;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.sprite.Sprite;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.tmx.TMXTile;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.atlas.bitmap.BuildableBitmapTextureAtlas;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

import pt.cagojati.bombahman.multiplayer.IMultiplayerServer;
import pt.cagojati.bombahman.multiplayer.WiFiServer;
import pt.cagojati.bombahman.multiplayer.messages.ExplodeBombServerMessage;
import pt.cagojati.bombahman.multiplayer.messages.MessageFlags;

import android.content.Context;
import android.util.Log;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;

public class Bomb {
	
	Body mBody;
	static ITextureRegion mBombTextureRegion;
	Sprite mSprite;
	Rectangle mBoundBox;
	Player mPlayer;
	private String mId;

	private static final String[] BOMB_TEXTURES = {"bomb_white.png", "bomb_black.png", "bomb_blue.png","bomb_red.png"};
	
	private static final short TIMEOUT = 3;
	public static final short CATEGORYBIT = 8;
	private final short MASKBITS = Wall.CATEGORYBIT + Brick.CATEGORYBIT + Player.CATEGORYBIT + Explosion.CATEGORYBIT + Bomb.CATEGORYBIT;
	private final FixtureDef BOMB_FIXTURE_DEF = PhysicsFactory.createFixtureDef(1, 0, 0, false, Bomb.CATEGORYBIT, this.MASKBITS, (short)0);
	private TimerHandler mTimerHandler;
	
	public Bomb(){
		generateId();
	}
	
	public static void loadResources(int playerId, BuildableBitmapTextureAtlas textureAtlas, Context context){
		mBombTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(textureAtlas, context, Bomb.BOMB_TEXTURES[playerId]);
	}
	
	public void createSprite(float posX, float posY, Scene scene, VertexBufferObjectManager vertexBufferManager){
		this.mSprite = new Sprite(0, 0, Bomb.mBombTextureRegion, vertexBufferManager);
		this.mSprite.setScale(0.35f);
		this.mSprite.setPosition(0-this.mSprite.getWidth()/2+7, 0-3*this.mSprite.getWidth()/4+7);
		//this.setPosition(posX, posY);
		
		mBoundBox = new Rectangle(posX,posY,32,32,vertexBufferManager);
		
		//this.mSprite.attachChild(mBoundBox);
		scene.attachChild(this.mSprite);
	}
	
	public Sprite getSprite() {
		return mSprite;
	}

	public void setSprite(Sprite mSprite) {
		this.mSprite = mSprite;
	}
	
	public Player getPlayer(){
		return mPlayer;
	}
	
	public void setPlayer(Player player){
		this.mPlayer = player;
	}
	
	public String getId() {
		return mId;
	}

	public void setId(String mId) {
		this.mId = mId;
	}
	
	public float getX()
	{
		return this.mBody.getTransform().getPosition().x*32f;
	}
	
	public float getY()
	{
		return this.mBody.getTransform().getPosition().y*32f;
	}

	public void definePosition(final int pX, final int pY) {
		GameActivity.getScene().postRunnable(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				Bomb.this.mSprite.setPosition(pX, pY-12);
				
				mBoundBox.setPosition(pX, pY);
				//mBoundBox.setColor(1, 1, 0);
				
				//boundBox.setAlpha(0);
				Bomb.this.mBody = PhysicsFactory.createBoxBody(GameActivity.getPhysicsWorld(), Bomb.this.mBoundBox, BodyType.StaticBody, BOMB_FIXTURE_DEF);
				GameActivity.getPhysicsWorld().registerPhysicsConnector(new PhysicsConnector(Bomb.this.mBoundBox, Bomb.this.mBody, true, false));
				
				Bomb.this.mBody.setUserData(Bomb.this);
				
				Bomb.this.mBody.getFixtureList().get(0).setSensor(true);
				
				mTimerHandler = new TimerHandler(TIMEOUT, new ITimerCallback() {
					
					@Override
					public void onTimePassed(TimerHandler pTimerHandler) {
						Bomb.this.explode();
						Bomb.this.mSprite.unregisterUpdateHandler(pTimerHandler);
					}
				});
				Bomb.this.mSprite.registerUpdateHandler(mTimerHandler);
			}
		});
	}
	
	public void explode(){
		//check if server
		IMultiplayerServer server = GameActivity.getServer();
		if(server!=null)
		{
			final ExplodeBombServerMessage explodeBombServerMessage = (ExplodeBombServerMessage) server.getMessagePool().obtainMessage(MessageFlags.FLAG_MESSAGE_SERVER_EXPLODE_BOMB);
			explodeBombServerMessage.set(mId);
			try {
				server.sendBroadcastServerMessage(explodeBombServerMessage);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			server.getMessagePool().recycleMessage(explodeBombServerMessage);
		}
		
		TMXTile tile = GameActivity.getMap().getTMXTileAt(getX(), getY());
		tile.setUserData(null);
		GameActivity.getScene().postRunnable(new Runnable() {
			
			@Override
			public void run() {
				if(GameActivity.getBombPool().getBomb(Bomb.this.getId())!= null){
					mPlayer.setNumberOfBombs(mPlayer.getNumberOfBombs()-1);
					GameActivity.getBombPool().recyclePoolItem(Bomb.this);
					GameActivity.getPhysicsWorld().destroyBody(mBody);
					
					//create Explosion
					Explosion explosion = new Explosion(Bomb.this.mPlayer.getPower());
					explosion.createSpriteGroup(Bomb.this.mSprite.getX(), Bomb.this.mSprite.getY()+12, (Scene)Bomb.this.mSprite.getParent(), Bomb.this.mSprite.getVertexBufferObjectManager());
				}
			}
		});
	}
	
	public boolean unregisterTimerHandler()
	{
		return this.mSprite.unregisterUpdateHandler(mTimerHandler);
	}

	public void generateId() {
		this.mId = UUID.randomUUID().toString();
	}

}
