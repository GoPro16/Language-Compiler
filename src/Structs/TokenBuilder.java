package Structs;

public class TokenBuilder {

    private Token token;
    private int depth;

    public TokenBuilder(){
        depth = 0;
    }

    public void addChar(char c){
        if(token == null){
            token = new Token();
            if(Character.isDigit(c)){
                token.setType(TokenType.NUM);
            }else{
                token.setType(TokenType.ID);
            }
        }
        token.addChar(c);
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
