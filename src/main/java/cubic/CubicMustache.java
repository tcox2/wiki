package cubic;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

import java.io.*;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

public class CubicMustache {

    private final MustacheFactory factory = new DefaultMustacheFactory();
    private final ClassLoader loader = CubicMustache.class.getClassLoader();

    public String template(String name, Map<String, Object> values) throws IOException {
        try (InputStream stream = loader.getResourceAsStream(name + ".mustache")) {
            assertThat(stream, notNullValue());
            try (Reader reader = new InputStreamReader(stream)) {
                Mustache compiled = factory.compile(reader, name);
                StringWriter out = new StringWriter();
                compiled.execute(out, values);
                return out.toString();
            }
        }
    }
}
