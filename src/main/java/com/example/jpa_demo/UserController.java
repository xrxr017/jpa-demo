package com.example.jpa_demo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;
    //1.根据ID查用户
    @GetMapping("/{id}")
    public User getUserById(@PathVariable Long id) {
        System.out.println("收到获取用户请求，ID=" + id);
        return userService.getUserById(id);
    }
    //2.创建用户
    @PostMapping
    public User createUser(@RequestBody User user) {
        System.out.println("收到创建用户请求，用户名=" + user.getUsername());
        return userService.createUser(user);
    }

}

