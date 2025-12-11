//package com.example.jpa_demo;
//
//public class User {
//}
package com.example.jpa_demo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import java.util.ArrayList;
import java.util.List;
import java.io.Serializable;

@Entity
@Table(name = "users")
@Data
public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String email;
    private String phone;
    // 🌟 核心：一对多
    // mappedBy = "user" 表示：我这边不存外键，外键在 Order 类的 "user" 字段那边
    // cascade = ALL 表示：如果你保存/删除了 User，请把他的 Order 也顺便保存/删除
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonIgnore
    @ToString.Exclude // ⚠️ 防止打印日志时死循环 (User打印Order, Order打印User...)
    private List<Order> orders = new ArrayList<>();

    @Column(
            name="nickname",
            nullable=true,
            length=20,
            unique=true
    )
    private String nickname;
}