import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    static int MAX_PLAYERS_CNT = 2;
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        Scanner sys_in = new Scanner(System.in);
        String ip;
//        System.out.print("INPUT SERVER IP: ");
//        ip = sys_in.next();
//        int port;
//        System.out.print("INPUT SERVER PORT: ");
//        port = sys_in.nextInt();
        ip = "localhost";
        int port = 1239;

        Socket connection = new Socket(ip, port);
        ObjectOutputStream out = new ObjectOutputStream(connection.getOutputStream());
        InputStream input = connection.getInputStream();
        ObjectInputStream in = new ObjectInputStream(input);

        KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();

        int id = in.read();
        if (id == MAX_PLAYERS_CNT) {
            connection.close();
            System.out.println("TOO MANY PLAYERS");
            return;
        }

        Thread ShutdownHook = new Thread(() -> {

        });
        Runtime.getRuntime().addShutdownHook(ShutdownHook);

        ClientFrame frame = new ClientFrame(id, connection, out);

        manager.addKeyEventDispatcher(frame);
        while (!connection.isClosed()) {
            try {
                while (input.available() > 0) {
                    Event ev = (Event) in.readObject();
                    frame.update_event(ev);
                }
                frame.repaint();
            } catch (IOException exc) {
                break;
            }
        }
    }
}
