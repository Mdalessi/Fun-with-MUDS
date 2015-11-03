/**
 *Author: Mike D'Alessio
 *Date: Spring 2015
 *File: Mobile.java
 */


import java.util.Random;

public class Mobile {
    
    //Current Health of player or monster
    public int health;
    
    //Current level of player or monster
    //Used to determine size of dice when rolling
    //for defense or attack.
    public int level;
    
    //Current speed of player or monster
    //Used to determine who goes first the player
    //or the monster.
    public int speed;

    public Mobile(int health, int level,int speed) {
      this.health=health;
      this.level=level;
      this.speed=speed;
    }
    
    
    public void alterHealth(int value){
        health+=value;
    }
        
   public int attackRoll(int aLevel, int dLevel){
       Random randomGenerator = new Random();
       if(aLevel>dLevel){
           int randomInt = randomGenerator.nextInt(10);
           return randomInt+1;
       }
       if(aLevel==dLevel){
           int randomInt = randomGenerator.nextInt(5);
           return randomInt+1;
       }
       if(dLevel>aLevel){
           int randomInt = randomGenerator.nextInt(3);
           return randomInt+1;
       }
       return 0;
   }
    
}
