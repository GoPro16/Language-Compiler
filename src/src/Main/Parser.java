package Main;
import Structs.*;
import com.sun.org.apache.xerces.internal.util.SynchronizedSymbolTable;

import java.util.ArrayList;
import java.util.regex.Pattern;

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
            Token compToken = expression();
            accept(")");
            statement();
            selectionStmtPrime(compToken);
        }else
            reject();
    }

    private void selectionStmtPrime(Token compToken){
        if(tokenList.get(walker).toString().equals("else")){
            int lCount = lineCount;
            output.add(new Code(lineCount++,"BR","","",""));
            accept("else");
            String lineNum = Integer.toString(lineCount);
            updateIfElse(compToken,lineNum);
            statement();
            int lCountNew = lineCount;
            output.add(new Code(lineCount++,"end","","",""));
            updateElseBlock(lCountNew,lCount);
        }else if(tokenList.get(walker).getType() == TokenType.ID || tokenList.get(walker).toString().equals("(")|| tokenList.get(walker).getType() == TokenType.NUM || tokenList.get(walker).getType() == TokenType.FLOAT || tokenList.get(walker).toString().equals(";")||
                tokenList.get(walker).toString().equals("{")|| tokenList.get(walker).toString().equals("if")|| tokenList.get(walker).toString().equals("while")||
                tokenList.get(walker).toString().equals("return")|| tokenList.get(walker).toString().equals("}")){
            //Find the statement just after comp
            String lineNum = Integer.toString(lineCount);
            updateIfElse(compToken,lineNum);
            output.add(new Code(lineCount++,"end","","",""));
        }
        else
            reject();
    }
    private void updateIfElse(Token compToken,String lineNum){
        for(int i=output.size()-1;i>=0;i--){
            if(output.get(i).getFirst().equals("comp")){
                if(output.get(i+1).getSecond().equals(compToken.toString())){
                    output.get(i+1).setFourth(lineNum);
                }//if the t matches
            }//if the first is equal to comp
        }
    }

    private void updateElseBlock(int currentLine,int newLine){
        for(int i=output.size()-1;i>=0;i--){
            if(output.get(i).getLineNumber() == newLine){
                output.get(i).setFourth(Integer.toString(currentLine));
            }//if the first is equal to comp
        }
    }

    private void iterationStmt(){
        if(tokenList.get(walker).toString().equals("while")){
            int startOfWhile = lineCount;
            accept("while");
            accept("(");
            Token compToken = expression();
            accept(")");
            statement();
            output.add(new Code(lineCount++,"BR","","",Integer.toString(startOfWhile)));
            //Find the statement just after comp
            String lineNum = Integer.toString(lineCount);
            updateIfElse(compToken,lineNum);
            output.add(new Code(lineCount++,"end","","",""));

        }else
            reject();
    }

    private void updateWhile(Token compToken,String lineNum){
        for(int i=output.size()-1;i>=0;i--){
            if(output.get(i).getFirst().equals("comp")){
                if(output.get(i+1).getSecond().equals(compToken.toString())){
                    output.get(i+1).setFourth(lineNum);
                }//if the t matches
            }//if the first is equal to comp
        }
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
            output.add(new Code(lineCount++,"ret","","",returnType.toString()));
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
            //PASTE
            String t = getNewT();
            output.add(new Code(lineCount++,"call",idToken.toString(),"",t));
            accept("(");
            args(idToken);
            accept(")");
            Token leftSideType = termPrime(semanticsTable.getReturnTypeFromId(idToken));//pass the returntype of the function
            additiveExpressionPrime(leftSideType);
            return (new Token(t,relopExpression(leftSideType).getType()));
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
            output.add(new Code(lineCount++,"assign",rightSideType.toString(),"",leftSideType.toString()));
            return rightSideType;
        }else if(tokenList.get(walker).toString().equals("*")|| tokenList.get(walker).toString().equals("/")|| tokenList.get(walker).toString().equals("+")|| tokenList.get(walker).toString().equals("-")||
                tokenList.get(walker).toString().equals("<=")|| tokenList.get(walker).toString().equals("<")|| tokenList.get(walker).toString().equals(">")|| tokenList.get(walker).toString().equals(">=")||
                tokenList.get(walker).toString().equals("==")|| tokenList.get(walker).toString().equals("!=")){
            Token lType = termPrime(leftSideType);
            lType = additiveExpressionPrime(leftSideType);
            return relopExpression(lType);
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
            if(returnType.getType() != TokenType.NUM)
                semanticsTable.semanticReject(112);
            accept("]");
            String t = getNewT();
            try {
                Integer.parseInt(returnType.toString());
                output.add(new Code(lineCount++,"disp",idToken.toString(),Integer.toString(Integer.parseInt(returnType.toString())*4),t));
            } catch (NumberFormatException ignored) {
                output.add(new Code(lineCount++,"mult",returnType.toString(),"4",t));
                String oldT = t;
                t = getNewT();
                output.add(new Code(lineCount++,"disp",idToken.toString(),oldT,t));
            }
            //returning the get type by id must be returning error?
            return (new Token(t,semanticsTable.getTypeFromId(idToken,true)));
        }else if(tokenList.get(walker).toString().equals("=")|| tokenList.get(walker).toString().equals("*")|| tokenList.get(walker).toString().equals("/")|| tokenList.get(walker).toString().equals("+")||
                tokenList.get(walker).toString().equals("-")|| tokenList.get(walker).toString().equals("<=") || tokenList.get(walker).toString().equals("<") || tokenList.get(walker).toString().equals(">")||
                tokenList.get(walker).toString().equals(">=")|| tokenList.get(walker).toString().equals("==")|| tokenList.get(walker).toString().equals("!=")|| tokenList.get(walker).toString().equals(";")||
                tokenList.get(walker).toString().equals(")")|| tokenList.get(walker).toString().equals("]")|| tokenList.get(walker).toString().equals(",")){
            return (new Token(idToken.toString(),semanticsTable.getTypeFromId(idToken,false)));
        } else{
            reject();
            return null;//This state is unreachable
        }
    }

    private Token relopExpression(Token leftSideType){
        if(tokenList.get(walker).toString().equals("<=")|| tokenList.get(walker).toString().equals("<")|| tokenList.get(walker).toString().equals(">")|| tokenList.get(walker).toString().equals(">=")||
                tokenList.get(walker).toString().equals("==")|| tokenList.get(walker).toString().equals("!=")){
            String op = relop();
            Token rightSideType = additiveExpression(leftSideType);
            semanticsTable.checkTypes(rightSideType.getType().toString(),leftSideType.getType().toString());
            String t = getNewT();
            output.add(new Code(lineCount++,"comp",leftSideType.toString(),rightSideType.toString(),t));
            output.add(new Code(lineCount++,op,t,"",""));
            return new Token(t,rightSideType.getType());
        }else if(tokenList.get(walker).toString().equals(";")|| tokenList.get(walker).toString().equals(")")|| tokenList.get(walker).toString().equals("]")|| tokenList.get(walker).toString().equals(","))
            return leftSideType;
        else{
            reject();//This will never be reached
            return null;
        }
    }

    private String relop(){
        if(tokenList.get(walker).toString().equals("<=")){
            accept("<=");
            return("BRGREQQ");
        }else if(tokenList.get(walker).toString().equals("<")){
            accept("<");
            return("BRGREQ");
        }else if(tokenList.get(walker).toString().equals(">")){
            accept(">");
            return("BRLEQQ");
        }else if(tokenList.get(walker).toString().equals(">=")){
            accept(">=");
            return("BRLEQQ");
        }else if(tokenList.get(walker).toString().equals("==")){
            accept("==");
            return("BRNEQ");
        }else if(tokenList.get(walker).toString().equals("!=")){
            accept("!=");
            return("BREQ");
        }else
            return reject().toString();
    }

    private Token additiveExpression(Token leftSideType) {
        if (tokenList.get(walker).toString().equals("(")) {
            accept("(");
            Token rightSideType = expression();
            accept(")");
            semanticsTable.checkTypes(rightSideType.getType().toString(),leftSideType.getType().toString());
            rightSideType = termPrime(rightSideType);
            return additiveExpressionPrime(rightSideType);
        } else if (tokenList.get(walker).getType() == TokenType.ID) {
            Token rightSideType = call(accept("id"));
            semanticsTable.checkTypes(rightSideType.getType().toString(), leftSideType.getType().toString());
            rightSideType = termPrime(leftSideType);
            return additiveExpressionPrime(rightSideType);
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
            String op = addop();
            Token rightSideType = term();
            semanticsTable.checkTypes(leftSideType.getType().toString(),rightSideType.getType().toString());
            String t = getNewT();
            output.add(new Code(lineCount++,op,leftSideType.toString(),rightSideType.toString(),t));
            return additiveExpressionPrime(new Token(t,rightSideType.getType()));
        }else if(tokenList.get(walker).toString().equals("<=")|| tokenList.get(walker).toString().equals("<")|| tokenList.get(walker).toString().equals(">")|| tokenList.get(walker).toString().equals(">=")||
                tokenList.get(walker).toString().equals("==")|| tokenList.get(walker).toString().equals("!=")|| tokenList.get(walker).toString().equals(";")|| tokenList.get(walker).toString().equals(")")||
                tokenList.get(walker).toString().equals("]")|| tokenList.get(walker).toString().equals(","))
            return leftSideType;
        else{
            reject();
            return null;//This will never be reached
        }
    }

    private String addop(){
        if(tokenList.get(walker).toString().equals("+")){
            accept("+");
            return("add");
        }else if(tokenList.get(walker).toString().equals("-")){
            accept("-");
            return("sub");
        }else
            return reject().toString();
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
            String op = mulop();
            Token rightSideType = factor();
            semanticsTable.checkTypes(leftSideType.getType().toString(),rightSideType.getType().toString());
            String t = getNewT();
            output.add(new Code(lineCount++,op,leftSideType.toString(),rightSideType.toString(),t));
            return termPrime(new Token(t,rightSideType.getType()));
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

    private String mulop(){
        if (tokenList.get(walker).toString().equals("*")){
            accept("*");
            return("mult");
        }else if (tokenList.get(walker).toString().equals("/")){
            accept("/");
            return ("div");
        }else
            return reject().toString();
    }

    private Token call(Token idToken){
        if (tokenList.get(walker).toString().equals("(")){
            String t = getNewT();
            output.add(new Code(lineCount++,"call",idToken.toString(),"",t));
            accept("(");
            args(idToken);
            accept(")");
            return new Token(t,semanticsTable.getReturnTypeFromId(idToken).getType());
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
            output.add(new Code(lineCount++,"arg","","",returnType.toString()));
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

    public String getNewT(){
        return "t"+(tCount++);
    }

    private Token reject(){
       // System.out.println("REJECTING: "+tokenList.get(walker).toString()+" Type: "+tokenList.get(walker).getType());

        System.out.println("REJECT");
        System.exit(0);
        //This will never be reached
        return null;
    }



}
