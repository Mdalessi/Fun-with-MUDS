/*
Michael D'Alessio
MUD Server
Senior Project
Professor Miller's class
Spring 2015
*/

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;


public class Server {
    // a unique ID for each connection
    private static int uniqueId;

    // an ArrayList to keep the list of the Client
    private ArrayList<ClientThread> arrayList;

    // to display time
    private SimpleDateFormat dateFormat;

    // the port number to listen for connection
    private int port;

    // the boolean that will be turned of to stop the server
    private boolean keepGoing;

    //The Queue to hold all the actions until a "tick" passes.
    private Queue<String> qe=new LinkedList<String>();



    

/*
    NAME

        Server- Constructor.

SYNOPSIS

            Port             --> the Port the server will be running on.


DESCRIPTION

        This function will serve as the constructor for the server class.

RETURNS

        None

AUTHOR

        Michael D'Alessio

DATE

        6:30pm 3/1/2015
/**/

    public Server(int port) {
        // the port
        this.port = port;

        // to display hh:mm:ss
        dateFormat = new SimpleDateFormat("HH:mm:ss");

        // ArrayList for the Client list
        arrayList = new ArrayList<ClientThread>();
    }

    /**/
/*
NAME
    Start()- Starts the server and waits for connections


DESCRIPTION

        This function will attempt to start a server and wait for connections. 
        When a new connection is accepted, the function will make a thread of it.
        Also adding it to the Arraylist, so to keep track of the connection, and for the 
        WHOISIN functionality. 
        If you get a LOGOUT call(keepgoing=false) close the connection to that client.

        Above was the orginal function of this function, however as of 4/17 and some research 
        I figured I need to implemnt the idea of a "tick" aka one moment in a game mode that passed.
        A tick currently is 200ms, the timeout of the conneciton accept, so after that 200ms
        the function will gather all the actions that are Queued and complete them all at the same
        time("tick"). This was an important feature to implement, and had to use a bit of creativity!


RETURNS

        None

AUTHOR

        Michael D'Alessio

DATE

         9:30pm 3/2/2015

*/
/**/

    public void start() {
        keepGoing = true;
        Iterator iterator;
        System.out.println("Server waiting for Clients on port " + port + ".");
        /* create socket server and wait for connection requests */
        try 
        {
            // the socket used by the server
            ServerSocket serverSocket = new ServerSocket(port);
            serverSocket.setSoTimeout(200);



            // infinite loop to wait for connections
            while(keepGoing) 
            {

                iterator = qe.iterator();
                while(iterator.hasNext()){
                    String element = (String) iterator.next();

                    //System.out.println(element); // test prints 
                    // System.out.println(elements);

                    parse(element);
              
            }
                qe.clear();

               
                try{
                Socket socket = serverSocket.accept();
                if(!keepGoing){
                    break;
                }
                ClientThread t = new ClientThread(socket);  // make a thread of it
                arrayList.add(t);                           // save it in the ArrayList
                t.start();
                 } catch (SocketTimeoutException s) {}      // accept connection
           

            }
            // Closing up shop
            try {

                serverSocket.close();

                for(int i = 0; i < arrayList.size(); ++i) {
                    ClientThread tc = arrayList.get(i);
                    try {
                    tc.sInput.close();
                    tc.sOutput.close();
                    tc.socket.close();
                    }

                    catch(IOException ioE) {
                        // not much I can do
                    }
                }
            }

            catch(Exception e) {
                System.out.println("Exception closing the server and clients: " + e);
            }
        }

        // something went bad
        catch (IOException e) {
            String msg = dateFormat.format(new Date()) + " Exception on new ServerSocket: " + e + "\n";
            System.out.println(msg);
        }
    }       
    
    /*
    NAME

        Broadcast- Send the same messages to all the clients.

SYNOPSIS

            message --> the message to send to the clients.


DESCRIPTION

        This function will start by adding the time to the orginal message. 
        It will then print the message to the server.
        Then the function will start looping through in revese ordering.
        Checking if the the client disconnected so we can disconnect from it.
        If not call writeMSG and send MESSAGEWT

RETURNS

        None

AUTHOR

        Michael D'Alessio

DATE

        7:30pm 3/1/2015
/**/

    private void broadcast(String message) {
        // add HH:mm:ss and \n to the message
        String time = dateFormat.format(new Date());
        String messageWT = time + " " + message + "\n";

        // display message on console 
        System.out.print(messageWT);
        
        // we loop in reverse order in case we would have to remove a Client
        // because it has disconnected
        for(int i = arrayList.size(); --i >= 0;) {
            ClientThread ct = arrayList.get(i);
            // try to write to the Client if it fails remove it from the list
            if(!ct.writeMsg(messageWT)) {
                arrayList.remove(i);
                System.out.println("Disconnected Client " + ct.username + " removed from list.");
            }
        }
    }


    /*
    NAME

        Parse- Parses the username of the client calling and action then does that action.

SYNOPSIS

            element --> the message to parse .


DESCRIPTION

        This function starts by spliting the username from the whole string element.
        Then splits again, so you have the command and the message(if applicable).
        Then the function checks for commands that are accepted by the system, then
        does the correct action. Actions like say are easy cause you can send the message
        to all the clients, the other actions such as logout whoisin etc, are specfic to the client
        therefore we need to find the username in our thread pool that matches the username of the person 
        calling the command. After we find that username, we have the current clientthread that we can send 
        our info to/do our action.


RETURNS

        None

AUTHOR

        Michael D'Alessio

DATE

        9:30pm 4/7/2015
/**/
public void parse(String element) {
        Tokenize token = new Tokenize(element);
                    if (token.isValid() == false) {
                        
                    } 
                    else {
                    String username = token.first;
                    String remainder = token.remainder;

                    Tokenize userToken = new Tokenize(remainder);
                    String command =userToken.first;
                    String message=userToken.remainder;
                    System.out.println(command);

                    if(command==null){
                       return;
                    }
                   
                    if (command.equalsIgnoreCase("say")){

                         broadcast(username + ": " + message);
                       
                    }
                    else if(command.equalsIgnoreCase("whoisin")){
                        int size=arrayList.size();

                        for(int i = 0; i < size; ++i) {
                        ClientThread ct = arrayList.get(i);
                        if(ct.username.equals(username)){
                            ct.writeMsg("List of the users connected at " + dateFormat.format(new Date()) + "\n");

                            // scan arrayList the users connected
                            for(int y = 0; y < size; ++y) {
                                ClientThread st = arrayList.get(y);
                                ct.writeMsg((y+1) + ") " + st.username + " since " + st.date);
                            }
                        }
                    }
                    }
                    else if (command.equalsIgnoreCase("look")) {
                        for(int i = 0; i < arrayList.size(); ++i) {
                        ClientThread ct = arrayList.get(i);
                        String message1="you looked congrats";
                        if(ct.username.equals(username))
                        {
                            ct.writeMsg(message1);

                        }
                        
                    }
                    }
                    else if (command.equalsIgnoreCase("logout")){
                         for(int i = 0; i < arrayList.size(); ++i){
                        ClientThread ct = arrayList.get(i);
                        String message1="you looked congrats";
                        if(ct.username.equals(username))
                        {
                            remove(ct.id);
                            ct.close();

                        }
                       }
                    }
                     else if (command.equalsIgnoreCase("north")) {
                        
                    } 
                    else if (command.equalsIgnoreCase("south")) {
                      
                    }
                     else if (command.equalsIgnoreCase("west")) {
                       
                    } 
                    else if (command.equalsIgnoreCase("east")) {
                       
                    } 
                }
            }


    /*
NAME

        Remove- Called when a logout msg is recived.

SYNOPSIS

            id  --> id to remove form the arrayList.


DESCRIPTION

        This function will remove a id from the arraylist

RETURNS

        None

AUTHOR

        Michael D'Alessio

DATE

        7:00pm 3/1/2015
/**/

    // for a client who logoff using the LOGOUT message
        private void remove(int id) {
        // scan the array list until we found the Id
        for(int i = 0; i < arrayList.size(); ++i) {
            ClientThread ct = arrayList.get(i);

            // found it
            if(ct.id == id) {
                arrayList.remove(i);
                return;
            }
        }
    }

    /*
NAME

        Main- Check usage, parse port.

SYNOPSIS

            args[0]     -> the Port the server will be running on.


DESCRIPTION

        This function will check that only one command line arugment is used.
        The function will also check that the arugment is a port is valid.
        Finally main will call start() to start the server.

RETURNS

        None

AUTHOR

        Michael D'Alessio

DATE

        7:30pm 3/1/2015
/**/
    /*
     * > java Server
     * > java Server portNumber
     * If the port number is not specified 8080 is used
     */ 

    public static void main(String[] args) {
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
        server.start();
    }

    /** One instance of this thread will run for each client */
    class ClientThread extends Thread {
        // the socket where to listen/talk
        Socket socket;
        ObjectInputStream sInput;
        ObjectOutputStream sOutput;

        // my unique id 
        int id;

        // the Username of the Client
        String username;

        // the only type of message a will receive
        ChatMessage cm;

        // the date I connect
        String date;

/*
    NAME

        ClientThread- Constructor.

SYNOPSIS

           socket           --> the socket the thread will be using.


DESCRIPTION

        This function will be called to create a new thread when 
        a new connection is made to the server.
        It will read in the username, the client will that as a 
        string(only message not sent as a  chatmessage object).
        This thread will also create a varaible for the date so that
        when Whoisin is called the times of logins can be displayed. 


RETURNS

        None

AUTHOR

        Michael D'Alessio

DATE

        8:30pm 3/1/2015
/**/

        ClientThread(Socket socket) {
            // a unique id
            id = ++uniqueId;
            this.socket = socket;
            /* Creating both Data Stream */
            System.out.println("Thread trying to create Object Input/Output Streams");
            try
            {
                // create output first
                sOutput = new ObjectOutputStream(socket.getOutputStream());
                sInput  = new ObjectInputStream(socket.getInputStream());

                // read the username
                username = (String) sInput.readObject();
                System.out.println(username + " just connected.");
            }

            catch (IOException e) {
                System.out.println("Exception creating new Input/output Streams: " + e);
                return;
            }
            // have to catch ClassNotFoundException
            // but I read a String, I am sure it will work
            catch (ClassNotFoundException e) {
            }

            date = new Date().toString() + "\n";
        }
/*
    NAME

        run()- AS long as the thread(connection) is active this function will run.

DESCRIPTION

        This function will serve as parsing the objects passed from the client.
        After the object is passed to the function from the input stream, the if
        else statments check the input for the type of chatmessage, WHOISIN, 
        Logout, MSG,etc.After the type is decided we will either broadcast 
        the msg to every client or commit and action.Eventually this will be 
        the focal part of my interactions between server and actions like 
        "NORTH, SOUTH, EAST, WEST, ATTACK" will be added. After the connection has ended
        keepgoing=false we close the connection and remove the id from the arraylist.


        As of 4/17 some of the functionality has been moved to the funciton "parse", 
        instead of completeing actions, this function now adds actions to the Queue
        qe, so that they can completed during a "tick"

RETURNS

        None

AUTHOR

        Michael D'Alessio

DATE

        9:30pm 3/1/2015
/**/

        // what will run forever
        public void run() {
            // to loop until LOGOUT
            boolean keepGoing = true;
            while(keepGoing) {
                // read a String (which is an object)
                try {
                    cm = (ChatMessage) sInput.readObject();
                }

                catch (IOException e) {
                    System.out.println(username + " Exception reading Streams: " + e);
                    break;              
                }

                catch(ClassNotFoundException e2) {
                    break;
                }

                // the messaage part of the ChatMessage
                String message = username;
                message+=" ";
                message+=cm.getMessage();

                //System.out.println(message);
                qe.add(message);

                // What type of message received

                /*if(cm.getType()==ChatMessage.MESSAGE){
                  broadcast(username + ": " + message);
                }

                else if(cm.getType()== ChatMessage.LOGOUT){
                    System.out.println(username + " disconnected with a LOGOUT message.");
                    keepGoing = false;
                }

                else if(cm.getType()==ChatMessage.WHOISIN){
                    writeMsg("List of the users connected at " + dateFormat.format(new Date()) + "\n");
                    // scan arrayList the users connected
                    for(int i = 0; i < arrayList.size(); ++i) {
                        ClientThread ct = arrayList.get(i);
                        writeMsg((i+1) + ") " + ct.username + " since " + ct.date);
                    }
                    }*/
                
            }
            // remove from the arrayList containing the list of the
            // connected Clients
            //remove(id);
            //close();
        }
        /*
NAME

        Close- try to close every aspect of the connection.


DESCRIPTION

        This function will serve as an effective way to close
        the input/output streams of the thread. Then finally
        also closing the socket itself.

RETURNS

        None

AUTHOR

        Michael D'Alessio

DATE

        9:30pm 3/1/2015
/**/
        // try to close everything
        private void close() {
            // try to close the connection
            try {
                if(sOutput != null) sOutput.close();
            }

            catch(Exception e) {}
            
            try {
                if(sInput != null) sInput.close();
            }
            
            catch(Exception e) {};
            
            try {
                if(socket != null) socket.close();
            }
           
            catch (Exception e) {}
        }

/*
    NAME

        WriteMsg- Writes a message to the client in the current thread.

SYNOPSIS

            msg --> the msg to send to the client.


DESCRIPTION

        This function will try to send a message to the client, through
        the current socket, however if the function is no longer connected
        call close() to close the socket up correctly. The function will then
        write the message to the output stream. Catching the error if there was a problem
        on the server side.

RETURNS

        None

AUTHOR

        Michael D'Alessio

DATE

        10:30pm 3/1/2015
/**/
        private boolean writeMsg(String msg) {

            // if Client is still connected send the message to it
            if(!socket.isConnected()) {
                close();
                return false;
            }

            // write the message to the stream
            try {
                sOutput.writeObject(msg);
            }

            // if an error occurs, do not abort just inform the user
            catch(IOException e) {
                System.out.println("Error sending message to " + username);
                System.out.println(e.toString());
            }

            return true;
        }
    }
}

