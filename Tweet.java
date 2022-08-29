import java.util.ArrayList;
import java.util.Date;

public class Tweet {
    private String content;
    private String username;
    private String date;
    private String time;
    private ArrayList<String> hashtags;

    public Tweet(String content, String username, String date, String time, ArrayList<String> hashtags){
        this.content = content;
        this.username = username;
        this.date = date;
        this.time = time;
        this.hashtags = hashtags;
    }

    //getters and setters
    public String getContent(){return content;}
    public String getUser(){return username;}
    public String getDate(){return date;}
    public String getTime(){return time;}
    public ArrayList<String> getTags(){return hashtags;}
    public void setContent(String content){this.content = content;}

    //toString
    public String toString(){
        return "From " + username + " on " + date + ", " + time + ":\n\t" + content;
    }
}
