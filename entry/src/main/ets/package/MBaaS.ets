import webSocket from '@ohos.net.webSocket';
import data from '@ohos.telephony.data';
import http from '@ohos.net.http';
import call from '@ohos.telephony.call';
import { ErrorCode } from '@ohos.ability.errorCode';

const SUCCESS:number = 0;
const TRANSACTION_FAILED:number = -100;
const NETWORK_ERROR:string = "Network error with response code ";

const ADD = "add";
const MODIFY = "modify";
const DELETE = "delete";

const NEW_TABLE = "newTable";
const DELETE_TABLE = "deleteTable";
const NEW_FIELD = "newField";
const DELETE_FIELD = "deleteField";
const RENAME_TABLE = "renameTable";
const RENAME_FIELD = "renameField";

const EQUAL = "=";
const LESS = "<";
const LESS_OR_EQUAL = "<=";
const GREATER = ">";
const GREATER_OR_EQUAL = ">=";
const NOT_EQUAL = "!=";

const TABLE_ID_AGGREGATION = "TableIdAggregation";
const TABLE_ID_JOIN = "TableIdJoin";

const FIELD_TYPE:Set<string> =new Set<string>(["string", "int", "double", "image", "boolean"]);

const TRANS_STATUS_RUNNING = "running";
const TRANS_STATUS_FAIL = "failed";
const TRANS_STATUS_COMMIT = "commit";
const TRANS_FAILED = "transaction failed";
const TRANS_COMMIT_SUCCESS = "Transaction successfully committed!"

const OPERATION_UPDATE = "update";
const OPERATION_ADD = "add";
const OPERATION_DEL ="delete";


namespace MBaaS{

  const serverUrl:string = "47.94.130.65:8081";
//   const serverUrl:string = "10.0.2.2:8088";
  const wsSuffix:string = "/websocket/";

  let wsUid:string = "";

  let tables:Map<string, TableRef> = new Map();
  let transactionStatus:Map<string, string> = new Map();


  export const SYN_CONTENT = "SynContent";
  export const SYN_FIELD = "SynField";
  export const AGGREGATION_SUM = "sum";
  export const AGGREGATION_AVERAGE = "average";
  export const AGGREGATION_MAX = "max";
  export const AGGREGATION_MIN = "min";


  interface Response {
    statusCode: number;
    errMessage: string;
    // metaData: Record<string, string[]>;
    // metaData: MetaData[];
    metaData:Map<string, Map<string, string>>;
    records: Record<string, any>[];

    transactionId: string;
    transactionVersion: number;

    uuid: string;

    // 补bug
    data?:any;
    code?:any;
    msg?:any;
  }

  interface MetaData {
    tableId: string;
    fieldAndType: Map<string, string>
  }
  interface WsMessage {
    messageType:string;
    databaseId:string;
    tableId:string;
    updateData?:Array<synContent>;

    fieldId?:string;
    fieldType?:string;
    changeType?:string;
    newItem?:string;
    oldItem?:string;
  }
  interface synContent {
    changeType:string;
    oldItem:Record<string, any>;
    newItem:Record<string, any>;
  }
  export interface contentData {
    messageType:string;
    databaseId:string;
    tableId:string;
    updateData:Array<synContent>;
  }
  export interface fieldData {
    messageType:string;
    databaseId:string;
    tableId:string;
    fieldId?:string;
    fieldType?:string;
    changeType:string;
    newField?:string;
    oldField?:string;
  }
  interface Operation {
    type:string;
    tableId:string;
    rowId?:string;
    data?:Record<string, any>;
  }

  class Connect {
    private wsAddress:string = "ws://" + serverUrl + wsSuffix;
    public static httpAddress:string = "http://" + serverUrl + "/";
    private readonly db:DatabaseHelper;

    public ws:webSocket.WebSocket = null;
    public isConnect:Boolean = false;

    constructor(databaseHelper:DatabaseHelper) {
      // socket地址补上数据库id后缀
      this.db = databaseHelper;
      this.wsAddress += databaseHelper.databaseId;
      this.wsAddress += "_" + wsUid;

      // 建立连接
      this.ws = webSocket.createWebSocket();
      this.ws.connect(this.wsAddress, (err, value) => {
        if (!err){
          console.log("Connect to " + this.wsAddress + " successfully");
        }
        else {
          console.log("Connection failed. Err:" + JSON.stringify(err));
        }
      });

      // 回调
      this.ws.on("open", (err, value) => {
        console.log("websocket on open");
        this.isConnect = true;
      })

      this.ws.on("close", (err, value) => {
        console.log("websocket on close, code is " + value.code + ", reason is " + value.reason);
        this.isConnect = false;
      })

      this.ws.on('error', (err) => {
        console.log("on error, error:" + JSON.stringify(err));
      });
    }

    public connect():void {
      this.ws.connect(this.wsAddress, (err, value) => {
        if (!err){
          console.log("Connect to " + this.wsAddress + " successfully");
        }
        else {
          console.log("Connection failed. Err:" + JSON.stringify(err));
        }
      });
    }
  }

  export class TableRef {
    private readonly db:DatabaseHelper;
    private tableId:string;  // 数据库表名
    private fields:Set<string> = new Set<string>();
    private fieldType:Map<string, string>;
    private keyFieldName:string;

    constructor(db:DatabaseHelper, tableId:string, fields?:Set<string>, keyFieldName?:string, fieldType?:Map<string, string>) {
      this.db = db;
      this.tableId = tableId;
      if (fields != undefined){
        this.fields = fields;
      }
      if (keyFieldName != undefined){
        this.keyFieldName = keyFieldName;
      }
      if (fieldType != undefined){
        this.fieldType = fieldType;
      }

    }
    public getKeyFieldName():string {
      return this.keyFieldName;
    }
    public getFields():Set<string>{
      return this.fields;
    }
    public getFieldType():Map<string, string>{
      return this.fieldType;
    }

    public record(keyFieldValue:any):RecordRef {
      let record = new RecordRef(this.db.databaseId, this.tableId, this.keyFieldName, keyFieldValue);
      return record;
    }

    public setFields(fields:Record<string, string>, isKeyAutoIncre:number = 1):Promise<string> {
      let promise = new Promise<string>((resolve, reject) => {
        for (const name in fields){
          if (!FIELD_TYPE.has(fields[name])){
            reject("Type " + fields[name] + " is invalid!");
          }
        }
        let httpRequest = http.createHttp();
        httpRequest.request(
          Connect.httpAddress + 'setFields',
          {
            method: http.RequestMethod.POST,
            header: {
              'Content-Type': 'application/json',
            },
            extraData: {
              "databaseId": this.db.databaseId,
              "tableId": this.tableId,
              "newFields": fields,
              "autoIncre": isKeyAutoIncre
            },
          }
          , (err, data) => {
          if (!err) {
            if (data.responseCode == 200) {
              let res = data.result.toString();
              const resJson = JSON.parse(res) as Response;
              // console.log(JSON.stringify(resJson));
              if (resJson.statusCode == SUCCESS) {
                this.fields = new Set<string>();
                this.fieldType = new Map<string, string>();
                for (const key in fields){
                  this.fields.add(key);
                  this.fieldType.set(key, fields[key]);
                }
                const entries = Object.entries(fields);
                const [firstKey, firstValue] = entries[0];
                this.keyFieldName = firstKey;
                resolve("Set fields successfully!");
              }
              else {
                reject("Fail to set fields!");
              }
            }
            else {
              reject(NETWORK_ERROR + data.responseCode);
            }
          } else {
            console.info('error:' + JSON.stringify(err));
            reject(err);
          }
        }
        );
      });
      return promise
    }

    public addField(field:string, fieldType:string):Promise<string> {
      let promise = new Promise<string>( (resolve, reject) => {
        if (!FIELD_TYPE.has(fieldType)){
          reject("Type " + fieldType + " is invalid!")
        }
        let httpRequest = http.createHttp();
        httpRequest.request(
          Connect.httpAddress + 'addField',
          {
            method: http.RequestMethod.POST,
            header: {
              'Content-Type': 'application/json',
            },
            extraData: {
              "databaseId": this.db.databaseId,
              "tableId": this.tableId,
              "newField": field,
              "fieldType": fieldType,
            },
          },
          (err, data) => {
            if (!err){
              if (data.responseCode == 200){
                let res = data.result.toString();
                const resJson = JSON.parse(res) as Response;
                if (resJson.statusCode == SUCCESS) {
                  this.fields.add(field);
                  resolve("Add a new field successfully!");
                }
                else {
                  reject("Fail to add a new field!");
                }
              }
              else {
                reject(NETWORK_ERROR + data.responseCode);
              }
            }
            else{
              console.info('error:' + JSON.stringify(err));
              reject(err);
            }
          }
        );
      });
      return promise;
    }

    public deleteField(field:string):Promise<string> {
      let promise = new Promise<string>( (resolve, reject) => {
        if (!this.fields.has(field)){
          reject("This table do not contain field " + field);
        }
        let httpRequest = http.createHttp();
        httpRequest.request(
          Connect.httpAddress + 'deleteField',
          {
            method: http.RequestMethod.POST,
            header: {
              'Content-Type': 'application/json',
            },
            extraData: {
              "databaseId": this.db.databaseId,
              "tableId": this.tableId,
              "deleteField": field,
            },
          },
          (err, data) => {
            if (!err) {
              if (data.responseCode == 200) {
                let res = data.result.toString();
                const resJson = JSON.parse(res) as Response;
                if (resJson.statusCode == SUCCESS) {
                  this.fields.delete(field);
                  resolve("Delete a field successfully!");
                }
                else {
                  reject("Fail to delete a field!");
                }
              }
              else {
                reject(NETWORK_ERROR + data.responseCode);
              }
            }
            else {
              console.info('error:' + JSON.stringify(err));
              reject(err);
            }
          }
        );
      });
      return promise;
    }

    // 向表中新增一条数据
    public add(data:Record<string, any>):Promise<string> {
      let promise = new Promise<string>( (resolve, reject) => {
        // 检查新记录的字段是否都属于表的字段
        // console.log(this.fields.size.toString());
        for (const key in data) {
          if (!this.fields.has(key)){
            reject("This table do not contain field " + key);
            return;
          }
        }

        let httpRequest = http.createHttp();
        httpRequest.request(
          Connect.httpAddress + 'addRecord',
          {
            method: http.RequestMethod.POST,
            header: {
              'Content-Type': 'application/json',
            },
            extraData: {
              "databaseId": this.db.databaseId,
              "tableId": this.tableId,
              "payload": data,
            },
          },
          (err, data) => {
            if (!err) {
              if (data.responseCode == 200) {
                let res = data.result.toString();
                const resJson = JSON.parse(res) as Response;
                if (resJson.statusCode == SUCCESS) {
                  resolve("Add a new record successfully!");
                }
                else {
                  reject("Fail to add a new record!");
                }
              }
              else {
                reject(NETWORK_ERROR + data.responseCode);
              }
            }
            else {
              console.info('error:' + JSON.stringify(err));
              reject(err);
            }
          }
        );
      });
      return promise;
    }

    public async get():Promise<RecordRef[]> {
      let query = new Query(this.db.databaseId, this.tableId);
      let res:RecordRef[] = await query.get();
      return res;
    }

    public subscribe(callback:(data:fieldData|contentData) => void): ()=>void {
      return this.db.subscribe(this.tableId, callback);
    }

    public whereEqualTo(whereField:string, whereTargetValue:any):Query {
      let query = new Query(this.db.databaseId, this.tableId, whereField, EQUAL, whereTargetValue);
      return query;
    }

    public whereLessThan(whereField:string, whereTargetValue:any):Query {
      let query = new Query(this.db.databaseId, this.tableId, whereField, LESS, whereTargetValue);
      return query;
    }

    public whereLessThanOrEqualTo(whereField:string, whereTargetValue:any):Query {
      let query = new Query(this.db.databaseId, this.tableId, whereField, LESS_OR_EQUAL, whereTargetValue);
      return query;
    }

    public whereGreaterThan(whereField:string, whereTargetValue:any):Query {
      let query = new Query(this.db.databaseId, this.tableId, whereField, GREATER, whereTargetValue);
      return query;
    }

    public whereGreaterThanOrEqualTo(whereField:string, whereTargetValue:any):Query {
      let query = new Query(this.db.databaseId, this.tableId, whereField, GREATER_OR_EQUAL, whereTargetValue);
      return query;
    }

    public whereNotEqualTo(whereField:string, whereTargetValue:any):Query {
      let query = new Query(this.db.databaseId, this.tableId, whereField, NOT_EQUAL, whereTargetValue);
      return query;
    }

    public orderBy(orderField:string, order:string = "asc"):Query {
      let orderFields:[string, string][] = new Array();
      orderFields.push([orderField, order]);
      let query = new Query(this.db.databaseId, this.tableId, undefined, undefined, undefined, undefined, orderFields);
      return query;
    }

    public aggregation(fieldName:string, operation:string):Query {
      let query = new Query(this.db.databaseId, this.tableId, undefined, undefined, undefined, undefined, undefined, [fieldName, operation]);
      return query;
    }

  }

  export class RecordRef {
    private databaseId:string;
    private tableId:string;
    private _data:Record<string, any>;
    private keyFieldValue:any;
    private keyFieldName:string;

    constructor(databaseId:string, tableId:string, keyFieldName?:string, keyFieldValue?:string, _data?:Record<string, any>) {
      this.databaseId = databaseId;
      this.tableId = tableId;
      if (keyFieldName != undefined){
        this.keyFieldName = keyFieldName;
      }
      if (keyFieldValue != undefined){
        this.keyFieldValue = keyFieldValue;
      }
      if (_data != undefined){
        this._data = _data;
      }
    }

    public data():Record<string, any> {
      return this._data;
    }

    public getDatabaseId():string {
      return this.databaseId;
    }
    public getTableId():string {
      return this.tableId;
    }
    // public getKeyFieldValue():any {
    //   return this.keyFieldValue;
    // }
    public setData(data:Record<string, any>):void{
      this._data = data;
    }

    public async get():Promise<RecordRef[]> {
      let query = new Query(this.databaseId, this.tableId, this.keyFieldName, EQUAL, this.keyFieldValue);
      // console.log(this.keyFieldName);
      // console.log(this.keyFieldValue);
      let res:RecordRef[] = await query.get();
      let record = res.pop();
      this._data = record.data();
      return res;
    }

    public set(data:Record<string, any>):Promise<string> {
      let promise = new Promise<string>( (resolve, reject) => {
        let httpRequest = http.createHttp();
        // console.log("hhhhhh");
        // console.log(this.keyFieldName);
        // console.log(this.keyFieldValue);
        httpRequest.request(
          Connect.httpAddress + 'setRecord',
          {
            method: http.RequestMethod.POST,
            header: {
              'Content-Type': 'application/json',
            },
            extraData: {
              "databaseId": this.databaseId,
              "tableId": this.tableId,
              "rowId": this.keyFieldValue,
              "payload": data,
            },
          },
          (err, data) => {
            if (!err) {
              if (data.responseCode == 200) {
                let res = data.result.toString();
                const resJson = JSON.parse(res) as Response;
                if (resJson.statusCode == SUCCESS) {
                  resolve("Set the record successfully!");
                }
                else {
                  reject("Fail to set the record!");
                }
              }
              else {
                reject(NETWORK_ERROR + data.responseCode);
              }
            }
            else {
              console.info('error:' + JSON.stringify(err));
              reject(err);
            }
          }
        );
      });
      return promise;
    }

    public update(data:Record<string, any>):Promise<string> {
      let promise = new Promise<string>( (resolve, reject) => {
        let httpRequest = http.createHttp();
        console.log(this.keyFieldValue);
        console.log(this.keyFieldName);
        console.log(JSON.stringify(data));
        httpRequest.request(
          Connect.httpAddress + 'updateRecord',
          {
            method: http.RequestMethod.POST,
            header: {
              'Content-Type': 'application/json',
            },
            extraData: {
              "databaseId": this.databaseId,
              "tableId": this.tableId,
              "rowId": this.keyFieldValue,
              "payload": data,
            },
          },
          (err, data) => {
            if (!err) {
              if (data.responseCode == 200) {
                let res = data.result.toString();
                const resJson = JSON.parse(res) as Response;
                if (resJson.statusCode == SUCCESS) {
                  resolve("Update the record successfully!");
                }
                else {
                  reject("Fail to update the record!");
                }
              }
              else {
                reject(NETWORK_ERROR + data.responseCode);
              }
            }
            else {
              console.info('error:' + JSON.stringify(err));
              reject(err);
            }
          }
        );
      });
      return promise;
    }

    public delete():Promise<string> {
      let promise = new Promise<string>( (resolve, reject) => {
        let httpRequest = http.createHttp();
        httpRequest.request(
          Connect.httpAddress + 'deleteRecord',
          {
            method: http.RequestMethod.POST,
            header: {
              'Content-Type': 'application/json',
            },
            extraData: {
              "databaseId": this.databaseId,
              "tableId": this.tableId,
              "rowId": this.keyFieldValue,
            },
          },
          (err, data) => {
            if (!err) {
              if (data.responseCode == 200) {
                let res = data.result.toString();
                const resJson = JSON.parse(res) as Response;
                if (resJson.statusCode == SUCCESS) {
                  resolve("Delete the record successfully!");
                }
                else {
                  reject("Fail to delete the record!");
                }
              }
              else {
                reject(NETWORK_ERROR + data.responseCode);
              }
            }
            else {
              console.info('error:' + JSON.stringify(err));
              reject(err);
            }
          }
        );
      });
      return promise;
    }
  }

  class Condition {
    public whereField:string;
    public whereRelation:string;
    public whereTargetValue:string;

    constructor(field:string, relation:string, targetValue:string) {
      // console.log("condition: targetValue:" + targetValue);
      this.whereField = field;
      this.whereRelation = relation;
      this.whereTargetValue = targetValue;
    }
  }

  export class Query {
    private databaseId:string;
    private tableId:string;
    private queryResult:RecordRef[] = [];
    private conditions:Condition[] = [];
    private orderFields:[string, string][] = [];
    private limitNumber:number;
    // 目前要求aggregation必须是操作串的最后一个操作
    // 即 .aggregation.where 是非法操作, aggregation后的所有where将被忽略
    private aggre:[string, string] = undefined;
    private isTransaction:boolean = false;
    private transactionId:string;
    private join:Record<string, string> = {};

    constructor(databaseId:string, tableId:string, whereField?:string, whereRelation?:string, whereTargetValue?:any, conditions?:Condition[],
                orderFields?:[string, string][], aggregation?:[string, string], transactionId?:string, join?:Record<string, string>) {
      this.databaseId = databaseId;
      this.tableId = tableId;
      if (conditions != undefined) {
        this.conditions = this.conditions.concat(conditions);
      }
      else if (whereField != undefined && whereRelation != undefined && whereTargetValue != undefined){
        let con = new Condition(whereField, whereRelation, whereTargetValue as string);
        this.conditions.push(con);
      }
      if (orderFields != undefined){
        this.orderFields = orderFields;
      }
      if (aggregation != undefined){
        this.aggre = aggregation;
      }
      if (transactionId != undefined){
        this.isTransaction = true;
        this.transactionId = transactionId;
      }
      if (join != undefined){
        this.join = join;
      }
    }

    public get():Promise<RecordRef[]> {
      let promise = new Promise<RecordRef[]>( (resolve, reject) => {
        let httpRequest = http.createHttp();
        let urlSuffix = this.isTransaction ? "transactionQuery" : "query";

        // console.log(JSON.stringify(this.isTransaction));
        // console.log(this.databaseId);
        // console.log(this.tableId);
        // console.log(this.conditions.length.toString());

        // for (let condition of this.conditions){
        //   console.log(condition.whereField);
        //   console.log(condition.whereRelation);
        //   console.log(condition.whereTargetValue);
        // }
        console.log("MBaaS query")
        console.log("whereconditions: " + JSON.stringify(this.conditions));
        console.log("join: " + JSON.stringify(this.join));
        httpRequest.request(
          Connect.httpAddress + urlSuffix,
          {
            method: http.RequestMethod.POST,
            header: {
              'Content-Type': 'application/json',
            },
            extraData: {
              "databaseId": this.databaseId,
              "tableId": this.tableId,
              "whereConditions": this.conditions,
              "join": this.join,
            },
          }
          , (err, data) => {
          if (!err){
            if (data.responseCode == 200) {
              let res = data.result.toString();
              const resJson = JSON.parse(res) as Response;
              console.log("MBaaS query result")
              console.log(JSON.stringify(resJson));
              if (resJson.statusCode == SUCCESS) {
                for (let record of resJson.records){
                  // !!! 缺省了key字段，record似乎没有第一条记录的概念，可能需要后端显示存一个字段
                  let newRec = new RecordRef(this.databaseId, this.tableId);
                  newRec.setData(record);
                  this.queryResult.push(newRec);
                }
                console.log("MBaaS query length: " + this.queryResult.length.toString())

                // order
                // 大小写敏感
                if (this.orderFields.length > 0){
                  let len = this.orderFields.length;
                  this.queryResult.sort( (a, b) => {
                    let curLevel = 0; // 当前考虑的标准层
                    while (curLevel < len){
                      let targetField = this.orderFields[curLevel][0];
                      let targetOrder = this.orderFields[curLevel][1];
                      let value_a = a.data()[targetField];
                      let value_b = b.data()[targetField];
                      if (value_a != undefined && value_b != undefined){
                        if (value_a != value_b){
                          if (targetOrder == "desc"){
                            return value_b - value_a;
                          }
                          else {
                            return value_a - value_b;
                          }
                        }
                      }
                      curLevel++;
                    }
                    return -1;
                  });
                }
                // limit
                if (this.limitNumber != undefined){
                  if (this.queryResult.length >= this.limitNumber){
                    this.queryResult = this.queryResult.slice(0, this.limitNumber);
                  }
                }
                // aggregation
                if (this.aggre != undefined){
                  let field = this.aggre[0];
                  switch(this.aggre[1]) {
                    case AGGREGATION_SUM:
                      let sum = 0;
                      console.log(JSON.stringify(this.queryResult))
                      this.queryResult.forEach( (record) => {
                        console.log(JSON.stringify(record.data()[field]));
                        sum = sum + record.data()[field];
                      });
                      console.log(sum.toString());
                      let newRecord:Record<string, number> = {
                        "sum": sum,
                      }
                      this.queryResult = [];
                      let newRecordRef = new RecordRef(this.databaseId, TABLE_ID_AGGREGATION, undefined, undefined, newRecord);
                      this.queryResult.push(newRecordRef);
                      break;

                    case AGGREGATION_AVERAGE:
                      let averageSum = 0;
                      this.queryResult.forEach( (record) => {
                        averageSum = averageSum + record.data()[field];
                      });
                      let average = averageSum / this.queryResult.length;
                      newRecord = {
                        "average": average,
                      }
                      this.queryResult = [];
                      newRecordRef = new RecordRef(this.databaseId, TABLE_ID_AGGREGATION, undefined, undefined, newRecord);
                      this.queryResult.push(newRecordRef);
                      break;

                    case AGGREGATION_MAX:
                      let maxValue = this.queryResult[0].data()[field];
                      for (let i = 1; i < this.queryResult.length; i++){
                        if (this.queryResult[i].data()[field] > maxValue){
                          maxValue = this.queryResult[i].data()[field];
                        }
                      }
                      newRecord = {
                        "max": maxValue,
                      }
                      this.queryResult = [];
                      newRecordRef = new RecordRef(this.databaseId, TABLE_ID_AGGREGATION, undefined, undefined, newRecord);
                      this.queryResult.push(newRecordRef);
                      break;

                    case AGGREGATION_MIN:
                      let minValue = this.queryResult[0].data()[field];
                      for (let i = 1; i < this.queryResult.length; i++){
                        if (this.queryResult[i].data()[field] < minValue){
                          minValue = this.queryResult[i].data()[field];
                        }
                      }
                      newRecord = {
                        "min": minValue,
                      }
                      this.queryResult = [];
                      newRecordRef = new RecordRef(this.databaseId, TABLE_ID_AGGREGATION, undefined, undefined, newRecord);
                      this.queryResult.push(newRecordRef);
                      break;
                  }
                }
                resolve(this.queryResult);
              }
              else if (resJson.statusCode == TRANSACTION_FAILED){
                if (!this.isTransaction){
                  console.log("Error! Receive transaction failed code -100, but this query do not from a transaction!");
                }
                console.log("query err");
                // transactionStatus.set(this.transactionId, TRANS_STATUS_FAIL);
                reject(new Error(TRANS_FAILED));
              }
              else {
                reject("Fail to query data!");
              }
            }
            else {
              reject(NETWORK_ERROR + data.responseCode);
            }
          }
          else{
            console.info('error:' + JSON.stringify(err));
            reject(err);
          }
        }
        );
      });
      return promise;
    }

    public where(whereField:string, whereRelation:string, whereTargetValue:any):Query {
      // 过滤aggregation非法顺序
      if (this.aggre != undefined){
        return this;
      }
      let condition = new Condition(whereField, whereRelation, whereTargetValue);
      this.conditions.push(condition);
      console.log(this.conditions.length.toString());
      return this;
    }
    // desc降序  asc升序
    public orderBy(orderFiled:string, order:string = "asc"):Query {
      this.orderFields.push([orderFiled, order]);
      return this;
    }

    public limit(limitNumber:number):Query {
      this.limitNumber = limitNumber;
      return this;
    }

    public aggregation(fieldName:string, operation:string):Query {
      this.aggre = [fieldName, operation];
      return this;
    }
  }

  class Transaction {
    protected  transactionId: string;
    protected transactionVersion:number = 0;
    protected databaseId:string;
    protected operations:Operation[] = [];


    constructor(databaseId:string, id:string, ver:number) {
      this.databaseId = databaseId;
      this.transactionId = id;
      this.transactionVersion = ver;
      transactionStatus.set(id, TRANS_STATUS_RUNNING);
    }

    public query(tableId:string, keyFieldValue?:any):Query {
      if (keyFieldValue == undefined){
        let query = new Query(this.databaseId, tableId, undefined, undefined, undefined, undefined, undefined, undefined, this.transactionId);
        return query;
      }
      else{
        let tableRef = tables.get(tableId);
        let query = new Query(this.databaseId, tableId, tableRef.getKeyFieldName(), EQUAL, keyFieldValue, undefined, undefined, undefined, this.transactionId);
        return query;
      }
    }

    public add(tableId, data:Record<string, any>):void{
      let op:Operation = {
        type:OPERATION_ADD,
        tableId: tableId,
        data:data,
      };
      this.operations.push(op);
    }

    public update(tableId:string, keyFieldValue:any, data:Record<string, any>):void{
      let op:Operation = {
        type: OPERATION_UPDATE,
        tableId: tableId,
        rowId: keyFieldValue,
        data: data,
      };
      this.operations.push(op);
    }

    public delete(tableId:string, keyFieldValue:any):void{
      let op:Operation = {
        type: OPERATION_DEL,
        tableId: tableId,
        rowId: keyFieldValue,
      };
      this.operations.push(op);
    }


  }

  class TransForCommit extends Transaction {
    constructor(databaseId:string, id:string, ver:number) {
      super(databaseId, id, ver);
    }
    public async commit():Promise<string>{
      let httpRequest = http.createHttp();
      let data = await httpRequest.request(
        Connect.httpAddress + 'transactionCommit',
        {
          method: http.RequestMethod.POST,
          header: {
            'Content-Type': 'application/json',
          },
          extraData: {
            "databaseId": this.databaseId,
            "transactionId": this.transactionId,
            "transactionVersion": this.transactionVersion,
            "operations": this.operations,
          },
        },
      );
      if (data.responseCode == 200){
        let res = data.result.toString();
        const resJson = JSON.parse(res) as Response;
        console.log(JSON.stringify(resJson));
        if (resJson.statusCode == SUCCESS){
          transactionStatus.set(this.transactionId, TRANS_STATUS_COMMIT);
          return TRANS_COMMIT_SUCCESS;
        }
        else if (resJson.statusCode == TRANSACTION_FAILED){
          // throw new Error(TRANS_FAILED);
          // transactionStatus.set(this.transactionId, TRANS_STATUS_FAIL);
          console.log("commit err");
          return TRANS_FAILED;
        }
        else{
          // throw new Error(TRANS_FAILED);
          return "Error!";
        }
      }
      else{
        return NETWORK_ERROR + data.responseCode;
      }
    }

    // public async commit():Promise<string>{
    //   let promise = new Promise<string>( async (resolve, reject)=>{
    //     let httpRequest = http.createHttp();
    //     let data = await httpRequest.request(
    //       Connect.httpAddress + 'transactionCommit',
    //       {
    //         method: http.RequestMethod.POST,
    //         header: {
    //           'Content-Type': 'application/json',
    //         },
    //         extraData: {
    //           "databaseId": this.databaseId,
    //           "transactionId": this.transactionId,
    //           "transactionVersion": this.transactionVersion,
    //           "operations": this.operations,
    //         },
    //       },
    //     );
    //     if (data.responseCode == 200){
    //       let res = data.result.toString();
    //       const resJson = JSON.parse(res) as Response;
    //       console.log(JSON.stringify(resJson));
    //       if (resJson.statusCode == SUCCESS){
    //         transactionStatus.set(this.transactionId, TRANS_STATUS_COMMIT);
    //         resolve(TRANS_COMMIT_SUCCESS);
    //       }
    //       else if (resJson.statusCode == TRANSACTION_FAILED){
    //         // throw new Error(TRANS_FAILED);
    //         // transactionStatus.set(this.transactionId, TRANS_STATUS_FAIL);
    //         reject(new Error(TRANS_FAILED));
    //       }
    //       else{
    //         // throw new Error(TRANS_FAILED);
    //         reject("Error!");
    //       }
    //     }
    //     else{
    //       reject(NETWORK_ERROR + data.responseCode);
    //     }
    //   });
    //   return promise;
    // }



  }

  export class DatabaseHelper {
    public databaseId:string = null;
    private connect:Connect = null;
    // 表名（tableId）到 table实体的映射
    // private tables:Map<string, TableRef> = new Map();
    private listenMap= new Map();

    // private transactionStatus:Map<string, string> = new Map();


    constructor(databaseId:string) {
      this.databaseId = databaseId;
      // 初始化数据库各表的元数据
      let httpRequest = http.createHttp();
      let res:string;

      httpRequest.request(
        Connect.httpAddress + 'synMetadata',
        {
          method: http.RequestMethod.POST,
          header: {
            'Content-Type': 'application/json',
          },
          extraData: {
            "databaseId" : this.databaseId,
          },
        }
        ,(err, data) =>{
        if (!err){
          if (data.responseCode == 200){
            res = data.result.toString();
            const resJson = JSON.parse(res) as Response;

            // console.info('code:' + JSON.stringify(data.responseCode));
            console.info('Result:' + JSON.stringify(resJson));

            wsUid = resJson.uuid;
            console.log("uuid: " + wsUid);
            this.connect = new Connect(this);


            this.connect.ws.on('message', (err, value) => {
              console.log("on message, message:" + JSON.stringify(value));

              let msg = JSON.parse(value as string) as WsMessage;
              console.log(JSON.stringify(msg));

              if (this.listenMap.has(msg.tableId)){
                switch (msg.messageType) {
                  case SYN_CONTENT:
                    let cData:contentData = {
                      messageType: SYN_CONTENT,
                      tableId: msg.tableId,
                      databaseId: msg.databaseId,
                      updateData: msg.updateData,
                    };
                    let content_callback = this.listenMap.get(msg.tableId);
                    content_callback(cData);
                    break;
                  case SYN_FIELD:
                  // console.log("here field");
                    let fData:fieldData = {
                      messageType: SYN_FIELD,
                      tableId: msg.tableId,
                      databaseId: msg.databaseId,
                      fieldId: msg.fieldId,
                      fieldType:msg.fieldType,
                      changeType: msg.changeType,
                      newField: msg.newItem,
                      oldField: msg.oldItem,
                    };
                    console.log(msg.changeType)
                    switch(msg.changeType){
                      case NEW_TABLE:
                        let tableRef = new TableRef(this, msg.tableId);
                        tables.set(msg.tableId, tableRef);
                        break;
                      case DELETE_TABLE:
                        if (tables.has(msg.tableId)){
                          tables.delete(msg.tableId);
                        }
                        break;
                      case RENAME_TABLE:
                        if (tables.has(msg.tableId)){
                          let oldTableRef = tables.get(msg.oldItem);
                          let fields = oldTableRef.getFields();
                          let keyFiledName = oldTableRef.getKeyFieldName();
                          let fieldType = oldTableRef.getFieldType();
                          let newTableRef = new TableRef(this, msg.tableId, fields, keyFiledName, fieldType);
                          tables.set(msg.tableId, newTableRef);
                          tables.delete(msg.oldItem);
                        }
                        else{
                          console.log("Error, table" + msg.tableId + "do not exist!");
                        }
                        break;
                      case NEW_FIELD:
                        if (tables.has(msg.tableId)){
                          let oldTableRef = tables.get(msg.tableId);
                          let fields = oldTableRef.getFields();
                          fields.add(msg.fieldId);
                          let keyFiledName = oldTableRef.getKeyFieldName();
                          let fieldType:Map<string, string> = oldTableRef.getFieldType();
                          fieldType.set(msg.fieldId, msg.fieldType);
                          let newTableRef = new TableRef(this, msg.tableId, fields, keyFiledName, fieldType);
                          console.log("new field " + JSON.stringify(fields));
                          tables.set(msg.tableId, newTableRef);
                        }
                        else{
                          console.log("Error, table" + msg.tableId + "do not exist!");
                        }
                        break;
                      case DELETE_FIELD:
                        if (tables.has(msg.tableId)){
                          let oldTableRef = tables.get(msg.tableId);
                          let fields = oldTableRef.getFields();
                          fields.delete(msg.fieldId);
                          let keyFiledName = oldTableRef.getKeyFieldName();
                          let fieldType:Map<string, string> = oldTableRef.getFieldType();
                          fieldType.delete(msg.fieldId);
                          let newTableRef = new TableRef(this, msg.tableId, fields, keyFiledName, fieldType);
                          tables.set(msg.tableId, newTableRef);
                        }
                        else{
                          console.log("Error, table" + msg.tableId + "do not exist!");
                        }
                        break;
                      case RENAME_FIELD:
                        if (tables.has(msg.tableId)){
                          let oldTableRef = tables.get(msg.tableId);
                          let fields = oldTableRef.getFields();
                          fields.delete(msg.oldItem);
                          fields.add(msg.newItem);
                          let keyFiledName = oldTableRef.getKeyFieldName();
                          let fieldType:Map<string, string> = oldTableRef.getFieldType();
                          fieldType.set(msg.newItem, fieldType.get(msg.oldItem));
                          fieldType.delete(msg.oldItem);
                          let newTableRef = new TableRef(this, msg.tableId, fields, keyFiledName, fieldType);
                          tables.set(msg.tableId, newTableRef);
                        }
                        else{
                          console.log("Error, table" + msg.tableId + "do not exist!");
                        }
                        break;
                    }
                    let field_callback = this.listenMap.get(msg.tableId);
                    field_callback(fData);
                    break;
                }
              }
            });



            const jsonObject = JSON.parse(JSON.stringify(resJson.metaData));
            for (const [tableId, fieldTypeObj] of Object.entries(jsonObject)){
              // console.log(tableId);
              let fields:Set<string> = new Set();
              let keyField:string;
              let index = 0;
              let typeMap = new Map<string, string>();
              for (const [field, type] of Object.entries(fieldTypeObj)){
                typeMap.set(field, type);
                fields.add(field);
                if (index == 0){
                  keyField = field;
                }
                index++;
              }
              // console.log(index.toString());
              let tableRef = new TableRef(this, tableId, fields, keyField, typeMap);
              // this.tables.set(tableId, tableRef);
              tables.set(tableId, tableRef);

            }
          }
          // 网络请求失败，容错暂时空着
          else {
          }
        } else {
          console.info('error:' + JSON.stringify(err));
        }
      }
      );



    }

    public connectWebSocket():void {
      this.connect.connect();
    }

    public table(tableId:string):TableRef{
      if (tables.has(tableId)){
        return tables.get(tableId)
      }
      else {
        let tableRef = new TableRef(this, tableId);
        tables.set(tableId, tableRef);
        return tableRef;
      }
    }

    public subscribe(tableId:string, callback:(data:fieldData|contentData) => void): ()=>void {
      console.log("subscribe here");
      let httpRequest = http.createHttp();
      httpRequest.request(
        Connect.httpAddress + 'subscribe',
        {
          method: http.RequestMethod.POST,
          header: {
            'Content-Type': 'application/json',
          },
          extraData: {
            "databaseId": this.databaseId,
            "tableId": tableId,
            "uuid": wsUid,
            // to be fix
            // "deviceId": "2333333"
          },
        },
        (err, data) => {
          if (!err) {
            if (data.responseCode == 200) {
              let res = data.result.toString();
              const resJson = JSON.parse(res) as Response;
              if (resJson.statusCode == SUCCESS) {
                this.listenMap.set(tableId, callback);
                console.log("subscribe success " + tableId);
              }
              else {
                console.log("subscribe success" )
              }
            }
            else {
              console.log("network " + data.responseCode.toString())
            }
          }
          else {
            console.info('error:' + JSON.stringify(err));
          }
        }
      );
      return () =>{
        if(this.listenMap.has(tableId)){
          let httpRequest = http.createHttp();
          httpRequest.request(
            Connect.httpAddress + 'unSubscribe',
            {
              method: http.RequestMethod.POST,
              header: {
                'Content-Type': 'application/json',
              },
              extraData: {
                "databaseId": this.databaseId,
                "tableId": tableId
              },
            },
            (err, data) => {
              if (!err) {
                if (data.responseCode == 200) {
                  let res = data.result.toString();
                  const resJson = JSON.parse(res) as Response;
                  if (resJson.statusCode == SUCCESS) {
                    // this.listenMap.delete(tableId);
                  }
                  else {}
                }
                else {}
              }
              else {
                console.info('error:' + JSON.stringify(err));
              }
            }
          );
          this.listenMap.delete(tableId);

        }
      }
    }

    public join(tableId_1:string, fieldName_1:string, tableId_2:string, fieldName_2:string):Query{
      let query = new Query(this.databaseId, TABLE_ID_JOIN, undefined, undefined, undefined, undefined, undefined, undefined, undefined, {
        "tableId_1": tableId_1,
        "fieldId_1": fieldName_1,
        "tableId_2": tableId_2,
        "fieldId_2": fieldName_2
      });
      return query;
    }

    // public join(tableId_1:string, fieldName_1:string, tableId_2:string, fieldName_2:string):Promise<RecordRef[]> {
    //   let promise = new Promise<RecordRef[]>( (resolve, reject) => {
    //     let httpRequest = http.createHttp();
    //     httpRequest.request(
    //       Connect.httpAddress + 'join',
    //       {
    //         method: http.RequestMethod.POST,
    //         header: {
    //           'Content-Type': 'application/json',
    //         },
    //         extraData: {
    //           "databaseId": this.databaseId,
    //           "tableId_1": tableId_1,
    //           "fieldId_1": fieldName_1,
    //           "tableId_2": tableId_2,
    //           "fieldId_2": fieldName_2,
    //         },
    //       }
    //       , (err, data) => {
    //         if (!err){
    //           if (data.responseCode == 200){
    //             let res = data.result.toString();
    //             const resJson = JSON.parse(res) as Response;
    //             console.log(JSON.stringify(resJson));
    //             if (resJson.statusCode == SUCCESS) {
    //               let joinResults:RecordRef[] = [];
    //               for (let record of resJson.records){
    //                 // !!! 缺省了key字段，record似乎没有第一条记录的概念，可能需要后端显示存一个字段
    //                 let newRec = new RecordRef(this.databaseId, TABLE_ID_JOIN);
    //                 newRec.setData(record);
    //                 joinResults.push(newRec);
    //               }
    //               resolve(joinResults);
    //             }
    //             else {
    //               reject("Fail to join data!");
    //             }
    //           }
    //           else {
    //             reject(NETWORK_ERROR + data.responseCode);
    //           }
    //         }
    //         else {
    //           console.info('error:' + JSON.stringify(err));
    //           reject(err);
    //         }
    //       }
    //     );
    //   });
    //   return promise;
    // }

    async createTransaction():Promise<string|[string,number]>{
      let httpRequest = http.createHttp();
      let data = await httpRequest.request(
        Connect.httpAddress + 'createTransaction',
        {
          method: http.RequestMethod.POST,
          header: {
            'Content-Type': 'application/json'
          },
          extraData: {
            "databaseId": this.databaseId,
          },
        }
      );
      if (data.responseCode == 200){
        let res = data.result.toString();
        const resJson = JSON.parse(res) as Response;
        console.log(JSON.stringify(resJson));
        if (resJson.statusCode == SUCCESS){
          transactionStatus.set(resJson.transactionId, TRANS_STATUS_RUNNING);
          return [resJson.transactionId, resJson.transactionVersion];
        }
        else{
          return "Fail to create Transaction!";
        }
      }
      else{
        return NETWORK_ERROR + data.responseCode;
      }
    }

    async runTransaction(callback:(transaction:Transaction)=>Promise<any>):Promise<any> {
      let promise = new Promise<any>( async(resolve, reject)=>{
        let response = await this.createTransaction();
        if (typeof response === 'string') {
          reject(response);
        }
        let [id, ver] = response as [string, number];
        let transaction = new TransForCommit(this.databaseId, id, ver);
        let tryCount = 0;
        let maxCount = 4;
        let returnValue;
        do{
          transactionStatus.set(id, TRANS_STATUS_RUNNING);
          try {
            returnValue = await callback(transaction as Transaction);
            let res = await transaction.commit();
            if (res != TRANS_COMMIT_SUCCESS){
              // reject(new Error(TRANS_FAILED));
              throw new Error(TRANS_FAILED);
            }
          } catch (error){
            console.log("try num: " + tryCount + "  catch " + error);
            if (error.message == TRANS_FAILED){
              transactionStatus.set(id, TRANS_STATUS_FAIL);
              // console.log("try time: " + tryCount);
              if (tryCount > maxCount){
                console.log("out of max retry time");
                break;
              }
              tryCount++;
            }
            else{
              resolve("Transaction failed! Error: " + error.message);
            }
          }
        }while(transactionStatus.get(id) != TRANS_STATUS_COMMIT);

        if (transactionStatus.get(id) == TRANS_STATUS_COMMIT){
          if (returnValue != undefined){
            resolve(returnValue);
          }
          else{
            resolve(TRANS_COMMIT_SUCCESS)
          }
        }
        else{
          resolve("Transaction failed! Please retry.");
        }
      });
      return promise;
    }
  }

}

export {MBaaS}