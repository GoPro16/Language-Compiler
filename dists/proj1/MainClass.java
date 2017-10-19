
import java.io.File;
import java.io.IOException;

public class MainClass {
    public static void main(String[] args) throws IOException {
        SymbolTable.initTable(40, "symbols");
        //SymbolTable.display();
        new Program(new File("file"));
    }//end main
}
