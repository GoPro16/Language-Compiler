package Main;

import Structs.Token;
import Structs.TokenBuilder;
import Structs.TokenType;
import Structs.SymbolTable;

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
            }else{
                //if there is no comment block, check for special tokens
                switch(c){
                    case '(':
                        tokenList.add(tokenBuilder.create());
                        tokenList.add(new Token("(",TokenType.PARENS));
                        continue;
                    case ')':
                        tokenList.add(tokenBuilder.create());
                        tokenList.add(new Token(")",TokenType.PARENS));
                        continue;
                    case '{':
                        tokenList.add(tokenBuilder.create());
                        tokenList.add(new Token("{",TokenType.BRACKETS));
                        continue;
                    case '}':
                        tokenList.add(tokenBuilder.create());
                        tokenList.add(new Token("}",TokenType.BRACKETS));
                        continue;
                    case '/':
                        if(tokenBuilder.getCurrentToken() != null){
                            tokenList.add(tokenBuilder.create());
                        }
                        if(x+1 > chars.length){
                            tokenList.add(new Token("/",TokenType.KEYWORD));
                            continue;
                        }else{
                            switch(chars[++x]){
                                case '/':
                                    tokenList.add(new Token("//",TokenType.COM));
                                    return true;
                                case '*':
                                    tokenList.add(new Token("/*",TokenType.NCOM));
                                    ++commentBlockCount;
                                    break;
                                default:
                                    tokenList.add(new Token("/",TokenType.EXP));
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
                    if(!tokenBuilder.getCurrentToken().checkType(c)){
                        tokenList.add(tokenBuilder.create());
                        tokenBuilder.addChar(c);
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

    public void printList(){
        tokenList.forEach(token -> {
            System.out.println(token.toString());
        });
    }
}//Program
