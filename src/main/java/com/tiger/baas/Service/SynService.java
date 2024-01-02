package com.tiger.baas.Service;

import com.tiger.baas.utils.RegisterBuffer;
import net.sf.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

@Service
public class SynService {

    @Resource
    private WebSocketService webSocketService;

    @Resource
    private TableService tableService;

    private RegisterBuffer registerBuffer = new RegisterBuffer();

    public enum changeType{
        newTable,deleteTable,newField,deleteField,renameTable,renameField,add,modify,delete;
    }

    public String subscribe(String databaseId, String tableId, String deviceId){
        registerBuffer.add(databaseId, tableId, deviceId);
        registerBuffer.Traverse();
        return "0";
    }

    public String unSubscribe(String databaseId, String tableId, String deviceId){
        registerBuffer.delete(databaseId,tableId,deviceId);
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

    public Map<String, String> generatePayloadContentHead(String databaseId, String tableId){
        Map<String, String> res = new HashMap<>();
        res.put("messageType","SynContent");
        res.put("databaseId", databaseId);
        res.put("tableId", tableId);
        return res;
    }

    public List<Map<String,Object>> generatePayloadContentBody(changeType type,Map<String, String> newItem, Map<String, String> oldItem){
        List<Map<String,Object>> res = new ArrayList<>();
        Map<String, Object> changeTypeEntry = new HashMap<>();
        changeTypeEntry.put("changeType", type);

        Map<String, Object> newItemEntry = new HashMap<>();
        newItemEntry.put("newItem", newItem);

        Map<String, Object> oldItemEntry = new HashMap<>();
        newItemEntry.put("oldItem", oldItem);

        res.add(changeTypeEntry);
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

        List<String> userList = registerBuffer.getUserList(databaseId,tableId);

        JSONObject jsonMessage = JSONObject.fromObject(payload);
        for(String userId : userList){
            WebSocketService.sendMessage(userId,jsonMessage.toString());
        }
    }

    public void sendMessageContent(Map<String, String> payloadHead, Map<String, Object> payloadBody){
        String messageType = payloadHead.get("messageType");
        String databaseId = payloadHead.get("databaseId");
        String tableId = payloadHead.get("tableId");

        List<String> userList = registerBuffer.getUserList(databaseId,tableId);

        JSONObject jsonMessageHead = JSONObject.fromObject(payloadHead);
        JSONObject jsonMessageBody = JSONObject.fromObject(payloadBody);
        for(String userId : userList){
            WebSocketService.sendMessage(userId,jsonMessageHead.toString() + jsonMessageBody.toString());
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
