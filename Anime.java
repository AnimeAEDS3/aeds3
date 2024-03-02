import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Scanner;

public class Anime {

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
        this.id = this.episodes = -1;
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

    /* Getters e setters */

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

    /* Conversões entre data, string e milisegundos */

    public static long dateStringToMilliseconds(String dateString) {
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

    /* To bytes */
    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeInt(id);
        dos.writeUTF(title);
        dos.writeInt(episodes);
        dos.writeUTF(status);
        dos.writeLong(startAiring);
        dos.writeLong(endAiring);
        dos.writeUTF(startingSeason);
        dos.writeUTF(broadcastTime);
        writeStringArray(dos, producers);
        writeStringArray(dos, licensors);
        writeStringArray(dos, studios);
        dos.writeUTF(sources);
        writeStringArray(dos, genres);
        dos.writeUTF(duration);
        dos.writeUTF(rating);
        dos.writeFloat(score);
        dos.writeInt(scoredBy);
        dos.writeInt(numOfMembers);
        dos.writeInt(numOfFavorites);
        dos.writeUTF(description);
        dos.writeUTF(type);

        return baos.toByteArray();
    }

    private static void writeStringArray(DataOutputStream dos, String[] array) throws IOException {
        // Escreve o tamanho do array
        dos.writeInt(array.length);
        // Escreve cada elemento do array
        for (String element : array) {
            dos.writeUTF(element);
        }
    }

    public void fromByteArray(byte[] ba) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(ba);
        DataInputStream dis = new DataInputStream(bais);
        id = dis.readInt();
        title = dis.readUTF();
        episodes = dis.readInt();
        status = dis.readUTF();
        startAiring = dis.readLong();
        endAiring = dis.readLong();
        startingSeason = dis.readUTF();
        broadcastTime = dis.readUTF();
        producers = readStringArray(dis);
        licensors = readStringArray(dis);
        studios = readStringArray(dis);
        sources = dis.readUTF();
        genres = readStringArray(dis);
        duration = dis.readUTF();
        rating = dis.readUTF();
        score = dis.readFloat();
        scoredBy = dis.readInt();
        numOfMembers = dis.readInt();
        numOfFavorites = dis.readInt();
        description = dis.readUTF();
        type = dis.readUTF();
    }

    private String[] readStringArray(DataInputStream dis) throws IOException {
        int length = dis.readInt();
        String[] array = new String[length];
        for (int i = 0; i < length; i++) {
            array[i] = dis.readUTF();
        }
        return array;
    }

    /* To string */

    @Override
    public String toString() {
        return "Anime{" +
                "title='" + title + '\'' +
                ", id=" + id +
                ", episodes=" + episodes +
                ", status='" + status + '\'' +
                ", startAiring=" + MillisecondsToDateString(startAiring).toString() +
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

    public static Anime fromStringArray(String[] values) {
        Anime anime = new Anime();
    
        anime.setTitle(values[0]);
        anime.setId(Integer.parseInt(values[1]));
        anime.setEpisodes(!values[2].equals("-") ? Integer.parseInt(values[2]) : anime.getEpisodes());
        anime.setStatus(values[3]);
        anime.setStartAiring(!values[4].equals("-") ? dateStringToMilliseconds(values[4]) : anime.getStartAiring());
        anime.setEndAiring(!values[5].equals("-") ? dateStringToMilliseconds(values[5]) : anime.getEndAiring());
        anime.setStartingSeason(values[6]);
        anime.setBroadcastTime(values[7]);
        anime.setProducers(!values[8].equals("-") ? values[8].split("\t") : anime.getProducers());
        anime.setLicensors(!values[9].equals("-") ? values[9].split("\t") : anime.getLicensors());
        anime.setStudios(!values[10].equals("-") ? values[10].split("\t") : anime.getStudios());
        anime.setSources(values[11]);
        anime.setGenres(!values[12].equals("-") ? values[12].split("\t") : anime.getGenres());
        anime.setDuration(values[13]);
        anime.setRating(values[14]);
        anime.setScore(!values[15].equals("-") ? Float.parseFloat(values[15]) : anime.getScore());
        anime.setScoredBy(!values[16].equals("-") ? Integer.parseInt(values[16]) : anime.getScoredBy());
        anime.setNumOfMembers(!values[17].equals("-") ? Integer.parseInt(values[17]) : anime.getNumOfMembers());
        anime.setNumOfFavorites(!values[18].equals("-") ? Integer.parseInt(values[18]) : anime.getNumOfFavorites());
        anime.setDescription(values[19]);
        anime.setType(values[20]);
    
        return anime;
    }

    public static Anime promptUser(int newId){
        Scanner scanner = new Scanner(System.in);
    
        Anime anime = new Anime();
    
        System.out.print("Digite o título do anime: ");
        anime.setTitle(scanner.nextLine());
    
        System.out.print("Digite o número de episódios: ");
        anime.setEpisodes(scanner.nextInt());
        scanner.nextLine(); // Consume newline
        
        // System.out.print("Digite o status do anime: ");
        // anime.setStatus(scanner.nextLine());
        
        // System.out.print("Digite a data de início de transmissão (formato: yyyy-MM-dd): ");
        // anime.setStartAiring(dateStringToMilliseconds(scanner.nextLine()));
        
        // System.out.print("Digite a data de fim de transmissão (formato: yyyy-MM-dd): ");
        // anime.setEndAiring(dateStringToMilliseconds(scanner.nextLine()));
        
        // System.out.print("Digite a temporada de início: ");
        // anime.setStartingSeason(scanner.nextLine());
        
        // System.out.print("Digite o horário de transmissão: ");
        // anime.setBroadcastTime(scanner.nextLine());
        
        // System.out.print("Digite os produtores (separados por tabulação): ");
        // anime.setProducers(scanner.nextLine().split("\t"));
        
        // System.out.print("Digite os licenciantes (separados por tabulação): ");
        // anime.setLicensors(scanner.nextLine().split("\t"));
        
        // System.out.print("Digite os estúdios (separados por tabulação): ");
        // anime.setStudios(scanner.nextLine().split("\t"));
        
        // System.out.print("Digite a fonte: ");
        // anime.setSources(scanner.nextLine());
        
        // System.out.print("Digite os gêneros (separados por tabulação): ");
        // anime.setGenres(scanner.nextLine().split("\t"));
        
        // System.out.print("Digite a duração de cada episódio: ");
        // anime.setDuration(scanner.nextLine());
        
        // System.out.print("Digite a classificação indicativa: ");
        // anime.setRating(scanner.nextLine());
        
        // System.out.print("Digite a pontuação: ");
        // anime.setScore(scanner.nextFloat());
        
        // System.out.print("Digite o número de pessoas que pontuaram: ");
        // anime.setScoredBy(scanner.nextInt());
        
        // System.out.print("Digite o número de membros: ");
        // anime.setNumOfMembers(scanner.nextInt());
        
        // System.out.print("Digite o número de favoritos: ");
        // anime.setNumOfFavorites(scanner.nextInt());
        // scanner.nextLine(); // Consume newline
        
        // System.out.print("Digite a descrição: ");
        // anime.setDescription(scanner.nextLine());
        
        // System.out.print("Digite o tipo de anime: ");
        // anime.setType(scanner.nextLine());
    
        // Imprimir o objeto anime criado
        System.out.print("Novo anime criado: ");
        System.out.println(anime.getTitle() + " ID: " + newId);

        scanner.close();
    
        return anime;
    }

    public static void animeInterface(){
                System.out.println("    _          _                ");
                System.out.println("   / \\   _ __ (_)_ __ ___   ___ ");
                System.out.println("  / _ \\ | '_ \\| | '_ ` _ \\ / _ \\");
                System.out.println(" / ___ \\| | | | | | | | | | | _/");
                System.out.println("/_/   \\_\\_| |_|_|_| |_| |_|\\___|");
                System.out.println();
                System.out.println("1. Carregar base de dados original");
                System.out.println("2. Criar novo registro (CREATE)");
                System.out.println("3. Busca por ID (READ)");
                System.out.println("4. Deletar um registro por ID (DELETE)");
                System.out.println("5. Update em um registro por ID (UPDATE)");
                System.out.println("6. Sair");
                System.out.print(">> ");
        }
    
}