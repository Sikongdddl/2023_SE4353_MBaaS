package com.tiger.baas.controller;

//import com.tiger.baas.Service.SynService;
import com.tiger.baas.Service.SynService;
import com.tiger.baas.Service.TableService;
import com.tiger.baas.utils.RegisterBuffer;
import com.tiger.baas.utils.Result;
import com.tiger.baas.utils.UtilFunc;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;


import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.persistence.EntityManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class TableController {

    @Resource
    private TableService tableService;

    private UtilFunc utilFunc = new UtilFunc();

    @Resource
    private SynService synService;

    @PostMapping("/synMetadata")
    public Result<Map<String, Map<String, String>>> synMetadata(@RequestBody JSONObject jsonObject) throws IllegalAccessException {
        List<String> fieldList = new ArrayList<>();
        Map<String, Map<String, String>> metaData = new HashMap<>();
        metaData = tableService.gainMeta(jsonObject.getString("databaseId"));
        return Result.success(metaData,"1","请查收您现在的Metadata");
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
        return Result.success(errno,"覆盖设置字段成功");
    }

    @PostMapping("/addField")
    public Result addFields(@RequestBody JSONObject jsonObject){
        String databaseId = jsonObject.getString("databaseId");
        String tableId = jsonObject.getString("tableId");
        String newField = jsonObject.getString("newField");
        String fieldType = jsonObject.getString("fieldType");
        String errno = tableService.addFields(databaseId, tableId, newField,fieldType);
        return Result.success(errno,"添加表字段成功");
    }

    @PostMapping("/deleteField")
    public Result deleteFields(@RequestBody JSONObject jsonObject){
        String databaseId = jsonObject.getString("databaseId");
        String tableId = jsonObject.getString("tableId");
        String deleteField = jsonObject.getString("deleteField");
        String errno = tableService.deleteFields(databaseId, tableId, deleteField);
        return Result.success(errno,"删除表字段成功");
    }

    @PostMapping("/addRecord")
    public Result addRecord(@RequestBody JSONObject jsonObject){
        String databaseId = jsonObject.getString("databaseId");
        String tableId = jsonObject.getString("tableId");
        Map<String, String> payload = jsonObject.getJSONObject("payload");
        System.out.println("payload:"+payload);
        String errno = tableService.addRecord(databaseId, tableId,payload);
        return Result.success(errno,"添加表字段成功");

    }

    //set null at default fields
    //insert null value brings strange bug
    //so String "nullValue" infers to null value
    //Please Take Care!
    @PostMapping("/setRecord")
//    public Result setRecord(@RequestParam String databaseId, @RequestParam String tableId, @RequestParam String rowId, @RequestParam Map<String, String> payload){
//        String errno = tableService.setRecord(databaseId, tableId, rowId, payload);
//        return Result.success(errno,"成功设置一条记录");
//    }
    public Result setRecord(@RequestBody JSONObject jsonObject) throws IllegalAccessException {
        String databaseId = jsonObject.getString("databaseId");
        String tableId = jsonObject.getString("tableId");
        String rowId = jsonObject.getString("rowId");
        Map<String, String> payload = jsonObject.getJSONObject("payload");
        String errno = tableService.setRecord(databaseId, tableId, rowId, payload);
        return Result.success(errno,"成功设置一条记录");
    }

    //don't modify default fields
    @PostMapping("/updateRecord")
    public Result updateRecord(@RequestBody JSONObject jsonObject){
        String databaseId = jsonObject.getString("databaseId");
        String tableId = jsonObject.getString("tableId");
        String rowId = jsonObject.getString("rowId");
        Map<String, String> payload = jsonObject.getJSONObject("payload");
        String errno = tableService.updateRecord(databaseId, tableId, rowId, payload);
        return Result.success(errno,"成功更新一条记录");
    }

    @PostMapping("/deleteRecord")
    public Result deleteRecord(@RequestBody JSONObject jsonObject){
        String databaseId = jsonObject.getString("databaseId");
        String tableId = jsonObject.getString("tableId");
        String rowId = jsonObject.getString("rowId");
        String errno = tableService.deleteRecord(databaseId, tableId, rowId);
        return Result.success(errno,"成功删除一条记录");
    }

    @PostMapping("subscribe")
    public Result subscribe(@RequestBody JSONObject jsonObject){
        String databaseId = jsonObject.getString("databaseId");
        String tableId = jsonObject.getString("tableId");
        String deviceId = jsonObject.getString("deviceId");
        String errno = synService.subscribe(databaseId,tableId,deviceId);
        return Result.success(errno,"成功订阅");
    }

    @PostMapping("unSubscribe")
    public Result unSubscribe(@RequestBody JSONObject jsonObject){
        String databaseId = jsonObject.getString("databaseId");
        String tableId = jsonObject.getString("tableId");
        String deviceId = jsonObject.getString("deviceId");
        String errno = synService.unSubscribe(databaseId,tableId,deviceId);
        return Result.success(errno,"成功取消订阅");
    }
    //support a single where condition every query
    //should we support multi-conditions query?
    @PostMapping("query")
    public Result<List<Map<String, String>>> query(@RequestBody JSONObject jsonObject){
        String databaseId = jsonObject.getString("databaseId");
        String tableId = jsonObject.getString("tableId");
        List<String> whereCondition = utilFunc.jsonArrayToList(jsonObject.getJSONArray("whereConditions"));
        List<Map<String, String>> QueryResult = tableService.query(databaseId,tableId,whereCondition);
        String errno = "0";
        return Result.success(QueryResult, errno, "Query完毕");
    }

    @PostMapping("join")
    public Result<List<Map<String, String>>> join(@RequestBody JSONObject jsonObject){
        String databaseId = jsonObject.getString("databaseId");
        String tableId_1 = jsonObject.getString("tableId_1");
        String tableId_2 = jsonObject.getString("tableId_2");
        String fieldId_1 = jsonObject.getString("fieldId_1");
        String fieldId_2 = jsonObject.getString("fieldId_2");

        List<Map<String, String>> joinResult = tableService.join(databaseId,tableId_1,tableId_2,fieldId_1, fieldId_2);
        String errno = "0";
        return Result.success(joinResult, errno, "join完毕");
    }
    //return full query list and parse by frontend
    //reduce pressure of ipads server XD
//    @PostMapping("aggregation")
//    public Result<List<Map<String, String>>> aggregation(@RequestBody JSONObject jsonObject){
//    }

    /*
    * Todo:
    *  test query and join
    *  implement multi-conditions query
    *  websocket
    *  deployment
    *  refact errno return value
    *  deal with different database with the same table name
    * */
}
