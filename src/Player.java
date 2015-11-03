/**
 *Author: Mike D'Alessio
 *Date: Spring 2015
 *File: Player.java
 */

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;



public class Player extends Mobile {
    
    //The Players name default == "Adventurer" 
    public String name;
    
    //Output Queue for a player
    public Queue<String> outputQ;
    
    //ClientThread handles all server data/activities
    public ClientThread client;
    
    //Flag to indicate if a user is in combat
    public boolean inComabat;
    
    //The Player's current room
    public Room currentRoom;
    
    //Used to determine how many more monster kills until a level up
    public int monstersTillLevel;
    
    //Current Max health for a player
    public int maxHealth;
    
    //Flag to indicate if the user completed the first quest
    public boolean completedFirstQuest;
    
    //Flag to indicate if the user completed the second quest
    public boolean completedSecondQuest;
    
    public Player(int health, int level,int speed,String name,ClientThread 
            client,Room currentRoom,boolean inCombat,int monstersTillLevel,
            int maxHealth,boolean completedFirstQuest,
            boolean completedSecondQuest) {
        
        super(health,level,speed);//inherited from mobile
        this.name = name; 
        this.client=client;
        this.currentRoom=currentRoom;
        this.outputQ = new ConcurrentLinkedQueue<String>();
        this.inComabat=inCombat;
        this.monstersTillLevel=monstersTillLevel;
        this.maxHealth=maxHealth;
        this.completedFirstQuest=completedFirstQuest;
        this.completedSecondQuest=completedSecondQuest;
    }
    

    public String toString(){
            return "Player(" + this.name + ")";
        }
    

    public void addToOutputQ(String output){
        this.outputQ.add(output);
    }
}