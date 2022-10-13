import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class FileCharSource implements CharSource {

    private final BufferedReader br;
    private boolean mayHaveNext;

    {
        mayHaveNext = true;
    }

    public FileCharSource(FileReader fileReader)
    {
        br = new BufferedReader(fileReader);
    }

    @Override
    public boolean hasNext()
    {
        if (!mayHaveNext){
            return false;
        }

        try {
            return br.ready();
        } catch (IOException ex) {
            return false;
        }
    }

    @Override
    public int next()
    {
        try {
            return br.read();
        } catch (IOException ex) {
            mayHaveNext = false;
            return '\0';
        }
    }
}