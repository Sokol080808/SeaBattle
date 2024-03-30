import java.awt.*;

public class Ship {
    int SQ_SIZE;
    int OUTLINE_SIZE;
    int START_X, START_Y;
    boolean type = false;
    boolean added = false;
    int dx = 0, dy = 0;
    int MOUSE_START_X, MOUSE_START_Y;
    int x, y, h, w, sz;

    void normalize() {
        dx = 0;
        dy = 0;
        if (!added) {
            type = false;
            x = START_X;
            y = START_Y;
            w = SQ_SIZE * sz + OUTLINE_SIZE * (sz - 1);
            h = SQ_SIZE;
        }
    }

    Ship(int SQ_SIZE, int OUTLINE_SIZE, int SIZE, int START_X, int START_Y) {
        this.SQ_SIZE = SQ_SIZE;
        this.OUTLINE_SIZE = OUTLINE_SIZE;
        this.sz = SIZE;
        this.START_X = START_X;
        this.START_Y = START_Y;

        normalize();
    }

    void paint(Graphics g) {
        g.setColor(Color.BLACK);
        g.fillRect(x + dx, y + dy, w, h);
    }

    boolean checkHit(int event_x, int event_y) {
        return x <= event_x && event_x < x + w && y <= event_y && event_y < y + h;
    }

    void rotate() {
        if (type == false) {
            w = SQ_SIZE;
            h = SQ_SIZE * sz + OUTLINE_SIZE * (sz - 1);
            int cur_x = x + dx;
            for (int i = 0; i < sz; i++) {
                cur_x += SQ_SIZE + OUTLINE_SIZE;
                if (MOUSE_START_X + dx < cur_x) {
                    x += i * (SQ_SIZE + OUTLINE_SIZE);
                    y -= i * (SQ_SIZE + OUTLINE_SIZE);
                    break;
                }
            }
        } else {
            w = SQ_SIZE * sz + OUTLINE_SIZE * (sz - 1);
            h = SQ_SIZE;
            int cur_y = y + dy;
            for (int i = 0; i < sz; i++) {
                cur_y += SQ_SIZE + OUTLINE_SIZE;
                if (MOUSE_START_Y + dy < cur_y) {
                    x -= i * (SQ_SIZE + OUTLINE_SIZE);
                    y += i * (SQ_SIZE + OUTLINE_SIZE);
                    break;
                }
            }
        }
        type = !type;
    }
}
