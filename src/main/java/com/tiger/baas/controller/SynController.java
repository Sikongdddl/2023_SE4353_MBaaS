package com.tiger.baas.controller;

import com.tiger.baas.Service.SynService;
import com.tiger.baas.Service.TableService;
import com.tiger.baas.utils.Result;
import com.tiger.baas.utils.TransactionBuffer;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.*;

@RestController
public class SynController {
    @Resource
    private TableService tableService;

    @Resource
    private SynService synService;

    TransactionBuffer transactionBuffer = new TransactionBuffer();

    @PostMapping("/createTransaction")
    public Result createTransaction(@RequestBody JSONObject jsonObject){
        String databaseId = jsonObject.getString("databaseId");
        String transactionId = synService.createTransaction(databaseId);
        int currentVersion = transactionBuffer.addTransaction(transactionId);
        return Result.successTransactionCreate("0","事务创建成功，已分配事务id",transactionId,currentVersion);

    }

    //read request: query
    //check if transaction is failed
    @PostMapping("/transactionQuery")
    public Result transactionQuery(@RequestBody JSONObject jsonObject){
        String databaseId = jsonObject.getString("databaseId");
        String transactionId = jsonObject.getString("transactionId");
        int transactionVersion = jsonObject.getInt("transactionVersion");
        String tableId = jsonObject.getString("tableId");
        List<Map<String, String>> whereConditions  = new ArrayList<>();
        JSONArray jsonArray = jsonObject.getJSONArray("whereConditions");

        for(int i = 0; i < jsonArray.size(); i++){
            JSONObject tmpjsonObject = jsonArray.getJSONObject(i);
            Map<String, String> dataMap = new HashMap<>();

            for (Object key : tmpjsonObject.keySet()) {
                dataMap.put(key.toString(), tmpjsonObject.getString(key.toString()));
            }

            whereConditions.add(dataMap);
        }
        //gain current version
        Integer currentVersion = transactionBuffer.getVersion(transactionId);

        //check transaction status
        if(Objects.equals(transactionBuffer.getStatus(transactionId), "fail")){
            return Result.error("-100","Your transaction is already fail, sorry!", currentVersion);
        }
        //check transaction version
        else if(transactionVersion < transactionBuffer.getVersion(transactionId)){
            return Result.error(
                    "-100","Old transaction version!",transactionVersion);
        }
        else{
            //maintain transaction2Tables list
            transactionBuffer.addTransactionTable(transactionId,tableId);
            //use normal query
            List<Map<String, Object>> QueryResult = tableService.query(databaseId,tableId,whereConditions);
            String errno = "0";
            return Result.successRecord(errno,"TransactionQuery完毕",QueryResult);
        }
    }

    //write request: addRecord deleteRecord updateRecord
    //if transactionVersion is Correct: atomic deal a batch of write requests
    //if transactionVersion is Old: FAIL Straight!
    @PostMapping("/transactionCommit")
    public Result transactionCommit(@RequestBody JSONObject jsonObject) {
        String databaseId = jsonObject.getString("databaseId");
        String transactionId = jsonObject.getString("transactionId");
        int transactionVersion = jsonObject.getInt("transactionVersion");
        JSONArray jsonArray = jsonObject.getJSONArray("operations");
        List<Map<String, Object>> operations = new ArrayList<>();
        for(Object obj : jsonArray){
            JSONObject mapObject = (JSONObject) obj;

            // 将每个 Map 对象转换为 Map<String, Object>
            Map<String, Object> map = new HashMap<>();
            for (Object key : mapObject.keySet()) {
                map.put((String) key, mapObject.get(key));
            }
            // 将 Map 添加到 List 中
            operations.add(map);
        }
        System.out.println("transactoinCommit receive operations: " + operations.toString());

        Integer currentVersion = transactionBuffer.getVersion(transactionId);

        //check transaction status
        if(Objects.equals(transactionBuffer.getStatus(transactionId), "fail")){
            return Result.error("-100","Your transaction is fail, sorry!", currentVersion);
        }
        else{
            //check transaction version
            if(transactionVersion < transactionBuffer.getVersion(transactionId)){
                return Result.error(
                        "-100","Old transaction version!", currentVersion);
            }
            else{
                synService.transactionCommit(databaseId, transactionId, transactionVersion, operations);
                return Result.success(String.valueOf(0),"finish transaction!");
            }
        }
    }
}
