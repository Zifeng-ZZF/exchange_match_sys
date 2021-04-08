package exchange_match_engine;

public class CancelElement implements RequestElement{
    int transactionID;

    public CancelElement(int transactionID){
        this.transactionID = transactionID;
    }
}
