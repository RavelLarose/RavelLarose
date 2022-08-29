import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.*;
import java.util.ArrayList;

import org.apache.poi.ss.formula.functions.NumericFunction;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.util.HashMap;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import javax.swing.*;
import java.util.List;
import java.util.Optional;



public class DataSort {

    public static void main (String [] args){
        //SCRIPTS DATABASE************************************************************************
        //initialize an arrayList to hold every line of dialogue
        ArrayList<Line> allDialogue = new ArrayList<Line>();
        String[] textFiles = new String[]{"script.xlsx", "s1s4_scripts.xlsx"};

        for (String file : textFiles){
            try{
                //find and open the data file
                FileInputStream scriptFis = new FileInputStream(new File(file));

                //make a workbook for the file, then a sheet object
                XSSFWorkbook scriptWb = new XSSFWorkbook(scriptFis);
                XSSFSheet scriptSheet = scriptWb.getSheetAt(0);

                String name = "";
                String dialogue = "";
                int number;
                int episode;

                //begin sorting through the data
                //for every row in the sheet
                for (Row row : scriptSheet){
                    //skipping the first row for column names
                    if (row.getRowNum() == 0)
                        continue;
                    //and for every cell in the rows
                    for (Cell cell : row){
                        try{
                            //if this comes from Column 0, it is the name of the speaking character
                            if (cell.getColumnIndex() == 0){
                                //grab the name of the character
                                name = cell.getStringCellValue();
                                name.toLowerCase();
                                //otherwise, it comes from Column 1 and is a line of dialogue
                            } else {
                                //grab the dialogue line
                                dialogue = cell.getStringCellValue();
                            }
                        } catch (IllegalStateException e) {
                            System.out.println("An illegal state exception was encountered. The row will be " +
                                    "converted to String.");
                            System.out.println("Row" + cell.getRowIndex() + " column " + cell.getColumnIndex());
                            double dialogueDouble = cell.getNumericCellValue();
                            dialogue = Double.toString(dialogueDouble);
                        }
                    }
                    //get the number of the line (+1 due to starting at 0, +1 due to heading row)
                    number = row.getRowNum();

                    /*
                    //and figure out which episode the line is from
                    //Episode 1 ends at Line 305
                    if (number < 305)
                        episode = 1;
                        //Episode 2 ends at Line 556
                    else if (number < 556)
                        episode = 2;
                        //Episode 3 ends at Line 825
                    else if (number < 825)
                        episode = 3;
                        //All other lines belong to episode 4
                    else
                        episode = 4;

                    //use all the data to create a new Line and store it in allDialogue
                     */
                    allDialogue.add(new Line(dialogue, name));
                }

            } catch (IOException e){
                System.out.println("IOException encountered: ");
                e.printStackTrace();
                System.exit(0);
            }
        }


        String line;
        String allLines = "";

        //variables for tracking data stats
        ArrayList<String> characters = new ArrayList<String>();
        ArrayList<Integer> characterLines = new ArrayList<Integer>();
        ArrayList<Term> termList = new ArrayList<Term>();
        int[] episodeLines = new int[4];
        int temp;
        int index;
        String[] lineTemp;
        Double avgLength = 0.0;
        Term tempTerm;

        //Go through all collected dialogue lines
        for (Line l : allDialogue){
           //System.out.println(l);

            line = l.getDialogue();
            allLines += l.getDialogue();

            //track number of terms per line
            //split on punctuation, space, etc
            lineTemp = line.split("[ ?.,:()&%$#!+*'/\"-]+");
            //add number of terms per line to the avgLength var (to finish calculating later)
            avgLength += lineTemp.length * 1.0;

            //track number of lines per episode
            //episodeLines[l.getEpisode()-1] += 1;

            //track number of lines per speaking character
            //some strings in the 'character name' section of the file also include stage directions or multiple characters
            //to avoid counting these as different characters, we parse for names
            //split l.getName() on space
            String[] nameTerms = l.getName().split(" ");
            //if the list is more than 1 term long, we need to parse it for the real character names
            if (nameTerms.length != 1)
                //loop through the list
                for (String name : nameTerms){
                    //if one of the terms is a character name
                    if (characters.contains(name.toLowerCase())){
                        //find the speaking character's index
                        index = characters.indexOf(name.toLowerCase());
                        //and increment their number of lines
                        temp = characterLines.get(index);
                        characterLines.set(index, temp+1);
                    }
                }
            //otherwise, if the name is only 1 term long, we can proceed as normal
            else {
                //if the name is new
                if (!characters.contains(l.getName().toLowerCase())){
                    //add to the character list. they have 1 line
                    characters.add(l.getName().toLowerCase());
                    characterLines.add(1);
                } else {
                    //otherwise, find the speaking character's index
                    index = characters.indexOf(l.getName().toLowerCase());
                    //and increment their number of lines
                    temp = characterLines.get(index);
                    characterLines.set(index, temp+1);
                }
            }

            //take the split dialogue and add the terms to an ArrayList as Term objects
            for (String t : lineTemp){
                //if the term isn't in the ArrayList yet
                if (termList.stream().noneMatch(p -> p.getTerm().equals(t))){
                    //make a new term using info from the current dialogue line
                    tempTerm = new Term(t);
                    tempTerm.addCharacter(l.getName());
                    //tempTerm.addEpisode(l.getEpisode());
                    //and add the term to the termList
                    termList.add(tempTerm);

                //if the term is already in the ArrayList
                } else {
                    //check to see if the term's character posting needs updating
                    if (termList.stream().noneMatch(p -> p.getCharacters().contains(l.getName()))){
                        //locate the Term object containing the current term
                        Optional<Term> optTerm = termList.stream().filter(p -> p.getTerm().equals(t)).findFirst();
                        tempTerm = optTerm.get();
                        //add the current character to the character posting
                        tempTerm.addCharacter(l.getName());
                    }
                    /*
                    //do the same with the term's episode posting
                    if (termList.stream().noneMatch(p -> p.getEpisodes().contains(l.getEpisode()))){
                        //locate the Term object containing the current term
                        Optional<Term> optTerm = termList.stream().filter(p -> p.getTerm().equals(t)).findFirst();
                        tempTerm = optTerm.get();
                        //add the current character to the character posting
                        tempTerm.addEpisode(l.getEpisode());
                    }
                     */
                }
            }


        }


        //TWEETS DATABASE***************************************************************************
        //read in the modern .xlsx file first
        //text-query-tweets.xlsx

        ArrayList<Tweet> allTweets = new ArrayList<Tweet>();
        //we have two tweet databases, so we'll put them in a list then iterate through that list to avoid rewriting
        //so much code
        String[] fileNames = new String[]{"b99.xlsx", "text-query-tweets.xlsx"};

        for (String file : fileNames){
            try {
                FileInputStream tweetFis = new FileInputStream(new File(file));

                //make a workbook for the file, then a sheet object
                XSSFWorkbook tweetWb = new XSSFWorkbook(tweetFis);
                XSSFSheet scriptSheet = tweetWb.getSheetAt(0);

                String content = "";
                String user = "";
                String date = "";
                String time = "";
                String[] dateTime;
                String[] userTemp;
                ArrayList<String> hashtags = new ArrayList<String>();
                String[] hashTemps;
                String hash;

                //begin sorting through the data
                //for every row in the sheet
                for (Row row : scriptSheet){
                    //(except the first row, all headings)
                    if (row.getRowNum() == 0)
                        continue;
                    //and for every cell in the rows
                    for (Cell cell : row) {
                        //check which column the cell is from
                        switch (cell.getColumnIndex()){

                            //if it's in column 2, it contains date & time information
                            case (2): {
                                //grab the information and split it on space (btwn date & time) and '+' (after time)
                                dateTime = cell.getStringCellValue().split("[ +]");

                                //then sort into date (YY/MM/DD) and time (Hr:Min:Sec)
                                date = dateTime[0].replaceAll("-","\\/");
                                time = dateTime[1];
                                break;
                            }

                            //if it's in column 4, it contains the tweet's contents
                            case (4): {
                                try {
                                    //grab the data
                                    content = cell.getStringCellValue();
                                    //fix some corruption of the data (mostly punctuation or emoji that java can't process)
                                    //remove ðŸ¤£, ðŸ˜, â€, â¤ï, , ðŸ¤, â€¦
                                    content = content.replaceAll("â€™", "'");
                                    content = content.replaceAll("ðŸ˜", "");
                                    content = content.replaceAll("ðŸ¤£", "");
                                    content = content.replaceAll("â\u009D¤ï", "");
                                    content = content.replaceAll("\u008F", "");
                                    content = content.replaceAll("ðŸ¤", "");
                                    content = content.replaceAll("â€¦", "");
                                    content = content.replaceAll("ðŸ¥°", "");
                                    content = content.replaceAll("ðŸ‘", "");
                                    content = content.replaceAll("ðŸ½", "");
                                } catch (IllegalStateException e) {
                                    System.out.println("Illegal State Exception encountered: missing data. The row will " +
                                            "be skipped");
                                    System.out.println("Row " + cell.getRowIndex() + " column " + cell.getColumnIndex());
                                    content = null;
                                }
                                break;
                            }

                            //if it's in column 6, it contains the username
                            case(6):{
                                try {
                                    //grab the information and split it on ' (marks each new section)
                                    userTemp = cell.getStringCellValue().split("'");

                                    //each cell is written the same way, so having split this way, the true username is
                                    //now contained in index 7. grab it
                                    user = userTemp[7];
                                } catch (IllegalStateException e){
                                    System.out.println("Illegal State Exception encountered: missing data. The row will " +
                                            "be skipped");
                                    System.out.println("Row " + cell.getRowIndex() + " column " + cell.getColumnIndex());
                                    content = null;
                                }
                                break;
                            }

                            //if it's in column 12, it denotes the language of the tweet. for simplicity, the system only
                            //processes tweets that are in english
                            case (12):
                                //if the language isn't english, skip this row
                                if (!cell.getStringCellValue().equals("en"))
                                    content = null;
                                break;

                            //if it's in column 25, it contains the hashtags
                            case(25):
                                try {
                                    //the hashtags are stored as: ['hashtag1', 'hashtag2'], so we parse on ','
                                    hashTemps = cell.getStringCellValue().split(",");

                                    //then we clean up the strings and add it to the arraylist, if there are any at all
                                    if (hashTemps.length != 0)
                                        for (int i = 0; i < hashTemps.length; i++) {
                                            hash = hashTemps[i].replaceAll("\\[", "");
                                            hash = (hashTemps[i].replaceAll("'", ""));
                                            hash = hashTemps[i].replaceAll("]", "");
                                            hashtags.add(hash);
                                        }
                                    else
                                        hashtags = null;
                                } catch (IllegalStateException e) {
                                    System.out.println("Illegal State Exception encountered: missing data. The row will " +
                                            "be skipped");
                                    System.out.println("Row " + cell.getRowIndex() + " column " + cell.getColumnIndex());
                                    content = null;
                                }

                                break;
                        }
                    }
                    //once all of the cells in the row have been iterated through, we can make the Tweet object and
                    //add it to the tweet list
                    if (content != null){
                        Tweet t = new Tweet(content, user, date, time, hashtags);
                        allTweets.add(t);
                        //System.out.println(t);
                    }
                }


            } catch (IOException e) {
                System.out.println("The system encountered and IOException:");
                e.printStackTrace();
                System.exit(0);
            }
        }

        //With the tweet objects created, we can now go through them and start tracking some stats
        //we want to know the characters most often mentioned and when

        String[] terms;
        ArrayList<String> tweetTerms = new ArrayList<String>();
        double avgTerm = 0.0;
        String[] tempContent;

        //indexes match with the characters array
        int[] characterTweets = new int[characters.size()];
        //indexes match with the characters array and each ArrayList tracks the dates for each character
        ArrayList<String>[] characterDates = new ArrayList[characters.size()];
        HashMap<String, Integer> commonDates = new HashMap<String, Integer>();

        //start by going through all the tweets
        for (Tweet t : allTweets){
            //for each tweet, we break it into terms
            //first, we check if the tweet contains a link
            if (t.getContent().contains("http")){
                //if it does, we remove the link(s) before proceeding
                tempContent = t.getContent().split("http");

                //then we set the link's content to equal the first string chunk, which is now just the contents
                t.setContent(tempContent[0]);

            }
            terms = t.getContent().split("[ ?.,:()&%$#!+*'/\"-]+");

            //note how long the tweet was, in terms
            avgTerm += terms.length * 1.0;

            //then search all the terms
            for (String term : terms){
                //case folding
                term = term.toLowerCase();
                //if it's a character's name
                if (characters.contains(term)){
                    //increment the character's tweet count, and add the date to the character's date list
                    int j = characters.indexOf(term);
                    characterTweets[j] += 1;
                    characterDates[j] = new ArrayList<String>();
                    characterDates[j].add(t.getDate());
                }

                //while we're here, add the terms to the tweetTerms arraylist if it's not there already
                if (!tweetTerms.contains(term)){
                    tweetTerms.add(term);
                }
            }

            //we also get the date
            String date = t.getDate();
            //if the date is not already in the hashmap
            if (!commonDates.containsKey(date)){
                //add the date with an initial count of 1
                commonDates.put(date, 1);
            } else {
                //if the date is already in the hashmap, we just increment the count
                int count = commonDates.get(date) + 1;
                commonDates.put(date, count);
            }

            //finally, print the tweet for testing purposes
            //System.out.println(t);
        }


        //print the tweet counts of each character

        for (int i = 0; i < characters.size(); i++){
            System.out.println(characters.get(i) + " is mentioned in " + characterTweets[i] + " tweets.");
        }

        //print the tweet dates
        for (String k : commonDates.keySet())
            System.out.println(commonDates.get(k) + " tweets were made on " + k);

        //print tweet number, tweet term number, and avg term length
        System.out.println("There are a total of " + allTweets.size() + " tweets.");
        System.out.println("There are a total of " + tweetTerms.size() + " terms in the tweets.");
        avgTerm /= allTweets.size();
        System.out.println("The average length of tweet in terms is " + avgTerm);


        //finish calculating the average length of dialogue lines by dividing by total number of lines
        avgLength /= allDialogue.size();

        //print out stats
        System.out.println("Speaking lines per character:");
        for (int i = 0; i < characters.size(); i++)
            System.out.println(characters.get(i) + " speaking lines: " + characterLines.get(i));

        System.out.println("Average number of terms per line of dialogue:" + avgLength);

        for (int i = 0; i < episodeLines.length; i++)
            System.out.println("Number of lines in episode " + (i+1) + ": " + episodeLines[i]);

        //total number of terms
        System.out.println("Total number of terms in all episodes: " + termList.size());




        //output everything to csv files
        try {
            //for the lines
            File csvLineOutput = new File("clean_dialogue.csv");
            FileWriter flw = new FileWriter(csvLineOutput);
            BufferedWriter blw = new BufferedWriter(flw);
            if (csvLineOutput.exists()){
                CSVPrinter csvPrint = new CSVPrinter(blw, CSVFormat.DEFAULT.withHeader("Character", "Line"));
                for (Line l : allDialogue){
                    csvPrint.printRecord(l.getName(), l.getDialogue());
                }
            } else
                System.out.println("File doesn't exist");

            //for the tweets
            File csvTweetOutput = new File("clean_tweets.csv");
            FileWriter ftw = new FileWriter(csvTweetOutput);
            BufferedWriter btw = new BufferedWriter(ftw);
            if (csvTweetOutput.exists()){
                CSVPrinter csvPrint = new CSVPrinter(btw, CSVFormat.DEFAULT.withHeader("User", "Content", "Date",
                        "Time"));
                for (Tweet t : allTweets){
                    csvPrint.printRecord(t.getUser(), t.getContent(), t.getDate(), t.getTime());
                }
            }


        } catch (IOException E) {
            System.out.println("There was an error:");
            E.printStackTrace();
            System.exit(0);
        }
    }
}
