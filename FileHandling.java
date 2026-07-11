import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

class FileHandling {
    
    public void writeToFile(String text){

        try (FileWriter writer = new FileWriter("Chat.txt", true)) {
            writer.write("=================================================================================\n");
            writer.write(text + '\n');
        } catch (IOException e) {
            System.err.print("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void readFromFile(){

        try (FileReader reader = new FileReader("Chat.txt")) {
            reader.read();
        } catch (IOException e) {
            System.err.print("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}