package exchange_match_engine;

public class Tuple<A, B, C> {

    public A first;
    public B second;
    public C third;

    public Tuple(A a, B b, C c) {
        first = a;
        second = b;
        third = c;
    }

    public String toString(){
        return "(" + first + "," + second + "," + third + ")";
    }

}
