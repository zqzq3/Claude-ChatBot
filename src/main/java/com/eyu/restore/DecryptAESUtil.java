package com.eyu.restore;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class DecryptAESUtil {

    // 加密
    public static String encrypt(String strToEncrypt, String secret) {
        try {
            // 生成密钥
            byte[] key = secret.getBytes("UTF-8");
            SecretKeySpec secretKey = new SecretKeySpec(key, "AES");

            // 创建加密工具
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            // 加密
            byte[] encryptedBytes = cipher.doFinal(strToEncrypt.getBytes("UTF-8"));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // 解密
    public static String decrypt(String strToDecrypt, String secret) {
        try {
            // 生成密钥
            byte[] key = secret.getBytes("UTF-8");
            SecretKeySpec secretKey = new SecretKeySpec(key, "AES");

            // 创建解密工具
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);

            // 解密
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(strToDecrypt));
            return new String(decryptedBytes, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {
        final String secretKey = "xxxxxxxxxxxxxxxx"; // 16位秘钥

        //以下为测试内容 无法正常解密
        String encryptedString = "aHqhuJ5XVH6+bvi7dyiqExr8jNh1ZTTChKU0MyVIfXD+uuj9kvzroQujVZY3x31vMDh/8FmHUEgeIWyj0RNWCA4mpmYnCGwxWkB7DAUzrNopffzXoUoXogudF9qDK+JcGHznhUkAjjIDJuEQGOpJLxgBORYflnxQVAj788aYhCnhrKwCJajh8DhmfeaSt7jZ+Q4+zgWaFn6rMgDbL32BMQ==";
        String decryptedString = DecryptAESUtil.decrypt(encryptedString, secretKey);
        System.out.println("解密后的字符串: " + decryptedString);
    }
}
