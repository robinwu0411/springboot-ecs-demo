package com.demo.crud.controller;

import com.demo.crud.model.Result;
import com.demo.crud.model.User;
import com.demo.crud.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public Result<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(userService.findAll(page, size));
    }

    @GetMapping("/{id}")
    public Result<User> getById(@PathVariable Long id) {
        return Result.success(userService.findById(id));
    }

    @PostMapping
    public Result<User> create(@Valid @RequestBody User user) {
        return Result.success(userService.create(user));
    }

    @PutMapping("/{id}")
    public Result<User> update(@PathVariable Long id, @Valid @RequestBody User user) {
        return Result.success(userService.update(id, user));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return Result.success();
    }
}
