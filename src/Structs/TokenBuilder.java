package Structs;

public class TokenBuilder {

    private Token token;
    private int depth;

    public TokenBuilder(){
        depth = 0;
    }

    public void addChar(char c){
        if(token == null) {
            token = new Token();
            Symbol temp = SymbolTable.find(Character.toString(c));
            if (temp != null) {
                token = new Token(Character.toString(c), temp.getType());
            } else {
                if (Character.isDigit(c)) {
                    token.setType(TokenType.NUM);
                } else {
                    token.setType(TokenType.ID);
                }
                token.addChar(c);
            }
        }else{
            Symbol temp = SymbolTable.find(token.toString()+c);
            if(temp != null){
                token.setType(temp.getType());
            }else{
                switch(token.getType()){
                    default:
                        ID:
                        if(Character.isDigit(c)){
                            create();
                            token = new Token(c,TokenType.ERROR);
                        }
                        break;

                }
            }
            token.addChar(c);
        }
    }

    public Token getCurrentToken(){
        return token;
    }


    public Token create(){
        Token t = token;
        System.out.println(token.display());
        token = null;
        return t;
    }
}
