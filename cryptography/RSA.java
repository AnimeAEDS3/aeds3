package cryptography;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class RSA {

    private static long p = 32452843; // Número primo com 8 dígitos decimais
    private static long q = 32452867; // Outro número primo com 8 dígitos decimais

    // Calcula n e φ(n)
    private static long n = p * q;
    private static long phi = (p - 1) * (q - 1);

    // Define o expoente público e calcula a chave privada d
    private static long e = 65537; // Comum usar 65537 como expoente público
    private static long d = modinv(e, phi);

    // Método para criptografar um arquivo
    public static void encryptFile(String inputFilePath, String outputFilePath) throws IOException {
        try (InputStream inputStream = Files.newInputStream(Paths.get(inputFilePath));
             OutputStream outputStream = Files.newOutputStream(Paths.get(outputFilePath))) {

            int character;
            long totalCharacters = 0;
            long startTime = System.currentTimeMillis();

            // Calcula o tamanho total do arquivo para mostrar o progresso
            long totalBytes = Files.size(Paths.get(inputFilePath));
            long processedBytes = 0;
            char[] loadingAnim = {'-', '\\', '|', '/'};
            int animIndex = 0;

            // Lê cada caractere do arquivo de entrada
            while ((character = inputStream.read()) != -1) {
                long plaintext = character;
                long ciphertext = powmod_verbose(plaintext, e, n);
                outputStream.write(longToBytes(ciphertext));
                totalCharacters++;
                processedBytes++;

                // Exibe a animação de carregamento e a porcentagem
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
        try (InputStream inputStream = Files.newInputStream(Paths.get(inputFilePath));
             OutputStream outputStream = Files.newOutputStream(Paths.get(outputFilePath))) {

            byte[] buffer = new byte[8];
            int bytesRead;
            long totalCharacters = 0;
            long startTime = System.currentTimeMillis();

            // Calcula o tamanho total do arquivo para mostrar o progresso
            long totalBytes = Files.size(Paths.get(inputFilePath));
            long processedBytes = 0;
            char[] loadingAnim = {'-', '\\', '|', '/'};
            int animIndex = 0;

            // Lê cada bloco de 8 bytes do arquivo criptografado
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                long ciphertext = bytesToLong(buffer);
                long decrypted = powmod_verbose(ciphertext, d, n);
                outputStream.write((int) decrypted);
                totalCharacters++;
                processedBytes += bytesRead;

                // Exibe a animação de carregamento e a porcentagem
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

    // Método para calcular a exponenciação modular
    public static long powmod_verbose(long base, long exponent, long modulus) {
        long result = 1;
        base = base % modulus;

        while (exponent > 0) {
            if ((exponent & 1) == 1) {
                result = (result * base) % modulus;
            }
            exponent = exponent >> 1;
            base = (base * base) % modulus;
        }

        return result;
    }

    // Método para calcular o inverso modular
    public static long modinv(long a, long m) {
        long m0 = m;
        long y = 0, x = 1;

        if (m == 1)
            return 0;

        while (a > 1) {
            long q = a / m;
            long t = m;

            m = a % m;
            a = t;
            t = y;

            y = x - q * y;
            x = t;
        }

        if (x < 0)
            x += m0;

        return x;
    }

    // Método para converter um array de bytes em um número long
    public static long bytesToLong(byte[] bytes) {
        long value = 0;
        for (int i = 0; i < bytes.length; i++) {
            value = (value << 8) + (bytes[i] & 0xff);
        }
        return value;
    }

    // Método para converter um número long em um array de bytes
    public static byte[] longToBytes(long value) {
        byte[] result = new byte[8];
        for (int i = 7; i >= 0; i--) {
            result[i] = (byte) (value & 0xff);
            value >>= 8;
        }
        return result;
    }
}
