/**
 *Author: Mike D'Alessio
 *Date: Spring 2015
 *File: WorldBuilder.java
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

//Used to load up a world into the gameserver.
public class WorldBuilder {


    public static World create() {
        //Creates a new world object
        World world = new World();
        //List of Rooms which is used in the 'setRooms' world function.
        List<Room> rooms = new ArrayList<Room>();
        
        //This is all intalizes the monsters for the first quest.
        Monster monster1 =new Monster(10,1,1,"Goblin", "looks like he "
                + "hasn't eaten days, and you are on the menu!",300,300,10);
        
        Monster monster2 =new Monster(10,1,1,"Goblin", "looks like he "
                + "hasn't eaten days, and you are on the menu!",300,300,10);
        
        Monster monster3 =new Monster(20,1,1,"Goblin King", "The goblin "
                + "leader, who reports directly to the Dreadlord, "
                + "free the people of whitebridge from his evil grip.",
                400,400,20);
        
        //Social rooms/start room
        Room startRoom = new Room(world, "start", new HashMap<Direction,
                Room>(), "You are in the center of the town of Whitebridge."
                + "There are kids playing in the streets, shop keepers "
                + "yelling to try to try and entice a sale,the "
                + "town mayor trying to"+ "stop an arugment between "
                + "two other adventurers, and the strangest uneasy feeling"
                + "that the forces of evil are all around you.");
      
        Room northOfStartRoom = new Room(world, "cave entrance ", 
                new HashMap<Direction, Room>(), "This look like the "
                        + "entrance to a goblin cave.",monster1,false);
        
        //THis is the first quest
        Room caveLair= new Room(world, "caveLair", new HashMap<Direction,
                Room>(), "You sneak further into the goblin lair "
                        + "looking for your next target.",monster2,false);
        
        Room caveBoss= new Room(world, "caveBoss", new HashMap<Direction,
                Room>(), "This room looks different then the others , "
                        + "trophies of the goblins kills are"
                        + " are hung on the walls. Fresh bones lie on the "
                        + "ground from recent kills, what could possibly "
                        + "have done this?",monster3,false);
        
        Room westOfStartRoom = new Room(world, "west", new HashMap<Direction,
                Room>(), "You are in the mess hall, poeple are starting "
                        + "to get desperate with not enough food to due the "
                        + "local goblins, and the vicious Goblin King. "
                        + "Pushing and fights are starting to break out i"
                        + "n line,the Mayor doesn't have enough men to keep "
                        + "the peace.....");
        
        //This is the final quest with the boss the dreadlord.
        Monster monster4 =new Monster(15,1,1,"Skeleton warrior", 
                "a once great hero who has been risen to fight for the "
                        + "dreadlord.",300,300,15);
        
        Monster monster5 =new Monster(15,1,1,"Skeleton warrior", 
                "a once great hero who has been risen to fight for "
                        + "the dreadlord.",300,300,15);
        
        Monster monster6 =new Monster(20,2,3,"Skeleton King", "The legendary"
                + " king of whitebridge who once protected these lands"
                + " from evil risen to be instrument of death and despair "
                + "to the people he vowed to protect.",500,500,20);
        
        Monster monsterBOSS =new Monster(25,3,15,"DreadLord", "The dreadlord "
                + "an incredibly powerful demon who wield the powers of "
                + "darkness and mental domination.He stands at a frightening"
                + " 8 feet tall, with talons as big your arm. This cunning, "
                + "malefic being once served as the Dark one's most trusted "
                + "lieutenant, and has lived for over 10,000 years. "
                + "His weapon of choice is an ice blade that can freeze "
                + "a man by just touching them. ",200,300,25);
        
        //Rooms for the final quest
        Room forestEntrance = new Room(world, "forest entrance", 
                new HashMap<Direction, Room>(), "This look like the entrance "
                        + "to the dreadlords forest.",monster4,false);
        
        Room creek= new Room(world, "creek", new HashMap<Direction, Room>(),
                "This area looks to be once a resting place for the animals of"
                + "the forest, but that has long since passed, only death "
                        + "remains.",monster5,false);
        
        Room castleEntrance= new Room(world, "Castle Entrance", 
                new HashMap<Direction, Room>(), " You find your self in "
                        + "front of a huge castle,hidden by the trees, you "
                        + "almost didn't find it.You can feel the evil "
                        + "emanating from every part of this castle down to "
                        + "every last stone.",monster6,false);
        
        Room dreadlordThrone= new Room(world, "dreadlordThrone",
                new HashMap<Direction, Room>(), "As you creep through "
                        + "the ancient castle,nothing stirs and the air is "
                        + "especially heavy.You reach the doors to the throne "
                        + "room and feel a chill, an evil none of which "
                        + "you have felt before lives right beyond this point. "
                        + "With great strain you push open the doors and your "
                        + "vision is immediately flooded with the terrifying "
                        + "image of a massive human...or creature seated on "
                        + "the throne simply smiling at you with arrogance "
                        + "gained from winning countless battles.",
                monsterBOSS,false);
        
         
        // Add the exits.  Exits in this case are non-bidirectional.
        startRoom.addExit(Direction.NORTH, northOfStartRoom);
        startRoom.addExit(Direction.WEST, westOfStartRoom);
        
        //First quest.
        northOfStartRoom.addExit(Direction.SOUTH, startRoom);
        northOfStartRoom.addExit(Direction.WEST, caveLair);
        caveLair.addExit(Direction.EAST, northOfStartRoom);
        caveLair.addExit(Direction.NORTH, caveBoss);
        caveBoss.addExit(Direction.SOUTH, caveLair);
        
        //mess room/social room
        westOfStartRoom.addExit(Direction.EAST, startRoom);
        startRoom.addExit(Direction.SOUTH, forestEntrance);
        
        //Dreadlord quest.
        forestEntrance.addExit(Direction.NORTH, startRoom);
        forestEntrance.addExit(Direction.WEST, creek);
        creek.addExit(Direction.EAST, forestEntrance);
        forestEntrance.addExit(Direction.SOUTH, castleEntrance);
        castleEntrance.addExit(Direction.NORTH, forestEntrance);
        castleEntrance.addExit(Direction.SOUTH, dreadlordThrone);
        dreadlordThrone.addExit(Direction.NORTH, castleEntrance);
       
        // Add the rooms.
        rooms.add(startRoom);
        rooms.add(northOfStartRoom);
        rooms.add(westOfStartRoom);
        rooms.add(caveLair);
        rooms.add(caveBoss);
        rooms.add(creek);
        rooms.add(forestEntrance);
        rooms.add(castleEntrance);
        rooms.add(dreadlordThrone);

        world.setRooms(rooms);//Sets the rooms in the world

        return world;//return the world object.
    }

}