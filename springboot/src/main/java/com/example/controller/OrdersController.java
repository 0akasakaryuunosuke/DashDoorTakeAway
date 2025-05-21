package com.example.controller;

import com.example.common.Result;
import com.example.entity.Business;
import com.example.entity.Orders;
import com.example.entity.OrdersDTO;
import com.example.service.BusinessService;
import com.example.service.OrdersService;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 前端操作接口
 **/
@RestController
@RequestMapping("/orders")
public class OrdersController {
    private static final String KEY_PREFIX  = "status"; // 确保键名与 Redis 中一致
    @Resource
    private OrdersService ordersService;
       @Resource
       private BusinessService businessService;
           @Autowired
    private RedisTemplate<String, Integer> redisTemplate;


    @PostMapping("/addOrder")
    public Result addOrder(@RequestBody OrdersDTO orders) {
        ordersService.addOrder(orders);
        return Result.success();
    }

    /**
     * 新增
     */
    @PostMapping("/add")
    public Result add(@RequestBody Orders orders) {
        ordersService.add(orders);
        return Result.success();
    }

    /**
     * 删除
     */
    @DeleteMapping("/delete/{id}")
    public Result deleteById(@PathVariable Integer id) {
        ordersService.deleteById(id);
        return Result.success();
    }

    /**
     * 批量删除
     */
    @DeleteMapping("/delete/batch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        ordersService.deleteBatch(ids);
        return Result.success();
    }

    /**
     * 修改
     */
    @PutMapping("/update")
    public Result updateById(@RequestBody Orders orders) {
        ordersService.updateById(orders);
       Business business = businessService.selectBasicBusinessById(orders.getBusinessId());
         String username = business.getUsername();
        if (username == null || username.isEmpty()) {
            return Result.success();
        }

        String key = KEY_PREFIX + "-"+username; // 为每个用户生成独立的 Redis 键
        Integer status = redisTemplate.opsForValue().get(key);
        if (status ==0) {
           return  Result.error("-1","该商家已打样");
        }
        return Result.success();
    }

    /**
     * 根据ID查询
     */
    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id) {
        Orders orders = ordersService.selectById(id);
        return Result.success(orders);
    }

    /**
     * 查询所有
     */
    @GetMapping("/selectAll")
    public Result selectAll(Orders orders ) {
        List<Orders> list = ordersService.selectAll(orders);
        return Result.success(list);
    }

    /**
     * 分页查询
     */
    @GetMapping("/selectPage")
    public Result selectPage(Orders orders,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        PageInfo<Orders> page = ordersService.selectPage(orders, pageNum, pageSize);
        return Result.success(page);
    }

}
