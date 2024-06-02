package compression;

import java.io.*;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class LZW {

    // Método de compressao
    public static List<Integer> compress(String uncompressed, Map<String, Integer> dictionary) {
        int dictSize = dictionary.size(); // Tamanho do dicionário atual

        String w = "";
        List<Integer> result = new ArrayList<>();
        for (char c : uncompressed.toCharArray()) { // percorre cada caractere do conteudo do arquivo ainda
                                                    // descomprimido
            String wc = w + c;
            if (dictionary.containsKey(wc))
                w = wc; // se a sequencia (w + c) está no dicionário, atualiza w para wc
            else {
                result.add(dictionary.get(w)); // adiciona o código de w ao resultado
                dictionary.put(wc, dictSize++); // adiciona wc ao dicionário como um novo código
                w = "" + c; // reinicia w com o caractere atual
            }
        }

        if (!w.equals("")) {
            result.add(dictionary.get(w)); // adiciona o código da ultima sequencia w ao resultado
        }
        return result;
    }

    // Método de descompressao
    public static String decompress(List<Integer> compressed, Map<Integer, String> dictionary) {
        int dictSize = dictionary.size(); // Tamanho do dicionário atual

        String w = "" + (char) (int) compressed.remove(0);
        StringBuilder result = new StringBuilder(w);
        for (int k : compressed) {
            String entry;
            if (dictionary.containsKey(k))
                entry = dictionary.get(k); // se o código está no dicionário, pega a sequencia correspondente
            else if (k == dictSize)
                entry = w + w.charAt(0); // caso especial: o código é igual ao tamanho do dicionário
            else
                throw new IllegalArgumentException("Erro: " + k);

            result.append(entry); // adiciona a sequencia ao resultado
            dictionary.put(dictSize++, w + entry.charAt(0)); // adiciona a nova sequencia ao dicionário
            w = entry; // atualiza w para a sequencia atual
        }
        return result.toString();
    }

    // Método para serializar o dicionário
    public static void serializeDictionary(Map<String, Integer> dictionary, String fileName) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fileName))) {
            oos.writeObject(dictionary);
        }
    }

    // Método para desserializar o dicionário
    @SuppressWarnings("unchecked")
    public static Map<String, Integer> deserializeDictionary(String fileName)
            throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fileName))) {
            return (Map<String, Integer>) ois.readObject();
        }
    }

    // Método para serializar o dicionário de descompressao
    public static void serializeDecompressionDictionary(Map<Integer, String> dictionary, String fileName)
            throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fileName))) {
            oos.writeObject(dictionary);
        }
    }

    // Método para desserializar o dicionário de descompressao
    @SuppressWarnings("unchecked")
    public static Map<Integer, String> deserializeDecompressionDictionary(String fileName)
            throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fileName))) {
            return (Map<Integer, String>) ois.readObject();
        }
    }

    // Método para escrever um inteiro usando codificaçao de comprimento variável
    public static void writeVariableLengthInt(DataOutputStream dos, int value) throws IOException {
        while ((value & ~0x7F) != 0) { // esse loop continua enquanto o valor ainda tiver mais do que 7 bits
                                       // significativos
            dos.writeByte((value & 0x7F) | 0x80); // escreve o Byte com MSB (most significant byte) definido
            value >>>= 7;
        }
        dos.writeByte(value & 0x7F); // após sair do loop, o valor restante é escrito
    }

    // Método para ler um inteiro usando codificaçao de comprimento variável
    public static int readVariableLengthInt(DataInputStream dis) throws IOException {
        int value = 0;
        int shift = 0;
        byte b;
        do {
            b = dis.readByte(); // Le um byte
            value |= (b & 0x7F) << shift; // Adiciona os 7 bits ao valor
            shift += 7; // Atualiza o deslocamento
        } while ((b & 0x80) != 0); // Continua se o MSB estiver definido
        return value; // Retorna o valor completo
    }

    // Método para compressao real
    public static void compressReal() {
        String comp = "animeDBLZWCompressed.bin";
        String dictFile = "LZWDictionary.ser"; // .ser é uma extensao comum para arquivos serializados
        Instant start = Instant.now();
        try {
            BufferedReader reader = new BufferedReader(new FileReader("anime.db"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) { // leitura de cada linha do arquivo tsv
                sb.append(line).append("\n");
            }
            reader.close();
            String data = sb.toString(); // conversao de string builder em string

            // Inicializar dicionário Unicode
            Map<String, Integer> dictionary = new HashMap<>();
            for (int i = 0; i < 65536; i++) {
                dictionary.put("" + (char) i, i); // adiçao de cada caractere Unicode no dicionário
            }

            List<Integer> compressed = LZW.compress(data, dictionary); // compressao da string contendo conteudo do
                                                                       // arquivo

            // Salvando dados comprimidos usando FileOutputStream
            try (FileOutputStream fos = new FileOutputStream(comp);
                    DataOutputStream dos = new DataOutputStream(fos)) {
                for (int value : compressed) {
                    writeVariableLengthInt(dos, value);
                }
            }

            // Serializar dicionário
            serializeDictionary(dictionary, dictFile);

            long originalSize = new File("anime.db").length();
            long compressedSize = new File(comp).length();

            Instant end = Instant.now();
            Duration timeElapsed = Duration.between(start, end);
            double compressionPercentage = ((double) (originalSize - compressedSize) / originalSize) * 100;

            System.out.println("Compressao completada em: " + timeElapsed.toMillis() + " ms");
            System.out.println("Numero de bytes comprimidos: " + (originalSize - compressedSize) + " bytes");
            System.out.println("Porcentagem de compressao: " + compressionPercentage + "%");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Método para descompressao real
    public static void decompressReal() {
        Instant start = Instant.now();
        try {
            // Ler arquivo comprimido
            List<Integer> compressedRead = new ArrayList<>();
            try (FileInputStream fis = new FileInputStream("animeDBLZWCompressed.bin");
                    DataInputStream dis = new DataInputStream(fis)) {
                while (dis.available() > 0) {
                    compressedRead.add(readVariableLengthInt(dis));
                }
            }

            // Desserializar dicionário
            Map<Integer, String> dictionary = new HashMap<>();
            for (int i = 0; i < 65536; i++) {
                dictionary.put(i, "" + (char) i);
            }

            String decompressed = decompress(compressedRead, dictionary);

            // Salvar dados descomprimidos
            BufferedWriter writer = new BufferedWriter(new FileWriter("animeDBLZWDecompressed.db"));
            writer.write(decompressed);
            writer.close();

            // Verificar integridade
            BufferedReader reader = new BufferedReader(new FileReader("anime.db"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            reader.close();
            String originalData = sb.toString();

            if (originalData.equals(decompressed)) {
                System.out.println("Descompressao identica ao original");
                Instant end = Instant.now();
                Duration timeElapsed = Duration.between(start, end);
                System.out.println("Descompressao completada em: " + timeElapsed.toMillis() + " ms");
            } else {
                System.out.println("Descompressao falhou");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
