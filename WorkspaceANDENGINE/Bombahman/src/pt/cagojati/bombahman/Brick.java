package pt.cagojati.bombahman;

import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.Scene;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.extension.tmx.TMXTile;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;

public class Brick {
	
	public static final short CATEGORYBIT = 2;
	public static final short MASKBITS = Wall.CATEGORYBIT + Player.CATEGORYBIT + Brick.CATEGORYBIT;
	
	final FixtureDef BRICK_FIXTURE_DEF = PhysicsFactory.createFixtureDef(1, 0, 0, false, Brick.CATEGORYBIT, Brick.MASKBITS, (short)0);

	private Body mBody;
	private IPowerUp mPowerUp;
	
	public Brick(){
		
	}
	
	public void createBody(final TMXTile pTMXTile,PhysicsWorld physicsWorld, Scene scene, VertexBufferObjectManager vertexBufferManager)
	{
		final Rectangle boundBox = new Rectangle(pTMXTile.getTileX(),pTMXTile.getTileY(), pTMXTile.getTileWidth(),pTMXTile.getTileHeight(),vertexBufferManager);
		this.mBody = PhysicsFactory.createBoxBody(physicsWorld, boundBox, BodyType.StaticBody, BRICK_FIXTURE_DEF);
		this.mBody.setUserData(this);
		scene.attachChild(boundBox);
	}

	public void explode() {		
		GameActivity.getScene().postRunnable(new Runnable() {
			
			@Override
			public void run() {
				GameActivity.getPhysicsWorld().destroyBody(Brick.this.mBody);
				if(mPowerUp!=null){
					Vector2 vec = mBody.getTransform().getPosition();
					mPowerUp.show(vec.x*32, vec.y*32);
				}
			}
		});
		
	}

	public void setPowerUp(IPowerUp iPowerUp) {
		mPowerUp = iPowerUp;
	}
	
	
}
