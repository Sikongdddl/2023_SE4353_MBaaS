package com.tiger.baas.Service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/**
 * 开启 WebSocket：使用 springboot 内嵌的tomcat容器启动 websocket
 **/
@Slf4j
@Configuration
public class WebSocketConfig {

    /**
     * 服务器节点
     * @return
     */
//    @Bean
//    public ServerEndpointExporter serverEndpointExporter() {
//        log.info("启动 WebSocket ...");
//        return new ServerEndpointExporter();
//    }

}
