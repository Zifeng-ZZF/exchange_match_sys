package exchange_match_engine;

public class SymbElement implements RequestElement{
    int accountID;
    String name;
    int amount;

    public SymbElement(String name, int accountID,int amount){
        this.name = name;
        this.accountID = accountID;
        this.amount = amount; 
    }
}
