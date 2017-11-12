import java.io.*;
import java.util.ArrayList;

public class Program {

    private BufferedReader reader;
    private ArrayList<Token> tokenList;
    private TokenBuilder tokenBuilder;
    private int commentBlockCount;
    private int lineCount;
    public Program(File f) throws IOException {
        lineCount = 0;
        commentBlockCount = 0;
        tokenBuilder = new TokenBuilder();
        reader = new BufferedReader(new FileReader(f));
        tokenList = new ArrayList<Token>();
        String line;
        while((line = reader.readLine()) != null){
            lineCount++;
            if(line.trim().length() != 0){
                parseLine(line);
            }
        }
        //printList();
        ArrayList<Token> temp = new ArrayList<Token>();
        for(int x=0;x<tokenList.size();x++){
            if(!(tokenList.get(x).getType() == TokenType.COM || tokenList.get(x).getType() == TokenType.NCOM)){
                temp.add(tokenList.get(x));
            }
        }//ensure no nested comments for parser
        tokenList = temp;
    }

    public int getLineCount(){
        return lineCount;
    }

    public boolean parseLine(String line){
        //System.out.println("INPUT: "+line);
        char chars[] = line.toCharArray();
        char c;
        for(int x = 0;x<chars.length;x++){
            c = chars[x];
            //System.out.println("Char: "+ c);
            if(commentBlockCount > 0 && chars.length > x+1){
                switch(c){
                    case '/':
                        if(chars[x+1] == '*'){
                            tokenList.add(new Token("/*",TokenType.NCOM));
                            commentBlockCount++;
                            ++x;
                        }
                        break;
                    case '*':
                        if(chars[x+1] == '/'){
                            tokenList.add(new Token("*/",TokenType.NCOM));
                            commentBlockCount--;
                            ++x;
                        }
                        break;
                }//comment blocks
                continue;
            }else if(!(commentBlockCount > 0)){
                //if there is no comment block, check for special tokens
                switch(c){
                    case '\t':
                    case ' ':
                        popToken();
                        continue;
                    case '!':
                        popToken();
                        if(x+1<chars.length){
                            if(chars[x+1] == '='){
                                tokenList.add(new Token("!=",TokenType.EXP));
                                //System.out.println("!=");
                                ++x;
                                continue;
                            }
                        }
                        tokenList.add(new Token("!", TokenType.ERROR));
                        //System.out.println("ERROR: !");
                        continue;
                    case '=':
                        popToken();
                        if(x+1<chars.length){
                            if(chars[x+1] == '='){
                                tokenList.add(new Token("==",TokenType.EXP));
                               // System.out.println("==");
                                ++x;
                            }else{
                                tokenList.add(new Token("=", TokenType.EXP));
                                //System.out.println("=");
                            }
                        }
                        continue;
                    case '>':
                        popToken();
                        if(x+1<chars.length){
                            if(chars[x+1] == '='){
                                tokenList.add(new Token(">=",TokenType.EXP));
                                // System.out.println(">=");
                                ++x;
                            }else{
                                tokenList.add(new Token(">",TokenType.EXP));
                                // System.out.println(">");
                            }

                        }
                        continue;
                    case '<':
                        popToken();
                        if(x+1<chars.length){
                            if(chars[x+1] == '='){
                                tokenList.add(new Token("<=",TokenType.EXP));
                                // System.out.println(">=");
                                ++x;
                            }else{
                                tokenList.add(new Token("<",TokenType.EXP));
                                // System.out.println(">");
                            }

                        }
                        continue;
                    case '(':
                    case ')':
                    case '{':
                    case '}':
                    case ',':
                    case '[':
                    case ';':
                    case ']':
                    case '*':
                        popToken();
                        tokenList.add(new Token(c,TokenType.EXP));
                        //System.out.println(c);
                        continue;
                    case '+':
                    case '-':
                        if(x+1>chars.length){
                            popToken();
                            tokenBuilder.addErrorChar(c);
                        }else{
                            if(tokenBuilder.getCurrentToken() != null){
                                if(tokenBuilder.getCurrentToken().getType() != TokenType.NUM){
                                    popToken();
                                    tokenList.add(new Token(c,TokenType.EXP));
                                }//if the current token is not a number currentlys
                            }else{
                                if(Character.isDigit(chars[x+1]) && chars[x-1] == 'E'){
                                    popToken();
                                    tokenBuilder.addErrorChar(c);
                                }else{
                                    popToken();
                                    tokenList.add(new Token(c,TokenType.EXP));
                                }
                            }//if the current token is null
                        }
                       // System.out.println(c);
                        continue;
                    case '/':
                        if(x+1 >= chars.length){
                            popToken();
                            tokenList.add(new Token("/",TokenType.EXP));
                          //  System.out.println("/");
                            continue;
                        }else{
                            switch(chars[x+1]){
                                case '/':
                                    popToken();
                                    tokenList.add(new Token("//",TokenType.COM));
                                    ++x;
                                    return true;
                                case '*':
                                    popToken();
                                    tokenList.add(new Token("/*",TokenType.NCOM));
                                    ++x;
                                    ++commentBlockCount;
                                    break;
                                default:
                                    popToken();
                                    tokenList.add(new Token("/",TokenType.EXP));
                                   // System.out.println('/');
                                    break;
                            }//check if comment block or just divide by sign
                            continue;
                        }
                }
            }//end special chars
            //Must be an id or a keyword
            if(c == ' ' && tokenBuilder.getCurrentToken() != null){
                tokenList.add(tokenBuilder.create());
            }else if(tokenBuilder.getCurrentToken() != null){
                //check if float
                if(tokenBuilder.getCurrentToken().getType() == TokenType.NUM){
                    boolean isNum = true,running = true;
                    do{
                        String temp = Character.toString(tokenBuilder.getCurrentToken().toString().toCharArray()[tokenBuilder.getCurrentToken().length()-1]);
                        c = chars[x];
                        switch(temp){
                            case "E":
                                if((temp+c).matches("E(-|\\+|\\d)")){
                                    tokenBuilder.getCurrentToken().addChar(c);
                                    ++x;
                                }else{
                                    isNum = false;
                                    --x;
                                }
                                break;
                            case "-":
                            case "+":
                            case ".":
                                if(Character.isDigit(c)){
                                    tokenBuilder.getCurrentToken().addChar(c);
                                    if(!temp.equals("+"))
                                        tokenBuilder.getCurrentToken().setType(TokenType.FLOAT);
                                    ++x;
                                }else{
                                    isNum = false;
                                    --x;
                                }
                                break;
                            default:
                                if((temp+c).matches("\\d+")){
                                    tokenBuilder.addChar(c);
                                    ++x;
                                }else{
                                    switch(c){
                                        case '.':
                                            if(tokenBuilder.getCurrentToken().toString().chars().filter(ch -> ch == '.').count() == 1){
                                                running = false;
                                                --x;
                                            }else{
                                                tokenBuilder.getCurrentToken().addChar(c);
                                                ++x;
                                            }
                                            break;
                                        case 'E':
                                            if(tokenBuilder.getCurrentToken().toString().chars().filter(ch -> ch == 'E').count() == 1){
                                                running = false;
                                                --x;
                                            }else{
                                                tokenBuilder.getCurrentToken().addChar(c);
                                                ++x;
                                            }
                                            break;
                                        default:
                                            running = false;
                                            --x;
                                            break;
                                    }
                                }

                        }
                    }while(running && isNum && (x < chars.length));
                    if(!isNum || !tokenBuilder.getCurrentToken().toString().matches("(\\d+)(\\.\\d+)?(E(-|\\+)?\\d+)?")){
                        tokenBuilder.getCurrentToken().setType(TokenType.ERROR);
                        popToken();
                    }else if(Character.isLetter(c)){
                        popToken();
                        tokenBuilder.addChar(c);
                        tokenBuilder.getCurrentToken().setType(TokenType.ERROR);
                        ++x;
                    }else{
                        popToken();
                    }
                    continue;
                }//test for float

                if(!tokenBuilder.getCurrentToken().checkType(c)){
                    popToken();
                    tokenBuilder.addErrorChar(c);
                }else {
                    tokenBuilder.addChar(c);
                }
            }else if( c != ' ' && tokenBuilder.getCurrentToken() == null && commentBlockCount == 0){
                    tokenBuilder.addChar(c);
            }
        }//end for
        if(tokenBuilder.getCurrentToken() != null && commentBlockCount == 0){
            popToken();
        }
        return true;
    }

    public boolean parseChar(char c) {
        return false;
    }

    public void popToken(){
        if(tokenBuilder.getCurrentToken() != null){
            //System.out.println(tokenBuilder.getCurrentToken().toString());
            tokenList.add(tokenBuilder.create());
        }
    }

    public ArrayList<Token> getTokenList(){
        return this.tokenList;
    }

    public void printList(){
        tokenList.forEach(token -> {
            System.out.println(token.toString());
        });
    }
}//Program
