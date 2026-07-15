import java.awt.*;
import javax.swing.*;

public class ARIN_GUI extends JFrame {

    JTextArea outputArea;
    JTextField inputField;
    JButton sendButton;
    GroqClient client;

    public ARIN_GUI() {
        // window setup
        setTitle("A.R.I.N. (Adaptive Response Intelligent Assistant)");
        setSize(1080, 720);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.BLACK);

        JLabel titleLabel = new JLabel("ARIN - Adaptive Reasoning Intelligent Assistant", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Monospaced", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBackground(Color.BLACK);
        add(titleLabel, BorderLayout.NORTH);
        

        ImageIcon icon = new ImageIcon("ARIN.png");
        setIconImage(icon.getImage());

        // where replies show up
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setBackground(Color.BLACK);
        outputArea.setForeground(Color.WHITE);
        outputArea.setFont(new Font("Consolas", Font.PLAIN, 18));
        add(new JScrollPane(outputArea), BorderLayout.CENTER);

        // where you type
        inputField = new JTextField();
        inputField.setFont(new Font("Consolas", Font.PLAIN, 18));
        inputField.setBackground(Color.BLACK);
        inputField.setForeground(Color.WHITE);
        sendButton = new JButton("Send");

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());
        bottomPanel.add(inputField, BorderLayout.CENTER);
        bottomPanel.add(sendButton, BorderLayout.EAST);
        bottomPanel.setBackground(Color.BLACK);
        add(bottomPanel, BorderLayout.SOUTH);
        sendButton.setFont(new Font("Consolas", Font.PLAIN, 18));

        // connect to your existing GroqClient
        client = new GroqClient();

        // what happens when you click Send
        // wrapped in try/catch because sendMessage() can throw an Exception
        inputField.addActionListener(e -> {
            try {
                sendMessage();
            } catch (Exception ex) {
                outputArea.append("Error: " + ex.getMessage() + "\n");
            }
        });
        setVisible(true);
    }

    void sendMessage() throws Exception {
        String userText = inputField.getText();
        if (userText.trim().isEmpty()) {
            return;
        }

        outputArea.append("You >> " + userText + "\n");
        inputField.setText("");

        // this line will freeze the GUI while it waits for a reply
        String reply = client.sendPrompt(userText);

        outputArea.append("ARIN >> " + reply + "\n\n");
    }

    public static void main(String[] args) {
        new ARIN_GUI();
    }
}