/*
Michael D'Alessio
MUD Server
Senior Project
Professor Miller's class
Spring 2015
*/

import java.net.*;
import java.io.*;
import java.util.*;


public class Client  {

	// for I/O
	private ObjectInputStream sInput;

	// to read from the socket
	private ObjectOutputStream sOutput;

	// to write on the socket
	private Socket socket;

	
	// the ip address, the username
	private String server, username;

	//the port we will be using to connect
	private int port;

	/*
    NAME

        Client- Constructor.

SYNOPSIS
			Server         --> The Ip address of the server.

            Port             --> the Port the server will be running on.

            username        --> the username of the current client is using.


DESCRIPTION

        This function will serve as the constructor for the client class.

RETURNS

        None

AUTHOR

        Michael D'Alessio

DATE

        6:30pm 3/15/2015
/**/

	Client(String server, int port, String username) {
		this.server = server;
		this.port = port;
		this.username = username;
	}


	/*
    NAME

        start- Trys to Create the connection between the client and the server



DESCRIPTION

        This function will try to make a socket at the ip address
         and port specfied by the user. After the connection is accepted 
         try to create the input and output streams the will be handling
         our chat objects. Create a thread that will be constantly listening
         for messages from the server. Send the username as a string to the
         server so the can add us to the arraylist.

RETURNS

        None

AUTHOR

        Michael D'Alessio

DATE

        7:00pm 3/15/2015
/**/

	public boolean start() {
		// try to connect to the server
		try {
			socket = new Socket(server, port);
		} 
		// if it failed not much I can so
		catch(Exception ec) {
			System.out.println("Error connectiong to server:" + ec);
			return false;
		}
		
		String msg = "Connection accepted " + socket.getInetAddress() + ":" + socket.getPort();
		System.out.println(msg);
	
		/* Creating both Data Stream */
		try
		{
			sInput  = new ObjectInputStream(socket.getInputStream());
			sOutput = new ObjectOutputStream(socket.getOutputStream());
		}
		catch (IOException eIO) {
			System.out.println("Exception creating new Input/output Streams: " + eIO);
			return false;
		}

		// creates the Thread to listen from the server 
		new ListenFromServer().start();

		// Send our username to the server this is the only message that we
		// will send as a String. All other messages will be ChatMessage objects
		try
		{
			sOutput.writeObject(username);
		}
		catch (IOException eIO) {
			System.out.println("Exception doing login : " + eIO);
			disconnect();
			return false;
		}
		// success we inform the caller that it worked
		return true;
	}

	/*
NAME

        sendMessage- Sends message to the server .

SYNOPSIS
			msg         --> The Message to the server.


DESCRIPTION

        This function will try to write to the output stream.

RETURNS

        None

AUTHOR

        Michael D'Alessio

DATE

        7:30pm 3/15/2015
/**/


	void sendMessage(ChatMessage msg) {
		try {
			//write to output stream
			sOutput.writeObject(msg);
		}

		catch(IOException e) {
			System.out.println("Exception writing to server: " + e);
		}
	}

	/*
	 * When something goes wrong
	 * Close the Input/Output streams and disconnect not much to do in the catch clause
	 */
	private void disconnect() {
		try { 
			if(sInput != null){
			 	sInput.close();
			}
		}

		catch(Exception e) {} // not much else I can do
		
		try {
			if(sOutput != null){
			 	sOutput.close();
			 }
		}
		catch(Exception e) {} // not much else I can do
        
        try{
			if(socket != null){
				 socket.close();
			}
		}

		catch(Exception e) {} // not much else I can do
		
	}

		/*
    NAME

        Main- Parses the command line args and waits for msgs.

SYNOPSIS
			arg[2]         --> The Ip address of the server.

            arg[1]             --> the Port the server will be running on.

            arg[0]       --> the username of the current client is using.


DESCRIPTION

        This function will parse the users command line args for the username 
        port and ip address for the server. Once the information is collected 
        create a client object. Test the connection to the server, if the connection is solid
        wait for msgs from the user to send to server. Parsing the message for keywords like logout
        or whoisin to send different chatmessage objects. Once the connection is dead disconnect the cleint.

RETURNS

        None

AUTHOR

        Michael D'Alessio

DATE

        8:30pm 3/15/2015
/**/


	public static void main(String[] args) {
		// default values
		int portNumber = 8080;
		String serverAddress = "localhost";
		String userName = "Anonymous";

		// depending of the number of arguments provided we fall through
			// > javac Client username portNumber serverAddr
			if (args.length==3){
				serverAddress = args[2];
				portNumber = Integer.parseInt(args[1]);
				userName = args[0];
			}

			// > javac Client username portNumber
			else if (args.length==2){
				try {
					portNumber = Integer.parseInt(args[1]);
					userName = args[0];
				}

				catch(Exception e) {
					System.out.println("Invalid port number.");
					System.out.println("Usage is: > java Client [username] [portNumber] [serverAddress]");
					System.exit(0);
				}
			}
			// > javac Client username
			else if (args.length==1){
				userName = args[0];
			}
			if (args.length>3){
				System.out.println("Usage is: > java Client [username] [portNumber] {serverAddress]");
				System.exit(0);
			}
		
		// create the Client object
		Client client = new Client(serverAddress, portNumber, userName);

		// test if we can start the connection to the Server
		// if it failed nothing we can do
		if(!client.start())
			return;
		
		// wait for messages from user
		Scanner scan = new Scanner(System.in);

		// loop forever for message from the user
		while(true) {
			System.out.print("> ");

			// read message from user
			String msg = scan.nextLine();

			// logout if message is LOGOUT
			if(msg.equalsIgnoreCase("LOGOUT")) {
				client.sendMessage(new ChatMessage(ChatMessage.MESSAGE, msg));
				// break to do the disconnect
				break;
			}

			// message WhoIsIn
			else if(msg.equalsIgnoreCase("WHOISIN")) {
				client.sendMessage(new ChatMessage(ChatMessage.MESSAGE, msg));				
			}

			else {				// default to ordinary message
				client.sendMessage(new ChatMessage(ChatMessage.MESSAGE, msg));
			}
		}
		// done disconnect
		client.disconnect();	
	}


	class ListenFromServer extends Thread {

			/*
NAME

        Run- Wait for messages from the server.


DESCRIPTION

        This function will keep running until disconnected 
        and the socket is closed. If there is any message from the 
        Server write it to the console. 

RETURNS

        None

AUTHOR

        Michael D'Alessio

DATE

        10:30pm 3/15/2015
/**/


		public void run() {
			while(true) {
				try {
					String msg = (String) sInput.readObject();

					// if console mode print the message and add back the prompt
						System.out.println(msg);
						System.out.print("> ");
				
				}

				catch(IOException e) {
					System.out.println("Server has close the connection: " + e);
					break;
				}

				// can't happen with a String object but need the catch anyhow
				catch(ClassNotFoundException e2) {
				}
			}
		}
	}
}
