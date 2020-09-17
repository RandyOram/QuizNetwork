import java.lang.Thread;
import java.net.*;
import java.io.*;
import org.json.simple.*;
import org.json.simple.parser.*;

public class ServerObject extends Thread
{

// ================================================================== //

/* MEMBERS */

// ================================================================== //

    int clientNum;
    boolean on, meister;
    ServerSocket server;
    Socket serverSocket;
    PrintWriter out;
    BufferedReader in;
    JSONArray questionBank; 
    JSONArray contestBank;
    String qBankFilePath = "./server/QuestionBank.json";
    String cBankFilePath = "./server/ContestBank.json";

// ================================================================== //

/* CONSTRUCTOR */

// ================================================================== //

    /* Creates the socket on an open port and listens for client
       connections. */
    public ServerObject(Socket clientInterface, int clientNum)
    {
        this.serverSocket = clientInterface;
        this.clientNum = clientNum;

        /* Populates the question bank from persistent storage */
        this.updateLocalQuestionBank();
        this.updateLocalContestBank();

        this.on = true;
    }

// ================================================================== //

/* THREAD STARTUP */

// ================================================================== //

    /* Thread starting */
    public void run() {
        try {
            out = new PrintWriter(serverSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));

            this.meisterInterface();
        }
        catch(Exception e) {
            System.out.println("Error: OMG" + e.toString()); 
        }
    }

// ================================================================== //

/* INTERFACES */

// ================================================================== //

    /* Opens the server up to accept commands */
    public void meisterInterface()
    {       
        try
        {
            String inputLine, outputLine;
            int qNum, cNum;

            while (this.on && (inputLine = in.readLine()) != null)
            {
                String[] inputVals = inputLine.split(" ");
                switch(inputVals[0])
                {
                    case "p":
                        qNum = Integer.parseInt(inputVals[1]);
                        this.acceptNewQuestion(qNum);
                        out.println("Success!");
                        break;
                    
                    case "d":
                        qNum = Integer.parseInt(inputVals[1]);
                        this.deleteQuestion(qNum);
                        break;

                    case "g":
                        qNum = Integer.parseInt(inputVals[1]);
                        this.dumpQuestion(qNum);
                        break;

                    case "r":
                        this.updateLocalContestBank();
                        cNum = Integer.parseInt(inputVals[1]);
                        this.reviewContest(cNum);
                        break;

                    case "l":
                        this.updateLocalContestBank();
                        this.listContests();
                        break;

                    case "k":
                        this.closeServer();
                        break;

                    case "b":
                        this.updateLocalContestBank();
                        this.beginContest(Integer.parseInt(inputVals[1]));
                        break;

                    case "s":
                        this.createContest(Integer.parseInt(inputVals[1]));
                        break;

                    case "a":
                        this.addToContest(Integer.parseInt(inputVals[1]),Integer.parseInt(inputVals[2]));
                        break;

                    default:
                        System.out.println("Invalid command.");
                        break;
                }
            }
        }
        catch(IOException e)
        {
            System.out.println("Error: ORANGE AND BLUE" + e.toString());
        }
    }

// ================================================================== //

/* QUESTION BANK METHODS */

// ================================================================== //

    /* Takes user input (currently unvalidated) and creates 
       a question object to hold it. Places that object into
       this object's JSONArray */
    public void acceptNewQuestion(int qNum)
    {
        String userInput;
        JSONObject newQuestion = new JSONObject();
        JSONArray answerArray = new JSONArray();

        try
        {
            newQuestion.put("qNum",qNum);
                
            if ((userInput = in.readLine()) != null)
                newQuestion.put("tags",userInput);
            if ((userInput = in.readLine()) != null)
                newQuestion.put("question",userInput);

            String lastInput = "";
            while((userInput = in.readLine()) != null)
            {
                if (lastInput.equals(".") && userInput.equals("."))
                {
                    newQuestion.put("answers",answerArray);
                    break;
                }

                lastInput = userInput;

                if (userInput.equals("."))
                    continue;
                
                answerArray.add(userInput);
            }

            if ((userInput = in.readLine()) != null)
                newQuestion.put("correct",userInput);
            
            if (this.questionExists(qNum))
                out.println("Error: question number " + qNum + " already used");

            // Print whether the question was successfully added
            out.println("Question " + qNum + " added");
            questionBank.add(newQuestion);
        }
        catch(IOException e)
        {
            System.out.println("Error: " + e.toString()); 
        }

        this.updateQuestionBank();
    }
    
    /* Deletes a question given its question number */
    public void deleteQuestion(int qNum)
    {
        if (!questionExists(qNum))
        {
            out.println("Error: question " + qNum + " not found");
            return;
        }

        JSONArray list = new JSONArray();
        int len = questionBank.size();
        if (questionBank != null)
        {
            int index = this.findQuestionIndex(qNum);

            for (int i = 0; i < len; i++)
            {
                if (i != index) 
                {
                    list.add(questionBank.get(i));
                }
            } 
        }
        questionBank = list;
        this.updateQuestionBank();

        out.println("Deleted question " + qNum);
    }

    /* Dumps all of the question contents to the client */
    public void dumpQuestion(int qNum)
    {
        if (!questionExists(qNum))
        {
            out.println("Error: question " + qNum + " not found");
            return;
        }

        int index = this.findQuestionIndex(qNum);
        JSONObject question = (JSONObject) questionBank.get(index);

        StringBuilder questionString = new StringBuilder();

        questionString.append(question.get("tags") + "\r\n");
        questionString.append(question.get("question") + "\r\n.\r\n");

        JSONArray answerArray = (JSONArray) question.get("answers");

        for (int i = 0; i < answerArray.size(); ++i)
        {
            questionString.append(answerArray.get(i) + "\r\n.\r\n");
        }
        questionString.append(".\r\n");
        questionString.append(question.get("correct") + "\r\n");
        questionString.append(question.get("qNum")+"\r\n");
        questionString.append("DONE\r\n");

        out.println(questionString.toString());
    }

    /* Gets the question from the specified qNum */
    public void getRandomQuestion(int qNum)
    {
        if (!questionExists(qNum))
        {
            out.println("Error: question not found");
            return;
        }

        int index = this.findQuestionIndex(qNum);
        JSONObject question = (JSONObject) questionBank.get(index);

        StringBuilder questionString = new StringBuilder();

        /* Extract fields */
        questionString.append(question.get("qNum") + "\r\n");
        questionString.append(question.get("question") + "\r\n");

        JSONArray answerArray = (JSONArray) question.get("answers");

        for (int i = 0; i < answerArray.size(); ++i)
        {
            questionString.append(answerArray.get(i) + "\r\n");
        }
        
        questionString.append("\r\nDONE");

        /* Send the question */
        out.println(questionString.toString());

        String inputString;

        try
        {
            inputString = in.readLine();
            this.checkAnswer(qNum, inputString);
        }
        catch(IOException e)
        {
            System.out.println("Error: " + e.toString());
        }
    }

    // DEPRECATED
    /* Checks the given answer against the question in the bank */
    public void checkAnswer(int qNum, String answer)
    {
        if (!questionExists(qNum))
        {
            out.println("Error: question " + qNum + " not found");
            return;
        }

        int index = this.findQuestionIndex(qNum);
        JSONObject question = (JSONObject) questionBank.get(index);
        
        if (question.get("correct").equals(answer))
            out.println("Correct");
        else
            out.println("Incorrect");
    }

// ================================================================== //

/* CONTEST METHODS */

// ================================================================== //

    public void reviewContest(int contestNum)
    {
        if (!contestExists(contestNum))
        {
            out.println("Error: Contest " + contestNum + " doesn't exist!");
            return;
        }

        /* Get num questions */
        int cIndex = this.findContestIndex(contestNum);
        JSONObject currContest = (JSONObject) contestBank.get(cIndex);
        JSONArray qNumArray = (JSONArray) currContest.get("qNums");
        int numQuestions = qNumArray.size();

        /* Get hasRun */
        Boolean hasRun = ((currContest.get("hasRun").toString().equals("true")) ? true : false);

        /* Print stuff! */
        if (hasRun)
        {
            /* Get avgCorrect and maxCorrect */
            double avgScore = Double.parseDouble(currContest.get("avgScore").toString());
            int maxScore = Integer.parseInt(currContest.get("maxScore").toString());

            /* Get qAvgs array */
            JSONArray qAvgsArray = (JSONArray) currContest.get("qAvgs");
            out.println(contestNum + "\t" + numQuestions + " question(s), run, average correct: " + avgScore + "; maximum correct: " + maxScore);
            for (int i = 0; i < qAvgsArray.size(); ++i)
            {
                out.println("\t" + qNumArray.get(i) + "\t" + (Double.parseDouble(qAvgsArray.get(i).toString()) * 100) + "%");
            }
        }
        else
        {
            out.println(contestNum + "\t" + numQuestions + " question(s), not run");
        }

        out.println("DONE");
    }

    public void listContests()
    {
        JSONObject currContest;
        JSONArray qNumArray;
        int numQuestions, contestNum, maxScore;
        double avgScore;
        Boolean hasRun;

        for (int i = 0; i < contestBank.size(); ++i)
        {
            currContest = (JSONObject) contestBank.get(i);
            qNumArray = (JSONArray) currContest.get("qNums");
            numQuestions = qNumArray.size();
            contestNum = Integer.parseInt(currContest.get("cNum").toString());
            hasRun = ((currContest.get("hasRun").toString().equals("true")) ? true : false);
        
            if (hasRun) 
            {
                avgScore = Double.parseDouble(currContest.get("avgScore").toString());
                maxScore = Integer.parseInt(currContest.get("maxScore").toString());
                out.println(contestNum + "\t" + numQuestions + " question(s), run, average correct: " + avgScore + "; maximum correct: " + maxScore);
            }
            else
                out.println(contestNum + "\t" + numQuestions + " question(s), not run");
        }

        out.println("DONE");
    }

    public void beginContest(int contestNum)
    {
        /* Get qNumArray */
        if (!this.contestExists(contestNum))
        {
            out.println("Error creating contest: contest " + contestNum + " doesn't exist!");
            return;
        }

        int contestIndex = this.findContestIndex(contestNum);
        JSONObject currContest = (JSONObject) contestBank.get(contestIndex);
        JSONArray qNumArray = (JSONArray) currContest.get("qNums");

        /* Obtain question indices */
        int currQNum, currQIndex;
        JSONArray questionArray = new JSONArray();
        for (int i = 0; i < qNumArray.size(); ++i)
        {
            currQNum = Integer.parseInt(qNumArray.get(i).toString());
            currQIndex = this.findQuestionIndex(currQNum);

            if (currQIndex == -1)
            {
                System.out.println("Question " + currQIndex + " does not exist!" +
                                    " It will not be included in contest " + contestNum +
                                    ".");

                continue;                     
            }

            questionArray.add(questionBank.get(currQIndex));
        }

        ContestObject newContest = new ContestObject(contestNum, questionArray, contestBank);
        newContest.start();

        /* Busy wait because we're more important than everyone else */
        while (newContest.getPortNum() == -1)
            continue;

        String contestMessage = "Contest " + contestNum + " listening on port "
        + newContest.getPortNum();

        out.println(contestMessage);
    }

    public void createContest(int contestNum)
    {
        JSONObject newContest = new JSONObject();

        if (this.contestExists(contestNum))
        {
            out.println("Error: Contest " + contestNum + " already exists");
            return;
        }
        
        out.println("Contest " + (contestNum) + " is set");

        newContest.put("cNum",contestNum);
        
        JSONArray contestQNums = new JSONArray();
        newContest.put("hasRun",false);
        newContest.put("avgScore",null);
        newContest.put("maxScore",null);
        newContest.put("qNums",contestQNums);

        contestBank.add(newContest);

        this.updateContestBank();
    }

    public void addToContest(int contestNum, int qNum) 
    {
        if (!this.contestExists(contestNum))
            out.println("Error: contest " + contestNum + " does not exist");
        else if (!this.questionExists(qNum))
            out.println("Error: question " + qNum + " does not exist");

        int contestIndex = this.findContestIndex(contestNum);
        JSONObject currContest = (JSONObject) contestBank.get(contestIndex);
        JSONArray qNumArray = (JSONArray) currContest.get("qNums");
        qNumArray.add(qNum);
        currContest.put("qNums", qNumArray);

        out.println("Added question " + qNum + " to contest " + contestNum);
        this.updateContestBank();
    }

// ================================================================== //

/* SERVER SHUTDOWN STUFF */

// ================================================================== //

    /* Saves question bank and closes the server */
    public void closeServer()
    {
        /* Save the question bank! */
        System.out.println("Saving question bank...");
        //this.updateQNums();
        this.updateQuestionBank();

        /* Close the server */
        System.out.println("Closing server...");
        try
        {
            //server.close();
            serverSocket.close();
            out.close();
            in.close();
            
        }
        catch(IOException e)
        {
            System.out.println("Error closing server: " + e.toString());
        }

        this.on = false;
    }

    /* Encapsulated update function - can be used anywhere so that more than
       one client can connect at a given time */
    public void updateQuestionBank()
    {
        try
        {
            /* Now, save the question bank */
            JSONObject questionBankObj = new JSONObject();
            questionBankObj.put("questions",questionBank);
            FileWriter file = new FileWriter(this.qBankFilePath);
            file.write(questionBankObj.toJSONString());
            file.flush();
        }
        catch(IOException e)
        {
            System.out.println("Error writing file: " + e.toString());
        }
    }

    public void updateContestBank()
    {
        try
        {
            /* Now, save the question bank */
            JSONObject contestBankObj = new JSONObject();
            contestBankObj.put("contests",contestBank);
            FileWriter file = new FileWriter(this.cBankFilePath);
            file.write(contestBankObj.toJSONString());
            file.flush();
        }
        catch(IOException e)
        {
            System.out.println("Error writing file: " + e.toString());
        }
    }

    /* Updates the QNums (i.e. 1, 2, 4 becomes 1, 2, 3) */
    public void updateQNums()
    {
        for (int i = 0; i < questionBank.size(); ++i)
        {
            JSONObject currObj = (JSONObject) questionBank.get(i);
            currObj.put("qNum",i+1);
        }
    }

// ================================================================== //

/* HELPER FUNCTIONS */

// ================================================================== //

    public void updateLocalQuestionBank()
    {
        try {
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(new FileReader(this.qBankFilePath));
            JSONObject questionBankObj = (JSONObject) obj;
            this.questionBank = (JSONArray) questionBankObj.get("questions");
        }
        catch(ParseException | IOException e)
        {
            System.out.println("Error writing file: " + e.toString());
        }       
    }

    public void updateLocalContestBank()
    {
        try {
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(new FileReader(this.cBankFilePath));
            JSONObject contestBankObj = (JSONObject) obj;
            this.contestBank = (JSONArray) contestBankObj.get("contests");
        }
        catch(ParseException | IOException e)
        {
            System.out.println("Error writing file: " + e.toString());
        }
    }

    /* Helper function to check if the question exists in the Qbank */
    public boolean questionExists(int qNum)
    {
        if (this.findQuestionIndex(qNum) == -1)
            return false;
        return true;
    }

    /* Returns a question index */
    public int findQuestionIndex(int qNum)
    {
        JSONObject currObject;
        for (int i = 0; i < questionBank.size(); ++i)
        {
            currObject = (JSONObject) questionBank.get(i);
            if (Integer.parseInt((currObject.get("qNum")).toString()) == qNum)
                return i;
        }

        return -1;
    }

    public boolean contestExists(int cNum)
    {
        if (this.findContestIndex(cNum) == -1)
            return false;
        return true;
    }

    public int findContestIndex(int cNum)
    {
        JSONObject currObject;
        for (int i = 0; i < contestBank.size(); ++i)
        {
            currObject = (JSONObject) contestBank.get(i);
            if (Integer.parseInt((currObject.get("cNum")).toString()) == cNum)
                return i;
        }

        return -1;
    }
}

// ================================================================== //