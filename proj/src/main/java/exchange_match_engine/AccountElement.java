package exchange_match_engine;

public class AccountElement implements RequestElement{
    int id;
    double balance;

    public AccountElement(int id){
        this.id = id;
        this.balance = -1;
    }

    public AccountElement(int id, double balance){
        this.id = id;
        this.balance = balance;
    }
}
