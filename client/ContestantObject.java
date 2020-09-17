import java.net.*;
import java.io.*;
import java.util.Scanner;
import java.util.regex.*;

public class ContestantObject {
    Socket contestantSocket;
    PrintWriter out;
    BufferedReader in, stdIn;
    String nickname;

    /* Creates the contestant by automatically connecting it to
       the provided server socket! Also instantiates PrintWriters
       and BufferedReaders to reat to / from socket. */
    public ContestantObject(String ipAddr, int portNum)
    {
        try
        {
            contestantSocket = new Socket(ipAddr, portNum);
            System.out.println("Connected!");
        }
        catch(IOException e)
        {
            System.out.println("Connection failed. " + e.toString());
        }

        this.openInterface();
    }

    /* Opens interface */
    public void openInterface()
    {
        try
        {
            out = new PrintWriter(contestantSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(contestantSocket.getInputStream()));
            stdIn = new BufferedReader(new InputStreamReader(System.in));
            this.sendIntro();
            this.contestInterface();

            contestantSocket.close();
        }
        catch(IOException e)
        {
            System.out.println("Error: " + e.toString());
        }
    }

    /* Courts the server */
    public void sendIntro()
    {
        try {
            while (true)
            {
                System.out.println("Please input a nickname:");
                String nickname = stdIn.readLine();
                out.println(nickname);
                String response = in.readLine();
                System.out.println(response);

                if (response.substring(0,5).equals("Error"))
                    continue;
                else
                    break;
            }
            
        }
        catch(IOException e) {
            System.out.println("Error: " + e.toString());
        }
    }

    public void contestInterface()
    {
        boolean quizFinished = false;
        String input;

        try {
            /* Get question! */
            while (!quizFinished)
            {
                while (((input = in.readLine()) != null))
                {
                    if (input.equals("DONE"))
                        break;
                    System.out.println(input);

                    if (input.length() >= 20  && input.substring(0,19).equals("The contest is over"))
                        System.exit(0);
                }

                /* Send answer choice */
                System.out.print("Enter your choice: ");
                out.println(stdIn.readLine());
                System.out.println();
                System.out.println(in.readLine());
                System.out.println(in.readLine());
            }

            /* Send answer! */
        }
        catch(IOException e)
        {
            System.out.println("Error: " + e.toString());
        }
    }
}