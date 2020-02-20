package com.iflytek.tps.service.util;

import java.util.concurrent.ConcurrentHashMap;

public class CommUtils {

    private static ConcurrentHashMap sids = new ConcurrentHashMap();

    public static ConcurrentHashMap getSids() {
        return sids;
    }

    public static void setSids(ConcurrentHashMap sids) {
        CommUtils.sids = sids;
    }
}
