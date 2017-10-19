import java.util.ArrayList;

public class Parser{

    private ArrayList<Token> tokenList;
    private int walker = 0;

    public Parser(ArrayList<Token> tokenList){
        this.tokenList = tokenList;
        tokenList.add(new Token("END", TokenType.END));
    }

    public void parse(){
        program();
        System.out.println("ACCEPT");
    }

    private void program(){
        declarationList();
    }

    private void declarationList(){
        declaration();
        declarationListPrime();
    }

    private void declaration(){
        if(tokenList.get(walker).toString().equals("int") || tokenList.get(walker).toString().equals("float") || tokenList.get(walker).toString().equals("void")){
            typeSpecifier();
            accept("id");
            declarationPrime();
        }else
            reject();
    }

    private void declarationListPrime(){
        if(tokenList.get(walker).toString().equals("int")|| tokenList.get(walker).toString().equals("float")|| tokenList.get(walker).toString().equals("void")){
            declaration();
            declarationListPrime();
        }
    }

    private void declarationPrime(){
        if(tokenList.get(walker).toString().equals(";")|| tokenList.get(walker).toString().equals("[")){
            specifier();
        }else if(tokenList.get(walker).toString().equals("(")){
            accept("(");
            params();
            accept(")");
            compoundStmt();
        }else
            reject();
    }

    private void specifier(){
        if(tokenList.get(walker).toString().equals(";")){
            accept(";");
        }else if(tokenList.get(walker).toString().equals("[")){
            accept("[");
            accept("num");
            accept("]");
            accept(";");
        }else
            reject();
    }

    private void typeSpecifier(){
        if(tokenList.get(walker).toString().equals("int")){
            accept("int");
        }else if(tokenList.get(walker).toString().equals("float")){
            accept("float");
        }else if(tokenList.get(walker).toString().equals("void")){
            accept("void");
        }else
            reject();
    }

    private void params(){
        if(tokenList.get(walker).toString().equals("int")){
            accept("int");
            accept("id");
            paramPrime();
            paramListPrime();
        }else if(tokenList.get(walker).toString().equals("float")){
            accept("float");
            accept("id");
            paramPrime();
            paramListPrime();
        }else if(tokenList.get(walker).toString().equals("void")){
            accept("void");
            paramList();
        }else
            reject();
    }

    private void paramList(){
        if(tokenList.get(walker).getType() == TokenType.ID){
            accept("id");
            paramPrime();
            paramListPrime();
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
            typeSpecifier();
            accept("id");
            paramPrime();
        }else
            reject();
    }


    private void paramPrime(){
        if(tokenList.get(walker).toString().equals("[")){
            accept("[");
            accept("]");
        }else if(tokenList.get(walker).toString().equals(",")|| tokenList.get(walker).toString().equals(")"))
            return;
        else
            reject();
    }

    private void compoundStmt(){
        if (tokenList.get(walker).toString().equals("{")){
            accept("{");
            localDeclarations();
            statementList();
            accept("}");
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
            typeSpecifier();
            accept("id");
            specifier();
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
            expression();
            accept(";");
        }else if(tokenList.get(walker).toString().equals(";")){
            accept(";");
        }else
            reject();
        return;
    }

    private void expression(){
        if(tokenList.get(walker).getType() == TokenType.ID){
            accept("id");
            var();
        }else if(tokenList.get(walker).toString().equals("(")){
            accept("(");
            expression();
            accept(")");
            termPrime();
            additiveExpressionPrime();
            relopExpression();
        }else if(tokenList.get(walker).getType() == TokenType.NUM){
            accept("num");
            termPrime();
            additiveExpressionPrime();
            relopExpression();
        }else
            reject();
    }

    private void var(){
        if(tokenList.get(walker).toString().equals("[")|| tokenList.get(walker).toString().equals("=")|| tokenList.get(walker).toString().equals("*")|| tokenList.get(walker).toString().equals("/")||
                tokenList.get(walker).toString().equals("+")|| tokenList.get(walker).toString().equals("-")|| tokenList.get(walker).toString().equals("<=")|| tokenList.get(walker).toString().equals("<")||
                tokenList.get(walker).toString().equals(">")|| tokenList.get(walker).toString().equals(">=")|| tokenList.get(walker).toString().equals("==")|| tokenList.get(walker).toString().equals("!=")){
            varArr();
            varPrime();
        }else if(tokenList.get(walker).toString().equals("(")){
            accept("(");
            args();
            accept(")");
            termPrime();
            additiveExpressionPrime();
            relopExpression();
        }else if(tokenList.get(walker).toString().equals(";")|| tokenList.get(walker).toString().equals(")")|| tokenList.get(walker).toString().equals("]")|| tokenList.get(walker).toString().equals(",")){
            varArr();
            varPrime();
        }else
            reject();
    }

    private void varPrime(){
        if(tokenList.get(walker).toString().equals("=")){
            accept("=");
            expression();
        }else if(tokenList.get(walker).toString().equals("*")|| tokenList.get(walker).toString().equals("/")|| tokenList.get(walker).toString().equals("+")|| tokenList.get(walker).toString().equals("-")||
                tokenList.get(walker).toString().equals("<=")|| tokenList.get(walker).toString().equals("<")|| tokenList.get(walker).toString().equals(">")|| tokenList.get(walker).toString().equals(">=")||
                tokenList.get(walker).toString().equals("==")|| tokenList.get(walker).toString().equals("!=")){
            termPrime();
            additiveExpressionPrime();
            relopExpression();
        }else if(tokenList.get(walker).toString().equals(";")|| tokenList.get(walker).toString().equals(")")|| tokenList.get(walker).toString().equals("]")|| tokenList.get(walker).toString().equals(",")){
            termPrime();
            additiveExpressionPrime();
            relopExpression();
        }else
            reject();
    }

    private void varArr(){
        if(tokenList.get(walker).toString().equals("[")){
            accept("[");
            expression();
            accept("]");
        }else if(tokenList.get(walker).toString().equals("=")|| tokenList.get(walker).toString().equals("*")|| tokenList.get(walker).toString().equals("/")|| tokenList.get(walker).toString().equals("+")||
                tokenList.get(walker).toString().equals("-")|| tokenList.get(walker).toString().equals("<=") || tokenList.get(walker).toString().equals("<") || tokenList.get(walker).toString().equals(">")||
                tokenList.get(walker).toString().equals(">=")|| tokenList.get(walker).toString().equals("==")|| tokenList.get(walker).toString().equals("!=")|| tokenList.get(walker).toString().equals(";")||
                tokenList.get(walker).toString().equals(")")|| tokenList.get(walker).toString().equals("]")|| tokenList.get(walker).toString().equals(","))
            return;
        else
            reject();
    }

    private void relopExpression(){
        if(tokenList.get(walker).toString().equals("<=")|| tokenList.get(walker).toString().equals("<")|| tokenList.get(walker).toString().equals(">")|| tokenList.get(walker).toString().equals(">=")||
                tokenList.get(walker).toString().equals("==")|| tokenList.get(walker).toString().equals("!=")){
            relop();
            additiveExpression();
        }else if(tokenList.get(walker).toString().equals(";")|| tokenList.get(walker).toString().equals(")")|| tokenList.get(walker).toString().equals("]")|| tokenList.get(walker).toString().equals(","))
            return;
        else
            reject();
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

    private void additiveExpression(){
        if(tokenList.get(walker).toString().equals("(")){
            accept("(");
            expression();
            accept(")");
            termPrime();
            additiveExpressionPrime();
        }else if(tokenList.get(walker).getType() == TokenType.ID){
            accept("id");
            call();
            termPrime();
            additiveExpressionPrime();
        }else if(tokenList.get(walker).getType() == TokenType.NUM){
            accept("num");
            termPrime();
            additiveExpressionPrime();
        }else
            reject();
    }

    private void additiveExpressionPrime(){
        if(tokenList.get(walker).toString().equals("+")|| tokenList.get(walker).toString().equals("-")){
            addop();
            term();
            additiveExpressionPrime();
        }else if(tokenList.get(walker).toString().equals("<=")|| tokenList.get(walker).toString().equals("<")|| tokenList.get(walker).toString().equals(">")|| tokenList.get(walker).toString().equals(">=")||
                tokenList.get(walker).toString().equals("==")|| tokenList.get(walker).toString().equals("!=")|| tokenList.get(walker).toString().equals(";")|| tokenList.get(walker).toString().equals(")")||
                tokenList.get(walker).toString().equals("]")|| tokenList.get(walker).toString().equals(","))
            return;
        else
            reject();
    }

    private void addop(){
        if(tokenList.get(walker).toString().equals("+")){
            accept("+");
        }else if(tokenList.get(walker).toString().equals("-")){
            accept("-");
        }else
            reject();
    }

    private void term(){
        if(tokenList.get(walker).toString().equals("(")){
            accept("(");
            expression();
            accept(")");
            termPrime();
        }else if(tokenList.get(walker).getType() == TokenType.ID){
            accept("id");
            call();
            termPrime();
        }else if(tokenList.get(walker).getType() == TokenType.NUM){
            accept("num");
            termPrime();
        } else
            reject();
    }

    private void termPrime(){
        if(tokenList.get(walker).toString().equals("*")|| tokenList.get(walker).toString().equals("/")){
            mulop();
            factor();
            termPrime();
        }else if(tokenList.get(walker).toString().equals("<=")|| tokenList.get(walker).toString().equals("<")|| tokenList.get(walker).toString().equals(">")|| tokenList.get(walker).toString().equals(">=")||
                tokenList.get(walker).toString().equals("==")|| tokenList.get(walker).toString().equals("!=")|| tokenList.get(walker).toString().equals("+")|| tokenList.get(walker).toString().equals("-")||
                tokenList.get(walker).toString().equals(";")|| tokenList.get(walker).toString().equals(")")|| tokenList.get(walker).toString().equals("]")|| tokenList.get(walker).toString().equals(","))
            return;
        else
            reject();
    }

    private void factor(){
        if (tokenList.get(walker).toString().equals("(")){
            accept("(");
            expression();
            accept(")");
        }else if(tokenList.get(walker).getType() == TokenType.ID){
            accept("id");
            call();
        }else if(tokenList.get(walker).getType() == TokenType.NUM){
            accept("num");
        }else
            reject();
    }

    private void mulop(){
        if (tokenList.get(walker).toString().equals("*")){
            accept("*");
        }else if (tokenList.get(walker).toString().equals("/")){
            accept("/");
        }else
            reject();
    }

    private void call(){
        if (tokenList.get(walker).toString().equals("(")){
            accept("(");
            args();
            accept(")");
        }else if (tokenList.get(walker).toString().equals("[")){
            varArr();
        }else if(tokenList.get(walker).toString().equals("+")|| tokenList.get(walker).toString().equals("-")|| tokenList.get(walker).toString().equals("*")|| tokenList.get(walker).toString().equals("/")||
                tokenList.get(walker).toString().equals("<=")|| tokenList.get(walker).toString().equals("<")|| tokenList.get(walker).toString().equals(">")|| tokenList.get(walker).toString().equals(">")||
                tokenList.get(walker).toString().equals(">=")|| tokenList.get(walker).toString().equals("==")|| tokenList.get(walker).toString().equals("!=")|| tokenList.get(walker).toString().equals(";")||
                tokenList.get(walker).toString().equals(")")|| tokenList.get(walker).toString().equals("]")|| tokenList.get(walker).toString().equals(",")){
            varArr();
        }else
            reject();
    }

    private void args(){
        if(tokenList.get(walker).getType() == TokenType.ID || tokenList.get(walker).toString().equals("(")|| tokenList.get(walker).getType() == TokenType.NUM){
            argList();
        }else if (tokenList.get(walker).toString().equals(")"))
            return;
        else
            reject();
    }

    private void argList(){
        if(tokenList.get(walker).getType() == TokenType.ID || tokenList.get(walker).toString().equals("(")|| tokenList.get(walker).getType() == TokenType.NUM){
            expression();
            argListPrime();
        }else
            reject();
    }

    private void argListPrime(){
        if(tokenList.get(walker).toString().equals(",")){
            accept(",");
            expression();
            argListPrime();
        }else if(tokenList.get(walker).toString().equals(")"))
            return;
        else
            reject();
    }

    private void accept(String value){
        if(!(tokenList.get(walker).equals(null))){
            switch(tokenList.get(walker).getType()) {
                case ID:
                    if (!(value.equals("id")))
                        reject();
                    break;
                case FLOAT:
                    if (!(value.equals("num")))
                        reject();
                    break;
                case NUM:
                    if (!(value.equals("num")))
                        reject();
                    break;
                default:
                    if (!(value.equals(tokenList.get(walker).toString())))
                        reject();
                    break;
            }//switch to check valid input
        }else
            reject();
        walker+=1;
    }

    private void reject(){
        System.out.println("REJECT");
        System.exit(0);
    }



}
