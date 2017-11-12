import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 * Table for placing data items in
 * an array
 * @author Kyle
 */
public class SymbolTable {

    private Symbol[] arr; // the array of items
    private int size;	//The size of the array
    private int maxSearch;
    private boolean returnHasBeenCalled;
    private String returnType;
    private Token functionID;
    private ArrayList<Symbol> paramsTable;

    /**
     * Constructor for the TokenTable
     * @param siz - the size of the table
     */
    public SymbolTable(int siz){
        returnHasBeenCalled =false;
        size = siz;
        arr = new Symbol[size];
        maxSearch = 1;
        paramsTable = new ArrayList<Symbol>();
        /*
        Files.lines(Paths.get(filename)).forEach(line -> {
            String temp[] = line.split(" ");
            insert(new Symbol(temp[0],temp[1]));
        });*/
    }//end HashTable


    public String getReturnType(){
        return returnType;
    }

    public Token getFunctionID(){
        return functionID;
    }


    public void fillFunctionInfo(String returnType,Token functionID){
        this.returnType = returnType;
        this.functionID = functionID;
    }

    public ArrayList<Symbol> getParamsTable(){
        return paramsTable;
    }

    public Symbol find(String key){
        int walker;//walker to walk on array
        int count = 1;//counter for probing
        int  newHash;
        boolean found = false;
        int hash = hash(key);
        if(arr[hash] != null && arr[hash].toString().equals(key)){
            return arr[hash];
        }
        while(!found){
            walker = hash;
            newHash = quadP(count++);
            for(int x=newHash;x>0;x--){
                if(++walker == arr.length){
                    walker = 0;
                }
            }
            if(arr[walker] != null && arr[walker].toString().equals(key)){
                return arr[walker];
            }//if the array item is null or
            if(count>maxSearch){
                break;
            }


        }
        return null;
    }

    /**
     * Hashes the given string
     * @param s - the string to hash
     * @return - the value of the hashed string
     */
    public int hash(String s){
        int hash = 0;
        char[] arr = s.toCharArray();
        hash=arr[0];
        for(int x=1;x<arr.length;x++){
            hash=(hash*26+arr[x])%size;
        }//hash the string
        hash%=size;//Just in case its one character(will not affect if more)
        return hash;
    }//end hash

    public boolean hasReturnBeenCalled(){
        return returnHasBeenCalled;
    }

    public void returnHasbeenCalled(){
        returnHasBeenCalled = true;
    }

    /**
     * Insertion method for hash table
     * @param item - the item to be inserted
     */
    public void insert(Symbol item){
        //Hash
        String s = item.toString();
        int hash = hash(s);

        //insert
        if(arr[hash] == null){
            arr[hash] = item;
        }else{//if the index is null or there is a deleted item
            probeInsert(hash,item);
        }//else probe until valid insertion
    }//end insert

    /**
     * probeInsertion
     * @param hash - the hash value of the string
     * @param item - the item to be inserted
     */
    public void probeInsert(int hash,Symbol item){
        boolean isFound = false;//boolean to know when found
        int walker = hash;//walker to walk on array
        int count = 1;//counter for probing
        int  newHash = hash;//New hash value

        while(!isFound){
            walker = hash;
            newHash = quadP(count++);
            for(int x=newHash;x>0;x--){
                if(++walker == arr.length){
                    walker = 0;
                }
            }
            if(arr[walker]== null){
                arr[walker] = item;
                isFound = true;
            }//if the array item is null or
        }//while the item position isn't found
        if(count>maxSearch){
            maxSearch = count;
        }
    }//end probeInsert

    //Used if using quadratic probing
    public int quadP(int x){
        return x*x;
    }//end quadP

    public void display(){
        for (Symbol symbol : arr) {
            if(symbol != null)
                System.out.println(symbol.toString());
        }
    }


}//end HashTable