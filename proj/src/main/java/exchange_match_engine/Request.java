package exchange_match_engine;

import java.util.ArrayList;

public class Request{
    private Type type;

    public enum Type {
        CREATE,
        TRANSACTION
    }

    public Request(Type type) {
        this.type = type;
    }

    public Type getType(){
        return type;
    }
}
