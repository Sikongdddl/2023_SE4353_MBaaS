package com.tiger.baas.Service;

import com.tiger.baas.entity.MetaData;
import com.tiger.baas.repository.MetaDataRepo;
import com.tiger.baas.utils.UtilFunc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
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
    private JdbcTemplate jdbcTemplate ;

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate ;

    @Resource
    private MetaDataRepo metaDataRepo ;

    private UtilFunc utilFunc = new UtilFunc();

    public List<String> getAllTableNames() {
        return entityManager.createNativeQuery("SHOW TABLES").getResultList();
    }

    public void createTable(String tableName, Map<String, String> fields){
    String createTableSQL = "CREATE TABLE IF NOT EXISTS "+tableName+" ("
            + "mbaas_system_id INT AUTO_INCREMENT PRIMARY KEY,";

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
            String fakeTableName = utilFunc.convertTableDbToTableAndDb(tablename)[0];
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
                res.put(fakeTableName,currentFields);
                newres.put(fakeTableName,currentFieldMap);
            }
        }
        return newres;
    }

    public String setFields(String databaseId, String tableId, Map<String, String> newFields){
        String errno = "0";
        //0 means success without create new table
        //1 means success with new table created
        //-1 means error

        //create if new table
        String realTableName = utilFunc.convertTableAndDbToTableDb(tableId, databaseId);
        List<String> tableNames = this.getAllTableNames();
        System.out.println("All tablenames list: " + tableNames.toString());
        Boolean newTableFlag = Boolean.TRUE;
        for(String tableName : tableNames){
            if(Objects.equals(tableName, realTableName)){
                newTableFlag = Boolean.FALSE;
            }
        }
        if(newTableFlag == Boolean.TRUE){
            System.out.println("new table! create it!!");
            createTable(realTableName,newFields);
        }
        else{
            System.out.println("exist table! delete and recreate it !!!");
            String sql = "DROP TABLE " + realTableName;
            jdbcTemplate.execute(sql);

            createTable(tableId,newFields);
            errno = "1";
        }

        //else modify field name
        //must discard all old data if change the name&type of field
        //so delete old table and create a new one from the same implementation

        //maintain metaData Table
        metaDataRepo.deleteByTablebelong(realTableName);

        boolean primaryKeyFlag = Boolean.FALSE;
        for(Map.Entry<String, String> entry: newFields.entrySet()){
            MetaData newRecord = new MetaData();
            newRecord.setFieldname(entry.getKey());
            newRecord.setFieldtype(entry.getValue());
            newRecord.setTablebelong(realTableName);
            newRecord.setDatabaseid(databaseId);
            if(primaryKeyFlag == Boolean.FALSE){
                newRecord.setPrimarykey(entry.getKey());
                primaryKeyFlag = Boolean.TRUE;
            }
            metaDataRepo.saveAndFlush(newRecord);
        }

        return errno;
    }

    public String addFields(String databaseId, String tableId, String newField, String fieldType){

        String realTableName = utilFunc.convertTableAndDbToTableDb(tableId, databaseId);

        String sql = "ALTER TABLE " + realTableName + " ADD COLUMN " + newField  ;

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
        newRecord.setTablebelong(realTableName);
        newRecord.setDatabaseid(databaseId);
        newRecord.setFieldtype(fieldType);
        metaDataRepo.saveAndFlush(newRecord);
        return "0";
    }

    public String deleteFields(String databaseId, String tableId, String deleteField){
        String realTableName = utilFunc.convertTableAndDbToTableDb(tableId, databaseId);
        String sql = "ALTER TABLE " + realTableName + " DROP COLUMN " + deleteField ;
        jdbcTemplate.execute(sql);

        MetaData deleteOne = metaDataRepo.findByFieldname(deleteField);
        metaDataRepo.delete(deleteOne);
        return "0";
    }

    public String addRecord(String databaseId, String tableId, Map<String, String> payload){
        String realTableName = utilFunc.convertTableAndDbToTableDb(tableId, databaseId);
        StringBuilder sqlBuilder = new StringBuilder("INSERT INTO "+realTableName+" (");
        StringBuilder valuesBuilder = new StringBuilder("VALUES (");

        for (Map.Entry<String, String> entry : payload.entrySet()) {
            String columnName = entry.getKey();

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
        String realTableName = utilFunc.convertTableAndDbToTableDb(tableId, databaseId);
        String primary_key_field_name = metaDataRepo.findDistinctFirstByTablebelong(realTableName).getPrimarykey();
        //gain all field list
        Map<String, List<String>> metaDataSet = this.gainMetaValue(databaseId);
        System.out.println(metaDataSet);
        List<String> fieldNameList = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : metaDataSet.entrySet()) {
            if(Objects.equals(entry.getKey(), realTableName)){
                fieldNameList = entry.getValue();
            }
        }
        System.out.println(fieldNameList);
        fieldNameList.remove(0);
        System.out.println(fieldNameList);

        StringBuilder sqlBuilder = new StringBuilder("UPDATE " + realTableName + " SET ");
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
            sqlBuilder.append(columnName).append(" = :").append(columnName).append(", ");
        }

        sqlBuilder.delete(sqlBuilder.length() - 2, sqlBuilder.length());

        System.out.println("primary key: " + primary_key_field_name);

        sqlBuilder.append(" WHERE "+primary_key_field_name+" = " + "'" + rowId + "'");

        //sqlBuilder.append(" WHERE "+primary_key_field_name+" = 'sikongsikong'");
        //sqlBuilder.append(" WHERE uuid = :uuid");
        //payload.put(primary_key_field_name,"'rowId'");
        System.out.println(sqlBuilder.toString());

        int affectedRows = namedParameterJdbcTemplate.update(sqlBuilder.toString(), payload);
        System.out.println(affectedRows);
        return "0";
    }

    public String updateRecord(String databaseId, String tableId, String rowId, Map<String, String> payload){
        String realTableName = utilFunc.convertTableAndDbToTableDb(tableId, databaseId);
        StringBuilder sqlBuilder = new StringBuilder("UPDATE " + realTableName + " SET ");

        for (Map.Entry<String, String> entry : payload.entrySet()) {
            String columnName = entry.getKey();

            sqlBuilder.append(columnName).append(" = :").append(columnName).append(", ");
        }

        sqlBuilder.delete(sqlBuilder.length() - 2, sqlBuilder.length());

        String primary_key_field_name = metaDataRepo.findDistinctFirstByTablebelong(realTableName).getPrimarykey();

        System.out.println("primary key: " + primary_key_field_name);

        sqlBuilder.append(" WHERE "+primary_key_field_name+" = " + "'" + rowId + "'");
        //sqlBuilder.append(" WHERE uuid = :uuid");

        int affectedRows = namedParameterJdbcTemplate.update(sqlBuilder.toString(), payload);

        return "0";
    }

    public String deleteRecord(String databaseId, String tableId, String rowId){
        String realTableName = utilFunc.convertTableAndDbToTableDb(tableId, databaseId);

        String primary_key_field_name = metaDataRepo.findDistinctFirstByTablebelong(realTableName).getPrimarykey();

        String sql = "DELETE FROM "+realTableName + " WHERE " + primary_key_field_name+" = ?";
        jdbcTemplate.update(sql,rowId);
        return "0";
    }

    public List<Map<String, Object>> query(String databaseId, String tableId, List<Map<String, String>> whereConditions){
        String realTableName = utilFunc.convertTableAndDbToTableDb(tableId, databaseId);
        String sql = "";
        String whereField = "";
        String fieldType = "";
        if(whereConditions.isEmpty()){
            sql = "SELECT * FROM " + realTableName;
        }
        else{
            sql = "SELECT * FROM "+realTableName + " WHERE ";
            //+ whereField + " " + whereRelation + " " + whereTargetValue;
            for(int i = 0; i < whereConditions.size(); ++i){
                whereField = whereConditions.get(i).get("whereField");
                sql += whereField;
                sql += " ";
                //System.out.println("WhereField: " + whereField);
                MetaData metaEntry = metaDataRepo.findDistinctFirstByFieldname(whereField);
                fieldType = metaEntry.getFieldtype();
                System.out.println("fieldType: " + metaEntry.getFieldtype());
                sql += whereConditions.get(i).get("whereRelation");
                sql += " ";
                if(Objects.equals(fieldType, "string")){
                    sql += "'";
                    sql += whereConditions.get(i).get("whereTargetValue");
                    sql += "'";
                }
                else{
                    sql += whereConditions.get(i).get("whereTargetValue");
                }
                if(i != whereConditions.size() - 1){
                    sql += " AND ";
                }
            }
        }
        System.out.println(sql);

        List<Map<String, Object>> resultList = jdbcTemplate.queryForList(sql);

        return resultList;
        //List<Map<String, String>> res = utilFunc.convertListMapsToStringValues(resultList);
        //return res;
    }

    public List<Map<String, Object>> joinAndQuery(String databaseId, String tableId_1, String tableId_2, String fieldId_1, String fieldId_2, List<Map<String, String>> whereConditions){
        String realTableName = utilFunc.convertTableAndDbToTableDb(tableId_1, databaseId);
        String realTableName1 = utilFunc.convertTableAndDbToTableDb(tableId_2, databaseId);
        String sql = "SELECT t1.*, t2.* " +  " FROM " + realTableName + " t1 " + "INNER JOIN " + realTableName1 + " t2 ON t1." + fieldId_1 + " = t2." + fieldId_2;
        if(!whereConditions.isEmpty()){
            sql +=  " WHERE ";
            String whereField = "";
            String fieldType = "";
            for(int i = 0; i < whereConditions.size(); ++i){
                whereField = whereConditions.get(i).get("whereField");
                sql += whereField;
                sql += " ";
                //System.out.println("WhereField: " + whereField);
                MetaData metaEntry = metaDataRepo.findDistinctFirstByFieldname(whereField);
                fieldType = metaEntry.getFieldtype();
                System.out.println("fieldType: " + metaEntry.getFieldtype());
                sql += whereConditions.get(i).get("whereRelation");
                sql += " ";
                if(Objects.equals(fieldType, "string")){
                    sql += "'";
                    sql += whereConditions.get(i).get("whereTargetValue");
                    sql += "'";
                }
                else{
                    sql += whereConditions.get(i).get("whereTargetValue");
                }
                if(i != whereConditions.size() - 1){
                    sql += " AND ";
                }
            }
        }

        System.out.println(sql);
        List<Map<String, Object>> resultList = jdbcTemplate.queryForList(sql);

        return resultList;
        //List<Map<String, String>> res = utilFunc.convertListMapsToStringValues(resultList);
        //return res;
    }


}
