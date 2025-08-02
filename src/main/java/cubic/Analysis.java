package cubic;

import java.io.File;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;

public class Analysis {

    final Repository repository;
    public File source; // directory
    Status status = Status.PENDING;

    final Map<Path, FileAnalysis> files = new TreeMap<>();

    public Analysis(Repository repository) {
        this.repository = repository;
    }

    public void setError(Throwable t) {
        status = Status.ERROR;
        t.printStackTrace(System.err);
    }

}
