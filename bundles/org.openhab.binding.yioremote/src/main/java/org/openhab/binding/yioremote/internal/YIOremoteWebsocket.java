package org.openhab.binding.yioremote.internal;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebSocket
public class YIOremoteWebsocket {

    private Session session;
    private @Nullable String string_receivedmessage;
    private final Logger logger = LoggerFactory.getLogger(YIOremoteHandler.class);

    CountDownLatch latch = new CountDownLatch(1);

    @OnWebSocketMessage
    public void onText(Session session, String message) throws IOException {
        logger.debug("Message received from server:" + message);
        string_receivedmessage = message;
        System.out.println("Message received from server:" + message + string_receivedmessage);
    }

    public String get_string_receivedmessage() {
        return this.string_receivedmessage;
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        System.out.println("Connected to server");
        this.session = session;
        latch.countDown();
    }

    public void sendMessage(String str) {
        try {
            session.getRemote().sendString(str);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public CountDownLatch getLatch() {
        return latch;
    }

}
