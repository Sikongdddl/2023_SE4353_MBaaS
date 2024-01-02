import {OrderCard} from '../view/OrderCard'
import router from '@ohos.router';
import { MBaaS } from '../package/MBaaS';

@Entry
@Component
struct OrderList {
  @State message: string = 'Hello World'
  filterUserName:string = "";
  filterOrderPrice:string = "";
  filterRelation:string="";
  @State orders: Record<string, any>[] = [
    {
      "userName": "alice",
      "orderNumber": "111",
      "orderItems": [
        {
          "name": "高考英语 五年高考三年模拟",
          "number": 2,
          "singlePrice": 17.5
        },
        {
          "name": "红楼梦",
          "number": 1,
          "singlePrice": 12.5
        }
      ],
      "totalPrice": 47.5,
    },
    {
      "userName": "alice",
      "orderNumber": "222",
      "orderItems": [
        {
          "name": "高考英语 五年高考三年模拟",
          "number": 1,
          "singlePrice": 17.5
        },
        {
          "name": "红楼梦",
          "number": 1,
          "singlePrice": 12.5
        },
        {
          "name": "草房子",
          "number": 1,
          "singlePrice": 20,
        }
      ],
      "totalPrice": 50,
    },
    {
      "userName": "alice",
      "orderNumber": "333",
      "orderItems": [
        {
          "name": "高考英语 五年高考三年模拟",
          "number": 1,
          "singlePrice": 17.5
        },
        {
          "name": "红楼梦",
          "number": 1,
          "singlePrice": 12.5
        },
        {
          "name": "草房子",
          "number": 1,
          "singlePrice": 20,
        }
      ],
      "totalPrice": 50,
    },
    {
      "userName": "alice",
      "orderNumber": "444",
      "orderItems": [
        {
          "name": "高考英语 五年高考三年模拟",
          "number": 1,
          "singlePrice": 17.5
        },
        {
          "name": "红楼梦",
          "number": 1,
          "singlePrice": 12.5
        },
        {
          "name": "草房子",
          "number": 1,
          "singlePrice": 20,
        }
      ],
      "totalPrice": 50,
    }
  ];
  @State sum:number = 0;
  @State average:number = 0;
  @State max:number = 0;
  @State min:number = 0;


  async onPageShow(){
    console.log("order manage appear");
    let db: MBaaS.DatabaseHelper = globalThis.db;
    let records = await db.join("orders", "orderid", "items", "orderid").orderBy("orderid").get();
    let curOrderNumber = "";
    let newOrder:Record<string, any> = {}
    let newItems:Record<string, any>[] = [];
    let totalPrice = 0;
    this.orders = [];
    for (let record of records){
      console.log(JSON.stringify(record));
      let data:Record<string, any> = record.data();
      if (data["orderid"] != curOrderNumber){
        if (curOrderNumber != ""){
          newOrder["orderItems"] = newItems;
          console.log(totalPrice.toString());
          newOrder["totalPrice"] = totalPrice;
          console.log(JSON.stringify(newOrder));
          this.orders.push(newOrder);
          newOrder = {};
          newItems = [];
          totalPrice = 0;
        }
        newOrder["orderNumber"] = data["orderid"];
        newOrder["userName"] = data["username"];
        newOrder["orderItems"] = [];
        newItems.push({
          "name": data["bookname"],
          "number": data["number"],
          "singlePrice": data["singleprice"]
        })
        curOrderNumber = data["orderid"];
        totalPrice += data["singleprice"] * data["number"];
        console.log(JSON.stringify(data["singleprice"]));
        console.log(JSON.stringify(data["number"]));
        console.log(totalPrice.toString());
      }
      else{
        newItems.push({
          "name": data["bookname"],
          "number": data["number"],
          "singlePrice": data["singleprice"]
        })

        totalPrice += data["singleprice"] * data["number"];
        console.log(JSON.stringify(data["singleprice"]));
        console.log(JSON.stringify(data["number"]));
        console.log(totalPrice.toString());
      }
    }
    newOrder["orderItems"] = newItems;
    newOrder["totalPrice"] = totalPrice;
    this.orders.push(newOrder);

    let res_sum = await db.table("orders").aggregation("totalprice", MBaaS.AGGREGATION_SUM).get();
    this.sum = res_sum.pop().data()["sum"];

    let res_aver = await db.table("orders").aggregation("totalprice", MBaaS.AGGREGATION_AVERAGE).get();
    this.average = res_aver.pop().data()["average"];

    let res_max = await db.table("orders").aggregation("totalprice", MBaaS.AGGREGATION_MAX).get();
    this.max = res_max.pop().data()["max"]

    let res_min = await db.table("orders").aggregation("totalprice", MBaaS.AGGREGATION_MIN).get();
    this.min = res_min.pop().data()["min"]

    // db.join("orders", "orderid", "items", "orderid").orderBy("orderid").get().then((records)=>{
    //   // console.log(JSON.stringify(records));
    //
    // })
  }

  build() {


    Column(){
      Row(){
        Image($r('app.media.ic_public_back'))
          .margin({left:20})
          .height("60%")
          .onClick(()=>{
            router.back();
          })

        Text("订单管理")
          .fontSize(20)
          .width("50%").height("100%")
          .textAlign(TextAlign.Center)
          .fontWeight(FontWeight.Bold)
          .margin({left:"15%"})

      }.margin({top:10}).width("100%").height(47)

      Row(){
        Text("用户名").height("100%")
          .fontColor("#ff3d92e3")
          .fontSize(15)
          .textAlign(TextAlign.Center)
          .width("15%")

        TextInput()
          .height("100%")
          .width("20%")
          .onChange((value)=>{
            this.filterUserName = value;
          })

        Text("订单总价").height("100%")
          .fontColor("#ff3d92e3")
          .fontSize(15)
          .textAlign(TextAlign.Center)
          .width("17%")
          .margin({left:"5%"})

        Select([
          {value:'>'},
          {value:'>='},
          {value:'='},
          {value:'<'},
          {value:'<='},
        ]).selected(1)
          .onSelect((index, value)=>{
            console.log(value);
            this.filterRelation = value;
          })

        TextInput()
          .width("20%")
          .height("100%")
          .onChange((value)=>{
            this.filterOrderPrice = value;
          })

        Image($r("app.media.load"))
          .height("70%")
          .margin({left:15})
          .onClick(async ()=>{
            console.log("order filter");
            console.log(this.filterOrderPrice.toString())
            console.log(this.filterRelation)
            console.log(this.filterUserName)
            let db: MBaaS.DatabaseHelper = globalThis.db;
            let records;
            if (this.filterOrderPrice != "" && this.filterRelation != ""){
              if (this.filterUserName == ""){
                console.log("aaa")
                records = await db.join("orders", "orderid", "items", "orderid")
                  .where("totalprice",this.filterRelation,parseFloat(this.filterOrderPrice))
                  .orderBy("orderid")
                  .get();
              }
              else{
                console.log("bbb")
                records = await db.join("orders", "orderid", "items", "orderid")
                  .where("username", "=", this.filterUserName)
                  .where("totalprice", this.filterRelation, parseFloat(this.filterOrderPrice))
                  .orderBy("orderid")
                  .get();
              }
            }
            else{
              console.log("ccc")
              if (this.filterUserName == ""){
                records = await db.join("orders", "orderid", "items", "orderid")
                  .orderBy("orderid")
                  .get();
              }
              else{
                console.log("ddd")
                records = await db.join("orders", "orderid", "items", "orderid")
                  .where("username", "=", this.filterUserName)
                  .orderBy("orderid")
                  .get();
              }
            }

            let curOrderNumber = "";
            let newOrder:Record<string, any> = {}
            let newItems:Record<string, any>[] = [];
            let totalPrice = 0;
            this.orders = [];
            console.log("233333333333333")
            console.log(JSON.stringify((records)));
            for (let record of records){
              console.log("here")
              console.log(JSON.stringify(record));
              let data:Record<string, any> = record.data();
              if (data["orderid"] != curOrderNumber){
                if (curOrderNumber != ""){
                  newOrder["orderItems"] = newItems;
                  // to be fix 改成单价和
                  // totalPrice = 100.5;
                  console.log(totalPrice.toString());
                  newOrder["totalPrice"] = totalPrice;
                  console.log(JSON.stringify(newOrder));
                  this.orders.push(newOrder);
                  newOrder = {};
                  newItems = [];
                  totalPrice = 0;
                }
                newOrder["orderNumber"] = data["orderid"];
                newOrder["userName"] = data["username"];
                newOrder["orderItems"] = [];
                newItems.push({
                  "name": data["bookname"],
                  "number": data["number"],
                  "singlePrice": data["singleprice"]
                })
                curOrderNumber = data["orderid"];
                totalPrice += data["singleprice"] * data["number"];
              }
              else{
                newItems.push({
                  "name": data["bookname"],
                  "number": data["number"],
                  "singlePrice": data["singleprice"]
                })

                totalPrice += data["singleprice"] * data["number"];
              }
            }
            newOrder["orderItems"] = newItems;
            newOrder["totalPrice"] = totalPrice;
            this.orders.push(newOrder);

            // let res_sum = await db.table("orders").aggregation("totalprice", MBaaS.AGGREGATION_SUM).get();
            // this.sum = res_sum.pop().data()["sum"];
            //
            // let res_aver = await db.table("orders").aggregation("totalprice", MBaaS.AGGREGATION_AVERAGE).get();
            // this.average = res_aver.pop().data()["average"];
            //
            // let res_max = await db.table("orders").aggregation("totalprice", MBaaS.AGGREGATION_MAX).get();
            // this.max = res_max.pop().data()["max"]
            //
            // let res_min = await db.table("orders").aggregation("totalprice", MBaaS.AGGREGATION_MIN).get();
            // this.min = res_min.pop().data()["min"]
          })


      }.height(33).width("100%")

      Column(){
        Text("订单价格统计：")
          .alignSelf(ItemAlign.Start)
          .margin({left:5})
        Row(){
          Text("总和：" + JSON.stringify(this.sum)).layoutWeight(1)
          Text("平均：" + JSON.stringify(this.average)).layoutWeight(1)
        }.height(25).width("70%")
        .margin({top:5, left:"15%", right:"15%"})
        Row(){
          Text("最大：" + JSON.stringify(this.max)).layoutWeight(1)
          Text("最小：" + JSON.stringify(this.min)).layoutWeight(1)
        }.height(25).width("70%")
        .margin({top:5, left:"15%", right:"15%"})
      }.width("100%").margin({top:15})


      // 表头
      Row(){
        Text("用户名")
          .fontSize(14)
          .fontColor("#989a9c")
          .width("15%")
          .textAlign(TextAlign.Center)
        // .backgroundColor(0xF5DEB3);

        Text("订单号")
          .fontSize(14)
          .fontColor("#989a9c")
          .width("15%")
          .textAlign(TextAlign.Center)
        // .backgroundColor(0xD2B48C);

        Text("书名")
          .fontSize(14)
          .fontColor("#989a9c")
          .width("25%")
          .textAlign(TextAlign.Center)
        // .backgroundColor(0xF5DEB3);

        Text("单价")
          .fontSize(14)
          .fontColor("#989a9c")
          .width("15%")
          .textAlign(TextAlign.Center)
        // .backgroundColor(0xD2B48C);

        Text("数量")
          .fontSize(14)
          .fontColor("#989a9c")
          .width("15%")
          .textAlign(TextAlign.Center)
        // .backgroundColor(0xF5DEB3);

        Text("总价")
          .fontSize(14)
          .fontColor("#989a9c")
          .width("15%")
          .textAlign(TextAlign.Center)
        // .backgroundColor(0xD2B48C);
      }.margin({top:15})

      Column(){
        List(){
          ForEach(this.orders, (item) => {
            ListItem(){
              OrderCard({
                orderItems: item.orderItems,
                userName: item.userName,
                totalPrice: item.totalPrice,
                orderNumber: item.orderNumber
              }).margin({top:15})
            }
          })
        }
      }







    }.width("100%")
  }
}