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

    public Program(File f) throws IOException {
        tokenBuilder = new TokenBuilder();
        reader = new BufferedReader(new FileReader(f));
        tokenList = new ArrayList();
        String line;
        while((line = reader.readLine()) != null){
            parseLine(line);
        }
        //printList();
    }

    public void parseLine(String line){
        System.out.println("INPUT: "+line);
        char chars[] = line.toCharArray();
        char c;
        for(int x = 0;x<chars.length;x++){
            c = chars[x];

            if(tokenList.get(tokenList.size()).getType() == TokenType.NCOM){

            }
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
                                break;
                            case '*':
                                tokenList.add(new Token("/*",TokenType.NCOM));
                                comms:
                                while(true) {
                                    switch(chars[++x]){
                                        case '*':
                                            if(chars[++x] == '/'){
                                                break;
                                            }else{

                                            }
                                    }
                                }
                                continue;
                        }
                    }
            }
            if(c == ' ' && tokenBuilder.getCurrentToken() != null){
                tokenList.add(tokenBuilder.create());
            }else {
                if(tokenBuilder.getCurrentToken() != null) {
                    if(!tokenBuilder.getCurrentToken().checkType(c)){
                        tokenList.add(tokenBuilder.create());
                        tokenBuilder.addChar(c);
                    }else {
                        tokenBuilder.addChar(c);
                    }
                }else{
                    tokenBuilder.addChar(c);
                }
            }
        }//end for
        if(tokenBuilder.getCurrentToken() != null){
            tokenList.add(tokenBuilder.create());
        }
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
