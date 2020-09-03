package org.openhab.binding.yioremote.internal;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.openhab.binding.yioremote.internal.YIOremoteBindingConstants.YIOREMOTEMESSAGETYPE;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@WebSocket
public class YIOremoteWebsocket {

    private Session session;
    private @Nullable String string_receivedmessage;
    private final Logger logger = LoggerFactory.getLogger(YIOremoteHandler.class);
    private @Nullable JsonObject JsonObject_recievedJsonObject;
    private boolean boolean_authentication_required = false;
    private boolean boolean_heartbeat = false;
    private boolean boolean_authentication_ok = false;
    private boolean boolean_sendir_status = false;
    private String received_ircode = "";

    CountDownLatch latch = new CountDownLatch(1);

    @OnWebSocketMessage
    public void onText(Session session, String message) throws IOException {
        logger.debug("Message received from server:" + message);
        string_receivedmessage = message;
        JsonObject_recievedJsonObject = convert_StringtoJsonObject(message);
        if (decode_receivedMessage(JsonObject_recievedJsonObject)) {
            logger.debug("Message decoded");
        } else {
            logger.debug("Error during message decoding");
        }
    }

    public String get_string_receivedmessage() {
        return this.string_receivedmessage;
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        this.session = session;
        latch.countDown();
    }

    @OnWebSocketError
    public void onError(Throwable cause) {
        try {
            /* this.handleTransportError(this.session, cause); */
            logger.debug(cause.toString());
        } catch (Throwable ex) {
            logger.debug(cause.toString());
            /* ExceptionWebSocketHandlerDecorator.tryCloseWithError(this.session, ex, logger); */
        }
    }

    public void sendMessage(String str) {
        try {
            session.getRemote().sendString(str);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void sendMessage(YIOREMOTEMESSAGETYPE messagetype, String messagepyload) {
        try {
            if (messagetype.equals(YIOREMOTEMESSAGETYPE.AUTHENTICATE)) {
                session.getRemote().sendString("{\"type\":\"auth\", \"token\":\"" + messagepyload + "\"}");
            } else if (messagetype.equals(YIOREMOTEMESSAGETYPE.HEARTBEAT)) {
                session.getRemote().sendString(
                        "{\"type\":\"dock\", \"command\":\"ir_send\",\"code\":\"0;0x0;0;0\", \"format\":\"hex\"}");
            } else if (messagetype.equals(YIOREMOTEMESSAGETYPE.IRRECEIVERON)) {
                session.getRemote().sendString("{\"type\":\"dock\", \"command\":\"ir_receive_on\"}");
            } else if (messagetype.equals(YIOREMOTEMESSAGETYPE.IRRECEIVEROFF)) {
                session.getRemote().sendString("{\"type\":\"dock\", \"command\":\"ir_receive_off\"}");
            } else if (messagetype.equals(YIOREMOTEMESSAGETYPE.IRSEND)) {
                session.getRemote().sendString("{\"type\":\"dock\", \"command\":\"ir_send\",\"code\":\"" + messagepyload
                        + "\", \"format\":\"hex\"}");

            }
        } catch (

        IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private boolean decode_receivedMessage(JsonObject JsonObject_recievedJsonObject) {
        boolean boolean_result = false;

        if (JsonObject_recievedJsonObject.has("type")) {
            logger.debug("json string has type member");
            if (JsonObject_recievedJsonObject.get("type").toString().equalsIgnoreCase("\"auth_required\"")) {
                logger.debug("auth required message");
                boolean_authentication_required = true;
                boolean_heartbeat = true;
                boolean_result = true;

            } else if (JsonObject_recievedJsonObject.get("type").toString().equalsIgnoreCase("\"auth_ok\"")) {
                logger.debug("auth ok message");
                boolean_authentication_required = false;
                boolean_authentication_ok = true;
                boolean_heartbeat = true;
                boolean_result = true;
            } else if (JsonObject_recievedJsonObject.get("type").toString().equalsIgnoreCase("\"dock\"")
                    && JsonObject_recievedJsonObject.has("message")) {
                logger.debug("dock message");
                if (JsonObject_recievedJsonObject.get("message").toString().equalsIgnoreCase("\"ir_send\"")) {
                    logger.debug("ir send message");
                    if (JsonObject_recievedJsonObject.get("success").toString().equalsIgnoreCase("true")) {
                        logger.debug("ir send message true");
                        boolean_sendir_status = true;
                        boolean_heartbeat = true;
                        boolean_result = true;
                    } else {
                        logger.debug("ir send message failed");
                        boolean_sendir_status = true;
                        boolean_heartbeat = true;
                        boolean_result = true;
                    }
                } else {
                    logger.debug("No known message1");
                    boolean_heartbeat = false;
                    boolean_result = false;
                }
            } else if (JsonObject_recievedJsonObject.get("command").toString().equalsIgnoreCase("\"ir_receive\"")) {
                received_ircode = JsonObject_recievedJsonObject.get("code").toString().replace("\"", "");

                if (received_ircode.matches("[0-9][;]0[xX][0-9a-fA-F]+[;][0-9]+[;][0-9]")) {
                    received_ircode = JsonObject_recievedJsonObject.get("code").toString().replace("\"", "");
                } else {
                    received_ircode = "";
                }
                logger.debug("ir_receive message" + received_ircode);
                boolean_heartbeat = true;
                boolean_result = true;
            } else {
                logger.debug("No known message2");
                boolean_heartbeat = false;
                boolean_result = false;
            }

        } else {
            logger.debug("No known message1");
            boolean_heartbeat = false;
            boolean_result = false;
        }
        return boolean_result;
    }

    private JsonObject convert_StringtoJsonObject(String jsonString) {
        logger.debug("StringtoJsonElement function called");
        JsonParser parser = new JsonParser();
        JsonElement jsonElement = parser.parse(jsonString);

        JsonObject result = null;
        if (jsonElement instanceof JsonObject) {
            result = jsonElement.getAsJsonObject();
        } else {
            logger.debug(jsonString + " is not valid JSON stirng");
            throw new IllegalArgumentException(jsonString + " is not valid JSON stirng");
        }
        return result;
    }

    public CountDownLatch getLatch() {
        return latch;
    }

    public boolean get_boolean_heartbeat() {
        boolean boolean_result = false;
        boolean_result = boolean_heartbeat;
        boolean_heartbeat = false;
        return boolean_result;
    }

    public boolean get_boolean_authentication_required() {
        boolean boolean_result = false;
        boolean_result = boolean_authentication_required;
        boolean_authentication_required = false;
        return boolean_result;
    }

    public String get_string_received_ircode() {
        String string_result = "";
        string_result = received_ircode;
        received_ircode = "";
        return string_result;
    }

    public boolean get_boolean_authentication_ok() {
        boolean boolean_result = false;
        boolean_result = boolean_authentication_ok;
        boolean_authentication_ok = false;
        return boolean_result;
    }

    public boolean get_boolean_sendir_status() {
        boolean boolean_result = false;
        boolean_result = boolean_sendir_status;
        boolean_sendir_status = false;
        return boolean_result;
    }

}
