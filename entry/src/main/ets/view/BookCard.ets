import {MBaaS} from '../package/MBaaS'
@CustomDialog
struct BuyCustomDialog {
  controller: CustomDialogController
  cancel: () => void;
  confirm: () => void;
  fresh: ()=>void;
  imageUrl: string;
  name: string;
  isbn: number;
  price: number;

  build() {
    Column() {
      Text('确定购买 ' + this.name + ' 吗?').width('70%')
        .fontSize(15).margin({ top: 10, bottom: 10 })
        .textAlign(TextAlign.Center)
      Image(this.imageUrl).width(80).height(80)  //自定义弹窗图片
      //      Image($r('app.media.icon')).width(80).height(80)
      // Text('确定要卸载 ' +  ' 吗?').fontSize(16).margin({ bottom: 10 })
      Flex({ justifyContent: FlexAlign.SpaceAround }) {
        Button('取消')
          .onClick(() => {
            this.controller.close()
            this.cancel()
          }).backgroundColor(0xffffff).fontColor(Color.Black)
        Button('确认')
          .onClick(() => {
            this.controller.close()
            this.confirm()
          }).backgroundColor(0xffffff).fontColor(Color.Red)
      }.margin({ bottom: 10 })
    }
  }
}

@Component
export struct BookCard{
  @State imageUrl: string = "";
  @State count: number = 0;
  @State price: number = 0;
  @State name: string = "";
  @State isbn: number = 0;

  dialogController: CustomDialogController = new CustomDialogController({
    builder: BuyCustomDialog({
      cancel: this.onCancel,
      confirm: this.onAccept,
      imageUrl: this.imageUrl,
      name: this.name,
      isbn: this.isbn,
      price: this.price,
    }),
    cancel: this.existApp,
    autoCancel: true,       //点击空白处退出弹窗 false不退出，true退出
    alignment: DialogAlignment.Center  //垂直底部对齐
    // offset:{dx:0,dy:-30}                 //需要注意的是弹窗从底部往上移取负值
  })



  onCancel() {
    console.info('点击取消按钮时的回调')
  }
  onAccept() {
    // to be fix  发送订单
    console.info('单击确认按钮时的回调')

    let db: MBaaS.DatabaseHelper = globalThis.db;
    if (this.price != null){
      console.log(this.price.toString())
    }
    else{
      console.log("price is null")
    }
    db.runTransaction( async(transaction) => {
      let records = await transaction.query("book", this.isbn).get();
      let oldCount = records.pop().data()["count"];

      if (oldCount <= 0){
        console.log("count not enough")
        return "count not enough"
      }
      else{
        let newCount = oldCount - 1;
        transaction.update("book", this.isbn, {
          "count": newCount
        })
        let currentDate = new Date();
        let words = currentDate.toDateString().split(" ").slice(1, 4);
        let date = words.join("-")
        words = currentDate.toTimeString().split(" ");
        words.pop();
        let time = words.join("-");

        transaction.add("orders", {
          "username": globalThis.userName,
          "totalprice": this.price,
          "orderid": date + "-" + time
        });

        transaction.add("items", {
          "orderid": date + "-" + time,
          "bookname": this.name,
          "number": 1,
          "singleprice": this.price
        });
      }
    }).then((res)=>{
      console.log("after transaction catch")
      if (res == "Transaction successfully committed!"){
        AlertDialog.show({
          // 提示信息
          message: "购买成功",
          // 弹窗显示位置
          alignment: DialogAlignment.Center,
        });
      }
      else if (res == "count not enough"){
        AlertDialog.show({
          // 提示信息
          message: "购买失败，数量不足",
          // 弹窗显示位置
          alignment: DialogAlignment.Center,
        });
      }
      else{
        AlertDialog.show({
          // 提示信息
          message: "购买失败，请稍后重试",
          // 弹窗显示位置
          alignment: DialogAlignment.Center,
        });
      }

    }).catch((err)=>{
      console.log(err);
    });

  }
  existApp() {
    console.info('点击空白处的回调')
  }

  build(){
    Row() {
      Column(){
        Image(this.imageUrl)
          .alignSelf(ItemAlign.Center)
      }.width("25%").height("100%")
      .borderStyle(BorderStyle.Solid)
      .borderColor('#ffe3e3e3')
      .borderWidth('0.50vp')

      Column(){
        Text(this.name)
          .fontSize(12)
          .textAlign(TextAlign.Center)
        // .align(VerticalAlign.Center)
      }.width("25%").height("100%").justifyContent(FlexAlign.Center)
      .borderStyle(BorderStyle.Solid)
      .borderColor('#ffe3e3e3')
      .borderWidth('0.50vp')

      Column(){
        Text(this.price.toString())
          .fontSize(12)
          .textAlign(TextAlign.Center)
      }.width("15%").height("100%").justifyContent(FlexAlign.Center)
      .borderStyle(BorderStyle.Solid)
      .borderColor('#ffe3e3e3')
      .borderWidth('0.50vp')

      Column(){
        Text(this.count.toString())
          .fontSize(12)
          .textAlign(TextAlign.Center)
      }.width("15%").height("100%").justifyContent(FlexAlign.Center)
      .borderStyle(BorderStyle.Solid)
      .borderColor('#ffe3e3e3')
      .borderWidth('0.50vp')

      Column(){
        Button("购买")
          .height(20)
          .width("80%")
          .onClick(()=>{
            this.dialogController.open();
            // to be fix 增加事务
          })
      }.width("20%").height("100%").justifyContent(FlexAlign.Center)
      .borderStyle(BorderStyle.Solid)
      .borderColor('#ffe3e3e3')
      .borderWidth('0.50vp')





    }.width("100%").height(100).margin({top:10})
  }
}