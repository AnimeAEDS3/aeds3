import java.io.*;

// Escolhemos a árvore b+ pra conseguir pegar vários registros simultaneamente caso fosse necessário. Na nossa implementação não foi necessário, mas é possível.

public class ArvoreBPlus {
    public static final int tamPag = 160;

    public int ordem;          
    public RandomAccessFile arq;
    public String nomearq;
    public boolean diminuiu; 
    public int chave;           // ID
    public long ponteiroChave;  // Ponteiro para outra página
    public long paginaAux;
    public boolean cresceu;
    
    public ArvoreBPlus(int ord, String nArq) throws IOException {
        ordem = ord;
        nomearq = nArq;
        
        arq = new RandomAccessFile(nomearq, "rw");
        if (arq.length() < 8) 
            arq.writeLong(-1);
    }

    public boolean create(int id, long pointer) throws IOException {
        // Verifica se o ID fornecido é válido (não pode ser negativo)
        if (id < 0) {
            System.out.println("O ID não pode ser negativo");
            return false;
        }
    
        // Acessa a raiz da árvore para começar a inserção
        arq.seek(0);
        long raiz = arq.readLong();
        chave = id;
        ponteiroChave = pointer;
        paginaAux = -1;
        cresceu = false;
    
        // Realiza a inserção de forma recursiva começando pela raiz
        boolean inserido = createRec(raiz);
    
        // Verifica se após a inserção, a raiz precisa ser dividida e a árvore cresceu
        if (cresceu) {
            // Cria uma nova raiz porque a antiga foi dividida
            Pagina novaRaiz = new Pagina(ordem);
            novaRaiz.numElementos = 1;
            novaRaiz.chaves[0] = chave;  // Chave promovida para a nova raiz
            novaRaiz.ponteirosChave[0] = ponteiroChave;
            novaRaiz.filhos[0] = raiz;           // Filho esquerdo é a antiga raiz
            novaRaiz.filhos[1] = paginaAux;      // Filho direito é a nova página criada durante a divisão
    
            // Salva a nova raiz no final do arquivo
            long novaPosicaoRaiz = arq.length();
            arq.seek(novaPosicaoRaiz);
            arq.write(novaRaiz.getBytes());
    
            // Atualiza o ponteiro da raiz no início do arquivo para apontar para a nova raiz
            arq.seek(0);
            arq.writeLong(novaPosicaoRaiz);
            inserido = true;
        }
        return inserido;
    }
    
    public boolean createRec(long paginaAtual) throws IOException {
        
        if (paginaAtual == -1) {
            cresceu = true;
            paginaAux = -1;
            return false;  // Sinaliza que a nova chave deve ser inserida aqui
        }
    
        // Carrega a página atual do arquivo para verificar onde inserir a nova chave
        arq.seek(paginaAtual);
        Pagina pagina = new Pagina(ordem);
        byte[] byteA = new byte[tamPag]; // Assume-se um tamanho padrão de página, definido pela ordem da árvore
        arq.read(byteA);
        pagina.setBytes(byteA);
    
        // Encontra a posição correta na página atual para a nova chave ou decide para qual filho prosseguir
        int i = 0;
        while (i < pagina.numElementos && chave > pagina.chaves[i]) {
            i++;
        }
    
        // Inserção recursiva na sub-árvore apropriada
        boolean inserido;
        if (i == pagina.numElementos || chave < pagina.chaves[i]) {
            inserido = createRec(pagina.filhos[i]);
        } else {
            inserido = createRec(pagina.filhos[i + 1]);
        }
    
        // Se não há necessidade de crescer, retorna o resultado da inserção
        if (!cresceu) return inserido;
    
        // Verifica se há espaço na página atual para inserir a nova chave diretamente
        if (pagina.numElementos < (ordem-1)) {
            // Desloca elementos para criar espaço para a nova chave
            for (int j = pagina.numElementos; j > i; j--) {
                pagina.chaves[j] = pagina.chaves[j - 1];
                pagina.ponteirosChave[j] = pagina.ponteirosChave[j - 1];
                pagina.filhos[j + 1] = pagina.filhos[j];
            }
            // Insere a nova chave e o ponteiro
            pagina.chaves[i] = chave;
            pagina.ponteirosChave[i] = ponteiroChave;
            pagina.filhos[i + 1] = paginaAux;
            pagina.numElementos++;
            arq.seek(paginaAtual);
            arq.write(pagina.getBytes());
            cresceu = false;  // A árvore não cresce mais após a inserção certa
            return true;
        }
    
        // Divide a página atual porque ela tá cheia
        Pagina novaPagina = new Pagina(ordem);
        int meio = (pagina.numElementos + 1) / 2;
        boolean novoElementoNaPaginaNova = i >= meio;
    
        // Transfere metade das chaves pra nova página
        for (int j = meio; j < pagina.numElementos; j++) {
            novaPagina.chaves[j - meio] = pagina.chaves[j];
            novaPagina.ponteirosChave[j - meio] = pagina.ponteirosChave[j];
            novaPagina.filhos[j - meio + 1] = pagina.filhos[j + 1];
        }
        novaPagina.numElementos = pagina.numElementos - meio;
        pagina.numElementos = meio;
        novaPagina.filhos[0] = pagina.filhos[meio];
    
        // Insere o novo elemento na página adequada após a divisão
        if (novoElementoNaPaginaNova) {
            // se o novo elemento pertencer à nova página
            for (int j = novaPagina.numElementos; j > i - meio; j--) {
                novaPagina.chaves[j] = novaPagina.chaves[j - 1];
                novaPagina.ponteirosChave[j] = novaPagina.ponteirosChave[j - 1];
                novaPagina.filhos[j + 1] = novaPagina.filhos[j];
            }
            novaPagina.chaves[i - meio] = chave;
            novaPagina.ponteirosChave[i - meio] = ponteiroChave;
            novaPagina.filhos[i - meio + 1] = paginaAux;
            novaPagina.numElementos++;
        } else {
            // se o novo elemento pertencer à página original
            for (int j = meio; j > i; j--) {
                pagina.chaves[j] = pagina.chaves[j - 1];
                pagina.ponteirosChave[j] = pagina.ponteirosChave[j - 1];
                pagina.filhos[j + 1] = pagina.filhos[j];
            }
            pagina.chaves[i] = chave;
            pagina.ponteirosChave[i] = ponteiroChave;
            pagina.filhos[i + 1] = paginaAux;
            pagina.numElementos++;
        }
    
        // Atualiza os ponteiros para garantir que a árvore permaneça corretamente ligada
        chave = novaPagina.chaves[0];
        ponteiroChave = novaPagina.ponteirosChave[0];
        paginaAux = arq.length();
        arq.seek(paginaAux);
        arq.write(novaPagina.getBytes());
        arq.seek(paginaAtual);
        arq.write(pagina.getBytes());
        cresceu = true;  // Indica que a árvore pode crescer mais se necessário
        return true;
    }    

    public boolean update(int id, long newPointer) throws IOException {
        arq.seek(0);
        long raiz = arq.readLong();
        if (raiz == -1) {
            return false;
        }
        return updateRec(id, newPointer, raiz);
    }
    
    private boolean updateRec(int id, long newPointer, long pagina) throws IOException {
        if (pagina == -1) {
            return false; // Chegou a uma folha sem encontrar o ID
        }
    
        // Lê a página atual
        arq.seek(pagina);
        Pagina pa = new Pagina(ordem);
        byte[] byteA = new byte[tamPag]; // Tamanho da página
        arq.read(byteA);
        pa.setBytes(byteA);
    
        // busca pra encontrar a posição do ID
        int i = 0;
        while (i < pa.numElementos && id > pa.chaves[i]) {
            i++;
        }
    
        // Verifica se encontrou o ID na página atual
        if (i < pa.numElementos && id == pa.chaves[i]) {
            pa.ponteirosChave[i] = newPointer; // Atualiza o ponteiro da chave
            arq.seek(pagina);
            arq.write(pa.getBytes()); // Escreve a página modificada de volta no arquivo
            return true; // Retorna true na atualização
        }
    
        // Continua a busca na sub-árvore apropriada se não for folha
        return updateRec(id, newPointer, pa.filhos[i]);
    }    
    
    public Long read(int id) throws IOException {
        arq.seek(0);
        long raiz = arq.readLong();
        if (raiz == -1) {
            return null;
        }
        return readRec(id, raiz);
    }
    
    public Long readRec(int id, long pagina) throws IOException {
        if (pagina == -1) {
            return null; // Chegou a uma folha sem encontrar o ID
        }
    
        // Lê a página atual
        arq.seek(pagina);
        Pagina pagina2 = new Pagina(ordem);
        byte[] byteA = new byte[tamPag]; // tamanho da pagina
        arq.read(byteA);
        pagina2.setBytes(byteA);
    
        // Busca linear
        int i = 0;
        while (i < pagina2.numElementos && id > pagina2.chaves[i]) {
            i++;
        }
    
        // Verifica se encontrou o ID na página atual
        if (i < pagina2.numElementos && id == pagina2.chaves[i]) {
            return pagina2.ponteirosChave[i]; // Retorna o conteúdo (long) associado ao ID
        }
    
        // Continua a busca na sub-árvore apropriada
        return readRec(id, pagina2.filhos[i]);
    }

    public boolean delete(int id) throws IOException {
        // buscamos a raiz da árvore no arquivo.
        arq.seek(0);
        long raiz = arq.readLong();
        if (raiz == -1) {
            return false; 
        }
    
        // Sinaliza se diminuiu a altura.
        diminuiu = false;
        // Chamada recursiva para deletar o elemento.
        boolean excluido = deleteRec(id, raiz, raiz);
    
        // Se o elemento foi excluído e a árvore está desbalanceada.
        if (excluido && diminuiu) {
            arq.seek(raiz);
            Pagina paginaRaiz = new Pagina(ordem);
            byte[] byteA = new byte[tamPag];
            arq.read(byteA);
            paginaRaiz.setBytes(byteA);
    
            // Se a página raiz está vazia mas tem filhos, reduzimos a altura da árvore.
            if (paginaRaiz.numElementos == 0 && paginaRaiz.filhos[0] != -1) {
                arq.seek(0);
                arq.writeLong(paginaRaiz.filhos[0]);  // Novo nó raiz.
            }
        }
    
        return excluido;
    }
    
    private boolean deleteRec(int id, long pagina, long raiz) throws IOException {
        // Caso base da recursão: chegou numa página inexistente.
        if (pagina == -1) {
            diminuiu = false;
            return false;  // ID não encontrado.
        }
    
        // Lê a página atual para encontrar o ID.
        arq.seek(pagina);
        Pagina paginaAtual = new Pagina(ordem);
        byte[] byteA = new byte[tamPag];  // tamanho da pagina
        arq.read(byteA);
        paginaAtual.setBytes(byteA);
    
        // Busca pra encontrar o ID ou decidir para qual filho ir.
        int i = 0;
        while (i < paginaAtual.numElementos && id > paginaAtual.chaves[i]) {
            i++;
        }
    
        boolean excluido = false;
        // Se encontrou o ID em uma folha.
        if (i < paginaAtual.numElementos && paginaAtual.filhos[0] == -1 && id == paginaAtual.chaves[i]) {
            // Remove o ID deslocando todos os elementos subsequentes para a esquerda.
            for (int j = i; j < paginaAtual.numElementos - 1; j++) {
                paginaAtual.chaves[j] = paginaAtual.chaves[j + 1];
                paginaAtual.ponteirosChave[j] = paginaAtual.ponteirosChave[j + 1];
            }
            paginaAtual.numElementos--;
            arq.seek(pagina);
            arq.write(paginaAtual.getBytes());
            diminuiu = paginaAtual.numElementos < (ordem-1) / 2;
            excluido = true;
        } else if (paginaAtual.filhos[0] != -1) {  // Continua a busca nos filhos se não for folha.
            if (i < paginaAtual.numElementos && id == paginaAtual.chaves[i]) i++;
            excluido = deleteRec(id, paginaAtual.filhos[i], raiz);
        }
    
        // se não teve exclusão ou não é necessário reestruturar, retorna.
        if (!diminuiu || !excluido) {
            return excluido;
        }
    
        // reestrutura a árvore se necessário após a exclusão.
        reestruturar(pagina, raiz);
    
        return excluido;
    }
    
    private void reestruturar(long pagina, long raiz) throws IOException {
        // Se a página for a raiz e estiver desbalanceada, o tratamento é especial.
        if (pagina == raiz) {
            return; // Ajuste da raiz é tratado em outro lugar.
        }
    
        // Prepara para possível empréstimo ou fusão.
        arq.seek(pagina);
        Pagina paginaAtual = new Pagina(ordem);
        byte[] byteA = new byte[tamPag];
        arq.read(byteA);
        paginaAtual.setBytes(byteA);
    
        long paginaPai = encontrarPaiPagina(pagina, raiz);
        arq.seek(paginaPai);
        Pagina pai = new Pagina(ordem);
        arq.read(byteA);
        pai.setBytes(byteA);
    
        int idx = 0;
        // Encontra a posição da página entre os filhos do pai.
        while (idx <= pai.numElementos && pai.filhos[idx] != pagina) idx++;
    
        boolean emprestado = false;
        // Tenta empréstimo com o irmão esquerdo.
        if (idx > 0) {
            emprestado = emprestIrmaoEsquerdo(pagina, pai.filhos[idx - 1], paginaPai, idx);
        }
        // Tenta empréstimo com o irmão direito se o esquerdo falhar.
        if (!emprestado && idx < pai.numElementos) {
            emprestado = emprestIrmaoDireito(pagina, pai.filhos[idx + 1], paginaPai, idx);
        }
    
        // Se o empréstimo falhar, realiza a fusão.
        if (!emprestado) {
            if (idx > 0) {
                fundirIrmas(pai.filhos[idx - 1], pagina, paginaPai, idx - 1);
            } else {
                fundirIrmas(pagina, pai.filhos[idx + 1], paginaPai, idx);
            }
        }
    }    
    
    private boolean emprestIrmaoEsquerdo(long pagina, long irmaoEsquerdo, long paginaPai, int idx) throws IOException {
        // Acessa a página do irmão esquerdo para verificar a possibilidade de empréstimo.
        arq.seek(irmaoEsquerdo);
        Pagina paginaEsquerda = new Pagina(ordem);
        byte[] byteA = new byte[tamPag];
        arq.read(byteA);
        paginaEsquerda.setBytes(byteA);
    
        // Verifica se o irmão esquerdo tem chaves suficientes para emprestar.
        if (paginaEsquerda.numElementos > (ordem-1) / 2) {
            // Carrega a página atual que precisa de chaves.
            arq.seek(pagina);
            Pagina paginaAtual = new Pagina(ordem);
            byteA = new byte[tamPag];
            arq.read(byteA);
            paginaAtual.setBytes(byteA);
    
            // Desloca os elementos existentes para abrir espaço para o novo elemento.
            System.arraycopy(paginaAtual.chaves, 0, paginaAtual.chaves, 1, paginaAtual.numElementos);
            System.arraycopy(paginaAtual.ponteirosChave, 0, paginaAtual.ponteirosChave, 1, paginaAtual.numElementos);
            System.arraycopy(paginaAtual.filhos, 0, paginaAtual.filhos, 1, paginaAtual.numElementos + 1);
            paginaAtual.numElementos++;
    
            // Insere o elemento emprestado na primeira posição.
            paginaAtual.chaves[0] = paginaEsquerda.chaves[paginaEsquerda.numElementos - 1];
            paginaAtual.ponteirosChave[0] = paginaEsquerda.ponteirosChave[paginaEsquerda.numElementos - 1];
            paginaAtual.filhos[0] = paginaEsquerda.filhos[paginaEsquerda.numElementos];
    
            // Atualiza a página do irmão esquerdo após o empréstimo.
            paginaEsquerda.numElementos--;
    
            // Atualiza o pai para refletir a nova chave que subiu.
            arq.seek(paginaPai);
            Pagina pai = new Pagina(ordem);
            byteA = new byte[tamPag];
            arq.read(byteA);
            pai.setBytes(byteA);
            pai.chaves[idx - 1] = paginaAtual.chaves[0];
    
            // Salva todas as alterações no arquivo.
            arq.seek(pagina);
            arq.write(paginaAtual.getBytes());
            arq.seek(irmaoEsquerdo);
            arq.write(paginaEsquerda.getBytes());
            arq.seek(paginaPai);
            arq.write(pai.getBytes());
    
            return true;
        }
    
        return false;
    }
    
    private boolean emprestIrmaoDireito(long pagina, long irmaoDireito, long paginaPai, int idx) throws IOException {
        // Acessa a página do irmão direito para verificar a possibilidade de empréstimo.
        arq.seek(irmaoDireito);
        Pagina paginaDireita = new Pagina(ordem);
        byte[] byteA = new byte[tamPag];
        arq.read(byteA);
        paginaDireita.setBytes(byteA);
    
        // Verifica se o irmão direito tem chaves suficientes para emprestar.
        if (paginaDireita.numElementos > (ordem-1) / 2) {
            // Carrega a página atual que precisa de chaves.
            arq.seek(pagina);
            Pagina paginaAtual = new Pagina(ordem);
            byteA = new byte[tamPag];
            arq.read(byteA);
            paginaAtual.setBytes(byteA);
    
            // Adiciona o primeiro elemento do irmão direito na última posição da página atual.
            paginaAtual.chaves[paginaAtual.numElementos] = paginaDireita.chaves[0];
            paginaAtual.ponteirosChave[paginaAtual.numElementos] = paginaDireita.ponteirosChave[0];
            paginaAtual.filhos[paginaAtual.numElementos + 1] = paginaDireita.filhos[0];
            paginaAtual.numElementos++;
    
            // Remove o elemento emprestado da página do irmão direito.
            System.arraycopy(paginaDireita.chaves, 1, paginaDireita.chaves, 0, paginaDireita.numElementos - 1);
            System.arraycopy(paginaDireita.ponteirosChave, 1, paginaDireita.ponteirosChave, 0, paginaDireita.numElementos - 1);
            System.arraycopy(paginaDireita.filhos, 1, paginaDireita.filhos, 0, paginaDireita.numElementos);
    
            paginaDireita.numElementos--;
    
            // Atualiza o pai para refletir a nova chave que subiu.
            arq.seek(paginaPai);
            Pagina pai = new Pagina(ordem);
            byteA = new byte[tamPag];
            arq.read(byteA);
            pai.setBytes(byteA);
            pai.chaves[idx] = paginaDireita.chaves[0];
    
            // Salva todas as alterações no arquivo.
            arq.seek(pagina);
            arq.write(paginaAtual.getBytes());
            arq.seek(irmaoDireito);
            arq.write(paginaDireita.getBytes());
            arq.seek(paginaPai);
            arq.write(pai.getBytes());
    
            return true;
        }
    
        return false;
    }
    
    private void fundirIrmas(long paginaEsquerda, long paginaDireita, long paginaPai, int idxPai) throws IOException {
        // Carrega as páginas irmãs que serão fundidas
        arq.seek(paginaEsquerda);
        Pagina esquerda = new Pagina(ordem);
        byte[] byteA = new byte[tamPag];
        arq.read(byteA);
        esquerda.setBytes(byteA);
    
        arq.seek(paginaDireita);
        Pagina direita = new Pagina(ordem);
        byteA = new byte[tamPag];
        arq.read(byteA);
        direita.setBytes(byteA);
    
        // Funde as chaves, ponteiros e filhos da página direita na página esquerda.
        System.arraycopy(direita.chaves, 0, esquerda.chaves, esquerda.numElementos, direita.numElementos);
        System.arraycopy(direita.ponteirosChave, 0, esquerda.ponteirosChave, esquerda.numElementos, direita.numElementos);
        System.arraycopy(direita.filhos, 0, esquerda.filhos, esquerda.numElementos + 1, direita.numElementos + 1);
        esquerda.numElementos += direita.numElementos;
    
        // Atualiza a página pai pra remover a referência à página direita
        arq.seek(paginaPai);
        Pagina pai = new Pagina(ordem);
        byteA = new byte[tamPag];
        arq.read(byteA);
        pai.setBytes(byteA);
    
        // Remove a referência à página direita no pai.
        for (int i = idxPai; i < pai.numElementos - 1; i++) {
            pai.chaves[i] = pai.chaves[i + 1];
            pai.ponteirosChave[i] = pai.ponteirosChave[i + 1];
            pai.filhos[i + 1] = pai.filhos[i + 2];
        }
        pai.numElementos--;
    
        // Salva as alterações nas páginas envolvidas no arquivo.
        arq.seek(paginaEsquerda);
        arq.write(esquerda.getBytes());
        arq.seek(paginaPai);
        arq.write(pai.getBytes());
    }
    
    private long encontrarPaiPagina(long child, long current) throws IOException {
        // Procura o pai da página especificada, começando da raiz ou página fornecida.
        arq.seek(current);
        Pagina paginaAtual = new Pagina(ordem);
        byte[] byteA = new byte[tamPag];
        arq.read(byteA);
        paginaAtual.setBytes(byteA);
    
        // Percorre os filhos da página atual para encontrar a página filho especificada.
        for (int i = 0; i <= paginaAtual.numElementos; i++) {
            if (paginaAtual.filhos[i] == child) {
                return current;  // Retorna o pai se encontrado.
            }
            if (paginaAtual.filhos[i] != -1) {
                long found = encontrarPaiPagina(child, paginaAtual.filhos[i]);
                if (found != -1) return found;  // Retorna o pai se encontrado em subárvores.
            }
        }
    
        return -1;  // Retorna -1 se o pai não for encontrado.
    }    
    

    public void imprimir() throws IOException {
        long rootAddr;
        arq.seek(0);
        rootAddr = arq.readLong();
        if (rootAddr != -1) {
            imprimirRec(rootAddr);
        } else {
            System.out.println("A árvore está vazia.");
        }
        System.out.println();  // Adiciona uma linha final para separação após a impressão completa.
    }
    
    private void imprimirRec(long nodeAddr) throws IOException {
        if (nodeAddr == -1) {
            return; // Encerra a recursão se um ponteiro nulo for encontrado, indicando o fim de um caminho.
        }
    
        // Acessa a página no arquivo para ler seus dados.
        arq.seek(nodeAddr);
        Pagina currentNode = new Pagina(ordem);
        byte[] databyteA = new byte[tamPag];
        arq.read(databyteA);
        currentNode.setBytes(databyteA);
    
        // Prepara o formato de saída para o endereço e conteúdo da página.
        String formattedAddress = String.format("Página %04d: ", nodeAddr);
        System.out.print(formattedAddress + currentNode.numElementos + " chaves: ");
    
        for (int i = 0; i < currentNode.numElementos; i++) {
            System.out.print(String.format("|%04d| %d -> %d ", currentNode.filhos[i], currentNode.chaves[i], currentNode.ponteirosChave[i]));
        }
        
        // Adiciona uma quebra de linha após imprimir os dados da página.
        System.out.println();
    
        // Processa recursivamente cada filho da página atual, se não for uma folha.
        if (currentNode.filhos[0] != -1) {
            for (int i = 0; i <= currentNode.numElementos; i++) {
                imprimirRec(currentNode.filhos[i]);
            }
        }
    }    
    
    public static void main(String[] args) {
        try {
            // Cria a árvore com ordem 3 (pode ter até 3 filhos)
            ArvoreBPlus arvore = new ArvoreBPlus(8, "arvore.db");

            // Insere alguns registros na árvore
            System.out.println("Inserindo registros na árvore:");
            arvore.create(1,100); //testes
            arvore.create(2,100);
            arvore.create(3,100);
            arvore.create(4,100);
            arvore.create(5,100);
            arvore.create(6,100);
            arvore.create(7,100);
            arvore.create(8,100);
            arvore.create(9,100);
            arvore.create(10,100);
            arvore.create(11,100);
            arvore.create(12,100);
            arvore.create(13,100);
            arvore.create(14,100);
            arvore.create(15,100);

            arvore.update(2,200);
            arvore.imprimir();

        }catch (IOException e) {
            System.err.println("Erro ao manipular a árvore B+.");
            e.printStackTrace();
        }
    }
}