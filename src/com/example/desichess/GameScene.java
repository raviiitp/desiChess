package com.example.desichess;

import java.io.IOException;
import java.util.ArrayList;

import org.andengine.engine.camera.hud.HUD;
import org.andengine.engine.handler.timer.ITimerCallback;
import org.andengine.engine.handler.timer.TimerHandler;
import org.andengine.entity.IEntity;
import org.andengine.entity.modifier.MoveModifier;
import org.andengine.entity.modifier.ScaleModifier;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.sprite.ButtonSprite;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.Text;
import org.andengine.entity.text.TextOptions;
import org.andengine.input.touch.TouchEvent;
import org.andengine.util.SAXUtils;
import org.andengine.util.adt.align.HorizontalAlign;
import org.andengine.util.level.EntityLoader;
import org.xml.sax.Attributes;
import org.andengine.util.level.constants.LevelConstants;
import org.andengine.util.level.simple.SimpleLevelLoader;
import org.andengine.util.level.simple.SimpleLevelEntityLoaderData;

import com.example.desichess.SceneManager.SceneType;

/**
 * @author ravi
 *
 */


public class GameScene extends BaseScene {

	//private static final GameScene INSTANCE = new GameScene();
	float[][] marble;
	int[][] legalMoves;
	int[][] isEnemyInBetween;
	int[] spriteAtPosition;
	Sprite yourTurn;
	ButtonSprite pauseSprite;
	public enum playerTurn{
		PLAYER1,
		PLAYER2
	}
	private playerTurn turn = playerTurn.PLAYER1;
	
	@Override
	public void createScene() {
		createBackground();
	    createHUD();
	    initGame();
	    loadLevel(1);
	}

	@Override
	public void onBackKeyPressed() {
		SceneManager.getInstance().loadResumeScene(engine);
	}

	@Override
	public SceneType getSceneType() {
		return SceneType.SCENE_GAME;
	}

	@Override
	public void disposeScene() {
		camera.setHUD(null);
		camera.setCenter(CAMERA_WIDTH*0.5f, CAMERA_HEIGHT*0.5f);
		camera.setChaseEntity(null);
	}

	private void createBackground(){
		setBackground(new Background(0.8784f, 0.3274f, 0.09804f));
		this.attachChild(new Sprite(CAMERA_WIDTH*0.5f, CAMERA_HEIGHT*0.5f, resourcesManager.game_Parallax_background_region, vbom));
	}
	
	private void initGame(){
	    initMarble();
	    initIsEnemyInBetween();
	    initLegalMoves();
	    initSpriteAtPosition();
	    yourTurn = new Sprite(120, CAMERA_HEIGHT-100, resourcesManager.yourTurn, vbom);
	    GameScene.this.attachChild(yourTurn);
	    pauseSprite = new ButtonSprite(CAMERA_WIDTH*0.5f, CAMERA_HEIGHT*0.9f, resourcesManager.pause_region, vbom){
	    	@Override
            public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float X, float Y) 
            {
	    		pauseButtonPressed();
                return true;
            };
	    };
	    GameScene.this.registerTouchArea(pauseSprite);
	    GameScene.this.attachChild(pauseSprite);
	    
		win = new Text(0, 0, resourcesManager.font, "You WIN!", vbom);
		loss = new Text(0, 0, resourcesManager.font, "You LOSE!", vbom);
		gameHUD.attachChild(win);
		gameHUD.attachChild(loss);
		win.setVisible(false);
		loss.setVisible(false);
	}
	
	public void restartGame(){
	    int i=0;
	    Sprite tempSprite;
		for(i=1;i<20;i++){
			spriteAtPosition[i] = -1;
			tempSprite = (Sprite)GameScene.this.getChildByTag(i);
			if(tempSprite != null && (tempSprite!= yourTurn || tempSprite!=pauseSprite)){
				GameScene.this.unregisterTouchArea(tempSprite);
				tempSprite.detachSelf();
				tempSprite.dispose();
			}
		}
		scorePlayer1.setText("player1 score: 0");
		scorePlayer2.setText("player2 score: 0");
		turn = playerTurn.PLAYER1;
		if(!yourTurn.isVisible()){
			yourTurn.setVisible(true);
		}
		yourTurn.setX(120);
		yourTurn.setY(CAMERA_HEIGHT-100);
		prevTouchedSprite = null;
		toBeDetached.clear();
		player1Score = 0; player2Score = 0;
		hasPlayer1Started = false;
		hasPlayer2Started = false;
		loadLevel(1);
		win.setVisible(false);
		loss.setVisible(false);
	}
	
	private HUD gameHUD;
	private Text scorePlayer1, scorePlayer2;
	private Text win, loss;
	private float scorePlayer1x, scorePlayer1y, scorePlayer2x, scorePlayer2y;
	
	private void createHUD() {
		scorePlayer1x = 50; scorePlayer1y = CAMERA_HEIGHT*0.6f;
		scorePlayer2x = CAMERA_WIDTH-50; scorePlayer2y = CAMERA_HEIGHT*0.4f;
		
		gameHUD = new HUD();
		scorePlayer1 = new Text(scorePlayer1x, scorePlayer1y, resourcesManager.font, "player1 score: 0123456789", new TextOptions(HorizontalAlign.LEFT), vbom);
		scorePlayer1.setRotation(90);
		scorePlayer1.setAnchorCenter(0, 0);
		scorePlayer1.setText("player1 score: 0");
		gameHUD.attachChild(scorePlayer1);
		
		scorePlayer2 = new Text(scorePlayer2x, scorePlayer2y, resourcesManager.font, "player2 score: 0123456789", new TextOptions(HorizontalAlign.LEFT), vbom);
		scorePlayer2.setRotation(-90);
		scorePlayer2.setAnchorCenter(0, 0);
		scorePlayer2.setText("player2 score: 0");
		gameHUD.attachChild(scorePlayer2);
		
		camera.setHUD(gameHUD);
	}
	
	private float player1Score = 0, player2Score = 0;

	private void addToScore(int playerType, float f)
	{
		if(playerType == 1){
			player1Score += f;
		    scorePlayer1.setText("player1 score: " + player1Score);
		    if(player1Score == 90.0f){
		    	gameOverText(playerType);
		    }
		}
		if(playerType == 2){
			player2Score += f;
		    scorePlayer2.setText("player2 score: " + player2Score);
		    if(player2Score == 90.0f){
		    	gameOverText(playerType);
		    }
		}
	}
	
	private void pauseButtonPressed(){
		SceneManager.getInstance().loadResumeScene(engine);
	}
	
	private void gameOverText(int playerType){
		yourTurn.setVisible(false);
		//yourTurn.dispose();
		if(playerType == 1){
			win.setX(150);win.setY(CAMERA_HEIGHT*0.55f);
			loss.setX(CAMERA_WIDTH-150); loss.setY(CAMERA_HEIGHT*0.45f);
			win.setRotation(90);
			loss.setRotation(-90);

		}
		else if(playerType == 2){
			win.setX(CAMERA_WIDTH-150);win.setY(CAMERA_HEIGHT*0.45f);
			loss.setX(150); loss.setY(CAMERA_HEIGHT*0.55f);
			win.setRotation(-90);
			loss.setRotation(90);
		}
		win.setVisible(true);
		loss.setVisible(true);
	}
	
	void initMarble(){
		float cw2 = CAMERA_WIDTH*0.5f;
		float ch2 = CAMERA_HEIGHT*0.5f;
		marble = new float[20][2];
		marble[10][0] = cw2;
		marble[10][1] = ch2;
		
		marble[2][1] = marble[5][1] = marble[8][1] = marble[12][1] = marble[15][1] = marble[18][1] = marble[10][1];
		
		marble[1][0] = marble[2][0] = marble[3][0] = cw2 - 300;
		marble[4][0] = marble[5][0] = marble[6][0] = cw2 - 200;
		marble[7][0] = marble[8][0] = marble[9][0] = cw2 - 100;
		marble[11][0] = marble[12][0] = marble[13][0] = cw2 + 100;		
		marble[14][0] = marble[15][0] = marble[16][0] = cw2 + 200;		
		marble[17][0] = marble[18][0] = marble[19][0] = cw2 + 300;
		
		marble[1][1] = marble[17][1] = ch2 - 300;
		marble[3][1] = marble[19][1] = ch2 + 300;
		marble[4][1] = marble[14][1] = ch2 - 200;
		marble[6][1] = marble[16][1] = ch2 + 200;
		marble[7][1] = marble[11][1] = ch2 - 100;
		marble[9][1] = marble[13][1] = ch2 + 100;
	}
	
	void initLegalMoves(){
		legalMoves = new int[20][];
		
		legalMoves[1] = new int[5]; legalMoves[1][0] = 4; legalMoves[1][1] = 2; legalMoves[1][2] = 4; legalMoves[1][3] = 3; legalMoves[1][4] = 7;
 		legalMoves[2] = new int[5]; legalMoves[2][0] = 4; legalMoves[2][1] = 1; legalMoves[2][2] = 3; legalMoves[2][3] = 5; legalMoves[2][4] = 8;
		legalMoves[3] = new int[5]; legalMoves[3][0] = 4; legalMoves[3][1] = 2; legalMoves[3][2] = 6; legalMoves[3][3] = 1; legalMoves[3][4] = 9;
		legalMoves[4] = new int[6]; legalMoves[4][0] = 5; legalMoves[4][1] = 1; legalMoves[4][2] = 5; legalMoves[4][3] = 6; legalMoves[4][4] = 7; 
		legalMoves[4][5] = 10; 
		legalMoves[5] = new int[6]; legalMoves[5][0] = 5; legalMoves[5][1] = 2; legalMoves[5][2] = 4; legalMoves[5][3] = 6; legalMoves[5][4] = 8; 
		legalMoves[5][5] = 10; 
		legalMoves[6] = new int[6]; legalMoves[6][0] = 5; legalMoves[6][1] = 3; legalMoves[6][2] = 4; legalMoves[6][3] = 5; legalMoves[6][4] = 9; 
		legalMoves[6][5] = 10; 
		legalMoves[7] = new int[7]; legalMoves[7][0] = 6; legalMoves[7][1] = 1; legalMoves[7][2] = 4; legalMoves[7][3] = 8; legalMoves[7][4] = 9; 
		legalMoves[7][5] = 10; legalMoves[7][6] = 13; 
		
		legalMoves[8] = new int[7]; legalMoves[8][0] = 6; legalMoves[8][1] = 2; legalMoves[8][2] = 5; legalMoves[8][3] = 7; legalMoves[8][4] = 9; 
		legalMoves[8][5] = 10; legalMoves[8][6] = 12; 
		
		legalMoves[9] = new int[7]; legalMoves[9][0] = 6; legalMoves[9][1] = 3; legalMoves[9][2] = 6; legalMoves[9][3] = 7; legalMoves[9][4] = 8; 
		legalMoves[9][5] = 10; legalMoves[9][6] = 11; 
		
		legalMoves[10] = new int[13]; legalMoves[10][0] = 12; legalMoves[10][1] = 4; legalMoves[10][2] = 5; legalMoves[10][3] = 6; legalMoves[10][4] = 7; 
		legalMoves[10][5] = 8; legalMoves[10][6] = 9; legalMoves[10][7] = 11; legalMoves[10][8] = 12; legalMoves[10][9] = 13; legalMoves[10][10] = 14; 
		legalMoves[10][11] = 15; legalMoves[10][12] = 16; 
		
		legalMoves[11] = new int[7]; legalMoves[11][0] = 6; legalMoves[11][1] = 9; legalMoves[11][2] = 10; legalMoves[11][3] = 14; legalMoves[11][4] = 17; 
		legalMoves[11][5] = 12; legalMoves[11][6] = 13; 
		
		legalMoves[12] = new int[7]; legalMoves[12][0] = 6; legalMoves[12][1] = 8; legalMoves[12][2] = 10; legalMoves[12][3] = 11; legalMoves[12][4] = 13; 
		legalMoves[12][5] = 15; legalMoves[12][6] = 18; 
		
		legalMoves[13] = new int[7]; legalMoves[13][0] = 6; legalMoves[13][1] = 7; legalMoves[13][2] = 10; legalMoves[13][3] = 11; legalMoves[13][4] = 12; 
		legalMoves[13][5] = 16; legalMoves[13][6] = 19; 
		
		legalMoves[14] = new int[6]; legalMoves[14][0] = 5; legalMoves[14][1] = 17; legalMoves[14][2] = 11; legalMoves[14][3] = 10; legalMoves[14][4] = 15; 
		legalMoves[14][5] = 16;
		
		legalMoves[15] = new int[6]; legalMoves[15][0] = 5; legalMoves[15][1] = 10; legalMoves[15][2] = 12; legalMoves[15][3] = 14; legalMoves[15][4] = 16; 
		legalMoves[15][5] = 18; 
		
		legalMoves[16] = new int[6]; legalMoves[16][0] = 5; legalMoves[16][1] = 10; legalMoves[16][2] = 13; legalMoves[16][3] = 14; legalMoves[16][4] = 15; 
		legalMoves[16][5] = 19; 
		
		legalMoves[17] = new int[5]; legalMoves[17][0] = 4; legalMoves[17][1] = 11; legalMoves[17][2] = 14; legalMoves[17][3] = 18; legalMoves[17][4] = 19; 
		legalMoves[18] = new int[5]; legalMoves[18][0] = 4; legalMoves[18][1] = 12; legalMoves[18][2] = 15; legalMoves[18][3] = 17; legalMoves[18][4] = 19; 
		legalMoves[19] = new int[5]; legalMoves[19][0] = 4; legalMoves[19][1] = 13; legalMoves[19][2] = 16; legalMoves[19][3] = 17; legalMoves[19][4] = 18; 
	}
	
	public void initIsEnemyInBetween() {
		int i = 0, j = 0;
		isEnemyInBetween = new int[20][20];
		for (i = 0; i < 20; i++) {
			for (j = 0; j < 20; j++) {
				isEnemyInBetween[i][j] = -1;
			}
		}
		isEnemyInBetween[1][7] = 4; isEnemyInBetween[1][3] = 2; isEnemyInBetween[2][8] = 5; isEnemyInBetween[3][1] = 2; isEnemyInBetween[3][9] = 6; 
		isEnemyInBetween[4][6] = 5; isEnemyInBetween[4][10] = 7; isEnemyInBetween[5][10] = 8; isEnemyInBetween[6][4] = 5; isEnemyInBetween[6][10] = 9; 
		isEnemyInBetween[7][1] = 4; isEnemyInBetween[7][9] = 8; isEnemyInBetween[7][13] = 10; isEnemyInBetween[8][2] = 5; isEnemyInBetween[8][12] = 10; 
		isEnemyInBetween[9][3] = 6; isEnemyInBetween[9][7] = 8; isEnemyInBetween[9][11] = 10; isEnemyInBetween[10][4] = 7; isEnemyInBetween[10][5] = 8; 
		isEnemyInBetween[10][6] = 9; isEnemyInBetween[10][14] = 11; isEnemyInBetween[10][15] = 12; isEnemyInBetween[10][16] = 13; isEnemyInBetween[11][9] = 10; 
		isEnemyInBetween[11][13] = 12; isEnemyInBetween[11][17] = 14; isEnemyInBetween[12][8] = 10; isEnemyInBetween[12][18] = 15; isEnemyInBetween[13][7] = 10; 
		isEnemyInBetween[13][11] = 12; isEnemyInBetween[13][19] = 16; isEnemyInBetween[14][10] = 11; isEnemyInBetween[14][16] = 15; isEnemyInBetween[15][10] = 12; 
		isEnemyInBetween[16][10] = 13; isEnemyInBetween[16][14] = 15; isEnemyInBetween[17][11] = 14; isEnemyInBetween[17][19] = 18; isEnemyInBetween[18][12] = 15; 
		isEnemyInBetween[19][13] = 16; isEnemyInBetween[19][17] = 18; 
	}
	
	public void initSpriteAtPosition(){
		int i=0;
		spriteAtPosition = new int[20];
		for(i=0;i<20;i++){
			spriteAtPosition[i] = -1;
		}
	}
	
	private static final String TAG_ENTITY = "entity";
	private static final String TAG_ENTITY_ATTRIBUTE_POSITION = "position";
	private static final String TAG_ENTITY_ATTRIBUTE_TYPE = "type";
	    
	private static final Object TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_PLAYER1 = "player1";
	private static final Object TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_PLAYER2 = "player2";
	
	private void loadLevel(int levelID)
	{
	    final SimpleLevelLoader levelLoader = new SimpleLevelLoader(vbom);
	    
	    levelLoader.registerEntityLoader(new EntityLoader<SimpleLevelEntityLoaderData>(LevelConstants.TAG_LEVEL)
	    {
	        public IEntity onLoadEntity(final String pEntityName, final IEntity pParent, final Attributes pAttributes, final SimpleLevelEntityLoaderData pSimpleLevelEntityLoaderData) throws IOException 
	        {
	            //final int width = SAXUtils.getIntAttributeOrThrow(pAttributes, LevelConstants.TAG_LEVEL_ATTRIBUTE_WIDTH);
	            //final int height = SAXUtils.getIntAttributeOrThrow(pAttributes, LevelConstants.TAG_LEVEL_ATTRIBUTE_HEIGHT);
	            
	            //camera.setBounds(0, 0, width, height); // here we set camera bounds
	            return GameScene.this;
	        }

	    });
	    
	    levelLoader.registerEntityLoader(new EntityLoader<SimpleLevelEntityLoaderData>(TAG_ENTITY)
	    {
	        public IEntity onLoadEntity(final String pEntityName, final IEntity pParent, final Attributes pAttributes, final SimpleLevelEntityLoaderData pSimpleLevelEntityLoaderData) throws IOException
	        {
	            final int position = SAXUtils.getIntAttributeOrThrow(pAttributes, TAG_ENTITY_ATTRIBUTE_POSITION);
	            final String type = SAXUtils.getAttributeOrThrow(pAttributes, TAG_ENTITY_ATTRIBUTE_TYPE);
	            
	            final Sprite levelObject;
	            if (type.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_PLAYER1))
	            {
	                levelObject = new Sprite(marble[position][0], marble[position][1], resourcesManager.player1_region, vbom){
	                    @Override
	                    public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float X, float Y) 
	                    {
	                    	if(turn == playerTurn.PLAYER1){
	                    		currTouchedSprite = this;
	                    		playerSpriteTouched(pSceneTouchEvent, X, Y);
	                    	}
	                        return true;
	                    };
	                };
	                spriteAtPosition[position] = 1;
	            } 
	            else if (type.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_PLAYER2))
	            {
	                levelObject = new Sprite(marble[position][0], marble[position][1], resourcesManager.player2_region, vbom){
	                    @Override
	                    public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float X, float Y) 
	                    {
	                    	if(turn == playerTurn.PLAYER2){
	                    		currTouchedSprite = this;
	                    		playerSpriteTouched(pSceneTouchEvent, X, Y);
	                    	}
	                        return true;
	                    };
	                    
	                };
	                spriteAtPosition[position] = 2;
	            }
	            else
	            {
	                throw new IllegalArgumentException();
	            }

                levelObject.setUserData(position);
                levelObject.setTag(position);
	            levelObject.setCullingEnabled(true);
	            GameScene.this.registerTouchArea(levelObject);
	            return levelObject;
	        }
	    });

	    levelLoader.loadLevelFromAsset(activity.getAssets(), "level/" + levelID + ".lvl");
	}
	
	public Sprite prevTouchedSprite;
	boolean hasPlayer1Started = false, hasPlayer2Started = false;
	ArrayList<Sprite> toBeDetached = new ArrayList<Sprite>();
	Sprite currTouchedSprite; 
	public void playerSpriteTouched(TouchEvent pSceneTouchEvent, float X, float Y)
	{
		int spritePosition; 
		int i=0;
		if(currTouchedSprite != prevTouchedSprite)
		{
			spritePosition =  currTouchedSprite.getUserData().hashCode();
			removePrevHilightedSprite(prevTouchedSprite, toBeDetached);

        	currTouchedSprite.clearEntityModifiers();
        	currTouchedSprite.registerEntityModifier(new ScaleModifier(0.1f, 1f, 2f));
        	for(i = 1; i <= legalMoves[spritePosition][0]; i++){
        		if(canHilight(spritePosition, i)){
        			Sprite tempHilightSprite = null;
        			if(turn == playerTurn.PLAYER1){
            			tempHilightSprite = new Sprite(marble[legalMoves[spritePosition][i]][0], marble[legalMoves[spritePosition][i]][1], resourcesManager.hilight1_region, vbom){
    	                    @Override
    	                    public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float X, float Y) 
    	                    {
    	                    	hilightedSpriteTouched(this, pSceneTouchEvent, X, Y, toBeDetached);
    	                        return true;
    	                    };
            			};
        			}
        			else if(turn == playerTurn.PLAYER2){
            			tempHilightSprite = new Sprite(marble[legalMoves[spritePosition][i]][0], marble[legalMoves[spritePosition][i]][1], resourcesManager.hilight2_region, vbom){
    	                    @Override
    	                    public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float X, float Y) 
    	                    {
    	                    	hilightedSpriteTouched(this, pSceneTouchEvent, X, Y, toBeDetached);
    	                        return true;
    	                    };
            			};
        			}

        			tempHilightSprite.setTag(legalMoves[spritePosition][i]);
        			tempHilightSprite.setUserData(legalMoves[spritePosition][i]);
        			GameScene.this.attachChild(tempHilightSprite);
        			GameScene.this.registerTouchArea(tempHilightSprite);
        			spriteAtPosition[legalMoves[spritePosition][i]] = 0;
        			toBeDetached.add(tempHilightSprite);
        		}
        	}
	        prevTouchedSprite = currTouchedSprite;
		}
	}
	
	public boolean canHilight(int sPos, int i){
		if(spriteAtPosition[legalMoves[sPos][i]] == -1){
			if(isEnemyInBetween[sPos][legalMoves[sPos][i]]==-1){
				return true;
			}
			else if(spriteAtPosition[sPos] == 1 && spriteAtPosition[isEnemyInBetween[sPos][legalMoves[sPos][i]]] ==2){
				return true;
			}
			else if(spriteAtPosition[sPos] == 2 && spriteAtPosition[isEnemyInBetween[sPos][legalMoves[sPos][i]]] ==1){
				return true;
			}
		}
		return false;
	}
	
	public void removePrevHilightedSprite(Sprite pTSprite, ArrayList<Sprite> detachedSprite){
		int pos;
		int i = 0;
		if(hasPlayer1Started == false){
			hasPlayer1Started = true;
			return;
		}
		
		pos = pTSprite.getUserData().hashCode();
		for(Sprite s: detachedSprite){
			GameScene.this.unregisterTouchArea(s);
			s.detachSelf();
			s.dispose();
		}
		detachedSprite.clear();
		pTSprite.clearEntityModifiers();
		pTSprite.registerEntityModifier(new ScaleModifier(0.1f, 2f, 1f));
		for(i = 1;i<=legalMoves[pos][0];i++){
			if(spriteAtPosition[legalMoves[pos][i]] == 0){
				spriteAtPosition[legalMoves[pos][i]] = -1;
			}
		}
	}
	
	public void hilightedSpriteTouched(Sprite toSprite, TouchEvent pSceneTouchEvent, float X, float Y, ArrayList<Sprite> detachedSprite){
		int toSpritePos, fromSpritePos;
		int i;
		float x, y;
		x = toSprite.getX();
		y = toSprite.getY();
		fromSpritePos = currTouchedSprite.getUserData().hashCode();
		toSpritePos = toSprite.getUserData().hashCode();
		final int pType = spriteAtPosition[fromSpritePos];
		
		for(i = 1;i<=legalMoves[fromSpritePos][0];i++){
			if(spriteAtPosition[legalMoves[fromSpritePos][i]] == 0){
				spriteAtPosition[legalMoves[fromSpritePos][i]] = -1;
			}
		}
		for(Sprite s: detachedSprite){
			GameScene.this.unregisterTouchArea(s);
			s.detachSelf();
			s.dispose();
		}
		detachedSprite.clear();
		hasPlayer1Started = false;
		currTouchedSprite.clearEntityModifiers();
		currTouchedSprite.registerEntityModifier(new ScaleModifier(0.1f, 3f, 1f));
		currTouchedSprite.registerEntityModifier(new MoveModifier(0.5f, currTouchedSprite.getX(), currTouchedSprite.getY(), x, y));
		if(isEnemyInBetween[fromSpritePos][toSpritePos] != -1){
			
			final Sprite tempEnemySprite = (Sprite) GameScene.this.getChildByTag(isEnemyInBetween[fromSpritePos][toSpritePos]);
			if(spriteAtPosition[fromSpritePos] == 1){
				tempEnemySprite.registerEntityModifier(new MoveModifier(0.5f, tempEnemySprite.getX(), tempEnemySprite.getY(), scorePlayer1x, scorePlayer1y*0.5f));
			}
			else if(spriteAtPosition[fromSpritePos] == 2){
				tempEnemySprite.registerEntityModifier(new MoveModifier(0.5f, tempEnemySprite.getX(), tempEnemySprite.getY(), scorePlayer2x, scorePlayer2y*1.5f));
			}
			GameScene.this.unregisterTouchArea(tempEnemySprite);
			GameScene.this.registerUpdateHandler(new TimerHandler(0.5f, new ITimerCallback() {
				
				@Override
				public void onTimePassed(TimerHandler pTimerHandler) {
					tempEnemySprite.detachSelf();
					tempEnemySprite.dispose();
					addToScore(pType, 10);
				}
			}));
			spriteAtPosition[isEnemyInBetween[fromSpritePos][toSpritePos]] = -1;
			
		}
		spriteAtPosition[toSpritePos] = spriteAtPosition[fromSpritePos];
		spriteAtPosition[fromSpritePos] = -1;
		currTouchedSprite.setUserData(toSpritePos);
		currTouchedSprite.setTag(toSpritePos);
		prevTouchedSprite = null;
		if(turn == playerTurn.PLAYER1){
			turn = playerTurn.PLAYER2;
			yourTurn.setX(CAMERA_WIDTH - 120);
			yourTurn.setY(100);
		}
		else if(turn == playerTurn.PLAYER2){
			turn = playerTurn.PLAYER1;
			yourTurn.setX(120);
			yourTurn.setY(CAMERA_HEIGHT-100);
		}
	}
	
/*    public static GameScene getInstance()
    {
        return INSTANCE;
    }*/
}
