package Structs;

public class Symbol {

    private Token token;
    private boolean arr;
    private boolean func;
    public Symbol(Token token){
        this.token = token;
    }//end constructor

    public String toString(){
        return token.toString();
    }
    public Token get(){
        return this.token;
    }

}
