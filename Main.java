public class Main {
    public static void main(String[] args) {
        GroqClient groq = new GroqClient();
        FileHandling fh = new FileHandling();
        try {
            while (true) {
                String result;
                try {
                    result = groq.requestToGroq();
                } catch (Exception e) {
                    System.out.println("Error: " + e.getMessage());
                    continue;
                }

                if (result == null) {
                    System.out.println("ByeBye");
                    break;
                } else if (result.equalsIgnoreCase("Set GROQ_API_KEY environment variable first.")) {
                    System.out.println("Set GROQ_API_KEY environment variable first.");
                    break;
                }

                System.out.println("ARIN >> " + result + "\n");
                fh.writeToFile("ARIN >> " + result );
            }
        } finally {
            groq.close();
        }
    }
}