package Main;
import Structs.*;

import java.util.ArrayList;

public class Parser{

    private ArrayList<Token> tokenList;
    private ArrayList<Code> output;
    private int walker = 0;
    private int tCount = 0;
    private int lineCount = 1;
    SymbolTableIterator semanticsTable;

    public Parser(ArrayList<Token> tokenList,int lineCount){
        output = new ArrayList<Code>();
        semanticsTable = new SymbolTableIterator(lineCount);
        this.tokenList = tokenList;
        tokenList.add(new Token("END", TokenType.END));
    }

    public void parse(){
        program();
        if((walker+1) != tokenList.size())
            reject();
        //System.out.println("ACCEPT");
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
            specifier(functionID,returnType.getType());
        }else if(tokenList.get(walker).toString().equals("(")){
            //Increment Table because we know at this point it is a function
            semanticsTable.increaseDepth();
            accept("(");
            params();
            //We have the parameters and the function name and return type so add this to the current symbol table
            ArrayList<Symbol> paramsList = semanticsTable.fillFunctionInformations(returnType,functionID);
            accept(")");

            output.add(new Code(lineCount++,"func",functionID.toString(),returnType.toString(),Integer.toString(paramsList.size())));
            paramsList.forEach(param ->{
                output.add(new Code(lineCount++,"param","4","",param.toString()));
            });
            compoundStmt();
        }else
            reject();
    }

    private void specifier(Token id,TokenType type){
        if(tokenList.get(walker).toString().equals(";")){
            accept(";");
            output.add(new Code(lineCount++,"alloc",Integer.toString(4),"",id.toString()));
            semanticsTable.findOrInsert(id,type,false,null,false,false);
        }else if(tokenList.get(walker).toString().equals("[")){
            accept("[");
            Token arrSize = accept("nonfloat");
            accept("]");
            accept(";");
            output.add(new Code(lineCount++,"alloc",Integer.toString(4*Integer.parseInt(arrSize.toString())),"",id.toString()));
            semanticsTable.findOrInsert(id,type,true,arrSize.toString(),false,false);
        }else
            reject();
    }

    private Token typeSpecifier(){
        if(tokenList.get(walker).toString().equals("int")){
            return new Token(accept("int").toString(),TokenType.NUM);
        }else if(tokenList.get(walker).toString().equals("float")){
           return new Token(accept("float").toString(),TokenType.FLOAT);
        }else if(tokenList.get(walker).toString().equals("void")){
            return new Token(accept("void").toString(),TokenType.KEYWORD);
        }else
            return reject();
    }

    private void params(){
        if(tokenList.get(walker).toString().equals("int")){
            accept("int");
            paramPrime(accept("id"),TokenType.NUM);
            paramListPrime();
        }else if(tokenList.get(walker).toString().equals("float")){
            accept("float");
            paramPrime(accept("id"),TokenType.FLOAT);
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
            TokenType type = typeSpecifier().getType();
            paramPrime(accept("id"),type);
        }else
            reject();
    }


    private void paramPrime(Token param,TokenType type){
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
        }else if (tokenList.get(walker).getType() == TokenType.ID || tokenList.get(walker).toString().equals("(")|| tokenList.get(walker).getType() == TokenType.NUM || tokenList.get(walker).getType() == TokenType.FLOAT||
                tokenList.get(walker).toString().equals(";")|| tokenList.get(walker).toString().equals("{")|| tokenList.get(walker).toString().equals("if")||
                tokenList.get(walker).toString().equals("while")|| tokenList.get(walker).toString().equals("return")|| tokenList.get(walker).toString().equals("}"))
            return;
        else
            reject();
    }

    private void localDeclarationsPrime(){
        if(tokenList.get(walker).toString().equals("int")|| tokenList.get(walker).toString().equals("float")|| tokenList.get(walker).toString().equals("void")){
            Token idType = typeSpecifier();
            specifier(accept("id"),idType.getType());
            localDeclarationsPrime();
        }else if(tokenList.get(walker).getType() == TokenType.ID || tokenList.get(walker).toString().equals("(")|| tokenList.get(walker).getType() == TokenType.NUM || tokenList.get(walker).getType() == TokenType.FLOAT ||
                tokenList.get(walker).toString().equals(";")|| tokenList.get(walker).toString().equals("{")|| tokenList.get(walker).toString().equals("if")||
                tokenList.get(walker).toString().equals("while")|| tokenList.get(walker).toString().equals("return")|| tokenList.get(walker).toString().equals("}"))
            return;
        else
            reject();
    }

    private void statementList(){
        if(tokenList.get(walker).getType() == TokenType.ID || tokenList.get(walker).toString().equals("(")|| tokenList.get(walker).getType() == TokenType.NUM || tokenList.get(walker).getType() == TokenType.FLOAT ||
                tokenList.get(walker).toString().equals(";")|| tokenList.get(walker).toString().equals("{")|| tokenList.get(walker).toString().equals("if")||
                tokenList.get(walker).toString().equals("while")|| tokenList.get(walker).toString().equals("return"))
            statementListPrime();
        else if(tokenList.get(walker).toString().equals("}"))
            return;
        else
            reject();
    }

    private void statementListPrime(){
        if(tokenList.get(walker).getType() == TokenType.ID || tokenList.get(walker).toString().equals("(")|| tokenList.get(walker).getType() == TokenType.NUM || tokenList.get(walker).getType() == TokenType.FLOAT ||
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
        if(tokenList.get(walker).getType() == TokenType.ID || tokenList.get(walker).toString().equals("(")|| tokenList.get(walker).getType() == TokenType.NUM || tokenList.get(walker).getType() == TokenType.FLOAT || tokenList.get(walker).toString().equals(";")){
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
        if (tokenList.get(walker).getType() == TokenType.ID || tokenList.get(walker).toString().equals("(")|| tokenList.get(walker).getType() == TokenType.NUM || tokenList.get(walker).getType() == TokenType.FLOAT){
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
        }else if(tokenList.get(walker).getType() == TokenType.ID || tokenList.get(walker).toString().equals("(")|| tokenList.get(walker).getType() == TokenType.NUM || tokenList.get(walker).getType() == TokenType.FLOAT || tokenList.get(walker).toString().equals(";")||
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
        if(tokenList.get(walker).getType() == TokenType.ID || tokenList.get(walker).toString().equals("(")|| tokenList.get(walker).getType() == TokenType.NUM || tokenList.get(walker).getType() == TokenType.FLOAT){
            Token returnType = expression();
            accept(";");
            //Temporary making it a nonnull return type
            semanticsTable.checkReturn(returnType.getType().toString());
        }else if(tokenList.get(walker).toString().equals(";")){
            accept(";");
            semanticsTable.checkReturn("void");
        }else
            reject();
        return;
    }

    private Token expression(){
        if(tokenList.get(walker).getType() == TokenType.ID){
            Token idToken = accept("id");
            return var(idToken);
        }else if(tokenList.get(walker).toString().equals("(")){
            accept("(");
            Token leftSideType = expression();
            accept(")");
            termPrime(leftSideType);
            additiveExpressionPrime(leftSideType);
            return relopExpression(leftSideType);
        }else if(tokenList.get(walker).getType() == TokenType.NUM){
            Token leftSideType = new Token(accept("num").toString(),TokenType.NUM);
            leftSideType = termPrime(leftSideType);
            leftSideType = additiveExpressionPrime(leftSideType);
            return relopExpression(leftSideType);
        }else if(tokenList.get(walker).getType() == TokenType.FLOAT ) {
            Token leftSideType = new Token(accept("num").toString(),TokenType.FLOAT);
            leftSideType = termPrime(leftSideType);
            leftSideType = additiveExpressionPrime(leftSideType);
            return relopExpression(leftSideType);
        }else{
            reject();
            return null; //This will never be reached
        }
    }

    private Token var(Token idToken){
        if(tokenList.get(walker).toString().equals("[")|| tokenList.get(walker).toString().equals("=")|| tokenList.get(walker).toString().equals("*")|| tokenList.get(walker).toString().equals("/")||
                tokenList.get(walker).toString().equals("+")|| tokenList.get(walker).toString().equals("-")|| tokenList.get(walker).toString().equals("<=")|| tokenList.get(walker).toString().equals("<")||
                tokenList.get(walker).toString().equals(">")|| tokenList.get(walker).toString().equals(">=")|| tokenList.get(walker).toString().equals("==")|| tokenList.get(walker).toString().equals("!=")){
            Token leftSideType = varArr(idToken);
            Token rightSideType = varPrime(leftSideType);
            //Get the corresponding type from the table
            semanticsTable.checkTypes(leftSideType.getType().toString(),rightSideType.getType().toString());
            return rightSideType;
        }else if(tokenList.get(walker).toString().equals("(")){
            accept("(");
            args(idToken);
            accept(")");
            Token leftSideType = termPrime(semanticsTable.getReturnTypeFromId(idToken));//pass the returntype of the function
            additiveExpressionPrime(leftSideType);
            return relopExpression(leftSideType);
        }else if(tokenList.get(walker).toString().equals(";")|| tokenList.get(walker).toString().equals(")")|| tokenList.get(walker).toString().equals("]")|| tokenList.get(walker).toString().equals(",")){
            Token leftSideType = varArr(idToken);
            Token rightSideType = varPrime(leftSideType);
            //Get the corresponding type from the table
            semanticsTable.checkTypes(leftSideType.getType().toString(),rightSideType.getType().toString());
            return rightSideType;
        }else{
            reject();
            return null;//This state is unreachable
        }
    }

    private Token varPrime(Token leftSideType){
        if(tokenList.get(walker).toString().equals("=")){
            accept("=");
            Token rightSideType = expression();
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

    private Token varArr(Token idToken){
        if(tokenList.get(walker).toString().equals("[")){
            accept("[");
            if(tokenList.get(walker).getType() == TokenType.FLOAT)
                reject();

            Token returnType = expression();//We may have to come back to this
            System.out.println("Array must be int:"+returnType.getType());
            if(returnType.getType() != TokenType.NUM)
                semanticsTable.semanticReject(112);
            accept("]");
            //returning the get type by id must be returning error?
            return (new Token(idToken.toString(),semanticsTable.getTypeFromId(idToken,true)));
        }else if(tokenList.get(walker).toString().equals("=")|| tokenList.get(walker).toString().equals("*")|| tokenList.get(walker).toString().equals("/")|| tokenList.get(walker).toString().equals("+")||
                tokenList.get(walker).toString().equals("-")|| tokenList.get(walker).toString().equals("<=") || tokenList.get(walker).toString().equals("<") || tokenList.get(walker).toString().equals(">")||
                tokenList.get(walker).toString().equals(">=")|| tokenList.get(walker).toString().equals("==")|| tokenList.get(walker).toString().equals("!=")|| tokenList.get(walker).toString().equals(";")||
                tokenList.get(walker).toString().equals(")")|| tokenList.get(walker).toString().equals("]")|| tokenList.get(walker).toString().equals(","))
            return (new Token(idToken.toString(),semanticsTable.getTypeFromId(idToken,false)));
        else{
            reject();
            return null;//This state is unreachable
        }
    }

    private Token relopExpression(Token leftSideType){
        if(tokenList.get(walker).toString().equals("<=")|| tokenList.get(walker).toString().equals("<")|| tokenList.get(walker).toString().equals(">")|| tokenList.get(walker).toString().equals(">=")||
                tokenList.get(walker).toString().equals("==")|| tokenList.get(walker).toString().equals("!=")){
            relop();
            Token rightSideType = additiveExpression(leftSideType);
            semanticsTable.checkTypes(rightSideType.getType().toString(),leftSideType.getType().toString());
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

    private Token additiveExpression(Token leftSideType) {
        if (tokenList.get(walker).toString().equals("(")) {
            accept("(");
            Token rightSideType = expression();
            accept(")");
            semanticsTable.checkTypes(rightSideType.getType().toString(),leftSideType.getType().toString());
            termPrime(leftSideType);
            return additiveExpressionPrime(leftSideType);
        } else if (tokenList.get(walker).getType() == TokenType.ID) {
            Token rightSideType = call(accept("id"));
            semanticsTable.checkTypes(rightSideType.getType().toString(), leftSideType.getType().toString());
            termPrime(leftSideType);
            return additiveExpressionPrime(leftSideType);
        } else if (tokenList.get(walker).getType() == TokenType.NUM) {
            Token rightSideType = new Token(accept("num").toString(),TokenType.NUM);
            rightSideType = termPrime(rightSideType);
            return additiveExpressionPrime(rightSideType);
        } else if (tokenList.get(walker).getType() == TokenType.FLOAT){
            Token rightSideType = new Token(accept("num").toString(),TokenType.FLOAT);
            rightSideType = termPrime(rightSideType);
            return additiveExpressionPrime(rightSideType);
        }else{
            reject();
            return null;//This state is unreachable
        }
    }

    private Token additiveExpressionPrime(Token leftSideType){
        if(tokenList.get(walker).toString().equals("+")|| tokenList.get(walker).toString().equals("-")){
            addop();
            Token rightSideType = term();
            semanticsTable.checkTypes(leftSideType.getType().toString(),rightSideType.getType().toString());
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

    private Token term(){
        if(tokenList.get(walker).toString().equals("(")){
            accept("(");
            Token leftSideType = expression();
            accept(")");
            return termPrime(leftSideType);
        }else if(tokenList.get(walker).getType() == TokenType.ID){
            Token leftSideType = call(accept("id"));
            return termPrime(leftSideType);
        }else if(tokenList.get(walker).getType() == TokenType.NUM){
            Token num = new Token(accept("num").toString(),TokenType.NUM);
            return termPrime(num);
        } else{ if(tokenList.get(walker).getType() == TokenType.FLOAT){
            Token num = new Token(accept("num").toString(),TokenType.FLOAT);
            return termPrime(num);
        }
            reject();
            return null; //This will never be reached
        }

    }

    private Token termPrime(Token leftSideType){
        if(tokenList.get(walker).toString().equals("*")|| tokenList.get(walker).toString().equals("/")){
            mulop();
            Token rightSideType = factor();
            semanticsTable.checkTypes(leftSideType.getType().toString(),rightSideType.getType().toString());
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

    private Token factor(){
        if (tokenList.get(walker).toString().equals("(")){
            accept("(");
            Token sideType = expression();
            accept(")");
            return sideType;
        }else if(tokenList.get(walker).getType() == TokenType.ID){
            return call(accept("id"));
        }else if(tokenList.get(walker).getType() == TokenType.NUM){
            Token num = new Token(accept("num").toString(),TokenType.NUM);
            return num;
        }else if(tokenList.get(walker).getType() == TokenType.FLOAT) {
            accept("num");//This will be an issue later
            Token num = new Token(accept("num").toString(),TokenType.FLOAT);//This will be an issue later
            return num;
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

    private Token call(Token idToken){
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
        if(tokenList.get(walker).getType() == TokenType.ID || tokenList.get(walker).toString().equals("(")|| tokenList.get(walker).getType() == TokenType.NUM || tokenList.get(walker).getType() == TokenType.FLOAT){
            argList(functionId);
        }else if (tokenList.get(walker).toString().equals(")"))
        	if(semanticsTable.getFunctionParametersLength(functionId) != 0) {

                semanticsTable.semanticReject(113);
            }
        	else
            	return;
        else
            reject();
    }

    private void argList(Token functionId){
        if(tokenList.get(walker).getType() == TokenType.ID || tokenList.get(walker).toString().equals("(")|| tokenList.get(walker).getType() == TokenType.NUM || tokenList.get(walker).getType() == TokenType.FLOAT){
            Token returnType = expression();
            semanticsTable.checkFunctionParameters(functionId,0,returnType.getType().toString());
            int lastIndex = argListPrime(functionId,1);
            if(lastIndex != semanticsTable.getFunctionParametersLength(functionId)) {

                semanticsTable.semanticReject(113);
            }
        }else
            reject();
    }

    private int argListPrime(Token functionId,int index){
        if(tokenList.get(walker).toString().equals(",")){
            accept(",");
            Token returnType = expression();
            System.out.println("Return TYPE!: "+returnType.toString());

            semanticsTable.checkFunctionParameters(functionId,index,returnType.getType().toString());
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
        //System.out.println("Accepted");
        return(tokenList.get(walker++));
    }

    public void printCode(){
        output.forEach(out -> {
            System.out.println(out.toString());
        });
    }

    private Token reject(){
       // System.out.println("REJECTING: "+tokenList.get(walker).toString()+" Type: "+tokenList.get(walker).getType());

        System.out.println("REJECT");
        System.exit(0);
        //This will never be reached
        return null;
    }



}
