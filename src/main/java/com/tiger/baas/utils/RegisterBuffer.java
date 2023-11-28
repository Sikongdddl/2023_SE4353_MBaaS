package com.tiger.baas.utils;

import java.util.HashMap;
import java.util.Map;

public class RegisterBuffer {
    Map<String, String> device2db = new HashMap<>();

    Map<String, String> device2tb = new HashMap<>();

    public void add(String databaseId, String tableId, String deviceId){
        device2db.put(deviceId,databaseId);
        device2tb.put(deviceId,tableId);
    }

    public void delete(String databaseId, String tableId, String deviceId){
        device2db.remove(deviceId,databaseId);
        device2db.remove(deviceId, tableId);
    }
}
