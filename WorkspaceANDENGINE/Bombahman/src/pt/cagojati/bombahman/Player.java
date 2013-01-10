package pt.cagojati.bombahman;

import org.andengine.engine.handler.timer.ITimerCallback;
import org.andengine.engine.handler.timer.TimerHandler;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.sprite.AnimatedSprite;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.extension.tmx.TMXTile;
import org.andengine.opengl.texture.ITexture;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.atlas.bitmap.BuildableBitmapTextureAtlas;
import org.andengine.opengl.texture.region.ITiledTextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.util.Constants;
import org.andengine.util.debug.Debug;
import org.andengine.util.debug.Debug.DebugLevel;

import pt.cagojati.bombahman.multiplayer.messages.AddBombClientMessage;

import android.content.Context;
import android.util.Log;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.FixtureDef;

public class Player {

	Body mBody;
	ITiledTextureRegion mPlayerTextureRegion;
	AnimatedSprite mSprite;
	Rectangle mDeadBoundBox;
	private boolean isOverBomb = false;
	private int mPower=1;
	private int mId;

	public static final short CATEGORYBIT = 4;
	private short MASKBITS = Wall.CATEGORYBIT + Brick.CATEGORYBIT + Player.CATEGORYBIT + Bomb.CATEGORYBIT;
	private final FixtureDef PLAYER_FIXTURE_DEF = PhysicsFactory.createFixtureDef(1, 0, 0, false, Player.CATEGORYBIT,this.MASKBITS, (short)0);
	private final FixtureDef DEAD_RECKONING_PLAYER_FIXTURE_DEF = PhysicsFactory.createFixtureDef(1, 0, 0, true, Player.CATEGORYBIT,this.MASKBITS, (short)0);


	public Player(int id){
		this.mId = id;
	}

	public void loadResources(BuildableBitmapTextureAtlas textureAtlas, Context context){
		String texture = "";
		switch(mId){
			case 0:
				texture = "playerwhite.png";
				break;
			case 1:
				texture = "playergrey.png";
				break;	
			case 2:
				texture = "playerblue.png";
				break;
			case 3:
				texture = "playerred.png";
				break;
		}
		this.mPlayerTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(textureAtlas, context, texture,3,4);
	}

	public ITexture[] loadResources(BitmapTextureAtlas textureAtlas,int offsetX, int offsetY, Context context)
	{
		ITexture[] vec = new ITexture[1];
		this.mPlayerTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(textureAtlas, context, "playerwhite.png", offsetX, offsetY, 3, 4);
		vec[0] = this.mPlayerTextureRegion.getTexture();
		return vec;
	}

	public void initialize(float posX, float posY, Scene scene, VertexBufferObjectManager vertexBufferManager){
		Debug.log(DebugLevel.ERROR, "PosX: "+posX+"\nPosY: "+posY);
		this.mSprite = new AnimatedSprite(0, 0, this.mPlayerTextureRegion, vertexBufferManager);
		this.mSprite.setCurrentTileIndex(7);
		this.mSprite.setScale(0.35f);
		this.mSprite.setPosition(0-this.mSprite.getWidth()/2+7, 0-3*this.mSprite.getWidth()/4+7);
		//this.setPosition(posX, posY);

		final Rectangle boundBox = new Rectangle(posX-8,posY-8,16,16,vertexBufferManager);
		boundBox.setColor(1, 0, 0, 0);
		//boundBox.setAlpha(0);
		this.mBody = PhysicsFactory.createBoxBody(GameActivity.getPhysicsWorld(), boundBox, BodyType.DynamicBody, this.PLAYER_FIXTURE_DEF);
		GameActivity.getPhysicsWorld().registerPhysicsConnector(new PhysicsConnector(boundBox, this.mBody, true, false));

		this.mDeadBoundBox = new Rectangle(posX-8,posY-8,16,16,vertexBufferManager);
		this.mDeadBoundBox.setColor(1, 0, 0, 0);
		//boundBox.setAlpha(0);
		//this.deadbody = PhysicsFactory.createBoxBody(GameActivity.getPhysicsWorld(), deadboundBox, BodyType.DynamicBody, this.DEAD_RECKONING_PLAYER_FIXTURE_DEF);
		//GameActivity.getPhysicsWorld().registerPhysicsConnector(new PhysicsConnector(deadboundBox, this.deadbody, true, false));
		
		this.mBody.setUserData(this);
		boundBox.attachChild(this.mSprite);
		scene.attachChild(boundBox);
		
		//deadbody.setUserData(this.mId);
		//deadboundBox.attachChild(this.mSprite);
		scene.attachChild(this.mDeadBoundBox);
		
	}

	public AnimatedSprite getSprite() {
		return mSprite;
	}
	
	public Rectangle getDeadBoundBox(){
		return this.mDeadBoundBox;
	}

	public void setSprite(AnimatedSprite mSprite) {
		this.mSprite = mSprite;
	}
	
	public float getPosX(){
		//return this.mSprite.getX();
		return this.getBody().getTransform().getPosition().x*32;
	}
	
	public float getPosY(){
		//return this.mSprite.getY();
		return this.getBody().getTransform().getPosition().y*32;
	}
	
	public void setPos(final float x, final float y){
		//return this.mSprite.getX();
		GameActivity.getScene().postRunnable(new Runnable() {
			
			@Override
			public void run() {
				Player.this.getBody().setTransform(x/32, y/32, 0);
			}
		});
	}
	
	public Body getBody()
	{
		return this.mBody;
	}

	public boolean isOverBomb() {
		return isOverBomb;
	}
	
	public int getId()
	{
		return this.mId;
	}

	public void setOverBomb(boolean isOverBomb) {
		this.isOverBomb = isOverBomb;
	}

	public int getPower(){
		return this.mPower;
	}

	public void setPower(int power){
		this.mPower = power;
	}

	public void animate(float pValueX, float pValueY){
		if(this.mBody!=null){
			this.mBody.setLinearVelocity(pValueX*3, pValueY*3);

			if(Math.abs(pValueX)>Math.abs(pValueY)){
				if(pValueX>0){
					if(!this.mSprite.isAnimationRunning() || (this.mSprite.isAnimationRunning() && (this.mSprite.getCurrentTileIndex()<3 || this.mSprite.getCurrentTileIndex()>5)))
						this.mSprite.animate(new long[]{200, 200, 200, 200},new int[]{3,4,5,4}, true);	
				}else{
					if(!this.mSprite.isAnimationRunning() || (this.mSprite.isAnimationRunning() && (this.mSprite.getCurrentTileIndex()<9 || this.mSprite.getCurrentTileIndex()>11)))
						this.mSprite.animate(new long[]{200, 200, 200, 200}, new int[]{9,10,11,10}, true);
				}

			}else if(Math.abs(pValueY)>Math.abs(pValueX)){
				if(pValueY>0){
					if(!this.mSprite.isAnimationRunning() || (this.mSprite.isAnimationRunning() && (this.mSprite.getCurrentTileIndex()<6 || this.mSprite.getCurrentTileIndex()>8)))
						this.mSprite.animate(new long[]{200, 200, 200, 200}, new int[]{6,7,8,7}, true);	
				}else{
					if(!this.mSprite.isAnimationRunning() || (this.mSprite.isAnimationRunning() && (this.mSprite.getCurrentTileIndex()>2)))
						this.mSprite.animate(new long[]{200, 200, 200, 200},new int[]{0,1,2,1}, true);
				}
			}

			if(pValueX ==0 && pValueY==0){
				this.mSprite.stopAnimation();
			}
		}
	}

	public void dropBomb() {
		//get Current Tile Position
		final float[] playerFootCordinates = this.getSprite().convertLocalToSceneCoordinates(this.getSprite().getWidthScaled()/2+32, this.getSprite().getHeightScaled()+50);

		/* Get the tile the feet of the player are currently waking on. */
		final TMXTile tmxTile = GameActivity.getMap().getTMXTileAt(playerFootCordinates[Constants.VERTEX_INDEX_X], playerFootCordinates[Constants.VERTEX_INDEX_Y]);
		if(tmxTile != null) {
//			Bomb bomb = GameActivity.getBombPool().obtainPoolItem();
//			bomb.setPlayer(this);
			int posX = tmxTile.getTileX();
			int posY = tmxTile.getTileY();
//			bomb.definePosition(posX,posY);
			Bomb bomb = dropBomb(posX, posY);
			AddBombClientMessage message = new AddBombClientMessage(posX, posY, this.mId, bomb.getId());
			GameActivity.getConnector().sendClientMessage(message);
		}
	}
	
	public void dropBomb(int x, int y, String bombId) {
		Bomb bomb = dropBomb(x, y);
		GameActivity.getBombPool().replaceBomb(bomb.getId(), bombId);
		bomb.setId(bombId);
	}
	
	public Bomb dropBomb(int posX, int posY){
		Bomb bomb = GameActivity.getBombPool().obtainPoolItem();
		bomb.setPlayer(this);
		bomb.definePosition(posX,posY);
		return bomb;
	}

	/**
	 * must be run inside updatethread
	 */
	public void kill() {
		// TODO Animation
		this.mSprite.detachSelf();
		GameActivity.getPhysicsWorld().destroyBody(mBody);

	}


}
