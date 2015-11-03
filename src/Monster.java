/**
 *Author: Mike D'Alessio
 *Date: Spring 2015
 *File: Monster.java
 */

import java.util.concurrent.ConcurrentLinkedQueue;



public class Monster extends Mobile{

    //Name of the monster
    public String name;
    
    //Description of the monster
    public String description;
    
    //The time it takes a monster to respawn after killed.
    public int respawnTime;
    
    //The current time till a monster is respawned--reset to respawnTime 
    //after reaches 0
    public int respawnTimer;
    
    //Health of the monster to return to after respawning
    public int respawnHealth;
    

    public Monster(int health, int level,int speed,String name, 
            String description,int respawnTimer, int respawnTime,
            int respawnHealth) {
        
        super(health,level,speed);//inherited from mobile
        this.name=name;
        this.description=description;
        this.respawnTimer=respawnTimer;
        this.respawnTime=respawnTime;
        this.respawnHealth=respawnHealth;
    }
}
