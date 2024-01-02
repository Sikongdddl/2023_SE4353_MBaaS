package com.tiger.baas.utils;

import com.tiger.baas.entity.MetaData;

import java.util.*;

public class RegisterBuffer {
    Map<String, String> device2db = new HashMap<>();

    Map<String, String> device2tb = new HashMap<>();

    List<String> deviceList = new ArrayList<>();

    public void add(String databaseId, String tableId, String deviceId){
        device2db.put(deviceId,databaseId);
        device2tb.put(deviceId,tableId);
        if(!deviceList.contains(deviceId)){
            deviceList.add(deviceId);
        }
    }

    public void delete(String databaseId, String tableId, String deviceId){
        device2db.remove(deviceId,databaseId);
        device2db.remove(deviceId, tableId);
    }

    public List<String> getUserList(String databaseId, String tableId){
        List<String> res = new ArrayList<>();
        for(int i = 0; i < deviceList.size(); ++i){
            boolean tbHitFlag = false;
            boolean dbHitFlag = false;
            for(Map.Entry<String, String> entry : device2db.entrySet()){
                if (Objects.equals(entry.getKey(), deviceList.get(i)) && Objects.equals(entry.getValue(),databaseId)) {
                    dbHitFlag = true;
                    break;
                }
            }
            for(Map.Entry<String, String> entry : device2tb.entrySet()){
                if (Objects.equals(entry.getKey(), deviceList.get(i)) && Objects.equals(entry.getValue(),tableId)) {
                    tbHitFlag = true;
                    break;
                }
            }
            if(tbHitFlag && dbHitFlag){
                res.add(deviceList.get(i));
            }
        }
        return res;
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
