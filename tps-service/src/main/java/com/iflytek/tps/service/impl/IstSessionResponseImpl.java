package com.iflytek.tps.service.impl;


import com.iflytek.tps.beans.transfer.IstSessionResponse;
import com.iflytek.tps.beans.transfer.IstSessionResult;
import com.iflytek.tps.foun.dto.HttpClientResult;
import com.iflytek.tps.foun.util.HttpClientUtils;
import com.iflytek.tps.service.client.IstClient;
import com.iflytek.tps.service.request.CallBackRequest;
import com.iflytek.tps.service.util.CommUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;


public class IstSessionResponseImpl implements IstSessionResponse {

    private static Logger logger = LoggerFactory.getLogger(IstSessionResponseImpl.class);

    private String sid; //音频id
    private String callBackUrl;//回调地址
    private static final String LST_SPEECH_IFLY = "LST_SPEECH_IFLY";
    private IstClient client;
    private String callBackErrorUrl;


    public IstSessionResponseImpl(String sid,String callBackUrl,IstClient client,String callBackErrorUrl){
        this.sid = sid;
        this.callBackUrl = callBackUrl;
        this.client = client;
        this.callBackErrorUrl = callBackErrorUrl;

    }


    @Override
    public void onCallback(IstSessionResult istSessionResult) {
        logger.info("当前解析 sid: {}的音频信息",sid);
        logger.info("code:{}", istSessionResult.getErrCode());
        logger.info("str:{}", istSessionResult.getAnsStr());
        logger.info("flag:{}", istSessionResult.isEndFlag());
        String sentence = istSessionResult.getAnsStr();
        if(istSessionResult.getErrCode() != 0){
            try {
                doPost(sid,"","",callBackErrorUrl);
                CommUtils.getSids().remove(sid);
                client.close();
                return;
            }catch (Exception e){
             logger.error("关闭引擎异常 {}",e.getMessage());
            }

        }
        String end = (istSessionResult.isEndFlag())?"1":"0";
        if(StringUtils.isNotBlank(sentence)){
            logger.info("开始进行第一次回调接口发送,发送内容为 sid{},restult {}",sid,sentence);
            HttpClientResult result = doPost(sid,sentence,end,callBackUrl);//第一次发送
            logger.info("第一次调用结果："+result.toString());
            if(result.getCode() != 200){
                logger.info("开始进行第二次回调接口发送,发送内容为 sid{},restult {}",sid,sentence);
                result = doPost(sid,sentence,end,callBackUrl);//第二次发送
                logger.info("第二次调用结果："+result.toString());
                if(result.getCode() != 200){
                    logger.info("开始进行第三次回调接口发送,发送内容为 sid{},restult {}",sid,sentence);
                    result = doPost(sid,sentence,end,callBackUrl);//第三次发送，三次发送后无论结果如何，都不在发送
                    logger.info("第三次调用结果："+result.toString());
                }
            }

        }

    }

    private HttpClientResult doPost(String sid, String result,String sentence,String url){
        CallBackRequest request = new CallBackRequest();
        request.setSentence(sentence);
        request.setSid(sid);
        request.setResult(result);
        request.setCallKey(LST_SPEECH_IFLY);
        HttpClientResult httpClientResult =null;
        try {
            httpClientResult = HttpClientUtils.doPost(url,request);

        }catch (Exception e){
            logger.error("调用回调接口错误，错误message{}",e.getMessage());
        }
        return httpClientResult;
    }

    @Override
    public void onError(Throwable throwable) {
        logger.error("当前sid {}，异常信息：{}",sid,throwable.getMessage());
    }

    @Override
    public void onCompleted() {
        logger.info("当前解析 sid {}的音频信息解析已完成",sid);
    }
}
