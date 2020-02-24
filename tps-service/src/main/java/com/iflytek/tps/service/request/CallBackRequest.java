package com.iflytek.tps.service.request;

import com.iflytek.tps.foun.dto.IRequest;

public class CallBackRequest implements IRequest {

    private String sid;
    private String result;
    private String callKey;
    private String sentence;

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getCallKey() {
        return callKey;
    }

    public void setCallKey(String callKey) {
        this.callKey = callKey;
    }

    public String getSentence() {
        return sentence;
    }

    public void setSentence(String sentence) {
        this.sentence = sentence;
    }

    @Override
    public void verify() {

    }
}
