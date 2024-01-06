package com.tiger.baas.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.tiger.baas.utils.RegisterBuffer;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.sound.midi.SysexMessage;
import java.util.*;

@Service
public class SynService {

    @Resource
    private WebSocketService webSocketService;

    @Resource
    private TableService tableService;

    public RegisterBuffer registerBuffer;

    public SynService(){
        this.registerBuffer = new RegisterBuffer();
    }
    public enum changeType{
        newTable,deleteTable,newField,deleteField,renameTable,renameField,add,modify,delete;
    }

    public String subscribe(String SessionId, String tableId){
        //System.out.println("in syn service : " + this.registerBuffer);
        registerBuffer.add(SessionId, tableId);
        registerBuffer.Traverse();
        return "0";
    }

    public String unSubscribe(String sessionId, String tableId){
        registerBuffer.delete(sessionId,tableId);
        registerBuffer.Traverse();
        return "0";
    }

    public Map<String, String> generatePayloadField(String databaseId, changeType type, String tableId, String fieldId, String fieldType, String newItem, String oldItem){
        Map<String, String> res = new HashMap<>();
        res.put("messageType","SynField");
        res.put("databaseId", databaseId);
        res.put("changeType","" + type);
        res.put("tableId", tableId);
        res.put("fieldId", fieldId);
        res.put("fieldType", fieldType);
        res.put("newItem", newItem);
        res.put("oldItem", oldItem);
        return res;
    }

    public Map<String, String> generatePayloadContentHead(String databaseId, String tableId) {
        Map<String, String> res = new HashMap<>();

        res.put("messageType","SynContent");
        res.put("databaseId", databaseId);
        res.put("tableId", tableId);

        System.out.println();
        return res;
    }

    public Map<String, Object> generatePayloadContentChangeType(changeType type){
        Map<String, Object> changeTypeEntry = new HashMap<>();
        changeTypeEntry.put("changeType",(Object)(""+type));
        return changeTypeEntry;
    }
    public List<Map<String,Object>> generatePayloadContentBody(Map<String, String> newItem, Map<String, String> oldItem){
        List<Map<String,Object>> res = new ArrayList<>();

        Map<String, Object> newItemEntry = new HashMap<>();
        newItemEntry.put("newItem", newItem);

        Map<String, Object> oldItemEntry = new HashMap<>();
        newItemEntry.put("oldItem", oldItem);

        res.add(newItemEntry);
        res.add(oldItemEntry);

        return res;
    }
    public void sendMessageField(Map<String, String> payload){
        String messageType = payload.get("messageType");
        String databaseId = payload.get("databaseId");
        String tableId = payload.get("tableId");

        String fieldId = payload.get("fieldId");
        String fieldType = payload.get("fieldType");
        String newItem = payload.get("newItem");
        String oldItem = payload.get("oldItem");

        //databaseId + synMetaUUID
        List<String> userList = registerBuffer.getSessionList(tableId);

        JSONObject jsonMessage = JSONObject.fromObject(payload);
        for(String userId : userList){
            WebSocketService.sendMessage(userId,jsonMessage.toString());
            System.out.println("send message field");
        }
    }

    public void sendMessageContent(Map<String, String> payloadHead, Map<String, Object> payloadChangeType,Map<String, Object> payloadBody) throws JsonProcessingException {
        System.out.println("start sendMessage");
        String messageType = payloadHead.get("messageType");
        String databaseId = payloadHead.get("databaseId");
        String tableId = payloadHead.get("tableId");
        String changeType = payloadHead.get("changeType");

        List<String> userList = registerBuffer.getSessionList(tableId);

        ObjectMapper objectmapper = new ObjectMapper();
        String jsonMessageHead = objectmapper.writeValueAsString(payloadHead);
//
//        String jsonMessageChangeType = objectmapper.writeValueAsString(payloadChangeType);
//
//        String jsonMessageBody = objectmapper.writeValueAsString(payloadBody);

        List<Map<String, Object>> res = new ArrayList<>();

        Map<String, Object> tempMap = new HashMap<>();
        tempMap.put("changeType",payloadChangeType.get("changeType"));
        tempMap.put("newItem",payloadBody.get("newItem"));
        tempMap.put("oldItem",payloadBody.get("oldItem"));
        res.add(tempMap);

        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("updateData",res);

        String jsonMessageBody = objectmapper.writeValueAsString(jsonMap);

//        System.out.println(jsonMessageHead);
//        System.out.println(jsonMessageChangeType);
//        System.out.println(jsonMessageBody);

        String jsonMessage = jsonMessageHead.substring(0,jsonMessageHead.length()-1) + "," + jsonMessageBody.substring(1,jsonMessageBody.length());

        for(String userId : userList){
            WebSocketService.sendMessage(userId,jsonMessage);
            System.out.println("send message content");
        }
    }

    public String createTransaction(String databaseId){
        String transactionId = UUID.randomUUID().toString();
        return databaseId + "_Concat_" + transactionId;
    }

    //when transactionCommit was called:
    //it means everything will be fine, just do it!
    @Transactional
    public void transactionCommit(String databaseId, String transactionId, int transactionVersion, List<Map<String, Object>> operations){
        //switch case for every write operation
        for (Map<String, Object> operation : operations) {
            String type = operation.get("type").toString();
            String tableId = operation.get("tableId").toString();
            String rowId = operation.get("rowId").toString();
            Map<String, String> payload = new HashMap<>();
            payload = (Map<String, String>) operation.get("data");
            switch (type) {
                case "add": {
                    tableService.addRecord(databaseId, tableId, payload);
                }
                break;
                case "update": {
                    tableService.updateRecord(databaseId, tableId, rowId, payload);
                }
                break;
                case "delete": {
                    tableService.deleteRecord(databaseId, tableId, rowId);
                }
                break;
            }
        }
    }

}
