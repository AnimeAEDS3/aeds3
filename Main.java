import java.io.*;
import java.util.*;

class Main {

    public static void main(String data[]) throws FileNotFoundException {
        FileOutputStream fos;
        DataOutputStream dos;
        FileInputStream fis;
        DataInputStream dis;
        Scanner scanner = new Scanner(System.in);
        int option;
        int lastId = 0;

        try {

            // Guia visual do crud
            System.out.println("Escolha uma tarefa:");
            System.out.println("1. Carregar base de dados original");
            System.out.println("2. Busca por ID (READ)");
            System.out.println("3. Criar novo registro (CREATE)");
            System.out.print("Tarefa: ");
            option = scanner.nextInt();

            switch (option) {
                // CARREGAR BASE ORIGINAL
                case 1:
                    // Abrindo arquivos de dados original
                    File file = new File("dataanime.tsv");
                    RandomAccessFile raf = new RandomAccessFile(file, "rw");

                    System.out.println("Carregando dados de " + file.getPath() + "...");

                    // Preparando caminho de input
                    fos = new FileOutputStream("anime.db");
                    dos = new DataOutputStream(fos);
                    RandomAccessFile rafAnimeDb = new RandomAccessFile("anime.db", "rw");

                    // Pulando header
                    String header = raf.readLine();

                    dos.writeInt(0); // reservando os 4 primeiros bytes para o último id

                    // Recebendo inputs do tsv
                    String line;
                    while ((line = raf.readLine()) != null) {
                        // Função de extração dos dados do tsv
                        Anime anime = Anime.fromStringArray(line.split("\t"));

                        // Escrever lastId atualizado no início do arquivo
                        rafAnimeDb.seek(0);
                        rafAnimeDb.writeInt(anime.getId());
                        
                        byte[] ba = anime.toByteArray(); // Objeto convertido em array de bytes
                        dos.writeBoolean(false); // Escrevendo a lápide antes do indicador de tamanho
                        dos.writeInt(ba.length); // Indicador de tamanho
                        dos.write(ba);
                    }

                    raf.close();
                    // Fechar o fluxo
                    dos.close();
                    fos.close();
                    rafAnimeDb.close();
                    break;

                    case 2:
                    // Código para ler um ID e imprimir as informações do objeto
                    System.out.print("Digite o ID do anime que deseja buscar: ");
                    int idBuscado = scanner.nextInt();

                    fis = new FileInputStream("anime.db");
                    dis = new DataInputStream(fis);
                    int recordSize;
                    boolean lapide;
                    boolean found = false;
                    dis.readInt();

                    while (dis.available() > 0) {
                        lapide = dis.readBoolean();
                        if (lapide == true) {
                            recordSize = dis.readInt();
                            dis.skipBytes(recordSize);
                            continue;
                        }
                        // Ler indicador de tamanho
                        recordSize = dis.readInt();
                        byte[] recordData = new byte[recordSize];
                        // Ler no arquivo vetor de bytes respectivo
                        dis.readFully(recordData);

                        // Transformar em objeto
                        Anime anime = new Anime();
                        anime.fromByteArray(recordData);

                        if (anime.getId() == idBuscado) {
                            System.out.println(anime.toString());
                            found = true;
                            break;
                        }
                    }

                    if (!found) {
                        System.out.println("Anime com ID " + idBuscado + " não encontrado.");
                    }

                    // Fechar os fluxos
                    fis.close();
                    dis.close();
                    break;

                // ADICIONAR NOVO REGISTRO
                case 3:
                    try (RandomAccessFile ra = new RandomAccessFile("anime.db", "rw")) {
                        // Acessando o último id inserido
                        int newId = ra.readInt() + 1;
                        ra.seek(0); // Voltar ao início do arquivo para atualizar o ID

                        Anime a = Anime.promptUser(newId);
                        a.setId(newId);

                        // Definir a posição de escrita no final do arquivo
                        ra.seek(ra.length());

                        // Gravar o novo registro
                        ra.writeBoolean(false); // Lapide
                        byte[] recordData = a.toByteArray();
                        ra.writeInt(recordData.length); // Tamanho do registro
                        ra.write(recordData); // Dados do registro

                        // Atualizar o último ID inserido
                        ra.seek(0);
                        ra.writeInt(newId);

                        System.out.println("Novo registro criado!");
                    } catch (IOException e) {
                        System.out.println("Erro ao criar novo registro: " + e.getMessage());
                        e.printStackTrace();
                    }
                    break;

                default:
                    System.out.println("Opção inválida.");

                    scanner.close();
            }

        } catch (FileNotFoundException e) {
            System.err.println("Arquivo não encontrado: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Erro ao ler arquivo: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.err.println("Erro de formatação numérico: " + e.getMessage());
        }
    }
}