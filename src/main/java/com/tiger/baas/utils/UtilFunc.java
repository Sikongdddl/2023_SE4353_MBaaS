package com.tiger.baas.utils;

import net.sf.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

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


}
