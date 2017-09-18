package Main;

import Structs.*;

import java.io.*;
import java.util.ArrayList;

public class Program {

    private BufferedReader reader;
    private ArrayList<Token> tokenList;
    private TokenBuilder tokenBuilder;
    private int commentBlockCount;

    public Program(File f) throws IOException {
        commentBlockCount = 0;
        tokenBuilder = new TokenBuilder();
        reader = new BufferedReader(new FileReader(f));
        tokenList = new ArrayList();
        String line;
        while((line = reader.readLine()) != null){
            if(line.trim().length() != 0){
                parseLine(line);
            }
        }
        //printList();
    }

    public boolean parseLine(String line){
        System.out.println("INPUT: "+line);
        char chars[] = line.toCharArray();
        char c;
        for(int x = 0;x<chars.length;x++){
            c = chars[x];
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
                    case '(':
                    case ')':
                    case '{':
                    case '}':
                    case ',':
                    case '[':
                    case ';':
                    case ']':
                        popToken();
                        tokenList.add(new Token(c,TokenType.EXP));
                        System.out.println(c);
                        continue;
                    case '*':
                        popToken();
                        if(x+1>chars.length){
                            System.out.println('*');
                            tokenList.add(new Token(c,TokenType.EXP));
                        }else{
                            if(chars[x+1] == '/'){
                                tokenList.add(new Token(Character.toString(c)+chars[x+1],TokenType.ERROR));
                                System.out.println(tokenList.get(tokenList.size()-1).display());
                                ++x;
                            }else{
                                tokenList.add(new Token(c,TokenType.EXP));
                                System.out.println('*');
                            }
                        }
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
                                }else if(chars[x+1] == ' '){
                                    tokenList.add(new Token(c,TokenType.EXP));
                                }else{
                                    tokenBuilder.addErrorChar(c);
                                }//if the next token is a digit
                            }//if the current token is null
                        }
                        System.out.println(c);
                        continue;
                    case '/':
                        if(x+1 > chars.length){
                            popToken();
                            tokenList.add(new Token("/",TokenType.KEYWORD));
                            System.out.println('/');
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
                                    System.out.println('/');
                                    break;
                            }//check if comment block or just divide by sign
                            continue;
                        }//
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
                                }
                                break;
                            case "-":
                            case "+":
                            case ".":
                                if(Character.isDigit(c)){
                                    tokenBuilder.getCurrentToken().addChar(c);
                                    ++x;
                                }else{
                                    isNum = false;
                                }
                                break;
                            default:
                                if((temp+c).matches("\\d+")){
                                    tokenBuilder.getCurrentToken().addChar(c);
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
                    if(!isNum){
                        tokenBuilder.getCurrentToken().setType(TokenType.ERROR);
                        tokenBuilder.create();
                    }else{
                        tokenBuilder.create();
                    }
                    continue;
                }//test for float

                if(!tokenBuilder.getCurrentToken().checkType(c)){
                    tokenList.add(tokenBuilder.create());
                    tokenBuilder.addErrorChar(c);
                }else {
                    tokenBuilder.addChar(c);
                }
            }else if( c != ' ' && tokenBuilder.getCurrentToken() == null && commentBlockCount == 0){
                    tokenBuilder.addChar(c);
            }
        }//end for
        if(tokenBuilder.getCurrentToken() != null && commentBlockCount == 0){
            tokenList.add(tokenBuilder.create());
        }
        return true;
    }

    public boolean parseChar(char c) {
        return false;
    }

    public void popToken(){
        if(tokenBuilder.getCurrentToken() != null){
            tokenList.add(tokenBuilder.create());
        }
    }

    public void printList(){
        tokenList.forEach(token -> {
            System.out.println(token.toString());
        });
    }
}//Program
