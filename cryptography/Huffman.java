package cryptography;

import java.io.*;
import java.util.*;

// Classe que cria nós da árvore
class Node {
    Character ch; // Caractere armazenado
    Integer freq; // Frequência do caractere
    Node left = null, right = null; // Filhos esquerdo e direito

    Node(Character ch, Integer freq) {
        this.ch = ch;
        this.freq = freq;
    }

    Node(Character ch, Integer freq, Node left, Node right) {
        this(ch, freq);
        this.left = left;
        this.right = right;
    }
}

// Classe principal para o algoritmo de Huffman
public class Huffman {

    // Método para construir a árvore de Huffman e processar o texto
    public static void createHuffmanTree(String text) {
        if (text == null || text.isEmpty()) return;

        // Contar frequência de cada caractere
        Map<Character, Integer> freq = new HashMap<>();
        for (char c : text.toCharArray()) {
            freq.put(c, freq.getOrDefault(c, 0) + 1);
        }

        // Fila de prioridade para gerenciar os nós da árvore
        PriorityQueue<Node> pq = new PriorityQueue<>(Comparator.comparingInt(n -> n.freq));
        freq.forEach((key, value) -> pq.add(new Node(key, value)));

        // Construir a árvore
        while (pq.size() > 1) {
            Node left = pq.poll();
            Node right = pq.poll();
            pq.add(new Node(null, left.freq + right.freq, left, right));
        }

        Node root = pq.peek();

        // Codificar os dados
        Map<Character, String> huffmanCode = new HashMap<>();
        encodeData(root, "", huffmanCode);

        System.out.println("Códigos de Huffman dos caracteres: " + huffmanCode);
        System.out.println("String inicial: " + text);

        StringBuilder encodedText = new StringBuilder();
        text.chars().forEach(c -> encodedText.append(huffmanCode.get((char) c)));
        saveBitsToFile(encodedText, "encodedHuffman.bin");

        if (isLeaf(root)) {
            while (root.freq-- > 0) System.out.print(root.ch);
        } else {
            decodeData(root, -1, encodedText);
        }
    }

    // Método para codificar os dados
    private static void encodeData(Node node, String str, Map<Character, String> huffmanCode) {
        if (node == null) return;
        if (isLeaf(node)) {
            huffmanCode.put(node.ch, str.isEmpty() ? "1" : str);
        }
        encodeData(node.left, str + '0', huffmanCode);
        encodeData(node.right, str + '1', huffmanCode);
    }

    // Método para decodificar os dados
    private static void decodeData(Node node, int index, StringBuilder sb) {
        if (node == null) return;
        while (++index < sb.length()) {
            node = sb.charAt(index) == '0' ? node.left : node.right;
            if (isLeaf(node)) {
                System.out.print(node.ch);
                node = sb.charAt(index) == '0' ? node.left : node.right;
            }
        }
    }

    // Verifica se o nó é uma folha
    private static boolean isLeaf(Node node) {
        return node.left == null && node.right == null;
    }

    // Lê dados de um arquivo
    public static String readFile(String filePath) {
        StringBuilder text = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                text.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return text.toString();
    }

    // Salva bits em um arquivo
    public static void saveBitsToFile(StringBuilder bits, String filePath) {
        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath))) {
            for (int i = 0; i < bits.length(); i += 8) {
                String byteString = bits.substring(i, Math.min(i + 8, bits.length()));
                bos.write(Integer.parseInt(byteString, 2));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Método principal
    public static void main(String[] args) {
        String filePath = "dataanime.tsv";
        String text = readFile(filePath);
        createHuffmanTree(text);
    }
}
