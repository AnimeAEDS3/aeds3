import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Anime{

    /* Attributes */

    protected String title;
    protected String type;
    protected int episodes;
    protected String status;
    protected Date startAiring;
    protected String startingSeason;
    protected String broadcastTime;
    protected String[] producers;
    protected String[] licensors;
    protected String[] studios;
    protected String sources;
    protected String[] genres;
    protected String duration;
    protected String rating;
    protected float score;
    protected int scoredBy;
    protected int numOfMembers;
    protected int numOfFavorites;
    protected String description;


    /* Constructor */

    public Anime() {
        this.title = "-";
        this.type = "-";
        this.episodes = -1;
        this.status = "-";
        this.startAiring = new Date(0);
        this.startingSeason = "-";
        this.broadcastTime = "-";
        this.producers = new String[0];
        this.licensors = new String[0];
        this.studios = new String[0];
        this.sources = "-";
        this.genres = new String[0];
        this.duration = "-";
        this.rating = "-";
        this.score = 0.0f;
        this.scoredBy = 0;
        this.numOfMembers = 0;
        this.numOfFavorites = 0;
        this.description = "-";
    }

    /* Date conversions */
    
    public long dateStringToMilliseconds(String dateString){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        long milliseconds = 0;
        try {
            Date date = sdf.parse(dateString);
            milliseconds = date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return milliseconds;
    }

    public String MillisecondsToDateString(long milliseconds) {
        Date date = new Date(milliseconds);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(date);
    }

    public Date parseDateString(String dateString) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            return sdf.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
    
}