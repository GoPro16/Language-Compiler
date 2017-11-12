import java.io.File;
import java.io.IOException;

public class MainClass {
    public static void main(String[] args) throws IOException {
        Program p1 = new Program(new File(args[0]));
        //p1.printList();
        Parser parser = new Parser(p1.getTokenList(),p1.getLineCount());
        parser.parse();
    }//end main
}
