package exchange_match_engine;

import java.util.ArrayList;

public class Create extends Request {

    ArrayList<RequestElement> requestElements;

    public Create() {
        super(Type.CREATE);
        this.requestElements = new ArrayList<>();
    }
}
