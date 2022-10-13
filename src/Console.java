import java.io.*;
public class Console {
    static public class TokenPrinter {
        static void print(Token token) throws FileNotFoundException {
            System.out.print("< " + token.lexeme + " > - ");
            System.out.println("< " +token.tokenClass.name +" >");

        }


    }
}

