import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Scanner;

class GroqClient {

    private static final String SYSTEM_PROMPT = """
        You are ARIN (Adaptive Reasoning Intelligent Assistant), the core intelligence of HAB (Human-like Assistant Brain).

        You are a capable personal AI assistant designed to help the user solve problems, learn new concepts, analyze information, and automate workflows.

        You communicate clearly, professionally, and efficiently.

        Guidelines:
        - Give Short Replies.
        - Be accurate and honest.
        - Never fabricate information.
        - Admit uncertainty when appropriate.
        - Maintain conversational context.
        - Prioritize useful, actionable responses.
        - Provide technical depth when needed.
        - Adapt your response style to the user's request.
        - Support software development, cybersecurity learning, research, and productivity tasks.

        Your role is to assist, not to make decisions on behalf of the user.
        """;

    private final HttpClient client = HttpClient.newHttpClient();
    private final Scanner scanner = new Scanner(System.in);
    

    /**
     * Runs one turn: reads input, calls Groq, returns the reply text.
     * Returns null when the user wants to exit.
     */
    public String requestToGroq() throws Exception {
        String apiKey = System.getenv("GROQ_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            return "Set GROQ_API_KEY environment variable first.";
        }

        System.out.print("YOU >> ");
        String userInput = scanner.nextLine();
        System.out.print("\n");

        if (userInput.equalsIgnoreCase("exit")) {
            return null;
        }

        String jsonBody = "{\"model\":\"llama-3.3-70b-versatile\",\"messages\":["
                + "{\"role\":\"system\",\"content\":\"" + escapeJson(SYSTEM_PROMPT) + "\"},"
                + "{\"role\":\"user\",\"content\":\"" + escapeJson(userInput) + "\"}"
                + "]}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.groq.com/openai/v1/chat/completions"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        String body = response.body();

        if (response.statusCode() != 200) {
            return "Groq API error (HTTP " + response.statusCode() + "): " + body;
        }

        String content = extractContent(body);
        if (content == null) {
            return "Couldn't parse Groq response. Raw body:\n" + body;
        }

        return content;
    }

    public void close() {
        scanner.close();
    }

    /** Escapes a string for safe embedding in a JSON string literal, per the JSON spec. */
    private static String escapeJson(String s) {
        StringBuilder sb = new StringBuilder(s.length() + 16);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                case '\b' -> sb.append("\\b");
                case '\f' -> sb.append("\\f");
                default -> {
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        return sb.toString();
    }

    /**
     * Finds the first "content":"..." field and decodes JSON escapes properly,
     * including \\uXXXX sequences and escaped backslashes.
     * Returns null if it can't find/parse the field.
     */
    private static String extractContent(String body) {
        String marker = "\"content\":\"";
        int start = body.indexOf(marker);
        if (start == -1) {
            return null;
        }
        start += marker.length();

        StringBuilder decoded = new StringBuilder();
        int i = start;
        while (i < body.length()) {
            char c = body.charAt(i);
            if (c == '"') {
                break;
            }
            if (c == '\\' && i + 1 < body.length()) {
                char next = body.charAt(i + 1);
                switch (next) {
                    case '"' -> { decoded.append('"'); i += 2; }
                    case '\\' -> { decoded.append('\\'); i += 2; }
                    case 'n' -> { decoded.append('\n'); i += 2; }
                    case 'r' -> { decoded.append('\r'); i += 2; }
                    case 't' -> { decoded.append('\t'); i += 2; }
                    case 'b' -> { decoded.append('\b'); i += 2; }
                    case 'f' -> { decoded.append('\f'); i += 2; }
                    case '/' -> { decoded.append('/'); i += 2; }
                    case 'u' -> {
                        if (i + 6 <= body.length()) {
                            String hex = body.substring(i + 2, i + 6);
                            decoded.append((char) Integer.parseInt(hex, 16));
                            i += 6;
                        } else {
                            i += 2;
                        }
                    }
                    default -> { decoded.append(next); i += 2; }
                }
            } else {
                decoded.append(c);
                i++;
            }
        }
        if (i >= body.length()) {
            return null;
        }
        return decoded.toString();
    }
}