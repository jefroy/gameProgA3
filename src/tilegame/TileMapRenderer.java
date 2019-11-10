package tilegame;

import java.awt.*;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import graphics.Sprite;
import tilegame.sprites.*;

/**
    The TileMapRenderer class draws a TileMap on the screen.
    It draws all tiles, sprites, and an optional background image
    centered around the position of the player.

    <p>If the width of background image is smaller the width of
    the tile map, the background image will appear to move
    slowly, creating a parallax background effect.

    <p>Also, three static methods are provided to convert pixels
    to tile positions, and vice-versa.

    <p>This TileMapRender uses a tile size of 64.
*/
public class TileMapRenderer {

    private static final int TILE_SIZE = 64;
    // the size in bits of the tile
    // Math.pow(2, TILE_SIZE_BITS) == TILE_SIZE
    private static final int TILE_SIZE_BITS = 6;

    private Image plx1;
    private Image plx2;
    private Image plx3;
    private Image plx4;
    private Image plx5;

    private GameManager gm;

    public TileMapRenderer(GameManager gm){
        this.gm = gm;
        System.out.println("created TileMapRenderer");
    }

    /**
        Converts a pixel position to a tile position.
    */
    public static int pixelsToTiles(float pixels) {
        return pixelsToTiles(Math.round(pixels));
    }


    /**
        Converts a pixel position to a tile position.
    */
    public static int pixelsToTiles(int pixels) {
        // use shifting to get correct values for negative pixels
        return pixels >> TILE_SIZE_BITS;

        // or, for tile sizes that aren't a power of two,
        // use the floor function:
        //return (int)Math.floor((float)pixels / TILE_SIZE);
    }


    /**
        Converts a tile position to a pixel position.
    */
    public static int tilesToPixels(int numTiles) {
        // no real reason to use shifting here.
        // it's slighty faster, but doesn't add up to much
        // on modern processors.
        return numTiles << TILE_SIZE_BITS;

        // use this if the tile size isn't a power of 2:
        //return numTiles * TILE_SIZE;
    }


    /**
        Sets the background to draw.
    */
    public void setBackground(Image plx1,Image plx2,Image plx3,Image plx4,Image plx5) {
        this.plx1 = plx1;
        this.plx2 = plx2;
        this.plx3 = plx3;
        this.plx4 = plx4;
        this.plx5 = plx5;
    }




    /**
        Draws the specified TileMap.
    */
    public void draw(Graphics2D g, TileMap map,
        int screenWidth, int screenHeight)
    {
        Player player = map.getPlayer();
        int mapWidth = tilesToPixels(map.getWidth());

        // get the scrolling position of the map
        // based on player's position
        int offsetX = screenWidth / 2 -
            Math.round(player.getX()) - TILE_SIZE;
        offsetX = Math.min(offsetX, 0);
        offsetX = Math.max(offsetX, screenWidth - mapWidth);
        int offsetX2 = screenWidth / 2 -
            Math.round(player.getX()) - TILE_SIZE;
        offsetX2 = Math.min(offsetX, 0);
        offsetX2 = Math.max(offsetX, screenWidth - mapWidth);

        // get the y offset to draw all sprites and tiles
        int offsetY = screenHeight -
            tilesToPixels(map.getHeight());

        // draw black background, if needed
        if (plx1 == null ||
            screenHeight > plx1.getHeight(null))
        {
            g.setColor(Color.black);
            g.fillRect(0, 0, screenWidth, screenHeight);
        }

        // draw parallax background image
        if (plx1 != null) {
            int x = offsetX *
                (screenWidth - plx1.getWidth(null)) /
                (screenWidth - mapWidth);
            x = x/7;
            int y = screenHeight - plx1.getHeight(null);
            g.drawImage(plx1, x, y, null);
        }
        if (plx2 != null) {
            int x = offsetX *
                (screenWidth - plx2.getWidth(null)) /
                (screenWidth - mapWidth);
            x = x / 5;
            int y = screenHeight - plx2.getHeight(null);
            g.drawImage(plx2, x, y, null);
        }
        if (plx3 != null) {
            int x = offsetX *
                (screenWidth - plx3.getWidth(null)) /
                (screenWidth - mapWidth);
            x = x / 4;
            int y = screenHeight - plx3.getHeight(null);
            g.drawImage(plx3, x, y, null);
        }
        if (plx4 != null) {
            int x = offsetX *
                (screenWidth - plx4.getWidth(null)) /
                (screenWidth - mapWidth);
            x = x / 3;
            int y = screenHeight - plx4.getHeight(null);
            g.drawImage(plx4, x, y, null);
        }
        if (plx5 != null) {
            int x = offsetX *
                (screenWidth - plx5.getWidth(null)) /
                (screenWidth - mapWidth);
            x = x / 2;
            int y = screenHeight - plx5.getHeight(null);
            g.drawImage(plx5, x, y, null);
        }

        // draw the visible tiles
        int firstTileX = pixelsToTiles(-offsetX);
        int lastTileX = firstTileX +
            pixelsToTiles(screenWidth) + 1;
        for (int y=0; y<map.getHeight(); y++) {
            for (int x=firstTileX; x <= lastTileX; x++) {
                Image image = map.getTile(x, y);
                if (image != null) {
                    g.drawImage(image,
                        tilesToPixels(x) + offsetX,
                        tilesToPixels(y) + offsetY,
                        null);
                }
            }
        }

        // draw player
        g.drawImage(player.getImage(),
            Math.round(player.getX()) + offsetX,
            Math.round(player.getY()) + offsetY,
//            player.newWidth,
//            player.newHeight,
            null);

        // draw sprites
        Iterator i = map.getSprites();
        while (i.hasNext()) {
            Sprite sprite = (Sprite)i.next();
            int x = Math.round(sprite.getX()) + offsetX;
            int y = Math.round(sprite.getY()) + offsetY + 10;
            g.drawImage(
                    sprite.getImage(), x, y,
//                    sprite.newWidth,
//                    sprite.newHeight,
                    null
            );

            // wake up the creature when it's on screen
            if (sprite instanceof Creature &&
                x >= 0 && x < screenWidth)
            {
                ((Creature)sprite).wakeUp();
            }
        }
        // draw gui over everything
        if(gm.resourceManager.getCurrentMap() == 4){
            drawGameOverGUI(g);
        }else{
            if(gm.map.getPlayer().drawDebug) drawDebugGUI(g);
            else drawGUI(g);
        }

    }

    public void drawDebugGUI(Graphics2D g2){
        Font f = new Font ("Times New Roman", Font.PLAIN, (30));
        g2.setFont(f);
        g2.setColor(Color.RED);
        ConcurrentHashMap<String, String> hm = gm.map.getPlayer().debugString();
        Enumeration e = hm.keys();
        String s;
        int x = 10;
        int y = 70;
        for (int i = 0; i < hm.size(); i++) {
            s = e.nextElement().toString();
            g2.drawString(s + hm.get(s), x , y);
            y += 50;
        }
    }

    public void drawGUI(Graphics2D g2){
        Font f = new Font ("Impact", Font.PLAIN, (30));
        g2.setFont(f);
        g2.setColor(Color.BLUE);
        g2.drawString("Time: " + Long.toString(gm.secondsPassed),10,70);
        g2.drawString("Level: " + Long.toString(gm.map.getPlayer().level),180,70);
        g2.drawString("HP: " + Integer.toString(gm.map.getPlayer().health) + "/" + Integer.toString(gm.map.getPlayer().maxHP),10,130);
        g2.drawString("DMG: " + Integer.toString(gm.map.getPlayer().damage),10,190);
        g2.drawString("Score: " + Integer.toString(gm.map.getPlayer().score),10,240);
        g2.drawString("EXP: " + Integer.toString(gm.map.getPlayer().exp) + "/" + gm.map.getPlayer().toNextLevel,180,240);
        g2.drawString("Baddies: " + Integer.toString(gm.resourceManager.numBaddies),10,290);
//        g2.drawString("DIO HP: " + Integer.toString(gm.resourceManager.dioSprite.health),500,70); // TODO: 07-Nov-19 fix this
    }

    public void drawGameOverGUI(Graphics2D g2){
        Font f = new Font ("Impact", Font.PLAIN, (60));
        g2.setFont(f);
        g2.setColor(Color.BLUE);
        g2.drawString("Time: " + Long.toString(gm.doneTime),10,70);
        g2.drawString("HP: " + Integer.toString(gm.map.getPlayer().health),10,130);
        g2.drawString("DMG: " + Integer.toString(gm.map.getPlayer().damage),10,190);
        g2.drawString("Score: " + Integer.toString(gm.map.getPlayer().score),10,240);
//        g2.drawString("DIO HP: " + Integer.toString(gm.map.Dio().health),500,70);
        if(gm.map.getPlayer().win) g2.drawString("you win :)",450,250);
        else{
            g2.drawString("you lose :(",450,250);
            g2.drawString("plz kill dio!",450,350);
        }
    }

}
