package exchange_match_engine;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class ExchangeMatchWorker extends Thread {
    private ExecutorService service;
    private DBProcess dbProcess;
    final Object lock;
    final Object dbLock;

    Queue<Order> arriveOrderQueue;

    public ExchangeMatchWorker(DBProcess dbProcess) {
        this.service = Executors.newFixedThreadPool(8);
        this.arriveOrderQueue = new LinkedBlockingQueue<>();
        this.lock = new Object();
        this.dbLock = new Object();
        this.dbProcess = dbProcess;
    }

    public void pushToQueue(Order order) {
        this.arriveOrderQueue.add(order);
    }

    public void startExchanging() {
        System.out.println("Engine start matching...");
        this.start();
    }

    @Override
    public void run() {
        try {
            while (true) {
                Order order = null;
                synchronized (lock) {
                    while ((order = arriveOrderQueue.poll()) == null) {
                        lock.wait();
                    }
                    System.out.println("Get an order...");
                    lock.notifyAll();
                }
                Order finalOrder = order;
                service.execute(() -> runFindMatch(finalOrder));
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    void runFindMatch(Order order) {
        System.out.println("Starting finding match of order id = " + order.id + " ..... ");
        if (order.amount >= 0) { //buying
            findSellMatch(order);
        }
        else {
            findBuyMatch(order);
        }
    }

    void findSellMatch(Order order) {
        ArrayList<Order> allMatchedSell = dbProcess.findMatchedSell(order);
        synchronized (dbLock) {
            for (Order matched : allMatchedSell) {
                if (findMatch(order, matched, false)) {
                    break;
                }
            }
        }
    }

    void findBuyMatch(Order order) {
        ArrayList<Order> allMatchedBuy = dbProcess.findMatchedBuy(order);
        synchronized (dbLock) {
            for (Order matched : allMatchedBuy) {
                if (findMatch(matched, order, true)) {
                    break;
                }
            }
        }
    }

    void updateSellerBalance(int sellerID, double addition) {
        Account seller = dbProcess.findAccount(sellerID);
        dbProcess.updateAccountBalance(sellerID, seller.getBalance() + addition);
    }

    void updateBuyerShares(int buyID, String symbol, double amount) {
        double originalAmt = dbProcess.findAmountOfSymbolOfAccount(symbol, buyID);
        if (originalAmt != -1) {
            dbProcess.updateAccountAmount(symbol, buyID, originalAmt + amount);
        }
        else {
            dbProcess.addNewAccountSymbolMapping(buyID, symbol, amount);
        }
    }

    void handleExactMatch(Order order, Order matched, LocalDateTime currTime, double price) {
        dbProcess.deleteOpenOrder(order.id);
        dbProcess.deleteOpenOrder(matched.id);
        order.setExecutedDate(currTime);
        matched.setExecutedDate(currTime);
        order.limit_price = price;
        dbProcess.insertExecutedOrder(order);
        dbProcess.insertOrder(matched);
    }

    void placeExecuted(Order full, Order partial, LocalDateTime currTime, double price) {
        //insert the executed part of the matched/order into executed
        Order executed = new Order(-1*full.amount, price, partial.symbol, partial.account_num);
        executed.id = partial.id;
        executed.setExecutedDate(currTime);
        executed.setStoreDate(partial.getCreateDateTime());
        dbProcess.insertExecutedOrder(executed);

        //delete the order from open.
        //insert into executed.
        dbProcess.deleteOpenOrder(full.id);
        full.setExecutedDate(currTime);
        full.limit_price = price;
        dbProcess.insertExecutedOrder(full);
    }

    boolean findMatch(Order buy, Order sell, boolean isSell) {
        LocalDateTime currTime = LocalDateTime.now();
        double price = buy.getCreateDateTime().isBefore(sell.getCreateDateTime()) ? buy.limit_price : sell.limit_price;
        if (-1 * sell.amount > buy.amount) {
            sell.amount += buy.amount;
            dbProcess.updateOpenOrderAmount(sell.id, sell.amount); //update the seller's order amount
            placeExecuted(buy, sell, currTime, price);
            updateSellerBalance(sell.account_num, buy.amount * price);
            updateBuyerShares(buy.account_num, buy.symbol, buy.amount);
            if (!isSell) {
                return true;
            }
        }
        else if (-1 * sell.amount < buy.amount) {
            //update the matched one's order amount
            buy.amount += sell.amount;
            dbProcess.updateOpenOrderAmount(buy.id, buy.amount);
            placeExecuted(sell, buy, currTime, price);
            updateSellerBalance(sell.account_num, -1 * sell.amount * price);
            updateBuyerShares(buy.account_num, buy.symbol, -1 * sell.amount);
            if (isSell) {
                return true;
            }
        }
        else { // == exactly matched
            handleExactMatch(buy, sell, currTime, price);
            return true;
        }
        return false;
    }
}
