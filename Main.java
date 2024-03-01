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

        try {

            // Guia visual do crud
            System.out.println("Escolha uma tarefa:");
            System.out.println("1. Carregar base de dados original");
            System.out.println("2. Criar novo registro (CREATE)");
            System.out.println("3. Busca por ID (READ)");
            System.out.println("4. Deletar um registro por ID (DELETE)");
            System.out.println("5. Update em um registro por ID (UPDATE)");
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

                // ADICIONAR NOVO REGISTRO
                case 2:
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

                case 3:
                    // Código para ler um ID e imprimir as informações do objeto
                    System.out.print("Digite o ID do anime que deseja buscar: ");
                    fis = new FileInputStream("anime.db");
                    dis = new DataInputStream(fis);
                    int idBuscado = scanner.nextInt();

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

                // DELETANDO REGISTRO
                case 4:
                    System.out.print("Digite o ID do anime que deseja remover: ");
                    RandomAccessFile raDel = new RandomAccessFile("anime.db", "rw");
                    int idRemover = scanner.nextInt();

                    int recordSize2;
                    boolean lapide2;
                    boolean found2 = false;
                    raDel.readInt(); // ler ultimo id

                    while (raDel.getFilePointer() < raDel.length()) {
                        long posicaoLapide = raDel.getFilePointer(); // guarda a posição da lapide
                        lapide2 = raDel.readBoolean();

                        if (lapide2 == true) {
                            recordSize2 = raDel.readInt();
                            raDel.skipBytes(recordSize2);
                            continue;
                        }
                        // Ler indicador de tamanho
                        recordSize = raDel.readInt();
                        byte[] recordData = new byte[recordSize];
                        // Ler no arquivo vetor de bytes respectivo
                        raDel.readFully(recordData);

                        // Transformar em objeto
                        Anime anime = new Anime();
                        anime.fromByteArray(recordData);

                        if (anime.getId() == idRemover) {
                            raDel.seek(posicaoLapide);
                            raDel.writeBoolean(true);
                            System.out.println(
                                    "O anime " + anime.getTitle() + ", com ID " + idRemover + ", foi removido");
                            found2 = true;
                            break;
                        }
                    }

                    if (!found2) {
                        System.out.println("Anime com ID " + idRemover + " não encontrado.");
                    }
                    raDel.close();
                    break;

                // ATUALIZAR REGISTRO POR ID
                // Essa versão coloca lápide no velho e cria um novo registro no final com o mesmo id do velho
                case 5:
                    System.out.print("Digite o ID do anime que deseja atualizar: ");
                    int idAtualizar = scanner.nextInt();
                    RandomAccessFile raUpd = new RandomAccessFile("anime.db", "rw");
                    boolean encontrado = false;

                    int recordSize3;
                    boolean lapide3;

                    raUpd.readInt(); // lê o último ID

                    while (raUpd.getFilePointer() < raUpd.length()) {
                        long posicaoLapide = raUpd.getFilePointer(); // guarda a posição da lapide
                        lapide3 = raUpd.readBoolean();

                        if (lapide3 == true) {
                            recordSize3 = raUpd.readInt();
                            raUpd.skipBytes(recordSize3);
                            continue;
                        }
                        // Ler indicador de tamanho
                        recordSize3 = raUpd.readInt();
                        byte[] recordData = new byte[recordSize3];
                        // Ler no arquivo vetor de bytes respectivo
                        raUpd.readFully(recordData);

                        // Transformar em objeto
                        Anime anime = new Anime();
                        anime.fromByteArray(recordData);

                        if (anime.getId() == idAtualizar) {
                            Anime novoAnime = Anime.promptUser(idAtualizar); // Solicita ao usuário os novos dados
                            novoAnime.setId(idAtualizar); // Define o mesmo ID

                            raUpd.seek(posicaoLapide);
                            raUpd.writeBoolean(true); // Marca o registro original como removido

                            raUpd.seek(raUpd.length()); // Vai para o final do arquivo para adicionar o novo registro
                            raUpd.writeBoolean(false); // Lapide
                            byte[] novoRecordData = novoAnime.toByteArray();
                            raUpd.writeInt(novoRecordData.length); // Tamanho do registro
                            raUpd.write(novoRecordData); // Dados do registro

                            System.out.println("Registro atualizado com sucesso.");
                            encontrado = true;
                            break;
                        }
                    }

                    if (!encontrado) {
                        System.out.println("Anime com ID " + idAtualizar + " não encontrado.");
                    }

                    raUpd.close();
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