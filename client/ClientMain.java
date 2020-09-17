import java.net.*;
import java.io.*;

class ClientMain
{
    public static void main(String[] args)
    {
        /* Checks if proper arguments are supplied */
        if (args.length != 2 && args.length != 3)
        {
            System.out.println("Enter IP address and port number, " +
                                "and optionally, a filename: " +
                                "i.e. java Client <IPA> <Port#> <fileName>");
            return;
        }

        if (args.length == 3)
        {
            ClientObject clientObj = new ClientObject(args[0], Integer.parseInt(args[1]), args[2]);
        }

        ClientObject clientObj = new ClientObject(args[0], Integer.parseInt(args[1]));
    }
}