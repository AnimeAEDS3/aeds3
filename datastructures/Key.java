package datastructures;

import java.io.Serializable;

public class Key implements Serializable {
    private int id;
    private long address;

    public Key() {// inicializa a chave com valores inválidos
        id = -1; 
        address = -1;
    }

    public Key(int id, long address){ // inicializa a chave com os valores fornecidos
        this.id = id;
        this.address = address;
    }

    public Key(long address, int id){ // inicializa a chave com os valores fornecidos
        this.id = id;
        this.address = address;
    }

    public String toString(){
        return "Id = " + this.id + " \nEndereço = " + this.address; // retorna os dados da chave
    }

    public int getId(){
        return this.id; // retorna o id da chave
    }

    public long getAddress(){
        return this.address; // retorna o endereço da chave
    }

    public void setAddress(long add){
        this.address = add;
    }

    public void setId(int id) {
        this.id = id;
    }
}
