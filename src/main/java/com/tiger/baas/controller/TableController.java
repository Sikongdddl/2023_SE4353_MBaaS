package com.tiger.baas.controller;

//import com.tiger.baas.Service.SynService;
import com.tiger.baas.Service.TableService;
import com.tiger.baas.utils.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class TableController {
    @Resource
    private TableService tableService;
//    @Resource
//    private SynService synService;

    @PostMapping("/synMetadata")
    public Result<Map<String, List<String>>> synMetadata(@RequestParam String database_id) throws IllegalAccessException {
        List<String> fieldList = new ArrayList<>();
        Map<String, List<String>> metaData = new HashMap<>();
        metaData = tableService.gainMeta(database_id);

        return Result.success(metaData,"1","请查收您现在的Metadata");
    }

    @PostMapping("/setFields")
    public Result setFields(@RequestParam String databaseId, @RequestParam String tableId, @RequestParam List<String> newFields){
        String errno = tableService.setFields(databaseId, tableId, newFields);
        return Result.success(errno,"覆盖设置表字段成功");
    }

    @PostMapping("/addField")
    public Result addFields(@RequestParam String databaseId, @RequestParam String tableId, @RequestParam String newField){
        String errno = tableService.addFields(databaseId, tableId, newField);
        return Result.success(errno,"添加表字段成功");
    }

    @PostMapping("/deleteField")
    public Result deleteFields(@RequestParam String databaseId, @RequestParam String tableId, @RequestParam String deleteField){
        String errno = tableService.deleteFields(databaseId, tableId, deleteField);
        return Result.success(errno,"删除表字段成功");
    }
//
//    @PostMapping("/addRecord")
//    public Result addRecord(@RequestParam String databaseId, @RequestParam String tableId, @RequestParam Map<String, String> payload){
//        String errno = tableService.addRecord(databaseId, tableId, payload);
//        return Result.success(errno,"成功添加一条记录");
//    }
//
//    //set null at default fields
//    @PostMapping("/setRecord")
//    public Result setRecord(@RequestParam String databaseId, @RequestParam String tableId, @RequestParam String rowId, @RequestParam Map<String, String> payload){
//        String errno = tableService.setRecord(databaseId, tableId, rowId, payload);
//        return Result.success(errno,"成功设置一条记录");
//    }
//
//    //don't modify default fields
//    @PostMapping("/updateRecord")
//    public Result updateRecord(@RequestParam String databaseId, @RequestParam String tableId, @RequestParam String rowId, @RequestParam Map<String, String> payload){
//        String errno = tableService.updateRecord(databaseId, tableId, rowId, payload);
//        return Result.success(errno,"成功更新一条记录");
//    }
//
//    @PostMapping("/deleteRecord")
//    public Result deleteRecord(@RequestParam String databaseId, @RequestParam String tableId, @RequestParam String rowId){
//        String errno = tableService.deleteRecord(databaseId, tableId, rowId);
//        return Result.success(errno,"成功删除一条记录");
//    }
//
//    @PostMapping("subscribe")
//    public Result subscribe(@RequestParam String databaseId, @RequestParam String tableId){
//        String errno = synService.subscribe();
//        return Result.success(errno,"成功订阅");
//    }
//
//    @PostMapping("query")
//    public Result<List<Map<String, String>>> query(@RequestBody QueryJson){
//        String errno = tableService.query();
//        return Result.success(QueryResult, errno, "Query完毕");
//    }

    /*
    * Todo:
    *  aggregation
    *  websocket
    * */


}
