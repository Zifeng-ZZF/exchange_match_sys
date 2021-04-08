package exchange_match_engine;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;

public class UserConnection {
    private Socket socket;
    private PrintWriter output;
    private BufferedReader input;
    private InputStreamReader streamReader;

    public UserConnection(Socket socket) throws IOException {
        this.socket = socket;
        this.output = new PrintWriter(socket.getOutputStream(), true);
        this.streamReader = new InputStreamReader(socket.getInputStream());
        this.input = new BufferedReader(streamReader);
    }

    public void sendResult(String result) {
        output.println(result);
    }

    public String getLine() throws IOException{
        String msg = input.readLine();
        if (msg == null || msg.isEmpty()) {
            throw new SocketException("Client closed.");
        }
        return msg;
    }

    public void release() throws IOException {
        socket.close();
    }
}
