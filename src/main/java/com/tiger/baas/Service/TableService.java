package com.tiger.baas.Service;

import com.tiger.baas.entity.MetaData;
import com.tiger.baas.repository.MetaDataRepo;
import com.tiger.baas.utils.UtilFunc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class TableService {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Resource
    private MetaDataRepo metaDataRepo;

    private UtilFunc utilFunc = new UtilFunc();

    public List<String> getAllTableNames() {
        return entityManager.createNativeQuery("SHOW TABLES").getResultList();
    }

    public void createTable(String tableName, Map<String, String> fields){
    String createTableSQL = "CREATE TABLE IF NOT EXISTS "+tableName+" ("
            + "uuid INT AUTO_INCREMENT PRIMARY KEY,";

    for(Map.Entry<String, String> entry : fields.entrySet()){
        createTableSQL = createTableSQL + entry.getKey();

        if(Objects.equals(entry.getValue(), "string")){
            createTableSQL += " VARCHAR(255),";
        }

        if(Objects.equals(entry.getValue(), "boolean")){
            createTableSQL += " INT,";
        }
        if(Objects.equals(entry.getValue(), "int")){
            createTableSQL += " INT,";
        }
        if(Objects.equals(entry.getValue(), "double")){
            createTableSQL += " DOUBLE,";
        }
        if(Objects.equals(entry.getValue(), "image")){
            createTableSQL += " VARCHAR(255),";
        }

    }

    int length = createTableSQL.length();
    createTableSQL = createTableSQL.substring(0,length-1);
    createTableSQL += ")";

    System.out.println(createTableSQL);

    jdbcTemplate.execute(createTableSQL);

    //System.out.println("Table created successfully.");
    }

    public Map<String, List<String>> gainMetaValue(String database_id) throws IllegalAccessException {
        List<MetaData> preData = metaDataRepo.findByDatabaseid(database_id);

        Map<String, List<MetaData>> metaData = new HashMap<>();
        metaData = preData.stream()
                .collect(Collectors.groupingBy(MetaData::getTablebelong));
        //System.out.println(metaData);
        Map<String, List<String>> res = new HashMap<>();
        for(Map.Entry<String, List<MetaData>> entry : metaData.entrySet()){
            String tablename = entry.getKey();
            List<MetaData> valueList = entry.getValue();
            List<String> currentFields = new ArrayList<>();
            //System.out.println("Processing table: " + tablename);
            for (MetaData value : valueList){
                //System.out.println("Value: " + value);

                Field[] fields = value.getClass().getDeclaredFields();

                for(Field field : fields){
                    field.setAccessible(true);
                    Object fieldvalue = field.get(value);
                    //System.out.println("fieldvalue: " + fieldvalue);
                    if(field.getName() == "fieldname"){
                        //System.out.println("current fieldname:" + fieldvalue);
                        currentFields.add(fieldvalue.toString());
                    }
                }
                res.put(tablename,currentFields);
            }
        }
        return res;
    }

    public Map<String, Map<String,String>> gainMeta(String database_id) throws IllegalAccessException {
        List<MetaData> preData = metaDataRepo.findByDatabaseid(database_id);

        Map<String, List<MetaData>> metaData = new HashMap<>();
        metaData = preData.stream()
                .collect(Collectors.groupingBy(MetaData::getTablebelong));
        System.out.println(metaData);
        Map<String, List<String>> res = new HashMap<>();
        Map<String, Map<String, String>> newres = new HashMap<>();

        for(Map.Entry<String, List<MetaData>> entry : metaData.entrySet()){
            String tablename = entry.getKey();
            List<MetaData> valueList = entry.getValue();
            List<String> currentFields = new ArrayList<>();
            Map<String, String> currentFieldMap = new HashMap<>();

            System.out.println("Processing table: " + tablename);
            for (MetaData value : valueList){
                System.out.println("Value: " + value);

                Field[] fields = value.getClass().getDeclaredFields();

                String currentKey = "";
                String currentValue = "";

                for(Field field : fields){
                    field.setAccessible(true);
                    Object fieldvalue = field.get(value);
                    System.out.println("fieldvalue: " + fieldvalue);
                    if(field.getName() == "fieldname"){
                        System.out.println("current fieldname:" + fieldvalue);
                        currentFields.add(fieldvalue.toString());
                        currentKey = fieldvalue.toString();
                    }
                    if(field.getName() == "fieldtype"){
                        System.out.println("current fieldtype:" + fieldvalue);
                        currentFields.add(fieldvalue.toString());
                        currentValue = fieldvalue.toString();
                        currentFieldMap.put(currentKey,currentValue);
                    }

                }
                res.put(tablename,currentFields);
                newres.put(tablename,currentFieldMap);
            }
        }
        return newres;
    }

    public String setFields(String databaseId, String tableId, Map<String, String> newFields){
        //create if new table
        List<String> tableNames = this.getAllTableNames();
        System.out.println("All tablenames list: " + tableNames.toString());
        Boolean newTableFlag = Boolean.TRUE;
        for(String tableName : tableNames){
            if(Objects.equals(tableName, tableId)){
                newTableFlag = Boolean.FALSE;
            }
        }
        if(newTableFlag == Boolean.TRUE){
            System.out.println("new table! create it!!");
            createTable(tableId,newFields);
        }

        //else modify field name
        //must discard all old data if change the name&type of field
        //so delete old table and create a new one from the same implementation

        //maintain metaData Table
        metaDataRepo.deleteByTablebelong(tableId);

        for(Map.Entry<String, String> entry: newFields.entrySet()){
            MetaData newRecord = new MetaData();
            newRecord.setFieldname(entry.getKey());
            newRecord.setFieldtype(entry.getValue());
            newRecord.setTablebelong(tableId);
            newRecord.setDatabaseid(databaseId);
            metaDataRepo.saveAndFlush(newRecord);
        }
//        for (String newField : newFields){
//            MetaData newRecord = new MetaData();
//            newRecord.setFieldname(newField);
//            newRecord.setTablebelong(tableId);
//            newRecord.setDatabaseid(databaseId);
//            metaDataRepo.saveAndFlush(newRecord);
//        }
        return "0";
    }

    public String addFields(String databaseId, String tableId, String newField, String fieldType){

        String sql = "ALTER TABLE " + tableId + " ADD COLUMN " + newField  ;

        if(Objects.equals(fieldType, "string")){
            sql += " VARCHAR(255)";
        }

        if(Objects.equals(fieldType, "boolean")){
            sql += " INT";
        }
        if(Objects.equals(fieldType, "int")){
            sql += " INT";
        }
        if(Objects.equals(fieldType, "double")){
            sql += " DOUBLE";
        }
        if(Objects.equals(fieldType, "image")){
            sql += " VARCHAR(255)";
        }


        jdbcTemplate.execute(sql);

        MetaData newRecord = new MetaData();
        newRecord.setFieldname(newField);
        newRecord.setTablebelong(tableId);
        newRecord.setDatabaseid(databaseId);
        newRecord.setFieldtype(fieldType);
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

    public String addRecord(String databaseId, String tableId, Map<String, String> payload){
        StringBuilder sqlBuilder = new StringBuilder("INSERT INTO "+tableId+" (");
        StringBuilder valuesBuilder = new StringBuilder("VALUES (");

        for (Map.Entry<String, String> entry : payload.entrySet()) {
            String columnName = entry.getKey();
            String columnValue = entry.getValue();

            sqlBuilder.append(columnName).append(", ");
            valuesBuilder.append(":").append(columnName).append(", ");
        }

        sqlBuilder.delete(sqlBuilder.length() - 2, sqlBuilder.length());
        valuesBuilder.delete(valuesBuilder.length() - 2, valuesBuilder.length());

        sqlBuilder.append(") ").append(valuesBuilder).append(")");

        System.out.println(sqlBuilder);
        int affectedRows = namedParameterJdbcTemplate.update(sqlBuilder.toString(), payload);

        return "0";
    }

    public String setRecord(String databaseId, String tableId, String rowId, Map<String, String> payload) throws IllegalAccessException {
        //gain all field list
        Map<String, List<String>> metaDataSet = this.gainMetaValue(databaseId);
        System.out.println(metaDataSet);
        List<String> fieldNameList = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : metaDataSet.entrySet()) {
            if(Objects.equals(entry.getKey(), tableId)){
                fieldNameList = entry.getValue();
            }
        }
        System.out.println(fieldNameList);

        StringBuilder sqlBuilder = new StringBuilder("UPDATE " + tableId + " SET ");
        System.out.println("before:" + payload);


        // set the other fields to null
        for(String fieldName : fieldNameList){
            System.out.println("Current field: " + fieldName);
            if(payload.containsKey(fieldName)){
                System.out.println("Contain this field: " + fieldName);
                continue;
            }
            else{
                payload.put(fieldName,"nullValue");
            }
        }

        System.out.println("after:" + payload);

        for (Map.Entry<String, String> entry : payload.entrySet()) {
            String columnName = entry.getKey();
            String columnValue = entry.getValue();
            sqlBuilder.append(columnName).append(" = :").append(columnName).append(", ");
        }

        sqlBuilder.delete(sqlBuilder.length() - 2, sqlBuilder.length());

        sqlBuilder.append(" WHERE uuid = :uuid");

        payload.put("uuid", rowId);

        int affectedRows = namedParameterJdbcTemplate.update(sqlBuilder.toString(), payload);

        return "0";
    }

    public String updateRecord(String databaseId, String tableId, String rowId, Map<String, String> payload){
        StringBuilder sqlBuilder = new StringBuilder("UPDATE " + tableId + " SET ");

        for (Map.Entry<String, String> entry : payload.entrySet()) {
            String columnName = entry.getKey();
            String columnValue = entry.getValue();

            sqlBuilder.append(columnName).append(" = :").append(columnName).append(", ");
        }

        sqlBuilder.delete(sqlBuilder.length() - 2, sqlBuilder.length());

        sqlBuilder.append(" WHERE uuid = :uuid");

        payload.put("uuid", rowId);

        int affectedRows = namedParameterJdbcTemplate.update(sqlBuilder.toString(), payload);

        return "0";
    }

    public String deleteRecord(String databaseId, String tableId, String rowId){
        String sql = "DELETE FROM "+tableId + " WHERE uuid = ?";
        jdbcTemplate.update(sql,rowId);
        return "0";
    }

    public List<Map<String, String>> query(String databaseId, String tableId, List<String> whereConditions){
        String whereField = whereConditions.get(0);
        String whereRelation = whereConditions.get(1);
        String whereTargetValue = whereConditions.get(2);

        String sql = "SELECT * FROM "+tableId + " WHERE " + whereField + " " + whereRelation + " " + whereTargetValue;

        List<Map<String, Object>> resultList = jdbcTemplate.queryForList(sql);

        List<Map<String, String>> res = utilFunc.convertListMapsToStringValues(resultList);

        return res;
    }

    public List<Map<String, String>> join(String databaseId, String tableId_1, String tableId_2, String fieldId_1, String fieldId_2){
        String sql = "SELECT t1.*, t2.* " +  " FROM " + tableId_1 + " t1 " + "INNER JOIN " + tableId_2 + " t2 ON t1." + fieldId_1 + " = t2." + fieldId_2;

        List<Map<String, Object>> resultList = jdbcTemplate.queryForList(sql);

        List<Map<String, String>> res = utilFunc.convertListMapsToStringValues(resultList);

        return res;
    }


}
