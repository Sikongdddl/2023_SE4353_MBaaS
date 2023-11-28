package com.tiger.baas.utils;

import net.sf.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        return inputList.stream()
                .map(entry -> entry.entrySet().stream()
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                e -> e.getValue() != null ? e.getValue().toString() : null
                        )))
                .collect(Collectors.toList());
    }

//    public Map<String, String> jsonArrayToMap(JSONArray jsonArray){
//
//    }


}
