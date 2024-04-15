import java.io.*;

public class Pagina {
    public int ordem;
    public int numElementos; 
    public int[] chaves;    
    public long[] ponteirosChave;  
    public long[] filhos;

    public Pagina(int ord) {
        numElementos = 0; 
        ordem = ord;
        chaves = new int[(ordem-1)];
        ponteirosChave = new long[(ordem-1)];
        filhos = new long[ord];
        
        for (int i = 0; i < (ordem-1); i++) {  
            chaves[i] = -1;
            ponteirosChave[i] = -1L;
            filhos[i] = -1L;
        }
        filhos[ord- 1] = -1L;
    }
    
    public byte[] getBytes() throws IOException {
        // Inicializa um fluxo de saída para construir um array de bytes.
        ByteArrayOutputStream ba = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(ba);
        
        // Escreve o número de elementos atualmente na página.
        out.writeInt(numElementos); 
        
        // Loop para escrever os dados de cada elemento: filho, chave e ponteiro da chave.
        for (int i = 0; i < numElementos; i++) { 
            out.writeLong(filhos[i]); // Escreve o ponteiro para o filho.
            out.writeInt(chaves[i]);  // Escreve a chave.
            out.writeLong(ponteirosChave[i]); // Escreve o ponteiro associado à chave.
        }
        // Escreve o ponteiro do último filho.
        out.writeLong(filhos[numElementos]); 
        
        // Cria um array de bytes para representar registros vazios.
        byte[] registroVazio = new byte[12]; // Considerando long (8 bytes) + int (4 bytes).
        // Preenche o espaço restante na página com registros vazios.
        for (int i = numElementos; i < (ordem-1); i++) {
            out.write(registroVazio);
            out.writeLong(filhos[i + 1]);
        }
        
        // Retorna o array de bytes que representa a página.
        return ba.toByteArray();
    }        

    public void setBytes(byte[] buffer) throws IOException {
        // Inicializa um fluxo de entrada para ler os dados de um array de bytes.
        ByteArrayInputStream ba = new ByteArrayInputStream(buffer);
        DataInputStream in = new DataInputStream(ba);
        
        // Lê o número de elementos presentes na página.
        numElementos = in.readInt(); 
        
        // Loop para ler os dados de cada elemento: filho, chave e ponteiro da chave.
        for (int i = 0; i < (ordem-1); i++) {
            filhos[i] = in.readLong(); // Lê o ponteiro para o filho.
            chaves[i] = in.readInt();  // Lê a chave.
            ponteirosChave[i] = in.readLong(); // Lê o ponteiro associado à chave.
        }
        // Lê o ponteiro do último filho.
        filhos[(ordem-1)] = in.readLong();
    }        
}
