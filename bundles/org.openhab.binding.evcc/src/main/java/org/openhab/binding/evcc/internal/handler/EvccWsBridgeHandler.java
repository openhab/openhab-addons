package org.openhab.binding.evcc.internal.handler;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.evcc.internal.EvccBridgeConfiguration;
import org.openhab.binding.evcc.internal.api.EvccRequestQueue;
import org.openhab.binding.evcc.internal.api.EvccWebSocketClient;
import org.openhab.binding.evcc.internal.discovery.EvccThingDiscoveryService;
import org.openhab.core.common.ThreadPoolManager;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

@NonNullByDefault
public class EvccWsBridgeHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(EvccWsBridgeHandler.class);

    private final ScheduledExecutorService scheduler = ThreadPoolManager.getScheduledPool("evcc-bridge-handler");

    private final Map<String, EvccThingLifecycleAware> listeners = new ConcurrentHashMap<>();
    private final Map<String, String> propertyByRoot = new ConcurrentHashMap<>();

    final EvccRequestQueue requestQueue;

    private final TranslationProvider i18nProvider;
    private final LocaleProvider localeProvider;

    private volatile JsonObject lastState = new JsonObject();
    private volatile boolean initialStateReceived = false;

    private @Nullable EvccWebSocketClient wsClient;

    private String endpoint = "";

    public EvccWsBridgeHandler(Bridge bridge, HttpClientFactory httpClientFactory, TranslationProvider i18nProvider,
            LocaleProvider localeProvider) {

        super(bridge);
        this.i18nProvider = i18nProvider;
        this.localeProvider = localeProvider;

        requestQueue = new EvccRequestQueue(httpClientFactory.getCommonHttpClient());
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(EvccThingDiscoveryService.class);
    }

    @Override
    public void initialize() {
        EvccBridgeConfiguration cfg = getConfigAs(EvccBridgeConfiguration.class);

        endpoint = cfg.scheme + "://" + cfg.host + ":" + cfg.port + "/api";
        String wsUrl = endpoint.replace("http", "ws").replace("/api", "/ws");

        EvccWebSocketClient client = new EvccWebSocketClient(wsUrl, scheduler, this::onFullState, this::onPartialUpdate,
                this::onConnected, this::onDisconnected);

        client.start();
        wsClient = client;
        requestQueue.start();
    }

    @Override
    public void dispose() {
        Optional.ofNullable(wsClient).ifPresent(EvccWebSocketClient::stop);
        listeners.clear();
        requestQueue.stop();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // no commands
    }

    public String getBaseURL() {
        return endpoint;
    }

    public TranslationProvider getI18nProvider() {
        return i18nProvider;
    }

    public LocaleProvider getLocaleProvider() {
        return localeProvider;
    }

    // --------------------------------------------------------------------
    // WebSocket Callbacks
    // --------------------------------------------------------------------

    private void onConnected() {
        updateStatus(ThingStatus.ONLINE);
        logger.info("EVCC WebSocket connected");
    }

    private void onDisconnected() {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "WebSocket disconnected");
        logger.info("EVCC WebSocket disconnected");
    }

    private void onFullState(JsonObject state) {
        lastState = state.deepCopy();
        for (Map.Entry<String, JsonElement> entry : state.entrySet()) {
            mergeIntoState(entry.getKey(), entry.getValue());
        }
        initialStateReceived = true;
        for (EvccThingLifecycleAware listener : listeners.values()) {
            try {
                listener.initializeThingFromLatestState(lastState);
            } catch (Exception e) {
                logListenerError(listener, e);
            }
        }
    }

    private void onPartialUpdate(String key, JsonElement value) {
        mergeIntoState(key, value);
        dispatchUpdate(key, value);
    }

    // --------------------------------------------------------------------
    // Listener Registration
    // --------------------------------------------------------------------

    public void register(EvccThingLifecycleAware handler) {
        if (initialStateReceived) {
            handler.initializeThingFromLatestState(lastState);
            listeners.put(handler.getType() + "$" + handler.getIdentifier(), handler);
            for (String root : handler.getRootTypes()) {
                propertyByRoot.put(root, handler.getType());
            }
        } else {
            scheduler.schedule(() -> register(handler), 5, TimeUnit.SECONDS);
        }
    }

    public void unregister(EvccThingLifecycleAware handler) {
        listeners.remove(handler.getType() + "$" + handler.getIdentifier());
    }

    // --------------------------------------------------------------------
    // State Merging
    // --------------------------------------------------------------------

    private void mergeIntoState(String key, JsonElement value) {
        String[] parts = key.split("\\.");
        if (parts.length != 3) {
            return;
        }

        int index = Integer.parseInt(parts[1]);
        lastState.remove(key);
        JsonArray arr = lastState.getAsJsonArray(parts[0]);
        if (arr == null) {
            arr = new JsonArray();
            lastState.add(parts[0], arr);
        }

        while (arr.size() <= index) {
            arr.add(new JsonObject());
        }

        arr.get(index).getAsJsonObject().add(parts[2], value);
    }

    // --------------------------------------------------------------------
    // Dispatching
    // --------------------------------------------------------------------

    private void dispatchUpdate(String key, JsonElement value) {
        String[] p = key.split("\\.", 3);
        String root = p.length > 1 ? p[0] : "site";
        String id = p.length > 1 ? p[1] : "";
        String sub = p.length == 3 ? p[2] : p[0];

        listeners.values().forEach(l -> {
            if (matchesType(l.getType(), root) && matchesIdentifier(l.getIdentifier(), id)) {
                l.handleUpdate(sub, value);
            }
        });
    }

    private boolean matchesType(String type, String root) {
        return type.equals(propertyByRoot.getOrDefault(root, ""));
    }

    private boolean matchesIdentifier(Object listenerId, String identifier) {
        if (listenerId instanceof Integer idInt) {
            return identifier.matches("\\d+") && idInt == Integer.parseInt(identifier);
        }
        if (listenerId instanceof String idStr) {
            return idStr.equals(identifier);
        }
        return false;
    }

    private void logListenerError(EvccThingLifecycleAware listener, Exception e) {
        if (listener instanceof BaseThingHandler handler) {
            logger.warn("Listener {} failed to process EVCC update", handler.getThing().getUID(), e);
        }
    }

    public JsonObject getCachedEvccState() {
        return lastState.deepCopy();
    }
}
