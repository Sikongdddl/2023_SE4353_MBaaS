import {OrderItemCard} from './OrderItemCard'
@Component
export struct OrderCard{
  orderItems:Record<string, any>[] = [];
  userName: string;
  totalPrice: number;
  orderNumber: string;
  build(){
    Row(){
      Column(){
        Text(this.userName)
          .fontSize(15)
          .textAlign(TextAlign.Center);
      }.width("15%").height("100%")
      .justifyContent(FlexAlign.Center)
      .borderStyle(BorderStyle.Solid)
      .borderColor('#ffe3e3e3')
      .borderWidth('0.50vp')

      Column(){
        Text(this.orderNumber)
          .fontSize(15)
          .textAlign(TextAlign.Center);
      }.width("15%").height("100%")
      .justifyContent(FlexAlign.Center)
      .borderStyle(BorderStyle.Solid)
      .borderColor('#ffe3e3e3')
      .borderWidth('0.50vp')

      Column(){
        List(){
          ForEach(this.orderItems, (item) => {
            ListItem(){
              OrderItemCard({
                name: item.name,
                singlePrice: item.singlePrice,
                number: item.number
              });
            }
          })
        }
      }.width("55%")
      Column(){
        Text(this.totalPrice.toString())
          .fontSize(15)
          .textAlign(TextAlign.Center);
      }.width("15%").height("100%")
      .justifyContent(FlexAlign.Center)
      .borderStyle(BorderStyle.Solid)
      .borderColor('#ffe3e3e3')
      .borderWidth('0.50vp')
    }.height(this.orderItems.length * 70)
  }
}