
package cubic;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import org.json.JSONObject;
import org.json.JSONArray;

public class OpenAiClient {

    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";
    private static final String API_KEY_PATH = System.getenv("HOME") + "/.openai-api-key";

    private final String key;

    public OpenAiClient() throws IOException {
        this.key = Files.readString(Path.of(API_KEY_PATH)).trim();
    }

    public String getChatCompletion(String prompt) throws IOException {
        URL url = new URL(OPENAI_API_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        // Set request headers
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Authorization", "Bearer " + key);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        // Create request body
        JSONObject message = new JSONObject();
        message.put("role", "user");
        message.put("content", prompt);

        JSONArray messages = new JSONArray();
        messages.put(message);

        JSONObject requestBody = new JSONObject();
        requestBody.put("model", "gpt-4");  // or "gpt-3.5-turbo"
        requestBody.put("messages", messages);
        requestBody.put("temperature", 0.7);

        // Write request
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = requestBody.toString().getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        // Read response
        StringBuilder response;
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), "utf-8"))) {
            response = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line.trim());
            }
        }

        // Parse response JSON
        JSONObject jsonResponse = new JSONObject(response.toString());
        return jsonResponse
                .getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content");
    }

}
