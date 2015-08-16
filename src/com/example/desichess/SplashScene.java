package com.example.desichess;

import org.andengine.entity.sprite.Sprite;
import org.andengine.opengl.util.GLState;
import org.andengine.engine.camera.Camera;

import com.example.desichess.BaseScene;
import com.example.desichess.SceneManager.SceneType;

public class SplashScene extends BaseScene{

	private Sprite splash;
	private static final int CAMERA_WIDTH = 1200, CAMERA_HEIGHT = 720;
	
	@Override
	public void createScene() {
		splash = new Sprite((CAMERA_WIDTH) * 0.5f, (CAMERA_HEIGHT) * 0.5f, resourcesManager.splashRegion, vbom)
		{
		    @Override
		    protected void preDraw(GLState pGLState, Camera pCamera) 
		    {
		       super.preDraw(pGLState, pCamera);
		       pGLState.enableDither();
		    }
		};
		        
		//splash.setScale(1.5f);
		//splash.setPosition((CAMERA_WIDTH) * 0.5f, (CAMERA_HEIGHT) * 0.5f);
		attachChild(splash);
	}

	@Override
	public void onBackKeyPressed() {
		return;
	}

	@Override
	public SceneType getSceneType() {
		return SceneType.SCENE_SPLASH;
	}

	@Override
	public void disposeScene() {
		splash.detachSelf();
		splash.dispose();
		this.detachSelf();
		this.dispose();
	}

}
