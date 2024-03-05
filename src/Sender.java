import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;

public class Sender implements Runnable {
    Socket client;
    ObjectInputStream in;
    ObjectOutputStream out;
    InputStream input;
    Server server;
    int user_id;

    Sender(Socket socket, int user_id, Server server) throws IOException {
        client = socket;
        out = new ObjectOutputStream(client.getOutputStream());
        input = client.getInputStream();
        in = new ObjectInputStream(input);

        this.server = server;
        this.user_id = user_id;
    }

    @Override
    public void run() {
        System.out.println("STARTED");
        try {
            out.write(user_id);
            out.flush();

            server.users[user_id] = this;
            while (!client.isClosed()) {
                if (input.available() > 0) {
                    Event ev = (Event) in.readObject();
                    send(ev);

                    if (ev.type == Event.DISCONNECTED) {
                        break;
                    }
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        System.out.println("DISCONNECTED");

        try {
            client.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        server.users_cnt--;
        server.users[user_id] = null;
    }

    void send(Event ev) throws IOException {
        for (Sender user : server.users) {
            if (user == null || user.user_id == this.user_id) continue;

            try {
                user.out.writeObject(ev);
                user.out.flush();
            } catch (SocketException se) {
                System.out.println("DISCONNECTED");
                // ДОБАВИТЬ ПОДТВЕРЖДЕНИЕ ОТКЛЮЧЕНИЯ ПОЛЬЗОВАТЕЛЯ
            }
        }
    }
}
