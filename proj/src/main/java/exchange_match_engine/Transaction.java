package exchange_match_engine;

import java.util.ArrayList;
import java.util.List;

import org.apache.ibatis.javassist.compiler.ast.Pair;

public class Transaction extends Request{
    int transactionID;
    int accountID;

    ArrayList<RequestElement> elements;

    public Transaction(int accountID) {
        super(Type.TRANSACTION);
        this.accountID = accountID;
        elements = new ArrayList<>();
    }
}
