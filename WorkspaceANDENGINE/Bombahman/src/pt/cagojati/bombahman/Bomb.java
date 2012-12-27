package pt.cagojati.bombahman;

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

	private static final String[] BOMB_TEXTURES = {"bomb_white.png", "bomb_black.png", "bomb_blue.png","bomb_red.png"};
	public static final FixtureDef BOMB_FIXTURE_DEF = PhysicsFactory.createFixtureDef(1, 0, 0);//, false, CATEGORY_BIT_PLAYER,MASK_BIT_PLAYER, (short)0);
	
	public Bomb(){
		
	}
	
	public static void loadResources(int playerId, BuildableBitmapTextureAtlas textureAtlas, Context context){
		mBombTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(textureAtlas, context, Bomb.BOMB_TEXTURES[playerId]);
	}
	
	public void createSprite(PhysicsWorld physicsWorld,float posX, float posY, Scene scene, VertexBufferObjectManager vertexBufferManager){
		this.mSprite = new Sprite(0, 0, Bomb.mBombTextureRegion, vertexBufferManager);
		this.mSprite.setScale(0.35f);
		this.mSprite.setPosition(0-this.mSprite.getWidth()/2+7, 0-3*this.mSprite.getWidth()/4+7);
		//this.setPosition(posX, posY);
		
//		final Rectangle boundBox = new Rectangle(posX-8,posY-8,16,16,vertexBufferManager);
//		boundBox.setAlpha(0);
//		this.mBody = PhysicsFactory.createBoxBody(physicsWorld, boundBox, BodyType.DynamicBody, Player.PLAYER_FIXTURE_DEF);
//		physicsWorld.registerPhysicsConnector(new PhysicsConnector(boundBox, this.mBody, true, false));
		
//		this.mBody.setUserData(this);
//		boundBox.attachChild(this.mSprite);
//		scene.attachChild(boundBox);
		scene.attachChild(this.mSprite);
	}
	
	public Sprite getSprite() {
		return mSprite;
	}

	public void setSprite(Sprite mSprite) {
		this.mSprite = mSprite;
	}

	public void setPosition(int pX, int pY) {
		this.mSprite.setPosition(pX, pY-12);
	}

}
