package tilegame.sprites;

import graphics.Animation;

/**
    A Fly is a Creature that fly slowly in the air.
*/
public class Creep_Fly extends Creature {

    public Creep_Fly(Animation left, Animation right,
                     Animation deadLeft, Animation deadRight)
    {
        super(left, right, deadLeft, deadRight);
        damage = 1;
        newHeight = 48;
        newWidth = 64;
        health = 2;
        worth = health;
        tileID = '3';
        this.speed = 0.2f;
    }

    public float getMaxSpeed() {
        return speed;
    }

    public boolean isFlying() {
        return isAlive();
    }

    public void upgrade(){
        this.health += 1;
        this.damage += 1;
        this.worth = this.health;
        this.speed += 0.1f;
        this.up = -1;
        System.out.println("upgraded fly");
    }

}
