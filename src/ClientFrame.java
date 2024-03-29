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
    boolean ready = false;
    boolean game_started = false;
    boolean move_now = false;
    boolean disconnected = false;
    int result = 0;
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
    Button READY;
    int READY_NOW = 0;

    void send(Event e) throws IOException {
        out.writeObject(e);
        out.flush();
    }

    int ALIVE_CNT = 0;
    void startGame() {
        this.setSize(1630, 840);
        READY_NOW = 2;
        ready = game_started = true;
        ALIVE_CNT = 4 * 1 + 3 * 2 + 2 * 3 + 1 * 4;
    }

    ClientFrame(int id, Socket connection, ObjectOutputStream out) throws IOException {
        this.id = id;
        this.out = out;
        this.connection = connection;

        our = new PlayingField(OUR_X_START, OUR_Y_START, SQ_SIZE, OUTLINE_SIZE);
        enemy = new PlayingField(ENEMY_X_START, ENEMY_Y_START, SQ_SIZE, OUTLINE_SIZE);
        Event ev = new Event(Event.CONNECTED);
        send(ev);

        READY = new Button(930, 700, 100, 40, new ButtonAction() {
            @Override
            public void onClick() {
                Event ev = new Event();
                if (ready) {
                    ready = false;
                    READY_NOW--;
                    ev.type = Event.NOT_READY;
                } else {
                    if (our.check() != 1) return;
                    ready = true;
                    READY_NOW++;
                    if (READY_NOW == 2) startGame();
                    ev.type = Event.READY;
                }
                try {
                    send(ev);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

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
        this.setSize(840, 840);
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

        if (result == -1) {
            g.setColor(Color.BLACK);
            g.drawString("Вы проиграли", 215, 240);
        } else if (result == 1) {
            g.setColor(Color.BLACK);
            g.drawString("Вы выиграли", 220, 240);
        } else if (!connected) {
            g.setColor(Color.BLACK);
            g.drawString("Соперник еще не подключился", 340, 390);
        } else if (!game_started) {
            our.paint(g);
            READY.paint(g);
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
            this.setSize(1200, 840);
            if (!connected) {
                connected = true;
                Event ev = new Event(Event.CONNECTED);
                send(ev);
            }
        } else if (e.type == Event.READY) {
            READY_NOW++;
            if (READY_NOW == 2) startGame();
        } else if (e.type == Event.NOT_READY){
            READY_NOW--;
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
            ALIVE_CNT -= our.field[y][x];
            if (ALIVE_CNT == 0) {
                Event loss = new Event(Event.LOSS);
                send(loss);
                result = -1;
                this.setSize(500, 500);
            }
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
        } else if (e.type == Event.LOSS) {
            result = 1;
            this.setSize(500, 500);
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (!connected || result != 0) return;

        if (game_started) {
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
        } else {
            Pair<Integer, Integer> id = our.getID(e.getX(), e.getY());
            if (id.x != -1 && id.y != -1) {
                our.change(id.x, id.y);
            }
            READY.onClick(e.getX(), e.getY());
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
