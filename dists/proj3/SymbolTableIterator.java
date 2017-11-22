import java.util.ArrayList;

public class SymbolTableIterator {

    private int length;
    private int current;
    private int size;
    private boolean mainHasBeenDeclared;
    private ArrayList<SymbolTable> tables;

    public SymbolTableIterator(int size){
        //System.out.println(size);
        length=0;
        this.size = size;
        tables = new ArrayList<SymbolTable>();
        tables.add(new SymbolTable(this.size));
    }


    public void increaseDepth() {
        tables.add(new SymbolTable(this.size));
        length++;
    }

    public boolean hasMain(){
        return mainHasBeenDeclared;
    }

    public void checkTypes(String leftSideType,String rightSideType){
        if(!leftSideType.equals(rightSideType))
            semanticReject(108);
    }

    public void checkReturn(String type){
        tables.get(length).returnHasbeenCalled();
        String actualReturnType = tables.get(length).getReturnType();
        if(actualReturnType.equals("void")){
            if(!type.equals("void"))
                semanticReject(102);
        }else{//if the returnType is void but the returnValue isn't void then throw error
            if(type.equals("void")){
                semanticReject(102);
            }else{//if the returnType is not void but the returned value is void then throw error
                //Else There is a return value and now we have to check the type
                if(!actualReturnType.equals(type))
                    semanticReject(102);
            }
        }
    }

    public int getDepth(){
        return length;
    }

    public void addFuncParam(Token t,String type,boolean arr){
        if(tables.get(length).getParamsTable().contains(t.toString()) == false)
            tables.get(length).getParamsTable().add(new Symbol(t,type,arr,null,false,false));
        else
            semanticReject(101);
    }

    public void fillFunctionInformations(Token returnType,Token functionID){
        if(!mainHasBeenDeclared){
            if(functionID.toString().equals("main"))
                mainHasBeenDeclared = true;
        }else{
            semanticReject(103);
        }
        tables.get(length).fillFunctionInfo(returnType.toString(),functionID);
        tables.get(length-1).insert(new Symbol(functionID,returnType.toString(),false,null,true,true));
        tables.get(length-1).find(functionID.toString()).setParamsTable(tables.get(length).getParamsTable());
    }

    public void findOrInsert(Token token,String type,boolean arr,String arrSize,boolean func,boolean initialzed){
        if(initialzed){
            current = length;
            boolean isFound =false;
            do{
                //try to find the symbol somewhere in the table
                current--;
            }while(current <= 0);
        }else{//if the token is claimed to already initialized
            //Try to insert it
            if(tables.get(length).find(token.toString()) != null || tables.get(length).getParamsTable().contains(token.toString()) != false){
                //Then throw a token exists error
                semanticReject(106);
            }else{
                tables.get(length).insert(new Symbol(token,type,arr,arrSize,func,initialzed));
            }
        }//if the token is claimed to not initialized
        current = length;
        //This is where we find or insert the current symbol. Error reporting is done here
        if(tables.get(current).find(token.toString()) != null){
            //The token exists make sure that it is being used and not declared else error
        }else{

        }
    }

    public void checkTypesUsingId(Token leftSideToken,String rightSideType){
        current = length;
        boolean found = false;
        while(current >= 0){
            if(tables.get(current).find(leftSideToken.toString()) != null){
                String leftSideType = tables.get(current).find(leftSideToken.toString()).getType();
                if(leftSideType.equals(rightSideType)) {
                    found = true;
                    break;
                }else{
                    semanticReject(108);
                }
            }else{
                for(int x=0;x<tables.get(current).getParamsTable().size();x++){
                    if(tables.get(current).getParamsTable().get(x).toString().equals(leftSideToken.toString())){
                        String leftSideType = tables.get(current).getParamsTable().get(x).getType();
                        if(leftSideType.equals(rightSideType)) {
                            found = true;
                            break;
                        }else{
                            semanticReject(108);
                        }
                    }
                }
            }
            current--;
        }//while the current is not passed the root level
        if(!found)
            semanticReject(110);
    }

    public String getReturnTypeFromId(Token t){
        String s = "s";
        current = length;
        while(current >= 0){
            if(tables.get(current).find(t.toString()) != null){
                if(tables.get(current).find(t.toString()).isFunction()){
                    s = tables.get(current).find(t.toString()).getType();
                    break;
                }
            }
            current--;
        }
        if(s.equals("s"))
            semanticReject(109);
        return s;
    }

    public String getTypeFromId(Token idToken,boolean indexing){
        String s = "s";
        current = length;
        while(current >= 0){
            if(tables.get(current).find(idToken.toString()) != null){
                if(tables.get(current).find(idToken.toString()).isArray() && !indexing){
                    s = tables.get(current).find(idToken.toString()).getType()+"arr";
                }else
                    s = tables.get(current).find(idToken.toString()).getType();
                break;
            }else{
                for(int x=0;x<tables.get(current).getParamsTable().size();x++){
                    if(tables.get(current).getParamsTable().get(x).toString().equals(idToken.toString())){
                        if(tables.get(current).getParamsTable().get(x).isArray() && !indexing){
                            s = tables.get(current).getParamsTable().get(x).getType()+"arr";
                        }else
                            s = tables.get(current).getParamsTable().get(x).getType();
                        break;
                    }
                }
            }
            current--;
        }
        if(s.equals("s")){
            semanticReject(110);
        }

        return s;
    }

    public void checkFunctionParameters(Token functionID,int index,String returnType){
    	if(getFunctionById(functionID).getParamsTable().size() > index){
    		String s = getFunctionById(functionID).getParamsTable().get(index).getType();
        if(getFunctionById(functionID).getParamsTable().get(index).isArray())
            s+="arr";
        if(!s.equals(returnType)){
            semanticReject(111);
        }
    	}else{
    		semanticReject(113);
    	}
    }

    public int getFunctionParametersLength(Token functionID){
    	return getFunctionById(functionID).getParamsTable().size();
    }
    public Symbol getFunctionById(Token functionID){
        if(tables.get(0).find(functionID.toString()) != null){
            return tables.get(0).find(functionID.toString());
        }
        semanticReject(109);
        return null;
    }

    public void decreaseDepth(){
        if(length > 0){
            //System.out.println(length);
            if(tables.get(length).getFunctionID() != null){
                if(!tables.get(length).getReturnType().equals("void") && !tables.get(length).hasReturnBeenCalled()){
                    semanticReject(107);
                }
            }
            tables.remove(length--);
        }else//if we aren't at the root level
            semanticReject(100);
    }


    public void semanticReject(int status){
        System.out.println("REJECT");
        //printReport(status);
        System.exit(0);
    }

    public void printReport(int status){
        switch(status){
            case 100:
                System.out.println("Decreasing Depth Error");
                break;
            case 101:
                System.out.println("Two or more parameters have the same ID.");
                break;
            case 102:
                System.out.println("Function return value error");
                break;
            case 103:
                System.out.println("functions declared after main.");
                break;
            case 104:
                System.out.println("Cannot have a void ID");
                break;
            case 105:
                System.out.println("No Main Function Found.");
                break;
            case 106:
                System.out.println("ID has been declared once already.");
                break;
            case 107:
                System.out.println("Function requires a return type");
                break;
            case 108:
                System.out.println("Left Side Type Doesn't agree with right side.");
                break;
            case 109:
                System.out.println("Function not defined.");
                break;
            case 110:
                System.out.println("ID has not been declared");
                break;
            case 111:
                System.out.println("Parameter doesn't agree with function");
                break;
            case 112:
                System.out.println("Array index can only be type int.");
                break;
            case 113:
            	System.out.println("Parameters count don't agree");
            	break;
            default:
                System.out.println("Semantics Rejected this");
                break;
        }
    }


}
