package com.tiger.baas.utils;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public class Result<T> {
    @JsonProperty(value = "statusCode")
    private String statusCode;
    @JsonProperty(value = "errMessage")
    private String errMessage;

    private Map<String, Map<String, String>> metaData;
    private List<Map<String, String>> records;

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public String getErrMessage() {
        return errMessage;
    }

    public void setErrMessage(String errMessage) {
        this.errMessage = errMessage;
    }

    public Map<String, Map<String, String>> getMetaData() {
        return metaData;
    }

    public void setMetaData(Map<String, Map<String, String>> metaData) {
        this.metaData = metaData;
    }

    public List<Map<String, String>> getRecords() {
        return records;
    }

    public void setRecords(List<Map<String, String>> records) {
        this.records = records;
    }

    public Result() {
    }

    public static Result success() {
        Result result = new Result<>();
        result.setStatusCode("0");
        result.setErrMessage("成功");
        return result;
    }

    public static Result success(String statusCode, String errMessage){
        Result result = new Result();
        result.setErrMessage(errMessage);
        result.setStatusCode(statusCode);
        return result;
    }


    public static Result successMeta(String statusCode, String errMessage, Map<String, Map<String, String>> metaData){
        Result result = new Result();
        result.setStatusCode(statusCode);
        result.setErrMessage(errMessage);
        result.setMetaData(metaData);
        return result;
    }

    public static Result successRecord(String statusCode, String errMessage, List<Map<String, String>> records){
        Result result = new Result();
        result.setStatusCode(statusCode);
        result.setErrMessage(errMessage);
        result.setRecords(records);
        return result;
    }



    public static Result error(String statusCode, String errMessage) {
        Result result = new Result();
        result.setStatusCode(statusCode);
        result.setErrMessage(errMessage);
        return result;
    }
}
