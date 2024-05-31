package compression;

import java.io.Serializable;

// Classe Node para representar os nós da árvore de Huffman
public class Node implements Serializable {
    private static final long serialVersionUID = 1L;
    
    Character ch;
    int freq;
    Node left, right;

    Node(Character ch, int freq) {
        this.ch = ch;
        this.freq = freq;
    }

    Node(Character ch, int freq, Node left, Node right) {
        this.ch = ch;
        this.freq = freq;
        this.left = left;
        this.right = right;
    }
}
