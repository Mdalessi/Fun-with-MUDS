/**
 *Author: Mike D'Alessio
 *Date: Spring 2015
 *File: Server.java
 */

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;


public class Server {

    // an ArrayList to keep the list of the Client
    public ArrayList<ClientThread> clientList;

    // the port number to listen for connection
    private int port;
    
    //starts the game runnign
    private GameServer game;

    // the boolean that will be turned of to stop the server
    private boolean keepGoing;

    //The Queue to hold all the actions until a "tick" passes.
   private Queue<PlayerInput> inputQ = new ConcurrentLinkedQueue<PlayerInput>();
   
    public Server(int port) {
        // the port
        this.port = port;

        // ArrayList for the Client list
        clientList = new ArrayList<ClientThread>();
    }

    
    public void start() throws IOException{
        this.game.start();//start gameworld
        keepGoing = true;

        System.out.println("Server waiting for Clients on port " + port + ".");
        /* create socket server and wait for connection requests */
        try 
        {
            // the socket used by the server
            ServerSocket serverSocket = new ServerSocket(port);
            int uniqueId=0;
            Socket socket=null;
            while(keepGoing){
                try{
                    socket = serverSocket.accept();// accept connection
                    uniqueId++;
                }catch (SocketTimeoutException s) {}      
                if(!keepGoing){
                   break;
                }
                 // make a thread of it
                ClientThread t = new ClientThread(socket,uniqueId,this.inputQ); 
                this.game.newClient(t);  //put the new client into the game
                clientList.add(t);  // save it in the ArrayList
                t.start();
            } 
           
        }catch (Exception e){
            e.printStackTrace();
        }
    }
            
    public static void main(String[] args) throws Exception {
        // start server on port 8080 unless a PortNumber is specified 
        int portNumber = 8080;
        if(args.length==1){
            try {
                portNumber = Integer.parseInt(args[0]);
            }

            catch(Exception e) {
                System.out.println("Invalid port number.");
                System.out.println("Usage is: > java Server [portNumber]");
                System.exit(0);
            }
            }

        else if(args.length>1){
            System.out.println("Usage is: > java Server [portNumber]");
            System.exit(0);
        }
                
        
        // create a server object and start it
        Server server = new Server(portNumber);
        //create new game.
        server.game=new GameServer(server.inputQ, server);
        server.start();
    }
}