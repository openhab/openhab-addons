package org.openhab.binding.evcc.internal.handler;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http.HttpHeader;
import org.openhab.binding.evcc.internal.discovery.EvccDiscoveryService;
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

import com.google.gson.Gson;
import com.google.gson.JsonObject;

@NonNullByDefault
public class EvccBridgeHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(EvccBridgeHandler.class);
    private final Gson gson = new Gson();

    private final HttpClientFactory httpClientFactory;
    private HttpClient httpClient;
    private final CopyOnWriteArrayList<EvccJsonAwareHandler> listeners = new CopyOnWriteArrayList<>();
    private @Nullable ScheduledFuture<?> pollJob;
    private volatile JsonObject lastState = new JsonObject();

    public EvccBridgeHandler(Bridge bridge, HttpClientFactory httpClientFactory) {
        super(bridge);
        this.httpClientFactory = httpClientFactory;
        httpClient = httpClientFactory.getCommonHttpClient();
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(EvccDiscoveryService.class);
    }

    @Override
    public void initialize() {
        httpClient = httpClientFactory.getCommonHttpClient();

        startPolling();

        fetchEvccState().ifPresent(state -> {
            this.lastState = state;
            notifyListeners(state);
            updateStatus(ThingStatus.ONLINE);
        });
    }

    @Override
    public void dispose() {
        Optional.ofNullable(pollJob).ifPresent(polling -> {
            if (!polling.isCancelled()) {
                polling.cancel(true);
            }
        });
        listeners.clear();
    }

    private void startPolling() {
        int refreshInterval = ((BigDecimal) getConfig().get("pollInterval")).intValue();
        if (refreshInterval <= 0) {
            refreshInterval = 30;
        }
        pollJob = scheduler.scheduleWithFixedDelay(() -> {
            fetchEvccState().ifPresent(state -> {
                this.lastState = state;
                notifyListeners(state);
            });
        }, 0, refreshInterval, TimeUnit.SECONDS);
    }

    public Optional<JsonObject> fetchEvccState() {
        String host = String.valueOf(getConfig().get("host"));
        int port = ((BigDecimal) getConfig().get("port")).intValue();
        String url = "http://" + host + ":" + port + "/api/state";

        try {
            ContentResponse response = httpClient.newRequest(url).timeout(5, TimeUnit.SECONDS)
                    .header(HttpHeader.ACCEPT, "application/json").send();

            if (response.getStatus() == 200) {
                @Nullable
                JsonObject return_value = gson.fromJson(response.getContentAsString(), JsonObject.class);
                if (return_value != null) {
                    updateStatus(ThingStatus.ONLINE);
                    return Optional.of(return_value.getAsJsonObject("result"));
                }
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
                logger.warn("EVCC API-Fehler: HTTP {}", response.getStatus());
            }

        } catch (Exception e) {
            logger.error("EVCC Bridge konnte API nicht abrufen", e);
        }

        return Optional.empty();
    }

    private void notifyListeners(JsonObject state) {
        for (EvccJsonAwareHandler listener : listeners) {
            try {
                listener.updateFromEvccState(state);
            } catch (Exception e) {
                if (listener instanceof BaseThingHandler handler) {
                    logger.warn("Listener {} konnte EVCC-State nicht verarbeiten", handler.getThing().getUID(), e);
                } else {
                    logger.warn("Ein Listener konnte EVCC-State nicht verarbeiten", e);
                }
            }
        }
    }

    public Optional<JsonObject> getCachedEvccState() {
        return Optional.ofNullable(lastState);
    }

    public void register(EvccJsonAwareHandler handler) {
        listeners.addIfAbsent(handler);
        Optional.ofNullable(lastState).ifPresent(state -> handler.updateFromEvccState(state));
    }

    public void unregister(EvccJsonAwareHandler handler) {
        listeners.remove(handler);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        return; // No commands to handle!
    }
}
