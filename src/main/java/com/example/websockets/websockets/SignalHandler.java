package com.example.websockets.websockets;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SignalHandler extends TextWebSocketHandler {
    private Map<String, UserSession> users = new ConcurrentHashMap<>();

    private ObjectMapper mapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {

        System.out.println("Connected: " + session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {

        JsonNode json = mapper.readTree(message.getPayload());

        String type = json.get("type").asText();

        switch(type) {

            case "login":
                handleLogin(session, json);
                break;

            case "offer":
            case "answer":
            case "candidate":
                relayMessage(session, json);
                break;
        }
    }

    private void handleLogin(WebSocketSession session, JsonNode json) throws Exception {

        String username = json.get("username").asText();

        users.put(username, new UserSession(username, session));

        broadcastUserList();
    }

    private void relayMessage(WebSocketSession sender, JsonNode json) throws Exception {

        String target = json.get("target").asText();

        // Find sender username
        String senderUsername = null;

        for (Map.Entry<String, UserSession> entry : users.entrySet()) {

            if (entry.getValue().getSession().equals(sender)) {

                senderUsername = entry.getKey();
                break;
            }
        }

        UserSession targetUser = users.get(target);

        if (targetUser != null) {

            ObjectNode relay = mapper.createObjectNode();

            relay.put("type", json.get("type").asText());

            relay.put("username", senderUsername);

            if (json.has("offer"))
                relay.set("offer", json.get("offer"));

            if (json.has("answer"))
                relay.set("answer", json.get("answer"));

            if (json.has("candidate"))
                relay.set("candidate", json.get("candidate"));

            targetUser.getSession()
                    .sendMessage(new TextMessage(relay.toString()));
        }
    }

    private void broadcastUserList() throws Exception {

        List<String> usernames = new ArrayList<>(users.keySet());

        ObjectNode msg = mapper.createObjectNode();

        msg.put("type", "user-list");
        msg.putPOJO("users", usernames);

        for(UserSession user : users.values()) {

            user.getSession().sendMessage(
                    new TextMessage(msg.toString())
            );
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {

        users.values().removeIf(user -> user.getSession().equals(session));
    }
}
