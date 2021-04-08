package exchange_match_engine;

import java.io.IOException;

public class App {
    public static void main(String[] args) throws IOException {
        ExchangeMatcherEngine engine = new ExchangeMatcherEngine();
        System.out.println("Engine start ...");
        engine.startEngine();
    }
}
