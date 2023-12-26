@CustomDialog
struct BuyCustomDialog {
  controller: CustomDialogController
  cancel: () => void
  confirm: () => void
  imageUrl: string;
  name: string;

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
  imageUrl: string = "";
  count: number;
  price: number;
  name: string = "";

  dialogController: CustomDialogController = new CustomDialogController({
    builder: BuyCustomDialog({
      cancel: this.onCancel,
      confirm: this.onAccept,
      imageUrl: this.imageUrl,
      name: this.name
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