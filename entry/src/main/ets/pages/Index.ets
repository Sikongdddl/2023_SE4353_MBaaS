
@Entry
@Component
struct Index {
  build() {
    Column() {
      Text("BookStore")
        .fontSize(20)
        .margin({top:10})
        .width("100%").height(47)
        .textAlign(TextAlign.Center)

      Row(){
        Text("封面")
          .fontSize(14)
          .fontColor("#989a9c")
          .width("30%");

        Text("书名")
          .fontSize(14)
          .fontColor("#989a9c")
          .width("30%");

        Text("价格")
          .fontSize(14)
          .fontColor("#989a9c")
          .width("20%");

        Text("库存")
          .fontSize(14)
          .fontColor("#989a9c")
          .width("20%");
      }.padding(15).width("90%")
    }
    // Row() {
    //   Column() {
    //     Text(this.message)
    //       .fontSize(50)
    //       .fontWeight(FontWeight.Bold)
    //   }
    //   .width('100%')
    // }
    // .height('100%')
  }


}