package com.leyou.upload.service;

import com.aliyun.oss.OSS;
import com.aliyun.oss.common.utils.BinaryUtil;
import com.aliyun.oss.model.MatchMode;
import com.aliyun.oss.model.PolicyConditions;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.upload.config.OSSProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;

@Service
public class UploadService {


    // 支持的文件类型
    private static final List<String> suffixes = Arrays.asList("image/png", "image/jpeg", "image/bmp");


    public String uploadImage(MultipartFile file) {

        //1,校验文档类型
        if (!suffixes.contains(file.getContentType())) {
            throw new LyException(ExceptionEnum.INVALID_REQUEST_PARAM);
        }

        //2，校验文件内容,ImageIO专门用来读取图片，如果可以读取，说明是图片
        try {
            BufferedImage read = ImageIO.read(file.getInputStream());

            if (null == read) {//说明给的文件不是图片不能读取
                throw new LyException(ExceptionEnum.INVALID_REQUEST_PARAM);
            }
        } catch (IOException e) {//说明给资源流是空的
            throw new LyException(ExceptionEnum.INVALID_REQUEST_PARAM);
        }

        //3,io读写

        //获取上传的文件的名称
        String originalFilename = file.getOriginalFilename();

        try {
            //把文件保存到nginx中
            file.transferTo(new File("D:/develop/nginx-heima88/html/" + originalFilename));
        } catch (IOException e) {
            throw new LyException(ExceptionEnum.FILE_SAVE_ERROR);
        }

        return "http://image.leyou.com/" + originalFilename;
    }


    @Autowired
    private OSSProperties prop;

    @Autowired
    private OSS client;

    // ...

    public Map<String, Object> getSignature() {
        try {
            long expireTime = prop.getExpireTime();
            long expireEndTime = System.currentTimeMillis() + expireTime * 1000;
            Date expiration = new Date(expireEndTime);
            PolicyConditions policyConds = new PolicyConditions();
            policyConds.addConditionItem(PolicyConditions.COND_CONTENT_LENGTH_RANGE, 0, prop.getMaxFileSize());
            policyConds.addConditionItem(MatchMode.StartWith, PolicyConditions.COND_KEY, prop.getDir());

            String postPolicy = client.generatePostPolicy(expiration, policyConds);
            byte[] binaryData = postPolicy.getBytes("utf-8");
            String encodedPolicy = BinaryUtil.toBase64String(binaryData);
            String postSignature = client.calculatePostSignature(postPolicy);

            Map<String, Object> respMap = new LinkedHashMap<>();
            respMap.put("accessId", prop.getAccessKeyId());
            respMap.put("policy", encodedPolicy);
            respMap.put("signature", postSignature);
            respMap.put("dir", prop.getDir());
            respMap.put("host", prop.getHost());
            respMap.put("expire", expireEndTime);
            return respMap;
        } catch (Exception e) {
            throw new LyException(ExceptionEnum.FILE_SAVE_ERROR);
        }
    }
}
