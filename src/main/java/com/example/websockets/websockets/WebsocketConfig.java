package com.example.websockets.websockets;


import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebsocketConfig implements WebSocketConfigurer {

    public final SignalHandler signalHandler;

    public WebsocketConfig(SignalHandler signalHandler) {
        this.signalHandler = signalHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
     registry.addHandler(signalHandler,"/signal")
             .setAllowedOrigins("*");
    }
}
