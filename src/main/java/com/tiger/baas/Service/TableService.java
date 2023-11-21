package com.tiger.baas.Service;

import com.tiger.baas.entity.MetaData;
import com.tiger.baas.repository.MetaDataRepo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
public class TableService {

    @Resource
    private MetaDataRepo metaDataRepo;

//    public Map<String, List<String>> gainMeta(String database_id) throws IllegalAccessException {
//        List<DataBase> preData = tableRepo.findByDatabaseid(database_id);
//
//        Map<String, List<DataBase>> metaData = new HashMap<>();
//        metaData = preData.stream()
//                .collect(Collectors.groupingBy(DataBase::getTablename));
//        System.out.println(metaData);
//        Map<String, List<String>> res = new HashMap<>();
//        for(Map.Entry<String, List<DataBase>> entry : metaData.entrySet()){
//            String tablename = entry.getKey();
//            List<DataBase> valueList = entry.getValue();
//            System.out.println("Processing table: " + tablename);
//            for (DataBase value : valueList){
//                System.out.println("Value: " + value);
//                List<String> currentMeta = new ArrayList<>();
//
//                Field[] fields = value.getClass().getDeclaredFields();
//
//                for(Field field : fields){
//                    field.setAccessible(true);
//                    //Object fieldvalue = field.get(value);
//                    currentMeta.add(field.getName());
//                }
//                res.put(tablename,currentMeta);
//            }
//        }
//        return res;
//
//    }
    public Map<String, List<String>> gainMeta(String database_id) throws IllegalAccessException {
        List<MetaData> preData = metaDataRepo.findByDatabaseid(database_id);

        Map<String, List<MetaData>> metaData = new HashMap<>();
        metaData = preData.stream()
                .collect(Collectors.groupingBy(MetaData::getTablebelong));
        System.out.println(metaData);
        Map<String, List<String>> res = new HashMap<>();
        for(Map.Entry<String, List<MetaData>> entry : metaData.entrySet()){
            String tablename = entry.getKey();
            List<MetaData> valueList = entry.getValue();
            List<String> currentFields = new ArrayList<>();
            System.out.println("Processing table: " + tablename);
            for (MetaData value : valueList){
                System.out.println("Value: " + value);

                Field[] fields = value.getClass().getDeclaredFields();

                for(Field field : fields){
                    field.setAccessible(true);
                    Object fieldvalue = field.get(value);
                    System.out.println("fieldvalue: " + fieldvalue);
                    if(field.getName() == "fieldname"){
                        System.out.println("current fieldname:" + fieldvalue);
                        currentFields.add(fieldvalue.toString());
                    }
                }
                res.put(tablename,currentFields);
            }
        }
        return res;
    }

    public String setFields(String databaseId, String tableId, List<String> newFields){
        metaDataRepo.deleteAll();
        for (String newField : newFields){
            MetaData newRecord = new MetaData();
            newRecord.setFieldname(newField);
            newRecord.setTablebelong(tableId);
            newRecord.setDatabaseid(databaseId);
            metaDataRepo.saveAndFlush(newRecord);
        }
        return "0";
    }

    public String addFields(String databaseId, String tableId, String newField){
            MetaData newRecord = new MetaData();
            newRecord.setFieldname(newField);
            newRecord.setTablebelong(tableId);
            newRecord.setDatabaseid(databaseId);
            metaDataRepo.saveAndFlush(newRecord);
            return "0";
    }

    public String deleteFields(String databaseId, String tableId, String deleteField){
            MetaData deleteOne = metaDataRepo.findByFieldname(deleteField);
            metaDataRepo.delete(deleteOne);
            return "0";
    }
//
//    public String addRecord(String databaseId, String tableId, Map<String, String> payload){
//
//    }
//
//    public String setRecord(String databaseId, String tableId, String rowId, Map<String, String> payload){
//
//    }
//
//    public String updateRecord(String databaseId, String tableId, String rowId, Map<String, String> payload){
//
//    }
//
//    public String deleteRecord(String databaseId, String tableId, String rowId){
//
//    }
}
