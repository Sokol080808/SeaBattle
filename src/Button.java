import java.awt.*;

public class Button {
    int x, y, w, h;
    ButtonAction action;

    Button(int x, int y, int w, int h, ButtonAction action) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        this.action = action;
    }

    boolean checkHit(int event_x, int event_y) {
        return x <= event_x && event_x < x + w && y <= event_y && event_y < y + w;
    }

    void onClick(int event_x, int event_y) {
        if (checkHit(event_x, event_y)) {
            action.onClick();
        }
    }

    void paint(Graphics g) {
        g.setColor(Color.GREEN);
        g.fillRect(x, y, w, h);
    }
}
