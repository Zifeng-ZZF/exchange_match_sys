package exchange_match_engine;

import java.util.HashMap;

public class Account {
    private int account_num;

    private double balance;

    /**
     * Position map ==> account_symbol in DB
     */
    private HashMap<String, Double> symbolShare;

    public Account() {
        symbolShare = new HashMap<>();
    }

    public Account(int accountID, double balance) {
        this.account_num = accountID;
        this.balance = balance;
    }

    public void putSymbol(String symbol, double amount) {
        symbolShare.put(symbol, amount);
    }

    public double getSymbolAmount(String symbol) {
        return symbolShare.get(symbol);
    }

    public void setAccount_num(int account_num) {
        this.account_num = account_num;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public int getAccountNum() {
        return account_num;
    }

    public double getBalance() {
        return balance;
    }
}
