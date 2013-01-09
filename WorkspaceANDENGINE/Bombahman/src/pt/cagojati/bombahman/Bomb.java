package pt.cagojati.bombahman;

import java.util.UUID;

import org.andengine.engine.handler.timer.ITimerCallback;
import org.andengine.engine.handler.timer.TimerHandler;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.sprite.AnimatedSprite;
import org.andengine.entity.sprite.Sprite;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.atlas.bitmap.BuildableBitmapTextureAtlas;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

import android.content.Context;

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
	private final short MASKBITS = Wall.CATEGORYBIT + Brick.CATEGORYBIT + Player.CATEGORYBIT + Bomb.CATEGORYBIT;
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

	public void definePosition(int pX, int pY) {
		this.mSprite.setPosition(pX, pY-12);
		
		mBoundBox.setPosition(pX, pY);
		//mBoundBox.setColor(1, 1, 0);
		
		//boundBox.setAlpha(0);
		this.mBody = PhysicsFactory.createBoxBody(GameActivity.getPhysicsWorld(), this.mBoundBox, BodyType.StaticBody, BOMB_FIXTURE_DEF);
		GameActivity.getPhysicsWorld().registerPhysicsConnector(new PhysicsConnector(this.mBoundBox, this.mBody, true, false));
		
		this.mBody.setUserData(this);
		
		this.mBody.getFixtureList().get(0).setSensor(true);
		
		mTimerHandler = new TimerHandler(TIMEOUT, new ITimerCallback() {
			
			@Override
			public void onTimePassed(TimerHandler pTimerHandler) {
//				Bomb.this.explode();
//				Bomb.this.mSprite.unregisterUpdateHandler(pTimerHandler);
			}
		});
		this.mSprite.registerUpdateHandler(mTimerHandler);
	}
	
	public void explode(){
		GameActivity.getPhysicsWorld().destroyBody(mBody);
		GameActivity.getBombPool().recyclePoolItem(this);
		
		//create Explosion
		Explosion explosion = new Explosion(this.mPlayer.getPower());
		explosion.createSpriteGroup(this.mSprite.getX(), this.mSprite.getY()+12, (Scene)this.mSprite.getParent(), this.mSprite.getVertexBufferObjectManager());
	}
	
	public boolean unregisterTimerHandler()
	{
		return this.mSprite.unregisterUpdateHandler(mTimerHandler);
	}

	public void generateId() {
		this.mId = UUID.randomUUID().toString();
	}

}
