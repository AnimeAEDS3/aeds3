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

            // Função da interface base do programa
            Anime.animeInterface();
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

                    // Pulando header do arquivo original
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
                    dos.close();
                    fos.close();
                    rafAnimeDb.close();
                    break;

                // ADICIONAR NOVO REGISTRO
                case 2:
                    try (RandomAccessFile ra = new RandomAccessFile("anime.db", "rw")) {
                        // Acessando o último id inserido
                        int newId = ra.readInt() + 1;

                        Anime a = Anime.promptUser(newId);
                        a.setId(newId);

                        // Definir a posição de escrita no final do arquivo
                        ra.seek(ra.length());

                        // Gravar o novo registro
                        ra.writeBoolean(false); // lapideRead
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

                    int tamRegistroRead;
                    boolean lapideRead;
                    boolean encontradoRead = false;
                    dis.readInt();

                    while (dis.available() > 0) { // Enquanto existirem bytes para serem lidos
                        lapideRead = dis.readBoolean();
                        if (lapideRead == true) {
                            tamRegistroRead = dis.readInt();
                            dis.skipBytes(tamRegistroRead);
                            continue;
                        }
                        // Ler indicador de tamanho
                        tamRegistroRead = dis.readInt();
                        byte[] recordData = new byte[tamRegistroRead];
                        // Ler no arquivo vetor de bytes respectivo
                        dis.readFully(recordData);

                        // Transformar em objeto
                        Anime anime = new Anime();
                        anime.fromByteArray(recordData);

                        if (anime.getId() == idBuscado) {
                            System.out.println(anime.toString());
                            encontradoRead = true;
                            break;
                        }
                    }

                    if (!encontradoRead) {
                        System.out.println("Anime com ID " + idBuscado + " não encontrado.");
                    }
                    fis.close();
                    dis.close();
                    break;

                // DELETANDO REGISTRO
                case 4:
                    System.out.print("Digite o ID do anime que deseja remover: ");
                    RandomAccessFile raDel = new RandomAccessFile("anime.db", "rw");
                    int idRemover = scanner.nextInt();

                    int tamRegistroDel;
                    boolean lapideDel;
                    boolean encontradoDel = false;
                    raDel.readInt(); // ler ultimo id

                    while (raDel.getFilePointer() < raDel.length()) {
                        long posicaoLapide = raDel.getFilePointer(); // guarda a posição da lapideRead
                        lapideDel = raDel.readBoolean();

                        if (lapideDel == true) {
                            tamRegistroDel = raDel.readInt();
                            raDel.skipBytes(tamRegistroDel);
                            continue;
                        }
                        // Ler indicador de tamanho
                        tamRegistroRead = raDel.readInt();
                        byte[] recordData = new byte[tamRegistroRead];
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
                            encontradoDel = true;
                            break;
                        }
                    }

                    if (!encontradoDel) {
                        System.out.println("Anime com ID " + idRemover + " não encontrado.");
                    }
                    raDel.close();
                    break;

                // ATUALIZAR REGISTRO POR ID
                case 5:
                    System.out.print("Digite o ID do anime que deseja atualizar: ");
                    int idAtualizar = scanner.nextInt();
                    RandomAccessFile raUpd = new RandomAccessFile("anime.db", "rw");
                    boolean encontrado = false;

                    int tamRegistroAtt;
                    boolean lapideAtt;

                    raUpd.readInt(); // Lê o último ID pra mover o ponteiro pra parte significante

                    while (raUpd.getFilePointer() < raUpd.length()) { // Enquanto o ponteiro não atingir o valor máximo no contexto
                        long posicaoLapide = raUpd.getFilePointer(); // Guarda a posição da lapide
                        lapideAtt = raUpd.readBoolean(); // Guarda o valor da lapide

                        if (lapideAtt == true) {
                            tamRegistroAtt = raUpd.readInt();
                            raUpd.skipBytes(tamRegistroAtt);
                            continue;
                        }
                        // Ler indicador de tamanho
                        tamRegistroAtt = raUpd.readInt();
                        byte[] recordData = new byte[tamRegistroAtt];
                        // Ler no arquivo vetor de bytes respectivo
                        raUpd.readFully(recordData);

                        // Transformar em objeto
                        Anime anime = new Anime();
                        anime.fromByteArray(recordData);

                        if (anime.getId() == idAtualizar) {
                            Anime novoAnime = Anime.promptUser(idAtualizar); // Solicita ao usuário os novos dados
                            novoAnime.setId(idAtualizar); // Define o mesmo ID
                        
                            byte[] novoRecordData = novoAnime.toByteArray();
                            int novoRecordSize = novoRecordData.length;
                        
                            // Se o novo registro for maior que o registro antigo
                            if (novoRecordSize > tamRegistroAtt) {
                                // Marcar o original como removido
                                raUpd.seek(posicaoLapide);
                                raUpd.writeBoolean(true);
                        
                                // Move pointeiro pro fim do arquivo
                                raUpd.seek(raUpd.length());
                        
                                // Escrever o novo registro
                                raUpd.writeBoolean(false); // Lapide
                                raUpd.writeInt(novoRecordSize); // Tamanho do registro
                                raUpd.write(novoRecordData); // Novo anime
                        
                                System.out.println("Registro atualizado com sucesso, movido para o fim do arquivo.");
                                encontrado = true;
                            } else { // Se o novo registro for <= registro antigo
                                // Move o ponteiro para o inicio do registro antigo
                                raUpd.seek(posicaoLapide);
                                // Escreve os padrões de existência do registro
                                raUpd.writeBoolean(false);
                                raUpd.writeInt(tamRegistroAtt);
                                // Escreve o novo registro
                                raUpd.write(novoRecordData);
                        
                                System.out.println("Registro atualizado com sucesso, mantido no mesmo lugar.");
                                encontrado = true;
                            }
                            break;
                        }
                        
                    }

                    if (!encontrado) {
                        System.out.println("Anime com ID " + idAtualizar + " não encontrado.");
                    }

                    raUpd.close();
                    break;

                default:
                    System.out.println("Saíndo...");

                    scanner.close();
            }

        // Exceptions

        } catch (FileNotFoundException e) {
            System.err.println("Arquivo não encontrado: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Erro ao ler arquivo: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.err.println("Erro de formatação numérico: " + e.getMessage());
        } catch (InputMismatchException e) {
            System.err.println("Esperado um inteiro.");
        }
    }
}