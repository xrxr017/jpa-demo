package com.example.jpa_demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DemoRunner implements CommandLineRunner {

    @Autowired
    private UserService userService; //
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private orderRepository orderRepository;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("====== Day 3: 事务测试开始 ======");

        // --- 测试 A: 无事务 ---
        System.out.println("\n正在执行: 无事务方法...");
        try {
            userService.createUserNoTransaction();
        } catch (RuntimeException e) {
            System.out.println("捕获到异常: " + e.getMessage());
        }

        // 验证 A
//        boolean existsA = userRepository.findAll().stream()
//                .anyMatch(u -> "No-Trans-User".equals(u.getUsername()));
        User dirtyUser = userRepository.findByUsername("No-Trans-User");
        System.out.println("❌ 无事务结果: 脏数据是否存在? " + (dirtyUser != null ? "是 (糟糕! 数据残留)" : "否"));
//        System.out.println("当前数据库里的用户: " + userRepository.findAll().stream().map(User::getUsername).toList());
//        System.out.println("❌ 无事务结果: 脏数据是否存在? " + (existsA ? "是 (糟糕!)" : "否"));


        // --- 测试 B: 有事务 ---
        System.out.println("\n正在执行: 有事务方法...");
        try {
            userService.createUserWithTransaction();
        } catch (RuntimeException e) {
            System.out.println("捕获到异常: " + e.getMessage());
        }

        // 验证 B
//        boolean existsB = userRepository.findAll().stream()
//                .anyMatch(u -> "With-Trans-User".equals(u.getUsername()));
//        System.out.println("✅ 有事务结果: 脏数据是否存在? " + (existsB ? "是 (失败)" : "否 (成功回滚!)"));
        User rolledBackUser = userRepository.findByUsername("With-Trans-User");
        System.out.println("✅ 有事务结果: 脏数据是否存在? " + (rolledBackUser != null ? "是 (失败)" : "否 (成功回滚!)"));

        // ... 前面的事务测试代码 ...

        System.out.println("\n====== Day 3 Part 2: 乐观锁并发测试 ======");

// 1. 先准备一个公共的订单 (假设这是数据库里已有的)
// 为了方便，我们新建一个并保存
        User u = userRepository.findByUsername("With-Trans-User"); // 复用刚才那个回滚测试剩下的用户，或者随便找一个
        if (u == null) {
            // 如果刚才回滚太干净了查不到，就现场造一个
            u = new User();
            u.setUsername("Concurrent-User");
            u.setNickname("CU-" + System.currentTimeMillis());
            userRepository.save(u);
        }

        Order order = new Order();
        order.setOrderNumber("LOCK-TEST-001");
        order.setAmount(new BigDecimal("500.00"));
        order.setStatus(OrderStatus.PENDING);
        order.setUser(u);
        orderRepository.saveAndFlush(order); // 存入库，版本号应该是 0

        Long orderId = order.getId();
        System.out.println("✅ 订单已创建，初始版本号: " + order.getVersion());

// ==========================================
// 🎭 模拟并发场景
// ==========================================

// 2. 管理员 A 打开了订单 (查出来，放在内存里)
        Order adminA_Order = orderRepository.findById(orderId).get();
        System.out.println("管理员 A 读取订单，版本: " + adminA_Order.getVersion());

// 3. 管理员 B 也打开了同一个订单 (查出来，放在内存里)
        Order adminB_Order = orderRepository.findById(orderId).get();
        System.out.println("管理员 B 读取订单，版本: " + adminB_Order.getVersion());

// 4. 管理员 A 修改并提交
        adminA_Order.setStatus(OrderStatus.SHIPPED);
        orderRepository.saveAndFlush(adminA_Order); // 数据库版本变成 1
        System.out.println("✅ 管理员 A 提交成功！");

// 5. 💣 管理员 B (手里还是旧版本 0) 尝试修改并提交
        System.out.println("👉 管理员 B 尝试提交 (预期会炸)...");
        adminB_Order.setStatus(OrderStatus.CANCELLED);

        try {
            orderRepository.saveAndFlush(adminB_Order);
        } catch (Exception e) {
            System.out.println("🔥🔥🔥 捕获到并发冲突异常！");
            System.out.println("异常类型: " + e.getCause().getClass().getName());
            System.out.println("B 的提交被拦截了，数据安全！");
        }
    }


}