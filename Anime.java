import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

public class Anime{

    /* Attributes */

    protected String title;
    protected int id;
    protected int episodes;
    protected String status;
    protected long startAiring;
    protected long endAiring;
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
    protected String type;


    /* Constructor */

    public Anime() {
        this.title = "-";
        this.id = 
        this.episodes = -1;
        this.status = "-";
         this.startAiring = 0;
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
        this.type = "-";
    }

    /* Getters and setters */

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getEpisodes() {
        return episodes;
    }

    public void setEpisodes(int episodes) {
        this.episodes = episodes;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getStartAiring() {
        return startAiring;
    }

    public void setStartAiring(long startAiring) {
        this.startAiring = startAiring;
    }

    public long getEndAiring() {
        return endAiring;
    }

    public void setEndAiring(long endAiring) {
        this.endAiring = endAiring;
    }

    public String getStartingSeason() {
        return startingSeason;
    }

    public void setStartingSeason(String startingSeason) {
        this.startingSeason = startingSeason;
    }

    public String getBroadcastTime() {
        return broadcastTime;
    }

    public void setBroadcastTime(String broadcastTime) {
        this.broadcastTime = broadcastTime;
    }

    public String[] getProducers() {
        return producers;
    }

    public void setProducers(String[] producers) {
        this.producers = producers;
    }

    public String[] getLicensors() {
        return licensors;
    }

    public void setLicensors(String[] licensors) {
        this.licensors = licensors;
    }

    public String[] getStudios() {
        return studios;
    }

    public void setStudios(String[] studios) {
        this.studios = studios;
    }

    public String getSources() {
        return sources;
    }

    public void setSources(String sources) {
        this.sources = sources;
    }

    public String[] getGenres() {
        return genres;
    }

    public void setGenres(String[] genres) {
        this.genres = genres;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }

    public int getScoredBy() {
        return scoredBy;
    }

    public void setScoredBy(int scoredBy) {
        this.scoredBy = scoredBy;
    }

    public int getNumOfMembers() {
        return numOfMembers;
    }

    public void setNumOfMembers(int numOfMembers) {
        this.numOfMembers = numOfMembers;
    }

    public int getNumOfFavorites() {
        return numOfFavorites;
    }

    public void setNumOfFavorites(int numOfFavorites) {
        this.numOfFavorites = numOfFavorites;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    /* CRUD */

    private static void writeAnime(RandomAccessFile file, Anime anime, int recordNumber) throws IOException {
        file.seek(recordNumber * 1024);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(anime);
        byte[] bytes = baos.toByteArray();
        file.write(bytes);
    }

    private static Anime readAnime(RandomAccessFile file, int recordNumber) throws IOException {
        file.seek(recordNumber * 1024);
        byte[] bytes = new byte[1024];
        file.read(bytes);
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bais);
        try {
            return (Anime) ois.readObject();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void updateAnime(RandomAccessFile file, Anime anime, int recordNumber) throws IOException {
        writeAnime(file, anime, recordNumber);
    }

    private static void deleteAnime(RandomAccessFile file, int recordNumber) throws IOException {
        file.seek(recordNumber * 1024);
        byte[] bytes = new byte[1024];
        file.write(bytes);
    }

    /* To string */

    @Override
    public String toString() {
        return "Anime{" +
                "title='" + title + '\'' +
                ", id=" + id +
                ", episodes=" + episodes +
                ", status='" + status + '\'' +
                ", startAiring=" + startAiring +
                ", startingSeason='" + startingSeason + '\'' +
                ", broadcastTime='" + broadcastTime + '\'' +
                ", producers=" + Arrays.toString(producers) +
                ", licensors=" + Arrays.toString(licensors) +
                ", studios=" + Arrays.toString(studios) +
                ", sources='" + sources + '\'' +
                ", genres=" + Arrays.toString(genres) +
                ", duration='" + duration + '\'' +
                ", rating='" + rating + '\'' +
                ", score=" + score +
                ", scoredBy=" + scoredBy +
                ", numOfMembers=" + numOfMembers +
                ", numOfFavorites=" + numOfFavorites +
                ", description='" + description + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}