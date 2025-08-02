
import cubic.Repository;
import cubic.RepositoryFactory;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class RepositoryFactoryTest {

    @Test
    void from() {
        Repository actual = RepositoryFactory.from("https://github.com/monkey/banana");
        Repository expected = new Repository("monkey", "banana");
        assertThat(actual, equalTo(expected));
    }

}