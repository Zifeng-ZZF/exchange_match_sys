package exchange_match_engine;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class ExchangeMatcherEngine {
    /**
     * Handle client connections
     */
    private ConnectionHandler connectionHandler;
    /**
     * Perform DB operations
     */
    private DBProcess dbProcess;
    /**
     * Create tags of DOM
     */
    private DOMCreate domCreate;
    /**
     * Responsible for matching all orders
     */
    private ExchangeMatchWorker matchWorker;
    /**
     * Locks
     */
    private final Object newOrderLock;
    private final Object symbolCreateLock;
    private final Object acctCreateLock;

    /**
     * Constructor
     * @throws IOException
     */
    public ExchangeMatcherEngine() throws IOException {
        this.connectionHandler = new ConnectionHandler(12345, this);
        this.dbProcess = new DBProcess();
        this.domCreate = new DOMCreate();
        this.matchWorker = new ExchangeMatchWorker(dbProcess);
        // locks
        this.newOrderLock = new Object();
        this.symbolCreateLock = new Object();
        this.acctCreateLock = new Object();
    }

    /**
     * Start engine to receive request and invoke exchange and match worker
     * @throws IOException
     */
    public void startEngine() throws IOException {
        matchWorker.startExchanging();
        this.connectionHandler.startServer();
    }

    /**
     * Process all requests
     * @param connection user connection
     * @param request request
     */
    public void processRequest(UserConnection connection, Request request) throws IOException {
        if (request == null) {
            connectionHandler.sendResult(connection, "<result><error>mal-formed request</error></result>");
        }
        String result = "";
        if (request.getType() == Request.Type.CREATE) {
            result = processCreate((Create) request);
        }
        else if (request.getType() == Request.Type.TRANSACTION) {
            result = processTransaction((Transaction) request);
        }
        connectionHandler.sendResult(connection, result);
    }

    /**
     * Process create request
     * @param request request
     */
    public String processCreate(Create request) {
        StringBuilder resultFormer = new StringBuilder();
        for (RequestElement requestElement : request.requestElements) {
            if (requestElement.getClass().equals(AccountElement.class)) { //create account
                AccountElement accountElement = (AccountElement) requestElement;
                handlerAccountCreate(accountElement, resultFormer);
            }
            else { //create symbol
                SymbElement symElement = (SymbElement) requestElement;
                handleSymbolCreate(symElement.name, symElement.accountID, symElement.amount, resultFormer);
            }
        }
        return domCreate.formResult(resultFormer.toString());
    }

    /**
     * Handle create account in create request
     * @param accountElement account element
     * @param resultFormer form xml
     */
    void handlerAccountCreate(AccountElement accountElement, StringBuilder resultFormer) {
        synchronized (acctCreateLock) {
            if (!checkAccountExist(accountElement.id)) {
                Account account = new Account(accountElement.id, accountElement.balance);
                dbProcess.addNewAccount(account);
                resultFormer.append(domCreate.getCreatedAccountTag(account.getAccountNum()));
            }
            else {
                resultFormer.append(domCreate.getAccountCreatedErrTag("Account already exist.", accountElement.id));
            }
        }
    }

    /**
     * Process transaction request
     * @param transaction transaction
     * @return xml string
     */
    public String processTransaction(Transaction transaction) {
        StringBuilder resultFormer = new StringBuilder();
        if (checkAccountExist(transaction.accountID)) {
            for (RequestElement requestElement : transaction.elements) {
                if (requestElement.getClass().equals(Order.class)) { //handle order
                    handleOrder((Order) requestElement, resultFormer);
                }
                else if (requestElement.getClass().equals(QueryElement.class)){ //handle query
                    handleQuery(((QueryElement) requestElement).transactionID, resultFormer, transaction.accountID);
                }
                else { //handle cancel
                    handleCancel(((CancelElement)requestElement).transactionID, resultFormer, transaction.accountID);
                }
            }
        }
        else {
            resultFormer.append(domCreate.getAccountCreatedErrTag("Account does not exist.", transaction.accountID));
        }
        return domCreate.formResult(resultFormer.toString());
    }

    /**
     * Handler order in transaction request
     * @param order order
     * @param resultFormer form result xml
     */
    void handleOrder(Order order, StringBuilder resultFormer) {
        synchronized (matchWorker.dbLock) {
            Account account = dbProcess.findAccount(order.account_num);
            if (checkAccountID(order, account, resultFormer)) {
                if (order.amount >= 0) { //buying
                    handleBuyOrder(order, account, resultFormer);
                }
                else { //selling
                    handleSellOrder(order, resultFormer);
                }
            }
        }
    }

    /**
     * Handle query request and produce result
     * @param orderID transaction ID
     * @param resultFormer form xml tags
     */
    void handleQuery(int orderID, StringBuilder resultFormer, int accountID) {
        // find in open orders
        StringBuilder tag = new StringBuilder("<status id=\"" + orderID + "\">");
        Order orderInOpen;
        Order orderInCancel;
        ArrayList<Order> ordersInExecuted;
        synchronized (matchWorker.dbLock) {
            orderInOpen = dbProcess.findOrder(orderID);
            orderInCancel = dbProcess.findOrderInCancel(orderID);
            ordersInExecuted = dbProcess.findOrderInExecuted(orderID);
        }
        // either to have open or cancel
        if (orderInOpen != null) {
            if (orderInOpen.account_num == accountID) {
                tag.append(domCreate.getQueryOpenTag(orderInOpen.amount));
            }
            else {
                tag.append("<error id=\"").append(orderID).append("\">Does not belong to you</error>");
            }
        }
        else if (orderInCancel != null) {
            if (orderInCancel.account_num == accountID) {
                long secs = orderInCancel.getEpochInSeconds(orderInCancel.getCanceledDate());
                tag.append(domCreate.getQueryCancelTag(orderInCancel.amount, secs));
            }
            else {
                tag.append("<error id=\"").append(orderID).append("\">Does not belong to you</error>");
            }
        }
        processExecutedOrders(tag, ordersInExecuted, accountID);
        tag.append("</status>");
        resultFormer.append(tag);
    }

    /**
     * Tagging executed orders
     * @param tag tag to append
     * @param ordersInExecuted executed orders list
     */
    private void processExecutedOrders(StringBuilder tag, ArrayList<Order> ordersInExecuted, int accountID) {
        // appending executed tags
        for (Order order : ordersInExecuted) {
            if (order.account_num == accountID) {
                long secs = order.getEpochInSeconds(order.getExecutedDate());
                tag.append(domCreate.getQueryExecutedTag(order.amount, order.limit_price, secs));
            }
            else {
                tag.append("<error id=\"").append(order.id).append("\">Does not belong to you</error>");
                break;
            }

        }
    }

    /**
     * Handle cancel request and do actual cancel, form xml results
     * @param orderID transaction id
     * @param resultFormer form xml
     */
    void handleCancel(int orderID, StringBuilder resultFormer, int accountID) {
        // find in open orders
        StringBuilder tag = new StringBuilder("<canceled id=\"" + orderID + "\">");
        synchronized (matchWorker.dbLock) {
            Order orderInOpen = dbProcess.findOrder(orderID);
            ArrayList<Order> ordersInExecuted = dbProcess.findOrderInExecuted(orderID);
            if (orderInOpen != null) {
                if (orderInOpen.account_num == accountID) {
                    // cancel: 1: delete from open orders. 2: insert a new cancel order.
                    // cancel: 3: if it is buy order, refund. 4: if it's sell order, restore shares of that symbol
                    // find in executed orders, append results
                    orderInOpen.setCancelDate(LocalDateTime.now());
                    dbProcess.deleteOpenOrder(orderInOpen.id);
                    dbProcess.insertCanceledOrder(orderInOpen);
                    tag.append("<canceled shares=\"").append(orderInOpen.amount);
                    tag.append("\" time=\"").append(orderInOpen.getEpochInSeconds(orderInOpen.getCanceledDate()));
                    tag.append("\"/>");
                    if (orderInOpen.amount >= 0) { // buying
                        Account account = dbProcess.findAccount(orderInOpen.account_num);
                        double newBalance = account.getBalance() + orderInOpen.amount * orderInOpen.limit_price;
                        dbProcess.updateAccountBalance(account.getAccountNum(), newBalance);
                    }
                    else { // selling
                        double amount = dbProcess.findAmountOfSymbolOfAccount(orderInOpen.symbol, orderInOpen.account_num);
                        double newAmount = amount - orderInOpen.amount;
                        dbProcess.updateAccountAmount(orderInOpen.symbol, orderInOpen.account_num, newAmount);
                    }
                }
                else {
                    tag.append("<error id=\"").append(orderID).append("\">Does not belong to you</error>");
                }
            }
            processExecutedOrders(tag, ordersInExecuted, accountID);
        }
        // wrap <cancel> tags
        tag.append("</canceled>");
        resultFormer.append(tag.toString());
    }

    /**
     * Handle order that buy a certain amount of symbol
     * @param order order
     * @param account account that issue the order
     * @param resultFormer form result xml
     */
    void handleBuyOrder(Order order, Account account, StringBuilder resultFormer) {
        double totalCost = order.amount * order.limit_price;
        if (account.getBalance() < totalCost) {
            String reason = "Account does not have sufficient funds.";
            resultFormer.append(domCreate.getAccountInvalidTag(order.symbol, order.amount, order.limit_price, reason));
        }
        else {
            //inserting order & update account balance
            synchronized (matchWorker.dbLock) {
                order.setStoreDate(LocalDateTime.now());
                dbProcess.insertOrder(order);
                dbProcess.updateAccountBalance(order.account_num, account.getBalance()- totalCost);
                resultFormer.append(domCreate.getOrderOpenTag(order.symbol, order.amount, order.limit_price, order.id));
                matchWorker.pushToQueue(order);
            }
            synchronized (matchWorker.lock) {
                matchWorker.lock.notifyAll();
            }
        }
    }

    /**
     * Handle selling order
     * @param order selling order
     * @param resultFormer form result
     */
    void handleSellOrder(Order order, StringBuilder resultFormer) {
        double currPosition = dbProcess.findAmountOfSymbolOfAccount(order.symbol, order.account_num);
        double sellingAmt = -1 * order.amount;
        if (currPosition < sellingAmt) { //not sufficient
            String reason = "Account does not have sufficient shares of the Symbol.";
            resultFormer.append(domCreate.getAccountInvalidTag(order.symbol, order.amount, order.limit_price, reason));
        }
        else {
            synchronized (matchWorker.dbLock) {
                order.setStoreDate(LocalDateTime.now());
                dbProcess.insertOrder(order);
                dbProcess.updateAccountAmount(order.symbol, order.account_num, currPosition - sellingAmt);
                resultFormer.append(domCreate.getOrderOpenTag(order.symbol, order.amount, order.limit_price, order.id));
                matchWorker.pushToQueue(order);
            }
            synchronized (matchWorker.lock) {
                matchWorker.lock.notifyAll();
            }
        }
    }

    /**
     * Check whether ID exist in the DB
     * @param order order info
     * @param account account to check
     * @param resultFormer to form result xml
     * @return true if account exists, false otherwise
     */
    boolean checkAccountID(Order order, Account account, StringBuilder resultFormer) {
        if (account == null) {
            resultFormer.append(domCreate.getAccountInvalidTag(order.symbol, order.amount, order.limit_price, "Account does not exist."));
            return false;
        }
        return true;
    }

    /**
     * Check account not exist and form corresponding result in create state
     * @param id id to check
     * @return true if not exist, false otherwise
     */
    public boolean checkAccountExist(int id) {
        if (dbProcess.findAccount(id) != null) {
            return true;
        }
        return false;
    }

    /**
     * Synchronized creating symbol
     * @param sym symbol to create
     * @param accountID account that create
     * @param amount amount to put
     * @param resultFormer form xml
     */
    public void handleSymbolCreate(String sym, int accountID, double amount, StringBuilder resultFormer) {
        synchronized (symbolCreateLock) {
            if (amount < 0) {
                resultFormer.append(domCreate.getInvalidSymbolTags("Negative amount", accountID, sym));
            }
            else if (checkAccountExist(accountID)) {
                ArrayList<Account> accounts = dbProcess.findAccountSymbol(sym);
                for (Account account : accounts) {
                    if (account.getAccountNum() == accountID) {
                        // update symbol account
                        double newAmount = account.getSymbolAmount(sym) + amount;
                        dbProcess.updateAccountAmount(sym, accountID, newAmount);
                        resultFormer.append(domCreate.getCreatedSymbolTag(accountID, sym));
                        return;
                    }
                }
                // insert symbol account
                dbProcess.addNewAccountSymbolMapping(accountID, sym, amount);
                resultFormer.append(domCreate.getCreatedSymbolTag(accountID, sym));
            }
            else {
                resultFormer.append(domCreate.getInvalidSymbolTags("Account does not exist.", accountID, sym));
            }
        }
    }
}
