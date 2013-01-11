package pt.cagojati.bombahman;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.camera.hud.controls.AnalogOnScreenControl;
import org.andengine.engine.camera.hud.controls.AnalogOnScreenControl.IAnalogOnScreenControlListener;
import org.andengine.engine.camera.hud.controls.BaseOnScreenControl;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.IOnAreaTouchListener;
import org.andengine.entity.scene.ITouchArea;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.sprite.Sprite;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.texture.ITexture;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.atlas.bitmap.BuildableBitmapTextureAtlas;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

import pt.cagojati.bombahman.multiplayer.DeadReckoning;
import pt.cagojati.bombahman.multiplayer.messages.MovePlayerClientMessage;

import com.badlogic.gdx.math.Vector2;

import android.content.Context;
import android.opengl.GLES20;
import android.util.Log;

public class OnScreenControls {
	
	private ITextureRegion mOnScreenControlBaseTextureRegion;
	private ITextureRegion mOnScreenControlKnobTextureRegion;
	private ITextureRegion mOnScreenBombButtonTextureRegion;
	private AnalogOnScreenControl mAnalogOnScreenControl;
	private Sprite mOnScreenBombButton;

	public OnScreenControls(){
		
	}
	
	public void loadResources(BuildableBitmapTextureAtlas textureAtlas, Context context)
	{
		this.mOnScreenControlBaseTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(textureAtlas, context, "onscreen_control_base.png");
		this.mOnScreenControlKnobTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(textureAtlas, context, "onscreen_control_knob.png");
		this.mOnScreenBombButtonTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(textureAtlas, context, "bombButton.png");
	}
	
	//deprecated
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
		this.mAnalogOnScreenControl = new AnalogOnScreenControl(posX, posY, camera, this.mOnScreenControlBaseTextureRegion, this.mOnScreenControlKnobTextureRegion, 0.1f, 200, vertexBufferManager, new IAnalogOnScreenControlListener() {
			@Override
			public void onControlChange(final BaseOnScreenControl pBaseOnScreenControl, final float pValueX, final float pValueY) {
				player.move(pValueX*3, pValueY*3);
				player.animate(pValueX, pValueY);
				
				if(!player.getDeadBoundBox().contains(player.getPosX(), player.getPosY()))
				{
					DeadReckoning.sendMoveMessage(pValueX*3, pValueY*3);
				}
			}

			@Override
			public void onControlClick(AnalogOnScreenControl pAnalogOnScreenControl) {

			}
		});

		this.mAnalogOnScreenControl.getControlBase().setBlendFunction(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
		this.mAnalogOnScreenControl.getControlBase().setAlpha(0.25f);
		this.mAnalogOnScreenControl.getControlKnob().setAlpha(0.5f);
		this.mAnalogOnScreenControl.getControlBase().setScaleCenter(0, 128);
		//analogOnScreenControl.getControlBase().setScale(1.25f);
		//analogOnScreenControl.getControlKnob().setScale(1.25f);
		this.mAnalogOnScreenControl.refreshControlKnobPosition();
		
		//Create Bomb Button Sprite
		this.mOnScreenBombButton = new Sprite(0, 0, this.mOnScreenBombButtonTextureRegion, vertexBufferManager);
		this.mOnScreenBombButton.setAlpha(0.5f);
		this.mOnScreenBombButton.setScale(0.5f);
		this.mOnScreenBombButton.setPosition(GameActivity.CAMERA_WIDTH-this.mOnScreenBombButton.getWidthScaled()*1.5f-posX,posY-this.mOnScreenBombButton.getHeightScaled()/2);
		this.mAnalogOnScreenControl.attachChild(this.mOnScreenBombButton);
		this.mAnalogOnScreenControl.registerTouchArea(this.mOnScreenBombButton);
		
		//register touch events
		this.mAnalogOnScreenControl.setOnAreaTouchListener(new IOnAreaTouchListener() {
			
			@Override
			public boolean onAreaTouched(TouchEvent pSceneTouchEvent,
					ITouchArea pTouchArea, float pTouchAreaLocalX,
					float pTouchAreaLocalY) {
				if(pSceneTouchEvent.isActionDown()){
					if(!player.isOverBomb()){
						player.dropBomb();
					}					
				}
				return true;
			}
		});
		
		scene.setChildScene(this.mAnalogOnScreenControl);
	}
	
	public float getJoystickHeight(){
		return this.mOnScreenControlBaseTextureRegion.getHeight();
	}
	
	public void disable(){
		this.mAnalogOnScreenControl.setIgnoreUpdate(true);
		this.mAnalogOnScreenControl.detachSelf();
	}

}
