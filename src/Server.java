import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.InputMismatchException;
import java.util.Random;
import java.util.Scanner;

public class Server {
    int MAX_PLAYERS_CNT = 2;

    int users_cnt = 0;
    Sender[] users = new Sender[MAX_PLAYERS_CNT + 1];

    void activate() throws IOException, InputMismatchException {
        Scanner in = new Scanner(System.in);
        System.out.print("INPUT PORT: ");
        int port = in.nextInt();
//        int port = 1239;

        ServerSocket server = new ServerSocket(port);
        while (true) {
            Socket socket_to_client = server.accept();
            System.out.println("NEW USER");

            int user_id;
            for (user_id = 0; user_id < MAX_PLAYERS_CNT; user_id++) {
                if (users[user_id] == null || users[user_id].client.isClosed()) {
                    users_cnt++;
                    break;
                }
            }
            System.out.println("user_id = " + user_id);

            Sender client = new Sender(socket_to_client, user_id, this);
            users[user_id] = client;
            client.out.write(user_id);
            client.out.flush();
            Thread thread = new Thread(client);
            thread.start();

            if (users_cnt == MAX_PLAYERS_CNT) {
                Random r = new Random();
                int id = r.nextInt(2);
                Event ev = new Event(Event.NEXT_MOVE);
                users[id].out.writeObject(ev);
                users[id].out.flush();
                System.out.println(id);
            }
        }
    }
}
