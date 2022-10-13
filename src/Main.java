import org.json.*;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintStream;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;

public class Main {

    private static VirtualTokenizer readAutomatonTokenizer(Path automatonPath, Path codePath) throws FileNotFoundException, JSONException
    {
        final FileReader fr = new FileReader(automatonPath.toString());
        final JSONTokener tokener = new JSONTokener(fr);
        final JSONObject root = new JSONObject(tokener);

        final var keyWords = new HashSet<String>();
        ((JSONArray)root.get("key_words")).forEach(ob -> keyWords.add((String)ob) );

        final String startState = (String)root.get("start_sate");
        final FileCharSource codeFileSource = new FileCharSource(new FileReader(codePath.toString()));
        final AutomatonTokenizer tokenizer = new AutomatonTokenizer(codeFileSource, keyWords, startState);

        ((JSONArray)root.get("states")).forEach(ob -> {
            final var transitionInfo = (JSONObject)ob;
            final String stateName = transitionInfo.getString("state");
            final boolean isFinalState = transitionInfo.getBoolean("is_final_state");
            if (isFinalState) {
                final String tokenName = transitionInfo.getString("name");
                tokenizer.addState(stateName, new TokenClass(tokenName));
            } else {
                tokenizer.addState(stateName);
            }

        });

        ((JSONArray)root.get("transitions")).forEach(ob -> {
            final var transitionInfo = (JSONObject)ob;
            final String state = transitionInfo.getString("state");
            final CharChecker checker;
            final var input = transitionInfo.get("input");
            if (input instanceof JSONArray) {
                final Set<Integer> codePointSet = new HashSet<>();
                ((JSONArray) input).forEach(codeOb -> {
                    codePointSet.add(codeOb.toString().codePointAt(0));
                });
                checker = new SetCharChecker(codePointSet);
            }  else {
                checker = new RegexCharChecker(Pattern.compile(input.toString()));
            }
            final String moveTo = transitionInfo.getString("move_to");
            tokenizer.addTransition(state, checker, moveTo);
        });
        return tokenizer;
    }

    public static void main(String[] args) throws FileNotFoundException {
        PrintStream out = new PrintStream(new FileOutputStream("output.txt"));
        System.setOut(out);

        final Path automatonPath;
        final Path filePath;
        try {
            automatonPath = FileSystems.getDefault().getPath("automaton.json").toAbsolutePath();
            filePath = FileSystems.getDefault().getPath("test.c").toAbsolutePath();
        } catch (IllegalArgumentException ex) {
            System.err.println(ex.getLocalizedMessage());
            System.exit(1);
            return;
        }
        try {
            final VirtualTokenizer tokenizer = readAutomatonTokenizer(automatonPath, filePath);
            while (tokenizer.hasNext()) {
                Console.TokenPrinter.print(tokenizer.next());
            }
        } catch (Exception ex) {
            System.err.println(ex.getLocalizedMessage());
        }
    }
}
