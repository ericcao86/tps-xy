package com.iflytek.tps.service;
/**
 * 实时引擎service
 */

import com.iflytek.tps.beans.common.Commons;
import com.iflytek.tps.beans.transfer.IstSessionParam;
import com.iflytek.tps.beans.transfer.IstSessionResponse;
import com.iflytek.tps.service.client.IstClient;
import com.iflytek.tps.service.impl.IstSessionResponseImpl;
import com.iflytek.tps.service.request.RequestDto;
import com.iflytek.tps.service.util.CommUtils;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class IstService {
    private Logger logger = LoggerFactory.getLogger(IstService.class);

    @Value("${ist.url}")
    private String istUrl;

    @Value("${ist.callback.url}")
    private String callBackUrl;

    @Value("${callback.error.url}")
    private String callBackErrorUrl;



    public Map<String,String> doConvert(RequestDto requestDto){
        logger.info("当前sid {},idx {},islast {}",requestDto.getSid(),requestDto.getIdx(),requestDto.getIslast());
        Map<String,String> resMap = new HashMap<>();
        resMap.put(Commons.FLAG,Commons.SUCEESS_FLAG);
        if(null == CommUtils.getSids().get(requestDto.getSid())){
            IstSessionParam sessionParam = new IstSessionParam(requestDto.getSid());
            sessionParam.setRate("16k");
            sessionParam.setDwa("");
            IstClient  client  = new IstClient(istUrl,sessionParam);
            IstSessionResponse istSessionResponse = new IstSessionResponseImpl(requestDto.getSid(),callBackUrl,client,callBackErrorUrl);
            boolean ret = client.connect(istSessionResponse);
            if(!ret){
                logger.error("【连接异常】sid : {}", requestDto.getSid());
                resMap.put(Commons.FLAG,Commons.ERROR_FLAG);
            }
            CommUtils.getSids().put(requestDto.getSid(),client);
            doPost(client,requestDto);
        }else{
            IstClient istClient = (IstClient) CommUtils.getSids().get(requestDto.getSid());
            doPost(istClient,requestDto);
        }

        return resMap;
    }

    private void doPost(IstClient client,RequestDto requestDto){
        try {
            byte [] bytes = Base64.decodeBase64(requestDto.getFrame());
            int z = 1280;//每次发送的字节数
            //总长度
            int bylenth =bytes.length;
            if(bylenth <=z){
                client.post(bytes);
            }else{
                //如果是接收的字节数是1280的倍数，循环发送
                if(bylenth % z == 0){
                    for(int j=0;j<bylenth;j+=z){
                        byte [] s2 = new byte[z];
                        System.arraycopy(bytes,j,s2,0,z);
                        client.post(s2);
                    }
                }else{
                    //如果不是整数倍
                    int n = bylenth/z;//倍数
                    int n1 = bylenth%z;//余数
                    for(int n2=0;n2<n*z;n2+=z){
                        byte [] s3 = new byte[z];
                        System.arraycopy(bytes,n2,s3,0,z);
                        client.post(s3);

                    }
                    int start = bylenth - n*z;
                    byte [] s4 = new byte[n1];
                    System.arraycopy(bytes,start,s4,0,n1);
                    client.post(s4);

                }
            }
            if(requestDto.getIslast()==1){//最后一包
                logger.info("当前最后一包");
                client.end();
                CommUtils.getSids().remove(requestDto.getSid());
            }
            logger.info("sid"+requestDto.getSid() + "：音频数据发送完毕！等待结果返回...");
        }catch (Exception e){
            logger.error(e.getMessage(),e);
        }
    }




}
