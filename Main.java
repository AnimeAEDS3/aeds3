import java.io.*;
import java.util.*;

class Main {

    public static void main(String data[]) throws FileNotFoundException {
        FileOutputStream fos;
        DataOutputStream dos;
        FileInputStream fis;
        DataInputStream dis;
        Scanner scanner = new Scanner(System.in);

        try {
            System.out.println("Escolha uma opção:");
            System.out.println("1. Realizar a carga da base de dados selecionada");
            System.out.println("2. Ler ID para imprimir informações do objeto");
            System.out.print("Opção: ");
            int option = scanner.nextInt();

            switch (option) {
                case 1:
                    // Código para realizar a carga da base de dados selecionada
                    // Aqui você pode colocar o código que estava dentro do try antes

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

                        byte[] ba;
                        ba = anime.toByteArray(); // objeto convertido em array de bytes
                        dos.writeBoolean(true); // escrevendo a lápide antes do indicador de tamanho
                        dos.writeInt(ba.length); // indicador de tamanho
                        dos.write(ba);
                        System.out.println(anime.toString());
                    }
                    raf.close();

                    // Fechar o fluxo de saída após terminar de escrever
                    dos.close();
                    fos.close();
                    break;

                case 2:
                    // Código para ler um ID e imprimir as informações do objeto
                    System.out.print("Digite o ID do anime que deseja buscar: ");
                    int idBuscado = scanner.nextInt(); // id que o usuário deseja procurar

                    fis = new FileInputStream("anime.db");
                    dis = new DataInputStream(fis);
                    int recordSize;
                    boolean numLapide;
                    boolean found = false;

                    while (dis.available() > 0) {
                        numLapide=dis.readBoolean();
                        if(numLapide==false){
                            break;
                        } // conferir a lápide
                        recordSize = dis.readInt(); // ler indicador de tamanho
                        byte[] recordData = new byte[recordSize]; 
                        dis.readFully(recordData); // ler no arquivo vetor de bytes respectivo

                        Anime anime = new Anime();
                        anime.fromByteArray(recordData); // transformar em objeto

                        if (anime.getId() == idBuscado) { // conferir se id bate com o procurado
                            System.out.println(anime.toString());
                            found = true;
                            break;
                        }
                    }

                    if (!found) { 
                        System.out.println("Anime com ID " + idBuscado + " não encontrado.");
                    }

                    // fechar os fluxos
                    fis.close();
                    dis.close();
                    break;
                default:
                    System.out.println("Opção inválida.");
            }

        } catch (FileNotFoundException e) {
            System.err.println("File not found: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.err.println("Number format error: " + e.getMessage());
        }
    }
}
