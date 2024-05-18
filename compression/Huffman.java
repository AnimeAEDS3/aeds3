package compression;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

// Classe principal para codificação e decodificação Huffman
public class Huffman {
    // Função para construir a árvore de Huffman
    public static void createHuffmanTree(String text) {
        if (text == null || text.length() == 0) {
            return;
        }

        Map<Character, Integer> freq = new HashMap<>();
        for (char c : text.toCharArray()) {
            freq.put(c, freq.getOrDefault(c, 0) + 1);
        }

        PriorityQueue<Node> pq = new PriorityQueue<>(Comparator.comparingInt(l -> l.freq));
        for (var entry : freq.entrySet()) {
            pq.add(new Node(entry.getKey(), entry.getValue()));
        }

        while (pq.size() != 1) {
            Node left = pq.poll();
            Node right = pq.poll();
            int sum = left.freq + right.freq;
            pq.add(new Node(null, sum, left, right));
        }

        Node root = pq.peek();
        Map<Character, String> huffmanCode = new HashMap<>();
        encodeData(root, "", huffmanCode);

        StringBuilder sb = new StringBuilder();
        for (char c : text.toCharArray()) {
            sb.append(huffmanCode.get(c));
        }

        saveBitsToFile(sb, "encodedHuffman.bin");
    }

    // Função para codificar os dados e armazenar os códigos de Huffman em um mapa
    public static void encodeData(Node root, String str, Map<Character, String> huffmanCode) {
        if (root == null) {
            return;
        }
        if (isLeaf(root)) {
            huffmanCode.put(root.ch, str.length() > 0 ? str : "1");
        }
        encodeData(root.left, str + '0', huffmanCode);
        encodeData(root.right, str + '1', huffmanCode);
    }

    // Função para decodificar a string codificada usando a árvore de Huffman
    public static String decodeData(Node root, StringBuilder sb) {
        StringBuilder result = new StringBuilder();
        Node current = root;
        for (int i = 0; i < sb.length(); i++) {
            if (sb.charAt(i) == '0') {
                current = current.left;
            } else {
                current = current.right;
            }

            if (isLeaf(current)) {
                result.append(current.ch);
                current = root;
            }
        }
        return result.toString();
    }

    // Função para verificar se um nó é uma folha
    public static boolean isLeaf(Node root) {
        return root.left == null && root.right == null;
    }

    // Função para ler o conteúdo de um arquivo
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

    // Função para ler bits de um arquivo
    public static StringBuilder readBitsFromFile(String filePath) {
        StringBuilder bits = new StringBuilder();
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(filePath))) {
            int byteValue;
            while ((byteValue = bis.read()) != -1) {
                bits.append(String.format("%8s", Integer.toBinaryString(byteValue)).replace(' ', '0'));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bits;
    }

    // Função para salvar bits em um arquivo
    public static void saveBitsToFile(StringBuilder bits, String filePath) {
        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath))) {
            for (int i = 0; i < bits.length(); i += 8) {
                String byteString = bits.substring(i, Math.min(i + 8, bits.length()));
                int byteValue = Integer.parseInt(byteString, 2);
                bos.write(byteValue);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Função para salvar texto em um arquivo
    public static void saveTextToFile(String text, String filePath) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(text);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Função para reconstruir a árvore de Huffman a partir do texto original
    public static Node reconstructHuffmanTree(String text) {
        Map<Character, Integer> freq = new HashMap<>();
        for (char c : text.toCharArray()) {
            freq.put(c, freq.getOrDefault(c, 0) + 1);
        }

        PriorityQueue<Node> pq = new PriorityQueue<>(Comparator.comparingInt(l -> l.freq));
        for (var entry : freq.entrySet()) {
            pq.add(new Node(entry.getKey(), entry.getValue()));
        }

        while (pq.size() != 1) {
            Node left = pq.poll();
            Node right = pq.poll();
            int sum = left.freq + right.freq;
            pq.add(new Node(null, sum, left, right));
        }

        return pq.peek();
    }
}
