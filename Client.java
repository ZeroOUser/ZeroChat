import java.awt.*;
import javax.swing.*;
import static javax.swing.JFrame.*;
import java.awt.event.*;

import java.io.*;
import java.net.*;
import java.nio.channels.*;
import static java.nio.charset.StandardCharsets.*;

import java.util.concurrent.*;

public class Client {
    private JFrame frame;
    private JTextArea incomingMsgsBox;
    private JTextField outMsgBox, nameField;
    private JButton sendBtn;
    private String incomingMsgs = "";
    private String name = "";
    private JPanel mainPanel;
    private Box loginPanel;
    
    private SocketChannel channel;
    private InetSocketAddress serverAddress;
    private BufferedReader reader;
    private PrintWriter writer;
    private ExecutorService executor;

    public static void main(String[] args) {
        new Client().launch();
    }
    public void launch() {
        setUpNetwork();
        setUpGUI();
        setUpThreads();
    }
    public class Key implements KeyListener {

        @Override
        public void keyPressed(KeyEvent e) {
            if(e.getKeyCode() == KeyEvent.VK_ENTER){
                sendMsg();
            }
        }

        @Override
        public void keyTyped(KeyEvent e) {}

        @Override
        public void keyReleased(KeyEvent e) {}


    };

    private void setUpGUI() {
        mainPanel = new JPanel(new BorderLayout());
        loginPanel = new Box(BoxLayout.Y_AXIS);
        frame = createFrame("Chat");
        createMainPage();
        createLoginPage();
        
        frame.setContentPane(loginPanel);
        
        frame.setVisible(true);

    }
    private void createMainPage() {
        incomingMsgsBox = createTextArea();
        JScrollPane msgScroller = createScroller(incomingMsgsBox);
        mainPanel.add(BorderLayout.CENTER, msgScroller);

        BorderLayout borderLayout = new BorderLayout();
        JPanel sendBox = new JPanel(borderLayout);
        outMsgBox = new JTextField();
        outMsgBox.addKeyListener(new Key());
        sendBtn = new JButton("Send");
        sendBtn.addActionListener(e -> sendMsg());
        sendBox.add(outMsgBox);
        sendBox.add(BorderLayout.EAST, sendBtn);
        mainPanel.add(BorderLayout.SOUTH, sendBox);
    }
    private void createLoginPage() {
        nameField = new JTextField();
        JButton loginBtn = new JButton("Login");
        loginBtn.setAlignmentX(loginBtn.CENTER_ALIGNMENT);
        loginBtn.addActionListener(e -> login());
        nameField.requestFocus();
        loginPanel.add(nameField);
        loginPanel.add(loginBtn);

    }
    private void login() {
        if (nameField.getText().equals("")) return;
        writer.println(nameField.getText());
        writer.flush();
        frame.setContentPane(mainPanel);
        frame.setSize(700, 700);
        frame.setVisible(true);
        outMsgBox.requestFocus();
    }
    private JFrame createFrame(String title) {
        JFrame f = new JFrame(title);
        f.setDefaultCloseOperation(EXIT_ON_CLOSE);
        f.setSize(500, 100);
        f.setLocation(700, 250);
        return f;
    }
    private JTextArea createTextArea() {
        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setFont(new Font("Serif", Font.BOLD, 15));
        textArea.setBackground(new Color(175, 124, 0));
        return textArea;
    }
    private JScrollPane createScroller(Component component) {
        JScrollPane scroller = new JScrollPane(component);
        scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        return scroller;
    }
    private void sendMsg() {
        String msg = outMsgBox.getText();
        if (msg.equals("")) return;
        
        // Send the message on the background thread
        writer.println(msg);
        writer.flush();
        
        // Clear the message input field
        outMsgBox.setText("");
        outMsgBox.requestFocus();
    }


    // network
    private void setUpNetwork() {
        try {
            serverAddress = new InetSocketAddress("ec2-16-171-139-33.eu-north-1.compute.amazonaws.com", 5000);
            channel = SocketChannel.open(serverAddress);
            reader = new BufferedReader(Channels.newReader(channel, UTF_8));
            writer = new PrintWriter(Channels.newWriter(channel, UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
        

    }

    // threads
    private void setUpThreads() {
        executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> incomingMsgsListener());
        executor.shutdown();
    }
    private void incomingMsgsListener() {
        try {
            String line = reader.readLine();
            while (line != null) {
                if (line.length() != 0) {
                    line += "\n";
                    incomingMsgs += line;
                    incomingMsgsBox.setText(incomingMsgs);
                }
                line = reader.readLine();
            }
            incomingMsgs += "Server is down.\n";
            incomingMsgsBox.setText(incomingMsgs);
        } catch(IOException e) {
            e.printStackTrace();
        }
        
    }
}