//package com.example.common.config;
//
//import com.example.entity.Orders;
//import com.example.entity.OrdersDTO;
//import org.springframework.stereotype.Component;
//import org.springframework.web.socket.CloseStatus;
//import org.springframework.web.socket.TextMessage;
//import org.springframework.web.socket.WebSocketSession;
//import org.springframework.web.socket.handler.TextWebSocketHandler;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import java.io.IOException;
//import java.io.IOException;
//import java.util.concurrent.CopyOnWriteArraySet;
//
//@Component
//public class OrderWebSocketHandler extends TextWebSocketHandler {
//
//    // 使用 CopyOnWriteArraySet 来存储会话
//    private static final CopyOnWriteArraySet<OrderWebSocketHandler> sessions = new CopyOnWriteArraySet<>();
//
//    private WebSocketSession session;
//
//    // 建立连接时，将会话添加到活动会话集合中
//    @Override
//    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
//        this.session = session;
//        sessions.add(this);  // 将当前会话添加到集合
//        System.out.println("New session added: " + session.getId());
//        System.out.println("Active Sessions size: " + sessions.size());  // 打印会话数量
//    }
//
//    // 连接关闭时，从活动会话集合中移除该会话
//    @Override
//    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
//        sessions.remove(this);  // 从集合中移除当前会话
//        System.out.println("Session removed: " + session.getId());
//        System.out.println("Active Sessions size after removal: " + sessions.size());  // 打印移除后的会话数量
//    }
//
//    // 处理接收到的消息
//    @Override
//    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
//        // 这里只是发送一个简单的消息
//        session.sendMessage(new TextMessage("You have a new order!"));
//    }
//
//    // 向所有活跃的 WebSocket 会话发送消息
//
//
//public static void sendOrderAlertToBusiness(OrdersDTO ordersDTO) {
//    System.out.println("Active Sessions: " + sessions.size());
//    ObjectMapper objectMapper = new ObjectMapper(); // 用于将对象转换为 JSON
//
//    for (OrderWebSocketHandler handler : sessions) {
//        try {
//            WebSocketSession clientSession = handler.session;
//            if (clientSession != null && clientSession.isOpen()) {
//                // 将订单对象转换为 JSON 字符串
//                String message = objectMapper.writeValueAsString(ordersDTO);
//                clientSession.sendMessage(new TextMessage(message));
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//}
//
//}

package com.example.common.config;

import com.example.entity.Orders;
import com.example.entity.OrdersDTO;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;

import java.util.concurrent.CopyOnWriteArraySet;

@Component
public class OrderWebSocketHandler extends TextWebSocketHandler {

    // 使用 CopyOnWriteArraySet 来存储会话
    private static final CopyOnWriteArraySet<WebSocketSession> sessions = new CopyOnWriteArraySet<>();

    private WebSocketSession session;

    // 建立连接时，将会话添加到活动会话集合中
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        this.session = session;
        sessions.add(session);  // 将当前会话添加到集合
        System.out.println("New session added: " + session.getId());
        System.out.println("Active Sessions size: " + sessions.size());  // 打印会话数量
    }

    // 连接关闭时，从活动会话集合中移除该会话
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(this);  // 从集合中移除当前会话
        System.out.println("Session removed: " + session.getId());
        System.out.println("Active Sessions size after removal: " + sessions.size());  // 打印移除后的会话数量
    }

    // 处理接收到的消息
    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // 这里只是发送一个简单的消息
        session.sendMessage(new TextMessage("You have a new order!"));
    }

    // 向所有活跃的 WebSocket 会话发送消息


public static void sendOrderAlertToBusiness(OrdersDTO ordersDTO) {
    System.out.println("Active Sessions: " + sessions.size());
    ObjectMapper objectMapper = new ObjectMapper(); // 用于将对象转换为 JSON

    for (WebSocketSession handler : sessions) {
        try {
            WebSocketSession clientSession = handler;
            if (clientSession != null && clientSession.isOpen()) {
                // 将订单对象转换为 JSON 字符串
                String message = objectMapper.writeValueAsString(ordersDTO);
                clientSession.sendMessage(new TextMessage(message));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

}
