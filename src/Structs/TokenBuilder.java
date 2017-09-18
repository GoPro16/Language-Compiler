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
                } else if(Character.toString(c).matches(".*[a-z].*")){
                    token.setType(TokenType.ID);
                }else{
                    token.setType(TokenType.ERROR);
                }
                token.addChar(c);
            }
        }else if(token.getType() == TokenType.ERROR) {
            Symbol temp = SymbolTable.find(Character.toString(c));
            if(temp != null) {
                create();
            }else {
                token.addChar(c);
            }
        }else{
                Symbol temp = SymbolTable.find(token.toString()+c);
                if(temp != null){
                    token.setType(temp.getType());
                }else{
                    switch(token.getType()){
                            case ID:
                            if(Character.isDigit(c) || !(token.toString()+c).matches(".*[a-z].*")){
                                create();
                                token = new Token(c,TokenType.ERROR);
                            }
                            break;
                        case KEYWORD:
                            token.setType(TokenType.ID);
                            break;
                           case NUM:
                            if(!((token.toString()+c).matches("(\\d+)(\\.\\d+)?(E(-|\\+)?\\d+)?"))){
                                System.out.println("NOT A NUMBER:"+c);
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

    public void addErrorChar(char c){
        if(token == null){
            token = new Token(c,TokenType.ERROR);
        }else{
            token.addChar(c);
        }
    }


    public Token create(){
        Token t = token;
        System.out.println(token.display());
        token = null;
        return t;
    }
}
