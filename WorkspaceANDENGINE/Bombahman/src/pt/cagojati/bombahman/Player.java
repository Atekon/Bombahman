package pt.cagojati.bombahman;

import org.andengine.entity.scene.Scene;
import org.andengine.entity.sprite.AnimatedSprite;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.atlas.bitmap.BuildableBitmapTextureAtlas;
import org.andengine.opengl.texture.region.ITiledTextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

import android.content.Context;

public class Player {

	
	ITiledTextureRegion mPlayerTextureRegion;
	AnimatedSprite mSprite;

	public Player(){
		
	}
	
	public void loadResources(BuildableBitmapTextureAtlas textureAtlas, Context context){
		this.mPlayerTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(textureAtlas, context, "playerwhite.png",3,4);
	}
	
	public void loadResources(BitmapTextureAtlas textureAtlas, Context context)
	{
		this.mPlayerTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(textureAtlas, context, "playerwhite.png", 0, 0, 3, 4);

	}
	
	public void createSprite(float posX, float posY, Scene scene, VertexBufferObjectManager vertexBufferManager){
		this.mSprite = new AnimatedSprite(0, 0, this.mPlayerTextureRegion, vertexBufferManager);
		this.mSprite.setCurrentTileIndex(7);
		this.mSprite.setScale(0.35f);
		this.setPosition(posX, posY);
		scene.attachChild(this.mSprite);
	}
	
	public void setPosition(float posX, float posY){
		this.mSprite.setPosition(posX-this.mSprite.getWidth()/2, posY-3*this.mSprite.getWidth()/4);

	}
	
	public AnimatedSprite getSprite() {
		return mSprite;
	}

	public void setSprite(AnimatedSprite mSprite) {
		this.mSprite = mSprite;
	}
	
}
