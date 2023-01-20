package org.openhab.binding.freeboxos.internal.api.rest;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.StatusCode;
import org.eclipse.jetty.websocket.api.WebSocketListener;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.openhab.binding.freeboxos.internal.api.ApiHandler;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.rest.LanBrowserManager.LanHost;
import org.openhab.binding.freeboxos.internal.api.rest.LoginManager.Session.Permission;
import org.openhab.binding.freeboxos.internal.api.rest.VmManager.VirtualMachine;
import org.openhab.binding.freeboxos.internal.handler.HostHandler;
import org.openhab.binding.freeboxos.internal.handler.VmHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;

import inet.ipaddr.mac.MACAddress;

@NonNullByDefault
public class WebSocketManager extends RestManager implements WebSocketListener {
    private static final String HOST_UNREACHABLE = "lan_host_l3addr_unreachable";
    private static final String HOST_REACHABLE = "lan_host_l3addr_reachable";
    private static final String VM_CHANGED = "vm_state_changed";
    private static final Register REGISTRATION = new Register("register",
            List.of(VM_CHANGED, HOST_REACHABLE, HOST_UNREACHABLE));
    private static final String WS_PATH = "ws/event";

    private final Logger logger = LoggerFactory.getLogger(WebSocketManager.class);
    private final Map<MACAddress, HostHandler> lanHosts = new HashMap<>();
    private final Map<Integer, VmHandler> vms = new HashMap<>();
    private final ApiHandler apiHandler;

    private volatile @Nullable Session wsSession;

    private record Register(String action, List<String> events) {

    }

    public WebSocketManager(FreeboxOsSession session) throws FreeboxException {
        super(session, Permission.NONE, session.getUriBuilder().path(WS_PATH));
        this.apiHandler = session.getApiHandler();
    }

    private static enum Action {
        REGISTER,
        NOTIFICATION,
        UNKNOWN;
    }

    private static record WebSocketResponse(boolean success, Action action, String event, String source,
            @Nullable JsonElement result) {
        public String getEvent() {
            return source + "_" + event;
        }
    }

    public void openSession(@Nullable String sessionToken) throws FreeboxException {
        WebSocketClient client = new WebSocketClient(apiHandler.getHttpClient());
        URI uri = getUriBuilder().scheme(getUriBuilder().build().getScheme().contains("s") ? "wss" : "ws").build();
        ClientUpgradeRequest request = new ClientUpgradeRequest();
        request.setHeader(ApiHandler.AUTH_HEADER, sessionToken);

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
            wsSession.getRemote().sendString(apiHandler.toJson(REGISTRATION));
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
            if (result.success) {
                switch (result.action) {
                    case REGISTER:
                        logger.info("Event registration successfull");
                        break;
                    case NOTIFICATION:
                        handleNotification(result);
                        break;
                    default:
                        logger.info("Unhandled notification received : {}", result.action);
                }
            }
        } catch (FreeboxException e) {
            logger.warn("Error deserializing notification : {} - {}", message, e.getMessage());
        }
    }

    private void handleNotification(WebSocketResponse result) throws FreeboxException {
        JsonElement json = result.result;
        if (json != null) {
            switch (result.getEvent()) {
                case VM_CHANGED:
                    VirtualMachine vm = apiHandler.deserialize(VirtualMachine.class, json);
                    logger.info("Received notification for VM {}", vm.id());
                    VmHandler vmHandler = vms.get(vm.id());
                    if (vmHandler != null) {
                        vmHandler.updateVmChannels(vm);
                    }
                    break;
                case HOST_UNREACHABLE, HOST_REACHABLE:
                    LanHost host = apiHandler.deserialize(LanHost.class, json);
                    MACAddress mac = host.getMac();
                    logger.info("Received notification for LanHost {}", mac.toColonDelimitedString());
                    HostHandler hostHandler = lanHosts.get(mac);
                    if (hostHandler != null) {
                        hostHandler.updateConnectivityChannels(host);
                    }
                    break;
                default:
                    logger.info("Unhandled event received : {}", result.getEvent());
            }
        } else {
            logger.warn("Empty json element in notification");
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

    public void registerListener(MACAddress mac, HostHandler hostHandler) {
        lanHosts.put(mac, hostHandler);
    }

    public void unregisterListener(MACAddress mac) {
        lanHosts.remove(mac);
    }

    public void registerVm(int clientId, VmHandler vmHandler) {
        vms.put(clientId, vmHandler);
    }

    public void unregisterVm(int clientId) {
        vms.remove(clientId);
    }
}
