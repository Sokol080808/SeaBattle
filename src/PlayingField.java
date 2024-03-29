import java.awt.*;

public class PlayingField {
    int X_START = 100;
    int Y_START = 100;
    int SQ_SIZE = 70;
    int OUTLINE_SIZE = 4;
    int FIELD_SIZE = 11 * OUTLINE_SIZE + 10 * SQ_SIZE;
    int[][] field = new int[][] {
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0}
    };

    int[][] type = new int[][] {
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0}
    };

    PlayingField() {}
    PlayingField(int X_START, int Y_START, int SQ_SIZE, int OUTLINE_SIZE) {
        this.X_START = X_START;
        this.Y_START = Y_START;
        this.SQ_SIZE = SQ_SIZE;
        this.OUTLINE_SIZE = OUTLINE_SIZE;
        FIELD_SIZE = 11 * OUTLINE_SIZE + 10 * SQ_SIZE;
    }

    void paint(Graphics g) {
        g.setColor(new Color(0, 200, 255));
        g.fillRect(X_START, Y_START, FIELD_SIZE, FIELD_SIZE);
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                int x = X_START + (i + 1) * OUTLINE_SIZE + i * SQ_SIZE;
                int y = Y_START + (j + 1) * OUTLINE_SIZE + j * SQ_SIZE;

                if (field[j][i] == 1) {
                    g.setColor(Color.BLACK);
                } else {
                    g.setColor(new Color(180, 180, 180));
                }

                g.fillRect(x, y, SQ_SIZE, SQ_SIZE);

                if (type[j][i] == 1) {
                    if (field[j][i] == 1) {
                        g.setColor(Color.RED);
                    } else {
                        g.setColor(Color.BLACK);
                    }
                    g.drawLine(x, y, x + SQ_SIZE - 1, y + SQ_SIZE - 1);
                    g.drawLine(x, y + SQ_SIZE - 1, x + SQ_SIZE - 1, y);
                }
            }
        }

        g.setColor(Color.BLACK);
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
    }

    Pair<Integer, Integer> getID(int event_x, int event_y) {
        Pair<Integer, Integer> res = new Pair<>(-1, -1);
        for (int i = 0; i < 10; i++) {
            int x = X_START + (i + 1) * OUTLINE_SIZE + i * SQ_SIZE;
            int y = Y_START + (i + 1) * OUTLINE_SIZE + i * SQ_SIZE;
            if (x <= event_x && event_x < x + SQ_SIZE) res.x = i;
            if (y <= event_y && event_y < y + SQ_SIZE) res.y = i;
        }
        return res;
    }

    boolean update(int x, int y) {
        if (type[y][x] == 1) return false;
        type[y][x] = 1;
        return true;
    }
}
