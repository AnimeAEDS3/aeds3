package cryptography;

import java.math.BigInteger;
import java.security.SecureRandom;

public class RSA {
    private BigInteger n; // Produto dos dois números primos p e q
    private BigInteger d; // Expoente privado
    private BigInteger e; // Expoente público
    private int bitlen = 1024; // Tamanho dos números primos

    public RSA(BigInteger newn, BigInteger newe, BigInteger newd) {
        n = newn;
        e = newe;
        d = newd;
    }

    public RSA(int bits) {
        bitlen = bits;
        SecureRandom r = new SecureRandom();
        BigInteger p = new BigInteger(bitlen / 2, 100, r);
        BigInteger q = new BigInteger(bitlen / 2, 100, r);
        n = p.multiply(q);
        BigInteger m = (p.subtract(BigInteger.ONE)).multiply(q.subtract(BigInteger.ONE));
        e = new BigInteger("3");

        // Escolhendo e tal que 1 < e < m e gcd(e, m) = 1 (e e m são primos entre si)
        while (m.gcd(e).intValue() > 1) {
            e = e.add(new BigInteger("2"));
        }
        d = e.modInverse(m);
    }

    // Criptografa a mensagem usando a chave pública
    public synchronized String encrypt(String message) {
        return (new BigInteger(message.getBytes())).modPow(e, n).toString();
    }

    // Descriptografa a mensagem usando a chave privada
    public synchronized String decrypt(String message) {
        return new String((new BigInteger(message)).modPow(d, n).toByteArray());
    }

    // Métodos para obter a chave pública e privada
    public synchronized BigInteger getPublicKey() {
        return e;
    }

    public synchronized BigInteger getPrivateKey() {
        return d;
    }

    public synchronized BigInteger getModulus() {
        return n;
    }

    public static void main(String[] args) {
        RSA rsa = new RSA(1024);

        String text1 = "Texto a ser criptografado";
        System.out.println("Texto original: " + text1);

        String encrypted = rsa.encrypt(text1);
        System.out.println("Texto criptografado: " + encrypted);

        String decrypted = rsa.decrypt(encrypted);
        System.out.println("Texto descriptografado: " + decrypted);
    }
}