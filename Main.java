import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

class Main {

    public static void main(String data[]) throws FileNotFoundException {

        FileOutputStream fos;
        DataOutputStream dos;

        try {
            // Opening original data
            File file = new File("dataanime.tsv");
            RandomAccessFile raf = new RandomAccessFile(file, "r");

            // Preparing input path
            fos = new FileOutputStream("anime.db");
            dos = new DataOutputStream(fos);

            // Skip header
            String header = raf.readLine();

            // Receive inputs
            String line;
            while ((line = raf.readLine()) != null) {
                String[] values = line.split("\t");

                Anime anime = new Anime();

                // Setting values
                anime.setTitle(values[0]);
                if (!values[1].equals("-"))
                    anime.setId(Integer.parseInt(values[1]));
                if (!values[2].equals("-"))
                    anime.setEpisodes(Integer.parseInt(values[2]));
                anime.setStatus(values[3]);
                if (!values[4].equals("-"))
                    anime.setStartAiring(anime.parseDateString(values[4]).getTime());
                if (!values[5].equals("-"))
                    anime.setEndAiring(anime.parseDateString(values[5]).getTime());
                anime.setStartingSeason(values[6]);
                anime.setBroadcastTime(values[7]);
                anime.setProducers(values[8].split(","));
                anime.setLicensors(values[9].split(","));
                anime.setStudios(values[10].split(","));
                anime.setSources(values[11]);
                anime.setGenres(values[12].split(","));
                anime.setDuration(values[13]);
                anime.setRating(values[14]);
                if (!values[15].equals("-"))
                    anime.setScore(Float.parseFloat(values[15]));
                if (!values[16].equals("-"))
                    anime.setScoredBy(Integer.parseInt(values[16]));
                if (!values[17].equals("-"))
                    anime.setNumOfMembers(Integer.parseInt(values[17]));
                if (!values[18].equals("-"))
                    anime.setNumOfFavorites(Integer.parseInt(values[18]));
                anime.setDescription(values[19]);
                anime.setType(values[20]);

                System.out.println(anime.toString());
                dos.writeUTF(anime.getTitle());
            }

            raf.close();

        } catch (FileNotFoundException e) {
            System.err.println("File not found: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.err.println("Number format error: " + e.getMessage());
        }
    }
}