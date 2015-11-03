/**
 *Author: Mike D'Alessio
 *Date: Spring 2015
 *File: Room.java
 */

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class Room {
    //World object which contains all the rooms.
    public  World world;
    
    //Room Id, used to differentiate rooms.
    public  String id;
    
    //map that contains all the connections between rooms.
    public  Map<Direction, Room> connectedRooms;
    
    //description of the room.
    public  String description;
    
   //monster object not every room has one.
    public Monster monster;
    
    //boolean to specify if its a users turn to attack
    public boolean userTurn;
    
    

    public Room(World world, String id, Map<Direction, Room> connectedRooms,
            String description) {
        this.world = world;
        this.id = id;
        this.connectedRooms = connectedRooms;
        this.description = description;
    }

      public Room(World world, String id, Map<Direction, Room> connectedRooms,
              String description,Monster monster,boolean userTurn) {
          
        this.world = world;
        this.id = id;
        this.connectedRooms = connectedRooms;
        this.description = description;
        this.monster=monster;
        this.userTurn=userTurn;
    }

    void addExit(Direction direction, Room room) {
        connectedRooms.put(direction, room);
    }



}