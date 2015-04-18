/*
Michael D'Alessio
MUD 
Senior Project
Professor Miller's class
Spring 2015
*/

import java.io.*;

public class ChatMessage implements Serializable {

 

    static final int WHOISIN = 0, MESSAGE = 1, LOGOUT = 2;

    //type of message, WHOISIN, logout,etc
    private int type;

    //Contents of the message
    private String message;

    //Constructer for the class.
    ChatMessage(int type, String message) {

        this.type = type;

        this.message = message;
    }

    //Returns the Type Of message 
    int getType() {
        return type;
    }

    // Returns the message
    String getMessage() {
        return message;
    }
}
