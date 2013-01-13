package pt.cagojati.bombahman;

import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.sprite.AnimatedSprite;
import org.andengine.entity.sprite.AnimatedSprite.IAnimationListener;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
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

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;

public class Player {

	Body mBody;
	ITiledTextureRegion mPlayerTextureRegion;
	ITiledTextureRegion mDeathAnimationTextureRegion;
	AnimatedSprite mSprite;
	AnimatedSprite mSpriteDeath;
	Rectangle mDeadBoundBox;
	private boolean isOverBomb = false;
	private int mPower = 1;
	private int mMaxBombs = 1;
	private int numberOfBombs = 0;
	private int mId;

	public static final short CATEGORYBIT = 4;
	private short MASKBITS = Wall.CATEGORYBIT + Brick.CATEGORYBIT + Player.CATEGORYBIT + Bomb.CATEGORYBIT;
	private final FixtureDef PLAYER_FIXTURE_DEF = PhysicsFactory.createFixtureDef(1, 0, 0, false, Player.CATEGORYBIT,this.MASKBITS, (short)0);
	private static final String[] PLAYER_TEXTURES = {"playerwhite.png", "playergrey.png", "playerblue.png", "playerred.png"};
	private static final String[] DEATH_TEXTURES = {"deadwhite.png", "deadgrey.png", "deadblue.png", "deadred.png"};
	
	public Player(int id){
		this.mId = id;
	}

	public void loadResources(BuildableBitmapTextureAtlas textureAtlas, Context context){
		String texture = PLAYER_TEXTURES[mId];
		this.mPlayerTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(textureAtlas, context, texture,3,4);
		this.mDeathAnimationTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(textureAtlas, context, DEATH_TEXTURES[mId],3,2);
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
		
		this.mSpriteDeath = new AnimatedSprite(0, 0, this.mDeathAnimationTextureRegion, vertexBufferManager);
		this.mSpriteDeath.setCurrentTileIndex(0);
		this.mSpriteDeath.setScale(0.35f);

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
		mSpriteDeath.setVisible(false);
		scene.attachChild(this.mSpriteDeath);
		
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
	
	public float getVelX(){
		//return this.mSprite.getX();
		return this.getBody().getLinearVelocity().x;
	}
	
	public float getVelY(){
		//return this.mSprite.getY();
		return this.getBody().getLinearVelocity().y;
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
	
	public void move(final float pValueX, final float pValueY){
		GameActivity.getScene().postRunnable(new Runnable() {
			
			@Override
			public void run() {
				Player.this.mBody.setLinearVelocity(pValueX, pValueY);
			}
		});
	}

	public void animate(float pValueX, float pValueY){
		if(this.mBody!=null){

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
	
	public TMXTile getTMXTile()
	{
		final float[] playerFootCordinates = this.getSprite().convertLocalToSceneCoordinates(this.getSprite().getWidthScaled()/2+32, this.getSprite().getHeightScaled()+50);

		return GameActivity.getMap().getTMXTileAt(playerFootCordinates[Constants.VERTEX_INDEX_X], playerFootCordinates[Constants.VERTEX_INDEX_Y]); 
	}

	public void dropBomb() {
		/* Get the tile the feet of the player are currently waking on. */
		final TMXTile tmxTile = this.getTMXTile();
		if(tmxTile != null) {
//			Bomb bomb = GameActivity.getBombPool().obtainPoolItem();
//			bomb.setPlayer(this);
			int posX = tmxTile.getTileX();
			int posY = tmxTile.getTileY();
//			bomb.definePosition(posX,posY);
			this.numberOfBombs++;
			Bomb bomb = dropBomb(posX, posY);
			tmxTile.setUserData(bomb);
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
		this.mSpriteDeath.setPosition(this.getPosX()-52, this.getPosY()-80);
		this.mSprite.detachSelf();
		this.mSpriteDeath.setVisible(true);
		this.mSpriteDeath.animate(new long[]{300,300,300,300,300},0,4,false, new IAnimationListener() {
			
			@Override
			public void onAnimationStarted(AnimatedSprite pAnimatedSprite,
					int pInitialLoopCount) {
				
			}
			
			@Override
			public void onAnimationLoopFinished(final AnimatedSprite pAnimatedSprite,
					int pRemainingLoopCount, int pInitialLoopCount) {
				Player.this.mSpriteDeath.stopAnimation();
				GameActivity.getScene().postRunnable(new Runnable() {
					
					@Override
					public void run() {
						pAnimatedSprite.detachSelf();
					}
				});
			}
			
			@Override
			public void onAnimationFrameChanged(AnimatedSprite pAnimatedSprite,
					int pOldFrameIndex, int pNewFrameIndex) {
				
			}
			
			@Override
			public void onAnimationFinished(final AnimatedSprite pAnimatedSprite) {
				Player.this.mSpriteDeath.stopAnimation();
				GameActivity.getScene().postRunnable(new Runnable() {
					
					@Override
					public void run() {
						pAnimatedSprite.detachSelf();
					}
				});
			}
		});
		GameActivity.getPhysicsWorld().destroyBody(mBody);

	}

	public int getNumberOfBombs() {
		return numberOfBombs;
	}

	public void setNumberOfBombs(int numberOfBombs) {
		this.numberOfBombs = numberOfBombs;
	}

	public int getMaxBombs() {
		return mMaxBombs;
	}

	public void setMaxBombs(int mMaxBombs) {
		this.mMaxBombs = mMaxBombs;
	}


}
