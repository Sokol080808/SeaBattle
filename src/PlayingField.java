import java.awt.*;
import java.util.ArrayDeque;

public class PlayingField {
    int X_START = 100;
    int Y_START = 100;
    int SQ_SIZE = 70;
    int OUTLINE_SIZE = 4;
    int FIELD_SIZE = 11 * OUTLINE_SIZE + 10 * SQ_SIZE;

    int[] dx = {1, 0, -1, 0, -1, -1, 1, 1};
    int[] dy = {0, 1, 0, -1, -1, 1, -1, 1};
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
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                if (field[j][i] == 0) continue;

                if (j + 1 < 10 && field[j + 1][i] == 1) {
                    int x = X_START + (i + 1) * OUTLINE_SIZE + i * SQ_SIZE;
                    int y = Y_START + (j + 1) * OUTLINE_SIZE + (j + 1) * SQ_SIZE;
                    g.fillRect(x, y, SQ_SIZE, OUTLINE_SIZE);
                }

                if (i + 1 < 10 && field[j][i + 1] == 1) {
                    int x = X_START + (i + 1) * OUTLINE_SIZE + (i + 1) * SQ_SIZE;
                    int y = Y_START + (j + 1) * OUTLINE_SIZE + j * SQ_SIZE;
                    g.fillRect(x, y, OUTLINE_SIZE, SQ_SIZE);
                }
            }
        }
    }

    int check() {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 10; j++) {
                if (field[i][j] == 0) continue;

                if (j + 1 < 10) {
                    if (field[i + 1][j + 1] == 1) {
                        return -1;
                    }
                }

                if (0 <= j - 1) {
                    if (field[i + 1][j - 1] == 1) {
                        return -1;
                    }
                }
            }
        }

        boolean[][] used = new boolean[10][10];
        int[] cnt = new int[5];

        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                if (field[i][j] == 0 || used[i][j]) continue;

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
                        if (field[nx][ny] == 1 && !used[nx][ny]) {
                            q.addLast(10 * nx + ny);
                            used[nx][ny] = true;
                        }
                    }
                }

                if (sz > 4) return -1;
                cnt[sz]++;
            }
        }

        if (cnt[4] == 1 && cnt[3] == 2 && cnt[2] == 3 && cnt[1] == 4) {
            return 1;
        } else {
            return 0;
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
        return type[y][x] == 0;
    }

    void change(int x, int y) {
        field[y][x] ^= 1;
        if (check() == -1) field[y][x] ^= 1;
    }

    void destroy(int bad_x, int bad_y) {
        boolean[][] used = new boolean[10][10];
        ArrayDeque<Integer> q = new ArrayDeque<>();
        used[bad_y][bad_x] = true;
        q.push(bad_y * 10 + bad_x);
        while (!q.isEmpty()) {
            int v = q.removeFirst();
            int x = v / 10;
            int y = v % 10;
            for (int k = 0; k < 8; k++) {
                int nx = x + dx[k];
                int ny = y + dy[k];
                if (nx < 0 || 10 <= nx || ny < 0 || 10 <= ny) continue;
                type[nx][ny] = 1;
                if (field[nx][ny] == 1 && !used[nx][ny]) {
                    q.addLast(10 * nx + ny);
                    used[nx][ny] = true;
                }
            }
        }
    }

    boolean checkShip(int ship_x, int ship_y) {
        if (field[ship_y][ship_x] == 0) return false;
        int sz = 0;
        int bad = 0;
        boolean[][] used = new boolean[10][10];
        ArrayDeque<Integer> q = new ArrayDeque<>();
        used[ship_y][ship_x] = true;
        q.push(ship_y * 10 + ship_x);
        while (!q.isEmpty()) {
            int v = q.removeFirst();
            int x = v / 10;
            int y = v % 10;
            sz++;
            bad += type[x][y];
            for (int k = 0; k < 4; k++) {
                int nx = x + dx[k];
                int ny = y + dy[k];
                if (nx < 0 || 10 <= nx || ny < 0 || 10 <= ny) continue;
                if (field[nx][ny] == 1 && !used[nx][ny]) {
                    q.addLast(10 * nx + ny);
                    used[nx][ny] = true;
                }
            }
        }
        return sz == bad;
    }

    void addShip(Ship ship) {
        int ship_x = ship.x + ship.dx;
        int ship_y = ship.y + ship.dy;

        int best = 2000;
        Pair<Integer, Integer> id = new Pair<>(-1, -1);
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                int x = X_START + (i + 1) * OUTLINE_SIZE + i * SQ_SIZE;
                int y = Y_START + (j + 1) * OUTLINE_SIZE + j * SQ_SIZE;
                int cur_dist = (x - ship_x) * (x - ship_x) + (y - ship_y) * (y - ship_y);
                if (cur_dist < best) {
                    best = cur_dist;
                    id.x = i;
                    id.y = j;
                }
            }
        }

        if (best == 2000) return;

        int OK_X = X_START + (id.x + 1) * OUTLINE_SIZE + id.x * SQ_SIZE;
        int OK_Y = Y_START + (id.y + 1) * OUTLINE_SIZE + id.y * SQ_SIZE;
        if (ship.type == false) {
            if (id.x + ship.sz > 10) return;
            for (int i = 0; i < ship.sz; i++) {
                if (field[id.y][id.x + i] == 1) {
                    return;
                }
            }

            for (int i = 0; i < ship.sz; i++) field[id.y][id.x + i] = 1;
            if (check() != -1) {
                ship.added = true;
                ship.x = OK_X;
                ship.y = OK_Y;
            } else {
                for (int i = 0; i < ship.sz; i++) field[id.y][id.x + i] = 0;
            }
        } else {
            if (id.y + ship.sz > 10) return;
            for (int i = 0; i < ship.sz; i++) {
                if (field[id.y + i][id.x] == 1) {
                    return;
                }
            }

            for (int i = 0; i < ship.sz; i++) field[id.y + i][id.x] = 1;
            if (check() != -1) {
                ship.added = true;
                ship.x = OK_X;
                ship.y = OK_Y;
            } else {
                for (int i = 0; i < ship.sz; i++) field[id.y + i][id.x] = 0;
            }
        }
    }

    void deleteShip(Ship ship) {
        if (!ship.added) return;
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                int x = X_START + (i + 1) * OUTLINE_SIZE + i * SQ_SIZE;
                int y = Y_START + (j + 1) * OUTLINE_SIZE + j * SQ_SIZE;
                if (ship.x == x && ship.y == y) {
                    if (ship.type == false) {
                        for (int k = 0; k < ship.sz; k++) field[j][i + k] = 0;
                    } else {
                        for (int k = 0; k < ship.sz; k++) field[j + k][i] = 0;
                    }
                }
            }
        }
    }
}
