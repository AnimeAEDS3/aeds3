import java.io.*;

public class ArvoreBPlus {
    public static final int tamPag = 160;

    public int ordem;
    public RandomAccessFile arq;
    public String nomearq;
    public boolean diminuiu;
    public int chave; // ID
    public long ponteiroChave; // Ponteiro para outra página
    public long paginaAux;
    public boolean cresceu;

    public class Pagina {
        public int ordem;
        public int n;
        public int[] chaves;
        public long[] ponteirosChave;
        public long proxFolha;
        public long[] filhos;

        public Pagina(int ord) {
            n = 0;
            ordem = ord;
            chaves = new int[(ordem - 1)];
            ponteirosChave = new long[(ordem - 1)];
            filhos = new long[ord];
            proxFolha = -1;

            for (int i = 0; i < (ordem - 1); i++) {
                chaves[i] = -1;
                ponteirosChave[i] = -1L;
                filhos[i] = -1L;
            }
            filhos[ord - 1] = -1L;
        }

        public byte[] getBytes() throws IOException {
            ByteArrayOutputStream ba = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(ba);

            out.writeInt(n);

            for (int i = 0; i < n; i++) {
                out.writeLong(filhos[i]);
                out.writeInt(chaves[i]);
                out.writeLong(ponteirosChave[i]);
            }
            out.writeLong(filhos[n]);

            byte[] registroVazio = new byte[12];
            for (int i = n; i < (ordem - 1); i++) {
                out.write(registroVazio);
                out.writeLong(filhos[i + 1]);
            }

            out.writeLong(proxFolha);

            return ba.toByteArray();
        }

        public void setBytes(byte[] buffer) throws IOException {
            ByteArrayInputStream ba = new ByteArrayInputStream(buffer);
            DataInputStream in = new DataInputStream(ba);

            n = in.readInt();

            for (int i = 0; i < (ordem - 1); i++) {
                filhos[i] = in.readLong();
                chaves[i] = in.readInt();
                ponteirosChave[i] = in.readLong();
            }
            filhos[(ordem - 1)] = in.readLong();
            proxFolha = in.readLong();
        }
    }

    public ArvoreBPlus(int o, String na) throws IOException {
        ordem = o;
        nomearq = na;

        arq = new RandomAccessFile(nomearq, "rw");
        if (arq.length() < 8)
            arq.writeLong(-1);
    }

    public boolean create(int id, long pointer) throws IOException {
        if (id < 0) {
            System.out.println("O ID não pode ser negativo");
            return false;
        }

        arq.seek(0);
        long pagina = arq.readLong();
        chave = id;
        ponteiroChave = pointer;
        paginaAux = -1;
        cresceu = false;

        boolean inserido = createRec(pagina);

        if (cresceu) {
            Pagina novaPagina = new Pagina(ordem);
            novaPagina.n = 1;
            novaPagina.chaves[0] = chave;
            novaPagina.ponteirosChave[0] = ponteiroChave;
            novaPagina.filhos[0] = pagina;
            novaPagina.filhos[1] = paginaAux;
            long raiz = arq.length();
            arq.seek(raiz);
            arq.write(novaPagina.getBytes());
            arq.seek(0);
            arq.writeLong(raiz);
            inserido = true;
        }
        return inserido;
    }

    public boolean createRec(long pagina) throws IOException {
        if (pagina == -1) {
            cresceu = true;
            paginaAux = -1;
            return false;
        }

        arq.seek(pagina);
        Pagina pa = new Pagina(ordem);
        byte[] buffer = new byte[tamPag];
        arq.read(buffer);
        pa.setBytes(buffer);

        int i = 0;
        while (i < pa.n && chave > pa.chaves[i]) {
            i++;
        }

        boolean inserido;
        if (i == pa.n || chave < pa.chaves[i])
            inserido = createRec(pa.filhos[i]);
        else
            inserido = createRec(pa.filhos[i + 1]);

        if (!cresceu)
            return inserido;

        if (pa.n < (ordem - 1)) {
            // Shift elements to the right to make space
            for (int j = pa.n; j > i; j--) {
                pa.chaves[j] = pa.chaves[j - 1];
                pa.ponteirosChave[j] = pa.ponteirosChave[j - 1];
                pa.filhos[j + 1] = pa.filhos[j];
            }
            // Insert new element
            pa.chaves[i] = chave;
            pa.ponteirosChave[i] = ponteiroChave;
            pa.filhos[i + 1] = paginaAux;
            pa.n++;
            arq.seek(pagina);
            arq.write(pa.getBytes());
            cresceu = false;
            return true;
        }

        // Divisão de página
        Pagina np = new Pagina(ordem);
        int meio = (pa.n + 1) / 2;
        boolean novoElementoNaPaginaNova = i >= meio;

        for (int j = meio; j < pa.n; j++) {
            np.chaves[j - meio] = pa.chaves[j];
            np.ponteirosChave[j - meio] = pa.ponteirosChave[j];
            np.filhos[j - meio + 1] = pa.filhos[j + 1];
        }
        np.n = pa.n - meio;
        pa.n = meio;
        np.filhos[0] = pa.filhos[meio];

        if (novoElementoNaPaginaNova) {
            // Shift elements to the right in new page
            for (int j = np.n; j > i - meio; j--) {
                np.chaves[j] = np.chaves[j - 1];
                np.ponteirosChave[j] = np.ponteirosChave[j - 1];
                np.filhos[j + 1] = np.filhos[j];
            }
            // Insert new element in new page
            np.chaves[i - meio] = chave;
            np.ponteirosChave[i - meio] = ponteiroChave;
            np.filhos[i - meio + 1] = paginaAux;
            np.n++;
        } else {
            // Shift elements to the right in old page
            for (int j = meio; j > i; j--) {
                pa.chaves[j] = pa.chaves[j - 1];
                pa.ponteirosChave[j] = pa.ponteirosChave[j - 1];
                pa.filhos[j + 1] = pa.filhos[j];
            }
            // Insert new element in old page
            pa.chaves[i] = chave;
            pa.ponteirosChave[i] = ponteiroChave;
            pa.filhos[i + 1] = paginaAux;
            pa.n++;
        }

        // Prepare for new root
        chave = np.chaves[0];
        ponteiroChave = np.ponteirosChave[0];
        paginaAux = arq.length();
        arq.seek(paginaAux);
        arq.write(np.getBytes());
        arq.seek(pagina);
        arq.write(pa.getBytes());
        cresceu = true;
        return true;
    }

    public Long read(int id) throws IOException {
        arq.seek(0);
        long raiz = arq.readLong();
        if (raiz == -1) {
            return null; // Empty
        }
        return readRec(id, raiz);
    }

    public Long readRec(int id, long pagina) throws IOException {
        if (pagina == -1) {
            return null; // Chegou a uma folha sem encontrar o ID
        }

        // Lê a página atual
        arq.seek(pagina);
        Pagina pa = new Pagina(ordem);
        byte[] buffer = new byte[1024]; // Tamanho do buffer baseado no tamanho esperado da página
        arq.read(buffer);
        pa.setBytes(buffer);

        // Busca linear
        int i = 0;
        while (i < pa.n && id > pa.chaves[i]) {
            i++;
        }

        // Verifica se encontrou o ID na página atual
        if (i < pa.n && id == pa.chaves[i]) {
            return pa.ponteirosChave[i]; // Retorna o conteúdo (long) associado ao ID
        }

        // Continua a busca na sub-árvore apropriada
        return readRec(id, pa.filhos[i]);
    }

    public boolean delete(int id) throws IOException {
        arq.seek(0);
        long raiz = arq.readLong();
        if (raiz == -1) {
            return false; // Empty
        }

        diminuiu = false;
        boolean excluido = deleteRec(id, raiz, raiz);

        if (excluido && diminuiu) {
            arq.seek(raiz);
            Pagina paginaRaiz = new Pagina(ordem);
            byte[] buffer = new byte[tamPag];
            arq.read(buffer);
            paginaRaiz.setBytes(buffer);

            if (paginaRaiz.n == 0 && paginaRaiz.filhos[0] != -1) {
                arq.seek(0);
                arq.writeLong(paginaRaiz.filhos[0]); // Torna o primeiro filho a nova raiz.
            }
        }

        return excluido;
    }

    private boolean deleteRec(int id, long pagina, long raiz) throws IOException {
        if (pagina == -1) {
            diminuiu = false;
            return false; // ID não encontrado
        }

        arq.seek(pagina);
        Pagina paginaAtual = new Pagina(ordem);
        byte[] buffer = new byte[1024]; // Assumindo um tamanho suficiente
        arq.read(buffer);
        paginaAtual.setBytes(buffer);

        int i = 0;
        while (i < paginaAtual.n && id > paginaAtual.chaves[i]) {
            i++;
        }

        boolean excluido = false;
        if (i < paginaAtual.n && paginaAtual.filhos[0] == -1 && id == paginaAtual.chaves[i]) {
            for (int j = i; j < paginaAtual.n - 1; j++) {
                paginaAtual.chaves[j] = paginaAtual.chaves[j + 1];
                paginaAtual.ponteirosChave[j] = paginaAtual.ponteirosChave[j + 1];
            }
            paginaAtual.n--;
            arq.seek(pagina);
            arq.write(paginaAtual.getBytes());
            diminuiu = paginaAtual.n < (ordem - 1) / 2;
            excluido = true;
        } else if (paginaAtual.filhos[0] != -1) {
            if (i < paginaAtual.n && id == paginaAtual.chaves[i])
                i++;
            excluido = deleteRec(id, paginaAtual.filhos[i], raiz);
        }

        if (!diminuiu || !excluido) {
            return excluido;
        }

        reestruturar(pagina, raiz);

        return excluido;
    }

    private void reestruturar(long pagina, long raiz) throws IOException {
        if (pagina == raiz) {
            return; // Ajuste da raiz é tratado em outro lugar.
        }

        arq.seek(pagina);
        Pagina paginaAtual = new Pagina(ordem);
        byte[] buffer = new byte[tamPag];
        arq.read(buffer);
        paginaAtual.setBytes(buffer);

        long paginaPai = encontrarPaiPagina(pagina, raiz);
        arq.seek(paginaPai);
        Pagina pai = new Pagina(ordem);
        arq.read(buffer);
        pai.setBytes(buffer);

        int idx = 0;
        while (idx <= pai.n && pai.filhos[idx] != pagina)
            idx++;

        boolean borrowed = false;
        if (idx > 0) {
            borrowed = emprestIrmaoEsquerdo(pagina, pai.filhos[idx - 1], paginaPai, idx);
        }
        if (!borrowed && idx < pai.n) {
            borrowed = emprestIrmaoDireito(pagina, pai.filhos[idx + 1], paginaPai, idx);
        }

        if (!borrowed) {
            if (idx > 0) {
                fundirIrmas(pai.filhos[idx - 1], pagina, paginaPai, idx - 1);
            } else {
                fundirIrmas(pagina, pai.filhos[idx + 1], paginaPai, idx);
            }
        }
    }

    private boolean emprestIrmaoEsquerdo(long pagina, long irmaoEsquerdo, long paginaPai, int idx) throws IOException {
        arq.seek(irmaoEsquerdo);
        Pagina paginaEsquerda = new Pagina(ordem);
        byte[] buffer = new byte[tamPag];
        arq.read(buffer);
        paginaEsquerda.setBytes(buffer);

        if (paginaEsquerda.n > (ordem - 1) / 2) {
            arq.seek(pagina);
            Pagina paginaAtual = new Pagina(ordem);
            buffer = new byte[tamPag];
            arq.read(buffer);
            paginaAtual.setBytes(buffer);

            System.arraycopy(paginaAtual.chaves, 0, paginaAtual.chaves, 1, paginaAtual.n);
            System.arraycopy(paginaAtual.ponteirosChave, 0, paginaAtual.ponteirosChave, 1, paginaAtual.n);
            System.arraycopy(paginaAtual.filhos, 0, paginaAtual.filhos, 1, paginaAtual.n + 1);
            paginaAtual.n++;

            paginaAtual.chaves[0] = paginaEsquerda.chaves[paginaEsquerda.n - 1];
            paginaAtual.ponteirosChave[0] = paginaEsquerda.ponteirosChave[paginaEsquerda.n - 1];
            paginaAtual.filhos[0] = paginaEsquerda.filhos[paginaEsquerda.n];

            paginaEsquerda.n--;

            arq.seek(paginaPai);
            Pagina parent = new Pagina(ordem);
            buffer = new byte[tamPag];
            arq.read(buffer);
            parent.setBytes(buffer);
            parent.chaves[idx - 1] = paginaAtual.chaves[0];

            arq.seek(pagina);
            arq.write(paginaAtual.getBytes());
            arq.seek(irmaoEsquerdo);
            arq.write(paginaEsquerda.getBytes());
            arq.seek(paginaPai);
            arq.write(parent.getBytes());

            return true;
        }

        return false;
    }

    private boolean emprestIrmaoDireito(long pagina, long irmaoDireito, long paginaPai, int idx) throws IOException {
        arq.seek(irmaoDireito);
        Pagina paginaDireita = new Pagina(ordem);
        byte[] buffer = new byte[tamPag];
        arq.read(buffer);
        paginaDireita.setBytes(buffer);

        if (paginaDireita.n > (ordem - 1) / 2) {
            arq.seek(pagina);
            Pagina paginaAtual = new Pagina(ordem);
            buffer = new byte[tamPag];
            arq.read(buffer);
            paginaAtual.setBytes(buffer);

            paginaAtual.chaves[paginaAtual.n] = paginaDireita.chaves[0];
            paginaAtual.ponteirosChave[paginaAtual.n] = paginaDireita.ponteirosChave[0];
            paginaAtual.filhos[paginaAtual.n + 1] = paginaDireita.filhos[0];
            paginaAtual.n++;

            System.arraycopy(paginaDireita.chaves, 1, paginaDireita.chaves, 0, paginaDireita.n - 1);
            System.arraycopy(paginaDireita.ponteirosChave, 1, paginaDireita.ponteirosChave, 0, paginaDireita.n - 1);
            System.arraycopy(paginaDireita.filhos, 1, paginaDireita.filhos, 0, paginaDireita.n);

            paginaDireita.n--;

            arq.seek(paginaPai);
            Pagina parent = new Pagina(ordem);
            buffer = new byte[tamPag];
            arq.read(buffer);
            parent.setBytes(buffer);
            parent.chaves[idx] = paginaDireita.chaves[0];

            arq.seek(pagina);
            arq.write(paginaAtual.getBytes());
            arq.seek(irmaoDireito);
            arq.write(paginaDireita.getBytes());
            arq.seek(paginaPai);
            arq.write(parent.getBytes());

            return true;
        }

        return false;
    }

    private void fundirIrmas(long paginaEsquerda, long paginaDireita, long paginaPai, int idxPai) throws IOException {
        arq.seek(paginaEsquerda);
        Pagina esquerda = new Pagina(ordem);
        byte[] buffer = new byte[tamPag];
        arq.read(buffer);
        esquerda.setBytes(buffer);

        arq.seek(paginaDireita);
        Pagina direita = new Pagina(ordem);
        buffer = new byte[tamPag];
        arq.read(buffer);
        direita.setBytes(buffer);

        System.arraycopy(direita.chaves, 0, esquerda.chaves, esquerda.n, direita.n);
        System.arraycopy(direita.ponteirosChave, 0, esquerda.ponteirosChave, esquerda.n, direita.n);
        System.arraycopy(direita.filhos, 0, esquerda.filhos, esquerda.n + 1, direita.n + 1);
        esquerda.n += direita.n;

        arq.seek(paginaPai);
        Pagina pai = new Pagina(ordem);
        buffer = new byte[tamPag];
        arq.read(buffer);
        pai.setBytes(buffer);

        for (int i = idxPai; i < pai.n - 1; i++) {
            pai.chaves[i] = pai.chaves[i + 1];
            pai.ponteirosChave[i] = pai.ponteirosChave[i + 1];
            pai.filhos[i + 1] = pai.filhos[i + 2];
        }
        pai.n--;

        arq.seek(paginaEsquerda);
        arq.write(esquerda.getBytes());
        arq.seek(paginaPai);
        arq.write(pai.getBytes());
    }

    private long encontrarPaiPagina(long child, long current) throws IOException {
        arq.seek(current);
        Pagina paginaAtual = new Pagina(ordem);
        byte[] buffer = new byte[tamPag];
        arq.read(buffer);
        paginaAtual.setBytes(buffer);

        for (int i = 0; i <= paginaAtual.n; i++) {
            if (paginaAtual.filhos[i] == child) {
                return current;
            }
            if (paginaAtual.filhos[i] != -1) {
                long found = encontrarPaiPagina(child, paginaAtual.filhos[i]);
                if (found != -1)
                    return found;
            }
        }

        return -1; // Não encontrado
    }

    // Imprime a árvore, usando uma chamada recursiva.
    // A função recursiva é chamada com uma página de referência (raiz)
    public void imprimir() throws IOException {
        long raiz;
        arq.seek(0);
        raiz = arq.readLong();
        if (raiz != -1)
            imprimirRec(raiz);
        System.out.println();
    }

    // Impressão recursiva
    public void imprimirRec(long pagina) throws IOException {

        // Retorna das chamadas recursivas
        if (pagina == -1)
            return;

        // Lê o registro da página passada como referência no arq
        arq.seek(pagina);
        Pagina pa = new Pagina(ordem);
        byte[] buffer = new byte[tamPag];
        arq.read(buffer);
        pa.setBytes(buffer);

        // Imprime a página
        String endereco = String.format("%04d", pagina);
        System.out.print(endereco + " " + pa.n + ":"); // endereço e número de Chaves
        for (int i = 0; i < pa.n; i++) { // Ajustado para imprimir apenas até n
            System.out.print(
                    "(" + String.format("%04d", pa.filhos[i]) + ") " + pa.chaves[i] + "," + pa.ponteirosChave[i] + " ");
        }
        System.out.print("(" + String.format("%04d", pa.filhos[pa.n]) + ")");
        if (pa.proxFolha == -1)
            System.out.println();
        else
            System.out.println(" --> (" + String.format("%04d", pa.proxFolha) + ")");

        // Chama recursivamente cada filho, se a página não for folha
        if (pa.filhos[0] != -1) {
            for (int i = 0; i <= pa.n; i++)
                imprimirRec(pa.filhos[i]);
        }
    }
}