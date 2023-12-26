##### 导入模块

```typescript
import {MBaaS} from '${path}/MBaaS'
```

##### 初始化

```typescript
let db = new MBaaS.DatabaseHelper("database id");
```

##### 字段操作

* 以表名为book的表为例

```typescript
// 设置表的字段，如果该表不存在，则自动创建，创建时第一个字段为主键
db.table("book").setFields({
    "book_id": "int",
    "book_name": "string",
    "book_price": "double",
}).then( (response)=>{
    // response: Set fields successfully!
}).catch( (err)=>{

});

// 增加表的字段

db.table("book").addField("book_isbn", "string")
.then( (response)=>{
    // response: Add a new field successfully!
}).catch( (err)=>{

});

// 删除表的字段
db.table("book").deleteFields("book_isbn")
.then( (response)=>{
    // response: Delete a field successfully!
}).catch( (err)=>{

});
```

##### 数据读取

```typescript
// 获取全表数据
db.table("book").get().then( (records)=>{
    for (let record of records){
        print(record.data())
    }
});



// 获取book表中主键（bookId）值为3的记录
db.table("book").record(3).get()
.then( (record)=>{
    let data = records.pop().data();
    print(JSON.stringify(data));
});
```

##### 数据写入

```typescript
// 向表中新增一条数据
db.table("book").add({
    "book_name": "三国演义",
    "book_price": 20.5,
    "book_id": 3
}).then( (response)=>{
    // response: Add a new record successfully!
}).catch( (err)=>{
});

// 设置book表中主键（bookId）值为2的记录，覆盖整条记录
db.table("book").record(2).set({
    "book_id": 2
    "book_name": "西游记",
    "book_price": 20.5,
}).then( (response)=>{
    // response: Set the record successfully!
}).catch( (err)=>{
});


// 设置book表中主键（bookId）值为2的记录，仅更新提及的字段，没有提及的字段保持不变
db.table("book").record(2).update({
    "bookPrice": 19.5,
}).then( (response)=>{
    // response: Update the record successfully!
}).catch( (err)=>{
});
```

##### 数据删除

```typescript
// 删除book表中主键（bookId）值为2的记录
db.table("book").record(2).delete().then( (response)=>{
    // response: Delete the record successfully!
}).catch( (err)=>{

});


// 删除表
好像没支持这个功能
乐
```

##### filter

* 可连续使用，按顺序执行

```typescript
db.table("book").where("book_name", "=", "水浒传").get()
.then( (records)=>{
    for (let record of records){
        print(record.data())
    }
} );
< <= > >= !=
db.table("book").whereEqualTo("book_name", "水浒传").get()
.then( (records)=>{
    for (let record of records){
        print(record.data())
    }
} );
whereLessThan
whereLessThanOrEqualTo
whereGreaterThan
whereGreaterThanOrEqualTo
whereNotEqualTo
```

##### order

* "desc"降序，"asc"升序，第二个参数省略默认是"asc"

* 可连续使用，优先级按顺序执行

```typescript
db.table("book").orderBy("bookId", "desc").get()
.then( (records)=>{
    for (let record of records){
        print(record.data())
    }
});
```

##### aggregation

```typescript
db.table("book").aggregation("book_price", MBaaS.AGGREGATION_SUM).get()
.then( (records)=>{
    let data = records.pop().data();
    print(JSON.stringify(data["sum"]));
});
AGGREGATION_AVERAGE
AGGREGATION_MAX
AGGREGATION_MIN
```

##### join

* 以items和orders两张表为例

* 将返回所有orders表的order_id字段和items表的belong_order_id字段相等的记录，record.data()中的字段是两张表的并集

* join操作可以和filter、order和aggregation一起使用，操作优先级为 join > filter > order > limit > aggregation

```typescript
db.join("orders", "order_id", "items", "belong_order_id").get()
.then( (records)=>{
    for (let record of records){
        print(record.data())
    }
});
```

##### limit

* 获取前几条数据

```typescript
// 获取按照bookId降序排列后的前3条数据
db.table("book").orderBy("book_id", "desc").limit(3).get()
.then( (records)=>{
    for (let record of records){
        print(record.data())
    }
});
```

##### 订阅

todo

##### 事务

todo
