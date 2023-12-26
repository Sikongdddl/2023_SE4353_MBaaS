import {MBaaS} from '../package/MBaaS'
@Entry
@Component
struct Index {
  @State message: string = 'Hello World'
  db = new MBaaS.DatabaseHelper("12345678");
  unSubscribe;

  build() {
    Row() {
      Column() {
        Text(this.message)
          .fontSize(20)
          .fontWeight(FontWeight.Bold)
          .onClick( () => {
            this.message = this.db.databaseId;
          })
        Button("modify database")
          .onClick(()=>{
            this.db.table("new_table").add({
              "field_1": "test",
              "field_2": "test",
              "field_3": "test",
              "icon": "111",
            }).then((res)=>{
              this.message = res;
            })
          })
        Button("for test")
          .onClick( async()=>{
            let cnt = 0;
            this.db.runTransaction(async(transaction)=>{
                let records = await transaction.query("book", 3).get();
                let data = ""
                records.forEach( (record) => {
                  data = data + JSON.stringify(record.data()) + "\n";
                })
                // console.log(data);
              transaction.add("book", {
                "id": 5,
                "name": "soft engineering",
              });
              transaction.update("book", 4, {
                "id": 4,
                "name": "CSAPP",
              });
              transaction.delete("book", 2);
              // console.log("callback function " + cnt);
              cnt++;
              // transaction.get();
              return "string from callback";
            }).then((res) => {
              this.message = res;
            }).catch((err)=>{
              this.message = JSON.stringify(err);
            });
          })
        Button("server query")
          .colorBlend(Color.Red)
          .onClick( ()=>{
            this.db.table("example_user").whereEqualTo("uname", "sikong").where("birthdate", "=", "71").get().then((records) =>{
            // this.db.table("example_user").get().then( (records) =>{
                let data = ""
                records.forEach( (record) => {
                  data = data + JSON.stringify(record.data()) + "\n";
                })
                this.message = data;
              }).catch( (err) => {
                this.message = err;
              })
          })
        Button("server set fields")
          .colorBlend(Color.Red)
          .onClick( ()=>{
            this.db.table("example_userr").setFields({
              "passwd":"string",
              "uname":"string",
              "icon":"image",
              "birthdate":"string",
            }).then(
              (response)=>{
                this.message = response;
              }
            ).catch((err)=>{
              this.message = err;
            })
          })
        Button("server add field")
          .colorBlend(Color.Red)
          .onClick(()=>{
            this.db.table("example_user").addFiled("bbb", "int").then(
              (response)=>{
                this.message = response;
              }
            ).catch((err)=>{
              this.message = err;
            })
          })
        Button("server delete field")
          .colorBlend(Color.Red)
          .onClick(()=>{
            this.db.table("example_user").deleteField("bbb").then(
              (response)=>{
                this.message = response;
              }
            ).catch((err)=>{
              this.message = err;
            })
            this.db.table("example_user").deleteField("aaa");
          })
        Button("server add record")
          .colorBlend(Color.Red)
          .onClick(()=>{
            this.db.table("example_user").add({
              "passwd":"test",
              "uname":"test",
              "icon":"null",
              "birthdate":"0701"
            }).then((response)=>{
              this.message = response;
            }).catch((err)=>{
              this.message = err;
            })
          })
        Button("server set record")
          .colorBlend(Color.Red)
          .onClick(()=>{
            this.db.table("example_user").record("3").set({
              "passwd":"233333",
              "uname":"sikong",
              "icon":"null",
            }).then((response)=>{
              this.message = response;
            }).catch((err)=>{
              this.message = err;
            })
          })
        Button("server delete record")
          .colorBlend(Color.Red)
          .onClick(()=>{
            this.db.table("example_user").record("4").delete().then((response)=>{
              this.message = response;
            }).catch((err)=>{
              this.message = err;
            })
          })
        Button("server update record")
          .colorBlend(Color.Red)
          .onClick(()=>{
            this.db.table("example_user").record("3").update({
              "birthdate":"11111",
            }).then((response)=>{
              this.message = response;
            }).catch((err)=>{
              this.message = err;
            })
          })
        Button("server create table")
          .colorBlend(Color.Red)
          .onClick(() => {
            this.db.table("new_table").setFields({
              "field_1":"string",
              "field_2":"string",
              "field_3":"string",
            })
              .then( (response) => {
               this.message = response;
              }).catch( (err) => {
              this.message = err;
            });
          })
        Button("server join")
          .colorBlend(Color.Red)
          .onClick(()=>{
            this.db.join("example_user", "icon", "new_table", "icon").then( (records) =>{
                let data = ""
                records.forEach( (record) => {
                  data = data + JSON.stringify(record.data()) + "\n";
                })
                this.message = data;
              }).catch( (err) => {
                this.message = err;
              })
          })


        Button("subscribe-content")
          .onClick( () => {
            this.message = "";
            this.unSubscribe = this.db.table("table-1").subscribe(
              (data) => {
                if (data.messageType == MBaaS.SYN_CONTENT){
                  let contentData = data as MBaaS.contentData;
                  let data_1 = contentData.updateData.pop();
                  let cnt = data_1.newItem["field-1-1"];
                  this.message = this.message + JSON.stringify(cnt);
                }
              }
            )
          })

        Button("unSubscribe")
          .onClick( () => {
            this.unSubscribe();
          })

        Button("subscribe-field")
          .onClick( ()=>{
            let set = ""
            this.db.table("table-1").getFields().forEach( (value) => {
              set = set + value;
            });
            this.message = set;
            this.db.table("table-1").subscribe( (data) => {
              console.log("callback");
              if (data.messageType == MBaaS.SYN_FIELD){
                let contentData = data as MBaaS.fieldData;
                console.log(JSON.stringify(contentData));
                set = ""
                this.db.table("table-1").getFields().forEach( (value) => {
                  set = set + value;
                });
                this.message = set;
              }
            })
          })

        Button("order")
          .onClick( () => {
            this.db.table("table-1").orderBy("field-1-1").orderBy("field-1-3", "asc").orderBy("field-1-2", "desc").limit(2).get().then(
              (records) => {
                let data = ""
                records.forEach( (record) => {
                  data = data + JSON.stringify(record.data()) + "\n";
                })
                this.message = data;
              }).catch( (err) => {
              this.message = err;
            })
          })
        Button("aggregation-sum")
          .onClick( () => {
            this.db.table("table-1").orderBy("field-1-2", "desc").aggregation("field-1-2", MBaaS.AGGREGATION_SUM).get().then(
              (records) => {
                let data = records.pop().data()["sum"];
                this.message = JSON.stringify(data);
              }
            )
          })
        Button("aggregation-average")
          .onClick( () => {
            this.db.table("table-1").orderBy("field-1-2", "desc").aggregation("field-1-2", MBaaS.AGGREGATION_AVERAGE).get().then(
              (records) => {
                let data = records.pop().data()["average"];
                this.message = JSON.stringify(data);
              }
            )
          })
        Button("aggregation-max")
          .onClick( () => {
            this.db.table("table-1").orderBy("field-1-2", "desc").aggregation("field-1-3", MBaaS.AGGREGATION_MAX).get().then(
              (records) => {
                let data = records.pop().data()["max"];
                this.message = JSON.stringify(data);
              }
            )
          })
        Button("aggregation-min")
          .onClick( () => {
            this.db.table("table-1").aggregation("field-1-3", MBaaS.AGGREGATION_MIN).get().then(
              (records) => {
                let data = records.pop().data()["min"];
                this.message = JSON.stringify(data);
              }
            )
          })

      }
      .width('100%')
    }
    .height('100%')
  }
}