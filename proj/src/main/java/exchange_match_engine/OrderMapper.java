package exchange_match_engine;

import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;

public interface OrderMapper {

    //-----------------------------------
    //       open orders mapper
    //-----------------------------------

    Order selectOrderById(int id);

    ArrayList<HashMap<String, Object>> selectOpenOrder();

    ArrayList<HashMap<String, Object>> selectOrderByAccountAndSymbol(int id, String sym);

    void insertAnOrder(@Param("order") Order order, @Param("accountNum") int accountNum, @Param("symbol") String sym,
                       @Param("amount") double amount, @Param("limit") double limit, @Param("time")LocalDateTime dateTime);

    void deleteAnOpenOrder(@Param("id") int id);

    ArrayList<HashMap<String, Object>> selectOpenSellSortedPriceAndTime(@Param("limit") double limit_price,
                                                                        @Param("symbol") String symbol, @Param("account") int account);

    ArrayList<HashMap<String, Object>> selectOpenBuySortedPriceAndTime(@Param("limit") double limit_price,
                                                                       @Param("symbol") String symbol, @Param("account") int account);

    void updateOpenOrderAmount(@Param("id") int id, @Param("amount") double amount);

    //-----------------------------------
    //     canceled orders mapper
    //-----------------------------------

    HashMap<String, Object> selectCancelByID(@Param("id") int transactionID);

    void insertACancelOrder(@Param("transactionID") int transactionID, @Param("accountNum") int accountNum, @Param("symbol") String sym,
                               @Param("amount") double amount, @Param("limit") double limit,
                               @Param("create_time") LocalDateTime createTime, @Param("time")LocalDateTime dateTime);

    //-----------------------------------
    //     executed orders mapper
    //-----------------------------------

    ArrayList<HashMap<String, Object>> selectExecutedByID(@Param("id") int transactionID);

    void insertAnExecutedOrder(@Param("transactionID") int transactionID, @Param("accountNum") int accountNum, @Param("symbol") String sym,
                               @Param("amount") double amount, @Param("final_price") double final_price,
                               @Param("create_time") LocalDateTime createTime, @Param("time")LocalDateTime dateTime);

}
