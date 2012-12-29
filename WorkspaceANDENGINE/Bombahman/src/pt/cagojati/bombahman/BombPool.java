package pt.cagojati.bombahman;

import org.andengine.entity.scene.Scene;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.opengl.texture.atlas.bitmap.BuildableBitmapTextureAtlas;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.util.adt.pool.GenericPool;

import android.content.Context;

public class BombPool extends GenericPool<Bomb> {

	private Scene mScene;
	private BuildableBitmapTextureAtlas mTextureAtlas;
	private Context mContext;
	private PhysicsWorld mPhysicsWorld;
	private VertexBufferObjectManager mVertexBufferManager;

	public BombPool(BuildableBitmapTextureAtlas textureAtlas, PhysicsWorld physicsWorld, Context context, VertexBufferObjectManager vertexBufferManager){
		this.mTextureAtlas = textureAtlas;
		this.mContext = context;
		mVertexBufferManager = vertexBufferManager;
		mPhysicsWorld = physicsWorld;
	}

	@Override
	protected Bomb onAllocatePoolItem() {
		if(getScene()!=null){
			Bomb bomb = new Bomb();
			bomb.createSprite(0, 0, getScene(), mVertexBufferManager);
			return bomb;
		}else{
			return null;
		}
	}

	@Override
	protected void onHandleRecycleItem(Bomb pItem) {
		pItem.getSprite().setIgnoreUpdate(true);
		pItem.getSprite().setVisible(false);

	}

	@Override
	protected void onHandleObtainItem(Bomb pItem) {
		pItem.getSprite().reset();
	}

	private Scene getScene() {
		return mScene;
	}

	public void setScene(Scene mScene) {
		this.mScene = mScene;
	}

}
