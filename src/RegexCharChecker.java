import java.util.regex.Pattern;

public class RegexCharChecker implements CharChecker {
    private final Pattern pattern;

    RegexCharChecker(Pattern aPattern)
    {
        pattern = aPattern;
    }

    public boolean check(int codePoint)
    {
        return pattern.matcher(Character.toString(codePoint)).find();
    }
}
