package pt.cagojati.bombahman;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.camera.hud.controls.AnalogOnScreenControl;
import org.andengine.engine.camera.hud.controls.AnalogOnScreenControl.IAnalogOnScreenControlListener;
import org.andengine.engine.camera.hud.controls.BaseOnScreenControl;
import org.andengine.entity.scene.Scene;
import org.andengine.opengl.texture.ITexture;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.atlas.bitmap.BuildableBitmapTextureAtlas;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

import android.content.Context;
import android.opengl.GLES20;

public class OnScreenControls {
	
	private ITextureRegion mOnScreenControlBaseTextureRegion;
	private ITextureRegion mOnScreenControlKnobTextureRegion;

	public OnScreenControls(){
		
	}
	
	public void loadResources(BuildableBitmapTextureAtlas textureAtlas, Context context)
	{
		this.mOnScreenControlBaseTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(textureAtlas, context, "onscreen_control_base.png");
		this.mOnScreenControlKnobTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(textureAtlas, context, "onscreen_control_knob.png");
	}
	
	public ITexture[] loadResources(BitmapTextureAtlas textureAtlas, int offsetX, int offsetY, Context context){
		ITexture[] vec = new ITexture[2];
		this.mOnScreenControlBaseTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(textureAtlas, context, "onscreen_control_base.png", offsetX, offsetY);
		vec[0] = this.mOnScreenControlBaseTextureRegion.getTexture();
		this.mOnScreenControlKnobTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(textureAtlas, context, "onscreen_control_knob.png", offsetX+128, offsetY);
		vec[1] = this.mOnScreenControlKnobTextureRegion.getTexture();
		return vec;
	}
	
	public void createAnalogControls(float posX, float posY, Camera camera, final Player player,Scene scene, VertexBufferObjectManager vertexBufferManager)
	{
		final AnalogOnScreenControl analogOnScreenControl = new AnalogOnScreenControl(posX, posY, camera, this.mOnScreenControlBaseTextureRegion, this.mOnScreenControlKnobTextureRegion, 0.1f, 200, vertexBufferManager, new IAnalogOnScreenControlListener() {
			@Override
			public void onControlChange(final BaseOnScreenControl pBaseOnScreenControl, final float pValueX, final float pValueY) {
				player.animate(pValueX, pValueY);
			}

			@Override
			public void onControlClick(AnalogOnScreenControl pAnalogOnScreenControl) {

			}
		});

		analogOnScreenControl.getControlBase().setBlendFunction(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
		analogOnScreenControl.getControlBase().setAlpha(0.5f);
		analogOnScreenControl.getControlBase().setScaleCenter(0, 128);
		//analogOnScreenControl.getControlBase().setScale(1.25f);
		//analogOnScreenControl.getControlKnob().setScale(1.25f);
		analogOnScreenControl.refreshControlKnobPosition();
		
		scene.setChildScene(analogOnScreenControl);
	}
	
	public float getJoystickHeight(){
		return this.mOnScreenControlBaseTextureRegion.getHeight();
	}
}
