package pt.cagojati.bombahman;

import java.io.IOException;
import java.util.Iterator;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.scene.Scene;
import org.andengine.extension.multiplayer.protocol.adt.message.IMessage;
import org.andengine.extension.multiplayer.protocol.util.MessagePool;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.extension.tmx.TMXProperties;
import org.andengine.extension.tmx.TMXTile;
import org.andengine.extension.tmx.TMXTileProperty;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.atlas.bitmap.BuildableBitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.source.IBitmapTextureAtlasSource;
import org.andengine.opengl.texture.atlas.buildable.builder.BlackPawnTextureAtlasBuilder;
import org.andengine.opengl.texture.atlas.buildable.builder.ITextureAtlasBuilder.TextureAtlasBuilderException;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.ui.activity.SimpleBaseGameActivity;
import org.andengine.util.debug.Debug;

import pt.cagojati.bombahman.multiplayer.DeadReckoningClient;
import pt.cagojati.bombahman.multiplayer.IMultiplayerConnector;
import pt.cagojati.bombahman.multiplayer.IMultiplayerServer;
import pt.cagojati.bombahman.multiplayer.WiFiConnector;
import pt.cagojati.bombahman.multiplayer.WiFiServer;
import pt.cagojati.bombahman.multiplayer.messages.AddPowerupServerMessage;
import pt.cagojati.bombahman.multiplayer.messages.ConnectionCloseServerMessage;
import pt.cagojati.bombahman.multiplayer.messages.GetPowerupServerMessage;
import pt.cagojati.bombahman.multiplayer.messages.KillPlayerServerMessage;
import pt.cagojati.bombahman.multiplayer.messages.MessageFlags;
import android.os.Bundle;
import android.util.Log;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Manifold;


public class GameActivity extends SimpleBaseGameActivity {

	static final int CAMERA_WIDTH = 800;
	static final int CAMERA_HEIGHT = 480;

	//	private ITextureRegion mFaceTextureRegion;
	private static IPowerUp[] mPowerUpList;
	private static IMultiplayerConnector mConnector;
	private static Map mMap;
	private static Player[] mPlayers = new Player[4];
	private static Scene mScene;
	private static VertexBufferObjectManager mVertexBufferObjectManager;
	private OnScreenControls mControls;
	private static final PhysicsWorld mPhysicsWorld = new PhysicsWorld(new Vector2(0,0), false);
	private static BombPool mBombPool;
	private static int mCurrentPlayer;
	private static int mTotalPlayers = 0;

	@Override
	protected void onCreate(Bundle pSavedInstanceState) {
		super.onCreate(pSavedInstanceState);

		//reads bundle and starts connector
		Bundle bundle = getIntent().getExtras();
		if(bundle.getBoolean("isWiFi")){
			mConnector = new WiFiConnector(bundle.getString("ip"));
		}
		mPhysicsWorld.reset();
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
		GameActivity.mTotalPlayers =0;

		this.mControls = new OnScreenControls();
		GameActivity.setMap(new Map());

		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
		BuildableBitmapTextureAtlas textureAtlas = new BuildableBitmapTextureAtlas(getTextureManager(), 2048, 2048,TextureOptions.BILINEAR);

		for(int i =0; i<4; i++){
			GameActivity.mPlayers[i] = new Player(i);
			GameActivity.mPlayers[i].loadResources(textureAtlas, this);
		}

		this.mControls.loadResources(textureAtlas, this);
		Bomb.loadResources(0, textureAtlas, this);
		Explosion.loadResources(textureAtlas, this);
		FirePowerup.loadResources(textureAtlas, this);
		BombPowerup.loadResources(textureAtlas, this);
		Brick.loadResources(textureAtlas, this);
		
		try {
			textureAtlas.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource, BitmapTextureAtlas>(0, 0, 1));
			textureAtlas.load();
		} catch (TextureAtlasBuilderException e) {
			Debug.e(e);
		}

		setBombPool(new BombPool(this.getVertexBufferObjectManager()));
	}

	@Override
	protected Scene onCreateScene() {
		final Scene scene = new Scene();
		GameActivity.getBombPool().setScene(scene);
		GameActivity.getMap().loadMap(scene, mEngine, this.getAssets(), this.getVertexBufferObjectManager());

		createContactListeners();

		scene.registerUpdateHandler(GameActivity.mPhysicsWorld);

		GameActivity.mVertexBufferObjectManager = this.getVertexBufferObjectManager();
		GameActivity.mScene = scene;
		return scene;
	}

	@Override
	public synchronized void onGameCreated() {
		super.onGameCreated();
		GameActivity.mConnector.setActivity(this);
		if(getServer()!=null)
		{
			generatePowerups();
		}
		GameActivity.mConnector.initClient();
	}


	private void generatePowerups() {
		int max = 14;
		mPowerUpList = new IPowerUp[max];
		int i = 0;
		while(i<max){
			double x = Math.random()*CAMERA_WIDTH;
			double y = Math.random()*CAMERA_HEIGHT;
			TMXTile tile = mMap.getTMXTileAt((float)x, (float)y);
			TMXProperties<TMXTileProperty> property = tile.getTMXTileProperties(mMap.getTMXTiledMap());
			if(property!= null && property.containsTMXProperty("brick", "true")){
				int choice = (int) Math.round(Math.random());
				switch(choice)
				{
				case 0:
					FirePowerup fire = new FirePowerup();
					fire.setX((float) x);
					fire.setY((float) y);
					((Brick)tile.getUserData()).setPowerUp(fire);
					mPowerUpList[i] = fire;
					break;
				case 1:
					BombPowerup bomb = new BombPowerup();
					bomb.setX((float) x);
					bomb.setY((float) y);
					((Brick)tile.getUserData()).setPowerUp(bomb);
					mPowerUpList[i] = bomb;
					break;
				}
				i++;
			}
		}
	}
	
	public void addPowerups(IPowerUp[] vecPowerUps)
	{
		mPowerUpList = vecPowerUps;
		for(int i =0; i<vecPowerUps.length; i++){
			TMXTile tile = mMap.getTMXTileAt(vecPowerUps[i].getX(), vecPowerUps[i].getY());
			Brick brick = (Brick)tile.getUserData();
			brick.setPowerUp(vecPowerUps[i]);
		}
	}

	private void createContactListeners(){
		mPhysicsWorld.setContactListener(new ContactListener() {

			@Override
			public void preSolve(Contact contact, Manifold oldManifold) {

			}

			@Override
			public void postSolve(Contact contact, ContactImpulse impulse) {

			}

			@Override
			public void endContact(final Contact contact) {
				//happens when a new bomb is placed and adds the collision to the bomb
				if(contact.getFixtureB().isSensor() && contact.getFixtureB().getBody().getUserData().getClass()==Bomb.class && contact.getFixtureA().getBody().getUserData().getClass()==Player.class)
				{
					contact.getFixtureB().setSensor(false);
					Player player = (Player) contact.getFixtureA().getBody().getUserData();
					player.setOverBomb(false);
				}
			}

			@Override
			public void beginContact(final Contact contact) {
				if(contact.getFixtureB().isSensor() && contact.getFixtureB().getBody().getUserData().getClass()==Bomb.class && contact.getFixtureA().getBody().getUserData().getClass()==Player.class)
				{
					Player player = (Player) contact.getFixtureA().getBody().getUserData();
					player.setOverBomb(true);
				}else if(contact.getFixtureA().getBody().getUserData().getClass()==Explosion.class && contact.getFixtureB().getBody().getUserData().getClass()==Player.class){
					Player player = (Player) contact.getFixtureB().getBody().getUserData();
					GameActivity.this.serverKillPlayer(player);
				}else if(contact.getFixtureB().getBody().getUserData().getClass()==Explosion.class && contact.getFixtureA().getBody().getUserData().getClass()==Player.class){
					Player player = (Player) contact.getFixtureA().getBody().getUserData();
					GameActivity.this.serverKillPlayer(player);
				}else if(contact.getFixtureA().isSensor() == true && (contact.getFixtureA().getBody().getUserData().getClass() == FirePowerup.class || contact.getFixtureA().getBody().getUserData().getClass() == BombPowerup.class) && contact.getFixtureB().getBody().getUserData().getClass() == Player.class){
					IPowerUp powerUp = (IPowerUp) contact.getFixtureA().getBody().getUserData();
					Player player = (Player) contact.getFixtureB().getBody().getUserData();
					powerUp.destroy();
					powerUp.apply(player);
//					IMultiplayerServer server = getServer();
//					if(getServer()!=null)
//					{
//						IPowerUp powerUp = (IPowerUp) contact.getFixtureA().getBody().getUserData();
//						Player player = (Player) contact.getFixtureB().getBody().getUserData();
//						MessagePool<IMessage> msgPool = server.getMessagePool();
//						GetPowerupServerMessage msg = (GetPowerupServerMessage) msgPool.obtainMessage(MessageFlags.FLAG_MESSAGE_SERVER_GET_POWERUP);
//						msg.set(player.getId(), powerUp.getX(), powerUp.getY());
//						try {
//							server.sendBroadcastServerMessage(msg);							
//						} catch (IOException e) {
//							e.printStackTrace();
//						}
//						msgPool.recycleMessage(msg);
//					}
				}
			}
		});
	}
	
	public IMultiplayerServer getServer()
	{
		IMultiplayerServer server = null;
		if(MainActivity.isWifi)
		{
			if(WiFiServer.isInitialized())
			{
				server = WiFiServer.getSingletonObject();
			}
		}else{
			//TODO: bluetooth
		}
		return server;
	}

	public void serverKillPlayer(Player player)
	{
		IMultiplayerServer server = getServer();
		if(server != null)
		{
			KillPlayerServerMessage killPlayerServerMessage = (KillPlayerServerMessage) server.getMessagePool().obtainMessage(MessageFlags.FLAG_MESSAGE_SERVER_KILL_PLAYER);
			killPlayerServerMessage.setPlayerId(player.getId());
			try {
				server.sendBroadcastServerMessage(killPlayerServerMessage);
			} catch (IOException e) {
				e.printStackTrace();
			}
			server.getMessagePool().recycleMessage(killPlayerServerMessage);
		}
	}

	public void killPlayer(final Player player){

		GameActivity.mScene.postRunnable(new Runnable() {	
			@Override
			public void run() {
				if(player == GameActivity.getPlayer(GameActivity.mCurrentPlayer)){
					GameActivity.this.mControls.disable();
					GameActivity.this.mEngine.getScene().clearChildScene();
				}
				player.kill();
			}
		});
	}

	@Override
	protected void onDestroy() {
		Iterator<Body> itr = mPhysicsWorld.getBodies();
		while(itr.hasNext()) {
			Body element = itr.next(); 
			if(element!=null)
				mPhysicsWorld.destroyBody(element);
		} 

		if(WiFiServer.isInitialized()) {
			WiFiServer server = WiFiServer.getSingletonObject();

			try {
				server.sendBroadcastServerMessage(new ConnectionCloseServerMessage());
			} catch (final IOException e) {
				Debug.e(e);
			}
			server.terminate();
		}

		if(GameActivity.mConnector != null) {
			GameActivity.mConnector.terminate();
		}

		super.onDestroy();
	}

	public static Map getMap() {
		return mMap;
	}

	private static void setMap(Map mMap) {
		GameActivity.mMap = mMap;
	}

	public static PhysicsWorld getPhysicsWorld(){
		return mPhysicsWorld;
	}

	public static BombPool getBombPool() {
		return mBombPool;
	}

	private static void setBombPool(BombPool mBombPool) {
		GameActivity.mBombPool = mBombPool;
	}

	public static Player getPlayer(int id){
		return GameActivity.mPlayers[id];
	}

	public static IMultiplayerConnector getConnector(){
		return GameActivity.mConnector;
	}

	public static Scene getScene(){
		return GameActivity.mScene;
	}

	public static VertexBufferObjectManager getVertexBufferManager()
	{
		return GameActivity.mVertexBufferObjectManager;
	}

	public void addPlayer() {
		//first player should be server right?
		if(mTotalPlayers==0)
		{
			Clock clock = new Clock(10, this);
			clock.startTimer();
		}
		
		float[] firstTilePosition = new float[2];
		switch(GameActivity.mTotalPlayers)
		{
		case 0:
			firstTilePosition[0] = GameActivity.getMap().getTileWidth()*1.5f;
			firstTilePosition[1] = GameActivity.getMap().getTileHeight()*1.5f;
			break;
		case 1:
			firstTilePosition[0] = GameActivity.getMap().getTileWidth()*23.5f;
			firstTilePosition[1] = GameActivity.getMap().getTileHeight()*13.5f;
			break;
		case 2:
			firstTilePosition[0] = GameActivity.getMap().getTileWidth()*1.5f;
			firstTilePosition[1] = GameActivity.getMap().getTileHeight()*13.5f;
			break;
		case 3:
			firstTilePosition[0] = GameActivity.getMap().getTileWidth()*23.5f;
			firstTilePosition[1] = GameActivity.getMap().getTileHeight()*1.5f;
			break;
		}

		GameActivity.mPlayers[GameActivity.mTotalPlayers].initialize(firstTilePosition[0],firstTilePosition[1], GameActivity.mScene, GameActivity.mVertexBufferObjectManager);
		GameActivity.mTotalPlayers++;
	}

	public void setCurrentPlayerServerMessage() {
		GameActivity.mCurrentPlayer = GameActivity.mTotalPlayers-1;
		this.mControls.createAnalogControls(0, CAMERA_HEIGHT - this.mControls.getJoystickHeight()*1.5f, this.mEngine.getCamera(), GameActivity.mPlayers[mCurrentPlayer], GameActivity.mScene, GameActivity.mVertexBufferObjectManager);
		DeadReckoningClient.setPlayer(GameActivity.getPlayer(GameActivity.mCurrentPlayer));
		DeadReckoningClient.startTimer();
	}

	public static int getTotalPlayers()
	{
		return mTotalPlayers;
	}
	
	public static IPowerUp[] getPowerUpList()
	{
		return mPowerUpList;
	}
}
