package pt.cagojati.bombahman;

import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.sprite.Sprite;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.atlas.bitmap.BuildableBitmapTextureAtlas;
import org.andengine.opengl.texture.region.ITextureRegion;

import android.content.Context;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;

public class BombPowerup implements IPowerUp {
	
	private float mX;
	private float mY;
	static ITextureRegion mBombPowerUpTextureRegion;
	Sprite mSprite;
	Body mBody;

	public static final short CATEGORYBIT = 32;
	private final short MASKBITS = Player.CATEGORYBIT + Explosion.CATEGORYBIT;
	private final FixtureDef BOMBPOWERUP_FIXTURE_DEF = PhysicsFactory.createFixtureDef(1, 0, 0, true);//, BombPowerup.CATEGORYBIT, this.MASKBITS, (short)0);

	public static void loadResources(BuildableBitmapTextureAtlas textureAtlas, Context context){
		mBombPowerUpTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(textureAtlas, context, "bombpowerup.png");
	}
	
	@Override
	public float getX() {
		return mX;
	}

	@Override
	public float getY() {
		return mY;
	}

	@Override
	public void show(float posX, float posY) {
		mX = posX-mBombPowerUpTextureRegion.getWidth()/2;
		mY = posY-mBombPowerUpTextureRegion.getHeight()/2;
		this.mSprite = new Sprite(mX, mY, BombPowerup.mBombPowerUpTextureRegion, GameActivity.getVertexBufferManager());
		//this.setPosition(posX, posY);
		
		final Rectangle mBoundBox = new Rectangle(posX,posY,32,32,GameActivity.getVertexBufferManager());
		
		GameActivity.getScene().postRunnable(new Runnable() {

			@Override
			public void run() {
				mBody = PhysicsFactory.createBoxBody(GameActivity.getPhysicsWorld(), mBoundBox, BodyType.StaticBody, BOMBPOWERUP_FIXTURE_DEF);
				GameActivity.getPhysicsWorld().registerPhysicsConnector(new PhysicsConnector(mBoundBox, mBody, true, false));
				
				mBody.setUserData(BombPowerup.this);

				GameActivity.getScene().attachChild(BombPowerup.this.mSprite);
			}
			
		});
	}

	@Override
	public int getType() {
		return 1;
	}

	public void setX(float x) {
		mX = x;
	}
	
	public void setY(float y) {
		mY = y;
	}

	@Override
	public void apply(Player player) {
		
	}

	@Override
	public void destroy() {
		GameActivity.getScene().postRunnable(new Runnable() {
			
			@Override
			public void run() {
				BombPowerup.this.mSprite.detachSelf();
				GameActivity.getPhysicsWorld().destroyBody(mBody);
			}
		});
	}


}
