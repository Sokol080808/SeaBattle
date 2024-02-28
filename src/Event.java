import java.io.Serializable;
import java.util.ArrayList;

public class Event implements Serializable {
    static int NOTHING = -1;
    int type = NOTHING;
    ArrayList<Integer> data = new ArrayList<>();
    Event() {}
}
