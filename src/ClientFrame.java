import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Field;
import java.net.Socket;
import java.net.SocketException;
import java.sql.SQLOutput;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Map;

public class ClientFrame extends JFrame implements MouseListener, MouseMotionListener {
    boolean connected = false;
    boolean game_started = false;
    boolean move_now = false;
    boolean disconnected = false;
    int MAX_PLAYER_CNT = 2;
    int OUR_X_START = 35;
    int OUR_Y_START = 60;
    int ENEMY_X_START = 850;
    int ENEMY_Y_START = 60;
    int SQ_SIZE = 70;
    int OUTLINE_SIZE = 4;
    Socket connection;
    ObjectOutputStream out;
    int id;
    PlayingField our, enemy;

    int[] dx = {1, 0, -1, 0};
    int[] dy = {0, 1, 0, -1};

    int check(int[][] f) {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 10; j++) {
                if (f[i][j] == 0) continue;

                if (j + 1 < 10) {
                    if (f[i + 1][j + 1] == 1) {
                        return -1;
                    }
                }

                if (0 <= j - 1) {
                    if (f[i + 1][j - 1] == 1) {
                        return -1;
                    }
                }
            }
        }

        boolean[][] used = new boolean[10][10];
        int[] cnt = new int[5];

        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                if (f[i][j] == 0 || used[i][j]) continue;

                ArrayDeque<Integer> q = new ArrayDeque<>();
                used[i][j] = true;
                q.push(i * 10 + j);
                int sz = 0;
                while (!q.isEmpty()) {
                    int v = q.removeFirst();
                    int x = v / 10;
                    int y = v % 10;
                    sz++;
                    for (int k = 0; k < 4; k++) {
                        int nx = x + dx[k];
                        int ny = y + dy[k];
                        if (nx < 0 || 10 <= nx || ny < 0 || 10 <= ny) continue;
                        if (f[nx][ny] == 1 && !used[nx][ny]) {
                            q.addLast(10 * nx + ny);
                            used[nx][ny] = true;
                        }
                    }
                }

                if (sz > 4) return 0;
                cnt[sz]++;
            }
        }

        if (cnt[4] == 1 && cnt[3] == 2 && cnt[2] == 3 && cnt[1] == 4) {
            return 1;
        } else {
            return 0;
        }
    }

    void send(Event e) throws IOException {
        out.writeObject(e);
        out.flush();
    }

    ClientFrame(int id, Socket connection, ObjectOutputStream out) throws IOException {
        this.id = id;
        this.out = out;
        this.connection = connection;

        our = new PlayingField(OUR_X_START, OUR_Y_START, SQ_SIZE, OUTLINE_SIZE);
        enemy = new PlayingField(ENEMY_X_START, ENEMY_Y_START, SQ_SIZE, OUTLINE_SIZE);
        Event ev = new Event(Event.CONNECTED);
        send(ev);

        addMouseListener(this);
        addMouseMotionListener(this);

        our.field = new int[][] {
                {1, 1, 0, 1, 0, 1, 0, 1, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 1, 0, 0, 0},
                {0, 1, 1, 1, 1, 0, 1, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 1, 0},
                {0, 1, 0, 0, 0, 0, 0, 0, 1, 0},
                {0, 1, 0, 1, 0, 0, 0, 0, 1, 0},
                {0, 1, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 1, 1, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0}
        };

        this.setTitle("Sea Battle") ;
        this.setSize(850, 850);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setResizable(false);
        this.setVisible(true);
    }

    @Override
    public void paint(Graphics g) {
        BufferStrategy bufferStrategy = getBufferStrategy();
        if (bufferStrategy == null) {
            createBufferStrategy(2);
            bufferStrategy = getBufferStrategy();
        }

        g = bufferStrategy.getDrawGraphics();
        g.clearRect(0, 0, getWidth(), getHeight());

        if (!connected) {
            g.setColor(Color.BLACK);
            g.drawString("Соперник еще не подключился", 340, 390);
        } else {
            g.setColor(Color.BLACK);
            if (move_now) {
                g.drawString("Ваш ход", 35, 45);
            } else {
                g.drawString("Ход оппонента", 35, 45);
            }
            our.paint(g);
            enemy.paint(g);
        }

        g.dispose();
        bufferStrategy.show();
    }

    void update_event(Event e) throws IOException {
        if (e.type == Event.CONNECTED) {
            this.setSize(1630, 840);
            if (!connected) {
                connected = true;
                Event ev = new Event(Event.CONNECTED);
                send(ev);
            }
        } else if (e.type == Event.DISCONNECTED) {
            disconnected = true;
        } else if (e.type == Event.NEXT_MOVE) {
            move_now = true;
        } else if (e.type == Event.MOVE) {
            Event ev = new Event(Event.INFO);
            int x = e.data.get(0);
            int y = e.data.get(1);
            our.type[y][x] = 1;
            ev.data.add(our.field[y][x]);
            ev.data.add(x);
            ev.data.add(y);
            send(ev);
        } else if (e.type == Event.INFO) {
            int res = e.data.get(0);
            int x = e.data.get(1);
            int y = e.data.get(2);
            if (res == 0) {
                Event ev = new Event(Event.NEXT_MOVE);
                send(ev);
                move_now = false;
            }
            enemy.field[y][x] = res;
            enemy.type[y][x] = 1;
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (!move_now) return;
        Pair<Integer, Integer> id = enemy.getID(e.getX(), e.getY());
        if (id.x == -1 || id.y == -1) return;

        if (enemy.update(id.x, id.y)) {
            Event ev = new Event(Event.MOVE);
            ev.data.add(id.x);
            ev.data.add(id.y);
            try {
                send(ev);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void mouseDragged(MouseEvent e) {

    }

    @Override
    public void mouseMoved(MouseEvent e) {

    }
}
