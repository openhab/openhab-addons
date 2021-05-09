package org.openhab.binding.vthing;

import java.io.IOException;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.HttpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Juergen Weber - Initial contribution
 */

@Component(name = "vlamp-websocket", immediate = true)
@WebSocket
public class WebSocketHandler {

    private final Logger logger = LoggerFactory.getLogger(WebSocketHandler.class);

    @Reference
    private HttpService httpService;

    @OnWebSocketConnect
    public void onOpen(Session session) {
        logger.trace("onOpen: {}", session);
        session.setIdleTimeout(-1);
        VLampHandlerFactory.onOpenWebsocket(session);
    }

    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) {
        logger.trace("onClose {}", session);

        VLampHandlerFactory.onCloseWebsocket(session);
    }

    @OnWebSocketMessage
    public void onTextMessage(Session session, String text) throws IOException {
        logger.debug("onTextMessage: {}", text);

        String[] s = text.split("=");

        if ("thingUID".equals(s[0])) {
            VLampHandlerFactory.onThingNamed(session, s[1]);
        }
    }
}
