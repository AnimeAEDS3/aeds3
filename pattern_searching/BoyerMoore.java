package pattern_searching;

public class BoyerMoore {

    // Número de caracteres possíveis em um byte
    static int NO_OF_CHARS = 256;

    // Função para retornar o valor máximo entre dois números
    public static int max(int a, int b) {
        return (a > b) ? a : b;
    }

    // Função para retornar o valor máximo entre três números
    public static int max(int a, int b, int c) {
        return (a > b) ? ((a > c) ? a : c) : ((b > c) ? b : c);
    }

    // Função para construir a tabela de heurística de mau-caracter
    public static void badCharHeuristic(byte[] str, int size, int[] badchar) {
        // Inicializa todas as entradas de badchar[] como -1
        for (int i = 0; i < NO_OF_CHARS; i++)
            badchar[i] = -1;

        // Preenche o valor da última ocorrência de um byte
        for (int i = 0; i < size; i++)
            badchar[str[i] & 0xFF] = i;
    }

    // Função para construir o array de bons sufixos
    public static void goodSuffixHeuristic(byte[] pat, int[] goodSuffix) {
        int m = pat.length; // Comprimento do padrão
        int[] suffix = new int[m]; // Array para armazenar os valores de sufixo
        int f = 0, g; // Variáveis auxiliares

        // Fase 1: Preenchendo o array de sufixos
        suffix[m - 1] = m; // O sufixo mais à direita é todo o padrão
        g = m - 1; // Índice do sufixo mais à direita

        // Iterando do penúltimo sufixo até o primeiro
        for (int i = m - 2; i >= 0; i--) {
            // Verificando se o sufixo está dentro de um sufixo maior
            if (i > g && suffix[i + m - 1 - f] < i - g)
                suffix[i] = suffix[i + m - 1 - f]; // O sufixo está dentro de um sufixo maior, então copiamos o valor do
                                                   // sufixo maior
            else {
                // Caso contrário, encontramos um novo sufixo
                if (i < g)
                    g = i; // Atualizamos g para o novo sufixo
                f = i; // Atualizamos f para o início do novo sufixo
                // Comparamos os caracteres do sufixo com os caracteres à direita do sufixo até
                // o final do padrão
                while (g >= 0 && pat[g] == pat[g + m - 1 - f])
                    g--; // Movemos g para a esquerda enquanto os caracteres coincidirem
                // Atribuímos ao sufixo atual a diferença entre f e g, que é o tamanho do sufixo
                suffix[i] = f - g;
            }
        }

        // Fase 2: Preenchendo o array de bons sufixos
        for (int i = 0; i < m; i++)
            goodSuffix[i] = m; // Inicializamos todos os valores do array de bons sufixos com o comprimento do
                               // padrão

        // Iterando do último sufixo até o primeiro
        for (int i = m - 1; i >= 0; i--) {
            // Se o sufixo completo for um sufixo bom
            if (suffix[i] == i + 1) {
                // Atualizamos os valores do array de bons sufixos
                for (int j = 0; j < m - 1 - i; j++) {
                    if (goodSuffix[j] == m)
                        goodSuffix[j] = m - 1 - i;
                }
            }
        }
    }

    // Função para procurar a ocorrência do padrão no texto usando o algoritmo
    // Boyer-Moore
    public static void search(byte[] txt, byte[] pat) {
        int m = pat.length; // Comprimento do padrão
        int n = txt.length; // Comprimento do texto

        int[] badchar = new int[NO_OF_CHARS]; // Tabela de heurística de mau-caracter
        int[] goodSuffix = new int[m]; // Array de bons sufixos

        // Construção da tabela de heurística de mau-caracter
        badCharHeuristic(pat, m, badchar);
        // Construção do array de bons sufixos
        goodSuffixHeuristic(pat, goodSuffix);

        int s = 0; // s é o deslocamento do padrão em relação ao texto
        while (s <= (n - m)) {
            int j = m - 1;

            // Continua movendo o índice j para a esquerda enquanto os bytes do padrão e do
            // texto coincidem
            while (j >= 0 && pat[j] == txt[s + j])
                j--;

            // Se o padrão está presente no deslocamento atual
            if (j < 0) {
                System.out.println("Padrao ocorre no deslocamento = " + s);

                // Desloca o padrão para a próxima possível posição de correspondência
                // Utiliza a tabela de mau-caracter para determinar o próximo deslocamento
                s += (s + m < n) ? m - badchar[txt[s + m] & 0xFF] : 1;
            } else {
                // Desloca o padrão para o próximo possível deslocamento usando a função de
                // mau-caracter e bons sufixos
                s += max(1, j - badchar[txt[s + j] & 0xFF], goodSuffix[j]);
            }
        }
    }
}
