import router from '@ohos.router';
import {MBaaS} from '../package/MBaaS'
@Entry
@Component
struct Register {
  userName = "";
  password = "";
  checkPassword = ""
  @State vis:Visibility = Visibility.Hidden;

  build() {
    RelativeContainer(){
      Image($r('app.media.ic_public_back'))
        .id("image_back")
        .alignRules({
          top: { anchor: '__container__', align: VerticalAlign.Top },
          left: { anchor: '__container__', align: HorizontalAlign.Start }
        })
        .margin({left:20, top:20})
        .height(28)
        .onClick(()=>{
          router.back();
        })
      Text("注册")
        .alignRules({
          top: { anchor: '__container__', align: VerticalAlign.Top },
          middle: { anchor: '__container__', align: HorizontalAlign.Center }
        })
        .id("text_register")
        .fontSize(40)
        .margin({ "top":"25%" })

      TextInput({placeholder:'请输入用户名'})
        .alignRules({
          top: { anchor: 'text_register', align: VerticalAlign.Bottom },
          middle: { anchor: '__container__', align: HorizontalAlign.Center }
        })
        .id("input_userName")
        .type(InputType.Normal)
        .width("70%").height(40)
        .margin({"top": 80})
        .onChange((value)=>{
          this.userName = value;
        })

      TextInput({placeholder:'请输入密码'})
        .alignRules({
          top: { anchor: 'input_userName', align: VerticalAlign.Bottom },
          middle: { anchor: '__container__', align: HorizontalAlign.Center }
        })
        .id("input_password")
        .type(InputType.Password)
        .width("70%").height(40)
        .margin({"top": 20})
        .onChange((value)=>{
          this.password = value;
          if (this.checkPassword != this.password){
            this.vis = Visibility.Visible;
          }
          else{
            this.vis = Visibility.Hidden;
          }
        })

      TextInput({placeholder:'请确认密码'})
        .alignRules({
          top: { anchor: 'input_password', align: VerticalAlign.Bottom },
          middle: { anchor: '__container__', align: HorizontalAlign.Center }
        })
        .id("check_password")
        .type(InputType.Password)
        .width("70%").height(40)
        .margin({"top": 20})
        .onChange((value)=>{
          this.checkPassword = value;
          if (this.checkPassword != this.password){
            this.vis = Visibility.Visible;
          }
          else{
            this.vis = Visibility.Hidden;
          }
        })
      Text("两次输入的密码不相同")
        .alignRules({
          left: { anchor: 'check_password', align: HorizontalAlign.Start },
          top: { anchor: 'check_password', align: VerticalAlign.Bottom },
        })
        .id("text_remind")
        .fontSize(12)
        .fontColor(Color.Red)
        .margin({top:5, left:20})
        .visibility(this.vis)

      Button("注册")
        .alignRules({
          top: { anchor: 'check_password', align: VerticalAlign.Bottom },
          middle: { anchor: '__container__', align: HorizontalAlign.Center }
        })
        .id("button_register")
        .width("70%").height(40)
        .margin({"top": 80})
        .onClick( async ()=>{
          if (this.password != this.checkPassword){
            AlertDialog.show({
              // 提示信息
              message: "两次输入的密码不相同",
              // 弹窗显示位置
              alignment: DialogAlignment.Center,
            });
          }
          else{
            let db:MBaaS.DatabaseHelper = globalThis.db;
            let records = await db.table("user").whereEqualTo("username", this.userName).get();
            if (records.length != 0){
              AlertDialog.show({
                // 提示信息
                message: "用户名已存在",
                // 弹窗显示位置
                alignment: DialogAlignment.Center,
              });
            }
            else{
              db.table("user").add({
                "username": this.userName,
                "password": this.password
              }).then((res)=>{
                if (res == "Add a new record successfully!"){
                  AlertDialog.show({
                    // 提示信息
                    message: "注册成功",
                    // 弹窗显示位置
                    alignment: DialogAlignment.Center,
                    primaryButton: {
                      value: "确定",
                      action: () => {
                        router.back();
                      }
                    },
                  });
                }
                else{
                  AlertDialog.show({
                    // 提示信息
                    message: "注册失败，请稍后重试",
                    // 弹窗显示位置
                    alignment: DialogAlignment.Center,

                  });
                }
              })
            }
          }

        })

    }
    .width("100%").height("100%")

  }


}