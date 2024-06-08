package cryptography;

import java.math.BigInteger;


class ExtendedEuclidean {

    static class Triplet {
        BigInteger gcd;
        BigInteger x;
        BigInteger y;

        Triplet(BigInteger gcd, BigInteger x, BigInteger y) {
            this.gcd = gcd;
            this.x = x;
            this.y = y;
        }
    }

    public static Triplet extendedEuclideanAlgo(BigInteger a, BigInteger b) {
        if (b.equals(BigInteger.ZERO)) {
            return new Triplet(a, BigInteger.ONE, BigInteger.ZERO);
        }
        Triplet ans = extendedEuclideanAlgo(b, a.mod(b));
        return new Triplet(ans.gcd, ans.y, ans.x.subtract(a.divide(b).multiply(ans.y)));
    }

    public static BigInteger modInverse(BigInteger a, BigInteger m) {
        Triplet ans = extendedEuclideanAlgo(a, m);
        return ans.x.mod(m).add(m).mod(m);
    }
}
