package tilegame.sprites;

import graphics.Animation;

/**
    A Fly is a Creature that fly slowly in the air.
*/
public class Creep_Zombie extends Creature {


    public Creep_Zombie(Animation left, Animation right,
                        Animation deadLeft, Animation deadRight)
    {
        super(left, right, deadLeft, deadRight);
        damage = 1;
        newWidth = 80;
        newHeight = 64;
        health = 4;
        worth = health;
        tileID = '4';
        this.speed = 0.06f;
    }

    public float getMaxSpeed() {
        return speed;
    }


    public boolean isFlying() {
        return isAlive();
    }

    public void upgrade(){
        this.health += 2;
        this.damage += 2;
        this.worth = this.health;
        this.speed += 0.05f;
        this.up = -1;
        System.out.println("upgraded zambee");
    }
}
