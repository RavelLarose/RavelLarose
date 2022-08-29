public class Line {
    //track the dialogue itself, the name of the character speaking, the line number, and the episode number
    private String dialogue;
    private String name;
    //private int number;
    //private int episode;

    public Line(String dialogue, String name){
        this.dialogue = dialogue;
        this.name = name;
        //this.number = number;
        //this.episode = episode;
    }

    public String getDialogue(){return dialogue;}
    public String getName(){return name;}
    //public int getNumber(){return number;}
    //public int getEpisode(){return episode;}

    public String toString(){
        return name + ": " + dialogue;
    }
}
