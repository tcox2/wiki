package cubic;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RepositoryFactory {

    private static final Pattern PATTERN = Pattern.compile("https?://github\\.com/([^/]+)/([^/]+)(?:/)?");

    public static Repository from(String url) {
        url = url.trim();
        Matcher matcher = PATTERN.matcher(url);

        if (matcher.matches()) {
            String owner = matcher.group(1);
            String repo = matcher.group(2);
            return new Repository(owner, repo);
        } else {
            throw new IllegalArgumentException("Unable to parse url: " + url);
        }

    }
}
