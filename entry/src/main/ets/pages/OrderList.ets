import {OrderCard} from '../view/OrderCard'
import router from '@ohos.router';
import { MBaaS } from '../package/MBaaS';

@Entry
@Component
struct OrderList {
  @State message: string = 'Hello World'
  filterUserName:string = "";
  filterOrderPrice:number = 0;
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
    }
  ]

  async onPageShow(){
    console.log("order manage appear");
    let db: MBaaS.DatabaseHelper = globalThis.db;
    db.join("orders", "orderid", "items", "orderid").get().then((records)=>{
      console.log(JSON.stringify(records));
    })
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
            this.filterOrderPrice = parseFloat(value);
          })

        Image($r("app.media.load"))
          .height("70%")
          .margin({left:15})
          .onClick(()=>{
            console.log("order manage appear");
            let db: MBaaS.DatabaseHelper = globalThis.db;

          })


      }.height(30).width("100%")

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