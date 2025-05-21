package com.example.controller;

import com.example.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 店铺管理控制器
 */
@RestController
@RequestMapping("/shop")
public class ShopController {

    private static final String KEY_PREFIX  = "status"; // 确保键名与 Redis 中一致

    @Autowired
    private RedisTemplate<String, Integer> redisTemplate;

    /**
     * 获取店铺状态
     * @return 返回店铺状态 (0/1)，0 表示已关闭，1 表示营业中
     */
  @PostMapping("/getStatus")
    public Result getStatus(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        if (username == null || username.isEmpty()) {
            return Result.success();
        }

        String key = KEY_PREFIX + "-"+username; // 为每个用户生成独立的 Redis 键
        Integer status = redisTemplate.opsForValue().get(key);
        if (status == null) {
            status = 0; // 默认状态为关闭
        }
        return Result.success(status); // 返回店铺当前的状态
    }

    /**
     * 设置店铺状态
     * @param request 包含用户名和状态
     * @return 更新结果
     */
    @PutMapping("/setStatus")
    public Result setStatus(@RequestBody Map<String, Object> request) {
        String username = (String) request.get("username");
        Integer status = (Integer) request.get("status");

        if (username == null || username.isEmpty()) {
              return Result.success(status); // 返回店铺当前的状态
        }
        if (status == null) {
             return Result.success(status); // 返回店铺当前的状态
        }

        String key = KEY_PREFIX + "-"+ username; // 为每个用户生成独立的 Redis 键
        redisTemplate.opsForValue().set(key, status); // 更新 Redis 中的店铺状态

        return Result.success();
    }
}
