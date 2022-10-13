public class TokenClass {
    final String name;

    // hard code
    final public static TokenClass keyWord = new TokenClass("Key word");
    final public static TokenClass unrecognizedLexeme = new TokenClass("Unrecognized lexeme");

    TokenClass(String aName)
    {
        name = aName;
    }
}
