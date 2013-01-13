package pt.cagojati.bombahman;

import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.sprite.Sprite;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.tmx.TMXTile;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.atlas.bitmap.BuildableBitmapTextureAtlas;
import org.andengine.opengl.texture.region.ITextureRegion;

import android.content.Context;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;

public class FirePowerup implements IPowerUp {
	
	private float mX;
	private float mY;
	static ITextureRegion mFirePowerUpTextureRegion;
	Sprite mSprite;
	Body mBody;

	public static final short CATEGORYBIT = 32;
	private final FixtureDef FIREPOWERUP_FIXTURE_DEF = PhysicsFactory.createFixtureDef(1, 0, 0, true);

	public static void loadResources(BuildableBitmapTextureAtlas textureAtlas, Context context){
		mFirePowerUpTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(textureAtlas, context, "firepowerup.png");
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
		mX = posX-mFirePowerUpTextureRegion.getWidth()/2;
		mY = posY-mFirePowerUpTextureRegion.getHeight()/2;
		TMXTile tile = GameActivity.getMap().getTMXTileAt(posX, posY);
		tile.setUserData(this);
		this.mSprite = new Sprite(mX, mY, FirePowerup.mFirePowerUpTextureRegion, GameActivity.getVertexBufferManager());
		//this.setPosition(posX, posY);
		
		final Rectangle mBoundBox = new Rectangle(posX,posY,32,32,GameActivity.getVertexBufferManager());
		
		GameActivity.getScene().postRunnable(new Runnable() {

			@Override
			public void run() {
				mBody = PhysicsFactory.createBoxBody(GameActivity.getPhysicsWorld(), mBoundBox, BodyType.StaticBody, FIREPOWERUP_FIXTURE_DEF);
				mBody.setUserData(FirePowerup.this);
				GameActivity.getPhysicsWorld().registerPhysicsConnector(new PhysicsConnector(mBoundBox, mBody, true, false));
				GameActivity.getScene().attachChild(FirePowerup.this.mSprite);
			}
			
		});
	}

	@Override
	public int getType() {
		return 0;
	}

	public void setX(float x) {
		mX = x;
	}
	
	public void setY(float y) {
		mY = y;
	}

	@Override
	public void apply(Player player) {
		player.setPower(player.getPower()+1);
	}

	@Override
	public void destroy() {
		GameActivity.getScene().postRunnable(new Runnable() {
			
			@Override
			public void run() {
				FirePowerup.this.mSprite.detachSelf();
				GameActivity.getPhysicsWorld().destroyBody(mBody);
			}
		});
	}


}
