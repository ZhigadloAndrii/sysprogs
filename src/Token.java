public class Token {
    public final String lexeme;
    public final TokenClass tokenClass;
    public final String forwardInput;

    Token(String aLexeme, TokenClass aTokenClass, String aForwardInput)
    {
        lexeme = aLexeme;
        tokenClass = aTokenClass;
        forwardInput = aForwardInput;
    }
}
