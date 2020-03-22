package com.leyou.common.auth;

import com.leyou.common.auth.entity.AppInfo;
import com.leyou.common.auth.entity.Payload;
import com.leyou.common.auth.entity.UserInfo;
import com.leyou.common.auth.utils.JwtUtils;
import com.leyou.common.auth.utils.RsaUtils;
import org.junit.Test;

import java.security.PrivateKey;
import java.security.PublicKey;


public class AuthTest {

    private String privateFilePath = "D:/heima/rsa/id_rsa";
    private String publicFilePath = "D:/heima/rsa/id_rsa.pub";

    @Test
    public void testRSA() throws Exception {
        // 生成密钥对
        RsaUtils.generateKey(publicFilePath, privateFilePath, "hello", 2048);

        // 获取私钥
        PrivateKey privateKey = RsaUtils.getPrivateKey(privateFilePath);
        System.out.println("privateKey = " + privateKey);
        // 获取公钥
        PublicKey publicKey = RsaUtils.getPublicKey(publicFilePath);
        System.out.println("publicKey = " + publicKey);
    }

    @Test
    public void testJWT() throws Exception {
        // 获取私钥
        PrivateKey privateKey = RsaUtils.getPrivateKey(privateFilePath);
        // 生成token
        String token = JwtUtils.generateTokenExpireInMinutes(new UserInfo(10086L, "heima88", "admin"), privateKey, 5);
        System.out.println("token = " + token);


        String newToken = "eyJhbGciOiJSUzI1NiJ9.eyJ1c2VyIjoie1wiaWRcIjoxLFwic2VydmljZU5hbWVcIjpcInVzZXItc2VydmljZVwiLFwidGFyZ2V0TGlzdFwiOlsxLDIsMyw0LDUsNiw3LDgsOV19IiwianRpIjoiT0dZNU1XUXdPRGd0TVRkaU5TMDBNREZqTFRnNVpqWXROVEkxWlRjeFl6a3daalZsIiwiZXhwIjoxNTc4MDM5OTM3fQ.pDjX9qbS_XVgwkdiFLX3gNS4dIy4AEdjKLqH7rdZb5DghHsiJkj0xk-pg9kOYxOMD5dyKEQeOX7YjGh74A626qeAK5VMGH5HCLx5EeNe6ISBYoJHEgXQmcn6l2Gsc7l3GXAi2A71fYok-o1aC9OmENeHzEh00OQNsfTjnh-2am1NxV_k0cvjZWcDpAqzr02R40BGGdB0ou1O78nwfZRHuKZWDO0yKwohOJImaRA7hN6TDLWRSvXV4f7jbVKhcOZ7EHPIem1J45eO8-9Q_oC_je2r1kJ7G23l0KheqGk821-aekwo3sWW8h-ICX-j-UrLlUBzZWJiH-FKfcAbuvfO0w";
        // 获取公钥
        PublicKey publicKey = RsaUtils.getPublicKey(publicFilePath);
        // 解析token
        Payload<AppInfo> info = JwtUtils.getInfoFromToken(newToken, publicKey, AppInfo.class);

        System.out.println("info.getExpiration() = " + info.getExpiration());
        System.out.println("info.getInfo() = " + info.getInfo());
        System.out.println("info.getId() = " + info.getId());
    }
}