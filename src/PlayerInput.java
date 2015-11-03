/**
 *Author: Mike D'Alessio
 *Date: Spring 2015
 *File: PlayerInput.java
 */



public class PlayerInput {
    //String containing the users' input
    public String input;
    
    //The player object of the current user
    public Player player;
    
    
    PlayerInput(Player player, String input){
        this.player=player;
        this.input=input;
    }
    
}
