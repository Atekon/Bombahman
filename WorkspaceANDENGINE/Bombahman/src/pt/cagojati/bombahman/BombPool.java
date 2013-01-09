package pt.cagojati.bombahman;

import java.util.Hashtable;

import org.andengine.entity.scene.Scene;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.opengl.texture.atlas.bitmap.BuildableBitmapTextureAtlas;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.util.adt.pool.GenericPool;

import android.content.Context;

public class BombPool extends GenericPool<Bomb> {

	private Scene mScene;
	private VertexBufferObjectManager mVertexBufferManager;
	private Hashtable<String, Bomb> mDictionary;

	public BombPool(VertexBufferObjectManager vertexBufferManager){
		mVertexBufferManager = vertexBufferManager;
		mDictionary = new Hashtable<String, Bomb>();
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
		mDictionary.remove(pItem.getId());
		pItem.getSprite().setIgnoreUpdate(true);
		pItem.getSprite().setVisible(false);
	}

	@Override
	protected void onHandleObtainItem(Bomb pItem) {
		pItem.generateId();
		mDictionary.put(pItem.getId(), pItem);
		pItem.getSprite().reset();
	}

	private Scene getScene() {
		return mScene;
	}

	public void setScene(Scene mScene) {
		this.mScene = mScene;
	}
	
	public Bomb getBomb(String id){
		if(mDictionary.containsKey(id)){
			return mDictionary.get(id);
		}
		return null;
	}
	
	public void replaceBomb(String oldId, String newId){
		Bomb bomb = mDictionary.remove(oldId);
		mDictionary.put(newId, bomb);
	}

}
