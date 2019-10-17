/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.lgwebos.internal.handler;

import static org.openhab.binding.lgwebos.internal.LGWebOSBindingConstants.*;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerService;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.lgwebos.action.LGWebOSActions;
import org.openhab.binding.lgwebos.internal.ChannelHandler;
import org.openhab.binding.lgwebos.internal.LGWebOSBindingConstants;
import org.openhab.binding.lgwebos.internal.LauncherApplication;
import org.openhab.binding.lgwebos.internal.MediaControlPlayer;
import org.openhab.binding.lgwebos.internal.MediaControlStop;
import org.openhab.binding.lgwebos.internal.PowerControlPower;
import org.openhab.binding.lgwebos.internal.TVControlChannel;
import org.openhab.binding.lgwebos.internal.TVControlChannelName;
import org.openhab.binding.lgwebos.internal.ToastControlToast;
import org.openhab.binding.lgwebos.internal.VolumeControlMute;
import org.openhab.binding.lgwebos.internal.VolumeControlVolume;
import org.openhab.binding.lgwebos.internal.handler.LGWebOSTVSocket.WebOSTVSocketListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link LGWebOSHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Sebastian Prehn - initial contribution
 */
public class LGWebOSHandler extends BaseThingHandler implements LGWebOSTVSocket.ConfigProvider, WebOSTVSocketListener {

    /*
     * constants for device polling
     */
    private static final int RECONNECT_INTERVAL_SECONDS = 10;
    private static final int RECONNECT_START_UP_DELAY_SECONDS = 0;

    /*
     * error messages
     */
    private static final String MSG_MISSING_PARAM = "Missing parameter \"host\"";

    private final Logger logger = LoggerFactory.getLogger(LGWebOSHandler.class);

    // ChannelID to CommandHandler Map
    private final Map<String, ChannelHandler> channelHandlers;

    private LauncherApplication appLauncher = new LauncherApplication();
    private LGWebOSTVSocket socket;
    private final WebSocketClient webSocketClient;

    private ScheduledFuture<?> reconnectJob;
    private LGWebOSConfiguration config;

    public LGWebOSHandler(@NonNull Thing thing, WebSocketClient webSocketClient) {
        super(thing);
        this.webSocketClient = webSocketClient;

        Map<String, ChannelHandler> handlers = new HashMap<>();
        handlers.put(CHANNEL_VOLUME, new VolumeControlVolume());
        handlers.put(CHANNEL_POWER, new PowerControlPower());
        handlers.put(CHANNEL_MUTE, new VolumeControlMute());
        handlers.put(CHANNEL_CHANNEL, new TVControlChannel());
        handlers.put(CHANNEL_CHANNEL_NAME, new TVControlChannelName());
        handlers.put(CHANNEL_APP_LAUNCHER, appLauncher);
        handlers.put(CHANNEL_MEDIA_STOP, new MediaControlStop());
        handlers.put(CHANNEL_TOAST, new ToastControlToast());
        handlers.put(CHANNEL_MEDIA_PLAYER, new MediaControlPlayer());
        channelHandlers = Collections.unmodifiableMap(handlers);
    }

    @Override
    public void initialize() {
        config = getConfigAs(LGWebOSConfiguration.class);
        logger.trace("Handler initialized with config {}", config);
        if (config.host == null || config.host.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, MSG_MISSING_PARAM);
            return;
        }

        socket = new LGWebOSTVSocket(webSocketClient, this, config.host, config.port);
        socket.setListener(this);

        startReconnectJob();
    }

    @Override
    public void dispose() {
        stopReconnectJob();
        if (this.socket != null) {
            this.socket.setListener(null);
            LGWebOSTVSocket oldSocket = this.socket;
            this.socket = null;
            this.scheduler.execute(() -> oldSocket.disconnect()); // dispose should be none-blocking
        }
        super.dispose();
    }

    private void startReconnectJob() {
        if (reconnectJob == null || reconnectJob.isCancelled()) {
            reconnectJob = scheduler.scheduleWithFixedDelay(() -> socket.connect(), RECONNECT_START_UP_DELAY_SECONDS,
                    RECONNECT_INTERVAL_SECONDS, TimeUnit.SECONDS);
        }
    }

    private void stopReconnectJob() {
        if (reconnectJob != null && !reconnectJob.isCancelled()) {
            reconnectJob.cancel(true);
        }
        reconnectJob = null;
    }

    public LGWebOSTVSocket getSocket() {
        return socket;
    }

    public LauncherApplication getLauncherApplication() {
        return appLauncher;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("handleCommand({},{})", channelUID, command);
        ChannelHandler handler = channelHandlers.get(channelUID.getId());
        if (handler == null) {
            logger.warn(
                    "Unable to handle command {}. No handler found for channel {}. This must not happen. Please report as a bug.",
                    command, channelUID);
            return;
        }

        handler.onReceiveCommand(channelUID.getId(), this, command);
    }

    @Override
    public String getKey() {
        return config.key;
    }

    @Override
    public void storeKey(String key) {
        // store it current configuration and avoiding complete re-initialization via handleConfigurationUpdate
        config.key = key;

        // persist the configuration change
        Configuration configuration = editConfiguration();
        configuration.put(LGWebOSBindingConstants.CONFIG_KEY, key);
        updateConfiguration(configuration);
    }

    @Override
    public void storeProperties(Map<String, String> properties) {
        Map<@NonNull String, @NonNull String> map = editProperties();
        map.putAll(properties);
        updateProperties(map);
    }

    @Override
    public void onStateChanged(LGWebOSTVSocket.State old, LGWebOSTVSocket.State state) {
        switch (state) {
            case DISCONNECTED:
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "TV is off");
                channelHandlers.forEach((k, v) -> {
                    v.onDeviceRemoved(k, this);
                    v.removeAnySubscription(this);
                });

                startReconnectJob();
                break;
            case CONNECTING:
                break;
            case REGISTERING:
                stopReconnectJob();
                updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE,
                        "Registering - You may need to confirm pairing on TV.");
                break;
            case REGISTERED:
                updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE, "Connected");
                channelHandlers.forEach((k, v) -> {
                    v.refreshSubscription(k, this);
                    v.onDeviceReady(k, this);
                });
                break;
            case DISCONNECTING:
                break;
        }

    }

    @Override
    public void onError(String error) {
        logger.debug("Connection failed - error: {}", error);
        switch (socket.getState()) {
            case CONNECTING:
            case DISCONNECTED:
            case DISCONNECTING:
                break;
            case REGISTERING:
            case REGISTERED:
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Connection Failed: " + error);
                break;
        }
    }

    public void postUpdate(String channelId, State state) {
        updateState(channelId, state);
    }

    public boolean isChannelInUse(String channelId) {
        return isLinked(channelId);
    }

    // channel linking modifications

    @Override
    public void channelLinked(ChannelUID channelUID) {
        refreshChannelSubscription(channelUID);
    }

    @Override
    public void channelUnlinked(ChannelUID channelUID) {
        refreshChannelSubscription(channelUID);
    }

    /**
     * Refresh channel subscription for one specific channel.
     *
     * @param channelUID must not be <code>null</code>
     */
    private void refreshChannelSubscription(ChannelUID channelUID) {
        String channelId = channelUID.getId();

        if (socket != null && socket.isConnected()) {
            channelHandlers.get(channelId).refreshSubscription(channelId, this);
        }

    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singleton(LGWebOSActions.class);
    }
}
