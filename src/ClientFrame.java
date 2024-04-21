import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferStrategy;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ClientFrame extends JFrame implements MouseListener, MouseMotionListener, KeyEventDispatcher {
    boolean connected = false;
    boolean ready = false;
    boolean game_started = false;
    boolean move_now = false, can = true;
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

    Ship drag_now = null;

    ArrayList<Ship> ships = new ArrayList<>();

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

        READY = new Button(930, 730, 100, 40, Color.RED, new ButtonAction() {
            @Override
            public void onClick() {
                Event ev = new Event();
                if (ready) {
                    ready = false;
                    READY_NOW--;
                    ev.type = Event.NOT_READY;
                    READY.color = Color.RED;
                } else {
                    if (our.check() != 1) return;
                    ready = true;
                    READY_NOW++;
                    if (READY_NOW == 2) startGame();
                    ev.type = Event.READY;
                    READY.color = Color.GREEN;
                }
                try {
                    send(ev);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        int SHIP_X = OUR_X_START + our.FIELD_SIZE + SQ_SIZE;
        int SHIP_Y = OUR_Y_START;
        for (int i = 0; i < 10; i++) {
            if (i < 1) {
                ships.add(new Ship(SQ_SIZE, OUTLINE_SIZE, 4, SHIP_X, SHIP_Y));
            } else if (i < 3) {
                ships.add(new Ship(SQ_SIZE, OUTLINE_SIZE, 3, SHIP_X, SHIP_Y));
            } else if (i < 6) {
                ships.add(new Ship(SQ_SIZE, OUTLINE_SIZE, 2, SHIP_X, SHIP_Y));
            } else {
                ships.add(new Ship(SQ_SIZE, OUTLINE_SIZE, 1, SHIP_X, SHIP_Y));
            }
            if (i < 6) {
                SHIP_Y += SQ_SIZE + OUTLINE_SIZE;
            } else {
                SHIP_X += SQ_SIZE + OUTLINE_SIZE;
            }
        }

        addMouseListener(this);
        addMouseMotionListener(this);

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
            for (Ship ship : ships) ship.paint(g);
            g.setColor(Color.BLACK);
            g.drawString("Перетащите корабли на игровое поле", 870, 650);
            g.drawString("Для поворота используйте R", 900, 670);
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

    void updateEvent(Event e) throws IOException {
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
            int x = e.data.get(0);
            int y = e.data.get(1);
            our.type[y][x] = 1;
            if (our.checkShip(x, y)) {
                Event ev = new Event(Event.DESTROYED);
                ev.data.add(x);
                ev.data.add(y);
                send(ev);
                our.destroy(x, y);
            } else {
                Event ev = new Event(Event.INFO);
                ev.data.add(our.field[y][x]);
                ev.data.add(x);
                ev.data.add(y);
                send(ev);
            }
            ALIVE_CNT -= our.field[y][x];
            if (ALIVE_CNT == 0) {
                Event loss = new Event(Event.LOSS);
                send(loss);
                result = -1;
                this.setSize(500, 500);
            }
        } else if (e.type == Event.INFO) {
            can = true;
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
        } else if (e.type == Event.DESTROYED) {
            can = true;
            int x = e.data.get(0);
            int y = e.data.get(1);
            enemy.field[y][x] = 1;
            enemy.type[y][x] = 1;
            enemy.destroy(x, y);
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (!connected || result != 0) return;

        if (game_started) {
            if (!move_now || !can) return;

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
                can = false;
            }
        } else {
            READY.onClick(e.getX(), e.getY());
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (ready || !connected || game_started || result != 0) return;

        for (Ship ship : ships) {
            if (ship.checkHit(e.getX(), e.getY())) {
                drag_now = ship;
                ship.MOUSE_START_X = e.getX();
                ship.MOUSE_START_Y = e.getY();
            }
        }

        if (drag_now != null) our.deleteShip(drag_now);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (ready || !connected || game_started || result != 0 || drag_now == null) return;

        our.addShip(drag_now);

        drag_now = null;
        for (Ship ship : ships) {
            ship.normalize();
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (ready || !connected || game_started || result != 0 || drag_now == null) return;
        drag_now.dx = e.getX() - drag_now.MOUSE_START_X;
        drag_now.dy = e.getY() - drag_now.MOUSE_START_Y;
    }

    @Override
    public void mouseMoved(MouseEvent e) {

    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_R && e.getID() == KeyEvent.KEY_RELEASED) {
            if (drag_now != null) {
                drag_now.rotate();
            }
        }
        return false;
    }
}
