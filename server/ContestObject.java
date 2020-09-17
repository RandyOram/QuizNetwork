import java.lang.Thread;
import java.net.*;
import java.io.*;
import java.util.Vector;
import org.json.simple.*;
import org.json.simple.parser.*;

public class ContestObject extends Thread
{
// ================================================================== //

/* MEMBERS */

// ================================================================== //

String cBankFilePath = "./server/ContestBank.json";
int portNum;
ServerSocket contestSocket;
int contestNum;
JSONArray questionArray;
JSONArray contestBank;

Vector<String> contestantNames = new Vector<String>();
Vector<Socket> contestantSockets = new Vector<Socket>();
Vector<BufferedReader> contestantIns = new Vector<BufferedReader>();
Vector<PrintWriter> contestantOuts = new Vector<PrintWriter>();

// ================================================================== //

/* CONSTRUCTOR */

// ================================================================== //

    public ContestObject(int contestNum, JSONArray questionArray, JSONArray contestBank)
    {
        this.portNum = -1;
        this.contestBank = contestBank;
        this.questionArray = questionArray;
        this.contestNum = contestNum;
    }

// ================================================================== //

/* THREAD STARTUP */

// ================================================================== //

    /* Thread starting */
    public void run() 
    {
        try {
            this.acceptContestants();
            this.beginContest();
        }
        catch(Exception e) {
            System.out.println("Error: " + e.toString()); 
        }
    }

    public int getPortNum() 
    {
        return this.portNum;
    }

// ================================================================== //

/* CONTEST SETUP */

// ================================================================== //

    public void acceptContestants() 
    {
        try {
            this.contestSocket = new ServerSocket(0);
            this.contestSocket.setSoTimeout(60000); // BLESS THIS METHOD
            this.portNum = contestSocket.getLocalPort();
            System.out.println("Contest " + this.contestNum + " listening on port "
                                 + this.portNum);
            
            int currContestant = 0;
            while (true) 
            {
                Socket contestantSocket = this.contestSocket.accept();
                this.contestantSockets.add(contestantSocket);
                this.contestantOuts.add(new PrintWriter(contestantSocket.getOutputStream(), true));
                this.contestantIns.add(new BufferedReader(new InputStreamReader(contestantSocket.getInputStream())));
                this.addContestantName(currContestant);

                ++currContestant;
            }
        }
        catch (InterruptedIOException e) {
            System.out.println("No longer accepting new contestants for contest " 
                                + this.contestNum + ".");
        }
        catch (Exception e) {
            System.out.println("Error: asdsadasd " + e.toString()); 
        }
    }

    public void addContestantName(int contestIndex)
    {
        try {
            Socket contestantSocket = contestantSockets.get(contestIndex);
            PrintWriter out = contestantOuts.get(contestIndex);
            BufferedReader in = contestantIns.get(contestIndex);
            String nickname;

            while (true)  
            {
                nickname = in.readLine();
                if (this.nicknameExists(nickname)) {
                    out.println("Error: Nickname " + nickname + " is already in use.");
                }
                else {
                    this.contestantNames.add(nickname);
                    out.println("Hello " + nickname + ", get ready for contest!");
                    return;
                }
            }
        }
        catch(IOException e) {
            System.out.println("Error: " + e.toString());
        }
    }

    public boolean nicknameExists(String nickname)
    {
        for (int i = 0; i < this.contestantNames.size(); ++i)
        {
            if (this.contestantNames.get(i).equals(nickname))
                return true;
        }

        return false;
    }

// ================================================================== //

/* CONTEST */

// ================================================================== //

    public void beginContest()
    {
        PrintWriter out;
        double[] averages = new double[this.questionArray.size()];
        int[] correct = new int[this.contestantNames.size()];
        int[] scores = new int[this.contestantNames.size()];
        int maxScore = 0;
        /* Iterate through all of the questions! */
        for (int i = 0; i < this.questionArray.size(); ++i)
        {
            for (int j = 0; j < this.contestantNames.size(); ++j)
            {
                this.dumpQuestion(i, this.contestantOuts.get(j));
            }

            /* Check answers */
            for (int j = 0; j < this.contestantNames.size(); ++j)
            {
                if (this.checkAnswer(i, this.contestantIns.get(j), this.contestantOuts.get(j)))
                    correct[j] = 1;
                else
                    correct[j] = 0;
            }

            /* Get current question average */
            int sum = 0;
            for (int j = 0; j < correct.length; ++j)
            {
                scores[j] += correct[j];
                sum += correct[j];
            }

            maxScore = this.getMaxScore(scores);
            averages[i] = (((double) sum ) / ((double) correct.length));
            
            String correctness = "";

            /* Update contestants with current results */
            for (int j = 0; j < this.contestantNames.size(); ++j)
            {
                out = contestantOuts.get(j);
                if (correct[j] == 0)
                    correctness = "Incorrect. ";
                else
                    correctness = "Correct. ";

                out.println(correctness + ((int) 100*averages[i]) + "% of contestants answered " +
                                "this question correctly.");

                out.println("Your score is " + scores[j] + "/" + (i + 1) + ". The top score is currently " + maxScore + "/" + (i + 1) + ".");
            }
        }

        /* Notify the contestants that the contest is over */
        for (int j = 0; j < this.contestantNames.size(); ++j)
        {
            out = contestantOuts.get(j);
            out.println("The contest is over - thanks for playing, " + contestantNames.get(j) + "!");
        }
        
        /* Update the contest bank */
        int cIndex = this.findContestIndex(this.contestNum);
        JSONObject currContest = (JSONObject) this.contestBank.get(cIndex);
        int timesRun = Integer.parseInt(currContest.get("timesRun").toString());
        int prevNumParticipants = Integer.parseInt(currContest.get("numParticipants").toString());
        int newNumParticipants = prevNumParticipants + this.contestantNames.size();
        currContest.put("numParticipants",newNumParticipants);
        
        /* Calculate overall average score */
        int sum = 0;
        for (int i = 0; i < scores.length; ++i)
        {
            sum += scores[i];
        }
        double avgScore = (((double) sum ) / ((double) scores.length));
        double prevAvgScore = Double.parseDouble(currContest.get("avgScore").toString());
        currContest.put("avgScore", (((prevAvgScore * timesRun) + avgScore)/(timesRun + 1)));

        /* Update information on how many times contest has been run */
        currContest.put("hasRun",true);
        currContest.put("timesRun",(timesRun + 1));

        /* Update score fields*/
        int prevMax = Integer.parseInt(currContest.get("maxScore").toString());

        if (maxScore > prevMax)
            currContest.put("maxScore",maxScore);
        
        JSONArray avgScoreArray = (JSONArray) currContest.get("qAvgs");
        double currAvgScore, newAvgScore;
        for (int i = 0; i < avgScoreArray.size(); ++i)
        {
            prevAvgScore = Double.parseDouble(avgScoreArray.get(i).toString());
            newAvgScore = (((prevNumParticipants*prevAvgScore) + (this.contestantNames.size()*averages[i])) / newNumParticipants);
            avgScoreArray.set(i,newAvgScore);
        }
        currContest.put("qAvgs",avgScoreArray);

        /* Save the contest bank */
        this.contestBank.set(cIndex, currContest);
        this.updateContestBank();
    }

    public void dumpQuestion(int qIndex, PrintWriter out)
    {
        JSONObject question = (JSONObject) questionArray.get(qIndex);
        StringBuilder questionString = new StringBuilder();

        questionString.append("Question " + (qIndex + 1) + ":\r\n");
        questionString.append(question.get("question") + "\r\n");

        JSONArray answerArray = (JSONArray) question.get("answers");
        for (int i = 0; i < answerArray.size(); ++i)
        {
            questionString.append(answerArray.get(i) + "\r\n");
        }

        questionString.append("DONE\r\n");

        out.println(questionString.toString());
    }

    public boolean checkAnswer(int qIndex, BufferedReader in, PrintWriter out)
    {
        JSONObject currQuestion = (JSONObject) questionArray.get(qIndex);
        String correctAnswer = currQuestion.get("correct").toString();
        try {
            String contestantAnswer = in.readLine();
            return (contestantAnswer.equals(correctAnswer));
        }
        catch (IOException e) {
            System.out.println("Error: " + e.toString());
        }

        return false;
    }

    public int getMaxScore(int[] scores)
    {
        int max = 0;

        for (int i = 0; i < scores.length; ++i)
        {
            if (scores[i] > max)
                max = scores[i];
        }

        return max;
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
}