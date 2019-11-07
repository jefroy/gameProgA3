package tilegame.sprites;

import graphics.Animation;

import java.lang.reflect.Constructor;

/**
    The Player.
*/
public class Player extends Creature {

    public static final float JUMP_SPEED = -.95f;

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

        up1 = 10;
        up2 = 20;
        up3 = 30;
    }

    @Override
    public void update(long elapsedTime) {
        // select the correct Animation
        Animation newAnim = anim;
        // moving left
        if (getVelocityX() < 0 && getVelocityY() == 0 && onGround) { // try adding getVelocityY() == 0, this solved the yea
            newAnim = left;
        }
        // moving right
        else if (getVelocityX() > 0 && getVelocityY() == 0 && onGround) {
            newAnim = right;
        }
        else if (state == STATE_NORMAL && newAnim == left && onGround) {
            newAnim = idleLeft;
        }
        else if (state == STATE_NORMAL && newAnim == right && onGround) {
            newAnim = idleRight;
        }
        // jumping ??
        else if (getVelocityY() < 0) {
            if(newAnim == left || newAnim == idleLeft) newAnim = jumpLeft;
            else if(newAnim == right || newAnim == idleRight) newAnim = jumpRight;
            jumped = false;
//            System.out.println(
//                    "player  jump update:"
//                            + "\nvelX: " + getVelocityX()
//                            + "\nvelY: " + getVelocityY()
//                            + "\nstate: " + state
//            );
        }
        // falling ??
        else if (!onGround && getVelocityY() > 0) {
            if(newAnim == left || newAnim == idleLeft || newAnim == jumpLeft) newAnim = fallLeft;
            else if(newAnim == right || newAnim == idleRight || newAnim == jumpRight) newAnim = fallRight;
            onGround = true;
//            System.out.println(
//                    "player fall update:"
//                            + "\nvelX: " + getVelocityX()
//                            + "\nvelY: " + getVelocityY()
//                            + "\nstate: " + state
//            );

        }

        if (state == STATE_DYING) {
            if(newAnim == left || newAnim == idleLeft) newAnim = deadLeft;
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
//        this.score = 0;
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
}
