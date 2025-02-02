import java.net.*;
import java.io.*;
import java.nio.channels.*;
import static java.nio.charset.StandardCharsets.*;
import java.util.*;
import java.util.concurrent.*;

public class Server {
    private InetSocketAddress serverAddress;
    private ServerSocketChannel serverChannel;
    // private BufferedReader reader;
    private ArrayList<PrintWriter> writers;
    private HashMap<SocketAddress, String> clients;
    private String outMsgs = "";
    public static void main(String[] args) {
        new Server().launch();
    }  
    public void launch() {
        writers = new ArrayList<>();
        ExecutorService executor = Executors.newCachedThreadPool();
        try {
            serverAddress = new InetSocketAddress(5000);
            serverChannel = ServerSocketChannel.open();
            serverChannel.bind(serverAddress);
            while(serverChannel.isOpen()) {
                SocketChannel clientChannel = serverChannel.accept();
                executor.execute(() -> clientHandler(clientChannel));
            }
            executor.shutdown();
        } catch(IOException e) {
            e.printStackTrace();
        }
        
    }
    private void clientHandler(SocketChannel channel) {
        PrintWriter writer = new PrintWriter(Channels.newWriter(channel, UTF_8));
        writer.println(outMsgs);
        writer.flush();
        writers.add(writer);
        try {
            BufferedReader reader = new BufferedReader(Channels.newReader(channel, UTF_8));
            SocketAddress address = channel.getRemoteAddress();
            String name = reader.readLine();
            // clients.put(address, name);
            sendToAllClients("New client connected at " + address + " & his name: " + name + "...");
            
            String line = reader.readLine();
            while(line != null) {
                sendToAllClients(name + ":: " + line);
                line = reader.readLine();
            }
            reader.close();
            channel.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
        
    }
    private void sendToAllClients(String msg) {
        outMsgs += msg + "\n";
        System.out.println(msg);
        for (PrintWriter writer : writers) {
            writer.println(msg);
            writer.flush();
        }
    }
}