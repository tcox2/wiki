
package cubic;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;
import org.eclipse.jetty.util.Fields;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ExecutorService;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

public class CubicHandler extends Handler.Abstract.NonBlocking {

    private final CubicMustache mustache = new CubicMustache();
    private final Map<Repository, Analysis> data;
    private final ExecutorService executor;
    private final OpenAiClient ai;

    public CubicHandler(Map<Repository, Analysis> data, ExecutorService executor, OpenAiClient ai) {
        this.data = data;
        this.executor = executor;
        this.ai = ai;
    }

    @Override
    public boolean handle(Request request, Response response, Callback callback) throws IOException {
        if (request.getHttpURI().getPath().equals("/")) {
            index(response, callback);
            return true;
        }

        if (request.getHttpURI().getPath().equals("/add")) {
            add(request, response, callback);
            return true;
        }

        if (request.getHttpURI().getPath().equals("/files")) {
            files(request, response, callback);
        }

        return false;
    }


    private void files(Request request, Response response, Callback callback) throws IOException {
        Fields fields = Request.extractQueryParameters(request);
        String url = String.valueOf(fields.getValue("repository"));
        url = new String(Base64.getDecoder().decode(url), StandardCharsets.UTF_8);
        Repository key = RepositoryFactory.from(url);
        response.setStatus(200);
        String bound = mustache.template("pages/files", Map.of(
                "repository", key.repository(),
                "owner", key.owner(),
                "files", filesAsList(data.get(key))
                ));
        response.write(true,
                ByteBuffer.wrap(bound.getBytes(StandardCharsets.UTF_8)),
                callback);
    }

    private static List<FileTableRow> filesAsList(Analysis analysis) {
        List<FileTableRow> out = new ArrayList<>();
        for (Map.Entry<Path, FileAnalysis> entry : analysis.files.entrySet()) {
            FileTableRow row = new FileTableRow();
            // (/foo/bar/baz, /foo) -> /bar/baz
            row.name = analysis.source.toPath().relativize(entry.getKey()).toString();
            row.status = String.valueOf(entry.getValue().status);
            row.description = entry.getValue().description;
            out.add(row);
        }
        return out;
    }

    private void add(Request request, Response response, Callback callback) throws IOException {
        Fields fields = Request.extractQueryParameters(request);
        Object url = fields.getValue("url");
        assertThat(url, notNullValue());
        Repository key = RepositoryFactory.from(String.valueOf(url));
        Analysis analysis = new Analysis(key);
        data.put(key, analysis);
        executor.submit(new CheckoutTask(analysis, executor, ai, mustache));
        response.setStatus(200);
        String bound = mustache.template("pages/add", Map.of("url", url));
        response.write(true,
                ByteBuffer.wrap(bound.getBytes(StandardCharsets.UTF_8)),
                callback);
    }

    private void index(Response response, Callback callback) throws IOException {
        Map<String, Object> values = new HashMap<>();
        values.put("repositories", repositoriesAsList(data));
        response.setStatus(200);
        response.write(true,
                ByteBuffer.wrap(mustache.template("pages/index", values).getBytes(StandardCharsets.UTF_8)),
                callback);
    }

    private static List<RepositoryTableRow> repositoriesAsList(Map<Repository, Analysis> data) {
        List<RepositoryTableRow> rows = new ArrayList<>();
        for (Map.Entry<Repository, Analysis> entry : data.entrySet()) {
            RepositoryTableRow row = new RepositoryTableRow();
            row.owner = entry.getKey().owner();
            row.repository = entry.getKey().repository();
            row.status = String.valueOf(entry.getValue().status);
            row.files = entry.getValue().files.isEmpty() ? "" : String.valueOf(entry.getValue().files.size());
            row.link = "/files?repository=" + entry.getKey().toBase64();
            rows.add(row);
        }
        return rows;
    }

}

