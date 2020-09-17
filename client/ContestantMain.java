import java.lang.Thread;
import java.net.*;
import java.io.*;
//import org.json.simple.JSONObject;

class ContestantMain
{
    public static void main(String[] args)
    {
        /* Checks if proper arguments are supplied */
        if (args.length != 2)
        {
            System.out.println("Enter IP address and port number " +
                                "i.e. java Client <IPA> <Port#>");
            return;
        }

        ContestantObject contestantObj = new ContestantObject(args[0],Integer.parseInt(args[1]));
    }
}