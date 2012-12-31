package pt.cagojati.bombahman;

import org.andengine.engine.handler.timer.ITimerCallback;
import org.andengine.engine.handler.timer.TimerHandler;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.sprite.batch.SpriteGroup;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.atlas.bitmap.BuildableBitmapTextureAtlas;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.ITiledTextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

import android.content.Context;

public class Explosion {

	private static final float TIMEOUT = 1f;

	static ITiledTextureRegion mExplosionTextureRegion;

	SpriteGroup mSpriteGroup;
	int mPower;

	public Explosion(int power){
		this.mPower = power;
	}

	public static void loadResources(BuildableBitmapTextureAtlas textureAtlas, Context context){
		mExplosionTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(textureAtlas, context, "explosion.png",3,1);
	}
	
	private void createSprite(float posX, float posY, float angle,ITextureRegion texture, VertexBufferObjectManager vertexBufferManager){
		Sprite spriteConnector = new Sprite(posX, posY, texture, vertexBufferManager);
		spriteConnector.setRotationCenter(16, 16);
		spriteConnector.setRotation(angle);
		this.mSpriteGroup.attachChild(spriteConnector);
	}

	public void createSpriteGroup(float posX, float posY, Scene scene, VertexBufferObjectManager vertexBufferManager){
		this.mSpriteGroup = new SpriteGroup(posX,posY,Explosion.mExplosionTextureRegion.getTexture(), (4*mPower +1), vertexBufferManager);
		//create center
		Sprite spriteCenter = new Sprite(0, 0, mExplosionTextureRegion.getTextureRegion(0), vertexBufferManager);
		this.mSpriteGroup.attachChild(spriteCenter);

		int angle = 0;
		int deltaX = 0;
		int deltaY = 0;
		int currentX =0;
		int currentY = 0;
		//four directions
		for(int i=0;i<4;i++){
			switch(i){
				case 0:
					//up
					deltaX = 0; deltaY = -32;
					break;
				case 1:
					//right
					deltaX = 32; deltaY = 0;
					break;
				case 2:
					//down
					deltaX = 0; deltaY = 32;
					break;
				case 3:
					//left
					deltaX = -32; deltaY = 0;
					break;
			}
			currentX = 0;
			currentY = 0;
			//for all the connectors
			for(int j=0;j<this.mPower-1;j++){
				currentX += deltaX;
				currentY += deltaY;
				createSprite(currentX, currentY, angle,Explosion.mExplosionTextureRegion.getTextureRegion(1), vertexBufferManager);
			}
			//for the tip
			currentX += deltaX;
			currentY += deltaY;
			createSprite(currentX, currentY, angle,Explosion.mExplosionTextureRegion.getTextureRegion(2), vertexBufferManager);
			
			angle +=90;
		}				
		//this.mSprite.attachChild(mBoundBox);
		scene.attachChild(this.mSpriteGroup);
		
		this.mSpriteGroup.registerUpdateHandler(new TimerHandler(TIMEOUT, new ITimerCallback() {
			
			@Override
			public void onTimePassed(TimerHandler pTimerHandler) {
				Explosion.this.mSpriteGroup.unregisterUpdateHandler(pTimerHandler);
			}
		}));
	}

	public int getPower() {
		return mPower;
	}

	public void setPower(int mPower) {
		this.mPower = mPower;
	}

}
