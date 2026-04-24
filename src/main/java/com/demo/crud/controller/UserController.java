package com.demo.crud.controller;

import com.demo.crud.model.Result;
import com.demo.crud.model.User;
import com.demo.crud.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "用户管理", description = "用户 CRUD 接口")
public class UserController {

    private final UserService userService;

    @GetMapping
    @Operation(summary = "查询用户列表", description = "分页查询所有用户")
    public Result<Map<String, Object>> list(
            @Parameter(description = "页码，从1开始") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") int size) {
        return Result.success(userService.findAll(page, size));
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询用户")
    public Result<User> getById(@Parameter(description = "用户ID") @PathVariable Long id) {
        return Result.success(userService.findById(id));
    }

    @PostMapping
    @Operation(summary = "创建用户")
    public Result<User> create(@Valid @RequestBody User user) {
        return Result.success(userService.create(user));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新用户")
    public Result<User> update(@Parameter(description = "用户ID") @PathVariable Long id,
                               @Valid @RequestBody User user) {
        return Result.success(userService.update(id, user));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除用户")
    public Result<Void> delete(@Parameter(description = "用户ID") @PathVariable Long id) {
        userService.delete(id);
        return Result.success();
    }
}
