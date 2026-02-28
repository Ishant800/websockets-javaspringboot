package com.example.websockets.websockets;

import org.springframework.web.socket.WebSocketSession;

public class UserSession {

    private String username;
    private WebSocketSession session;


    public UserSession(String username,WebSocketSession session){
        this.username = username;
        this.session = session;
    }

    public String getUsername(){
        return username;
    }
    public WebSocketSession getSession(){
        return session;
    }
}
