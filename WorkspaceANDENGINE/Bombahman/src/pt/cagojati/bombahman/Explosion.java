package pt.cagojati.bombahman;

import java.util.ArrayList;

import org.andengine.engine.handler.timer.ITimerCallback;
import org.andengine.engine.handler.timer.TimerHandler;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.sprite.batch.SpriteGroup;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.tmx.TMXProperties;
import org.andengine.extension.tmx.TMXTile;
import org.andengine.extension.tmx.TMXTileProperty;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.atlas.bitmap.BuildableBitmapTextureAtlas;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.ITiledTextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

import android.content.Context;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;

public class Explosion {

	private static final float TIMEOUT = 1f;

	public static final short CATEGORYBIT = 16;
	private final short MASKBITS = Player.CATEGORYBIT;
	private final FixtureDef EXPLOSION_FIXTURE_DEF = PhysicsFactory.createFixtureDef(1, 0, 0);//, false, Explosion.CATEGORYBIT, this.MASKBITS, (short)0);
	
	static ITiledTextureRegion mExplosionTextureRegion;

	ArrayList<Body> mSensorList;
	SpriteGroup mSpriteGroup;
	int mPower;

	public Explosion(int power){
		this.mPower = power;
		mSensorList = new ArrayList<Body>();
	}

	public static void loadResources(BuildableBitmapTextureAtlas textureAtlas, Context context){
		mExplosionTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(textureAtlas, context, "explosion.png",3,1);
	}
	
	private void createSprite(float posX, float posY, float angle,ITextureRegion texture, VertexBufferObjectManager vertexBufferManager){
		//add Sensor
		Rectangle boundBox = new Rectangle(posX+this.mSpriteGroup.getX(),posY+this.mSpriteGroup.getY(),32,32,vertexBufferManager);
		
		Body body = PhysicsFactory.createBoxBody(GameActivity.getPhysicsWorld(), boundBox, BodyType.StaticBody, EXPLOSION_FIXTURE_DEF);
		body.setUserData(this);
		
		body.getFixtureList().get(0).setSensor(true);
		this.mSensorList.add(body);
		GameActivity.getPhysicsWorld().registerPhysicsConnector(new PhysicsConnector(boundBox, body, true, false));
		
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

				if(j<mPower-1)
					createSprite(currentX, currentY, angle,Explosion.mExplosionTextureRegion.getTextureRegion(1), vertexBufferManager);
				else{
					//for the tip
					createSprite(currentX, currentY, angle,Explosion.mExplosionTextureRegion.getTextureRegion(2), vertexBufferManager);
				}
			}
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
				for (Body element : Explosion.this.mSensorList) {
					GameActivity.getPhysicsWorld().destroyBody(element);
				}
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
