import java.lang.Thread;
import java.net.*;
import java.io.*;

class ServerMain
{
    public static void main(String[] args)
    {
        /* Starts the server */
        try {
            ServerSocket server = new ServerSocket(0);
            System.out.println("Listening on port " + server.getLocalPort());

            int counter = 0;

            while (true) {
                ++counter;
                Socket serverClient = server.accept();
                System.out.println("Client " + counter + " started!");
                ServerObject serverClientThread = new ServerObject(serverClient, counter);

                serverClientThread.start();
            }
        }
        catch (Exception e) {
            System.out.println("Error: adfkjasddfkajsfk " + e.toString()); 
        }
        
        System.out.println("Server is off!");
    }
}