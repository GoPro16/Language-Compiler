import java.lang.reflect.Array;
import java.util.ArrayList;

public class Symbol {

    private Token token;
    private String type;
    private boolean arr;
    private String arrSize;
    private boolean func;
    private boolean init;
    private ArrayList<Symbol> paramsTable;

    public Symbol(Token token,String type,boolean arr,String arrSize,boolean func,boolean init){
        this.type = type;
        this.token = token;
        this.arr = arr;
        this.arrSize = arrSize;
        this.func = func;
        this.init = init;
    }//end constructor

    public String getType(){
        return type;
    }

    public void setParamsTable(ArrayList<Symbol> paramsTable){
        this.paramsTable = paramsTable;
    }
    public ArrayList<Symbol> getParamsTable(){
        return paramsTable;
    }

    public boolean isArray(){
        return arr;
    }

    public boolean isFunction(){
        return func;
    }
    public String toString(){
        return token.toString();
    }
    public Token get(){
        return this.token;
    }

}
