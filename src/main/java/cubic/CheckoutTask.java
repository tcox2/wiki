
package cubic;

import org.eclipse.jgit.api.Git;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class CheckoutTask implements Runnable {

    private static final int MAX_DEPTH = 10;
    private final Analysis analysis;
    private final ExecutorService executor;
    private final OpenAiClient ai;
    private final CubicMustache mustache;

    public CheckoutTask(Analysis analysis, ExecutorService executor, OpenAiClient ai, CubicMustache mustache) {
        this.analysis = analysis;
        this.executor = executor;
        this.ai = ai;
        this.mustache = mustache;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    public void run() {
        try {
            analysis.status = Status.CLONING;
            File temp = File.createTempFile("cubic", "git");
            temp.delete();
            temp.mkdirs();

            //noinspection resource
            Git.cloneRepository()
                    .setURI("https://github.com/" + analysis.repository.owner() + "/" + analysis.repository.repository())
                    .setDirectory(temp.getAbsoluteFile())
                    .call();

            analysis.source = temp;
            analysis.status = Status.CLONED;

            sources(analysis.source.toPath(), new Consumer<Path>() {
                @Override
                public void accept(Path path) {
                    FileAnalysis fileAnalysis = new FileAnalysis();
                    analysis.files.put(path, fileAnalysis);
                    executor.submit(new InferenceTask(path, fileAnalysis, ai, mustache));

                }
            });

        } catch (Throwable t) {
            analysis.setError(t);
        }
    }

    private static void sources(Path source, Consumer<Path> sink) throws IOException {
        try (Stream<Path> stream = Files.walk(source, MAX_DEPTH)) {
            stream.forEach(path -> {
                if (Files.isDirectory(path)) {
                    return;
                }
                if (path.toAbsolutePath().toString().contains("/.git/")) {
                    return;
                }
                sink.accept(path);
            });
        }
    }
}
