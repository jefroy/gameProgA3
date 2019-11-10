package tilegame.sprites;

import graphics.Animation;

import java.lang.reflect.Constructor;
import java.util.concurrent.ConcurrentHashMap;

/**
    The Player.
*/
public class Player extends Creature {

    public static final float JUMP_SPEED = -.95f;
    public static final int STATE_JUMPING = 4;
    public static final int STATE_FALLING = 5;
    public boolean drawDebug;

    // additional animations
    private Animation idleLeft;
    private Animation idleRight;
    private Animation jumpRight;
    private Animation jumpLeft;
    private Animation fallRight;
    private Animation fallLeft;

    public int score;
    public int up1;
    public int up2;
    public int up3;
    public boolean win;
    public int maxHP;
    public int level;
    public int toNextLevel;

    public Player(
            Animation runLeft, Animation runRight,
            Animation deadLeft, Animation deadRight,
            Animation idleLeft, Animation idleRight,
            Animation jumpLeft, Animation jumpRight,
            Animation fallLeft, Animation fallRight
    )
    {
        super(runLeft, runRight, deadLeft, deadRight);
        this.idleLeft = idleLeft;
        this.idleRight = idleRight;
        this.jumpLeft = jumpLeft;
        this.jumpRight = jumpRight;
        this.fallLeft = fallLeft;
        this.fallRight = fallRight;
        newWidth = 80;
        newHeight = 64;
        score = 0;
        maxHP = 5;
        health = maxHP;
        damage = 1;
        jumped = false;
        isFalling = false;
        facingLeft = false;
        facingRight =true;
        DIE_TIME = 1500;
        onGround = true;
        win = false;
        speed = 0.5f;
        level = 1;
        exp = 0;
        toNextLevel = 10;
        drawDebug = false;

        up1 = 10;
        up2 = 20;
        up3 = 30;
        System.out.println("created player");
    }

    @Override
    public void update(long elapsedTime) {
        // select the correct Animation
        Animation newAnim = anim;
        // moving left
        if (getVelocityX() < 0 && getVelocityY() == 0) {
            newAnim = left;
            facingLeft = true;
            facingRight = false;
        }
        // moving right
        else if (getVelocityX() > 0 && getVelocityY() == 0) {
            newAnim = right;
            facingRight = true;
            facingLeft = false;
        }
        else if (state == STATE_NORMAL && onGround && getVelocityY() == 0){
            if(facingLeft) newAnim = idleLeft;
            else newAnim = idleRight;
        }
        // jumping ??
        else if (jumped) {
            if(facingLeft) newAnim = jumpLeft;
            else if(facingRight) newAnim = jumpRight;
            if(getVelocityY() > 0) {
//                setState(STATE_FALLING);
                isFalling = true;
                jumped = false;
            }
        }
        // falling ??
        else if (isFalling) {
            if(facingLeft) newAnim = fallLeft;
            else if(facingRight) newAnim = fallRight;
            if(getVelocityY() == 0){
//                setState(STATE_NORMAL);
                isFalling = false;
            }
        }


        if (state == STATE_DYING) {
            if(facingLeft) newAnim = deadLeft;
            else newAnim = deadRight;
        }

        // update the Animation
        if (anim != newAnim) {
            anim = newAnim;
            anim.start();
        } else {
            anim.update(elapsedTime);
        }

        // update to "dead" state
//        if(state != STATE_DYING) return;
        stateTime += elapsedTime;
        if (state == STATE_DYING && stateTime >= DIE_TIME) {
            setState(STATE_DEAD);
        }
    }

    @Override
    public Object clone() {
        // use reflection to create the correct subclass
        Constructor constructor = getClass().getConstructors()[0];
        try {
            Object o;
            o = constructor.newInstance(
                    (Animation) left.clone(),
                    (Animation) right.clone(),
                    (Animation) deadLeft.clone(),
                    (Animation) deadRight.clone(),
                    (Animation) idleLeft.clone(),
                    (Animation) idleRight.clone(),
                    (Animation) jumpLeft.clone(),
                    (Animation) jumpRight.clone(),
                    (Animation) fallLeft.clone(),
                    (Animation) fallRight.clone());
            return o;
        }
        catch (Exception ex) {
            // should never happen
            ex.printStackTrace();
            return null;
        }
    }


    public void collideHorizontal() {
        setVelocityX(0);
    }


    public void collideVertical() {
        // check if collided with ground
        if (getVelocityY() > 0) {
            onGround = true;
        }
        setVelocityY(0);
    }


    public void setY(float y) {
        // check if falling
        if (Math.round(y) > Math.round(getY())) {
            onGround = false;
        }
        super.setY(y);
    }


    public void wakeUp() {
        // do nothing
    }


    /**
        Makes the player jump if the player is on the ground or
        if forceJump is true.
    */
    public void jump(boolean forceJump) {
        if (onGround || forceJump) {
            onGround = false;
//            setState(STATE_JUMPING);
            jumped = true;
            setVelocityY(JUMP_SPEED);
        }
    }

    /**
        Makes the player FALL if the player is on the AIR or
        if forceFall is true.
    */
    public void fall(boolean forceFall) {
        if (!onGround || forceFall) {
            onGround = true;
            setVelocityY(-JUMP_SPEED);
        }
    }

    public float getMaxSpeed() {
        return this.speed;
    }

    public void grace(){
        this.x -= 15;
        this.y -= 30;
    }

    // player dies, reset score
    public void resetStats(){
        System.out.println("did u die? resetting stats.");
        this.score /= 2;
        this.health = maxHP;
//        this.state = STATE_NORMAL;
    }

    public void upgrade1(){
        this.maxHP += 3;
        this.damage += 1;
        health = maxHP;
        up1 = -1;
        System.out.println("upgraded player 1");
    }
    public void upgrade2(){
        this.maxHP += 2;
        this.damage += 1;
        this.speed += 0.1f;
        health = maxHP;
        up2 = -1;
        System.out.println("upgraded player 2");
    }
    public void upgrade3(){
        this.maxHP += 2;
        this.damage += 1;
        this.speed += 0.1f;
        health = maxHP;
        up3 = -1;
        System.out.println("upgraded player 3");
    }
    public void levelUp(){
        if(up1 == -1 && up2 == -1 && up3 == -1){
            toNextLevel += 10;
            level++;
            if(level % 2 == 0) damage++;
            else{
                maxHP++;
                health = maxHP;
            }
        }
        else return;
    }
    public ConcurrentHashMap debugString(){
        ConcurrentHashMap<String, String> s = new ConcurrentHashMap();
        s.put("x: ", Float.toString(x));
        s.put("y: ", Float.toString(y));
        s.put("xvel: ", Float.toString(getVelocityX()));
        s.put("yvel: ", Float.toString(getVelocityY()));
        s.put("onground: ", Boolean.toString(onGround));
        s.put("jumped: ", Boolean.toString(jumped));
        s.put("left: ", Boolean.toString(facingLeft));
        s.put("right: ", Boolean.toString(facingRight));
        s.put("state: ", (getStateString(state)));
        return s;
    }
    public String getStateString(int state){
        if(state == STATE_NORMAL) return "NORMAL";
        if(state == STATE_DYING) return "DYING";
        if(state == STATE_DEAD) return "DEAD";
        if(state == STATE_JUMPING) return "JUMPING";
        if(state == STATE_FALLING) return "FALLING";
        return "error";
    }
}
