//package com.example.jpa_demo;
//
//public class Order {
//}
package com.example.jpa_demo;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;

@Entity
@Table(name = "orders")
@Data
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_number")
    private String orderNumber;

    private BigDecimal amount;

    // 🌟 核心：多对一
    // JoinColumn 告诉 JPA：数据库表里的外键列名叫 "user_id"
    @ManyToOne
    @JoinColumn(name = "user_id")
    @ToString.Exclude
    private User user;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Transient
    public BigDecimal getAmountWithTax() {
        if(amount == null) {
            return BigDecimal.ZERO;
        }
        return amount.multiply(new BigDecimal("1.10")); // 假设税率是10%
    }
    @Version // 核心注解：JPA 会自动维护它，不需要手动 setVersion
    private Integer version;
}