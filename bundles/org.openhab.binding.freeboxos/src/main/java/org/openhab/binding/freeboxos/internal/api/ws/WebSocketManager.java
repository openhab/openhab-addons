package org.openhab.binding.freeboxos.internal.api.ws;

import static org.openhab.binding.freeboxos.internal.api.ApiConstants.AUTH_HEADER;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.StatusCode;
import org.eclipse.jetty.websocket.api.WebSocketListener;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.openhab.binding.freeboxos.internal.api.ApiConstants.Permission;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.lan.browser.LanHost;
import org.openhab.binding.freeboxos.internal.handler.HostHandler;
import org.openhab.binding.freeboxos.internal.rest.FreeboxOsSession;
import org.openhab.binding.freeboxos.internal.rest.RestManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;

@NonNullByDefault
public class WebSocketManager extends RestManager implements WebSocketListener {
    private static final String WS_PATH = "ws/event";
    private static final String HOST_UNREACHABLE = "lan_host_l3addr_unreachable";
    private static final String HOST_REACHABLE = "lan_host_l3addr_reachable";

    private final Logger logger = LoggerFactory.getLogger(WebSocketManager.class);
    private final Map<String, HostHandler> listeners = new HashMap<>();

    private volatile @Nullable Session wsSession;

    public WebSocketManager(FreeboxOsSession session) throws FreeboxException {
        super(session, Permission.NONE, session.getUriBuilder().path(WS_PATH));
    }

    public void openSession(@Nullable String sessionToken) throws FreeboxException {
        WebSocketClient client = new WebSocketClient(session.getApiHandler().getHttpClient());
        URI uri = getUriBuilder().scheme(getUriBuilder().build().getScheme().contains("s") ? "wss" : "ws").build();
        ClientUpgradeRequest request = new ClientUpgradeRequest();
        request.setHeader(AUTH_HEADER, sessionToken);

        try {
            client.start();
            client.connect(this, uri, request);
        } catch (Exception e) {
            throw new FreeboxException(e, "Exception connecting websocket client");
        }
    }

    public void closeSession() {
        logger.info("Awaiting closure from remote");
        Session localSession = wsSession;
        if (localSession != null) {
            localSession.close();
        }
    }

    @Override
    public void onWebSocketConnect(@NonNullByDefault({}) Session wsSession) {
        this.wsSession = wsSession;
        logger.info("Websocket connection establisehd");
        try {
            wsSession.getRemote().sendString("{\"action\" : \"register\", \"events\" : [\"vm_state_changed\", \""
                    + HOST_REACHABLE + "\", \"" + HOST_UNREACHABLE + "\"] }");
        } catch (IOException e) {
            logger.warn("Error connecting to websocket : {}", e.getMessage());
        }
    }

    @Override
    public void onWebSocketText(@NonNullByDefault({}) String message) {
        Session localSession = wsSession;
        if (message.toLowerCase(Locale.US).contains("bye") && localSession != null) {
            localSession.close(StatusCode.NORMAL, "Thanks");
            return;
        }
        try {
            WebSocketResponse result = session.getApiHandler().deserialize(WebSocketResponse.class, message);
            if (result.isSuccess()) {
                switch (result.getAction()) {
                    case "register":
                        logger.info("Event registration successfull");
                        break;
                    case "notification":
                        logger.info("Notification received");
                        switch (result.getEvent()) {
                            case HOST_UNREACHABLE, HOST_REACHABLE:
                                JsonElement json = result.getResult();
                                if (json != null) {
                                    LanHost host = session.getApiHandler().deserialize(LanHost.class, json);
                                    logger.info("Received notification for {}", host.getMac());
                                    HostHandler listener = listeners.get(host.getMac());
                                    if (listener != null) {
                                        listener.updateConnectivityChannels(host);
                                    }
                                } else {
                                    logger.warn("Empty json element in notification");
                                }
                                break;
                            default:
                                logger.info("Unhandled event received : {}", result.getEvent());
                        }
                        break;
                    default:
                        logger.info("Unhandled notification received : {}", result.getAction());
                }
            }
        } catch (FreeboxException e) {
            logger.warn("Error deserializing notification : {} - {}", message, e.getMessage());
        }
    }

    @Override
    public void onWebSocketClose(int statusCode, @NonNullByDefault({}) String reason) {
        logger.info("Socket Closed: [{}] - reason {}", statusCode, reason);
        this.wsSession = null;
    }

    @Override
    public void onWebSocketError(@NonNullByDefault({}) Throwable cause) {
        logger.warn("Error on websocket : {}", cause.getMessage());
    }

    @Override
    public void onWebSocketBinary(byte @Nullable [] payload, int offset, int len) {
        /* do nothing */
    }

    public void registerListener(String id, HostHandler hostHandler) {
        listeners.put(id, hostHandler);
    }

    public void unregisterListener(String id) {
        listeners.remove(id);
    }
}
