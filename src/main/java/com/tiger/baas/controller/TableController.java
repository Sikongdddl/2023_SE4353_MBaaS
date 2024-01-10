package com.tiger.baas.controller;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.tiger.baas.Service.SynService;
import com.tiger.baas.Service.TableService;
import com.tiger.baas.repository.MetaDataRepo;
import com.tiger.baas.utils.Result;
import com.tiger.baas.utils.TransactionBuffer;
import com.tiger.baas.utils.UtilFunc;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.springframework.expression.spel.ast.NullLiteral;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

import java.sql.SQLSyntaxErrorException;
import java.util.*;

@RestController
public class TableController {

    public static TransactionBuffer transactionBuffer = new TransactionBuffer();

    private final boolean TransactionDEBUG;

    @Resource
    private TableService tableService;

    @Resource
    private MetaDataRepo metaDataRepo;

    @Resource
    public SynService synService;

    public TableController(){
        this.TransactionDEBUG = true;
    }
    @PostMapping("/synMetadata")
    public Result<Map<String, Map<String, String>>> synMetadata(@RequestBody JSONObject jsonObject) throws IllegalAccessException {
        List<String> fieldList = new ArrayList<>();
        Map<String, Map<String, String>> metaData = new HashMap<>();
        metaData = tableService.gainMeta(jsonObject.getString("databaseId"));
        String uuid = UUID.randomUUID().toString();
        return Result.successMeta("1","请查收您现在的Metadata",metaData, uuid);
    }

    @PostMapping("/setFields")
    public Result setFields(@RequestBody JSONObject jsonObject){
        System.out.println(jsonObject.toString());
        System.out.println("go into service");
        String databaseId = jsonObject.getString("databaseId");
        String tableId = jsonObject.getString("tableId");
        //List<String> newFields = utilFunc.jsonArrayToList(jsonObject.getJSONArray("newFields"));
        Map<String, String> newFieldsMap = jsonObject.getJSONObject("newFields");

        System.out.println(databaseId + tableId + newFieldsMap.toString());
        String errno = tableService.setFields(databaseId,tableId,newFieldsMap);
        if(!Objects.equals(errno, "-1")){
            List<String> sessoinList = this.synService.registerBuffer.getSessionList(tableId);
            for(int i = 0; i < sessoinList.size(); ++i){
                synService.sendMessageField(synService.generatePayloadField(sessoinList.get(i), SynService.changeType.newTable,tableId,"","","",""));
            }
            if(this.TransactionDEBUG){
                this.transactionBuffer.setTableDanger(tableId);
            }
            return Result.success(errno,"覆盖设置字段成功");
        }
        else{
            return Result.error(errno,"sql失败");
        }
    }

    @PostMapping("/addField")
    public Result addFields(@RequestBody JSONObject jsonObject){
        String databaseId = jsonObject.getString("databaseId");
        String tableId = jsonObject.getString("tableId");
        String newField = jsonObject.getString("newField");
        String fieldType = jsonObject.getString("fieldType");
        String errno = tableService.addFields(databaseId, tableId, newField,fieldType);
        if(!Objects.equals(errno,"-1")){
            List<String> sessoinList = synService.registerBuffer.getSessionList(tableId);
            for(int i = 0; i < sessoinList.size(); ++i){
                synService.sendMessageField(synService.generatePayloadField(databaseId, SynService.changeType.newField,tableId,newField,fieldType,"",""));
            }

            if(this.TransactionDEBUG){
                this.transactionBuffer.setTableDanger(tableId);
            }
            return Result.success(errno,"添加表字段成功");
        }else{
            return Result.error(errno,"sql失败");
        }
    }

    @PostMapping("/deleteField")
    public Result deleteFields(@RequestBody JSONObject jsonObject){
        String databaseId = jsonObject.getString("databaseId");
        String tableId = jsonObject.getString("tableId");
        String deleteField = jsonObject.getString("deleteField");
        String errno = tableService.deleteFields(databaseId, tableId, deleteField);
        if(!Objects.equals(errno,"-1")) {
            List<String> sessoinList = synService.registerBuffer.getSessionList(tableId);
            for(int i = 0; i < sessoinList.size(); ++i){
                synService.sendMessageField(synService.generatePayloadField(databaseId, SynService.changeType.deleteField,tableId,deleteField,"","",""));
            }

            if(this.TransactionDEBUG){
                this.transactionBuffer.setTableDanger(tableId);
            }
            return Result.success(errno,"删除表字段成功");
        }else{
            return Result.error(errno,"sql失败");
        }

    }

    @PostMapping("/addRecord")
    public Result addRecord(@RequestBody JSONObject jsonObject) throws JsonProcessingException {
        String databaseId = jsonObject.getString("databaseId");
        String tableId = jsonObject.getString("tableId");
        Map<String, Object> payload = jsonObject.getJSONObject("payload");
        System.out.println("payload:"+payload);
        String errno = tableService.addRecord(databaseId, tableId,payload);
        if(!Objects.equals(errno,"-1")) {
            List<String> sessoinList = synService.registerBuffer.getSessionList(tableId);
            String primaryKeyName = metaDataRepo.findDistinctFirstByTablebelong(tableId + "_Concat_" + databaseId).getPrimarykey();
            for(int i = 0; i < sessoinList.size(); ++i){
                synService.sendMessageContent(
                        synService.generatePayloadContentHead(databaseId,tableId),synService.generatePayloadContentChangeType(SynService.changeType.add),synService.generatePayloadContentBody(payload,null,primaryKeyName,"unkownAddValue").get(0));
            }
            if(this.TransactionDEBUG){
                this.transactionBuffer.setTableDanger(tableId);
            }
            return Result.success(errno,"添加表字段成功");
        }else{
            return Result.error(errno,"sql失败");
        }
    }

    //set null at default fields
    //insert null value brings strange bug
    //so String "nullValue" infers to null value
    //TAKE CARE!!!
    @PostMapping("/setRecord")
    public Result setRecord(@RequestBody JSONObject jsonObject) throws IllegalAccessException, JsonProcessingException {
        String databaseId = jsonObject.getString("databaseId");
        String tableId = jsonObject.getString("tableId");
        Object rowId = jsonObject.get("rowId");
        Map<String, Object> payload = jsonObject.getJSONObject("payload");
        Map<String, Object> payloadWithPrimaryKey = new HashMap<>();
        payloadWithPrimaryKey.putAll(payload);
        payloadWithPrimaryKey.put("rowId",rowId);
        String errno = tableService.setRecord(databaseId, tableId, rowId, payload);
        if(!Objects.equals(errno,"-1")) {
            List<String> sessoinList = synService.registerBuffer.getSessionList(tableId);
            String primaryKeyName = metaDataRepo.findDistinctFirstByTablebelong(tableId + "_Concat_" + databaseId).getPrimarykey();
            for(int i = 0; i < sessoinList.size(); ++i){
                synService.sendMessageContent(
                        synService.generatePayloadContentHead(databaseId,tableId),synService.generatePayloadContentChangeType(SynService.changeType.modify),synService.generatePayloadContentBody(payloadWithPrimaryKey,null,primaryKeyName,rowId).get(0));
            }

            if(this.TransactionDEBUG){
                this.transactionBuffer.setTableDanger(tableId);
            }
            return Result.success(errno,"成功设置一条记录");
        }else{
            return Result.error(errno,"sql失败");
        }

    }

    //don't modify default fields
    @PostMapping("/updateRecord")
    public Result updateRecord(@RequestBody JSONObject jsonObject) throws JsonProcessingException {
        String databaseId = jsonObject.getString("databaseId");
        String tableId = jsonObject.getString("tableId");
        Object rowId = jsonObject.get("rowId");
        Map<String, Object> payload = jsonObject.getJSONObject("payload");
        Map<String, Object> payloadWithPrimaryKey = new HashMap<>();
        payloadWithPrimaryKey.putAll(payload);
        payloadWithPrimaryKey.put("rowId",rowId);

        String errno = tableService.updateRecord(databaseId, tableId, rowId, payload);
        if(!Objects.equals(errno,"-1")) {
            System.out.println("return value of updateRecordService: " + errno);

            List<String> sessionList = new ArrayList<>();

            if(this.synService.registerBuffer == null){
                System.out.println("hell no ! empty register buffer!");
            }

            sessionList = this.synService.registerBuffer.getSessionList(tableId);

            if(sessionList == null){
                System.out.println("hell no ! empty sessionList!");
            }
            if(sessionList != null){
                String primaryKeyName = metaDataRepo.findDistinctFirstByTablebelong(tableId + "_Concat_" + databaseId).getPrimarykey();
                System.out.println("当前订阅列表长度： " + sessionList.size());
                for(int i = 0; i < sessionList.size(); ++i){
                    System.out.println("正在处理第" + i + "项，");
                    System.out.println(sessionList.get(i));
                    synService.sendMessageContent(
                            synService.generatePayloadContentHead(databaseId,tableId),synService.generatePayloadContentChangeType(SynService.changeType.modify),synService.generatePayloadContentBody(payloadWithPrimaryKey,null,primaryKeyName,rowId).get(0));
                }
            }

            if(this.TransactionDEBUG){
                this.transactionBuffer.setTableDanger(tableId);
            }
            return Result.success(errno,"成功更新一条记录");
        }else{
            return Result.error(errno,"sql失败");
        }

    }

    @PostMapping("/deleteRecord")
    public Result deleteRecord(@RequestBody JSONObject jsonObject) throws JsonProcessingException {
        String databaseId = jsonObject.getString("databaseId");
        String tableId = jsonObject.getString("tableId");
        Object rowId = jsonObject.get("rowId");

        String errno = tableService.deleteRecord(databaseId, tableId, rowId);
        if(!Objects.equals(errno,"-1")) {
            List<String> sessoinList = synService.registerBuffer.getSessionList(tableId);
            String primaryKeyName = metaDataRepo.findDistinctFirstByTablebelong(tableId + "_Concat_" + databaseId).getPrimarykey();
            for(int i = 0; i < sessoinList.size(); ++i){
                synService.sendMessageContent(
                        synService.generatePayloadContentHead(databaseId,tableId),synService.generatePayloadContentChangeType(SynService.changeType.delete),synService.generatePayloadContentBody(null,null,primaryKeyName,rowId).get(0));
            }

            if(this.TransactionDEBUG){
                this.transactionBuffer.setTableDanger(tableId);
            }
            return Result.success(errno,"成功删除一条记录");
        }else{
            return Result.error(errno, "sql失败");
        }

    }

    @PostMapping("subscribe")
    public Result subscribe(@RequestBody JSONObject jsonObject){
        String databaseId = jsonObject.getString("databaseId");
        String tableId = jsonObject.getString("tableId");
        String uuid = jsonObject.getString("uuid");
        //String deviceId = jsonObject.getString("deviceId");
        //System.out.println("in tableController subscribe : before used buffer: " + this.synService.registerBuffer);
        String errno = this.synService.subscribe(databaseId+"_"+uuid,tableId);
        //System.out.println("in table Controller subscribe: " + this.synService);
        //System.out.println("in tableController subscribe : buffer: " + this.synService.registerBuffer);
        return Result.success(errno,"成功订阅");
    }

    @PostMapping("unSubscribe")
    public Result unSubscribe(@RequestBody JSONObject jsonObject){
        String databaseId = jsonObject.getString("databaseId");
        String tableId = jsonObject.getString("tableId");
        String uuid = jsonObject.getString("uuid");
        //String deviceId = jsonObject.getString("deviceId");
        String errno = synService.unSubscribe(databaseId+"_"+uuid,tableId);
        return Result.success(errno,"成功取消订阅");
    }


    @PostMapping("query")
    public Result<List<Map<String, Object>>> query(@RequestBody JSONObject jsonObject){
        String databaseId = jsonObject.getString("databaseId");
        String tableId = jsonObject.getString("tableId");
        List<Map<String, String>> whereConditions  = new ArrayList<>();

        JSONArray jsonArray = jsonObject.getJSONArray("whereConditions");
        Map<String, String> join = jsonObject.getJSONObject("join");

        for(int i = 0; i < jsonArray.size(); i++){
            JSONObject tmpjsonObject = jsonArray.getJSONObject(i);
            Map<String, String> dataMap = new HashMap<>();

            for (Object key : tmpjsonObject.keySet()) {
                dataMap.put(key.toString(), tmpjsonObject.getString(key.toString()));
            }

            whereConditions.add(dataMap);
        }

        System.out.println(whereConditions);
        //naive query
        if(join.isEmpty()){
            List<Map<String, Object>> QueryResult = tableService.query(databaseId,tableId,whereConditions);
            String errno = "0";
            return Result.successRecord(errno,"Query完毕",QueryResult);
        }
        //join and query
        else{
            String tableId_1 = join.get("tableId_1");
            String tableId_2 = join.get("tableId_2");
            String fieldId_1 = join.get("fieldId_1");
            String fieldId_2 = join.get("fieldId_2");

            List<Map<String, Object>> joinResult = tableService.joinAndQuery(databaseId,tableId_1,tableId_2,fieldId_1, fieldId_2,whereConditions);

            String errno="0";
            return Result.successRecord(errno,"join+Query完毕",joinResult);
        }
    }


    /*
    * Todo:
    *  refact errno return value
    * */
}
