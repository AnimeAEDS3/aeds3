import java.io.*;
import java.util.*;

class Main {

    public static void main(String data[]) throws FileNotFoundException, IOException {

        // Declarando elementos importantes no escopo superior, pra reutilizar
        int option;
        boolean loop = true;
        Scanner scanner = new Scanner(System.in);
        Directory d = new Directory("index.db");
        d.clear();
        int last_id = 0;
        long id_add;

        try (RandomAccessFile raf = new RandomAccessFile("anime.db", "rwd")) {

            while (loop) {

                // Função da interface base do programa
                Anime.animeInterface();
                option = scanner.nextInt();

                switch (option) {
                    // CARREGAR BASE ORIGINAL
                    case 1:
                        try (RandomAccessFile TSVRAF = new RandomAccessFile("dataanime.tsv", "rwd")) {

                            System.out.println("Carregando dados...");

                            // Pulando header do arquivo original
                            String header = TSVRAF.readLine();

                            raf.seek(0);
                            raf.writeInt(0); // reservando os 4 primeiros bytes para o último id

                            d.clear();
                            
                            // Recebendo inputs do tsv
                            String line;
                            while ((line = TSVRAF.readLine()) != null) {
                                // Função de extração dos dados do tsv
                                Anime anime = Anime.fromStringArray(line.split("\t"));

                                byte[] ba = anime.toByteArray(); // Objeto convertido em array de bytes
                                d.clear();
                                d.AddItem(anime.getId(), raf.getFilePointer()); // Adiciona a tupla no bucket do hash
                                raf.writeBoolean(false); // Escrevendo a lápide antes do indicador de tamanho
                                raf.writeInt(ba.length); // Indicador de tamanho
                                raf.write(ba);


                                last_id = anime.getId();

                            }
                            raf.seek(0);
                            raf.writeInt(last_id);
                            TSVRAF.close();

                        } catch (IOException e) {
                            System.err.println("Erro ao ler arquivo: " + e.getMessage());
                            e.printStackTrace(); // Adicionado para obter mais informações sobre o erro
                        }
                        break;

                    // ADICIONAR NOVO REGISTRO
                    case 2:
                        // Acessando o último id inserido
                        raf.seek(0);
                        int newId = raf.readInt() + 1;

                        Anime a = Anime.promptUser(newId);
                        a.setId(newId);

                        // Definir a posição de escrita no final do arquivo
                        raf.seek(raf.length());
                        d.AddItem(a.getId(), raf.getFilePointer()); // Inserindo no hash

                        // Gravar o novo registro
                        raf.writeBoolean(false); // lapideRead
                        byte[] recordData = a.toByteArray();
                        raf.writeInt(recordData.length); // Tamanho do registro
                        raf.write(recordData); // Dados do registro

                        // Atualizar o último ID inserido
                        raf.seek(0);
                        raf.writeInt(newId);

                        System.out.println("Novo registro criado!");
                        timer();
                        break;

                    case 3:
                        // Código para ler um ID e imprimir as informações do objeto
                        System.out.print("Digite o ID do anime que deseja buscar: ");

                        int idBuscado = scanner.nextInt();

                        int tamRegistroRead;
                        boolean lapideRead;
                        boolean encontradoRead = false;

                        raf.seek(0);
                        raf.readInt();

                        while (raf.getFilePointer() <= raf.length()) { // Enquanto existirem bytes para serem lidos
                            lapideRead = raf.readBoolean();
                            if (lapideRead == true) {
                                tamRegistroRead = raf.readInt();
                                raf.skipBytes(tamRegistroRead);
                                continue;
                            }
                            // Ler indicador de tamanho
                            tamRegistroRead = raf.readInt();
                            recordData = new byte[tamRegistroRead];
                            // Ler no arquivo vetor de bytes respectivo
                            raf.readFully(recordData);

                            // Transformar em objeto
                            Anime anime = new Anime();
                            anime.fromByteArray(recordData);

                            if (anime.getId() == idBuscado) {
                                System.out.println(anime.toString());
                                encontradoRead = true;
                                timer();
                                break;
                            }
                        }

                        if (!encontradoRead) {
                            System.out.println("Anime com ID " + idBuscado + " não encontrado.");
                        }
                        break;

                    // DELETANDO REGISTRO
                    case 4:
                        System.out.print("Digite o ID do anime que deseja remover: ");
                        int idRemover = scanner.nextInt();

                        int tamRegistroDel;
                        boolean lapideDel;
                        boolean encontradoDel = false;

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
                            tamRegistroRead = raf.readInt();
                            recordData = new byte[tamRegistroRead];
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
                        break;

                    // ATUALIZAR REGISTRO POR ID
                    case 5:
                        System.out.print("Digite o ID do anime que deseja atualizar: ");
                        int idAtualizar = scanner.nextInt();

                        raf.seek(0);
                        boolean encontrado = false;

                        int tamRegistroAtt;
                        boolean lapideAtt;

                        raf.readInt(); // Lê o último ID pra mover o ponteiro pra parte significante

                        while (raf.getFilePointer() < raf.length()) { // Enquanto o ponteiro não atingir o valor máximo
                                                                      // no contexto
                            long posicaoLapide = raf.getFilePointer(); // Guarda a posição da lapide
                            lapideAtt = raf.readBoolean(); // Guarda o valor da lapide

                            if (lapideAtt == true) {
                                tamRegistroAtt = raf.readInt();
                                raf.skipBytes(tamRegistroAtt);
                                continue;
                            }
                            // Ler indicador de tamanho
                            tamRegistroAtt = raf.readInt();
                            recordData = new byte[tamRegistroAtt];
                            // Ler no arquivo vetor de bytes respectivo
                            raf.readFully(recordData);

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
                                    raf.seek(posicaoLapide);
                                    raf.writeBoolean(true);

                                    // Move pointeiro pro fim do arquivo
                                    raf.seek(raf.length());

                                    // Escrever o novo registro
                                    raf.writeBoolean(false); // Lapide
                                    raf.writeInt(novoRecordSize); // Tamanho do registro
                                    raf.write(novoRecordData); // Novo anime

                                    System.out
                                            .println("Registro atualizado com sucesso, movido para o fim do arquivo.");
                                    timer();
                                    encontrado = true;
                                } else { // Se o novo registro for <= registro antigo
                                    // Move o ponteiro para o inicio do registro antigo
                                    raf.seek(posicaoLapide);
                                    // Escreve os padrões de existência do registro
                                    raf.writeBoolean(false);
                                    raf.writeInt(tamRegistroAtt);
                                    // Escreve o novo registro
                                    raf.write(novoRecordData);

                                    System.out.println("Registro atualizado com sucesso, mantido no mesmo lugar.");
                                    timer();
                                    encontrado = true;
                                }
                                break;
                            }

                        }

                        if (!encontrado) {
                            System.out.println("Anime com ID " + idAtualizar + " não encontrado.");
                            timer();
                        }
                        break;

                    case 6:
                        System.out.print("Digite o ID do anime que deseja buscar usando hash: ");
                        idBuscado = scanner.nextInt();
                        
                        id_add = d.search(idBuscado);

                        // Move the file pointer to the position corresponding to the address found in
                        // the hash
                        raf.seek(id_add);

                        Anime animeHash = new Anime();
                        // Read the data for the anime at the specified position
                        boolean lapide = raf.readBoolean(); // Read the marker for tombstone
                        if (lapide == true) {
                            System.out.println("Registro foi deletado");
                        } else {
                            int tamRegistro = raf.readInt(); // Read the size of the record
                            recordData = new byte[tamRegistro]; // Create a byte array to hold the record data
                            raf.readFully(recordData); // Read the record data into the byte array

                            // Populate the Anime object with the data read from the file
                            animeHash.fromByteArray(recordData);
                            System.out.println(animeHash.toString());
                        }

                        break;

                    case 7:
                        d.readFile();
                        System.out.println("Saindo...");
                        scanner.close();
                        loop = false;
                        break;

                    default:
                        loop = false;
                        scanner.close();

                }
            }
            d.saveDirectory();
            raf.close();

            // Catching errors
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

    public static void timer() {
        // Timer pra dar tempo de ler uma informação
        try {
            Thread.sleep(2000); // 2000 milliseconds = 2 segundos
        } catch (InterruptedException e) {
            System.err.println("Timer interrupted: " + e.getMessage());
        }
    }
}
