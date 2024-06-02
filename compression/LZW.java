package compression;

import java.io.*;
import java.util.*;

public class LZW {

    public static List<Integer> compress(String uncompressed) {
        int dictSize = 65536; // Tamanho do dicionário para incluir todos os caracteres Unicode
        Map<String, Integer> dictionary = new HashMap<>();
        for (int i = 0; i < 65536; i++) {
            dictionary.put("" + (char)i, i); // adição de cada caractere unicode no dicionário
        }

        String w = "";
        List<Integer> result = new ArrayList<>();
        for (char c : uncompressed.toCharArray()) {  // percorre cada caractere do conteudo do arquivo ainda descomprimido
            String wc = w + c;
            if (dictionary.containsKey(wc))
                w = wc;  // se a sequência (w + c) está no dicionário, atualiza w para wc
            else {
                result.add(dictionary.get(w)); // adiciona o código de w ao resultado
                dictionary.put(wc, dictSize++);  // adiciona wc ao dicionário como um novo código
                 w = "" + c;  // reinicia w com o caractere atual
            }
        }

        if (!w.equals("")) {
            result.add(dictionary.get(w)); // adiciona o código da última sequência w ao resultado
        }
        return result;
    }

    public static String decompress(List<Integer> compressed) {
        // criação do dicionário novamente pra realizar a operação
        int dictSize = 65536;
        Map<Integer, String> dictionary = new HashMap<>();
        for (int i = 0; i < 65536; i++) {
            dictionary.put(i, "" + (char)i);
        }

        String w = "" + (char)(int)compressed.remove(0);
        StringBuilder result = new StringBuilder(w);
        for (int k : compressed) {
        String entry;
            if (dictionary.containsKey(k))
            entry = dictionary.get(k);  // se o código está no dicionário, pega a sequência correspondente
            else if (k == dictSize)
            entry = w + w.charAt(0);  //caso especial: o código é igual ao tamanho do dicionário
            else
                throw new IllegalArgumentException("De errado: " + k);

        result.append(entry);  // adiciona a sequência ao resultado
        dictionary.put(dictSize++, w + entry.charAt(0));  // adiciona a nova sequência ao dicionário
        w = entry;  // atualiza w para a sequência atual
    }
        return result.toString();
    }

    // escreve um inteiro usando codificação de comprimento variável, assim economizando espaço, pois, por exemplo,
    // um número como "1" que ocuparia 4 bytes por padrão, por se tratar de um número inteiro, agora ocupa só 1
    public static void writeVariableLengthInt(DataOutputStream dos, int value) throws IOException {
        while ((value & ~0x7F) != 0) { // esse loop continua enquanto o valor ainda tiver mais do que 7 bits significativos
            // escreve o Byte com MSB (most significant byte) definido
            dos.writeByte((value & 0x7F) | 0x80);
            value >>>= 7;
        }
        dos.writeByte(value & 0x7F); // após sair do loop, o valor restante é escrito
    }

    // lê um inteiro usando codificação de comprimento variável
    public static int readVariableLengthInt(DataInputStream dis) throws IOException {
        int value = 0;
        int shift = 0;
        byte b;
        do {
            b = dis.readByte(); // Lê um byte
            value |= (b & 0x7F) << shift; // Adiciona os 7 bits ao valor
            shift += 7; // Atualiza o deslocamento
        } while ((b & 0x80) != 0); // Continua se o MSB estiver definido
        return value; // Retorna o valor completo
    }

    public static void compressReal() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader("dataanime.tsv"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) { // leitura de cada linha do arquivo tsv
                sb.append(line).append("\n");
            }
            reader.close();
            String data = sb.toString(); // conversão de string builder em string

            // Compressão
            long startTime = System.nanoTime();
            List<Integer> compressed = LZW.compress(data); // compressão da string contendo conteúdo do arquivo
            long endTime = System.nanoTime();
            double compressionTime = (endTime - startTime) / 1e6; 

            // Salvando dados comprimidos usando FileOutputStream
            try (FileOutputStream fos = new FileOutputStream("dataanimeLZWCompressed.bin");
                 DataOutputStream dos = new DataOutputStream(fos)) {
                for (int value : compressed) {
                    writeVariableLengthInt(dos, value);
                }
            }
            System.out.println("Compression Time: " + compressionTime + " ms");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void decompressReal() {
        try {
            // Leitura do arquivo comprimido usando FileInputStream
            List<Integer> compressedRead = new ArrayList<>();
            try (FileInputStream fis = new FileInputStream("dataanimeLZWCompressed.bin");
                 DataInputStream dis = new DataInputStream(fis)) {
                while (dis.available() > 0) {
                    compressedRead.add(readVariableLengthInt(dis));
                }
            }

            // Descompressão
            long startTime = System.nanoTime();
            String decompressed = decompress(compressedRead); //descompressão pela lista de números do arquivo bin
            long endTime = System.nanoTime();
            double decompressionTime = (endTime - startTime) / 1e6; 

            // Salvando dados descomprimidos
            BufferedWriter writer = new BufferedWriter(new FileWriter("dataanimeLZWDecompressed.tsv"));
            writer.write(decompressed); // escrevendo a string contendo o conteudo do arquivo descomprimido
            writer.close();

            System.out.println("Decompression Time: " + decompressionTime + " ms");

            // Verificação de integridade
            BufferedReader reader = new BufferedReader(new FileReader("dataanime.tsv"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            reader.close();
            String originalData = sb.toString();

            if (originalData.equals(decompressed)) {
                System.out.println("Decompression is correct");
            } else {
                System.out.println("Decompression is incorrect");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
