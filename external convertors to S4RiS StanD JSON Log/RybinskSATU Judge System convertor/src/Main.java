import java.io.*;
import java.util.*;

/**
 * Convertor from log of Rybinsk SATU Judge System to S4RiS StanD JSON log.
 * @author Strekalovsky Oleg
 */
public class Main {

   private static class ParseError extends Exception {

      private static final long serialVersionUID = 7350867548172196232L;

      public ParseError(String string) {
         super(string);
      }
   }

   private static class Contest {

      private class ContestPrinter {

         private String tab = "   ";

         private String getTabs(int cnt) {
            String st = new String();
            for (int i = 0; i < cnt; i++) {
               st += tab;
            }
            return st;
         }

         private String getLine(String line, int tabs) {
            StringBuilder sb = new StringBuilder();
            sb.append(getTabs(tabs));
            sb.append(line);
            sb.append("\n");
            return sb.toString();
         }

         private String getString(String content) {
            return "\"" + content + "\"";
         }

         private String getInt(int num) {
            return num + "";
         }

         private String getBoolean(boolean val) {
            return val + "";
         }

         public String appendHeader() {
            return getLine("{", 0);
         }

         public String appendContestInfo() {
            StringBuilder sb = new StringBuilder();
            int tabs = 1;
            sb.append(getLine(getString("contestName") + ": "
                  + getString(getContestCaption()) + ",", tabs));
            sb.append(getLine(getString("freezeTimeMinutesFromStart") + ": "
                  + getInt(getFreezeTimeMin()) + ",", tabs));
            return sb.toString();
         }

         public Object appendProblems() {
            StringBuilder sb = new StringBuilder();
            int tabs = 1;
            sb.append(getLine(getString("problemLetters") + ": ", tabs));
            sb.append(getLine("[", tabs));
            tabs++;
            for (int i = 0; i < problems.size(); i++) {
               String problemLetter = problems.get(i);
               String st = getString(problemLetter);
               if (i != problems.size() - 1) {
                  st += ",";
               }
               sb.append(getLine(st, tabs));
            }
            tabs--;
            sb.append(getLine("],", tabs));
            return sb.toString();
         }

         public String apendContestants() {
            StringBuilder sb = new StringBuilder();
            int tabs = 1;
            sb.append(getLine(getString("contestants") + ":", tabs));
            sb.append(getLine("[", tabs));
            tabs++;
            int nTeams = teams.size();
            int curTeam = 0;
            for (String teamName : teams.values()) {
               String st = getString(teamName);
               if (curTeam != nTeams - 1) {
                  st += ",";
               }
               sb.append(getLine(st, tabs));
            }
            tabs--;
            sb.append(getLine("],", tabs));
            return sb.toString();
         }

         public String appendRuns() {
            StringBuilder sb = new StringBuilder();
            int tabs = 1;
            sb.append(getLine(getString("runs") + ":", tabs));
            sb.append(getLine("[", tabs));
            tabs++;
            for (int i = 0; i < runs.size(); i++) {
               Run run = runs.get(i);
               sb.append(getLine("{", tabs));
               tabs++;
               sb.append(getLine(
                     getString("contestant") + ": "
                           + getString(run.getContestant()) + ",", tabs));
               sb.append(getLine(getString("problemLetter") + ": "
                     + getString(run.getProblemLetter()) + ",", tabs));
               sb.append(getLine(getString("timeMinutesFromStart") + ": "
                     + getInt(run.getTimeMinutesFromStart()) + ",", tabs));
               sb.append(getLine(
                     getString("success") + ": " + getBoolean(run.isSuccess())
                           + ",", tabs));
               tabs--;
               String st = getTabs(tabs) + "}";
               if (i != runs.size() - 1) {
                  st += ",";
               }
               sb.append(getLine(st, 0));
            }
            tabs--;
            sb.append(getLine("]", tabs));
            return sb.toString();
         }

         public String appendFooter() {
            return getLine("}", 0);
         }

      }

      private class Run {
         private final String contestant;
         private final String problemLetter;
         private final int timeMinutesFromStart;
         private final boolean isSuccess;

         Run(String contestant, String problemLetter, int time,
               boolean isSuccess) {
            this.contestant = contestant;
            this.problemLetter = problemLetter;
            this.timeMinutesFromStart = time;
            this.isSuccess = isSuccess;
         }

         public String getContestant() {
            return contestant;
         }

         public String getProblemLetter() {
            return problemLetter;
         }

         public int getTimeMinutesFromStart() {
            return timeMinutesFromStart;
         }

         public boolean isSuccess() {
            return isSuccess;
         }

      }

      private int freezeTimeMin;
      private String contestCaption = "NoName contest";

      public Contest() {
      }

      public int getFreezeTimeMin() {
         return freezeTimeMin;
      }

      public void setFreezeTimeMin(int freezeTimeMin) {
         this.freezeTimeMin = freezeTimeMin;
      }

      public String getContestCaption() {
         return contestCaption;
      }

      public void setContestCaption(String contestCaption) {
         this.contestCaption = contestCaption;
      }

      ArrayList<Run> runs = new ArrayList<Run>();
      TreeMap<String, String> teams = new TreeMap<String, String>();
      ArrayList<String> problems = new ArrayList<String>();

      public void addRun(String contestant, String problemLetter, int time,
            boolean isSuccess) {
         Run run = new Run(contestant, problemLetter, time, isSuccess);
         runs.add(run);
      }

      public String getContestLog() {
         StringBuilder sb = new StringBuilder();
         ContestPrinter printer = new ContestPrinter();

         sb.append(printer.appendHeader());
         sb.append(printer.appendContestInfo());
         sb.append(printer.appendProblems());
         sb.append(printer.apendContestants());
         sb.append(printer.appendRuns());
         sb.append(printer.appendFooter());
         return sb.toString();
      }
   }

   void run() {
      try {
         Contest contest = new Contest();
         final String teamsSubFolderName = "RegisterPath";
         readTeams(contest.teams, teamsSubFolderName);

         final String problemLogFileName = "Problems.log";
         reamProblems(contest.problems, problemLogFileName);

         final String contestInfoLogFileName = "ContestInfo.log";
         readContestInfo(contest, contestInfoLogFileName);

         final String runsLogFileName = "Messages.log";
         readRuns(contest, runsLogFileName);

         final String outputFileName = "contest-json.txt";
         writeOutputLog(contest, outputFileName);

      } catch (Exception e) {
         System.out.println(e.getMessage());
         e.printStackTrace();
         return;
      }

   }

   private void readRuns(Contest contest, String runsLogFileName)
         throws ParseError {
      try {
         Scanner in = new Scanner(new FileReader(runsLogFileName));
         in.nextLine();
         while (in.hasNextLine()) {
            String line = in.nextLine();
            int curCharPos = line.indexOf("Team:");
            if (curCharPos == -1) {
               throw new ParseError("can't find pos of team");
            }
            curCharPos += 6;
            String teamCodeName = "";
            while (line.charAt(curCharPos) != ' ') {
               teamCodeName += line.charAt(curCharPos);
               curCharPos++;
            }
            String teamName = null;
            if (contest.teams.containsKey(teamCodeName)) {
               teamName = contest.teams.get(teamCodeName);
            } else {
               throw new ParseError("can't find team with code name: "
                        + teamCodeName);
            }

            while (line.charAt(curCharPos) != '[') {
               curCharPos++;
            }
            curCharPos += 2; // [+

            String timeSt = "";
            while (line.charAt(curCharPos) != ']') {
               timeSt += line.charAt(curCharPos);
               curCharPos++;
            }

            int time = parseTime(timeSt);

            curCharPos = line.indexOf("Problem ", curCharPos);
            String problemLetter = null;
            if (curCharPos == -1) {
               throw new ParseError("can't find problem name");
            }
            curCharPos += 8;
            problemLetter = line.charAt(curCharPos) + "";
            curCharPos += 2; // skip ". "

            Boolean isSuccess = null;
            if (line.indexOf("Accepted.", curCharPos) != -1) {
               isSuccess = true;
            } else {
               isSuccess = false;
            }

            contest.addRun(teamName, problemLetter, time, isSuccess);
         }
      } catch (Exception e) {
         throw new ParseError("Can't read runs log file: " + runsLogFileName
               + " \n Cause: " + e);
      }
   }

   private int parseTime(String timeSt) {
      int hour = Integer.parseInt(timeSt.substring(0, 2));
      int min = Integer.parseInt(timeSt.substring(3, 5));
      int sec = Integer.parseInt(timeSt.substring(6, 8));
      return hour * 60 + min;
   }

   private void writeOutputLog(Contest contest, String outputFileName)
         throws ParseError {
      try {
         PrintWriter out = null;
         String output = contest.getContestLog();
         try {
            out = new PrintWriter(outputFileName);
            out.print(output);
            System.out.println("Successful Done. Look at file: "
                  + outputFileName);
         } finally {
            if (out != null) {
               out.close();
            }
         }
      } catch (Exception e) {
         throw new ParseError("Can't write output log file: " + outputFileName
               + " \n Cause: " + e);
      }
   }

   private void readContestInfo(Contest contest, String contestInfoLogFileName)
         throws ParseError {
      try {
         Scanner in = null;
         try {
            in = new Scanner(new FileReader(contestInfoLogFileName));
            String contestName = in.nextLine();
            int freezeTime = 0;
            if (in.hasNext()) {
               freezeTime = in.nextInt();
            }
            contest.setContestCaption(contestName);
            contest.setFreezeTimeMin(freezeTime);
         } finally {
            if (in != null) {
               in.close();
            }
         }
      } catch (Exception e) {
         throw new ParseError("Can't read contest info file: "
               + contestInfoLogFileName + " \n Cause: " + e);
      }

   }

   private void reamProblems(ArrayList<String> problems,
         String problemLogFileName) throws ParseError {
      try {
         Scanner in = null;
         try {
            in = new Scanner(new FileReader(problemLogFileName));
            while (in.hasNextLine()) {
               problems.add(in.nextLine());
            }
         } finally {
            if (in != null) {
               in.close();
            }
         }
      } catch (Exception e) {
         throw new ParseError("Can't read problems log file: "
               + problemLogFileName + " \n Cause: " + e);
      }
   }

   private void readTeams(TreeMap<String, String> teams,
         String teamsSubFolderName) throws ParseError {
      try {
         File file = new File(".\\" + teamsSubFolderName);
         for (File teamFile : file.listFiles()) {
            if (teamFile.isFile() && teamFile.canRead()) {
               final String teamCodeName = teamFile.getName().toUpperCase();
               Scanner in = null;
               try {
                  in = new Scanner(teamFile);
                  String teamName = in.nextLine();
                  teams.put(teamCodeName, teamName);
               } finally {
                  if (in != null) {
                     in.close();
                  }
               }
            }
         }
      } catch (Exception e) {
         throw new ParseError("Can't read teams files.\n Cause: " + e);
      }
   }

   public static void main(String... args) {
      new Main().run();
   }
}