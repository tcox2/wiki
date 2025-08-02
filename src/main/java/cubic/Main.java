
package cubic;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

    public static void main(String[] args) throws Exception {
        Map<Repository, Analysis> data = new ConcurrentSkipListMap<>();
        try (ExecutorService executor = Executors.newSingleThreadExecutor()) {
            OpenAiClient ai = new OpenAiClient();
            Server server = new Server(new InetSocketAddress("localhost", 8080));
            ContextHandlerCollection handlers = new ContextHandlerCollection();
            handlers.addHandler(new CubicHandler(data, executor, ai));
            server.setHandler(handlers);
            server.start();
            System.out.println("Server started at http://localhost:8080/");
            server.join();
        }
    }

}