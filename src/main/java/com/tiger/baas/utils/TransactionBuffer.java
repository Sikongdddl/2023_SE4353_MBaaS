package com.tiger.baas.utils;

import org.springframework.transaction.annotation.Transactional;

import java.util.*;

public class TransactionBuffer {
    public Map<String, List<String>> transaction2Tables = new HashMap<>();

    public Map<String, String> transactionStatus = new HashMap<>();

    public Map<String, Integer> transactionVersion = new HashMap<>();


    public TransactionBuffer(){

    }

    public int addTransaction(String transactionId){
        List<String> tableList = new ArrayList<>();
        transaction2Tables.put(transactionId,tableList);
        setStatus(transactionId,"normal");
        transactionVersion.put(transactionId,0);
        transactionStatus.put(transactionId,"normal");
        System.out.println("new transaction added!" + transactionId);
        return 0;
    }

    //status normal: nothing wrong
    //status fail: transaction failed
    public String getStatus(String transactionId){
        for(Map.Entry<String, String> entry : transactionStatus.entrySet()){
            if(Objects.equals(entry.getKey(), transactionId)){
                System.out.println("transaction" + transactionId + "status: " + entry.getValue());
                return entry.getValue();
            }
        }
        return "NoSuchTransactionId";
    }

    public Integer getVersion(String transactionId){
        for(Map.Entry<String, Integer> entry : transactionVersion.entrySet()){
            if(Objects.equals(entry.getKey(), transactionId)){
                System.out.println("transaction" + transactionId + " version: " + entry.getValue());
                return entry.getValue();
            }
        }
        return -1;
    }

    public void setVersionWhenFail(String transactionId){
        for(Map.Entry<String, Integer> entry : transactionVersion.entrySet()){
            if(Objects.equals(entry.getKey(), transactionId)){
                Integer currentVersion = entry.getValue();
                entry.setValue(currentVersion + 1);
                System.out.println("transaction" + transactionId + " version change to : " + entry.getValue());
                System.out.println("transaction" + transactionId + "recover normal status");
                setStatus(transactionId, "normal");
            }
        }
    }
    public void setStatus(String transactionId, String status){
        for(Map.Entry<String, String> entry : transactionStatus.entrySet()){
            System.out.println("target transaction: " + transactionId);
            System.out.println("In set Status Current deal with: " + entry.getKey());
            if(Objects.equals(entry.getKey(), transactionId)){
                System.out.println("transaction" + transactionId + "change status to : " + status);
                String formerStatus = entry.getValue();
                entry.setValue(status);
                if(Objects.equals(status, "fail") && !Objects.equals(formerStatus, "fail")){
                    setVersionWhenFail(transactionId);
                }
            }
        }
    }
    //used when a table was read by transaction
    //maintain a correct map from transactionId to List<tableId>
    public void addTransactionTable(String transactionId, String tableId){
        for(Map.Entry<String, List<String>> entry : transaction2Tables.entrySet()){
            if(Objects.equals(entry.getKey(), transactionId)){
                List<String> curTableList = entry.getValue();
                curTableList.add(tableId);
                entry.setValue(curTableList);
                System.out.println("add table: "+ tableId + "to transaction: " + transactionId);
            }
        }
    }

    public void setTableDanger(String tableId){
        for(Map.Entry<String, List<String>> entry : transaction2Tables.entrySet()){
            List<String> curTableList = entry.getValue();
            String curTransactionId = entry.getKey();
            for(String curTableId : curTableList){
                if(Objects.equals(curTableId, tableId)){
                    System.out.println("set table Danger: " + tableId);
                    System.out.println("curTransactionId: " + curTransactionId);
                    this.setStatus(curTransactionId,"fail");
                }
            }
        }
    }
}
