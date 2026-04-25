package com.demo.crud.service;

import com.demo.crud.exception.BusinessException;
import com.demo.crud.mapper.UserMapper;
import com.demo.crud.model.User;
import com.demo.crud.model.UserRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public Map<String, Object> findAll(int page, int size) {
        int offset = (page - 1) * size;
        List<User> users = userMapper.findAll(offset, size);
        int total = userMapper.countAll();
        Map<String, Object> result = new HashMap<>();
        result.put("list", users);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        result.put("pages", (total + size - 1) / size);
        return result;
    }

    public User findById(Long id) {
        User user = userMapper.findById(id);
        if (user == null) throw new BusinessException(404, "用户不存在，id=" + id);
        return user;
    }

    @Transactional
    public User create(UserRequest request) {
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .phone(request.getPhone())
                .build();
        userMapper.insert(user);
        log.info("新增用户成功，id={}, username={}", user.getId(), user.getUsername());
        return user;
    }

    @Transactional
    public User update(Long id, UserRequest request) {
        findById(id);
        User user = User.builder()
                .id(id)
                .username(request.getUsername())
                .email(request.getEmail())
                .phone(request.getPhone())
                .build();
        userMapper.update(user);
        log.info("更新用户成功，id={}", id);
        return user;
    }

    @Transactional
    public void delete(Long id) {
        findById(id);
        userMapper.deleteById(id);
        log.info("删除用户成功，id={}", id);
    }
}
