import datastructures.*;
import pattern_searching.BoyerMoore;
import register.*;
import compression.*;
import cryptography.*;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.IntStream;

public class Main {

    private static final String DB_FILE_NAME = "anime.db";
    private static final String INDEX_FILE_NAME = "index.db";
    private static final String TSV_FILE_NAME = "dataanime.tsv";
    private static final int SLEEP_TIME_MS = 2000; // 2000 milliseconds = 2 seconds

    private RandomAccessFile raf;
    private InvertedListName il;
    private InvertedListGenre il2;
    private Directory directory;
    private ArvoreBPlus arvoreBPlus;
    private Scanner scanner;

    public Main() throws IOException {
        setup();
    }

    private void setup() throws IOException {
        raf = new RandomAccessFile(DB_FILE_NAME, "rwd");
        scanner = new Scanner(System.in);
        il = new InvertedListName();
        il2 = new InvertedListGenre();
        directory = new Directory(INDEX_FILE_NAME);
        arvoreBPlus = new ArvoreBPlus(8, "arvore.db");
    }

    public static void main(String[] args) {
        try {
            Main mainApp = new Main();
            mainApp.run();
        } catch (Exception e) {
            System.err.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void run() throws IOException {
        boolean loop = true;
        int option;
        while (loop) {
            Scanner scan = new Scanner(System.in);
            Anime.animeInterface();
            option = scan.nextInt();
            switch (option) {
                case 1:
                    loadOriginalBase();
                    System.out.println();
                    break;
                case 2:
                    addNewRecord();
                    break;
                case 3:
                    findRecordByID();
                    break;
                case 4:
                    deleteRecord();
                    break;
                case 5:
                    updateRecord();
                    break;
                case 6:
                    searchUsingHash();
                    break;
                case 7:
                    searchUsingBTree();
                    break;
                case 8:
                    searchByTitle();
                    break;
                case 9:
                    searchByGenres();
                    break;
                case 10:
                    directory.readFile();
                    break;
                case 11:
                    arvoreBPlus.imprimir();
                    break;
                case 12:
                    comprimirHuffman();
                    break;
                case 13:
                    descomprimirHuffman();
                    break;
                case 14:
                    LZW.compressReal();
                    timer();
                    break;
                case 15:
                    LZW.decompressReal();
                    timer();
                    break;
                case 16:
                    boyerMooreSearch();
                    timer();
                    break;
                case 17:
                    RSA.encryptFile("anime.db","RSAencrypted_anime.db");
                    timer();
                    break;
                case 18:
                    RSA.decryptFile("RSAencrypted_anime.db","RSAdecrypted_anime.db");
                    timer();
                    break;
                case 19:
                    loop = false;
                    System.out.println();
                    System.out.println("Sayounara...");
                    break;
                default:
                    loop = false;
                    System.out.println();
                    System.out.println("Opçao invalida...");
                    break;
            }
        }
        scanner.close();
        raf.close();
    }

    private void loadOriginalBase() throws IOException {
        limpar(); // Limpa arquivos antigos
        setup(); // Arquivos resetados
        try (RandomAccessFile TSVRAF = new RandomAccessFile(TSV_FILE_NAME, "rwd")) {
            TSVRAF.readLine(); // Pulando header do arquivo original
            raf.seek(0);
            raf.writeInt(0); // reservando os 4 primeiros bytes para o último id
            String line;
            int lastId = 0;
            long totalLines = Files.lines(Paths.get(TSV_FILE_NAME)).count() - 1; // Total de linhas excluindo header
            long currentLine = 0;
            System.out.println();
            while ((line = TSVRAF.readLine()) != null) {
                Anime anime = Anime.fromStringArray(line.split("\t"));
                addAnimeRecord(anime);
                lastId = anime.getId();
                currentLine++;
                progressBar(currentLine, totalLines);
            }
            raf.seek(0);
            raf.writeInt(lastId);
            saveAndClose();
        }
    }

    private static void boyerMooreSearch() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Digite o padrao a ser pesquisado: ");
        String patternStr = scanner.nextLine();

        byte[] pattern = patternStr.getBytes(); // Converte a string do padrão para bytes

        File file = new File(DB_FILE_NAME);
        byte[] fileBytes = null;

        try (FileInputStream fis = new FileInputStream(file)) {
            fileBytes = new byte[(int) file.length()];
            int bytesRead = fis.read(fileBytes);
            if (bytesRead == -1) {
                System.err.println("Erro ao ler o arquivo.");
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        // Executa a pesquisa
        BoyerMoore.search(fileBytes, pattern);
    }

    private void saveAndClose() throws IOException {
        directory.saveDirectory();
        il.saveToFile();
        il2.saveToFile();
    }

    private static void timer() {
        try {
            Thread.sleep(SLEEP_TIME_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Timer interrompido: " + e.getMessage());
        }
    }

    private static void comprimirHuffman() {
        String inputFilePath = DB_FILE_NAME;
        String treeFilePath = "huffmanTree.bin";
        Instant start = Instant.now();

        String text = Huffman.readFile(inputFilePath);
        Node root = Huffman.createHuffmanTree(text);
        Huffman.saveTreeToFile(root, treeFilePath);

        Instant end = Instant.now();
        long originalSize = new File(inputFilePath).length();
        long compressedSize = new File("encodedHuffman.bin").length();

        double compressionPercentage = ((double) (originalSize - compressedSize) / originalSize) * 100;
        Duration timeElapsed = Duration.between(start, end);

        System.out.println("Compressao completada em: " + timeElapsed.toMillis() + " ms");
        System.out.println("Numero de bytes comprimidos: " + (originalSize - compressedSize) + " bytes");
        System.out.println("Porcentagem de compressao: " + compressionPercentage + "%");
        timer();
    }

    private static void descomprimirHuffman() {
        String encodedFilePath = "encodedHuffman.bin";
        String decodedFilePath = "huffmanAnime.db";
        String treeFilePath = "huffmanTree.bin";

        Instant start = Instant.now();

        StringBuilder encodedBits = Huffman.readBitsFromFile(encodedFilePath);
        Node root = Huffman.loadTreeFromFile(treeFilePath);
        String decodedText = Huffman.decodeData(root, encodedBits);
        Huffman.saveTextToFile(decodedText, decodedFilePath);

        Instant end = Instant.now();

        Duration timeElapsed = Duration.between(start, end);

        System.out.println("Decompressao completada em: " + timeElapsed.toMillis() + " ms");
        timer();
    }

    private void progressBar(long current, long total) {
        int width = 50; // Tamanho da barra de progresso
        double progress = (double) current / total;
        int completed = (int) (progress * width);
        StringBuilder sb = new StringBuilder();
        sb.append('\r').append('[');
        IntStream.range(0, completed).forEach(i -> sb.append('='));
        IntStream.range(completed, width).forEach(i -> sb.append(' '));
        sb.append(']').append(String.format(" %d%%", (int) (progress * 100)));
        System.out.print(sb.toString());
    }

    private void addAnimeRecord(Anime anime) throws IOException {
        byte[] ba = anime.toByteArray();
        long filePointer = raf.getFilePointer();
        raf.writeBoolean(false); // Escrevendo a lápide antes do indicador de tamanho
        raf.writeInt(ba.length); // Indicador de tamanho
        raf.write(ba); // Dados do anime

        directory.AddItem(anime.getId(), filePointer);
        il.addKey(anime.getTitle(), new Key(filePointer, anime.getId()));
        il2.addKey(anime.getGenres(), new Key(filePointer, anime.getId()));
        arvoreBPlus.create(anime.getId(), filePointer);
    }

    private void addNewRecord() throws IOException {
        raf.seek(0);
        int newId = raf.readInt() + 1;
        Anime anime = Anime.promptUser(newId);
        anime.setId(newId);
        addAnimeRecord(anime);
        raf.seek(0);
        raf.writeInt(newId);
        System.out.println("Novo registro criado!");
        timer();
    }

    private void findRecordByID() throws IOException {
        // Solicita ao usuário que digite o ID do anime que deseja buscar
        System.out.print("Digite o ID do anime que deseja buscar: ");
        int searchedID = scanner.nextInt();

        int recordSize;
        boolean isDeleted;
        boolean found = false;

        raf.seek(0);
        raf.readInt(); // Le e descarta o inteiro inicial se nao for utilizado em outro lugar

        // Enquanto houver bytes para serem lidos
        while (raf.getFilePointer() < raf.length()) {
            isDeleted = raf.readBoolean(); // Le o marcador de exclusao
            if (isDeleted) {
                recordSize = raf.readInt(); // Le o tamanho do registro excluído
                raf.skipBytes(recordSize); // Pula o registro inteiro
                continue;
            }

            // Le o tamanho do próximo registro
            recordSize = raf.readInt();
            byte[] recordData = new byte[recordSize];
            // Le os dados reais do registro
            raf.readFully(recordData);

            // Desserializa os dados em um objeto Anime
            Anime anime = new Anime();
            anime.fromByteArray(recordData);

            // Verifica se o ID do anime corresponde ao ID procurado
            if (anime.getId() == searchedID) {
                System.out.println(
                        "Id: " + anime.getId() + " | Titulo: " + anime.getTitle() + " | Score: " + anime.getScore());
                found = true;
                break; // Sai do loop se o anime for encontrado
            }
        }

        // Se o anime nao for encontrado após a leitura de todos os registros
        if (!found) {
            System.out.println("Anime com ID " + searchedID + " nao encontrado.");
        }
        timer(); // Chama um temporizador para uma pequena pausa
    }

    private void deleteRecord() throws IOException {
        System.out.print("Digite o ID do anime que deseja remover: ");
        int idRemover = scanner.nextInt();

        int tamRegistroDel;
        boolean lapideDel;
        boolean encontradoDel = false;

        directory.deleteItem(idRemover);
        arvoreBPlus.delete(idRemover);

        raf.seek(0);
        raf.readInt(); // ler ultimo id

        while (raf.getFilePointer() < raf.length()) {
            long posicaoLapide = raf.getFilePointer(); // guarda a posição da lapideRead
            lapideDel = raf.readBoolean();

            if (lapideDel == true) {
                tamRegistroDel = raf.readInt();
                raf.skipBytes(tamRegistroDel);
                continue;
            }
            // Ler indicador de tamanho
            int tamRegistroRead = raf.readInt();
            byte[] recordData = new byte[tamRegistroRead];
            // Ler no arquivo vetor de bytes respectivo
            raf.readFully(recordData);

            // Transformar em objeto
            Anime anime = new Anime();
            anime.fromByteArray(recordData);

            if (anime.getId() == idRemover) {
                raf.seek(posicaoLapide);
                raf.writeBoolean(true);
                System.out.println(
                        "O anime " + anime.getTitle() + ", com ID " + idRemover + ", foi removido");
                timer();
                encontradoDel = true;
                break;
            }
        }

        if (!encontradoDel) {
            System.out.println("Anime com ID " + idRemover + " não encontrado.");
        }
    }

    private void updateRecord() throws IOException {
        // Solicita ao usuário que digite o ID do anime que deseja atualizar
        System.out.print("Digite o ID do anime que deseja atualizar: ");
        int idAtualizar = scanner.nextInt();

        // Inicializa variáveis para controle do processo de atualizaçao
        boolean encontrado = false;
        int tamRegistroAtt;
        boolean lapideAtt;
        byte[] recordData;

        // Move o ponteiro para o início do arquivo e le o último ID
        raf.seek(0);
        raf.readInt();

        // Percorre os registros no arquivo
        while (raf.getFilePointer() < raf.length()) {
            // Guarda a posiçao da lapide
            long posicaoLapide = raf.getFilePointer();
            // Le o valor da lapide
            lapideAtt = raf.readBoolean();

            // Se o registro estiver marcado como removido, avança para o próximo registro
            if (lapideAtt) {
                tamRegistroAtt = raf.readInt();
                raf.skipBytes(tamRegistroAtt);
                continue;
            }

            // Le o indicador de tamanho do registro
            tamRegistroAtt = raf.readInt();
            // Le os dados do registro
            recordData = new byte[tamRegistroAtt];
            raf.readFully(recordData);

            // Transforma os dados do registro em um objeto Anime
            Anime anime = new Anime();
            anime.fromByteArray(recordData);

            // Verifica se o ID do anime corresponde ao ID informado pelo usuário
            if (anime.getId() == idAtualizar) {
                // Solicita ao usuário os novos dados do anime
                Anime novoAnime = Anime.promptUser(idAtualizar);
                // Define o mesmo ID para o novo anime
                novoAnime.setId(idAtualizar);

                // Converte o novo registro em um array de bytes
                byte[] novoRecordData = novoAnime.toByteArray();
                int novoRecordSize = novoRecordData.length;

                // Verifica se o novo registro é maior que o registro existente
                if (novoRecordSize > tamRegistroAtt) {
                    // Marca o registro original como removido
                    raf.seek(posicaoLapide);
                    raf.writeBoolean(true);

                    // Move o ponteiro para o fim do arquivo
                    raf.seek(raf.length());

                    // Escreve o novo registro no final do arquivo
                    raf.writeBoolean(false); // Lapide
                    raf.writeInt(novoRecordSize); // Tamanho do registro
                    raf.write(novoRecordData); // Novo anime

                    // Atualiza o endereço do registro nos índices
                    directory.updateItem(novoAnime.getId(), raf.length());
                    arvoreBPlus.update(novoAnime.getId(), raf.length());

                    System.out.println("Registro atualizado com sucesso, movido para o fim do arquivo.");
                    timer();
                } else { // Se o novo registro é menor ou igual ao registro existente
                    // Move o ponteiro para o início do registro existente
                    raf.seek(posicaoLapide);
                    // Escreve os dados do novo registro sobre o registro existente
                    raf.writeBoolean(false); // Marca como existente
                    raf.writeInt(tamRegistroAtt); // Tamanho do registro
                    raf.write(novoRecordData); // Novos dados do anime

                    System.out.println("Registro atualizado com sucesso, mantido no mesmo lugar.");
                    timer();
                }
                encontrado = true;
                break; // Sai do loop após encontrar o registro
            }
        }

        // Se o registro nao foi encontrado, exibe uma mensagem ao usuário
        if (!encontrado) {
            System.out.println("Anime com ID " + idAtualizar + " nao encontrado.");
            timer();
        }
    }

    private void searchUsingHash() throws IOException {
        System.out.print("Digite o ID do anime que deseja buscar usando hash: ");
        int idBuscado = scanner.nextInt();

        long address = directory.search(idBuscado);
        if (address != -1) {
            readAndDisplayAnime(address);
        } else {
            System.out.println("Anime com ID " + idBuscado + " nao encontrado.");
        }
        timer();
    }

    private void searchUsingBTree() throws IOException {
        System.out.print("Digite o ID do anime que deseja buscar usando arvore b+: ");
        int idBuscado = scanner.nextInt();

        Long address = arvoreBPlus.read(idBuscado);
        if (address != null) {
            readAndDisplayAnime(address);
        } else {
            System.out.println("Registro nao encontrado");
        }
        timer();
    }

    private void readAndDisplayAnime(long address) throws IOException {
        raf.seek(address);
        boolean lapide = raf.readBoolean();
        if (lapide) {
            System.out.println("Registro foi deletado");
        } else {
            int tamRegistro = raf.readInt();
            byte[] recordData = new byte[tamRegistro];
            raf.readFully(recordData);
            Anime anime = new Anime();
            anime.fromByteArray(recordData);
            System.out.println(
                    "Id: " + anime.getId() + " | Titulo: " + anime.getTitle() + " | Score: " + anime.getScore());
        }
    }

    private void limpar() {
        // Limpa todos os arquivos relacionados
        deleteFile(DB_FILE_NAME);
        deleteFile(INDEX_FILE_NAME);
        deleteFile("dir.db");
        deleteFile("invertedListName.db");
        deleteFile("invertedListGenre.db");
        deleteFile("arvore.db");
    }

    public static void deleteFile(String fileName) {
        File file = new File(fileName);
        if (file.exists()) {
            try (FileChannel channel = FileChannel.open(file.toPath(), StandardOpenOption.WRITE)) {
                FileLock lock = channel.tryLock();
                if (lock != null) {
                    file.delete();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void searchByTitle() throws IOException {
        // Solicita ao usuário o nome que deseja pesquisar
        Scanner scanner = new Scanner(System.in);
        System.out.println("Digite o nome que quer pesquisar:");
        System.out.print(">> ");
        String termo;
        if (scanner.hasNextLine()) {
            termo = scanner.nextLine();
        } else {
            termo = scanner.next();
        }
        String[] termoSignificativo = termo.split(" ");

        // Busca os animes pelo termo de pesquisa no título
        List<Anime> animeList = searchByTerm(termoSignificativo[0], il);
        // Exibe a lista de animes encontrados
        displayAnimeList(animeList);
    }

    private void searchByGenres() throws IOException {
        // Solicita ao usuário os generos que deseja pesquisar
        System.out.println("Digite os generos que deseja pesquisar:");
        System.out.print(">> ");
        String termos = scanner.nextLine();
        String[] generos = termos.split(" ");
        // Busca os animes pelos generos informados
        List<Anime> animeList = searchByTerm(generos, il2);
        // Exibe a lista de animes encontrados
        displayAnimeList(animeList);
    }

    private List<Anime> searchByTerm(String termo, InvertedListName il) throws IOException {
        List<Anime> animeList = new ArrayList<>();
        // Obtém as chaves de busca para o termo informado
        List<Key> chaves = il.getKeys(termo);
        for (Key chave : chaves) {
            try {
                long address = chave.getAddress(); // Obtém o endereço do registro
                raf.seek(address); // Move o ponteiro do arquivo para o endereço especificado
                boolean lapide = raf.readBoolean(); // Le o marcador de lápide
                if (!lapide) { // Se o registro não estiver marcado como deletado
                    int tamRegistro = raf.readInt(); // Le o tamanho do registro
                    byte[] recordData = new byte[tamRegistro]; // Cria um array de bytes para armazenar os dados do
                                                               // registro
                    raf.readFully(recordData); // Le os dados do registro para o array de bytes
                    // Preenche o objeto Anime com os dados lidos do arquivo
                    Anime anime = new Anime();
                    anime.fromByteArray(recordData);
                    animeList.add(anime);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return animeList;
    }

    private List<Anime> searchByTerm(String[] terms, InvertedListGenre il) throws IOException {
        List<Anime> animeList = new ArrayList<>();
        List<Key> chaves = il.getKeys(terms);
        for (Key chave : chaves) {
            try {
                long address = chave.getAddress(); // Obtém o endereço do registro
                raf.seek(address); // Move o ponteiro do arquivo para o endereço especificado
                boolean lapide = raf.readBoolean(); // Le o marcador de lápide
                if (!lapide) { // Se o registro não estiver marcado como deletado
                    int tamRegistro = raf.readInt(); // Le o tamanho do registro
                    byte[] recordData = new byte[tamRegistro]; // Cria um array de bytes para armazenar os dados do
                                                               // registro
                    raf.readFully(recordData); // Le os dados do registro para o array de bytes
                    // Preenche o objeto Anime com os dados lidos do arquivo
                    Anime anime = new Anime();
                    anime.fromByteArray(recordData);
                    animeList.add(anime);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return animeList;
    }

    private void displayAnimeList(List<Anime> animeList) {
        for (Anime anime : animeList) {
            System.out.println(
                    "Id: " + anime.getId() + " | Titulo: " + anime.getTitle() + " | Score: " + anime.getScore());
        }
        timer();
    }
}