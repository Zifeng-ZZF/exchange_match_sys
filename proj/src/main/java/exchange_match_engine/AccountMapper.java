package exchange_match_engine;

import org.apache.ibatis.annotations.Param;

public interface AccountMapper {
    Account selectAccountById(int id);

    void insertNewAccount(@Param("id") int id, @Param("balance") double balance);

    void updateBalance(@Param("id") int id, @Param("newBalance") double balance);
}
