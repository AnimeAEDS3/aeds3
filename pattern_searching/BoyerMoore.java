package pattern_searching;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Scanner;

public class BoyerMoore {

    // Número de caracteres possíveis em um byte
    static int NO_OF_CHARS = 256;

    // Função para retornar o valor máximo entre dois números
    static int max(int a, int b) {
        return (a > b) ? a : b;
    }

    // Função para construir a tabela de heurística de mau-caracter
    static void badCharHeuristic(byte[] str, int size, int[] badchar) {
        // Inicializa todas as entradas de badchar[] como -1
        for (int i = 0; i < NO_OF_CHARS; i++)
            badchar[i] = -1;

        // Preenche o valor da última ocorrência de um byte
        for (int i = 0; i < size; i++)
            badchar[(int) str[i] & 0xFF] = i;
    }

    // Função para procurar a ocorrência do padrão no texto usando o algoritmo Boyer-Moore
    static void search(byte[] txt, byte[] pat) {
        int m = pat.length; // Comprimento do padrão
        int n = txt.length; // Comprimento do texto

        int[] badchar = new int[NO_OF_CHARS]; // Tabela de heurística de mau-caracter

        // Construção da tabela de heurística de mau-caracter
        badCharHeuristic(pat, m, badchar);

        int s = 0; // s é o deslocamento do padrão em relação ao texto
        while (s <= (n - m)) {
            int j = m - 1;

            // Continua movendo o índice j para a esquerda enquanto os bytes do padrão e do texto coincidem
            while (j >= 0 && pat[j] == txt[s + j])
                j--;

            // Se o padrão está presente no deslocamento atual
            if (j < 0) {
                System.out.println("Padrão ocorre no deslocamento = " + s);

                // Desloca o padrão para a próxima possível posição de correspondência
                // Utiliza a tabela de mau-caracter para determinar o próximo deslocamento
                s += (s + m < n) ? m - badchar[txt[s + m] & 0xFF] : 1;
            } else {
                // Desloca o padrão para o próximo possível deslocamento usando a função de mau-caracter
                s += max(1, j - badchar[txt[s + j] & 0xFF]);
            }
        }
    }

    // Função principal para executar a pesquisa no arquivo anime.db
    public static void main(String[] args) {
        // Lê o nome do arquivo e o padrão a ser pesquisado do usuário
        Scanner scanner = new Scanner(System.in);

        String fileName = "anime.db";

        System.out.print("Digite o padrão a ser pesquisado: ");
        String patternStr = scanner.nextLine();

        byte[] pattern = patternStr.getBytes(); // Converte a string do padrão para bytes

        File file = new File(fileName);
        byte[] fileBytes = null;

        try (FileInputStream fis = new FileInputStream(file)) {
            fileBytes = new byte[(int) file.length()];
            fis.read(fileBytes);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        // Executa a pesquisa
        search(fileBytes, pattern);
    }
}
