package exchange_match_engine;

public class DOMCreate {

    String errEnd = "</error>";

    public String getAccountCreatedErrTag(String msg, int id) {
        String str = "<error id=\"" + id + "\">";
        return str + msg + errEnd;
    }

    public String getInvalidSymbolTags(String msg, int accountID, String sym) {
        return "<error sym=\"" + sym + "\" id=\"" + accountID + "\">" + msg + errEnd;
    }

    public String getSymCreatedErrTag(String msg) {
        return null;
    }

    public String getCreatedAccountTag(int accountID) {
        return "<created id=\"" + accountID + "\"/>";
    }

    public String getCreatedSymbolTag(int accountID, String sym) {
        return "<created sym=\"" + sym + "\" id=\"" + accountID + "\"/>";
    }

    public String getAccountInvalidTag(String sym, double amount, double limit, String msg) {
        return "<error sym=\"" + sym + "\" amount=\""+ amount + "\" limit=\"" + limit + "\">" + msg + errEnd;
    }

    public String getOrderOpenTag(String sym, double amount, double limit, int orderID) {
        return "<opened sym=\"" + sym + "\" amount=\""+ amount + "\" limit=\"" + limit + "\" id=\"" + orderID +"\" />";
    }

    public String formResult(String content) {
        return "<results>" + content + "</results>";
    }

    public String getQueryOpenTag(double amount) {
        return "<open shares=\"" + amount + "\"/>";
    }

    public String getQueryCancelTag(double amount, long secs) {
        return "<canceled shares=\"" + amount + "\" time=\"" + secs + "\"/>";
    }

    public String getQueryExecutedTag(double amount, double limit_price, long secs) {
        return "<executed shares=\"" + amount + "\" price=\"" + limit_price + "\" time=\"" + secs + "\"/>";
    }
}
