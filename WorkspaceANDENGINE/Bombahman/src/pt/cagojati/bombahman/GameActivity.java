package pt.cagojati.bombahman;

import java.io.IOException;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.extension.multiplayer.protocol.adt.message.IMessage;
import org.andengine.extension.multiplayer.protocol.util.MessagePool;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.atlas.bitmap.BuildableBitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.source.IBitmapTextureAtlasSource;
import org.andengine.opengl.texture.atlas.buildable.builder.BlackPawnTextureAtlasBuilder;
import org.andengine.opengl.texture.atlas.buildable.builder.ITextureAtlasBuilder.TextureAtlasBuilderException;
import org.andengine.ui.activity.SimpleBaseGameActivity;
import org.andengine.util.debug.Debug;

import pt.cagojati.bombahman.multiplayer.IMultiplayerConnector;
import pt.cagojati.bombahman.multiplayer.WiFiConnector;
import pt.cagojati.bombahman.multiplayer.WiFiServer;
import pt.cagojati.bombahman.multiplayer.messages.AddFaceClientMessage;
import pt.cagojati.bombahman.multiplayer.messages.ConnectionCloseServerMessage;
import pt.cagojati.bombahman.multiplayer.messages.MessageFlags;
import android.os.Bundle;

import com.badlogic.gdx.math.Vector2;


public class GameActivity extends SimpleBaseGameActivity {

	private static final int CAMERA_WIDTH = 800;
	private static final int CAMERA_HEIGHT = 480;

	//	private ITextureRegion mFaceTextureRegion;
	private IMultiplayerConnector mConnector;
	private Map mMap;
	private Player[] mPlayers = new Player[4];
	private OnScreenControls mControls;
	private PhysicsWorld mPhysicsWorld;
	
	@Override
	protected void onCreate(Bundle pSavedInstanceState) {
		super.onCreate(pSavedInstanceState);

		//reads bundle and starts connector
		Bundle bundle = getIntent().getExtras();
		if(bundle.getBoolean("isWiFi")){
			mConnector = new WiFiConnector(bundle.getString("ip"));
		}
	}

	@Override
	public EngineOptions onCreateEngineOptions() {
		final Camera camera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);

		EngineOptions options = new EngineOptions(true, ScreenOrientation.LANDSCAPE_FIXED, new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), camera);
		options.getTouchOptions().setNeedsMultiTouch(true);
		return options;
	}

	@Override
	protected void onCreateResources() {
		this.mPlayers[0] = new Player();
		this.mControls = new OnScreenControls();
		this.mMap = new Map();
		this.mPhysicsWorld = new PhysicsWorld(new Vector2(0,0), false);

		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
		BuildableBitmapTextureAtlas textureAtlas = new BuildableBitmapTextureAtlas(getTextureManager(), 2048, 2048);
		
		this.mPlayers[0].loadResources(textureAtlas, this);
		this.mControls.loadResources(textureAtlas, this);

		try {
			textureAtlas.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource, BitmapTextureAtlas>(0, 0, 1));
			textureAtlas.load();
		} catch (TextureAtlasBuilderException e) {
			Debug.e(e);
		}
				
		//		playerBitmapTextureAtlas.load();

		//for testing purposes
		//		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
		//		
		//		this.mBitmapTextureAtlas = new BitmapTextureAtlas(this.getTextureManager(), 32, 32, TextureOptions.BILINEAR);
		//		this.mFaceTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas, this, "face_box.png", 0, 0);
		//
		//		this.mBitmapTextureAtlas.load();	
	}

	@Override
	protected Scene onCreateScene() {
		final Scene scene = new Scene();
		//scene.setBackground(new Background(0.09804f, 0.6274f, 0.8784f));
		mMap.loadMap(this.mPhysicsWorld, scene, mEngine, this.getAssets(), this.getVertexBufferObjectManager());

		float[] firstTilePosition = new float[2];
		firstTilePosition[0] = mMap.getTileWidth()*1.5f;
		firstTilePosition[1] = mMap.getTileHeight()*1.5f;
		this.mPlayers[0].createSprite(this.mPhysicsWorld,firstTilePosition[0],firstTilePosition[1], scene, this.getVertexBufferObjectManager());

		this.mControls.createAnalogControls(0, CAMERA_HEIGHT - this.mControls.getJoystickHeight(), this.mEngine.getCamera(), this.mPlayers[0], scene, this.getVertexBufferObjectManager());
		
		this.mConnector.setActivity(this);
		this.mConnector.initClient();
		//startTouchEvents(scene);
		
		scene.registerUpdateHandler(this.mPhysicsWorld);

		return scene;
	}

	public void addFace(final float pX, final float pY) {
		//		final Scene scene = this.mEngine.getScene();
		//		/* Create the face and add it to the scene. */
		//		final Sprite face = new Sprite(0, 0, this.mFaceTextureRegion, this.getVertexBufferObjectManager());
		//		face.setPosition(pX - face.getWidth() * 0.5f, pY - face.getHeight() * 0.5f);
		//		
		//		scene.attachChild(face);
		//this.mPlayers[0].setPosition(pX, pY);
	}

	public void startTouchEvents(Scene scene){

		scene.setOnSceneTouchListener(new IOnSceneTouchListener() {
			@Override
			public boolean onSceneTouchEvent(final Scene pScene, final TouchEvent pSceneTouchEvent) {
				if(pSceneTouchEvent.isActionDown()) {
					MessagePool<IMessage> messagePool = GameActivity.this.mConnector.getMessagePool();
					final AddFaceClientMessage addFaceClientMessage = (AddFaceClientMessage) messagePool.obtainMessage(MessageFlags.FLAG_MESSAGE_CLIENT_ADD_FACE);
					addFaceClientMessage.set(pSceneTouchEvent.getX(), pSceneTouchEvent.getY());
					GameActivity.this.mConnector.sendClientMessage(addFaceClientMessage);

					messagePool.recycleMessage(addFaceClientMessage);
					return true;
				} else {
					return true;
				}
			}
		});

		scene.setTouchAreaBindingOnActionDownEnabled(true);
	}

	@Override
	protected void onDestroy() {
		if(WiFiServer.isInitialized()) {
			WiFiServer server = WiFiServer.getSingletonObject();

			try {
				server.sendBroadcastServerMessage(new ConnectionCloseServerMessage());
			} catch (final IOException e) {
				Debug.e(e);
			}
			server.terminate();
		}

		if(this.mConnector != null) {
			this.mConnector.terminate();
		}

		super.onDestroy();
	}

}
