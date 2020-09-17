import java.net.*;
import java.io.*;
import java.util.Scanner;
import java.util.regex.*;

public class ClientObject
{

// ================================================================== //

/* MEMBERS */

// ================================================================== //

    Socket clientSocket;
    PrintWriter out;
    BufferedReader in, stdIn;
    String filePath;

// ================================================================== //

/* CONSTRUCTOR */

// ================================================================== //

    /* Creates the client by automatically connecting it to
       the provided server socket! Also instantiates PrintWriters
       and BufferedReaders to reat to / from socket. */
    public ClientObject(String ipAddr, int portNum)
    {
        try
        {
            clientSocket = new Socket(ipAddr, portNum);
            System.out.println("Connected!");

            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            stdIn = new BufferedReader(new InputStreamReader(System.in));
        }
        catch(IOException e)
        {
            System.out.println("Connection failed. " + e.toString());
        }

       this.openInterface(false);
    }

    public ClientObject(String ipAddr, int portNum, String fileName) 
    {
        try
        {
            clientSocket = new Socket(ipAddr, portNum);
            System.out.println("Connected!");

            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            stdIn = new BufferedReader(new InputStreamReader(System.in));
        }
        catch(IOException e)
        {
            System.out.println("Connection failed. " + e.toString());
        }

        this.filePath = fileName;

        this.openInterface(true);
    }

// ================================================================== //

/* Setup */

// ================================================================== //

    /* Opens a file and sends contents to server to create a quiz */
    public void processFile(String fileName)
    {
        try {
            File quizFile = new File(fileName);
            BufferedReader fileReader = new BufferedReader(new FileReader(quizFile));
            String input = "";
            String response = "";

            while ((input = fileReader.readLine()) != null)
            {
                switch (input.substring(0,1))
                {
                    case "p":
                        try
                        {
                            out.println(input);
                            String lastInput = "";
                            String userInput;
                
                            while ((userInput = fileReader.readLine()) != null)
                            {
                                if (lastInput.equals(".") && userInput.equals("."))
                                {
                                    out.println(".");
                                    break;
                                }
                
                                out.println(userInput);
                                lastInput = userInput;
                            }
                            if ((userInput = fileReader.readLine()) != null) // Correct answer
                                out.println(userInput);
                
                            // Print out the success or failure message
                            if ((userInput = in.readLine()) != null)
                                System.out.println(userInput);
                        }
                        catch(IOException e)
                        {
                            System.out.println("Error: " + e.toString());
                        }
                        break;

                    case "d":
                        out.println(input);
                        System.out.println(in.readLine());
                        break;

                    case "g":
                        out.println(input);
                        this.getQuestion(false);
                        break;

                    case "r":
                        out.println(input);
                        this.reviewContest();
                        break;

                    case "s":
                        out.println(input);
                        response = in.readLine();
                        System.out.println(response);
                        break;

                    case "a":
                        out.println(input);
                        response = in.readLine();
                        System.out.println(response);
                        break;

                    case "b":
                        out.println(input);
                        response = in.readLine();
                        System.out.println(response);
                        break;

                    case "l":
                        out.println(input);
                        this.listContests();
                        break;

                    case "k":
                        System.out.println("Murderer!");
                        out.println(input);
                        this.closeClient();
                        break;

                    case "q":
                        this.closeClient();
                        break;

                    case "h":
                        this.help();
                        break;

                    default:
                        System.out.println("Invalid command. Use command 'h' for" +
                            " a list of commands and their implementations.");
                        break;
                }
            }

            fileReader.close();
        }
        catch(IOException e)
            {
                System.out.println("Error: " + e.toString());
            }
    }

    /* Opens the client command interface */
    public void openInterface(boolean file)
    {
        try
        {
            if (file) {
                this.processFile(filePath);
            }
            this.commandRouter();  
            
            clientSocket.close();
        }
        catch(IOException e)
        {
            System.out.println("Error: " + e.toString());
        }
    }

// ================================================================== //

/* INTERFACE */

// ================================================================== //

    /* Routes user input to correct handlers */
    public void commandRouter()
    {
        String userInput, response;
        
        System.out.print("> ");

        try
        {
            while ((userInput = stdIn.readLine()) != null)
            {
                if (userInput.length() == 0)
                {
                    System.out.print("> ");
                    continue;
                }
                else if (!Pattern.matches("([pdgrsab])( )([0-9])( )([a-z]|[0-9])|([pdgrsab])( )([0-9]*)|([kqhl])",userInput))
                {
                    System.out.println("Invalid command. Use command \"h\" for help.");
                    System.out.print("> ");
                    continue;
                }

                // CHANGE ALL SUBSTRINGS TO ARRAYS
                switch (userInput.substring(0,1))
                {
                    case "p":
                        out.println(userInput);
                        this.putQuestion();
                        break;

                    case "d":
                        out.println(userInput);
                        System.out.println(in.readLine());
                        break;

                    case "g":
                        out.println(userInput);
                        this.getQuestion(false);
                        break;

                    case "r":
                        out.println(userInput);
                        this.reviewContest();
                        break;

                    case "s":
                        out.println(userInput);
                        response = in.readLine();
                        System.out.println(response);
                        break;

                    case "a":
                        out.println(userInput);
                        response = in.readLine();
                        System.out.println(response);
                        break;

                    case "b":
                        out.println(userInput);
                        response = in.readLine();
                        System.out.println(response);
                        break;

                    case "l":
                        out.println(userInput);
                        this.listContests();
                        break;

                    case "k":
                        System.out.println("Murderer!");
                        out.println(userInput);
                        break;

                    case "q":
                        this.closeClient();
                        break;

                    case "h":
                        this.help();
                        break;

                    default:
                        System.out.println("Invalid command. Use command 'h' for" +
                            " a list of commands and their implementations.");
                        break;
                }
                System.out.print("> ");
            }
        }
        catch(IOException e)
        {
            System.out.println("Error: " + e.toString());
        }
    }

// ================================================================== //

/* COMMANDS */

// ================================================================== //

    /* Sends user input in proper format to the server */
    public void putQuestion()
    {
        try
        {
            String lastInput = "";
            String userInput;

            while ((userInput = stdIn.readLine()) != null)
            {
                if (lastInput.equals(".") && userInput.equals("."))
                {
                    out.println(".");
                    break;
                }

                out.println(userInput);
                lastInput = userInput;
            }
            if ((userInput = stdIn.readLine()) != null) // Correct answer
                out.println(userInput);

            // Print out the success or failure message
            if ((userInput = in.readLine()) != null)
                System.out.println(userInput);
        }
        catch(IOException e)
        {
            System.out.println("Error: " + e.toString());
        }
    }

    public void reviewContest()
    {
        String inputString;
        System.out.println();
        try {
            while ((inputString = in.readLine()) != null)
            {
                if (inputString.equals("DONE"))
                    break;

                System.out.println(inputString);

                if (inputString.length() >= 5 && inputString.substring(0,5).equals("Error"))
                    return;
            }
        }
        catch(IOException e)
        {
            System.out.println("Error: " + e.toString());
        }
    }

    public void listContests()
    {
        String inputString;
        System.out.println();
        try {
            while ((inputString = in.readLine()) != null)
            {
                if (inputString.equals("DONE"))
                    break;

                System.out.println(inputString);
            }
        }
        catch(IOException e)
        {
            System.out.println("Error: " + e.toString());
        }
    }

    /* Gets a question */
    public void getQuestion(boolean isRandom)
    {
        String inputString;
        try
        {
            while ((inputString = in.readLine()) != null)
            {
                if (inputString.equals("DONE")) {
                    inputString = in.readLine(); // duct tape
                    break;
                }
                System.out.println(inputString);
                if (inputString.length() >= 5 && inputString.substring(0,5).equals("Error"))
                    return;
            }

            /* Send client's answer if this is a random question! */
            if (isRandom)
            {
                out.println(stdIn.readLine());
                System.out.println(in.readLine());
            }
        }
        catch(IOException e)
        {
            System.out.println("Error: " + e.toString());
        }
        
    }

    /* Help function! */
    public void help()
    {
        System.out.println("Commands:");
        System.out.println("\tCommands are shown in brackets <>. Type them without the brackets.");
        System.out.println("\tp - add a new question --- check the man page for details");
        System.out.println("\td - delete a question by its question number <d #>");
        System.out.println("\tg - get a question by its question number <g #>");
        System.out.println("\ts - creates contest with specified contest number <s c#>");
        System.out.println("\ta - adds question (q#) to contest (c#) <a c# q#>");
        System.out.println("\tb - begin specified contest <b c#>");
        System.out.println("\tr - get details about specified contest <r c#>");
        System.out.println("\tl - list all contests <l>");
        System.out.println("\tk - kills the server <k>");
        System.out.println("\tq - quits the client <q>");
    }

// ================================================================== //

/* SHUTDOWN */

// ================================================================== //

    /* Closes the client */
    public void closeClient()
    {
        try
        {
            clientSocket.close();
            out.close();
            in.close();
        }
        catch(IOException e)
        {
            System.out.println("Error closing server: " + e.toString());
        }
        System.exit(0);
    }
}