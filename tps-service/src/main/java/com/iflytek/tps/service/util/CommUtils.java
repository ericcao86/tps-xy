package com.iflytek.tps.service.util;

import java.util.concurrent.ConcurrentHashMap;

public class CommUtils {

    private static ConcurrentHashMap<String,Object> sids = new ConcurrentHashMap();

    public static ConcurrentHashMap<String, Object> getSids() {
        return sids;
    }

    public static void setSids(ConcurrentHashMap<String, Object> sids) {
        CommUtils.sids = sids;
    }
}
