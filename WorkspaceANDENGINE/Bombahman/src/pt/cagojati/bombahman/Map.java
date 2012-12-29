package pt.cagojati.bombahman;

import org.andengine.engine.Engine;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.Scene;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.extension.tmx.TMXLayer;
import org.andengine.extension.tmx.TMXLoader;
import org.andengine.extension.tmx.TMXLoader.ITMXTilePropertiesListener;
import org.andengine.extension.tmx.TMXProperties;
import org.andengine.extension.tmx.TMXTile;
import org.andengine.extension.tmx.TMXTileProperty;
import org.andengine.extension.tmx.TMXTiledMap;
import org.andengine.extension.tmx.util.exception.TMXLoadException;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.util.debug.Debug;

import android.content.res.AssetManager;
import android.util.Log;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;
public class Map{

	// ===========================================================
	// Fields
	// ===========================================================

	private TMXTiledMap mTMXTiledMap;
	
	public Map()
	{
		
	}
	
	public void loadMap(final Scene scene, final Engine engine, final AssetManager assetManager, final VertexBufferObjectManager vertexBufferManager){
		try {
			final TMXLoader tmxLoader = new TMXLoader(assetManager, engine.getTextureManager(), TextureOptions.BILINEAR_PREMULTIPLYALPHA, vertexBufferManager, new ITMXTilePropertiesListener() {
				@Override
				public void onTMXTileWithPropertiesCreated(final TMXTiledMap pTMXTiledMap, final TMXLayer pTMXLayer, final TMXTile pTMXTile, final TMXProperties<TMXTileProperty> pTMXTileProperties) {
					if(pTMXTileProperties.containsTMXProperty("brick", "true")) {
						Brick brick = new Brick();
						brick.createBody(pTMXTile, GameActivity.getPhysicsWorld(), scene, vertexBufferManager);
					}else if(pTMXTileProperties.containsTMXProperty("wall","true")){
						Wall wall = new Wall();
						wall.createBody(pTMXTile, GameActivity.getPhysicsWorld(), scene, vertexBufferManager);
					}
				}
			});
			this.mTMXTiledMap = tmxLoader.loadFromAsset("tmx/map.tmx");

		} catch (final TMXLoadException e) {
			Debug.e(e);
		}

		final TMXLayer tmxLayer = this.mTMXTiledMap.getTMXLayers().get(0);
		
		scene.attachChild(tmxLayer);
	}
	
	public int getTileWidth(){
		return this.mTMXTiledMap.getTileWidth();
	}
	
	public int getTileHeight(){
		return this.mTMXTiledMap.getTileHeight();
	}

	public TMXTile getTMXTileAt(float pX, float pY) {
		return this.mTMXTiledMap.getTMXLayers().get(0).getTMXTileAt(pX, pY);
	}

}
