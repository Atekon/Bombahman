package org.andengine.entity.particle;

import java.util.ArrayList;

import org.andengine.engine.camera.Camera;
import org.andengine.entity.Entity;
import org.andengine.entity.IEntityFactory;
import org.andengine.entity.particle.emitter.IParticleEmitter;
import org.andengine.entity.particle.initializer.IParticleInitializer;
import org.andengine.entity.particle.modifier.IParticleModifier;
import org.andengine.entity.sprite.Sprite;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.util.GLState;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.util.Constants;
import org.andengine.util.math.MathUtils;

import android.util.FloatMath;

public class EditableSpriteParticleSystem extends Entity{

	// ===========================================================
	// Constants
	// ===========================================================

	private static final float[] POSITION_OFFSET_CONTAINER = new float[2];

	// ===========================================================
	// Fields
	// ===========================================================

	protected final IEntityFactory<Sprite> mEntityFactory;
	protected final IParticleEmitter mParticleEmitter;

	protected final Particle<Sprite>[] mParticles;

	protected final ArrayList<IParticleInitializer<Sprite>> mParticleInitializers = new ArrayList<IParticleInitializer<Sprite>>();
	protected final ArrayList<IParticleModifier<Sprite>> mParticleModifiers = new ArrayList<IParticleModifier<Sprite>>();

	private float mRateMinimum;
	private float mRateMaximum;

	private boolean mParticlesSpawnEnabled = true;

	private int mParticlesMaximum;
	protected int mParticlesAlive;
	private float mParticlesDueToSpawn;

	// ===========================================================
	// Constructors
	// ===========================================================
	
	public EditableSpriteParticleSystem(final IParticleEmitter pParticleEmitter, final float pRateMinimum, final float pRateMaximum, final int pParticlesMaximum, final ITextureRegion pTextureRegion, final VertexBufferObjectManager pVertexBufferObjectManager) {
		this(0, 0, pParticleEmitter, pRateMinimum, pRateMaximum, pParticlesMaximum, pTextureRegion, pVertexBufferObjectManager);
	}

	public EditableSpriteParticleSystem(final IEntityFactory<Sprite> pEntityFactory, final IParticleEmitter pParticleEmitter, final float pRateMinimum, final float pRateMaximum, final int pParticlesMaximum) {
		this(0, 0, pEntityFactory, pParticleEmitter, pRateMinimum, pRateMaximum, pParticlesMaximum);
	}

	@SuppressWarnings("unchecked")
	public EditableSpriteParticleSystem(final float pX, final float pY, final IEntityFactory<Sprite> pEntityFactory, final IParticleEmitter pParticleEmitter, final float pRateMinimum, final float pRateMaximum, final int pParticlesMaximum) {
		super(pX, pY);

		this.mEntityFactory = pEntityFactory;
		this.mParticleEmitter = pParticleEmitter;
		this.mParticles = (Particle<Sprite>[])new Particle[pParticlesMaximum];
		this.mRateMinimum = pRateMinimum;
		this.mRateMaximum = pRateMaximum;
		this.setParticlesMaximum(pParticlesMaximum);

		this.registerUpdateHandler(this.mParticleEmitter);
	}
	
	public EditableSpriteParticleSystem(final float pX, final float pY, final IParticleEmitter pParticleEmitter, final float pRateMinimum, final float pRateMaximum, final int pParticlesMaximum, final ITextureRegion pTextureRegion, final VertexBufferObjectManager pVertexBufferObjectManager) {
		this(pX, pY, new IEntityFactory<Sprite>() {
			@Override
			public Sprite create(final float pX, final float pY) {
				return new Sprite(pX, pY, pTextureRegion, pVertexBufferObjectManager);
			}
		}, pParticleEmitter, pRateMinimum, pRateMaximum, pParticlesMaximum);
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	public boolean isParticlesSpawnEnabled() {
		return this.mParticlesSpawnEnabled;
	}

	public void setParticlesSpawnEnabled(final boolean pParticlesSpawnEnabled) {
		this.mParticlesSpawnEnabled = pParticlesSpawnEnabled;
	}

	public IEntityFactory<Sprite> getParticleFactory() {
		return this.mEntityFactory;
	}

	public IParticleEmitter getParticleEmitter() {
		return this.mParticleEmitter;
	}
	
	public void setMaxRate(float rate){
		this.mRateMaximum = rate;
	}

	public float getMaxRate()
	{
		return this.mRateMaximum;
	}

	public void setMinRate(float rate){
		this.mRateMinimum = rate;
	}

	public float getMinRate()
	{
		return this.mRateMinimum;
	}	
	
	public int getParticlesMaximum() {
		return mParticlesMaximum;
	}

	public void setParticlesMaximum(int mParticlesMaximum) {
		this.mParticlesMaximum = mParticlesMaximum;
	}

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	@Override
	public void reset() {
		super.reset();

		this.mParticlesDueToSpawn = 0;
		this.mParticlesAlive = 0;
	}

	@Override
	protected void onManagedDraw(final GLState pGLState, final Camera pCamera) {
		for(int i = this.mParticlesAlive - 1; i >= 0; i--) {
			this.mParticles[i].onDraw(pGLState, pCamera);
		}
	}

	@Override
	protected void onManagedUpdate(final float pSecondsElapsed) {
		super.onManagedUpdate(pSecondsElapsed);

		if(this.isParticlesSpawnEnabled()) {
			this.spawnParticles(pSecondsElapsed);
		}

		final int particleModifierCountMinusOne = this.mParticleModifiers.size() - 1;
		for(int i = this.mParticlesAlive - 1; i >= 0; i--) {
			final Particle<Sprite> particle = this.mParticles[i];

			/* Apply all particleModifiers */
			for(int j = particleModifierCountMinusOne; j >= 0; j--) {
				this.mParticleModifiers.get(j).onUpdateParticle(particle);
			}

			particle.onUpdate(pSecondsElapsed);
			if(particle.isExpired()){
				this.mParticlesAlive--;

				this.moveParticleToEnd(i);
			}
		}
	}

	protected void moveParticleToEnd(final int pIndex) {
		final Particle<Sprite> particle = this.mParticles[pIndex];

		final int particlesToCopy = this.mParticlesAlive - pIndex;
		if(particlesToCopy > 0) {
			System.arraycopy(this.mParticles, pIndex + 1, this.mParticles, pIndex, particlesToCopy);
		}
		this.mParticles[this.mParticlesAlive] = particle;

		/* This mode of swapping particles is faster than copying tons of array elements, 
		 * but it doesn't respect the 'lifetime' of the particles. */
		//			particles[i] = particles[this.mParticlesAlive];
		//			particles[this.mParticlesAlive] = particle;
	}

	// ===========================================================
	// Methods
	// ===========================================================

	public void addParticleModifier(final IParticleModifier<Sprite> pParticleModifier) {
		this.mParticleModifiers.add(pParticleModifier);
	}

	public void removeParticleModifier(final IParticleModifier<Sprite> pParticleModifier) {
		this.mParticleModifiers.remove(pParticleModifier);
	}

	public void addParticleInitializer(final IParticleInitializer<Sprite> pParticleInitializer) {
		this.mParticleInitializers.add(pParticleInitializer);
	}

	public void removeParticleInitializer(final IParticleInitializer<Sprite> pParticleInitializer) {
		this.mParticleInitializers.remove(pParticleInitializer);
	}

	private void spawnParticles(final float pSecondsElapsed) {
		final float currentRate = this.determineCurrentRate();
		final float newParticlesThisFrame = currentRate * pSecondsElapsed;

		this.mParticlesDueToSpawn += newParticlesThisFrame;

		final int particlesToSpawnThisFrame = Math.min(this.getParticlesMaximum() - this.mParticlesAlive, (int)FloatMath.floor(this.mParticlesDueToSpawn));
		this.mParticlesDueToSpawn -= particlesToSpawnThisFrame;

		for(int i = 0; i < particlesToSpawnThisFrame; i++){
			this.spawnParticle();
		}
	}

	private void spawnParticle() {
		if(this.mParticlesAlive < this.getParticlesMaximum()){
			Particle<Sprite> particle = this.mParticles[this.mParticlesAlive];

			/* New particle needs to be created. */
			this.mParticleEmitter.getPositionOffset(EditableSpriteParticleSystem.POSITION_OFFSET_CONTAINER);

			final float x = EditableSpriteParticleSystem.POSITION_OFFSET_CONTAINER[Constants.VERTEX_INDEX_X];
			final float y = EditableSpriteParticleSystem.POSITION_OFFSET_CONTAINER[Constants.VERTEX_INDEX_Y];

			if(particle == null) {
				particle = new Particle<Sprite>();
				this.mParticles[this.mParticlesAlive] = particle;
				particle.setEntity(this.mEntityFactory.create(x, y));
			} else {
				particle.reset();
				particle.getEntity().setPosition(x, y);
			}

			/* Apply particle initializers. */
			{
				for(int i = this.mParticleInitializers.size() - 1; i >= 0; i--) {
					this.mParticleInitializers.get(i).onInitializeParticle(particle);
				}

				for(int i = this.mParticleModifiers.size() - 1; i >= 0; i--) {
					this.mParticleModifiers.get(i).onInitializeParticle(particle);
				}
			}

			this.mParticlesAlive++;
		}
	}

	protected float determineCurrentRate() {
		if(this.mRateMinimum == this.mRateMaximum){
			return this.mRateMinimum;
		} else {
			return MathUtils.random(this.mRateMinimum, this.mRateMaximum);
		}
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

}
