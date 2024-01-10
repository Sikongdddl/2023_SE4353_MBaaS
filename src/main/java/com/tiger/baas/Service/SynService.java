package com.tiger.baas.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.tiger.baas.repository.MetaDataRepo;
import com.tiger.baas.utils.RegisterBuffer;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.sound.midi.SysexMessage;
import java.util.*;

import static com.tiger.baas.controller.TableController.transactionBuffer;

@Service
public class SynService {

    @Resource
    private WebSocketService webSocketService;

    @Resource
    private TableService tableService;

    @Resource
    private MetaDataRepo metaDataRepo;

    public static RegisterBuffer registerBuffer;

    public SynService(){
        registerBuffer = new RegisterBuffer();
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
    public List<Map<String,Object>> generatePayloadContentBody(Map<String, Object> newItem, Map<String, Object> oldItem, String primaryKey, Object primaryValue){
        List<Map<String,Object>> res = new ArrayList<>();

        newItem.put(primaryKey, primaryValue);
        Map<String, Object> newItemEntry = new HashMap<>();
        newItemEntry.put("newItem", newItem);

        Map<String, Object> oldItemEntry = new HashMap<>();
        oldItemEntry.put("oldItem", oldItem);

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
        List<String> userList = this.registerBuffer.getSessionList(tableId);

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

        List<String> userList = this.registerBuffer.getSessionList(tableId);

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
    public String transactionCommit(String databaseId, String transactionId, int transactionVersion, List<Map<String, Object>> operations) throws JsonProcessingException {
        //switch case for every write operation
        for (Map<String, Object> operation : operations) {
            String type = operation.get("type").toString();
            changeType enumType = null;
            String tableId = operation.get("tableId").toString();
            Object rowId = "";
            String errno = "";
            String primaryKeyName = metaDataRepo.findDistinctFirstByTablebelong(tableId + "_Concat_" + databaseId).getPrimarykey();
            if(operation.get("rowId") != null){
                rowId = operation.get("rowId");
            }
            Map<String, Object> payload = new HashMap<>();
            payload = (Map<String, Object>) operation.get("data");

            switch (type) {
                case "add": {
                    errno = tableService.addRecord(databaseId, tableId, payload);
                    enumType = changeType.add;
                }
                break;
                case "update": {
                System.out.println("transaction commit in update service!!!!!!!!!!!!!!!!");
                    errno = tableService.updateRecord(databaseId, tableId, rowId, payload);
                    enumType = changeType.modify;
                }
                break;
                case "delete": {
                    errno = tableService.deleteRecord(databaseId, tableId, rowId);
                    enumType = changeType.delete;
                }
                break;
            }

            if(Objects.equals(errno, "-1")){
                return errno;
            }
            else{
                System.out.println("Transaction commit and send ws message!!!!!!!!!!");
                List<String> sessoinList = this.registerBuffer.getSessionList(tableId);
                for(int i = 0; i < sessoinList.size(); ++i){
                    System.out.println("start traverse sessionList in trasaction commit");
                    this.sendMessageContent(
                            generatePayloadContentHead(databaseId,tableId),generatePayloadContentChangeType(enumType), generatePayloadContentBody(payload, null,primaryKeyName,rowId).get(0)
                    );
                }
                transactionBuffer.setTableDanger(tableId);
            }
        }
        return "0";
    }
}
