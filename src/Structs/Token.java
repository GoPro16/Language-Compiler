package Structs;

public class Token {

    private TokenType type;
    private String value;

    public Token(){value="";}

    public Token(char value, TokenType type){
        this.value = Character.toString(value);
        this.type = type;
    }
    public Token(String value,TokenType type){
        this.value = value;
        this.type = type;
    }

    public Token(String value, String type){
        this.value = value;
        switch(type){
            case "int":
            case "INT":
            case "num":
            case "NUM":
                this.type = TokenType.NUM;
                break;
            case "float":
                this.type = TokenType.FLOAT;
                break;
            case "floatarr":
                this.type = TokenType.FLOATARR;
                break;
            case "intarr":
                this.type = TokenType.INTARR;
                break;
            case "KEYWORD":
                this.type = TokenType.KEYWORD;
                break;
            case "ID":
                this.type = TokenType.ID;
                break;
            default:
                this.type = TokenType.ERROR;
                break;
        }
    }


    public void setType(TokenType type){
        this.type = type;
    }

    public boolean checkType(char c){
        switch(type){
            case NUM:
                return (value+c).matches("(\\d+)(\\.\\d+)?(E(-|\\+)?\\d+)?");
            case ID:
                return (value+c).chars().allMatch(Character::isLetter);
            default:
                return true;
        }
    }

    public int length(){
        return value.length();
    }
    public TokenType getType(){
        return type;
    }

    public String display(){
        switch(type){
            case NUM:
                return "NUM: "+value;
            case FLOAT:
                return "FLOAT: "+value;
            case ID:
                return "ID: "+value;
            case COM:
            case NCOM:
                return null;
            case KEYWORD:
                return "keyword: "+value;
            case ERROR:
                return "ERROR:"+value;
            default:
                return value;
        }
    }


    public void addChar(char c){
        value+=c;
    }

    public String toString(){
        return value;
    }

}
