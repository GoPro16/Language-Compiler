import java.util.ArrayList;

public class Parser{

    private ArrayList<Token> tokenList;
    private int walker = 0;
    SymbolTableIterator semanticsTable;

    public Parser(ArrayList<Token> tokenList,int lineCount){
        semanticsTable = new SymbolTableIterator(lineCount);
        this.tokenList = tokenList;
        tokenList.add(new Token("END", TokenType.END));
    }

    public void parse(){
        program();
        if((walker+1) != tokenList.size())
            reject();
        System.out.println("ACCEPT");
    }

    private void program(){
        declarationList();
    }

    private void declarationList(){
        declaration();
        declarationListPrime();
        //Check if there is a main
        if(!semanticsTable.hasMain()){
            semanticsTable.semanticReject(105);
        }
    }

    private void declaration(){
        if(tokenList.get(walker).toString().equals("int") || tokenList.get(walker).toString().equals("float") || tokenList.get(walker).toString().equals("void")){
            Token returnValue = typeSpecifier();
            declarationPrime(accept("id"),returnValue);
        }else
            reject();
    }

    private void declarationListPrime(){
        if(tokenList.get(walker).toString().equals("int")|| tokenList.get(walker).toString().equals("float")|| tokenList.get(walker).toString().equals("void")){
            declaration();
            declarationListPrime();
        }
    }

    private void declarationPrime(Token functionID,Token returnType){
        if(tokenList.get(walker).toString().equals(";")|| tokenList.get(walker).toString().equals("[")){
            specifier(functionID,returnType.toString());
        }else if(tokenList.get(walker).toString().equals("(")){
            //Increment Table because we know at this point it is a function
            semanticsTable.increaseDepth();
            accept("(");
            params();
            //We have the parameters and the function name and return type so add this to the current symbol table
            semanticsTable.fillFunctionInformations(returnType,functionID);
            accept(")");
            compoundStmt();
        }else
            reject();
    }

    private void specifier(Token id,String type){
        if(tokenList.get(walker).toString().equals(";")){
            accept(";");
            semanticsTable.findOrInsert(id,type,false,null,false,false);
        }else if(tokenList.get(walker).toString().equals("[")){
            accept("[");
            Token arrSize = accept("nonfloat");
            accept("]");
            accept(";");
            semanticsTable.findOrInsert(id,type,true,arrSize.toString(),false,false);
        }else
            reject();
    }

    private Token typeSpecifier(){
        if(tokenList.get(walker).toString().equals("int")){
            return accept("int");
        }else if(tokenList.get(walker).toString().equals("float")){
           return accept("float");
        }else if(tokenList.get(walker).toString().equals("void")){
            return accept("void");
        }else
            return reject();
    }

    private void params(){
        ArrayList<Token> paramTokens = new ArrayList<Token>();
        if(tokenList.get(walker).toString().equals("int")){
            accept("int");
            paramPrime(accept("id"),"int");
            paramListPrime();
        }else if(tokenList.get(walker).toString().equals("float")){
            accept("float");
            paramPrime(accept("id"),"float");
            paramListPrime();
        }else if(tokenList.get(walker).toString().equals("void")){
            accept("void");
            paramList();
        }else
            reject();
    }

    private void paramList(){
        if(tokenList.get(walker).getType() == TokenType.ID){
            semanticsTable.semanticReject(104);
        }else if(tokenList.get(walker).toString().equals(")"))
            return;
        else
            reject();
    }

    private void paramListPrime(){
        if(tokenList.get(walker).toString().equals(",")){
            accept(",");
            param();
            paramListPrime();
        }else if(tokenList.get(walker).toString().equals(")"))
            return;
        else
            reject();
    }

    private void param(){
        if(tokenList.get(walker).toString().equals("int")|| tokenList.get(walker).toString().equals("float")|| tokenList.get(walker).toString().equals("void")){
            String type = typeSpecifier().toString();
            paramPrime(accept("id"),type);
        }else
            reject();
    }


    private void paramPrime(Token param,String type){
        if(tokenList.get(walker).toString().equals("[")){
            accept("[");
            accept("]");
            //Token token,String type,boolean arr,boolean func,boolean init
            semanticsTable.addFuncParam(param,type,true);
        }else if(tokenList.get(walker).toString().equals(",")|| tokenList.get(walker).toString().equals(")")){
            semanticsTable.addFuncParam(param,type,false);
            return;
        }else
            reject();
    }

    private void compoundStmt(){
        if (tokenList.get(walker).toString().equals("{")){
            accept("{");
            localDeclarations();
            statementList();
            accept("}");
            //decrement depth
            semanticsTable.decreaseDepth();
        }else
            reject();
    }

    private void localDeclarations(){
        if (tokenList.get(walker).toString().equals("int")|| tokenList.get(walker).toString().equals("float")|| tokenList.get(walker).toString().equals("void")){
            localDeclarationsPrime();
        }else if (tokenList.get(walker).getType() == TokenType.ID || tokenList.get(walker).toString().equals("(")|| tokenList.get(walker).getType() == TokenType.NUM ||
                tokenList.get(walker).toString().equals(";")|| tokenList.get(walker).toString().equals("{")|| tokenList.get(walker).toString().equals("if")||
                tokenList.get(walker).toString().equals("while")|| tokenList.get(walker).toString().equals("return")|| tokenList.get(walker).toString().equals("}"))
            return;
        else
            reject();
    }

    private void localDeclarationsPrime(){
        if(tokenList.get(walker).toString().equals("int")|| tokenList.get(walker).toString().equals("float")|| tokenList.get(walker).toString().equals("void")){
            Token idType = typeSpecifier();
            specifier(accept("id"),idType.toString());
            localDeclarationsPrime();
        }else if(tokenList.get(walker).getType() == TokenType.ID || tokenList.get(walker).toString().equals("(")|| tokenList.get(walker).getType() == TokenType.NUM ||
                tokenList.get(walker).toString().equals(";")|| tokenList.get(walker).toString().equals("{")|| tokenList.get(walker).toString().equals("if")||
                tokenList.get(walker).toString().equals("while")|| tokenList.get(walker).toString().equals("return")|| tokenList.get(walker).toString().equals("}"))
            return;
        else
            reject();
    }

    private void statementList(){
        if(tokenList.get(walker).getType() == TokenType.ID || tokenList.get(walker).toString().equals("(")|| tokenList.get(walker).getType() == TokenType.NUM ||
                tokenList.get(walker).toString().equals(";")|| tokenList.get(walker).toString().equals("{")|| tokenList.get(walker).toString().equals("if")||
                tokenList.get(walker).toString().equals("while")|| tokenList.get(walker).toString().equals("return"))
            statementListPrime();
        else if(tokenList.get(walker).toString().equals("}"))
            return;
        else
            reject();
    }

    private void statementListPrime(){
        if(tokenList.get(walker).getType() == TokenType.ID || tokenList.get(walker).toString().equals("(")|| tokenList.get(walker).getType() == TokenType.NUM ||
                tokenList.get(walker).toString().equals(";")|| tokenList.get(walker).toString().equals("{")|| tokenList.get(walker).toString().equals("if")||
                tokenList.get(walker).toString().equals("while")|| tokenList.get(walker).toString().equals("return")){
            statement();
            statementListPrime();
        }else if(tokenList.get(walker).toString().equals("}"))
            return;
        else
            reject();
    }

    private void statement(){
        if(tokenList.get(walker).getType() == TokenType.ID || tokenList.get(walker).toString().equals("(")|| tokenList.get(walker).getType() == TokenType.NUM || tokenList.get(walker).toString().equals(";")){
            expressionStmt();
        }else if(tokenList.get(walker).toString().equals("{")){
            semanticsTable.increaseDepth();
            compoundStmt();
        }else if(tokenList.get(walker).toString().equals("if")){
            selectionStmt();
        }else if(tokenList.get(walker).toString().equals("while")){
            iterationStmt();
        }else if(tokenList.get(walker).toString().equals("return")){
            returnStmt();
        }else
            reject();
    }

    private void expressionStmt(){
        if (tokenList.get(walker).getType() == TokenType.ID || tokenList.get(walker).toString().equals("(")|| tokenList.get(walker).getType() == TokenType.NUM){
            expression();
            accept(";");
        }else if(tokenList.get(walker).toString().equals(";")){
            accept(";");
        }else
            reject();
    }

    private void selectionStmt(){
        if(tokenList.get(walker).toString().equals("if")){
            accept("if");
            accept("(");
            expression();
            accept(")");
            statement();
            selectionStmtPrime();
        }else
            reject();
    }

    private void selectionStmtPrime(){
        if(tokenList.get(walker).toString().equals("else")){
            accept("else");
            statement();
        }else if(tokenList.get(walker).getType() == TokenType.ID || tokenList.get(walker).toString().equals("(")|| tokenList.get(walker).getType() == TokenType.NUM|| tokenList.get(walker).toString().equals(";")||
                tokenList.get(walker).toString().equals("{")|| tokenList.get(walker).toString().equals("if")|| tokenList.get(walker).toString().equals("while")||
                tokenList.get(walker).toString().equals("return")|| tokenList.get(walker).toString().equals("}"))
            return;
        else
            reject();
    }

    private void iterationStmt(){
        if(tokenList.get(walker).toString().equals("while")){
            accept("while");
            accept("(");
            expression();
            accept(")");
            statement();
        }else
            reject();
    }

    private void returnStmt(){
        if (tokenList.get(walker).toString().equals("return")){
            accept("return");
            returnStmtPrime();
        }else
            reject();
    }

    private void returnStmtPrime(){
        if(tokenList.get(walker).getType() == TokenType.ID || tokenList.get(walker).toString().equals("(")|| tokenList.get(walker).getType() == TokenType.NUM){
            String returnType = expression();
            accept(";");
            //Temporary making it a nonnull return type
            semanticsTable.checkReturn(returnType);
        }else if(tokenList.get(walker).toString().equals(";")){
            accept(";");
            semanticsTable.checkReturn("void");
        }else
            reject();
        return;
    }

    private String expression(){
        if(tokenList.get(walker).getType() == TokenType.ID){
            Token idToken = accept("id");
            return var(idToken);
        }else if(tokenList.get(walker).toString().equals("(")){
            accept("(");
            String leftSideType = expression();
            accept(")");
            termPrime(leftSideType);
            additiveExpressionPrime(leftSideType);
            return relopExpression(leftSideType);
        }else if(tokenList.get(walker).getType() == TokenType.NUM){
            accept("num");
            termPrime("int");
            additiveExpressionPrime("int");
            return relopExpression("int");
        }else if(tokenList.get(walker).getType() == TokenType.FLOAT ) {
            accept("num");
            termPrime("float");
            additiveExpressionPrime("float");
            return relopExpression("float");
        }else{
            reject();
            return null; //This will never be reached
        }
    }

    private String var(Token idToken){
        if(tokenList.get(walker).toString().equals("[")|| tokenList.get(walker).toString().equals("=")|| tokenList.get(walker).toString().equals("*")|| tokenList.get(walker).toString().equals("/")||
                tokenList.get(walker).toString().equals("+")|| tokenList.get(walker).toString().equals("-")|| tokenList.get(walker).toString().equals("<=")|| tokenList.get(walker).toString().equals("<")||
                tokenList.get(walker).toString().equals(">")|| tokenList.get(walker).toString().equals(">=")|| tokenList.get(walker).toString().equals("==")|| tokenList.get(walker).toString().equals("!=")){
            String leftSideType = varArr(idToken);
            String rightSideType = varPrime(leftSideType);
            //Get the corresponding type from the table
            semanticsTable.checkTypes(leftSideType,rightSideType);
            return rightSideType;
        }else if(tokenList.get(walker).toString().equals("(")){
            accept("(");
            args(idToken);
            accept(")");
            String leftSideType = termPrime(semanticsTable.getReturnTypeFromId(idToken));//pass the returntype of the function
            additiveExpressionPrime(leftSideType);
            return relopExpression(leftSideType);
        }else if(tokenList.get(walker).toString().equals(";")|| tokenList.get(walker).toString().equals(")")|| tokenList.get(walker).toString().equals("]")|| tokenList.get(walker).toString().equals(",")){
            String leftSideType = varArr(idToken);
            String rightSideType = varPrime(leftSideType);
            //Get the corresponding type from the table
            semanticsTable.checkTypes(leftSideType,rightSideType);
            return rightSideType;
        }else{
            reject();
            return null;//This state is unreachable
        }
    }

    private String varPrime(String leftSideType){
        if(tokenList.get(walker).toString().equals("=")){
            accept("=");
            String rightSideType = expression();
            return rightSideType;
        }else if(tokenList.get(walker).toString().equals("*")|| tokenList.get(walker).toString().equals("/")|| tokenList.get(walker).toString().equals("+")|| tokenList.get(walker).toString().equals("-")||
                tokenList.get(walker).toString().equals("<=")|| tokenList.get(walker).toString().equals("<")|| tokenList.get(walker).toString().equals(">")|| tokenList.get(walker).toString().equals(">=")||
                tokenList.get(walker).toString().equals("==")|| tokenList.get(walker).toString().equals("!=")){
            termPrime(leftSideType);
            additiveExpressionPrime(leftSideType);
            return relopExpression(leftSideType);
        }else if(tokenList.get(walker).toString().equals(";")|| tokenList.get(walker).toString().equals(")")|| tokenList.get(walker).toString().equals("]")|| tokenList.get(walker).toString().equals(",")){
            termPrime(leftSideType);
            additiveExpressionPrime(leftSideType);
            return relopExpression(leftSideType);
        }else{
            reject();
            return null;//This state is unreachable
        }
    }

    private String varArr(Token idToken){
        if(tokenList.get(walker).toString().equals("[")){
            accept("[");
            if(tokenList.get(walker).getType() == TokenType.FLOAT)
                reject();

            String returnType = expression();//We may have to come back to this
            if(!returnType.equals("int"))
                semanticsTable.semanticReject(112);
            accept("]");
            return semanticsTable.getTypeFromId(idToken,true);
        }else if(tokenList.get(walker).toString().equals("=")|| tokenList.get(walker).toString().equals("*")|| tokenList.get(walker).toString().equals("/")|| tokenList.get(walker).toString().equals("+")||
                tokenList.get(walker).toString().equals("-")|| tokenList.get(walker).toString().equals("<=") || tokenList.get(walker).toString().equals("<") || tokenList.get(walker).toString().equals(">")||
                tokenList.get(walker).toString().equals(">=")|| tokenList.get(walker).toString().equals("==")|| tokenList.get(walker).toString().equals("!=")|| tokenList.get(walker).toString().equals(";")||
                tokenList.get(walker).toString().equals(")")|| tokenList.get(walker).toString().equals("]")|| tokenList.get(walker).toString().equals(","))
            return semanticsTable.getTypeFromId(idToken,false);
        else{
            reject();
            return null;//This state is unreachable
        }
    }

    private String relopExpression(String leftSideType){
        if(tokenList.get(walker).toString().equals("<=")|| tokenList.get(walker).toString().equals("<")|| tokenList.get(walker).toString().equals(">")|| tokenList.get(walker).toString().equals(">=")||
                tokenList.get(walker).toString().equals("==")|| tokenList.get(walker).toString().equals("!=")){
            relop();
            String rightSideType = additiveExpression(leftSideType);
            semanticsTable.checkTypes(rightSideType,leftSideType);
            return rightSideType;
        }else if(tokenList.get(walker).toString().equals(";")|| tokenList.get(walker).toString().equals(")")|| tokenList.get(walker).toString().equals("]")|| tokenList.get(walker).toString().equals(","))
            return leftSideType;
        else{
            reject();//This will never be reached
            return null;
        }
    }

    private void relop(){
        if(tokenList.get(walker).toString().equals("<=")){
            accept("<=");
        }else if(tokenList.get(walker).toString().equals("<")){
            accept("<");
        }else if(tokenList.get(walker).toString().equals(">")){
            accept(">");
        }else if(tokenList.get(walker).toString().equals(">=")){
            accept(">=");
        }else if(tokenList.get(walker).toString().equals("==")){
            accept("==");
        }else if(tokenList.get(walker).toString().equals("!=")){
            accept("!=");
        }else
            reject();
    }

    private String additiveExpression(String leftSideType) {
        if (tokenList.get(walker).toString().equals("(")) {
            accept("(");
            String rightSideType = expression();
            accept(")");
            semanticsTable.checkTypes(rightSideType, leftSideType);
            termPrime(leftSideType);
            return additiveExpressionPrime(leftSideType);
        } else if (tokenList.get(walker).getType() == TokenType.ID) {
            String rightSideType = call(accept("id"));
            semanticsTable.checkTypes(rightSideType, leftSideType);
            termPrime(leftSideType);
            return additiveExpressionPrime(leftSideType);
        } else if (tokenList.get(walker).getType() == TokenType.NUM) {
            accept("num");
            termPrime("int");
            return additiveExpressionPrime("int");
        } else if (tokenList.get(walker).getType() == TokenType.FLOAT){
            accept("num");
            termPrime("float");
            return additiveExpressionPrime("float");
        }else{
            reject();
            return null;//This state is unreachable
        }
    }

    private String additiveExpressionPrime(String leftSideType){
        if(tokenList.get(walker).toString().equals("+")|| tokenList.get(walker).toString().equals("-")){
            addop();
            String rightSideType = term();
            semanticsTable.checkTypes(leftSideType,rightSideType);
            return additiveExpressionPrime(rightSideType);
        }else if(tokenList.get(walker).toString().equals("<=")|| tokenList.get(walker).toString().equals("<")|| tokenList.get(walker).toString().equals(">")|| tokenList.get(walker).toString().equals(">=")||
                tokenList.get(walker).toString().equals("==")|| tokenList.get(walker).toString().equals("!=")|| tokenList.get(walker).toString().equals(";")|| tokenList.get(walker).toString().equals(")")||
                tokenList.get(walker).toString().equals("]")|| tokenList.get(walker).toString().equals(","))
            return leftSideType;
        else{
            reject();
            return null;//This will never be reached
        }
    }

    private Token addop(){
        if(tokenList.get(walker).toString().equals("+")){
            return(accept("+"));
        }else if(tokenList.get(walker).toString().equals("-")){
            return(accept("-"));
        }else
            return reject();
    }

    private String term(){
        if(tokenList.get(walker).toString().equals("(")){
            accept("(");
            String leftSideType = expression();
            accept(")");
            return termPrime(leftSideType);
        }else if(tokenList.get(walker).getType() == TokenType.ID){
            String leftSideType = call(accept("id"));
            return termPrime(leftSideType);
        }else if(tokenList.get(walker).getType() == TokenType.NUM){
            accept("num");
            return termPrime("int");
        } else{ if(tokenList.get(walker).getType() == TokenType.FLOAT){
            accept("num");
            return termPrime("float");
        }
            reject();
            return null; //This will never be reached
        }

    }

    private String termPrime(String leftSideType){
        if(tokenList.get(walker).toString().equals("*")|| tokenList.get(walker).toString().equals("/")){
            mulop();
            String rightSideType = factor();
            semanticsTable.checkTypes(leftSideType,rightSideType);
            return termPrime(rightSideType);
        }else if(tokenList.get(walker).toString().equals("<=")|| tokenList.get(walker).toString().equals("<")|| tokenList.get(walker).toString().equals(">")|| tokenList.get(walker).toString().equals(">=")||
                tokenList.get(walker).toString().equals("==")|| tokenList.get(walker).toString().equals("!=")|| tokenList.get(walker).toString().equals("+")|| tokenList.get(walker).toString().equals("-")||
                tokenList.get(walker).toString().equals(";")|| tokenList.get(walker).toString().equals(")")|| tokenList.get(walker).toString().equals("]")|| tokenList.get(walker).toString().equals(","))
            return leftSideType;
        else{
            reject();
            return null;//This will never be reached
        }
    }

    private String factor(){
        if (tokenList.get(walker).toString().equals("(")){
            accept("(");
            String sideType = expression();
            accept(")");
            return sideType;
        }else if(tokenList.get(walker).getType() == TokenType.ID){
            return call(accept("id"));
        }else if(tokenList.get(walker).getType() == TokenType.NUM){
            accept("num");//This will be an issue later
            return "int";

        }else if(tokenList.get(walker).getType() == TokenType.FLOAT) {
            accept("num");//This will be an issue later
            return "float";
        }else{
            reject();
            return null;//THis state is unreachable
        }
    }

    private void mulop(){
        if (tokenList.get(walker).toString().equals("*")){
            accept("*");
        }else if (tokenList.get(walker).toString().equals("/")){
            accept("/");
        }else
            reject();
    }

    private String call(Token idToken){
        if (tokenList.get(walker).toString().equals("(")){
            accept("(");
            args(idToken);
            accept(")");
            return semanticsTable.getReturnTypeFromId(idToken);
        }else if (tokenList.get(walker).toString().equals("[")){
            return varArr(idToken);
        }else if(tokenList.get(walker).toString().equals("+")|| tokenList.get(walker).toString().equals("-")|| tokenList.get(walker).toString().equals("*")|| tokenList.get(walker).toString().equals("/")||
                tokenList.get(walker).toString().equals("<=")|| tokenList.get(walker).toString().equals("<")|| tokenList.get(walker).toString().equals(">")|| tokenList.get(walker).toString().equals(">")||
                tokenList.get(walker).toString().equals(">=")|| tokenList.get(walker).toString().equals("==")|| tokenList.get(walker).toString().equals("!=")|| tokenList.get(walker).toString().equals(";")||
                tokenList.get(walker).toString().equals(")")|| tokenList.get(walker).toString().equals("]")|| tokenList.get(walker).toString().equals(",")){
            return varArr(idToken);
        }else {
            reject();
            return null;//this will never be reached
        }
    }

    private void args(Token functionId){
        if(tokenList.get(walker).getType() == TokenType.ID || tokenList.get(walker).toString().equals("(")|| tokenList.get(walker).getType() == TokenType.NUM){
            argList(functionId);
        }else if (tokenList.get(walker).toString().equals(")"))
        	if(semanticsTable.getFunctionParametersLength(functionId) != 0)
        		semanticsTable.semanticReject(113);
        	else
            	return;
        else
            reject();
    }

    private void argList(Token functionId){
        if(tokenList.get(walker).getType() == TokenType.ID || tokenList.get(walker).toString().equals("(")|| tokenList.get(walker).getType() == TokenType.NUM){
            String returnType = expression();
            semanticsTable.checkFunctionParameters(functionId,0,returnType);
            int lastIndex = argListPrime(functionId,1);
            if(lastIndex != semanticsTable.getFunctionParametersLength(functionId))
            	semanticsTable.semanticReject(113);
        }else
            reject();
    }

    private int argListPrime(Token functionId,int index){
        if(tokenList.get(walker).toString().equals(",")){
            accept(",");
            String returnType = expression();
            semanticsTable.checkFunctionParameters(functionId,index,returnType);
            return argListPrime(functionId,index+1);
        }else if(tokenList.get(walker).toString().equals(")"))
            return index;
        else{
            reject();
            return -1;
        }

    }

    private Token accept(String value){
        //System.out.println("Accepting:"+tokenList.get(walker).toString());
        if(!(tokenList.get(walker).equals(null))){
            switch(tokenList.get(walker).getType()) {
                case ID:
                    if (!(value.equals("id")))
                        reject();
                    break;
                case FLOAT:
                    if (!(value.equals("num")) || value.equals("nonfloat"))
                        reject();
                    break;
                case NUM:
                    if (!(value.equals("num")) && !(value.equals("nonfloat")))
                        reject();
                    break;
                default:
                    if (!(value.equals(tokenList.get(walker).toString())))
                        reject();
                    break;
            }//switch to check valid input
        }else
            reject();
        return(tokenList.get(walker++));
    }

    private Token reject(){
        //System.out.println("REJECTING: "+tokenList.get(walker).toString()+" Type: "+tokenList.get(walker).getType());
        System.out.println("REJECT");
        System.exit(0);
        //This will never be reached
        return null;
    }



}
