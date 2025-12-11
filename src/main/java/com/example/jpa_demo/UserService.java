package com.example.jpa_demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    // ❌ 实验 A：不加 @Transactional
    // 预期：因为没有事务保护，saveAndFlush 会立刻提交，后面的异常无法撤销它。
    public void createUserNoTransaction() {
        User u = new User();
        u.setUsername("No-Trans-User");
        u.setEmail("error@test.com");
        // 🟢 关键修改1：缩短前缀，防止超长 (3字符 + 13数字 = 16字符 < 20)
        u.setNickname("NT-" + System.currentTimeMillis());

        // 🟢 关键修改2：使用 saveAndFlush 强制刷盘
        userRepository.saveAndFlush(u);

        System.out.println(">>> (无事务) 用户已强制写入数据库，准备抛出异常...");

        // 模拟异常
        throw new RuntimeException("模拟的业务异常！");
    }

    // ✅ 实验 B：加上 @Transactional
    // 预期：虽然 save 成功了，但异常会导致整个事务回滚，数据消失。
    @Transactional
    public void createUserWithTransaction() {
        User u = new User();
        u.setUsername("With-Trans-User");
        u.setEmail("rollback@test.com");
        // 关键修改：缩短前缀
        u.setNickname("WT-" + System.currentTimeMillis());

        userRepository.save(u);

        System.out.println(">>> (有事务) 用户已保存，准备抛出异常...");

        // 模拟异常
        throw new RuntimeException("事务中的业务异常！");
    }

    //1.根据ID查用户
    @Cacheable(value = "users", key = "#id")
    public User getUserById(Long id) {
        System.out.println("🧐 缓存未命中，正在查询数据库...");
        return userRepository.findById(id).orElseThrow(()-> new RuntimeException("用户未找到"));
    }

    //2.创建用户
    // 写：清空缓存
    // 一旦修改或删除了用户，必须把缓存里的旧数据删掉，否则会读到脏数据
    public User createUser(User user) {
        return userRepository.save(user);
    }
}