
import java.util.*;

public class AutomatonTokenizer implements VirtualTokenizer {

    private static class Automaton {

        private static class State {
            final public String name;
            final public TokenClass tokenClass;

            State(String aName, TokenClass aTokenClass){
                name = aName;
                tokenClass = aTokenClass;
            }

            public boolean isFinal(){
                return tokenClass != null;
            }

            @Override
            public int hashCode()
            {
                return name.hashCode();
            }

            @Override
            public boolean equals(Object obj)
            {
                if (obj instanceof String) {
                    return name.equals((String)obj);
                }
                if (obj instanceof State) {
                    return name.equals(((State)obj).name);
                }
                return false;
            }
        }

        private static class TransitionData {
            public final State toState;
            public final CharChecker checker;

            TransitionData(State aToState, CharChecker aChecker)
            {
                toState = aToState;
                checker = aChecker;
            }
        }

        final State startState;
        State currentState;
        final Map<State, List<TransitionData>> transitions;
        final Map<String, State> states;

        final StringBuilder currentInput;
        final StringBuilder currentForwardInput;
        Queue<Token> parsedTokens;

        {
            transitions = new HashMap<>();
            states = new HashMap<>();
            currentInput = new StringBuilder();
            currentForwardInput = new StringBuilder();
            parsedTokens = new LinkedList<>();
        }

        Automaton(String aStartState)
        {
            addState(aStartState);
            currentState = startState = states.get(aStartState);
        }

        public void addState(String name)
        {
            addState(name, null);
        }

        public void addState(String name, TokenClass tokenClass)
        {
            states.put(name, new State(name, tokenClass));
            transitions.put(states.get(name), new ArrayList<>());
        }

        public void addTransition(String fromStateStr, CharChecker checker, String toStateStr)
        {
            final State fromState = states.get(fromStateStr);
            if (fromState == null) throw new IllegalStateException("AutomatonTokenizer.Automaton.addTransition, " +
                                                                   "has no state with name fromStateStr");
            final State toState = states.get(toStateStr);
            if (toState == null) throw new IllegalStateException("AutomatonTokenizer.Automaton.addTransition, " +
                                                                 "has no state with name toStateStr");
            final List<TransitionData> trData = transitions.get(fromState);
            Objects.requireNonNull(trData).add(new TransitionData(toState, checker));
        }

        boolean hasToken()
        {
            return !parsedTokens.isEmpty();
        }

        Token getToken()
        {
            if (!parsedTokens.isEmpty()) {
                return parsedTokens.remove();
            }
            return createToken();
        }

        void put(int codePoint)
        {
            if (!transitions.containsKey(currentState)) throw new IllegalStateException("AutomatonTokenizer.Automaton.put, " +
                                                                                        "transition set do not contains currentState key");
            TransitionData trData = null;
            for (var data : transitions.get(currentState)) {
                if (data.checker.check(codePoint)) {
                    trData = data;
                    break;
                }
            }
            if (trData == null) {
                if (currentState.isFinal()) {
                    parsedTokens.add(createToken());
                    clearInputs();
                    put(codePoint);
                } else {
                    currentInput.appendCodePoint(codePoint);
                    currentState = startState;
                    parsedTokens.add(createToken());
                }
                return;
            }
            final var prevState = currentState;
            currentState = trData.toState;
            if (prevState.equals(startState) && trData.toState.equals(startState)){
                currentForwardInput.appendCodePoint(codePoint);
                return;
            }
            currentInput.appendCodePoint(codePoint);
        }

        private void clearInputs()
        {
            currentInput.setLength(0);
            currentForwardInput.setLength(0);
            currentState = startState;
        }

        private Token createToken(){
            if (currentInput.isEmpty()) throw new IllegalStateException("AutomatonTokenizer.Automaton.getToken, currentInput is empty");
            final String lexeme = currentInput.toString();
            final String forwardInput = currentForwardInput.toString();
            final State state = currentState;
            clearInputs();
            return new Token(lexeme, state.isFinal() ? state.tokenClass : TokenClass.unrecognizedLexeme, forwardInput);
        }

    }

    private final Automaton automaton;
    private final CharSource source;
    private final Set<String> keyWords;
    private Token currentToken;

    {
        currentToken = null;
    }

    AutomatonTokenizer(CharSource aCharSource, Set<String> aKeyWords, String aStartState)
    {
        automaton = new Automaton(aStartState);
        source = aCharSource;
        keyWords = aKeyWords;
    }

    public boolean hasNext()
    {
        if (currentToken == null) {
            readNext();
        }
        return currentToken != null;
    }

    public Token next()
    {
        if (currentToken == null) {
            readNext();
        }
        if (currentToken == null) throw new IllegalStateException("AutomatonTokenizer.next, currentToken == null");
        var nextToken = currentToken;
        currentToken = null;
        return nextToken;
    }

    public void addState(String name)
    {
        automaton.addState(name);
    }

    public void addState(String name, TokenClass tokenClass)
    {
        automaton.addState(name, tokenClass);
    }

    public void addTransition(String fromState, CharChecker checker, String toState)
    {
        automaton.addTransition(fromState, checker, toState);
    }

    private void readNext()
    {
        if (!source.hasNext()) { return; }
        while (source.hasNext() && !automaton.hasToken()) {
            automaton.put(source.next());
        }
        var token = automaton.getToken();
        currentToken = keyWords.contains(token.lexeme) ? new Token(token.lexeme, TokenClass.keyWord, token.forwardInput)
                                                       : token;
    }
}
