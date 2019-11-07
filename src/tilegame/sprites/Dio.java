package tilegame.sprites;

import graphics.Animation;

import java.lang.reflect.Constructor;

/**
    A Fly is a Creature that fly slowly in the air.
*/
public class Dio extends Creature {

    private int id;
    protected int startNextSpawn;
    private static int idCounter = 0;
    private Animation attackLeft;
    private Animation attackRight;
    public boolean isEnraged;

    public Dio(
            Animation left, Animation right,
            Animation deadLeft, Animation deadRight,
            Animation attackLeft, Animation attackRight
    )
    {
        super(left, right, deadLeft, deadRight);
        this.attackLeft = attackLeft;
        this.attackRight = attackRight;
        damage = 3;
        newWidth = 80;
        newHeight = 64;
        id = idCounter;
        idCounter++;
        startNextSpawn = 10;
        health = 30;
        worth = health;
        tileID = '5';
        speed = 0.1f;
        isEnraged = false;
    }

    @Override
    public void update(long elapsedTime) {
        // select the correct Animation
        Animation newAnim = anim;
        if (getVelocityX() < 0) {
            if(isEnraged) newAnim = attackLeft;
            else newAnim = left;
        }
        else if (getVelocityX() > 0) {
            if(isEnraged) newAnim = attackRight;
            else newAnim = right;
        }
        if (state == STATE_DYING && newAnim == left) {
            newAnim = deadLeft;
        }
        else if (state == STATE_DYING && newAnim == right) {
            newAnim = deadRight;
        }

        // update the Animation
        if (anim != newAnim) {
            anim = newAnim;
            anim.start();
        }
        else {
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
            final Object o;
            o = constructor.newInstance((Animation) left.clone(),
                    (Animation) right.clone(),
                    (Animation) deadLeft.clone(),
                    (Animation) deadRight.clone(),
                    (Animation) attackLeft.clone(),
                    (Animation) attackRight.clone());
            return o;
        }
        catch (Exception ex) {
            // should never happen
            ex.printStackTrace();
            return null;
        }
    }

    public float getMaxSpeed() {
        return speed;
    }


    public boolean isFlying() {
        return isAlive();
    }

    public void upgrade(){
        this.health += 10;
        this.damage += 3;
        this.worth = this.health;
        this.speed += 0.1f;
        this.up = -1;
        System.out.println("upgraded DIO");
    }

    public void enrage(){
        this.health *= 2;
        this.damage *= 2;
        this.worth = this.health;
        this.speed *= 1.5;
        this.isEnraged = true;
    }

}
