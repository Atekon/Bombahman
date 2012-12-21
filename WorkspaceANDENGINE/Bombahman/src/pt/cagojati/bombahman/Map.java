package pt.cagojati.bombahman;

import org.andengine.engine.Engine;
import org.andengine.engine.camera.BoundCamera;
import org.andengine.engine.camera.hud.controls.AnalogOnScreenControl;
import org.andengine.engine.camera.hud.controls.BaseOnScreenControl;
import org.andengine.engine.camera.hud.controls.AnalogOnScreenControl.IAnalogOnScreenControlListener;
import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.engine.handler.physics.PhysicsHandler;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.IEntity;
import org.andengine.entity.modifier.LoopEntityModifier;
import org.andengine.entity.modifier.PathModifier;
import org.andengine.entity.modifier.ScaleModifier;
import org.andengine.entity.modifier.SequenceEntityModifier;
import org.andengine.entity.modifier.PathModifier.IPathModifierListener;
import org.andengine.entity.modifier.PathModifier.Path;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.sprite.AnimatedSprite;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.util.FPSLogger;
import org.andengine.extension.physics.box2d.PhysicsConnector;
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
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.bitmap.BitmapTextureFormat;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.TiledTextureRegion;
import org.andengine.ui.activity.SimpleBaseGameActivity;
import org.andengine.util.Constants;
import org.andengine.util.debug.Debug;
import org.andengine.ui.activity.SimpleBaseGameActivity;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;

import android.opengl.GLES20;
import android.util.Log;
import android.widget.Toast;
public class Map{
	
	//private BitmapTextureAtlas mOnScreenControlTexture;
	//private ITextureRegion mOnScreenControlBaseTextureRegion;
	//private ITextureRegion mOnScreenControlKnobTextureRegion;
	//private PhysicsWorld mPhysicsWorld;
	
	public static final short CATEGORY_BIT_PLAYER = 1;
	public static final short CATEGORY_BIT_CACTUS = 2;
	
	public static final short MASK_BIT_PLAYER = CATEGORY_BIT_CACTUS + CATEGORY_BIT_PLAYER;
	public static final short MASK_BIT_CACTUS = CATEGORY_BIT_CACTUS + CATEGORY_BIT_PLAYER;
	
	public static final FixtureDef PLAYER_FIXTURE_DEF = PhysicsFactory.createFixtureDef(1, 0, 0);//, false, CATEGORY_BIT_PLAYER,MASK_BIT_PLAYER, (short)0);
	public static final FixtureDef CACTUS_FIXTURE_DEF = PhysicsFactory.createFixtureDef(1, 0, 0);//, false, CATEGORY_BIT_CACTUS,MASK_BIT_CACTUS, (short)0);

	// ===========================================================
	// Fields
	// ===========================================================

//	private BitmapTextureAtlas mOnScreenControlTexture;
//	private ITextureRegion mOnScreenControlBaseTextureRegion;
//	private ITextureRegion mOnScreenControlKnobTextureRegion;
//
//	private BoundCamera mBoundChaseCamera;
//
//	private BitmapTextureAtlas mBitmapTextureAtlas;
//	private TiledTextureRegion mPlayerTextureRegion;
	private TMXTiledMap mTMXTiledMap;
	protected int mCactusCount;
	
	private PhysicsWorld mPhysicsWorld;

	
	public void loadMap(final Scene scene, final Engine mEngine){
//		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
//		this.mOnScreenControlTexture = new BitmapTextureAtlas(this.getTextureManager(), 256, 128, TextureOptions.DEFAULT);
//		this.mOnScreenControlBaseTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mOnScreenControlTexture, this, "onscreen_control_base.png", 0, 0);
//		this.mOnScreenControlKnobTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mOnScreenControlTexture, this, "onscreen_control_knob.png", 128, 0);
//		this.mOnScreenControlTexture.load();	
		
		this.mEngine.registerUpdateHandler(new FPSLogger());

		//final Scene scene = new Scene();
		//final Scene scene = this.mEngine.getScene();
		this.mPhysicsWorld = new PhysicsWorld(new Vector2(0,0), false);

		try {
			final TMXLoader tmxLoader = new TMXLoader(this.getAssets(), this.mEngine.getTextureManager(), TextureOptions.BILINEAR_PREMULTIPLYALPHA, this.getVertexBufferObjectManager(), new ITMXTilePropertiesListener() {
				@Override
				public void onTMXTileWithPropertiesCreated(final TMXTiledMap pTMXTiledMap, final TMXLayer pTMXLayer, final TMXTile pTMXTile, final TMXProperties<TMXTileProperty> pTMXTileProperties) {
					/* We are going to count the tiles that have the property "cactus=true" set. */
					if(pTMXTileProperties.containsTMXProperty("cactus", "true")) {
						Map.this.mCactusCount++;
						final Rectangle boundBox = new Rectangle(pTMXTile.getTileX(),pTMXTile.getTileY(), pTMXTile.getTileWidth(),pTMXTile.getTileHeight(),Map.this.getVertexBufferObjectManager());
						Body body = PhysicsFactory.createBoxBody(mPhysicsWorld, boundBox, BodyType.StaticBody, CACTUS_FIXTURE_DEF);
						body.setUserData("cactus");
						scene.attachChild(boundBox);
					}
				}
			});
			this.mTMXTiledMap = tmxLoader.loadFromAsset("tmx/desert2.tmx");

//			this.runOnUiThread(new Runnable() {
//				@Override
//				public void run() {
//					Toast.makeText(Map.this, "Cactus count in this TMXTiledMap: " + TMXTiledMapExample.this.mCactusCount, Toast.LENGTH_LONG).show();
//				}
//			});
		} catch (final TMXLoadException e) {
			Debug.e(e);
		}

		final TMXLayer tmxLayer = this.mTMXTiledMap.getTMXLayers().get(0);
		
		scene.attachChild(tmxLayer);
	}

}
