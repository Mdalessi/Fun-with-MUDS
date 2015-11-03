/**
 *Author: Mike D'Alessio
 *Date: Spring 2015
 *File: World.
 */

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


// A complete game world, which is a collection of rooms.
public class World {
    //List of all rooms in the world.
    public List<Room> rooms;
    
    //The startroom the focal point of world map. 
    public Room startRoom;


    void setRooms(List<Room> rooms) {
        this.rooms = rooms;
        //iterate through all the rooms
        for (Room room : rooms) {
            //if the room is the startroom mark as so.
            if (room.id.equals("start")) {
                startRoom = room;
            }
        }
    }

}