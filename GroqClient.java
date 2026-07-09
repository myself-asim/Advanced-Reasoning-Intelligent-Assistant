import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Scanner;

public class GroqClient {

    public static void main(String[] args) throws Exception {
        String apiKey = System.getenv("GROQ_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            System.err.println("Set GROQ_API_KEY environment variable first.");
            return;
        }

        HttpClient client = HttpClient.newHttpClient();
        Scanner scanner = new Scanner(System.in);

        System.out.println("Type your message (or 'exit' to quit):");

        while (true) {
            System.out.print("YOU >> ");
            String userInput = scanner.nextLine();
            System.out.print("\n");
             
            if (userInput.equalsIgnoreCase("exit")) {
                break;
            }

            String escapedInput = userInput
                    .replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n");

            String fixedText = """
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

        String escapedFixedText = fixedText
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n");

        String jsonBody = """
                {
                "model": "llama-3.3-70b-versatile",
                "messages": [
                    {"role": "system", "content": "%s"},
                    {"role": "user", "content": "%s"}
                ]
                }
                """.formatted(escapedFixedText, escapedInput);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.groq.com/openai/v1/chat/completions"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String body = response.body();

            String marker = "\"content\":\"";
            int start = body.indexOf(marker);
            if (start == -1) {
                System.out.println("Couldn't find content field. Raw response:");
                System.out.println(body);
                continue;
            }
            start += marker.length();

            int end = start;
            while (end < body.length()) {
                if (body.charAt(end) == '"' && body.charAt(end - 1) != '\\') {
                    break;
                }
                end++;
            }

            String content = body.substring(start, end)
                    .replace("\\n", "\n")
                    .replace("\\\"", "\"")
                    .replace("\\\\", "\\");

            System.out.println("ARIN >>" + content + "\n");
        }

        scanner.close();
        System.out.println("Bye.");
    }
}