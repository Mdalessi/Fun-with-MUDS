/**
 *Author: Mike D'Alessio
 *Date: Spring 2015
 *File: GameServer.java
 */


import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;


public class GameServer extends Thread {
    
    //Players input Queue
    private Queue<PlayerInput> inputQ;
    
    //Server object
    private Server s;
    
    //World data(the current world is created in this class)
    private WorldBuilder worldBuild;
    
    //World object containing all the rooms
    private World world;

   
    GameServer(Queue inputQ,Server s){
        world=worldBuild.create();//create the world from worldBuilder
        this.inputQ=inputQ;
        this.s=s;
    }
    

    
    public void run(){
        while(true){
            
            try {
                this.processInput();//parse commands
                this.processOutput();//send output back to users'
                //respawn monsters that have been killed
                this.processRespawn(world);

            } catch (IOException ex) {
                Logger.getLogger(GameServer.class.getName()).log(Level.SEVERE,
                        null, ex);
            }
            
            try {
                
                //This is a game 'tick',a user should not be able to send 1000 
                //commands before another user has inputed one command. 
                //Therefore a tick is needed to keep the game world updated 
                //at the same rate for all players.
                Thread.sleep(500); //half a second
            } catch (InterruptedException ex) {
                Logger.getLogger(GameServer.class.getName()).log(Level.SEVERE, 
                        null, ex);
            }
        }
    }
    
        
    private void processInput() throws IOException{
        
        //The object holding the players input 
        //to be parsed(player,input)
        PlayerInput pi;
        
        //Input string from the player
        String input;
        
        //Current player attached to the input
        Player p;
        

        while(!this.inputQ.isEmpty()){//while the input Queue is not empty
            
            pi=this.inputQ.poll();//Retrieves and removes the head of this queue
            input=pi.input.replace("\n","");
            p=pi.player;//player is pulled from the input
            
            String[] tokens = input.split(" ");//split words by spaces
            
            StringTokenizer st = new StringTokenizer(input);//tokenize the input

            Command cmd = new Command();
            //Make sure that you have atleast one input command
            if(tokens.length > 0){
                cmd.name = tokens[0];
            }

            if(cmd.name.equals("who")){//if the input is a who command
                //loop through all clients
                for(ClientThread c : this.s.clientList ){
                    //output to player all other players
                    p.addToOutputQ(TelnetColors.COLOR_WHITE+c.player.name);
                }
                //Print prompt
                p.addToOutputQ("Prompt "+TelnetColors.COLOR_WHITE+p.name+"/"+
                        TelnetColors.COLOR_RED+
                        Integer.toString(p.health)+
                        "/"+TelnetColors.COLOR_CYAN+Integer.toString(p.level)
                        +"/"+TelnetColors.COLOR_CYAN+Integer.toString(p.speed)+
                        TelnetColors.COLOR_LIGHTGREEN+"  >");
            }
            
            else if(cmd.name.equals("help")){//if the input is a help command
                
                //Print how to use every command.
                p.addToOutputQ(TelnetColors.COLOR_WHITE+"Welcome to "
                        + "Mike D'Alessio MUD--if you aren't famliar with"
                        + " the general flow of"
                        + " a MUD you can always call up this menu with the "
                        + "'help' command.");
                
                p.addToOutputQ(TelnetColors.COLOR_WHITE+"The 'who' command lists"
                        + " all the people playing the MUD right now which can"
                        + " be "+ "helpful to you to try and make a team to try"
                        + " and take down the dreadlord.");
                
                p.addToOutputQ(TelnetColors.COLOR_WHITE+"The 'talk' command "
                        + "followed by a sentence allows you to communicatie "
                        + "with other"
                        + " people in the game!");
                
                p.addToOutputQ(TelnetColors.COLOR_WHITE+"You can use any "
                        + "direction(north,south,etc) to move around the "
                        + "game world.");
                
                p.addToOutputQ(TelnetColors.COLOR_WHITE+"You can use the "
                        + "'attack' command to initiate an attack during "
                        + "combat.");
                
                p.addToOutputQ(TelnetColors.COLOR_WHITE+"Use the 'look' command"
                        + " to describe your current enviroment in the game.");
                
                p.addToOutputQ(TelnetColors.COLOR_WHITE+"If you haven't already"
                        + " done it you can use the 'name' command if "
                        + "your in whitebridge to rename your character.");
                
                p.addToOutputQ(TelnetColors.COLOR_WHITE+"Good luck hero!");
                
                //print prompt
                 p.addToOutputQ("Prompt "+TelnetColors.COLOR_WHITE+p.name+"/"+
                         TelnetColors.COLOR_RED+Integer.toString(p.health)+"/"+
                         TelnetColors.COLOR_CYAN+Integer.toString(p.level)+"/"+
                         TelnetColors.COLOR_CYAN+Integer.toString(p.speed)+
                         TelnetColors.COLOR_LIGHTGREEN+"  >");
                       
            }
            
            else if(cmd.name.equals("talk")){//if the input is a talk command
                
                //Copy everything after 'talk'
                String[] cmdArr = Arrays.copyOfRange(tokens,1,tokens.length);
                StringBuilder builder = new StringBuilder();
                
                //for every string in the array
                for(String s : cmdArr) {
                    //add it to the builder
                    builder.append(s + " ");
                }
                //make a string out of the builder
                String star = builder.toString();
                
                //For all clients
                for(ClientThread c : this.s.clientList ){
                    //Broadcast to all players
                    c.player.addToOutputQ(TelnetColors.COLOR_WHITE+"\n[TALK " +
                            p.name + "]: "+TelnetColors.COLOR_LIGHTBLUE + star);
                    //Prompt
                    c.player.addToOutputQ("Prompt "+TelnetColors.COLOR_WHITE+
                            p.name+"/"+TelnetColors.COLOR_RED+
                            Integer.toString(p.health)+ "/"+
                            TelnetColors.COLOR_CYAN+Integer.toString(p.level)
                            +"/"+ TelnetColors.COLOR_CYAN+
                            Integer.toString(p.speed)+
                            TelnetColors.COLOR_LIGHTGREEN+"  >");
                    
                }
            }
            
            else if(cmd.name.equals("north")){//if the input is a north command
                
                if (p.inComabat==false){
                    //try to move the character north
                    leave(p, Direction.NORTH); 
                }
                else{
                    //you can't run from combat
                    p.addToOutputQ(TelnetColors.COLOR_RED+"You can't "
                            + "leave you are in the middle "
                            + "of a fight!");
                    
                    //prompt
                    p.addToOutputQ("Prompt "+TelnetColors.COLOR_WHITE+p.name+"/"+
                            TelnetColors.COLOR_RED+Integer.toString(p.health)+
                             "/"+TelnetColors.COLOR_CYAN+
                            Integer.toString(p.level)+"/"+
                            TelnetColors.COLOR_CYAN+Integer.toString(p.speed)
                            +TelnetColors.COLOR_LIGHTGREEN+"  >");
                }
            }

            else if(cmd.name.equals("south")){//if the input is a south command
                if (p.inComabat==false){
                    //try to move the character south
                    leave(p, Direction.SOUTH);
                }
                else{
                    //you can't run from combat
                    p.addToOutputQ(TelnetColors.COLOR_RED+"You can't "
                            + "leave you are in the middle "
                            + "of a fight!");
                    
                    //prompt
                    p.addToOutputQ("Prompt "+TelnetColors.COLOR_WHITE+p.name+"/"
                            +TelnetColors.COLOR_RED+Integer.toString(p.health)+
                            "/"+TelnetColors.COLOR_CYAN+
                            Integer.toString(p.level)+"/"
                            +TelnetColors.COLOR_CYAN+
                            Integer.toString(p.speed)+
                            TelnetColors.COLOR_LIGHTGREEN+"  >");
                }
            }
            else if(cmd.name.equals("west")){//if the input is a west command
                if (p.inComabat==false){
                    //try to move the character west
                    leave(p, Direction.WEST);
                }
                else{
                    //you can't run from combat
                    p.addToOutputQ(TelnetColors.COLOR_RED+"You can't "
                            + "leave you are in the middle "
                            + "of a fight!");
                    
                    //prompt
                    p.addToOutputQ("Prompt "+TelnetColors.COLOR_WHITE+p.name+"/"+
                            TelnetColors.COLOR_RED+Integer.toString(p.health)+
                            "/"+TelnetColors.COLOR_CYAN
                            +Integer.toString(p.level)+"/"+
                            TelnetColors.COLOR_CYAN+
                            Integer.toString(p.speed)+
                            TelnetColors.COLOR_LIGHTGREEN+"  >");
                }
            }
            else if(cmd.name.equals("east")){//if the input is a east command
                if (p.inComabat==false){
                    //try to move the character east
                    leave(p, Direction.EAST);
                }
                else{
                    //you can't run from combat
                    p.addToOutputQ(TelnetColors.COLOR_RED+"You can't "
                            + "leave you are in the middle "
                            + "of a fight!");
                    
                    //prompt
                    p.addToOutputQ("Prompt "+TelnetColors.COLOR_WHITE+p.name+"/"
                            +TelnetColors.COLOR_RED+Integer.toString(p.health)+
                            "/"+TelnetColors.COLOR_CYAN+
                            Integer.toString(p.level)+"/"+
                            TelnetColors.COLOR_CYAN+
                            Integer.toString(p.speed)+
                            TelnetColors.COLOR_LIGHTGREEN+"  >");
                }
            }
            else if(cmd.name.equals("look")){//if the input is a look command
                
                //if there is a monster in the room
                if(p.currentRoom.monster!=null){
                    
                    //if the monster is alive
                    if(p.currentRoom.monster.health>0){
                        
                        //Print description and alert the user to the fight.
                        p.addToOutputQ(TelnetColors.COLOR_PURPLE
                                +p.currentRoom.description);//output description
                        p.addToOutputQ(TelnetColors.COLOR_RED+
                                "You are in deadly combat with: "+
                                p.currentRoom.monster.name+" who "+
                                        p.currentRoom.monster.description);
                        
                        //prompt
                        p.addToOutputQ("Prompt "+TelnetColors.COLOR_WHITE
                                +p.name+"/"+TelnetColors.COLOR_RED+
                                Integer.toString(p.health)+"/"+
                                TelnetColors.COLOR_CYAN+
                                Integer.toString(p.level)+"/"
                                +TelnetColors.COLOR_CYAN+
                                Integer.toString(p.speed)+
                                TelnetColors.COLOR_LIGHTGREEN+"  >");
                    }
                    else{
                        
                        //Monster is dead, alert the user they can move on
                        p.addToOutputQ(TelnetColors.COLOR_PURPLE+
                                p.currentRoom.description);//output description
                        p.addToOutputQ(TelnetColors.COLOR_GREY+"The bones"
                                + " of the defeated "+p.currentRoom.
                                monster.name+" lay at your feet"
                                + " continue on with your adventure.");
                        
                        //prompt
                        p.addToOutputQ("Prompt "+TelnetColors.COLOR_WHITE
                                +p.name+"/" +TelnetColors.COLOR_RED+
                                Integer.toString(p.health)+"/"+
                                TelnetColors.COLOR_CYAN+
                                Integer.toString(p.level)+"/"
                                +TelnetColors.COLOR_CYAN+
                                Integer.toString(p.speed)+
                                TelnetColors.COLOR_LIGHTGREEN+"  >");
                    }

                }
                else{
                    //No monster give the user a description of the room
                    p.addToOutputQ(TelnetColors.COLOR_PURPLE+
                            p.currentRoom.description);
                    
                    //prompt
                    p.addToOutputQ("Prompt "+TelnetColors.COLOR_WHITE+p.name+"/"
                            +TelnetColors.COLOR_RED+Integer.toString(p.health)+
                            "/"+TelnetColors.COLOR_CYAN+
                            Integer.toString(p.level)+"/"
                            +TelnetColors.COLOR_CYAN+
                            Integer.toString(p.speed)+
                            TelnetColors.COLOR_LIGHTGREEN+"  >");
                }
            }
            //if the input is a attack command
            else if(cmd.name.equals("attack")){
                
                //if its the user's turn to attack and they are in combat
                if (p.currentRoom.userTurn==true&&p.inComabat==true){
                    
                    //roll for the user, based on the current level of
                    //the hero and the monsters level
                    int userRoll=p.attackRoll(p.level, 
                            p.currentRoom.monster.level);
                    
                    //subtract health from monster
                    p.currentRoom.monster.alterHealth(0-userRoll);
                    
                    //Output the user the result of the attack
                    p.addToOutputQ(TelnetColors.COLOR_YELLOW+"Nice attack! "
                            + "You hit for "+Integer.toString(userRoll));
                    
                    //if the monster is defeated
                    if(p.currentRoom.monster.health<=0){
                        
                        //Output a celebration to the user!
                        p.addToOutputQ(TelnetColors.COLOR_GREEN+"You did it! "
                                + "The "+p.currentRoom.monster.name +
                                " has been defeated, you may continue your "
                                + "adventure. ");
                        
                        //prompt
                        p.addToOutputQ("Prompt "+TelnetColors.COLOR_WHITE
                                +p.name+"/"+TelnetColors.COLOR_RED+
                                Integer.toString(p.health)+
                                "/"+TelnetColors.COLOR_CYAN+
                                Integer.toString(p.level)+"/"
                                +TelnetColors.COLOR_CYAN+
                                Integer.toString(p.speed)+
                                TelnetColors.COLOR_LIGHTGREEN+"  >");
                        
                        p.inComabat=false;//player no longerin combat
                        //player is one monster closer to leveling
                        p.monstersTillLevel-=1;
                        
                        //If its a special monster defeated
                        if(p.currentRoom.monster.name=="Goblin King"){
                            //completed the first quest.
                            p.completedFirstQuest=true;
                        }
                        //If its a special monster defeated
                         if(p.currentRoom.monster.name=="DreadLord"){
                            //completed the second quest
                            p.completedSecondQuest=true;
                        }
                        //If the user is ready to reach the next level
                        if(p.monstersTillLevel==0){
                            //generate a random number
                            Random randomGenerator = new Random();
                            
                            p.level+=1;//add a level
                            //add speed between 1-2
                            p.speed+=(randomGenerator.nextInt(2)+1);
                            //You gained more health between 1-10
                            p.maxHealth+=(randomGenerator.nextInt(10)+1);
                            
                            p.health=p.maxHealth;//set current health to max
                            
                            //Output the good news to the user
                            p.addToOutputQ(TelnetColors.COLOR_WHITE+"You leveled"
                                    + " up Adventurer! Congratulations your new "
                                    + "level is "+Integer.toString(p.level)+ 
                                    " with a speed of " +
                                    Integer.toString(p.speed) + " and max"
                                    + " hitpoints at " +
                                    Integer.toString(p.health));
                            
                            //reset the monsters till the next level
                            p.monstersTillLevel=(p.level+2);
                            
                            //prompt
                            p.addToOutputQ("Prompt "+TelnetColors.COLOR_WHITE+
                                    p.name+"/"+TelnetColors.COLOR_RED+
                                    Integer.toString(p.health)+
                                    "/"+TelnetColors.COLOR_CYAN+
                                    Integer.toString(p.level)
                                    +"/"+TelnetColors.COLOR_CYAN+
                                    Integer.toString(p.speed)+
                                    TelnetColors.COLOR_LIGHTGREEN+"  >");
                        }
                        
                    }
                    else{
                        //roll for the monster, based on the current level
                        // of the hero and the monsters level
                        int monsterRoll=p.attackRoll(p.level,
                                p.currentRoom.monster.level);
                        
                        //subtract health from the user for the hit
                        p.alterHealth(0-monsterRoll);
                        
                        //output to the user the result of the defense.
                        p.addToOutputQ(TelnetColors.COLOR_RED+"Ouch!"
                                + " You got hit for "+
                                Integer.toString(monsterRoll)+" dammage.");
                        
                        if(p.health<=0){//the player has died
                            
                            //Output the bad news
                            p.addToOutputQ(TelnetColors.COLOR_BGWHITE+
                                    TelnetColors.COLOR_BLACK+"You failed the"
                                    + " people of whitebridge, and been"
                                    + " defeated by the forces of evil, leaving"
                                    + " us defenseless against the Dreadlord."
                                    + "Lucky for you the town mystic just got"
                                    + " back into town and she has some "
                                    + "experience with the dark arts of "
                                    + "necromancy. We need you to defeat the "
                                    + "Dreadlord!"
                                    +TelnetColors.COLOR_BGDEFAULT);
                            
                            //respawn in the startroom
                            p.currentRoom=world.startRoom;
                            p.health=p.maxHealth;
                            
                            //prompt
                            p.addToOutputQ("Prompt "+TelnetColors.COLOR_WHITE
                                    +p.name+"/"+TelnetColors.COLOR_RED+
                                    Integer.toString(p.health)+
                                    "/"+TelnetColors.COLOR_CYAN+
                                    Integer.toString(p.level)+
                                    "/"+TelnetColors.COLOR_CYAN+
                                    Integer.toString(p.speed)+
                                    TelnetColors.COLOR_LIGHTGREEN+"  >");
                                    }
                        else{
                            
                            //You are still alive after the hit,
                            // tell the player to keep fighting!
                            p.addToOutputQ(TelnetColors.COLOR_WHITE+"Get back "
                                    + "up, and start doing some damage!");
                            
                            //prompt
                            p.addToOutputQ("Prompt "+TelnetColors.COLOR_WHITE
                                    +p.name+"/"+TelnetColors.COLOR_RED+
                                    Integer.toString(p.health)+
                                    "/"+TelnetColors.COLOR_CYAN+
                                    Integer.toString(p.level)+
                                    "/"+TelnetColors.COLOR_CYAN+
                                    Integer.toString(p.speed)+
                                    TelnetColors.COLOR_LIGHTGREEN+"  >");
                            }
                        }
                }
                else{
                    
                    //No monster in this room or the mosnter has been defeated.
                    //tell the user that they cannot attack anything.
                    p.addToOutputQ(TelnetColors.COLOR_WHITE+"Nothing for you "
                            + "to attack! ");
                    
                    //prompt
                    p.addToOutputQ("Prompt "+TelnetColors.COLOR_WHITE+p.name+
                            "/"+TelnetColors.COLOR_RED+
                            Integer.toString(p.health)+
                            "/"+TelnetColors.COLOR_CYAN+
                            Integer.toString(p.level)+
                            "/"+TelnetColors.COLOR_CYAN+
                            Integer.toString(p.speed)+
                            TelnetColors.COLOR_LIGHTGREEN+"  >");
                }
            }
            //if the name is a attack command and the user is in the 
            //startroom (game logic requirement)
            else if(cmd.name.equals("name") && p.currentRoom.id=="start"){
                
                //make an array of everything after 'name'
                String[] cmdArr = Arrays.copyOfRange(tokens,1,tokens.length);
                StringBuilder builder = new StringBuilder();
                
                //for every string in the arry
                for(String s : cmdArr) {
                    //add it to builder
                    builder.append(s + " ");
                }
                //create the string
                String star = builder.toString();
                
                ////If its the first time changing names,and its not a long name
                if(p.name.equals("Adventurer") && star.length()<30){
                    
                    //get rid of leading and trailing blank space
                    p.name=star.trim();
                    
                    //output to the user that his name has been changed
                     p.addToOutputQ(TelnetColors.COLOR_BLUE+p.name + " it is, "
                             + "glad to have you on our side. "
                             + "Now go explore and help these town's "
                            + "people by defeating the local dreadlord." );
                     
                     //prompt
                     p.addToOutputQ("Prompt "+TelnetColors.COLOR_WHITE+p.name+
                             "/"+TelnetColors.COLOR_RED+
                             Integer.toString(p.health)+
                             "/"+TelnetColors.COLOR_CYAN+
                             Integer.toString(p.level)+
                             "/"+TelnetColors.COLOR_CYAN+
                             Integer.toString(p.speed)+
                             TelnetColors.COLOR_LIGHTGREEN+"  >");
                }
                //if the name is not too long
                else if( star.length()>=30){
                    
                    //output to the player that they should try another name
                    p.addToOutputQ(TelnetColors.COLOR_BLUE+"The people will "
                            + "never remember such a long name!"
                            + "Choose a shorter one and ill tell Whitebridge.");
                    
                    //prompt
                    p.addToOutputQ("Prompt "+TelnetColors.COLOR_WHITE+p.name+
                            "/"+TelnetColors.COLOR_RED+
                            Integer.toString(p.health)+
                            "/"+TelnetColors.COLOR_CYAN+
                            Integer.toString(p.level)+
                            "/"+TelnetColors.COLOR_CYAN+
                            Integer.toString(p.speed)+
                            TelnetColors.COLOR_LIGHTGREEN+"  >");
                }
                //if the player has already changed names
                else{
                    
                    //output to the user that he is already known 
                    //as something else
                     p.addToOutputQ(TelnetColors.COLOR_BLUE+"The people "
                             + "already know you as, "+
                             p.name+", you can't expect them to learn a new one"
                             + "just cause you feel like it!");
                     
                     //prompt
                     p.addToOutputQ("Prompt "+TelnetColors.COLOR_WHITE+p.name+
                             "/"+TelnetColors.COLOR_RED+
                             Integer.toString(p.health)+
                             "/"+TelnetColors.COLOR_CYAN+
                             Integer.toString(p.level)+
                             "/"+TelnetColors.COLOR_CYAN+
                             Integer.toString(p.speed)+
                             TelnetColors.COLOR_LIGHTGREEN+"  >");
   
                }
            }
            //command could not be parsed correctly or the user had a typo      
            else{
                
                //output to the user his/her mistake
                p.addToOutputQ(TelnetColors.COLOR_LIGHTRED+"Unkown Command");
                
                //prompt
                p.addToOutputQ("Prompt "+TelnetColors.COLOR_WHITE+p.name+
                             "/"+TelnetColors.COLOR_RED+
                              Integer.toString(p.health)+
                             "/"+TelnetColors.COLOR_CYAN+
                              Integer.toString(p.level)+
                             "/"+TelnetColors.COLOR_CYAN+
                              Integer.toString(p.speed)+
                              TelnetColors.COLOR_LIGHTGREEN+"  >");
                
            }
            System.out.println(pi.input);
        }
    }

    private void processOutput() throws IOException{
        //for everyclient connected
        for(ClientThread c : this.s.clientList ){
            //while there are some clients still connected
           while(!c.player.outputQ.isEmpty()){
               
               ////Retrieves and removes the head of this queue
               String msg = c.player.outputQ.poll();
               String arr[] = msg.split(" ", 2);//split word by spaces
               System.out.println(arr[0]);//test print
               
               if (arr[0].equals("Prompt")){//if its a prompt message
                   
                   c.sendPrompt(arr[1]);//outprint prompt without '/n'
               }
               else{
                   
                    //else its a string, output it to the player
                    c.sendMsg(msg);
               }
            
           }
        }
    }


    public void newClient(ClientThread client) throws IOException{
        
        //spawn player with base stats, and no completed quests
        Player p=new Player(100,1,1,"Adventurer", client,world.startRoom,
                false,3,100,false,false);
        
        //setup client thread 
        client.player=p;
        p.client=client;

        //Game intro to the user
        p.addToOutputQ(TelnetColors.COLOR_WHITE+ "Welcome to a world of"
                + " fantasy and adventure. Your story starts in a small town, "
                + "thats been battling the forces of evil since before the "
                + "creation of the Empire.They will need your help, your "
                + "strength, and most importantly your sword arm. "
                + "Can you be their savior or will you let the forces of "
                + "darkness win and destroy the people of Whitebridge.");
        
        p.addToOutputQ(TelnetColors.COLOR_BGDEFAULT+
                TelnetColors.COLOR_NONE+
                "------------------------------------------");
        p.addToOutputQ(TelnetColors.COLOR_WHITE +" The Carriage Driver yells"
                + " over the hustle and bustle of horses and townsfolk: "
                + TelnetColors.COLOR_BLUE+"'You hit your head pretty hard, "
                + "on the way to town, and could not tell us your name. "
                + "When you get back to town,"
                + "use the 'name' command to tell us your adventurer name. "
                + "Be careful you only get one chance at letting "
                + "the people know!");
        
        
        //prompt
        p.addToOutputQ("Prompt "+TelnetColors.COLOR_WHITE+p.name+""
                + "/"+TelnetColors.COLOR_RED+Integer.toString(p.health)+
                "/"+TelnetColors.COLOR_CYAN+Integer.toString(p.level)+
                "/"+TelnetColors.COLOR_CYAN+Integer.toString(p.speed)+
                TelnetColors.COLOR_LIGHTGREEN+"  >");
        
        //spawn the player
        enterRoom(p.name,p.currentRoom);
    }
    

    public void enterRoom(String playerName, Room roomEntered){
        
        //for all clients
        for(ClientThread c : this.s.clientList ){
            
            //if the current client is in the room
            if(c.player.currentRoom==roomEntered && c.player.name!=playerName){
                
                //output his/her arrival into the room
                c.player.addToOutputQ(TelnetColors.COLOR_LIGHTBLUE+
                        "A new adventurer," + playerName +
                        ", has entered the "+ c.player.currentRoom.id + 
                        " room.");
                
                //prompt
                c.player.addToOutputQ("Prompt "+TelnetColors.COLOR_WHITE+
                        c.player.name+"/"+TelnetColors.COLOR_RED+
                        Integer.toString(c.player.health)+
                        "/"+TelnetColors.COLOR_CYAN+
                        Integer.toString(c.player.level)+
                        "/"+TelnetColors.COLOR_CYAN+
                        Integer.toString(c.player.speed)+
                        TelnetColors.COLOR_LIGHTGREEN+"  >");
            }
            //if there is a monster in the room
            if(c.player.name==playerName && roomEntered.monster!=null){
                
                //and the monster is still alive
                if(roomEntered.monster.health>0){
                    
                    //start the battle logic!
                    c.player.addToOutputQ("Battle Time!");
                    
                    c.player.inComabat=true;//the player is in combat
                    
                    //if the players speed is faster than the monsters
                    if(c.player.speed>= c.player.currentRoom.monster.speed){
                        
                        //User attacks first
                        c.player.addToOutputQ(TelnetColors.COLOR_YELLOW+"You "
                                + "are faster than the "+
                                c.player.currentRoom.monster.name+
                                " you attack first!");
                                
                                //it is now the user turn
                                c.player.currentRoom.userTurn=true;
                }
                    else{
                        //if the monsters speed is faster than the players
                        //monster attacks first
                        c.player.addToOutputQ(TelnetColors.COLOR_YELLOW+"You "
                                + "are slower than the "+
                                c.player.currentRoom.monster.name+" they "
                                + "attack first!");
                                
                                //the user can attack now
                                c.player.currentRoom.userTurn=true;
                                
                         //roll for the monster, based on the current level
                        // of the hero and the monsters level
                        int monsterRoll=c.player.attackRoll(c.player.level,
                                c.player.currentRoom.monster.level);
                        
                        //subtract the hit from the user's health
                        c.player.alterHealth(0-monsterRoll);
                        
                        //output the user the result of the defense
                        c.player.addToOutputQ(TelnetColors.COLOR_RED+"Ouch! "
                                + "You got hit for "+
                                Integer.toString(monsterRoll)+
                                " dammage.");
                        
                        //if the user died in battle
                        if(c.player.health<=0){
                            
                            //output him the result of his/her defeat
                            c.player.addToOutputQ(TelnetColors.COLOR_BGWHITE+
                                    TelnetColors.COLOR_BLACK+"You failed the "
                                    + "people of whitebridge, and been defeated"
                                    + "by the forces of evil, leaving us "
                                    + "defenseless against the Dreadlord."
                                    + "Lucky for you the town mystic just "
                                    + "got back into town and she has some "
                                    + "experience with the dark arts of "
                                    + "necromancy. We need you to defeat the "
                                    + "Dreadlord!"+
                                    TelnetColors.COLOR_BGDEFAULT);
                            
                            //spawn in the start room
                            c.player.health=c.player.maxHealth;
                            c.player.currentRoom=world.startRoom;
                                    }
                        else{
                            //user was not defeated from the hit
                            c.player.addToOutputQ(TelnetColors.COLOR_WHITE+
                                    "Get back up, and start "
                                    + "doing some damage!");
                           
                            }
                    }
                    //prompt
                    c.player.addToOutputQ("Prompt "+TelnetColors.COLOR_WHITE
                            + c.player.name+"/"+TelnetColors.COLOR_RED+
                            Integer.toString(c.player.health)+
                            "/"+TelnetColors.COLOR_CYAN+
                            Integer.toString(c.player.level)+
                            "/"+TelnetColors.COLOR_CYAN+
                            Integer.toString(c.player.speed)+
                            TelnetColors.COLOR_LIGHTGREEN+"  >");
                }
            }
            
            //if the user has completed the first quest
            //and they are back in the startroom
            if(c.player.completedFirstQuest==true && 
                    c.player.name==playerName && 
                    c.player.currentRoom.id.equals("start")){
                
                //output to the user a small celebration!
                c.player.addToOutputQ(TelnetColors.COLOR_WHITE+"The Mayor "
                        + "proclaims:" +TelnetColors.COLOR_BLUE+
                        "You did it Adventurer! You killed the Dreadlord's "
                        + "lieutenant and you gained more strength. I think "
                        + "you are ready to "
                        + "take on the Dreadlord himself. His forces and him "
                        + "are located South of here "
                        + "in a the forest with many twists and turns, "
                        + "be careful his forces are weakned,"
                        + "but he is still very strong. Good Luck. ");
                
                //prompt
                c.player.addToOutputQ("Prompt "+TelnetColors.COLOR_WHITE+ 
                        c.player.name+"/"+TelnetColors.COLOR_RED+
                        Integer.toString(c.player.health)+
                        "/"+TelnetColors.COLOR_CYAN+
                        Integer.toString(c.player.level)+
                        "/"+TelnetColors.COLOR_CYAN+
                        Integer.toString(c.player.speed)+
                        TelnetColors.COLOR_LIGHTGREEN+"  >");
            }
            
            //if the user has completed the second quest and they
            //are back in the startroom(this is the end of the story)
             if(c.player.completedSecondQuest==true && 
                     c.player.name==playerName &&
                     c.player.currentRoom.id.equals("start")){
                 
                 //output to the user a big celebration
                c.player.addToOutputQ(TelnetColors.COLOR_WHITE+"The Mayor and "
                        + "townsfolk proclaims:" +TelnetColors.COLOR_BLUE+
                        "Cheers to the hero of whitebridge! We present you "
                        + "with the greatest gift that our town has, The "
                        + "sword of a thousand truths it shall give you great"
                        + " power to defeat any foe; it is yours.");
                
                //and some jokes!
                c.player.addToOutputQ(TelnetColors.COLOR_YELLOW+"You Respond:"+
                        TelnetColors.COLOR_BLUE+" Why didn't you give this to "
                        + "me from the start of my quest? You almost got me "
                        + "killed! and you had this the whole time!? "
                        + "You people are crazy I'm leaving...with the sword!");
                
                //make the player super strong
                c.player.maxHealth=500;
                c.player.health=500;
                c.player.level=10;
                c.player.speed=25;
                
                //output credits
                c.player.addToOutputQ(TelnetColors.COLOR_WHITE+"THANKS for "
                        + "playing! Check back for updates and more "
                        + "quests for our heros.Hope you enjoyed playing it as"
                        + " much as I loved building it--Mike D'Alessio 2015.");
                
                //prompt the user can keep playing after the story is finished
                c.player.addToOutputQ("Prompt "+TelnetColors.COLOR_WHITE+
                        c.player.name+"/"+TelnetColors.COLOR_RED+
                        Integer.toString(c.player.health)+"/"+
                        TelnetColors.COLOR_CYAN+
                        Integer.toString(c.player.level)+
                        "/"+TelnetColors.COLOR_CYAN+
                        Integer.toString(c.player.speed)+
                        TelnetColors.COLOR_LIGHTGREEN+"  >");
                
            }
        }
        
    }
       
     public void leave(Player player, Direction direction){
        //the room the player is trying to travel to
        Room otherRoom;
        
        synchronized (this) {
            //get the room connect in the direction the user wants to move
            otherRoom = player.currentRoom.connectedRooms.get(direction);
            
            if (otherRoom == null) {//if the room does not exist
                //tell the user they hit a wall
                player.addToOutputQ(TelnetColors.COLOR_WHITE+"Whoops, "
                        + "you hit a wall try another direction.");
                
                //prompt
                player.addToOutputQ("Prompt "+TelnetColors.COLOR_WHITE+
                        player.name+"/"+TelnetColors.COLOR_RED+
                        Integer.toString(player.health)+"/"+
                        TelnetColors.COLOR_CYAN+Integer.toString(player.level)+
                        "/"+TelnetColors.COLOR_CYAN+
                        Integer.toString(player.speed)+
                        TelnetColors.COLOR_LIGHTGREEN+"  >");
                return;
                
            } else {
                //if the player has not changed their name
                if(player.name=="Adventurer"){
                    //dont let them leave the town,and output this to the player
                    player.addToOutputQ(TelnetColors.COLOR_WHITE+"You should"
                            + " name your character with the 'name' "
                            + " command before you leave.");
                    
                    //prompt
                    player.addToOutputQ("Prompt "+TelnetColors.COLOR_WHITE+
                            player.name+"/"+TelnetColors.COLOR_RED+
                            Integer.toString(player.health)+"/"+
                            TelnetColors.COLOR_CYAN+
                            Integer.toString(player.level)+"/"+
                            TelnetColors.COLOR_CYAN+
                            Integer.toString(player.speed)+
                            TelnetColors.COLOR_LIGHTGREEN+"  >");
                }
                //if the player is ready to face the boss level
                else if(player.completedFirstQuest==true &&
                        otherRoom.id=="forest entrance"){
                    
                    //output the travel output to the user
                    player.addToOutputQ(TelnetColors.COLOR_WHITE+"You are"
                            + " traveling!");
                    player.currentRoom=otherRoom;
                    player.addToOutputQ(TelnetColors.COLOR_PURPLE+"You"
                            + " have a look around: "+
                            player.currentRoom.description);
                    
                    //prompt
                    player.addToOutputQ("Prompt "+TelnetColors.COLOR_WHITE+
                            player.name+"/"+TelnetColors.COLOR_RED+
                            Integer.toString(player.health)+"/"+
                            TelnetColors.COLOR_CYAN+
                            Integer.toString(player.level)+
                            "/"+TelnetColors.COLOR_CYAN+
                            Integer.toString(player.speed)+
                            TelnetColors.COLOR_LIGHTGREEN+"  >");
                    
                    //enter the room
                    enterRoom(player.name,player.currentRoom);
                }
                //if the player is not ready for the boss level
                else if(player.completedFirstQuest==false &&
                        otherRoom.id=="forest entrance"){
                    
                    //output to the user that they should finish the first quest
                    player.addToOutputQ(TelnetColors.COLOR_WHITE+"The Mayor "
                            + "says:" +TelnetColors.COLOR_BLUE+" 'Aren't you "
                            + "a little eager to take on the dreadlord? "
                            + "Deal with the goblin king first!'");
                    
                    //prompt
                    player.addToOutputQ("Prompt "+TelnetColors.COLOR_WHITE+
                            player.name+"/"+TelnetColors.COLOR_RED+
                            Integer.toString(player.health)+
                            "/"+TelnetColors.COLOR_CYAN+
                            Integer.toString(player.level)+
                            "/"+TelnetColors.COLOR_CYAN+
                            Integer.toString(player.speed)+
                            TelnetColors.COLOR_LIGHTGREEN+"  >");
                    return;
                }
                //you are ok to travel
                else{
                    //normal travel logic output
                    player.addToOutputQ(TelnetColors.COLOR_WHITE+"You are "
                            + "traveling!");
                    player.currentRoom=otherRoom;
                    player.addToOutputQ(TelnetColors.COLOR_PURPLE+"You have "
                            + "a look around: "+player.currentRoom.description);
                    
                    //prompt
                    player.addToOutputQ("Prompt "+TelnetColors.COLOR_WHITE+
                            player.name+"/"+TelnetColors.COLOR_RED+
                            Integer.toString(player.health)+
                            "/"+TelnetColors.COLOR_CYAN+
                            Integer.toString(player.level)+
                            "/"+TelnetColors.COLOR_CYAN+
                            Integer.toString(player.speed)+
                            TelnetColors.COLOR_LIGHTGREEN+"  >");
                    
                    //spawn into the room
                    enterRoom(player.name,player.currentRoom);
                }
          
            }
        }
    }
      
    public void processRespawn(World world){
        //for all the rooms in the world
        for(Room r : world.rooms ){
            //if the room has a monster
           if (r.monster!=null){
               //if the mosnter is dead and ready to respawn
            if((r.monster.health<=0)&&(r.monster.respawnTimer==0)){
                //respawn the mosnter with full health
                r.monster.health=r.monster.respawnHealth;
                r.monster.respawnTimer=r.monster.respawnTime;//reset timer
                System.out.println("Respawing: "+r.monster.name+ " "+ r.id);
            }
            else if(r.monster.health<=0){
                //not ready to respawn, but take one tick off the timer
                r.monster.respawnTimer-=1;
            }
        }
        }
    }
  
}
