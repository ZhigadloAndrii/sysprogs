import java.util.Set;

public class SetCharChecker implements CharChecker {
    private final Set<Integer> codes;

    SetCharChecker(Set<Integer> aCodes)
    {
        codes = aCodes;
    }

    public boolean check(int codePoint)
    {
        return codes.contains(codePoint);
    }
}
