package exchange_match_engine;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConnectionHandler extends Thread {
    private ServerSocket serverSocket;
    ArrayList<UserConnection> connections;
    private ExecutorService service;
    ExchangeMatcherEngine engine;

    public ConnectionHandler(int port, ExchangeMatcherEngine engine) throws IOException {
        this.serverSocket = new ServerSocket(port);
        this.connections = new ArrayList<>();
        this.service = Executors.newFixedThreadPool(8); // specify number of threads
        this.engine = engine;
    }

    public void startServer() throws IOException {
        while (true) {
            Socket socket = serverSocket.accept();
            UserConnection connection = new UserConnection(socket);
            this.connections.add(connection);
            this.service.execute(() -> {
                handleConnection(connection);
            });
        }
    }

    void handleConnection(UserConnection connection){
        RequestHandler requestHandler = new RequestHandler();
        try {
            String bytesStr = connection.getLine();
            int bytes = Integer.parseInt(bytesStr);
            System.out.println("Receive xml length:" + bytes);
            String xml = connection.getLine();
            engine.processRequest(connection, requestHandler.startHandling(xml));
        } catch (IOException e) {
            e.printStackTrace();
            connections.remove(connection);
        }
    }

    void sendResult(UserConnection connection, String result) throws IOException {
        connection.sendResult(result);
        connection.release();
        connections.remove(connection);
    }
}
