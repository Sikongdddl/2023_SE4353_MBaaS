@Component
export struct OrderItemCard{
  name: string;
  singlePrice: number;
  number: number;
  build(){
    Row(){
      Column(){
        Text(this.name)
          .fontSize(15)
          .textAlign(TextAlign.Center)
      }.height("100%")
      .justifyContent(FlexAlign.Center)
      .layoutWeight(5)
      .borderStyle(BorderStyle.Solid)
      .borderColor('#ffe3e3e3')
      .borderWidth('0.50vp')

      Column(){
        Text(this.singlePrice.toString())
          .fontSize(15)
          .textAlign(TextAlign.Center);
      }.height("100%")
      .justifyContent(FlexAlign.Center)
      .layoutWeight(3)
      .borderStyle(BorderStyle.Solid)
      .borderColor('#ffe3e3e3')
      .borderWidth('0.50vp')

      Column(){
        Text(this.number.toString())
          .fontSize(15)
          .textAlign(TextAlign.Center);
      }.height("100%")
      .justifyContent(FlexAlign.Center)
      .layoutWeight(3)
      .borderStyle(BorderStyle.Solid)
      .borderColor('#ffe3e3e3')
      .borderWidth('0.50vp')
    }.height(70)
  }
}