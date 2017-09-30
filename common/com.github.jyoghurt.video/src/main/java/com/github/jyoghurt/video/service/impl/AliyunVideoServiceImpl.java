package com.github.jyoghurt.video.service.impl;

import com.alibaba.fastjson.JSON;
import com.aliyun.signature.Signature;
import com.aliyun.vod.upload.impl.UploadVideoImpl;
import com.aliyun.vod.upload.req.UploadVideoRequest;
import com.aliyun.vod.upload.resp.UploadVideoResponse;
import com.github.jyoghurt.core.exception.BaseErrorException;
import com.github.jyoghurt.http.util.HttpClientUtils;
import com.github.jyoghurt.video.dto.PlayInfoDTO;
import com.github.jyoghurt.video.dto.PublicRequest;
import com.github.jyoghurt.video.service.AliyunVideoService;
import com.github.jyoghurt.video.service.UploadVideoService;
import com.github.jyoghurt.video.util.AliyunUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: DELL
 * Date: 2017/9/30
 * Time: 9:46
 * To change this template use File | Settings | File Templates.
 */
@Service("aiyunVideoService")
public class AliyunVideoServiceImpl implements AliyunVideoService {


    @Override
    public String upload(UploadVideoRequest uploadRequest) {
        UploadVideoImpl uploader = new UploadVideoImpl();
        UploadVideoResponse response = uploader.uploadVideo(uploadRequest);
        if ("0" != response.getCode()) {
            throw new BaseErrorException("上传视频至阿里云失败,失败编码:{}", response.getCode());
        }
        return response.getVideoId();
    }

    @Override
    public PlayInfoDTO getPlayInfo(PublicRequest publicRequest) {
        return getInfo(publicRequest, 0);
    }

    private PlayInfoDTO getInfo(PublicRequest publicRequest, int count) {
        String url = "http://vod.cn-shanghai.aliyuncs.com?Action=GetPlayInfo&VideoId=" + publicRequest.getVideoId() + "&Format=JSON";
        url = url + AliyunUtils.buildPublicParam(publicRequest.getAccessKeyId());
        HttpResponse response1 = HttpClientUtils.post(Signature.newBuilder()
                .method("POST")
                .url(url)
                .secret(publicRequest.getAccessKeySecret())
                .build()
                .compose(), null);
        try {
            String xmlStr = EntityUtils.toString(response1.getEntity(), "UTF-8");
            PlayInfoDTO playInfoDTO = JSON.parseObject(xmlStr, PlayInfoDTO.class);
            if (StringUtils.isNotEmpty(playInfoDTO.getMessage())) {
                if (count > 10) {
                    throw new BaseErrorException("阿里云视频上传异常:" + playInfoDTO.getMessage());
                }
                count++;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new BaseErrorException(e);
                }
                getInfo(publicRequest, count);
            }
            return playInfoDTO;
        } catch (IOException e) {
            throw new BaseErrorException("获取阿里云视频信息IO异常", e);
        }
    }
}
