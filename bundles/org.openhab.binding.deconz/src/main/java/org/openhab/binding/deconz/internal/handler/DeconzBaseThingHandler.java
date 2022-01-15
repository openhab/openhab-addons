/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.deconz.internal.handler;

import static org.openhab.binding.deconz.internal.BindingConstants.*;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.deconz.internal.dto.DeconzBaseMessage;
import org.openhab.binding.deconz.internal.netutils.WebSocketConnection;
import org.openhab.binding.deconz.internal.netutils.WebSocketMessageListener;
import org.openhab.binding.deconz.internal.types.ResourceType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.type.ChannelKind;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * This base thing doesn't establish any connections, that is done by the bridge Thing.
 *
 * It waits for the bridge to come online, grab the websocket connection and bridge configuration
 * and registers to the websocket connection as a listener.
 **
 * @author David Graeff - Initial contribution
 * @author Jan N. Klug - Refactored to abstract class
 */
@NonNullByDefault
public abstract class DeconzBaseThingHandler extends BaseThingHandler implements WebSocketMessageListener {
    private final Logger logger = LoggerFactory.getLogger(DeconzBaseThingHandler.class);
    protected final ResourceType resourceType;
    protected ThingConfig config = new ThingConfig();
    protected final Gson gson;
    private @Nullable ScheduledFuture<?> initializationJob;
    protected @Nullable WebSocketConnection connection;

    public DeconzBaseThingHandler(Thing thing, Gson gson, ResourceType resourceType) {
        super(thing);
        this.gson = gson;
        this.resourceType = resourceType;
    }

    /**
     * Stops the API request
     */
    private void stopInitializationJob() {
        ScheduledFuture<?> future = initializationJob;
        if (future != null) {
            future.cancel(true);
            initializationJob = null;
        }
    }

    private void registerListener() {
        WebSocketConnection conn = connection;
        if (conn != null) {
            conn.registerListener(resourceType, config.id, this);
        }
    }

    private void unregisterListener() {
        WebSocketConnection conn = connection;
        if (conn != null) {
            conn.unregisterListener(resourceType, config.id);
        }
    }

    private @Nullable DeconzBridgeHandler getBridgeHandler() {
        Bridge bridge = getBridge();
        if (bridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            return null;
        }
        return (DeconzBridgeHandler) bridge.getHandler();
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        if (config.id.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "ID not set");
            return;
        }

        if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE) {
            // the bridge is ONLINE, we can communicate with the gateway, so we update the connection parameters and
            // register the listener
            DeconzBridgeHandler bridgeHandler = getBridgeHandler();
            if (bridgeHandler == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
                return;
            }

            final WebSocketConnection webSocketConnection = bridgeHandler.getWebsocketConnection();
            this.connection = webSocketConnection;

            updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE);

            // Real-time data
            registerListener();

            // get initial values
            requestState(this::processStateResponse);
        } else {
            // if the bridge is not ONLINE, we assume communication is not possible, so we unregister the listener and
            // set the thing status to OFFLINE
            unregisterListener();
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        }
    }

    /**
     * processes a newly received (initial) state response
     *
     * MUST set the thing status!
     *
     * @param stateResponse
     */
    protected abstract void processStateResponse(DeconzBaseMessage stateResponse);

    /**
     * Perform a request to the REST API for retrieving the full light state with all data and configuration.
     */
    protected void requestState(Consumer<DeconzBaseMessage> processor) {
        DeconzBridgeHandler bridgeHandler = getBridgeHandler();
        if (bridgeHandler != null) {
            bridgeHandler.getBridgeFullState()
                    .thenAccept(f -> f.map(s -> s.getMessage(resourceType, config.id)).ifPresentOrElse(message -> {
                        logger.trace("{} processing {}", thing.getUID(), message);
                        processor.accept(message);
                    }, () -> {
                        if (initializationJob != null) {
                            stopInitializationJob();
                            initializationJob = scheduler.schedule(() -> requestState(this::processStateResponse), 10,
                                    TimeUnit.SECONDS);
                        }
                    }));
        }
    }

    /**
     * sends a command to the bridge with the default command URL
     *
     * @param object must be serializable and contain the command
     * @param originalCommand the original openHAB command (used for logging purposes)
     * @param channelUID the channel that this command was send to (used for logging purposes)
     * @param acceptProcessing additional processing after the command was successfully send (might be null)
     */
    protected void sendCommand(@Nullable Object object, Command originalCommand, ChannelUID channelUID,
            @Nullable Runnable acceptProcessing) {
        sendCommand(object, originalCommand, channelUID, resourceType.getCommandUrl(), acceptProcessing);
    }

    /**
     * sends a command to the bridge with a caller-defined command URL
     *
     * @param object must be serializable and contain the command
     * @param originalCommand the original openHAB command (used for logging purposes)
     * @param channelUID the channel that this command was send to (used for logging purposes)
     * @param commandUrl the command URL
     * @param acceptProcessing additional processing after the command was successfully send (might be null)
     */
    protected void sendCommand(@Nullable Object object, Command originalCommand, ChannelUID channelUID,
            String commandUrl, @Nullable Runnable acceptProcessing) {
        DeconzBridgeHandler bridgeHandler = getBridgeHandler();
        if (bridgeHandler == null) {
            return;
        }
        String endpoint = Stream.of(resourceType.getIdentifier(), config.id, commandUrl)
                .collect(Collectors.joining("/"));

        bridgeHandler.sendObject(endpoint, object).thenAccept(v -> {
            if (acceptProcessing != null) {
                acceptProcessing.run();
            }
            if (v.getResponseCode() != java.net.HttpURLConnection.HTTP_OK) {
                logger.warn("Sending command {} to channel {} failed: {} - {}", originalCommand, channelUID,
                        v.getResponseCode(), v.getBody());
            } else {
                logger.trace("Result code={}, body={}", v.getResponseCode(), v.getBody());
            }
        }).exceptionally(e -> {
            logger.warn("Sending command {} to channel {} failed: {} - {}", originalCommand, channelUID, e.getClass(),
                    e.getMessage());
            return null;
        });
    }

    @Override
    public void dispose() {
        stopInitializationJob();
        unregisterListener();
        super.dispose();
    }

    @Override
    public void initialize() {
        config = getConfigAs(ThingConfig.class);

        Bridge bridge = getBridge();
        if (bridge != null) {
            bridgeStatusChanged(bridge.getStatusInfo());
        }
    }

    protected void createChannel(String channelId, ChannelKind kind) {
        if (thing.getChannel(channelId) != null) {
            // channel already exists, no update necessary
            return;
        }

        ThingHandlerCallback callback = getCallback();
        if (callback != null) {
            ChannelUID channelUID = new ChannelUID(thing.getUID(), channelId);
            ChannelTypeUID channelTypeUID;
            switch (channelId) {
                case CHANNEL_BATTERY_LEVEL:
                    channelTypeUID = new ChannelTypeUID("system:battery-level");
                    break;
                case CHANNEL_BATTERY_LOW:
                    channelTypeUID = new ChannelTypeUID("system:low-battery");
                    break;
                default:
                    channelTypeUID = new ChannelTypeUID(BINDING_ID, channelId);
                    break;
            }
            Channel channel = callback.createChannelBuilder(channelUID, channelTypeUID).withKind(kind).build();
            updateThing(editThing().withChannel(channel).build());
        }
    }
}
