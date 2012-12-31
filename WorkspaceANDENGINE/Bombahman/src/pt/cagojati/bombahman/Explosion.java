package pt.cagojati.bombahman;

import org.andengine.engine.handler.timer.ITimerCallback;
import org.andengine.engine.handler.timer.TimerHandler;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.sprite.batch.SpriteGroup;
import org.andengine.extension.tmx.TMXProperties;
import org.andengine.extension.tmx.TMXProperty;
import org.andengine.extension.tmx.TMXTile;
import org.andengine.extension.tmx.TMXTileProperty;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.atlas.bitmap.BuildableBitmapTextureAtlas;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.ITiledTextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.badlogic.gdx.physics.box2d.RayCastCallback;

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
		directionloop:
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
			for(int j=0;j<this.mPower;j++){
				currentX += deltaX;
				currentY += deltaY;
				//checks if there is a wall or brick in the way
				TMXTile tile = GameActivity.getMap().getTMXTileAt(currentX+this.mSpriteGroup.getX(), currentY+this.mSpriteGroup.getY());
				TMXProperties<TMXTileProperty> property = tile.getTMXTileProperties(GameActivity.getMap().getTMXTiledMap());
				if(property!=null){
					if(property.containsTMXProperty("wall", "true"))
					{
						angle+=90;
						continue directionloop;
					}
					if(property.containsTMXProperty("brick", "true")){
						tile.setGlobalTileID(GameActivity.getMap().getTMXTiledMap(), 2, 0);
						tile.setTextureRegion(GameActivity.getMap().getTMXTiledMap().getTMXTileSets().get(0).getTextureRegionFromGlobalTileID(2));
						Brick brick = (Brick) tile.getUserData();
						brick.explode();
						
						angle+=90;
						continue directionloop;
					}
				}
				createSprite(currentX, currentY, angle,Explosion.mExplosionTextureRegion.getTextureRegion(1), vertexBufferManager);
			}
			//for the tip
			createSprite(currentX, currentY, angle,Explosion.mExplosionTextureRegion.getTextureRegion(2), vertexBufferManager);
			
			angle +=90;
		}				
		//this.mSprite.attachChild(mBoundBox);
		scene.attachChild(this.mSpriteGroup);
		
		this.mSpriteGroup.registerUpdateHandler(new TimerHandler(TIMEOUT, new ITimerCallback() {
			
			@Override
			public void onTimePassed(TimerHandler pTimerHandler) {
				Explosion.this.mSpriteGroup.unregisterUpdateHandler(pTimerHandler);
				Explosion.this.mSpriteGroup.detachSelf();
				Explosion.this.mSpriteGroup.dispose();
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
