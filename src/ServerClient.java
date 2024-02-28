import java.io.IOException;

public class ServerClient {
    public static void main(String[] args) throws IOException {
        Server server = new Server();
        server.activate();
    }
}
