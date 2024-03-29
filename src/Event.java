import java.io.Serializable;
import java.util.ArrayList;

public class Event implements Serializable {
    static int NOTHING = -1;
    static int DISCONNECTED = 0;
    static int CONNECTED = 1;
    static int MOVE = 2;
    static int INFO = 3;
    static int NEXT_MOVE = 4;
    static int READY = 5;
    static int NOT_READY = 6;
    static int LOSS = 7;
    int type = NOTHING;
    ArrayList<Integer> data = new ArrayList<>();
    Event() {}
    Event(int type) {
        this.type = type;
    }
}
