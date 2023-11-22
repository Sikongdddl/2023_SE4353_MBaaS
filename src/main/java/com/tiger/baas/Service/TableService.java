package com.tiger.baas.Service;

import com.tiger.baas.entity.MetaData;
import com.tiger.baas.repository.MetaDataRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
public class TableService {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Resource
    private MetaDataRepo metaDataRepo;

    public List<String> getAllTableNames() {
        return entityManager.createNativeQuery("SHOW TABLES").getResultList();
    }

    public void createTable(String tableName, List<String> fields){
    String createTableSQL = "CREATE TABLE IF NOT EXISTS "+tableName+" ("
            + "uuid INT AUTO_INCREMENT PRIMARY KEY,";
    for (String field : fields){
        createTableSQL = createTableSQL + field + " VARCHAR(255),";
    }
    int length = createTableSQL.length();
    createTableSQL = createTableSQL.substring(0,length-1);
    createTableSQL += ")";

    System.out.println(createTableSQL);

    jdbcTemplate.execute(createTableSQL);

    //System.out.println("Table created successfully.");
    }

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
        //create if new table
        List<String> tableNames = this.getAllTableNames();
        Boolean newTableFlag = Boolean.TRUE;
        for(String tableName : tableNames){
            if(tableName == tableId){
                newTableFlag = Boolean.FALSE;
            }
        }
        if(newTableFlag == Boolean.TRUE){
            createTable(tableId,newFields);
        }

        //else modify field name
        //must discard all old data if change the name&type of field
        //so delete old table and create a new one from the same implementation

        //maintain metaData Table
        metaDataRepo.deleteByTablebelong(tableId);
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
        String sql = "ALTER TABLE " + tableId + " ADD COLUMN " + newField + " " + "VARCHAR(255)" ;
        jdbcTemplate.execute(sql);

        MetaData newRecord = new MetaData();
        newRecord.setFieldname(newField);
        newRecord.setTablebelong(tableId);
        newRecord.setDatabaseid(databaseId);
        metaDataRepo.saveAndFlush(newRecord);
        return "0";
    }

    public String deleteFields(String databaseId, String tableId, String deleteField){
        String sql = "ALTER TABLE " + tableId + " DROP COLUMN " + deleteField ;
        jdbcTemplate.execute(sql);

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
