package Structs;

import java.util.ArrayList;

public class SymbolTableIterator {

    private int length;
    private int current;
    private int size;

    private ArrayList<SymbolTable> tables;

    public SymbolTableIterator(int size){
        System.out.println(size);
        length=0;
        this.size = size;
        tables = new ArrayList<SymbolTable>();
        tables.add(new SymbolTable(this.size));
    }

    public void increaseDepth(){
        tables.add(new SymbolTable(this.size));
        length++;
    }

    public int getDepth(){
        return length;
    }


    public void findOrInsert(Token token,boolean initialzed){
        if(initialzed){
            current = length;
            boolean isFound =false;
            do{
                //try to find the symbol somewhere in the table
                current--;
            }while(current <= 0);
        }else{//if the token is claimed to already initialized
            //Try to insert it
            if(tables.get(length).find(token.toString()) != null){
                //Then throw a token exists error
                semanticReject(1);
            }else{
                tables.get(length).insert(new Symbol(token));
            }
        }//if the token is claimed to not initialized
        current = length;
        //This is where we find or insert the current symbol. Error reporting is done here
        if(tables.get(current).find(token.toString()) != null){
            //The token exists make sure that it is being used and not declared else error
        }else{

        }
    }

    public void decreaseDepth(){
        if(length > 0){
            System.out.println(length);
            tables.remove(length--);
        }else//if we aren't at the root level
            System.out.println("Semantics Depth error!");
    }

    public void semanticReject(int status){
        switch(status){
            default:
                System.out.println("Semantics Rejected this");
                break;
        }

        System.exit(0);
    }


}
