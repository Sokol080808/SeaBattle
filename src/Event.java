import java.io.Serializable;
import java.util.ArrayList;

public class Event implements Serializable {
    static int NOTHING = -1;
    static int DISCONNECTED = 0;
    static int CONNECTED = 1;
    static int MOVE = 2;
    static int INFO = 3;
    static int NEXT_MOVE = 4;
    int type = NOTHING;
    ArrayList<Integer> data = new ArrayList<>();
    Event() {}
    Event(int type) {
        this.type = type;
    }
}
