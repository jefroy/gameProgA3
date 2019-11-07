package tilegame;

import graphics.Sprite;
import graphics.input.GameAction;
import graphics.input.InputManager;
import sound.*;
import test.GameCore;
import tilegame.sprites.*;

import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.sampled.AudioFormat;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.Iterator;


/**
    GameManager manages all parts of the game.
*/
public class GameManager extends GameCore {

    public int doneTime;

    public static void main(String[] args) {
        new GameManager().run();
    }

    // uncompressed, 44100Hz, 16-bit, mono, signed, little-endian
    private static final AudioFormat PLAYBACK_FORMAT =
        new AudioFormat(44100, 16, 1, true, false);

    private static final int DRUM_TRACK = 1;

    public static final float GRAVITY = 0.002f;

    private Point pointCache = new Point();
    public TileMap map;
    private MidiPlayer midiPlayer;
    private SoundManager soundManager;
    private AudioHandler audioHandler;
    public ResourceManager resourceManager;

    private Sound prizeSound;
    private Sound boopSound;
    private Sound oofSound;

    private graphics.input.InputManager inputManager;
    private TileMapRenderer renderer;

    private graphics.input.GameAction moveLeft;
    private graphics.input.GameAction moveRight;
    private graphics.input.GameAction shoot;
    private graphics.input.GameAction jump;
    private graphics.input.GameAction exit;

    public void init() {
        super.init();

        // set up input manager
        initInput();

        // start resource manager
        resourceManager = new ResourceManager(screen.getFullScreenWindow().getGraphicsConfiguration());

        // load resources
        renderer = new TileMapRenderer(this);
        renderer.setBackground(
                resourceManager.loadImage("environment/plx1.png"),
                resourceManager.loadImage("environment/plx2.png"),
                resourceManager.loadImage("environment/plx3.png"),
                resourceManager.loadImage("environment/plx4.png"),
                resourceManager.loadImage("environment/plx5.png")
        );

        // load first map
        map = resourceManager.loadNextMap();

        // load sounds
        soundManager = new SoundManager(PLAYBACK_FORMAT);
        prizeSound = soundManager.getSound("sounds/prize.wav");
        boopSound = soundManager.getSound("sounds/boop2.wav");
//        oofSound = soundManager.getSound("sounds/player/oof.wav");

        // start music
        midiPlayer = new MidiPlayer();
        Sequence sequence =
            midiPlayer.getSequence("sounds/music.midi");
        midiPlayer.play(sequence, true);
        toggleDrumPlayback();
    }


    /**
        Closes any resources used by the GameManager.
    */
    public void stop() {
        super.stop();
        midiPlayer.close();
        soundManager.close();
    }


    private void initInput() { // controls
        moveLeft = new graphics.input.GameAction("moveLeft");
        moveRight = new graphics.input.GameAction("moveRight");
        jump = new graphics.input.GameAction(
                "jump",
                graphics.input.GameAction.DETECT_INITAL_PRESS_ONLY
        );
        shoot = new graphics.input.GameAction(
                "shoot",
                GameAction.NORMAL // THIS MAY NEED TO CHANGE TO initial press only
        );
        exit = new graphics.input.GameAction(
                "exit",
                graphics.input.GameAction.DETECT_INITAL_PRESS_ONLY
        );

        inputManager = new InputManager(
            screen.getFullScreenWindow());
        inputManager.setCursor(InputManager.INVISIBLE_CURSOR);

        inputManager.mapToKey(moveLeft, KeyEvent.VK_LEFT);
        inputManager.mapToKey(moveRight, KeyEvent.VK_RIGHT);
        inputManager.mapToKey(jump, KeyEvent.VK_SPACE);
        inputManager.mapToKey(shoot, KeyEvent.VK_Z);
        inputManager.mapToKey(exit, KeyEvent.VK_ESCAPE);
    }


    private void checkInput(long elapsedTime) {

        if (exit.isPressed()) {
            stop();
        }

        Player player = (Player)map.getPlayer();
        if (player.isAlive()) {
            float velocityX = 0;
            if (moveLeft.isPressed()) {
                velocityX-=player.getMaxSpeed();
            }
            if (moveRight.isPressed()) {
                velocityX+=player.getMaxSpeed();
            }
            if (jump.isPressed()) {
                player.jump(false);
                player.jumped = true;
            }
            if(shoot.isPressed()){
                // TODO: 22-Oct-19 make player.shoot();
            }
            player.setVelocityX(velocityX);
        }

    }


    public void draw(Graphics2D g) {
        renderer.draw(g, map,
            screen.getWidth(), screen.getHeight());
    }


    /**
        Gets the current map.
    */
    public TileMap getMap() {
        return map;
    }


    /**
        Turns on/off drum playback in the midi music (track 1).
    */
    public void toggleDrumPlayback() {
        Sequencer sequencer = midiPlayer.getSequencer();
        if (sequencer != null) {
            sequencer.setTrackMute(DRUM_TRACK,
                !sequencer.getTrackMute(DRUM_TRACK));
        }
    }


    /**
        Gets the tile that a Sprites collides with. Only the
        Sprite's X or Y should be changed, not both. Returns null
        if no collision is detected.
    */
    public Point getTileCollision(Sprite sprite,
        float newX, float newY)
    {
        float fromX = Math.min(sprite.getX(), newX);
        float fromY = Math.min(sprite.getY(), newY);
        float toX = Math.max(sprite.getX(), newX);
        float toY = Math.max(sprite.getY(), newY);

        // get the tile locations
        int fromTileX = TileMapRenderer.pixelsToTiles(fromX);
        int fromTileY = TileMapRenderer.pixelsToTiles(fromY);
        int toTileX = TileMapRenderer.pixelsToTiles(
            toX + sprite.getWidth() - 1);
        int toTileY = TileMapRenderer.pixelsToTiles(
            toY + sprite.getHeight() - 1);

        // check each tile for a collision
        for (int x=fromTileX; x<=toTileX; x++) {
            for (int y=fromTileY; y<=toTileY; y++) {
                if (x < 0 || x >= map.getWidth() ||
                    map.getTile(x, y) != null)
                {
                    // collision found, return the tile
                    pointCache.setLocation(x, y);
                    return pointCache;
                }
            }
        }

        // no collision found
        return null;
    }


    /**
        Checks if two Sprites collide with one another. Returns
        false if the two Sprites are the same. Returns false if
        one of the Sprites is a Creature that is not alive.
    */
    public boolean isCollision(Sprite s1, Sprite s2) {
        // if the Sprites are the same, return false
        if (s1 == s2) {
            return false;
        }

        // if one of the Sprites is a dead Creature, return false
        if (s1 instanceof Creature && !((Creature)s1).isAlive()) {
            return false;
        }
        if (s2 instanceof Creature && !((Creature)s2).isAlive()) {
            return false;
        }

        // get the pixel location of the Sprites
        int s1x = Math.round(s1.getX());
        int s1y = Math.round(s1.getY());
        int s2x = Math.round(s2.getX());
        int s2y = Math.round(s2.getY());

        // check if the two sprites' boundaries intersect
        return (s1x < s2x + s2.getNewWidth() &&
            s2x < s1x + s1.getNewWidth() &&
            s1y < s2y + s2.getNewHeight() &&
            s2y < s1y + s1.getNewHeight());
    }


    /**
        Gets the Sprite that collides with the specified Sprite,
        or null if no Sprite collides with the specified Sprite.
    */
    public Sprite getSpriteCollision(Sprite sprite) {

        // run through the list of Sprites
        Iterator i = map.getSprites();
        while (i.hasNext()) {
            Sprite otherSprite = (Sprite)i.next();
            if (isCollision(sprite, otherSprite)) {
                // collision found, return the Sprite
                return otherSprite;
            }
        }

        // no collision found
        return null;
    }


    /**
        Updates Animation, position, and velocity of all Sprites
        in the current map.
    */
    public void update(long elapsedTime) {
        Player player = (Player)map.getPlayer();
/*
	if (player == null)
		System.out.println("Player is null. Program about to crash.");
	else
		System.out.println("Player is not null. ");
*/
        // player is dead! start map over
        if (player.getState() == Creature.STATE_DEAD) {
            map = resourceManager.reloadMap();
            // TODO: 28-Oct-19 game over?
            return;
        }

        // get keyboard/mouse input
        checkInput(elapsedTime);

        // update player
        updatePlayer(player, elapsedTime);
        player.update(elapsedTime);

        // update other sprites
        Iterator i = map.getSprites();
        while (i.hasNext()) {
            Sprite sprite = (Sprite)i.next();
            if (sprite instanceof Creature) {
                Creature creature = (Creature)sprite;
                if (creature.getState() == Creature.STATE_DEAD) {
                    i.remove();
                }
                else {
                    updateCreature(creature, elapsedTime);
                }
            }
            // normal update
            sprite.update(elapsedTime);
        }
    }


    /**
        Updates the creature, applying gravity for creatures that
        aren't flying, and checks collisions.
    */
    private void updateCreature(Creature creature,
        long elapsedTime)
    {

        // apply gravity
        if (!creature.isFlying()) {
            creature.setVelocityY(creature.getVelocityY() +
                GRAVITY * elapsedTime);
        }

        // change x
        float dx = creature.getVelocityX();
        float oldX = creature.getX();
        float newX = oldX + dx * elapsedTime;
        Point tile =
            getTileCollision(creature, newX, creature.getY());
        if (tile == null) {
            creature.setX(newX);
        }
        else {
            // line up with the tile boundary
            if (dx > 0) {
                creature.setX(
                    TileMapRenderer.tilesToPixels(tile.x) -
                    creature.getWidth());
            }
            else if (dx < 0) {
                creature.setX(
                    TileMapRenderer.tilesToPixels(tile.x + 1));
            }
            creature.collideHorizontal();
        }
        if (creature instanceof Player) {
            checkPlayerCollision((Player)creature, false);
        }

        // change y
        float dy = creature.getVelocityY();
        float oldY = creature.getY();
        float newY = oldY + dy * elapsedTime;
        tile = getTileCollision(creature, creature.getX(), newY);
        if (tile == null) {
            creature.setY(newY);
        }
        else {
            // line up with the tile boundary
            if (dy > 0) {
                creature.setY(
                    TileMapRenderer.tilesToPixels(tile.y) -
                    creature.getHeight());
            }
            else if (dy < 0) {
                creature.setY(
                    TileMapRenderer.tilesToPixels(tile.y + 1));
            }
            creature.collideVertical();
        }
        if (creature instanceof Player) {
            boolean canKill = (oldY < creature.getY());
            checkPlayerCollision((Player)creature, canKill);
        }

        // upgrade all mobs after the time limit as passed
        if(secondsPassed >= creature.upTime){
            if(creature instanceof Creep_Zombie && creature.up != -1) ((Creep_Zombie) creature).upgrade();
            if(creature instanceof Creep_Fly && creature.up != -1) ((Creep_Fly) creature).upgrade();
            if(creature instanceof Dio && creature.up != -1) ((Dio) creature).upgrade();
        }

        if(creature instanceof Dio && creature.health <= 10 && !((Dio) creature).isEnraged) ((Dio) creature).enrage();

    }

    /**
     Updates the creature, applying gravity for creatures that
     aren't flying, and checks collisions.
     */
    private void updatePlayer(Player player, long elapsedTime)
    {

        // apply gravity
        if (!player.isFlying()) {
            player.setVelocityY(player.getVelocityY() +
                    GRAVITY * elapsedTime);
            player.jumped = true;
//            player.setState(player.STATE_FALLING);
        }
        else if(player.getVelocityY() < 0) {
//            player.setState(player.STATE_JUMPING);
            player.jumped = false;
        }
//        else player.setState(player.STATE_NORMAL);

        // change x
        float dx = player.getVelocityX();
        float oldX = player.getX();
        float newX = oldX + dx * elapsedTime;
        Point tile =
                getTileCollision(player, newX, player.getY());
        if (tile == null) {
            player.setX(newX);
        }
        else {
            // line up with the tile boundary
            if (dx > 0) {
                player.setX(
                        TileMapRenderer.tilesToPixels(tile.x) -
                                player.getWidth());
            }
            else if (dx < 0) {
                player.setX(
                        TileMapRenderer.tilesToPixels(tile.x + 1));
            }
            player.collideHorizontal();
        }
        if (player instanceof Player) {
            checkPlayerCollision((Player)player, false);
        }

        // change y
        float dy = player.getVelocityY();
        float oldY = player.getY();
        float newY = oldY + dy * elapsedTime;
        tile = getTileCollision(player, player.getX(), newY);
        if (tile == null) {
            player.setY(newY);
        }
        else {
            // line up with the tile boundary
            if (dy > 0) {
                player.setY(
                        TileMapRenderer.tilesToPixels(tile.y) -
                                player.getHeight());
            }
            else if (dy < 0) {
                player.setY(
                        TileMapRenderer.tilesToPixels(tile.y + 1));
            }
            player.collideVertical();
        }
        if (player instanceof Player) {
            boolean canKill = (oldY < player.getY());
            checkPlayerCollision((Player)player, canKill);
        }

        if(player.score >= player.up1 && player.up1 != -1) player.upgrade1();
        if(player.score >= player.up2 && player.up2 != -1) player.upgrade2();
        if(player.score >= player.up3 && player.up3 != -1) player.upgrade3();

    }


    /**
        Checks for Player collision with other Sprites. If
        canKill is true, collisions with Creatures will kill
        them.
    */
    public void checkPlayerCollision(Player player,
        boolean canKill)
    {
        if (!player.isAlive()) {
            return;
        }

        // check for player collision with other sprites
        Sprite collisionSprite = getSpriteCollision(player);
        if (collisionSprite instanceof PowerUp) {
            acquirePowerUp((PowerUp)collisionSprite);
        }
        else if (collisionSprite instanceof Creature) {
            Creature badguy = (Creature)collisionSprite;
            if (canKill) {
                // kill the badguy and make player bounce
                // add score here
                soundManager.play(boopSound);
                badguy.health -= player.damage;
                if(badguy.health <= 0){
                    player.score += badguy.worth;
                    badguy.setState(Creature.STATE_DYING);
                    if(badguy instanceof Dio){
                        player.win = true;
                        doneTime = this.secondsPassed;
                    }
                }

                player.setY(badguy.getY() - player.getHeight());
                player.jump(true);
            }
            else {
                // player dies!
//                soundManager.play(oofSound);
                player.health -= badguy.damage;
                if(player.health <= 0){
                    player.setState(Creature.STATE_DYING);
                    player.resetStats();
                    // TODO: 28-Oct-19 oof sound
                }
                else{
                    // if the player dies while idle, game can crash
                    player.grace();
                }

            }
        }
    }


    /**
        Gives the player the speicifed power up and removes it
        from the map.
    */
    public void acquirePowerUp(PowerUp powerUp) {
        // remove it from the map
        map.removeSprite(powerUp);

        if (powerUp instanceof PowerUp.Heart) {
            // do something here, like give the player points
            this.map.getPlayer().health += powerUp.worth;
            soundManager.play(prizeSound);
        }
        else if (powerUp instanceof PowerUp.Music) {
            // change the music
            soundManager.play(prizeSound);
            toggleDrumPlayback();
        }
        else if (powerUp instanceof PowerUp.Goal) {
            // advance to next map
            soundManager.play(prizeSound,
                new EchoFilter(2000, .7f), false);
            map = resourceManager.loadNextMap();
        }
    }



}
