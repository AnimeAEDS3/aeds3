import java.io.*;
import java.util.*;

class MainB {

    public static void main(String data[]) throws FileNotFoundException {
        Scanner scanner = new Scanner(System.in);
        int option;
        boolean loop = true;

        try {

            while (loop) {

                // Função da interface base do programa
                Anime.animeInterface();
                option = scanner.nextInt();
                ArvoreBPlus ABMais = new ArvoreBPlus(8, "arvore.db");

                switch (option) {
                    // CARREGAR BASE ORIGINAL
                    case 1:
                        // Abrindo arquivos de dados original
                        File file = new File("dataanime.tsv");
                        RandomAccessFile raf = new RandomAccessFile(file, "rw");

                        System.out.println("Carregando dados de " + file.getPath() + "...");

                        // Preparando caminho de input
                        RandomAccessFile rafAnimeDb = new RandomAccessFile("anime.db", "rw");
                        RandomAccessFile rafAnimeDb2 = new RandomAccessFile("anime.db", "rw");

                        // Pulando header do arquivo original
                        String header = raf.readLine();

                        rafAnimeDb2.writeInt(0); // reservando os 4 primeiros bytes para o último id
                        long offsetAntesDeEscrever;

                        // Recebendo inputs do tsv
                        String line;
                        while ((line = raf.readLine()) != null) {
                            // Função de extração dos dados do tsv
                            Anime anime = Anime.fromStringArray(line.split("\t"));

                            // Escrever lastId atualizado no início do arquivo
                            rafAnimeDb.seek(0);
                            rafAnimeDb.writeInt(anime.getId());

                            byte[] ba = anime.toByteArray(); // Objeto convertido em array de bytes
                            offsetAntesDeEscrever = rafAnimeDb2.getFilePointer(); //conteudo de cada chave da arvore(ponteiro para o id)
                            rafAnimeDb2.writeBoolean(false); // Escrevendo a lápide antes do indicador de tamanho

                            rafAnimeDb2.writeInt(ba.length); // Indicador de tamanho
                            rafAnimeDb2.write(ba);
                            ABMais.create(anime.id, offsetAntesDeEscrever);
                        }

                        raf.close();
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
                            long temp = ra.length();

                            // Gravar o novo registro
                            ra.writeBoolean(false); // lapideRead
                            byte[] recordData = a.toByteArray();
                            ra.writeInt(recordData.length); // Tamanho do registro
                            ra.write(recordData); // Dados do registro

                            // Atualizar o último ID inserido
                            ra.seek(0);
                            ra.writeInt(newId);
                            ABMais.create(newId, temp); //criando novo registro no arquivo de índices também

                            System.out.println("Novo registro criado!");
                            timer();
                        } catch (IOException e) {
                            System.out.println("Erro ao criar novo registro: " + e.getMessage());
                            e.printStackTrace();
                        }
                        break;

                    case 3:
                        // Código para ler um ID e imprimir as informações do objeto
                        System.out.print("Digite o ID do anime que deseja buscar: ");
                        RandomAccessFile raf3 = new RandomAccessFile("anime.db", "rw");
                        int idBuscado = scanner.nextInt();

                        Long endereco = ABMais.read(idBuscado); //buscando pela árvore
                        if (endereco == null) {
                            System.out.println("Registro não encontrado");
                            break;
                        }

                        int tamRegistroRead;
                        boolean lapideRead;
                        raf3.seek(endereco); //indo diretamente ao endereço
                        lapideRead = raf3.readBoolean();
                        if (lapideRead == true) {
                            System.out.println("Registro não encontrado");
                            break;
                        }
                        else{
                            // Ler indicador de tamanho
                            tamRegistroRead = raf3.readInt();
                            byte[] recordData = new byte[tamRegistroRead];
                            // Ler no arquivo vetor de bytes respectivo
                            raf3.readFully(recordData);

                            // Transformar em objeto
                            Anime anime = new Anime();
                            anime.fromByteArray(recordData);

                            System.out.println(anime.toString());
                            timer();
                            break;
                            }

                    // DELETANDO REGISTRO
                    case 4:
                        System.out.print("Digite o ID do anime que deseja remover: ");
                        RandomAccessFile raDel = new RandomAccessFile("anime.db", "rw");
                        int idRemover = scanner.nextInt();

                        boolean lapideDel;
                        endereco = ABMais.read(idRemover);
                        if (endereco == null) {
                            System.out.println("Registro não encontrado");
                            break;
                        }
                        else{
                        raDel.seek(endereco);
                            long posicaoLapide = raDel.getFilePointer(); // guarda a posição da lapideRead
                            lapideDel = raDel.readBoolean();

                            if (lapideDel == true) {
                                System.out.println("Registro não encontrado");
                            break;
                            }
                            else{
                            // Ler indicador de tamanho
                            tamRegistroRead = raDel.readInt();
                            byte[] recordData = new byte[tamRegistroRead];
                            // Ler no arquivo vetor de bytes respectivo
                            raDel.readFully(recordData);

                            // Transformar em objeto
                            Anime anime = new Anime();
                            anime.fromByteArray(recordData);

                            raDel.seek(posicaoLapide);
                            raDel.writeBoolean(true);
                            ABMais.delete(idRemover);
                            System.out.println(
                                        "O anime " + anime.getTitle() + ", com ID " + idRemover + ", foi removido");
                            timer();
                        raDel.close();
                        break;
                            }
                        }

                    // ATUALIZAR REGISTRO POR ID
                    case 5:
                        System.out.print("Digite o ID do anime que deseja atualizar: ");
                        int idAtualizar = scanner.nextInt();
                        RandomAccessFile raUpd = new RandomAccessFile("anime.db", "rw");

                        int tamRegistroAtt;
                        boolean lapideAtt;
                        endereco = ABMais.read(idAtualizar); //busca do id pela árvore
                        if (endereco == null) {
                            System.out.println("Registro não encontrado");
                            break;
                        } //verificação de existência do id

                        raUpd.seek(endereco);
                            long posicaoLapide = raUpd.getFilePointer(); // Guarda a posição da lapide
                            lapideAtt = raUpd.readBoolean(); // Guarda o valor da lapide

                            if (lapideAtt == true) {
                                System.out.println("Registro não encontrado");
                                break;
                            } //dupla verificação de existência
                            // Ler indicador de tamanho
                            tamRegistroAtt = raUpd.readInt();
                            byte[] recordData = new byte[tamRegistroAtt];
                            // Ler no arquivo vetor de bytes respectivo
                            raUpd.readFully(recordData);

                            // Transformar em objeto
                            Anime anime = new Anime();
                            anime.fromByteArray(recordData);

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

                                    System.out
                                            .println("Registro atualizado com sucesso, movido para o fim do arquivo.");
                                    timer();
                                } else { // Se o novo registro for <= registro antigo
                                    // Move o ponteiro para o inicio do registro antigo
                                    raUpd.seek(posicaoLapide);
                                    // Escreve os padrões de existência do registro
                                    raUpd.writeBoolean(false);
                                    raUpd.writeInt(tamRegistroAtt);
                                    // Escreve o novo registro
                                    raUpd.write(novoRecordData);

                                    System.out.println("Registro atualizado com sucesso, mantido no mesmo lugar.");
                                    timer();
                                }

                        raUpd.close();
                        break;

                    case 6:
                        System.out.println("Saíndo...");
                        scanner.close();
                        loop = false;
                        break;

                    default:
                        loop = false;
                        scanner.close();

                }
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

    public static void timer() {
        // Timer pra dar tempo de ler uma informação
        try {
            Thread.sleep(2000); // 2000 milliseconds = 2 segundos
        } catch (InterruptedException e) {
            System.err.println("Timer interrupted: " + e.getMessage());
        }
    }
}