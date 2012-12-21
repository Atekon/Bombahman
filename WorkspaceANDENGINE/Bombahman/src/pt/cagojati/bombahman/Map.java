package pt.cagojati.bombahman;

import org.andengine.engine.Engine;
import org.andengine.entity.scene.Scene;
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
public class Map{

	// ===========================================================
	// Fields
	// ===========================================================

	private TMXTiledMap mTMXTiledMap;
	protected int mCactusCount;
	
	public Map()
	{
		
	}
	
	public void loadMap(final Scene scene, final Engine engine, final AssetManager assetManager, final VertexBufferObjectManager vertexBufferManager){
		try {
			final TMXLoader tmxLoader = new TMXLoader(assetManager, engine.getTextureManager(), TextureOptions.BILINEAR_PREMULTIPLYALPHA, vertexBufferManager, new ITMXTilePropertiesListener() {
				@Override
				public void onTMXTileWithPropertiesCreated(final TMXTiledMap pTMXTiledMap, final TMXLayer pTMXLayer, final TMXTile pTMXTile, final TMXProperties<TMXTileProperty> pTMXTileProperties) {
					/* We are going to count the tiles that have the property "cactus=true" set. */
//					if(pTMXTileProperties.containsTMXProperty("cactus", "true")) {
//						Map.this.mCactusCount++;
//						//create cactus
//					}
				}
			});
			this.mTMXTiledMap = tmxLoader.loadFromAsset("tmx/map3.tmx");

		} catch (final TMXLoadException e) {
			Debug.e(e);
		}

		final TMXLayer tmxLayer = this.mTMXTiledMap.getTMXLayers().get(0);
		
		scene.attachChild(tmxLayer);
	}

}
