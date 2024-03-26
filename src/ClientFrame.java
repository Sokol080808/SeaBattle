import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Field;
import java.net.Socket;
import java.net.SocketException;
import java.sql.SQLOutput;
import java.util.ArrayDeque;
import java.util.ArrayList;

public class ClientFrame extends JFrame implements KeyEventDispatcher {
    boolean game_started = false;
    boolean move_now = false;
    boolean disconnected = false;
    int MAX_PLAYER_CNT = 2;
    int X_START = 100;
    int Y_START = 100;
    int SQ_SIZE = 70;
    int OUTLINE_SIZE = 4;
    int FIELD_SIZE = 11 * OUTLINE_SIZE + 10 * SQ_SIZE;
    Socket connection;
    ObjectOutputStream out;
    int id;

    int[][] field = new int[][] {
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

    int [][] type = new int[][] {
            {1, 1, -1, -1, -1, -1, -1, -1, -1, -1},
            {0, -1, -1, -1, -1, -1, -1, -1, -1, -1},
            {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
            {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
            {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
            {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
            {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
            {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
            {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
            {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
    };

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

        boolean[] used = new boolean[100];
        int[] cnt = new int[5];

        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                if (field[i][j] == 0 || used[10 * i + j]) continue;

                ArrayDeque<Integer> q = new ArrayDeque<>();
                used[10 * i + j] = true;
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
                        if (field[nx][ny] == 1 && !used[10 * nx + ny]) {
                            q.addLast(10 * nx + ny);
                            used[10 * nx + ny] = true;
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

    ClientFrame(int id, Socket connection, ObjectOutputStream out) throws IOException {
        this.id = id;
        this.out = out;
        this.connection = connection;

        Event ev = new Event(Event.CONNECTED);
        out.writeObject(ev);
        out.flush();

        this.setSize(1100, 1100);
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

        if (!game_started) {
            g.setColor(Color.BLACK);
            g.drawString("Соперник еще не подключился", 500, 500);
        } else {
            g.setColor(new Color(0, 200, 255));
            g.fillRect(X_START, Y_START, FIELD_SIZE, FIELD_SIZE);
            for (int i = 0; i < 10; i++) {
                for (int j = 0; j < 10; j++) {
                    int x = X_START + (i + 1) * OUTLINE_SIZE + i * SQ_SIZE;
                    int y = Y_START + (j + 1) * OUTLINE_SIZE + j * SQ_SIZE;

                    if (type[j][i] == 1) {
                        g.setColor(new Color(255, 0, 0));
                    } else if (field[j][i] == 1) {
                        g.setColor(new Color(0, 0, 0));
                    } else {
                        g.setColor(new Color(180, 180, 180));
                    }

                    g.fillRect(x, y, SQ_SIZE, SQ_SIZE);

                    if (type[j][i] == 0) {
                        g.setColor(new Color(0, 0, 0));
                        g.drawLine(x, y, x + SQ_SIZE - 1, y + SQ_SIZE - 1);
                        g.drawLine(x, y + SQ_SIZE - 1, x + SQ_SIZE - 1, y);
                    }
                }
            }

            g.setColor(new Color(0, 0, 0));
            for (int i = 0; i < 9; i++) {
                for (int j = 0; j < 9; j++) {
                    if (field[j][i] == 0) continue;

                    if (field[j + 1][i] == 1) {
                        int x = X_START + (i + 1) * OUTLINE_SIZE + i * SQ_SIZE;
                        int y = Y_START + (j + 1) * OUTLINE_SIZE + (j + 1) * SQ_SIZE;
                        g.fillRect(x, y, SQ_SIZE, OUTLINE_SIZE);
                    }

                    if (field[j][i + 1] == 1) {
                        int x = X_START + (i + 1) * OUTLINE_SIZE + (i + 1) * SQ_SIZE;
                        int y = Y_START + (j + 1) * OUTLINE_SIZE + j * SQ_SIZE;
                        g.fillRect(x, y, OUTLINE_SIZE, SQ_SIZE);
                    }
                }
            }

            g.setColor(new Color(255, 0, 0));
            for (int i = 0; i < 9; i++) {
                for (int j = 0; j < 9; j++) {
                    if (type[j][i] != 1) continue;

                    if (type[j + 1][i] == 1) {
                        int x = X_START + (i + 1) * OUTLINE_SIZE + i * SQ_SIZE;
                        int y = Y_START + (j + 1) * OUTLINE_SIZE + (j + 1) * SQ_SIZE;
                        g.fillRect(x, y, SQ_SIZE, OUTLINE_SIZE);
                    }

                    if (type[j][i + 1] == 1) {
                        int x = X_START + (i + 1) * OUTLINE_SIZE + (i + 1) * SQ_SIZE;
                        int y = Y_START + (j + 1) * OUTLINE_SIZE + j * SQ_SIZE;
                        g.fillRect(x, y, OUTLINE_SIZE, SQ_SIZE);
                    }
                }
            }
        }

        g.dispose();
        bufferStrategy.show();
    }

    void update_event(Event e) throws IOException {
        if (e.type == Event.CONNECTED) {
            System.out.println("CONN");
            if (!game_started) {
                game_started = true;
                Event ev = new Event(Event.CONNECTED);
                System.out.println("SENDED");
                out.writeObject(ev);
                out.flush();
            }
        } else if (e.type == Event.DISCONNECTED) {
            disconnected = true;
        } else if (e.type == Event.NEXT_MOVE) {
            System.out.println("MOVE");
            move_now = true;
        } else if (e.type == Event.MOVE) {
            System.out.println("OP MOVE");
            Event ev = new Event(Event.INFO);
            int x = e.data.get(0);
            int y = e.data.get(1);
            ev.data.add(field[y][x]);
            ev.data.add(x);
            ev.data.add(y);
            out.writeObject(ev);
            out.flush();
        } else if (e.type == Event.INFO) {
            int res = e.data.get(0);
            int x = e.data.get(1);
            int y = e.data.get(2);
            if (res == 0) {
                Event ev = new Event(Event.NEXT_MOVE);
                ev.data.add(0);
                out.writeObject(ev);
                move_now = false;
            }
            type[y][x] = res;
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent e) {
        System.out.println(e.getKeyCode());
        return false;
    }
}
