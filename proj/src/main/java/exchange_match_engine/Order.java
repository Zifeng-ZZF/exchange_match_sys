package exchange_match_engine;

import java.time.Duration;
import java.time.LocalDateTime;

public class Order implements RequestElement {
    int id;
    double amount;
    double limit_price;
    String symbol;
    int account_num;
    LocalDateTime create_date;
    LocalDateTime canceled_date;
    LocalDateTime executed_date;

    public Order(double amount, double limit_price, String symbol, int accountID){
        this.amount = amount;
        this.limit_price = limit_price;
        this.symbol = symbol;
        this.account_num = accountID;
    }

    public LocalDateTime getCanceledDate() {
        return this.canceled_date;
    }

    public LocalDateTime getExecutedDate() {
        return this.executed_date;
    }

    public LocalDateTime getCreateDateTime() {
        return this.create_date;
    }

    public void setStoreDate(LocalDateTime date) {
        this.create_date = date;
    }

    public void setCancelDate(LocalDateTime date) {
        this.canceled_date = date;
    }

    public void setExecutedDate(LocalDateTime executedDate) {
        this.executed_date = executedDate;
    }

    public long getEpochInSeconds(LocalDateTime current) {
        Duration duration = Duration.between(create_date, current);
        System.out.println(duration.getSeconds() + " seconds");
        return duration.getSeconds();
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Order ").append(id).append(" deal ").append(amount);
        stringBuilder.append(" of ").append(symbol).append(" at ").append(limit_price);
        stringBuilder.append(" by ").append(account_num);
        return stringBuilder.toString();
    }
}
