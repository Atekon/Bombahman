package pt.cagojati.bombahman;

import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.sprite.AnimatedSprite;
import org.andengine.entity.sprite.AnimatedSprite.IAnimationListener;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.extension.tmx.TMXTile;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.atlas.bitmap.BuildableBitmapTextureAtlas;
import org.andengine.opengl.texture.region.ITiledTextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

import android.content.Context;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;

public class Brick {
	
	public static final short CATEGORYBIT = 2;
	public static final short MASKBITS = Wall.CATEGORYBIT + Player.CATEGORYBIT + Brick.CATEGORYBIT;
	AnimatedSprite mSpriteExplosion;
	static ITiledTextureRegion mBrickExplosionAnimationTextureRegion;

	final FixtureDef BRICK_FIXTURE_DEF = PhysicsFactory.createFixtureDef(1, 0, 0, false, Brick.CATEGORYBIT, Brick.MASKBITS, (short)0);

	private Body mBody;
	private IPowerUp mPowerUp;
	
	public Brick(){
		
	}
	
	public static void loadResources(BuildableBitmapTextureAtlas textureAtlas, Context context){
		mBrickExplosionAnimationTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(textureAtlas, context, "explosionbrick.png",4,1);
	}
	
	public void createBody(final TMXTile pTMXTile,PhysicsWorld physicsWorld, Scene scene, VertexBufferObjectManager vertexBufferManager)
	{
		final Rectangle boundBox = new Rectangle(pTMXTile.getTileX(),pTMXTile.getTileY(), pTMXTile.getTileWidth(),pTMXTile.getTileHeight(),vertexBufferManager);
		this.mBody = PhysicsFactory.createBoxBody(physicsWorld, boundBox, BodyType.StaticBody, BRICK_FIXTURE_DEF);
		this.mBody.setUserData(this);
		scene.attachChild(boundBox);
	}

	public void explode() {		
		Vector2 vec = mBody.getTransform().getPosition();
		
		this.mSpriteExplosion = new AnimatedSprite(vec.x*32-16, vec.y*32-16, mBrickExplosionAnimationTextureRegion, GameActivity.getVertexBufferManager());
		this.mSpriteExplosion.setCurrentTileIndex(0);
		GameActivity.getScene().attachChild(mSpriteExplosion);
		this.mSpriteExplosion.animate(new long[]{200,200,200,200},0,3,false,new IAnimationListener() {
			
			@Override
			public void onAnimationStarted(AnimatedSprite pAnimatedSprite,
					int pInitialLoopCount) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationLoopFinished(AnimatedSprite pAnimatedSprite,
					int pRemainingLoopCount, int pInitialLoopCount) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationFrameChanged(AnimatedSprite pAnimatedSprite,
					int pOldFrameIndex, int pNewFrameIndex) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationFinished(final AnimatedSprite pAnimatedSprite) {
				pAnimatedSprite.stopAnimation();
				GameActivity.getScene().postRunnable(new Runnable() {
					
					@Override
					public void run() {
						pAnimatedSprite.detachSelf();
						GameActivity.getPhysicsWorld().destroyBody(Brick.this.mBody);
						if(mPowerUp!=null){
							Vector2 vec = mBody.getTransform().getPosition();
							mPowerUp.show(vec.x*32, vec.y*32);
						}
					}
				});				
			}
		});
	}

	public void setPowerUp(IPowerUp iPowerUp) {
		mPowerUp = iPowerUp;
	}

	public IPowerUp getPowerUp() {
		return mPowerUp;
	}
	
	
}
