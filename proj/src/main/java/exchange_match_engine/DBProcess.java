package exchange_match_engine;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;

public class DBProcess {
    private final SqlSessionFactory factory;
    public DBProcess() throws IOException {
        InputStream in = Resources.getResourceAsStream("mybatis-config.xml");
        factory = new SqlSessionFactoryBuilder().build(in);
    }

    //------------------------------------
    //   Order tables related
    //------------------------------------

    Order findOrder(int id) {
        try (SqlSession session = factory.openSession()) {
            OrderMapper mapper = session.getMapper(OrderMapper.class);
            Order order = mapper.selectOrderById(id);
            return order;
        }
    }

    public Order findOrderInCancel(int orderID) {
        try (SqlSession session = factory.openSession()) {
            OrderMapper mapper = session.getMapper(OrderMapper.class);
            HashMap<String, Object> result = mapper.selectCancelByID(orderID);
            if (result != null && !result.isEmpty()) {
                double amount = (Float) result.get("amount");
                double price = (Float) result.get("limit_price");
                String symbol = (String) result.get("symbol");
                int accountID = (Integer) result.get("account_num");
                LocalDateTime cancelDate = ((Timestamp) result.get("canceled_date")).toLocalDateTime();
                LocalDateTime createDate = ((Timestamp) result.get("create_date")).toLocalDateTime();
                Order order = new Order(amount, price, symbol, accountID);
                order.setCancelDate(cancelDate);
                order.setStoreDate(createDate);
                order.id = (Integer) result.get("id");
                return order;
            }
            return null;
        }
    }

    public ArrayList<Order> findOrderInExecuted(int orderID) {
        try (SqlSession session = factory.openSession()) {
            OrderMapper mapper = session.getMapper(OrderMapper.class);
            ArrayList<HashMap<String, Object>> results = mapper.selectExecutedByID(orderID);
            ArrayList<Order> orderRes = new ArrayList<>();
            if (!results.isEmpty()) {
                for (HashMap<String, Object> entry : results) {
                    double amount = (Float) entry.get("amount");
                    double price = (Float) entry.get("final_price");
                    String symbol = (String) entry.get("symbol");
                    int accountID = (Integer) entry.get("account_num");
                    LocalDateTime executedDate = ((Timestamp) entry.get("executed_date")).toLocalDateTime();
                    LocalDateTime createDate = ((Timestamp) entry.get("create_date")).toLocalDateTime();
                    Order order = new Order(amount, price, symbol, accountID);
                    order.setExecutedDate(executedDate);
                    order.setStoreDate(createDate);
                    order.id = (Integer) entry.get("id");
                    orderRes.add(order);
                }
            }
            return orderRes;
        }
    }

    public void insertOrder(Order order) {
        try (SqlSession session = factory.openSession()) {
            OrderMapper mapper = session.getMapper(OrderMapper.class);
            order.setStoreDate(LocalDateTime.now());
            mapper.insertAnOrder(order, order.account_num, order.symbol, order.amount, order.limit_price, order.getCreateDateTime());
            session.commit();
        }
    }

    public void insertCanceledOrder(Order order) {
        try (SqlSession session = factory.openSession()) {
            OrderMapper mapper = session.getMapper(OrderMapper.class);
            order.setCancelDate(LocalDateTime.now());
            mapper.insertACancelOrder(order.id, order.account_num, order.symbol, order.amount,
                    order.limit_price, order.getCreateDateTime(),order.getCanceledDate());
            session.commit();
        }
    }

    public void deleteOpenOrder(int id) {
        try (SqlSession session = factory.openSession()) {
            OrderMapper mapper = session.getMapper(OrderMapper.class);
            mapper.deleteAnOpenOrder(id);
            session.commit();
        }
    }

    public ArrayList<Order> findMatchedSell(Order order) {
        try (SqlSession session = factory.openSession()) {
            OrderMapper mapper = session.getMapper(OrderMapper.class);
            // amount < 0
            // price  <= order.price
            // sort by price asc
            // second sort by create time asc
            ArrayList<HashMap<String, Object>> results =
                    mapper.selectOpenSellSortedPriceAndTime(order.limit_price, order.symbol, order.account_num);
            return getOrders(results);
        }
    }

    public ArrayList<Order> findMatchedBuy(Order order) {
        try (SqlSession session = factory.openSession()) {
            OrderMapper mapper = session.getMapper(OrderMapper.class);
            // amount > 0
            // price  <= order.price
            // sort by price desc
            // second sort by create time asc
            ArrayList<HashMap<String, Object>> results =
                    mapper.selectOpenBuySortedPriceAndTime(order.limit_price, order.symbol, order.account_num);
            return getOrders(results);
        }
    }

    private ArrayList<Order> getOrders(ArrayList<HashMap<String, Object>> results) {
        ArrayList<Order> orderList = new ArrayList<>();
        for (HashMap<String, Object> entry : results) {
            double amount = (Float) entry.get("amount");
            double price = (Float) entry.get("limit_price");
            String symbol = (String) entry.get("symbol");
            int accountID = (Integer) entry.get("account_num");
            LocalDateTime createDate = ((Timestamp) entry.get("create_date")).toLocalDateTime();
            Order matchedOrder = new Order(amount, price, symbol, accountID);
            matchedOrder.setStoreDate(createDate);
            matchedOrder.id = (Integer) entry.get("id");
            orderList.add(matchedOrder);
        }
        return orderList;
    }

    public void insertExecutedOrder(Order executed) {
        try (SqlSession session = factory.openSession()) {
            OrderMapper mapper = session.getMapper(OrderMapper.class);
            mapper.insertAnExecutedOrder(executed.id, executed.account_num, executed.symbol, executed.amount,
                    executed.limit_price, executed.getCreateDateTime(), executed.getExecutedDate());
            session.commit();
        }
    }

    public void updateOpenOrderAmount(int id, double amount) {
        try (SqlSession session = factory.openSession()) {
            OrderMapper mapper = session.getMapper(OrderMapper.class);
            mapper.updateOpenOrderAmount(id, amount);
            session.commit();
        }
    }

    //------------------------------------
    //     Account tables related
    //------------------------------------

    public void updateAccountBalance(int accountID, double newBalance) {
        try (SqlSession session = factory.openSession()) {
            AccountMapper mapper = session.getMapper(AccountMapper.class);
            mapper.updateBalance(accountID, newBalance);
            session.commit();
        }
    }

    Account findAccount(int id) {
        try (SqlSession session = factory.openSession()) {
            AccountMapper mapper = session.getMapper(AccountMapper.class);
            Account account = mapper.selectAccountById(id);
            return account;
        }
    }

    void addNewAccount(Account account) {
        try (SqlSession session = factory.openSession()) {
            AccountMapper mapper = session.getMapper(AccountMapper.class);
            mapper.insertNewAccount(account.getAccountNum(), account.getBalance());
            session.commit();
        }
    }

    //----------------------------------------------------
    //   Account Symbol/Position tables related
    //----------------------------------------------------

    ArrayList<Account> findAccountSymbol(String sym) {
        try (SqlSession session = factory.openSession()) {
            ArrayList<Account> res = new ArrayList<>();
            AccountMapper accountMapper = session.getMapper(AccountMapper.class);
            AccountSymbolMapper accountSymbolMapper = session.getMapper(AccountSymbolMapper.class);
            ArrayList<HashMap<String, Object>> resultSet = accountSymbolMapper.selectAccountBySymbol(sym);
            for (HashMap<String, Object> map : resultSet) {
                int accountID = (Integer)map.get("account_num");
                Account account = accountMapper.selectAccountById(accountID);
                String symbol = (String)map.get("symbol");
                double amount = (Float)map.get("amount");
                account.putSymbol(symbol, amount);
                res.add(account);
            }
            return res;
        }
    }

    float findAmountOfSymbolOfAccount(String sym, int accountID) {
        try (SqlSession session = factory.openSession()) {
            AccountSymbolMapper mapper = session.getMapper(AccountSymbolMapper.class);
            HashMap<String, Object> mapping = mapper.selectAccntSym(accountID, sym);
            if (mapping == null) {
                return -1;
            }
            return (Float)mapping.get("amount");
        }
    }


    void addNewAccountSymbolMapping(int accountID, String sym, double amount) {
        try (SqlSession session = factory.openSession()) {
            AccountSymbolMapper mapper = session.getMapper(AccountSymbolMapper.class);
            mapper.insertNewAccountSymMapping(accountID, sym, amount);
            session.commit();
        }
    }

    void updateAccountAmount(String sym, int accountID, double newAmount) {
        try (SqlSession session = factory.openSession()) {
            AccountSymbolMapper mapper = session.getMapper(AccountSymbolMapper.class);
            mapper.updateAccountSymMapping(accountID, sym, newAmount);
            session.commit();
        }
    }


}
