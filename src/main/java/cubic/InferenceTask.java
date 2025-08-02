
package cubic;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class InferenceTask implements Runnable {

    private static final int MAX_SIZE = 50 * 1024; // 50k

    private final Path path;
    private final FileAnalysis analysis;
    private final OpenAiClient ai;
    private final CubicMustache mustache;

    public InferenceTask(Path path, FileAnalysis analysis, OpenAiClient ai, CubicMustache mustache) {
        this.path = path;
        this.analysis = analysis;
        this.ai = ai;
        this.mustache = mustache;
    }

    @Override
    public void run() {
        try {
            analysis.status = FileAnalysisStatus.READING;
            if (Files.size(path) > MAX_SIZE) {
                analysis.status = FileAnalysisStatus.TOO_BIG;
                return;
            }
            byte[] data = Files.readAllBytes(path);
            analysis.status = FileAnalysisStatus.INFERENCING;
            String encoded = Base64.getEncoder().encodeToString(data);
            Map<String, Object> values = new HashMap<>();
            values.put("encoded", encoded);
            String bound = mustache.template("prompts/file", values);
            String result = ai.getChatCompletion(bound);
            analysis.status = FileAnalysisStatus.RESULT_AVAILABLE;
            analysis.description = result;
        } catch (Throwable e) {
            analysis.status = FileAnalysisStatus.ERROR;
            e.printStackTrace(System.err);
        }
    }
}
