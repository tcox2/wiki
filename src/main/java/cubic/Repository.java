package cubic;

import org.apache.commons.codec.binary.Base64;

import java.nio.charset.StandardCharsets;

public record Repository(String owner, String repository) implements Comparable<Repository> {

    public Repository {
        if (isBlank(owner)) {
            throw new IllegalArgumentException("owner");
        }
        if (isBlank(repository)) {
            throw new IllegalArgumentException("repository");
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    @Override
    public int compareTo(Repository that) {
        int comparison = this.owner.compareTo(that.owner);
        if (comparison != 0) {
            return comparison;
        }
        return this.repository.compareTo(that.repository);
    }

    public String toBase64() {
        String url = "https://github.com/" + owner + "/" + repository;
        return Base64.encodeBase64String(url.getBytes(StandardCharsets.UTF_8));
    }
}
