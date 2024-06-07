package cryptography;

import java.io.*;
import java.time.Duration;
import java.time.Instant;
import java.nio.file.Files;
import java.nio.file.Paths;


public class Caesar {

    // Método para criptografar texto usando cifra de César
    public static String encrypt(String text, int shift) {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            // Garantir que o valor resultante permaneça dentro do intervalo Unicode
            char shifted = (char) ((c + shift) % 65536); 
            result.append(shifted);
        }

        return result.toString();
    }

    // Método para descriptografar texto usando cifra de César
    public static String decrypt(String text, int shift) {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            // Garantir que o valor resultante permaneça dentro do intervalo Unicode
            char shifted = (char) ((c - shift + 65536) % 65536); 
            result.append(shifted);
        }

        return result.toString();
    }

    public static void encryptReal(int shift) {
        String comp = "animeDBCesarEncrypted.bin";
        Instant start = Instant.now();
        try {
            // Read file as bytes
            byte[] bytes = Files.readAllBytes(Paths.get("anime.db"));
            // Encrypt bytes
            byte[] encryptedBytes = Caesar.encrypt(new String(bytes), shift).getBytes();
            // Write encrypted bytes to file
            Files.write(Paths.get(comp), encryptedBytes);
    
            long originalSize = new File("anime.db").length();
            long encryptedSize = new File(comp).length();
    
            Instant end = Instant.now();
            Duration timeElapsed = Duration.between(start, end);
    
            System.out.println("Criptografia completada em: " + timeElapsed.toMillis() + " ms");
            System.out.println("Tamanho original: " + originalSize + " bytes");
            System.out.println("Tamanho criptografado: " + encryptedSize + " bytes");
    
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void decryptReal(int shift) {
        String comp = "animeDBCesarEncrypted.bin";
        String decryptedFile = "animeDBCesarDecrypted.db";
        Instant start = Instant.now();
        try {
            // Read encrypted file as bytes
            byte[] encryptedBytes = Files.readAllBytes(Paths.get(comp));
            // Decrypt bytes
            byte[] decryptedBytes = Caesar.decrypt(new String(encryptedBytes), shift).getBytes();
            // Write decrypted bytes to file
            Files.write(Paths.get(decryptedFile), decryptedBytes);
    
            // Check integrity
            String originalData = new String(Files.readAllBytes(Paths.get("anime.db")));
            String decryptedData = new String(decryptedBytes);
    
            if (originalData.equals(decryptedData)) {
                System.out.println("Descriptografia idêntica ao original");
                Instant end = Instant.now();
                Duration timeElapsed = Duration.between(start, end);
                System.out.println("Descriptografia completada em: " + timeElapsed.toMillis() + " ms");
            } else {
                System.out.println("Descriptografia falhou");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        int shift = 3;
        encryptReal(shift);
        decryptReal(shift);
    }
}
