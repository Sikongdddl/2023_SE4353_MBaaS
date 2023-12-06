package com.tiger.baas.utils;

import com.tiger.baas.entity.MetaData;

import java.util.HashMap;
import java.util.List;
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

    public void Traverse(){
        System.out.println("Starting Traverse device to database buffer");
        for(Map.Entry<String, String> entry : device2db.entrySet()){
            System.out.println("Key: " + entry.getKey());
            System.out.println("Value: " + entry.getValue());
        }

        System.out.println("Starting Traverse device to table buffer");
        for(Map.Entry<String, String> entry : device2tb.entrySet()){
            System.out.println("Key: " + entry.getKey());
            System.out.println("Value: " + entry.getValue());
        }

    }
}
