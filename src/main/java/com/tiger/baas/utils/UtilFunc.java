package com.tiger.baas.utils;

import net.sf.json.JSONArray;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.persistence.EntityManager;

public class UtilFunc {

    public List<String> jsonArrayToList(JSONArray jsonArray){
        List<String> resultList = new ArrayList<>();

        for (Object element : jsonArray) {
            if (element instanceof String) {
                resultList.add((String) element);
            }
        }
        return resultList;
    }

    public List<Map<String, String>> convertListMapsToStringValues(List<Map<String, Object>> inputList) {
        List<Map<String, String>> resultList = new ArrayList<>();

        for (Map<String, Object> inputMap : inputList) {
            Map<String, String> resultMap = new HashMap<>();

            for (Map.Entry<String, Object> entry : inputMap.entrySet()) {
                String key = entry.getKey();
                Object valueObject = entry.getValue();

                // 处理值为 null 的情况
                String valueString = Optional.ofNullable(valueObject)
                        .map(Object::toString)
                        .orElse(null);

                resultMap.put(key, valueString);
            }
            resultList.add(resultMap);
        }
        return resultList;
    }

    public String[] convertTableDbToTableAndDb(String tableDbName){
        String[] result = new String[2];
        int idx = tableDbName.indexOf("_Concat_");
        result[0] = tableDbName.substring(0,idx);
        result[1] = tableDbName.substring(idx+8);
        return result;
    }

    public String convertTableAndDbToTableDb(String tableId, String databaseId){
        String result = "";
        result = result + tableId + "_Concat_" + databaseId;
        return result;
    }


}
