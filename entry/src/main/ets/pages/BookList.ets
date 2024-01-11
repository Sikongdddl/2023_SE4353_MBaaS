import router from '@ohos.router';
import { MBaaS } from '../package/MBaaS';
import {BookCard} from '../view/BookCard'
@Entry
@Component
struct BookList {

  @State books:Record<string, any>[] = [
    // {
    //   "name": "高考英语 五年高考三年模拟",
    //   "price": 25.5,
    //   "count": 10,
    //   "image": "http://img3m4.ddimg.cn/62/14/27883214-1_w_2.jpg"
    // },
    // {
    //   "name": "红楼梦",
    //   "price": 18.5,
    //   "count": 16,
    //   "image": "http://img3m6.ddimg.cn/31/22/23828836-1_w_8.jpg"
    // }
  ];
  unSubscribe;
  async fresh() {
    console.log("book list fresh");
    console.log(JSON.stringify(this.books))
    let db: MBaaS.DatabaseHelper = globalThis.db;
    this.books = [];
    let records = await db.table("book").get();
    console.log(JSON.stringify(records));
    for (let record of records){
      let data:Record<string, any> = record.data();
      let newBook:Record<string, any> = {}
      newBook["name"] = data["name"];
      newBook["price"] = data["price"];
      newBook["count"] = data["count"];
      newBook["image"] = data["image"];
      newBook["isbn"] = data["isbn"];
      this.books.push(newBook);
      console.log(JSON.stringify(newBook));
    }
  }
  async onPageShow(){
    console.log("book list appear");
    let db: MBaaS.DatabaseHelper = globalThis.db;
    this.books = [];
    let records = await db.table("book").get();
    console.log(JSON.stringify(records));
    for (let record of records){
      let data:Record<string, any> = record.data();
      let newBook:Record<string, any> = {}
      newBook["name"] = data["name"];
      newBook["price"] = data["price"];
      newBook["count"] = data["count"];
      newBook["image"] = data["image"];
      newBook["isbn"] = data["isbn"];
      this.books.push(newBook);
      console.log(JSON.stringify(newBook));
    }

    console.log('begin subscribe')
    this.unSubscribe = db.table("book").subscribe(
      (data) => {
        console.log("call back")
        // this.books = [];
        console.log(JSON.stringify(data))
        if (data.messageType == MBaaS.SYN_CONTENT){

          let contentData = data as MBaaS.contentData;
          let data_1 = contentData.updateData.pop();
          console.log(JSON.stringify(data_1))

          let target = 0;
          let newBook:Record<string, any> = {};
          for (const [index, book] of this.books.entries()){
            // console.log(book["isbn"])
            if (book["isbn"] == data_1.newItem["isbn"]){
              // console.log("hhhhhhhhhhhh")
              target = index;
              newBook = book;
              break;
            }
          }
          for (const key in data_1.newItem){
            console.log(key)
            console.log(data_1.newItem[key])
            newBook[key] = data_1.newItem[key];
          }
          console.log("update " + target.toString())
          let newBooks = this.books;
          newBooks[target] = newBook;
          this.books = []
          this.books = newBooks;
        }
        else if (data.messageType == MBaaS.SYN_FIELD){
          let fieldData = data as MBaaS.fieldData;
        }
      }
    )

  }
  onPageHide(){
    this.unSubscribe()
  }

  build() {
    Column() {
      Row(){

        Text("退出")
          .layoutWeight(2)
          .fontColor(Color.Red)
          .fontSize(15)
          .margin({left:25})
          .onClick(()=>{
            router.replaceUrl({ url: "pages/Login" })
          }).height("100%")

        Text(globalThis.userName)
          .fontSize(25)
          .height("100%")
          .layoutWeight(4)
          .textAlign(TextAlign.Center)
          .fontWeight(FontWeight.Bold)
          // .backgroundColor(0xD2B48C);
        Text("管理模式")
          .layoutWeight(2)
          .textAlign(TextAlign.Center)
          .fontColor(Color.Black)
          .fontSize(15)
          .onClick(()=>{
            router.pushUrl({url:"pages/OrderList"})
          }).height("100%").margin({right:10})
      }
      .width("100%").height(47)
      // .backgroundColor(0xF5DEB3);


      // 表头
      Row(){
        Text("封面")
          .fontSize(14)
          .fontColor("#989a9c")
          .width("25%")
          .textAlign(TextAlign.Center)
          // .backgroundColor(0xF5DEB3);

        Text("书名")
          .fontSize(14)
          .fontColor("#989a9c")
          .width("25%")
          .textAlign(TextAlign.Center)
          // .backgroundColor(0xD2B48C);

        Text("价格")
          .fontSize(14)
          .fontColor("#989a9c")
          .width("15%")
          .textAlign(TextAlign.Center)
          // .backgroundColor(0xF5DEB3);

        Text("库存")
          .fontSize(14)
          .fontColor("#989a9c")
          .width("15%")
          .textAlign(TextAlign.Center)
          // .backgroundColor(0xD2B48C);

        Text("购买")
          .fontSize(14)
          .fontColor("#989a9c")
          .width("20%")
          .textAlign(TextAlign.Center)
          // .backgroundColor(0xF5DEB3);
      }

      Column(){
        List() {
          ForEach(this.books, (item) => {
            ListItem(){
              BookCard({
                imageUrl:item.image,
                name: item.name,
                price: item.price,
                count: item.count,
                isbn: item.isbn,
              });
            }
          })
        }
      }

      // this.BookList();


      // Row() {
      //   Column(){
      //     Image("http://img3m4.ddimg.cn/62/14/27883214-1_w_2.jpg")
      //       .alignSelf(ItemAlign.Center)
      //   }.width("25%").height("100%")
      //   .borderStyle(BorderStyle.Solid)
      //   .borderColor('#ffe3e3e3')
      //   .borderWidth('0.50vp')
      //
      //   Column(){
      //     Text("高考英语 五年高考三年模拟")
      //       .fontSize(12)
      //       .textAlign(TextAlign.Center)
      //       // .align(VerticalAlign.Center)
      //   }.width("25%").height("100%").justifyContent(FlexAlign.Center)
      //   .borderStyle(BorderStyle.Solid)
      //   .borderColor('#ffe3e3e3')
      //   .borderWidth('0.50vp')
      //
      //   Column(){
      //     Text("20.5")
      //       .fontSize(12)
      //       .textAlign(TextAlign.Center)
      //   }.width("15%").height("100%").justifyContent(FlexAlign.Center)
      //   .borderStyle(BorderStyle.Solid)
      //   .borderColor('#ffe3e3e3')
      //   .borderWidth('0.50vp')
      //
      //   Column(){
      //     Text("18")
      //       .fontSize(12)
      //       .textAlign(TextAlign.Center)
      //   }.width("15%").height("100%").justifyContent(FlexAlign.Center)
      //   .borderStyle(BorderStyle.Solid)
      //   .borderColor('#ffe3e3e3')
      //   .borderWidth('0.50vp')
      //
      //   Column(){
      //     Button("购买")
      //       .height(20)
      //       .width("80%")
      //   }.width("20%").height("100%").justifyContent(FlexAlign.Center)
      //   .borderStyle(BorderStyle.Solid)
      //   .borderColor('#ffe3e3e3')
      //   .borderWidth('0.50vp')
      //
      //
      //
      //
      //
      // }.width("100%").height(100).margin({top:10})


    }.width("100%").margin({top:10})

  }

  // @Builder BookList(){
  //   Column(){
  //     List() {
  //       ForEach(this.books, (item) => {
  //         ListItem(){
  //           BookCard({
  //             imageUrl:item.image,
  //             name: item.name,
  //             price: item.price,
  //             count: item.count,
  //             isbn: item.isbn,
  //           });
  //         }
  //       })
  //     }
  //   }
  //
  // }

}