package exchange_match_engine;


public class QueryElement implements RequestElement{
    int transactionID;

    public QueryElement(int transactionID){
        this.transactionID = transactionID;
    }
}
