package exchange_match_engine;

import org.apache.ibatis.annotations.Param;

import java.util.ArrayList;
import java.util.HashMap;

public interface AccountSymbolMapper {

    HashMap<String, Object> selectAccntSym(@Param("accountID") int accountID, @Param("symbol") String symbol);

    ArrayList<HashMap<String, Object>> selectAccountBySymbol(@Param("symbol") String symbol);

//    ArrayList<HashMap<String, Object>> selectSymbolByAccount(@Param("accountNum") int accountID);

    void insertNewAccountSymMapping(@Param("accountID") int accountID, @Param("symbol") String symbol, @Param("amount") double amount);

    void updateAccountSymMapping(@Param("accountID") int accountID, @Param("symbol") String symbol, @Param("amount") double newAmount);
}
