//package com.example.jpa_demo;
//
//public interface UserRepository {
//}
package com.example.jpa_demo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    // 🌟 覆盖默认的 findAll 方法
    // 使用 "JOIN FETCH" 关键字，强制一次性把 orders 抓取出来
    @Query("SELECT DISTINCT u FROM User u JOIN FETCH u.orders")
    List<User> findAll();
    User findByUsername(String username);
}