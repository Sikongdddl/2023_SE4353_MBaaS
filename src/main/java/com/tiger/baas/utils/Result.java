package com.tiger.baas.utils;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public class Result<T> {
    @JsonProperty(value = "statusCode")
    private String statusCode;
    @JsonProperty(value = "errMessage")
    private String errMessage;
    @JsonProperty(value = "transactionId")
    private String transactionId;
    @JsonProperty(value = "transactionVersion")
    private int transactionVersion;
    @JsonProperty(value = "uuid")
    private String uuid;


    private Map<String, Map<String, String>> metaData;
    private List<Map<String, Object>> records;

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

    public List<Map<String, Object>> getRecords() {
        return records;
    }

    public void setRecords(List<Map<String, Object>> records) {
        this.records = records;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transationId) {
        this.transactionId = transationId;
    }

    public int getTransactionVersion() {
        return transactionVersion;
    }

    public void setTransactionVersion(int transationVersion) {
        this.transactionVersion = transationVersion;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
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


    public static Result successMeta(String statusCode, String errMessage, Map<String, Map<String, String>> metaData, String uuid){
        Result result = new Result();
        result.setStatusCode(statusCode);
        result.setErrMessage(errMessage);
        result.setMetaData(metaData);
        result.setUuid(uuid);
        return result;
    }

    public static Result successRecord(String statusCode, String errMessage, List<Map<String, Object>> records){
        Result result = new Result();
        result.setStatusCode(statusCode);
        result.setErrMessage(errMessage);
        result.setRecords(records);
        return result;
    }

    public static Result successTransactionCreate(String statusCode, String errMessage, String transationId, int transationVersion){
        Result result = new Result();
        result.setStatusCode(statusCode);
        result.setErrMessage(errMessage);
        result.setTransactionId(transationId);
        result.setTransactionVersion(transationVersion);
        return result;
    }

    public static Result successUUID(String statusCode, String errMessage, String uuid){
        Result result = new Result();
        result.setStatusCode(statusCode);
        result.setErrMessage(errMessage);
        result.setUuid(uuid);
        return result;
    }


    public static Result error(String statusCode, String errMessage) {
        Result result = new Result();
        result.setStatusCode(statusCode);
        result.setErrMessage(errMessage);
        return result;
    }

    public static Result error(String statusCode, String errMessage, int transactionversion) {
        Result result = new Result();
        result.setStatusCode(statusCode);
        result.setErrMessage(errMessage);
        result.setTransactionVersion(transactionversion);
        return result;
    }
}
