package com.tiger.baas.utils;

import org.springframework.stereotype.Service;

import java.util.*;


public class RegisterBuffer {

    Map<String, String> device2db = new HashMap<>();

    Map<String, String> device2tb = new HashMap<>();

    Map<String, String> session2tb = new HashMap<>();

    List<String> sessionList = new ArrayList<>();

    public void add(String sessionId, String tableId){
        this.session2tb.put(sessionId, tableId);

        if(!this.sessionList.contains(sessionId)){
            System.out.println("New Session: " + sessionId);
            this.sessionList.add(sessionId);
        }
    }

    public void delete(String sessionId, String tableId){
        //device2db.remove(deviceId,databaseId);
        //device2db.remove(deviceId, tableId);
        this.session2tb.remove(sessionId, tableId);
    }

    public List<String> getSessionList(String tableId){
        System.out.println("start get session list");
        List<String> res = new ArrayList<>();

        System.out.println("length of sessionList: " + this.sessionList.size());
        for(int i = 0; i < this.sessionList.size(); ++i){
            boolean tableHitFlag = false;
            boolean sessionHitFlag = false;

            for(Map.Entry<String, String> entry : this.session2tb.entrySet()){
                if(sessionHitFlag && tableHitFlag){
                    break;
                }
                if (Objects.equals(entry.getKey(), this.sessionList.get(i))) {
                    sessionHitFlag = true;
                }
                if(Objects.equals(entry.getValue(), tableId)){
                    tableHitFlag = true;
                }
            }

            if(tableHitFlag && sessionHitFlag){
                res.add(this.sessionList.get(i));
            }
        }
        return res;
    }

    public void Traverse(){
        System.out.println("Starting Traverse session to tableId buffer");
        for(Map.Entry<String, String> entry : this.session2tb.entrySet()){
            System.out.println("Key: " + entry.getKey());
            System.out.println("Value: " + entry.getValue());
        }
        System.out.println("Starting Traverse sessionList");
        for(int i = 0; i < this.sessionList.size(); ++i){
            System.out.println("number "+ i +"item: " + this.sessionList.get(i));
        }
    }
}
