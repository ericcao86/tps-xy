package com.iflytek.tps.service.impl;

import com.iflytek.tps.beans.dictation.IatSessionResponse;
import com.iflytek.tps.beans.dictation.IatSessionResult;
import com.iflytek.tps.foun.dto.HttpClientResult;
import com.iflytek.tps.foun.util.HttpClientUtils;
import com.iflytek.tps.service.client.IatClient;
import com.iflytek.tps.service.request.CallBackRequest;
import com.iflytek.tps.service.util.CommUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;


public class IatSessionResponseImpl implements IatSessionResponse {

    private String sid;//音频sid
    private String callBackUrl;//回调地址
    private static final String LST_SPEECH_IFLY = "LST_SPEECH_IFLY";
    private IatClient client;
    private String callBackErrorUrl;
    private List<String> strResult;

    public IatSessionResponseImpl(String sid,String callBackUrl,IatClient client,String callBackErrorUrl){
        this.sid = sid;
        this.callBackUrl =callBackUrl;
        this.client = client;
        this.callBackErrorUrl=callBackErrorUrl;
        strResult = new ArrayList<>();
    }

    private Logger logger = LoggerFactory.getLogger(IatSessionResponseImpl.class);

    @Override
    public void onCallback(IatSessionResult iatSessionResult) {
        logger.info("当前解析 sid: {}的音频信息",sid);
        logger.info("code:{}", iatSessionResult.getErrCode());
        logger.info("str:{}", iatSessionResult.getAnsStr());
        logger.info("flag:{}", iatSessionResult.isEndFlag());
        String sentence = iatSessionResult.getAnsStr();
        logger.info("sentence:{}",sentence);
        try {
            if(iatSessionResult.getErrCode() != 0){
                doPost(sid,"",callBackErrorUrl);
                CommUtils.getSids().remove(sid);
                strResult.clear();
                client.close();
                return;
            }
        }catch (Exception e){
            logger.error("关闭引擎发生异常 {}",e.getMessage());
        }

        strResult.add(sentence);
        if(iatSessionResult.isEndFlag()){//如果已经解析完成
            StringBuffer buffer = new StringBuffer();
            strResult.stream().forEach(e->buffer.append(e));
            String sidres = sid;
            String resultStr = buffer.toString();
            System.out.println("当前解析结果："+resultStr);
//            //TODO 发送回调接口
            logger.info("开始进行第一次回调接口发送,发送内容为 sid{},restult {}",sidres,resultStr);
            HttpClientResult result = doPost(sidres,resultStr,callBackUrl);//第一次发送
            logger.info("第一次调用结果："+result.toString());
            if(result.getCode() != 200 || result == null){//如果不等于200
                logger.info("开始进行第二次回调接口发送,发送内容为 sid{},restult {}",sidres,resultStr);
                result = doPost(sidres,resultStr,callBackUrl);//发送第二次
                logger.info("第二次调用结果："+result.toString());
                if (result.getCode() != 200 || result == null){
                    logger.info("开始进行第三次回调接口发送,发送内容为 sid{},restult {}",sidres,resultStr);
                    result = doPost(sidres,resultStr,callBackUrl);//发送第三次
                    logger.info("第三次调用结果："+result.toString());
                    strResult.clear();
                }
            }
            //发送三次后，无论正确与否，释放内存内容
            strResult.clear();
        }

    }


    private HttpClientResult doPost(String sid,String result,String url){
        logger.info("开始回调接口 sid {},result{},url{}",sid,result,url);
        CallBackRequest request = new CallBackRequest();
        request.setSid(sid);
        request.setResult(result);
        request.setCallKey(LST_SPEECH_IFLY);
        HttpClientResult httpClientResult =null;
        try {
            httpClientResult = HttpClientUtils.doPost(url,request);
            logger.info("回调接口结束，result {},",httpClientResult.toString());
        }catch (Exception e){
          logger.error("调用回调接口错误，错误message{}",e.getMessage());
        }
        return httpClientResult;
    }

    @Override
    public void onError(Throwable throwable) {
       logger.error("当前sid {}音频异常，异常信息：{}",sid,throwable.getMessage());
    }

    @Override
    public void onCompleted() {
      logger.info("当前解析 sid {}的音频信息解析已完成",sid);
    }
}
