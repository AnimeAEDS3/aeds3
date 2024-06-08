package cryptography;

import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.io.FileOutputStream;

public class RSA {

    private BigInteger p;
    private BigInteger q;
    private BigInteger N;
    private BigInteger phi;
    private BigInteger e;
    private BigInteger d;
    private int bitlength = 1024;
    private int certainty = 10;
    private Random r;

    public RSA() {
        r = new Random();
        p = probablePrime(bitlength, r);
        q = probablePrime(bitlength, r);
        N = p.multiply(q);
        phi = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE));
        e = BigInteger.valueOf(65537);
        d = ExtendedEuclidean.modInverse(e, phi);
    }

    private BigInteger probablePrime(int bitlength, Random r) {
        return new BigInteger(bitlength, certainty, r);
    }

    public static void main(String[] args) throws Exception {
        RSA rsa = new RSA();

        String inputFile = "anime.db";
        String encryptedFile = "animeDBRSAEncrypted.bin";
        String decryptedFile = "animeDBRSADecrypted.db";

        // Encrypt the file
        rsa.encryptFile(inputFile, encryptedFile);

        // Decrypt the file
        rsa.decryptFile(encryptedFile, decryptedFile);

        // Verify the files
        rsa.verifyFiles(inputFile, decryptedFile);
    }

    private void encryptFile(String inputFile, String encryptedFile) throws Exception {
        byte[] fileBytes = Files.readAllBytes(Paths.get(inputFile));
        int blockSize = bitlength / 8 - 11; // RSA block size (in bytes)
        int blockCount = (int) Math.ceil((double) fileBytes.length / blockSize);

        try (FileOutputStream fos = new FileOutputStream(encryptedFile)) {
            for (int i = 0; i < blockCount; i++) {
                int start = i * blockSize;
                int end = Math.min(start + blockSize, fileBytes.length);
                byte[] block = Arrays.copyOfRange(fileBytes, start, end);
                byte[] encryptedBlock = encrypt(block, e, N);
                fos.write(intToByteArray(encryptedBlock.length));
                fos.write(encryptedBlock);
            }
        }

        System.out.println("File encrypted: " + encryptedFile);
    }

    private void decryptFile(String encryptedFile, String decryptedFile) throws Exception {
        byte[] encryptedBytes = Files.readAllBytes(Paths.get(encryptedFile));
        ArrayList<byte[]> encryptedBlocks = new ArrayList<>();

        int i = 0;
        while (i < encryptedBytes.length) {
            int blockLength = byteArrayToInt(Arrays.copyOfRange(encryptedBytes, i, i + 4));
            i += 4;
            byte[] block = Arrays.copyOfRange(encryptedBytes, i, i + blockLength);
            encryptedBlocks.add(block);
            i += blockLength;
        }

        try (FileOutputStream fos = new FileOutputStream(decryptedFile)) {
            for (int j = 0; j < encryptedBlocks.size(); j++) {
                byte[] decryptedBlock = decrypt(encryptedBlocks.get(j), d, N);
                // For the last block, ensure we remove any padding if present
                if (j == encryptedBlocks.size() - 1) {
                    int paddingIndex = decryptedBlock.length;
                    for (int k = decryptedBlock.length - 1; k >= 0; k--) {
                        if (decryptedBlock[k] != 0) {
                            paddingIndex = k + 1;
                            break;
                        }
                    }
                    decryptedBlock = Arrays.copyOf(decryptedBlock, paddingIndex);
                }
                fos.write(decryptedBlock);
            }
        }

        System.out.println("File decrypted: " + decryptedFile);
    }

    private void verifyFiles(String originalFile, String decryptedFile) throws Exception {
        byte[] originalBytes = Files.readAllBytes(Paths.get(originalFile));
        byte[] decryptedBytes = Files.readAllBytes(Paths.get(decryptedFile));

        if (Arrays.equals(originalBytes, decryptedBytes)) {
            System.out.println("Verification successful: Files are identical.");
        } else {
            System.out.println("Verification failed: Files are not identical.");
        }
    }

    private byte[] encrypt(byte[] message, BigInteger e, BigInteger N) {
        return (new BigInteger(1, message)).modPow(e, N).toByteArray();
    }

    private byte[] decrypt(byte[] message, BigInteger d, BigInteger N) {
        return (new BigInteger(1, message)).modPow(d, N).toByteArray();
    }

    private static byte[] intToByteArray(int value) {
        return new byte[] {
            (byte)(value >> 24),
            (byte)(value >> 16),
            (byte)(value >> 8),
            (byte)value
        };
    }

    private static int byteArrayToInt(byte[] bytes) {
        return ((bytes[0] & 0xFF) << 24) |
               ((bytes[1] & 0xFF) << 16) |
               ((bytes[2] & 0xFF) << 8) |
               (bytes[3] & 0xFF);
    }
}