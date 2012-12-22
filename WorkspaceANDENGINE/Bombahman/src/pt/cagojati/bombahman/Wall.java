package pt.cagojati.bombahman;

import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.Scene;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.extension.tmx.TMXTile;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

public class Wall {
	final FixtureDef WALL_FIXTURE_DEF = PhysicsFactory.createFixtureDef(1, 0, 0);//, false, CATEGORY_BIT_CACTUS,MASK_BIT_CACTUS, (short)0);

	private Body mBody;
	
	public Wall(){
		
	}
	
	public void createBody(final TMXTile pTMXTile,PhysicsWorld physicsWorld, Scene scene, VertexBufferObjectManager vertexBufferManager)
	{
		final Rectangle boundBox = new Rectangle(pTMXTile.getTileX(),pTMXTile.getTileY(), pTMXTile.getTileWidth(),pTMXTile.getTileHeight(),vertexBufferManager);
		this.mBody = PhysicsFactory.createBoxBody(physicsWorld, boundBox, BodyType.StaticBody, WALL_FIXTURE_DEF);
		this.mBody.setUserData(this);
		scene.attachChild(boundBox);
	}
}
