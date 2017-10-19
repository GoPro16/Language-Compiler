public class Symbol {

    private String name;
    private TokenType type;

    public Symbol(String name,String type){
        this.name = name;
        switch(type){
            case "keyword":
                this.type=TokenType.KEYWORD;
                break;
            case "LD":
            case "RD":
                this.type = TokenType.PARENS;
                break;
            case "LB":
            case "RB":
                this.type = TokenType.BRACES;
                break;
            case "LBB":
            case "RBB":
                this.type = TokenType.BRACKETS;
                break;
            case "/*":
            case "*/":
                this.type = TokenType.NCOM;
                break;
            case "//":
                this.type = TokenType.COM;
                break;
            default:
                this.type= TokenType.EXP;
                break;
        }
    }//end constructor

    public String toString(){
        return name;
    }
    public TokenType getType(){
        return type;
    }

}
