package cryptography;

import java.io.*;
import java.math.BigInteger;

public class RSA {

    private static BigInteger p = new BigInteger("104729"); // Número primo
    private static BigInteger q = new BigInteger("1299709"); // Outro número primo

    // Calcula n e φ(n)
    private static BigInteger n = p.multiply(q);
    private static BigInteger phi = (p.subtract(BigInteger.ONE)).multiply(q.subtract(BigInteger.ONE));

    // Define o expoente público e calcula a chave privada d
    private static BigInteger e = new BigInteger("65537"); // Usando 65537 como expoente público
    private static BigInteger d = e.modInverse(phi);

    public static void main(String[] args) throws IOException {

        // Exibe as chaves públicas e privadas
        System.out.println("Chave pública (e, n): (" + e + ", " + n + ")");
        System.out.println("Chave privada (d, n): (" + d + ", " + n + ")");

        // Caminhos dos arquivos de entrada e saída
        String inputFilePath = "anime.db";
        String encryptedFilePath = "RSAencrypted_anime.db";
        String decryptedFilePath = "RSAdecrypted_anime.db";

        // Realiza a criptografia e descriptografia dos arquivos
        encryptFile(inputFilePath, encryptedFilePath);
        decryptFile(encryptedFilePath, decryptedFilePath);
    }

    // Método para criptografar um arquivo
    public static void encryptFile(String inputFilePath, String outputFilePath) throws IOException {
        try (InputStream inputStream = new FileInputStream(inputFilePath);
             DataOutputStream outputStream = new DataOutputStream(new FileOutputStream(outputFilePath))) {

            int character;
            long totalCharacters = 0;
            long startTime = System.currentTimeMillis();

            // Calcula o tamanho total do arquivo para mostrar o progresso
            long totalBytes = new File(inputFilePath).length();
            long processedBytes = 0;
            char[] loadingAnim = {'-', '\\', '|', '/'};
            int animIndex = 0;

            // Lê cada byte do arquivo de entrada
            while ((character = inputStream.read()) != -1) {
                BigInteger plaintext = BigInteger.valueOf(character);
                BigInteger ciphertext = plaintext.modPow(e, n);
                byte[] ciphertextBytes = ciphertext.toByteArray();
                outputStream.writeInt(ciphertextBytes.length); // Escreve o tamanho do valor criptografado
                outputStream.write(ciphertextBytes); // Escreve o valor criptografado como bytes
                totalCharacters++;
                processedBytes++;

                // Atualiza o progresso na mesma linha
                if (processedBytes % 100 == 0) {
                    double progress = (double) processedBytes / totalBytes * 100;
                    System.out.print("\rCriptografando: " + loadingAnim[animIndex % 4] + " " + String.format("%.2f", progress) + "%");
                    animIndex++;
                }
            }

            long endTime = System.currentTimeMillis();
            System.out.println("\nCriptografia concluída.");
            System.out.println("Total de caracteres criptografados: " + totalCharacters);
            System.out.println("Tempo total: " + (endTime - startTime) + " ms");
        }
    }

    // Método para descriptografar um arquivo
    public static void decryptFile(String inputFilePath, String outputFilePath) throws IOException {
        try (DataInputStream inputStream = new DataInputStream(new FileInputStream(inputFilePath));
             OutputStream outputStream = new FileOutputStream(outputFilePath)) {

            long totalCharacters = 0;
            long startTime = System.currentTimeMillis();

            // Calcula o tamanho total do arquivo para mostrar o progresso
            long totalBytes = new File(inputFilePath).length();
            long processedBytes = 0;
            char[] loadingAnim = {'-', '\\', '|', '/'};
            int animIndex = 0;

            // Lê cada valor criptografado do arquivo de entrada
            while (inputStream.available() > 0) {
                int length = inputStream.readInt(); // Lê o tamanho do valor criptografado
                byte[] ciphertextBytes = new byte[length];
                inputStream.readFully(ciphertextBytes); // Lê o valor criptografado
                BigInteger ciphertext = new BigInteger(ciphertextBytes);
                BigInteger decrypted = ciphertext.modPow(d, n);
                outputStream.write(decrypted.intValue()); // Escreve o valor descriptografado como byte
                totalCharacters++;
                processedBytes += length + 4; // Adiciona o tamanho do valor criptografado mais 4 bytes para o comprimento

                // Atualiza o progresso na mesma linha
                if (processedBytes % 100 == 0) {
                    double progress = (double) processedBytes / totalBytes * 100;
                    System.out.print("\rDescriptografando: " + loadingAnim[animIndex % 4] + " " + String.format("%.2f", progress) + "%");
                    animIndex++;
                }
            }

            long endTime = System.currentTimeMillis();
            System.out.println("\nDescriptografia concluída.");
            System.out.println("Total de caracteres descriptografados: " + totalCharacters);
            System.out.println("Tempo total: " + (endTime - startTime) + " ms");
        }
    }
}
