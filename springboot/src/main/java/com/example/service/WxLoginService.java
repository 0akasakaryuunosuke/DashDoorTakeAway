package com.example.service;

import com.example.common.enums.RoleEnum;
import com.example.entity.Account;
import com.example.entity.User;
import com.example.mapper.UserMapper;
import com.example.utils.TokenUtils;
import com.example.utils.UsernameGeneratorUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.ibatis.jdbc.Null;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;


import java.util.HashMap;
import java.util.Map;
@Service
public class WxLoginService {

    @Value("${wechat.app-id}")
    private String appId;

    @Value("${wechat.secret}")
    private String secret;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private UserMapper userMapper;

    /**
     * 使用微信登录凭证 Code 登录
     */
        @Transactional
    public Account  loginWithWxCodeAndUserInfo(String code, String nickName, String avatarUrl) throws Exception {
        // 拼接微信登录接口 URL
//        , String nickName, String avatarUrl
        String url = String.format(
            "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
            appId, secret, code
        );

        // 调用微信登录接口
        ResponseEntity<String> responseEntity = restTemplate.getForEntity(url, String.class);
        String body = responseEntity.getBody();

        // 解析微信返回的数据
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> result;
        result = objectMapper.readValue(body, Map.class);

        if (result.get("errcode") != null) {
            throw new Exception("微信登录失败：" + result.get("errmsg"));
        }

        // 获取 openid 和 session_key
        String openid = (String) result.get("openid");

      User user = userMapper.findByOpenid(openid);
	if (user == null) {
           if (nickName == null || nickName.isEmpty()) {
            nickName = "外卖用户" + (int) ((Math.random() * 9 + 1) * 100000); // 6位随机数字
        }
        if (avatarUrl == null || avatarUrl.isEmpty()) {
            avatarUrl = "https://oss-jlx.oss-cn-beijing.aliyuncs.com/30fa170c-fb01-4c28-a731-a586bcd9fdb6.jpeg";
        }
  String username = UsernameGeneratorUtil.generateUsernameFromTimestamp();
		// 如果用户不存在，自动注册
		user = new User();
		user.setOpenid(openid);
		user.setRole("USER");
        user.setPassword("123456"); // 存储昵称
         user.setUsername(username);
        user.setName(nickName);
        user.setAvatar(avatarUrl);
		userMapper.insertUser(user);
        user = userMapper.findByOpenid(openid);
	}
	// 返回登录成功的信息
            Account account=user;
             String tokenData = user.getId() + "-" + RoleEnum.USER.name();
       String token = TokenUtils.createToken(tokenData, user.getPassword());
        account.setToken(token);
   return account;

}


}
