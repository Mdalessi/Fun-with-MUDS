/**
 *Author: Mike D'Alessio
 *Date: Spring 2015
 *File: ClientThread.java
 */


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Date;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;


    class ClientThread extends Thread {
        // the socket where to listen/talk
        Socket socket;
       
        // my unique id 
        int id;
        
        //player object attack to clientThread
        Player player;
        
        // the date I connect
        String date;
        
        //input Queue for each player
        private Queue inputQ;
        
        //Input from players
        BufferedReader in;
        
        //Output for players
        BufferedWriter out;
    
        //used for debugging
        private static final Logger LOG = Logger.getLogger(ClientThread.class
                .getName());


        ClientThread(Socket socket, int id, Queue inputQ) {
            // a unique id
            this.id=id;
            this.inputQ=inputQ;
            this.socket = socket;
            date = new Date().toString() + "\n";
        }


        // what will run forever
        public void run() {
            try {
                this.in =new BufferedReader(new InputStreamReader(this.socket
                        .getInputStream()));
                this.out=new BufferedWriter(new OutputStreamWriter(this.socket
                        .getOutputStream()));
            } catch (IOException ex) {
                Logger.getLogger(ClientThread.class.getName()).log(Level.SEVERE,
                        null, ex);
            }
            
            // to loop until exit
            boolean keepGoing = true;
            String userInput="";
            while(keepGoing) {
       
                try {
                    //get user input
                    userInput = this.in.readLine();
                } catch (IOException ex) {
                    this.LOG.info("IOEx");
                    break;
                }
                if(userInput==null){
                    break;
                }
                System.out.println("in client Thread" + this.player);
                //add it to the input Queue
                inputQ.add(new PlayerInput(this.player,userInput));
                
   
            }
           
        }


       
        public void sendMsg(String msg) throws IOException{
            System.out.println("Sending msg to user");
            this.out.write(msg+ "\n");
            this.out.flush();
        }
        
 
        public void sendPrompt(String msg) throws IOException{
            System.out.println("Sending msg to user");
            this.out.write(msg);
            this.out.flush();
        }


    }