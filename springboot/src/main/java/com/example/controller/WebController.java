package com.example.controller;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.example.common.Result;
import com.example.common.enums.ResultCodeEnum;
import com.example.common.enums.RoleEnum;
import com.example.entity.Account;
import com.example.service.AdminService;
import com.example.service.BusinessService;
import com.example.service.UserService;
import com.example.service.WxLoginService;

import com.example.utils.AliOssUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

/**
 * 基础前端接口
 */
@RestController
public class WebController {

    @Resource
    private AdminService adminService;

    @Resource
    private BusinessService businessService;

    @Resource
    private UserService userService;
   @Resource
    private WxLoginService wxLoginService;
    @GetMapping("/")
    public Result hello() {
        return Result.success("访问成功");
    }

    @Autowired
    private AliOssUtil  aliOssUtil;

    /**
     * 登录
     */
    @PostMapping("/login")
    public Result login(@RequestBody Account account) {
        if (ObjectUtil.isEmpty(account.getUsername()) || ObjectUtil.isEmpty(account.getPassword())
                || ObjectUtil.isEmpty(account.getRole())) {
            return Result.error(ResultCodeEnum.PARAM_LOST_ERROR);
        }
        if (RoleEnum.ADMIN.name().equals(account.getRole())) {
            account = adminService.login(account);
        } else if (RoleEnum.BUSINESS.name().equals(account.getRole())) {
            account = businessService.login(account);
        } else if (RoleEnum.USER.name().equals(account.getRole())) {
            account = userService.login(account);
        }
        return Result.success(account);
    }

    /**
     * 注册
     */
    @PostMapping("/register")
    public Result register(@RequestBody Account account) {
        if (StrUtil.isBlank(account.getUsername()) || StrUtil.isBlank(account.getPassword())
                || ObjectUtil.isEmpty(account.getRole())) {
            return Result.error(ResultCodeEnum.PARAM_LOST_ERROR);
        }
        if (RoleEnum.BUSINESS.name().equals(account.getRole())) {
            businessService.register(account);
        } else  if (RoleEnum.USER.name().equals(account.getRole())) {
            userService.register(account);
        }
        return Result.success();
    }

    /**
     * 修改密码
     */
    @PutMapping("/updatePassword")
    public Result updatePassword(@RequestBody Account account) {
        if (StrUtil.isBlank(account.getUsername()) || StrUtil.isBlank(account.getPassword())
                || ObjectUtil.isEmpty(account.getNewPassword())) {
            return Result.error(ResultCodeEnum.PARAM_LOST_ERROR);
        }
        if (RoleEnum.ADMIN.name().equals(account.getRole())) {
            adminService.updatePassword(account);
        } else if (RoleEnum.BUSINESS.name().equals(account.getRole())) {
            businessService.updatePassword(account);
        }
        return Result.success();
    }

  @PostMapping("/upload")
    public String upload(@RequestParam("file") MultipartFile file){

        try {
            // 获取文件原始文件名
            String originalFilename = file.getOriginalFilename();
            // 拿到文件后缀
            String last = originalFilename.substring(originalFilename.lastIndexOf("."));
            // 使用UUID拼接文件名
            String objectName = UUID.randomUUID().toString() + last;
            return aliOssUtil.upload(file.getBytes(), objectName);
        } catch (IOException e) {
           // log.error("文件上传失败");
            e.printStackTrace();
        }
        return "PARAM_LOST_ERROR";
    }
 @PostMapping("/wx-login")
    public Result wxLogin(@RequestBody Map<String, String> requestBody) {
        String code = requestBody.get("code");
    String nickName = requestBody.get("nickName");
    String avatarUrl = requestBody.get("avatarUrl");

        if (code == null || code.isEmpty()) {
           return Result.error(ResultCodeEnum.PARAM_LOST_ERROR);
        }

        try {
            // 调用服务层处理微信登录逻辑
           Account account=wxLoginService.loginWithWxCodeAndUserInfo(code, nickName, avatarUrl);
         return Result.success(account);
        } catch (Exception e) {
        return Result.error(ResultCodeEnum.PARAM_LOST_ERROR);
        }
    }

}
