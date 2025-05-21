package com.example.utils;

public class UsernameGeneratorUtil {

    /**
     * 生成基于当前时间戳的六位随机用户名
     * @return 随机生成的用户名
     */
    public static String generateUsernameFromTimestamp() {
        // 获取当前时间戳（毫秒）
        long timestamp = System.currentTimeMillis();

        // 将时间戳转换为字符串
        String timestampStr = Long.toString(timestamp);

        // 提取时间戳的后六位（确保足够随机）
        String randomPart = timestampStr.substring(timestampStr.length() - 6);

        // 将这六位数字与随机字母混合
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder username = new StringBuilder();

        // 在时间戳后面追加一个随机字母（确保多样性）
        for (int i = 0; i < 1; i++) {  // 可以调整生成的字母数量
            int randomIndex = (int) (Math.random() * chars.length());
            username.append(chars.charAt(randomIndex));
        }

        // 拼接生成的用户名
        return randomPart + username.toString();
    }

    // 其他可能的辅助方法可以在这里继续添加
}
