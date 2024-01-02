package com.tiger.baas.Service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint(value = "/websocket/{userId}")
@Component
public class WebSocketService {
    private static final Logger log = LoggerFactory.getLogger(WebSocketService.class);

    /**
     * concurrent包的线程安全Set，用来存放每个客户端对应的WebSocketServer对象。
     */
    private static ConcurrentHashMap<String, WebSocketClient> webSocketMap = new ConcurrentHashMap<>();

    public WebSocketService() {
        //每当有一个连接，都会执行一次构造方法
        System.out.println("新的连接。。。");
    }

    /**与某个客户端的连接会话，需要通过它来给客户端发送数据*/
    private Session session;
    private String userId="";
    /**
     * 连接建立成功调用的方法
     * */
    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userId) {
        this.session = session;
        this.userId= userId;
        WebSocketClient client = new WebSocketClient();
        client.setSession(session);
        client.setUri(session.getRequestURI().toString());
        webSocketMap.put(userId, client);
        log.info("连接:"+userId+",当前使用人数为:" + webSocketMap.size());
//        System.out.println(this);
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose() {
        if(webSocketMap.containsKey(userId)){
            webSocketMap.remove(userId);
        }
//        log.info(userId+"测试结束,当前在测数量为:" + webSocketMap.size());
    }

    /**
     * 收到客户端消息后调用的方法
     * @param message 客户端发送过来的消息
     * */
    @OnMessage
    public void onMessage(String message, Session session) {

        log.info("收到用户消息:"+userId+",报文:"+message);
        log.info("userId: " + getUserId());
        sendMessage(getUserId(), message);
//        sendMessage(getUserId(), "Done");
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.error("用户错误:"+this.userId+",原因:"+error.getMessage());
        error.printStackTrace();
    }

    /**
     * 连接服务器成功后主动推送
     */
    public void sendMessage(String message) throws IOException {
        System.out.println("【websocket消息】广播消息:"+message);
        webSocketMap.forEach((key,value) -> {
            try {
                value.getSession().getBasicRemote().sendText(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * 向指定客户端发送消息（字符串形式）
     * @param userId
     * @param message
     */
    public static void sendMessage(String userId,String message){
        try {
            WebSocketClient webSocketClient = webSocketMap.get(userId);
            if(webSocketClient!=null){
                webSocketClient.getSession().getBasicRemote().sendText(message);
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }


}