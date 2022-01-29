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
package org.openhab.binding.lgwebos.internal.handler;

import static org.openhab.binding.lgwebos.internal.LGWebOSBindingConstants.*;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.openhab.binding.lgwebos.internal.ChannelHandler;
import org.openhab.binding.lgwebos.internal.LGWebOSBindingConstants;
import org.openhab.binding.lgwebos.internal.LGWebOSStateDescriptionOptionProvider;
import org.openhab.binding.lgwebos.internal.LauncherApplication;
import org.openhab.binding.lgwebos.internal.MediaControlPlayer;
import org.openhab.binding.lgwebos.internal.MediaControlStop;
import org.openhab.binding.lgwebos.internal.PowerControlPower;
import org.openhab.binding.lgwebos.internal.RCButtonControl;
import org.openhab.binding.lgwebos.internal.TVControlChannel;
import org.openhab.binding.lgwebos.internal.ToastControlToast;
import org.openhab.binding.lgwebos.internal.VolumeControlMute;
import org.openhab.binding.lgwebos.internal.VolumeControlVolume;
import org.openhab.binding.lgwebos.internal.WakeOnLanUtility;
import org.openhab.binding.lgwebos.internal.action.LGWebOSActions;
import org.openhab.binding.lgwebos.internal.handler.LGWebOSTVSocket.WebOSTVSocketListener;
import org.openhab.binding.lgwebos.internal.handler.core.AppInfo;
import org.openhab.binding.lgwebos.internal.handler.core.ResponseListener;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.StateOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link LGWebOSHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Sebastian Prehn - initial contribution
 */
@NonNullByDefault
public class LGWebOSHandler extends BaseThingHandler
        implements LGWebOSTVSocket.ConfigProvider, WebOSTVSocketListener, PowerControlPower.ConfigProvider {

    /*
     * constants for device polling
     */
    private static final int RECONNECT_INTERVAL_SECONDS = 10;
    private static final int RECONNECT_START_UP_DELAY_SECONDS = 0;
    private static final int CHANNEL_SUBSCRIPTION_DELAY_SECONDS = 1;
    private static final String APP_ID_LIVETV = "com.webos.app.livetv";

    private final Logger logger = LoggerFactory.getLogger(LGWebOSHandler.class);

    // ChannelID to CommandHandler Map
    private final Map<String, ChannelHandler> channelHandlers;

    private final LauncherApplication appLauncher = new LauncherApplication();

    private final WebSocketClient webSocketClient;

    private final LGWebOSStateDescriptionOptionProvider stateDescriptionProvider;

    private @Nullable LGWebOSTVSocket socket;

    private @Nullable ScheduledFuture<?> reconnectJob;
    private @Nullable ScheduledFuture<?> keepAliveJob;
    private @Nullable ScheduledFuture<?> channelSubscriptionJob;

    private @Nullable LGWebOSConfiguration config;

    public LGWebOSHandler(Thing thing, WebSocketClient webSocketClient,
            LGWebOSStateDescriptionOptionProvider stateDescriptionProvider) {
        super(thing);
        this.webSocketClient = webSocketClient;
        this.stateDescriptionProvider = stateDescriptionProvider;

        Map<String, ChannelHandler> handlers = new HashMap<>();
        handlers.put(CHANNEL_VOLUME, new VolumeControlVolume());
        handlers.put(CHANNEL_POWER, new PowerControlPower(this, scheduler));
        handlers.put(CHANNEL_MUTE, new VolumeControlMute());
        handlers.put(CHANNEL_CHANNEL, new TVControlChannel());
        handlers.put(CHANNEL_APP_LAUNCHER, appLauncher);
        handlers.put(CHANNEL_MEDIA_STOP, new MediaControlStop());
        handlers.put(CHANNEL_TOAST, new ToastControlToast());
        handlers.put(CHANNEL_MEDIA_PLAYER, new MediaControlPlayer());
        handlers.put(CHANNEL_RCBUTTON, new RCButtonControl());
        channelHandlers = Collections.unmodifiableMap(handlers);
    }

    private LGWebOSConfiguration getLGWebOSConfig() {
        LGWebOSConfiguration c = config;
        if (c == null) {
            c = getConfigAs(LGWebOSConfiguration.class);
            config = c;
        }
        return c;
    }

    @Override
    public void initialize() {
        logger.debug("Initializing handler for thing {}", getThing().getUID());
        LGWebOSConfiguration c = getLGWebOSConfig();
        logger.trace("Handler initialized with config {}", c);
        String host = c.getHost();
        if (host.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.config-error-unknown-host");
            return;
        }

        LGWebOSTVSocket s = new LGWebOSTVSocket(webSocketClient, this, host, c.getPort(), scheduler);
        s.setListener(this);
        socket = s;

        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "@text/offline.tv-off");

        startReconnectJob();
    }

    @Override
    public void dispose() {
        logger.debug("Disposing handler for thing {}", getThing().getUID());
        stopKeepAliveJob();
        stopReconnectJob();
        stopChannelSubscriptionJob();

        LGWebOSTVSocket s = socket;
        if (s != null) {
            s.setListener(null);
            s.disconnect();
        }
        socket = null;
        config = null; // ensure config gets actually refreshed during re-initialization
        super.dispose();
    }

    private void startReconnectJob() {
        ScheduledFuture<?> job = reconnectJob;
        if (job == null || job.isCancelled()) {
            reconnectJob = scheduler.scheduleWithFixedDelay(() -> {
                getSocket().disconnect();
                getSocket().connect();
            }, RECONNECT_START_UP_DELAY_SECONDS, RECONNECT_INTERVAL_SECONDS, TimeUnit.SECONDS);
        }
    }

    private void stopReconnectJob() {
        ScheduledFuture<?> job = reconnectJob;
        if (job != null && !job.isCancelled()) {
            job.cancel(true);
        }
        reconnectJob = null;
    }

    /**
     * Keep alive ensures that the web socket connection is used and does not time out.
     */
    private void startKeepAliveJob() {
        ScheduledFuture<?> job = keepAliveJob;
        if (job == null || job.isCancelled()) {
            // half of idle time out setting
            long keepAliveInterval = this.webSocketClient.getMaxIdleTimeout() / 2;

            // it is irrelevant which service is queried. Only need to send some packets over the wire

            keepAliveJob = scheduler
                    .scheduleWithFixedDelay(() -> getSocket().getRunningApp(new ResponseListener<AppInfo>() {

                        @Override
                        public void onSuccess(AppInfo responseObject) {
                            // ignore - actual response is not relevant here
                        }

                        @Override
                        public void onError(String message) {
                            // ignore
                        }
                    }), keepAliveInterval, keepAliveInterval, TimeUnit.MILLISECONDS);

        }
    }

    private void stopKeepAliveJob() {
        ScheduledFuture<?> job = keepAliveJob;
        if (job != null && !job.isCancelled()) {
            job.cancel(true);
        }
        keepAliveJob = null;
    }

    public LGWebOSTVSocket getSocket() {
        LGWebOSTVSocket s = this.socket;
        if (s == null) {
            throw new IllegalStateException("Component called before it was initialized or already disposed.");
        }
        return s;
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
    public String getMacAddress() {
        return getLGWebOSConfig().getMacAddress();
    }

    @Override
    public String getKey() {
        return getLGWebOSConfig().getKey();
    }

    @Override
    public void storeKey(@Nullable String key) {
        if (!getKey().equals(key)) {
            logger.debug("Store new access Key in the thing configuration");
            // store it current configuration and avoiding complete re-initialization via handleConfigurationUpdate
            getLGWebOSConfig().key = key;

            // persist the configuration change
            Configuration configuration = editConfiguration();
            configuration.put(LGWebOSBindingConstants.CONFIG_KEY, key);
            updateConfiguration(configuration);
        }
    }

    @Override
    public void storeProperties(Map<String, String> properties) {
        logger.debug("storeProperties {}", properties);
        Map<String, String> map = editProperties();
        map.putAll(properties);
        updateProperties(map);
    }

    @Override
    public void onStateChanged(LGWebOSTVSocket.State state) {
        switch (state) {
            case DISCONNECTING:
                postUpdate(CHANNEL_POWER, OnOffType.OFF);
                break;
            case DISCONNECTED:
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "@text/offline.tv-off");
                channelHandlers.forEach((k, v) -> {
                    v.onDeviceRemoved(k, this);
                    v.removeAnySubscription(this);
                });

                stopKeepAliveJob();
                startReconnectJob();
                break;
            case CONNECTING:
                stopReconnectJob();
                break;
            case REGISTERING:
                updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE, "@text/online.registering");
                findMacAddress();
                break;
            case REGISTERED:
                startKeepAliveJob();
                updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE, "@text/online.connected");

                channelHandlers.forEach((k, v) -> {
                    // refresh subscriptions except on channel, which can only be subscribe in livetv app. see
                    // postUpdate method
                    if (!CHANNEL_CHANNEL.equals(k)) {
                        v.refreshSubscription(k, this);
                    }
                    v.onDeviceReady(k, this);
                });

                break;

        }
    }

    @Override
    public void onError(String error) {
        logger.debug("Connection failed - error: {}", error);

        switch (getSocket().getState()) {
            case DISCONNECTING:
            case DISCONNECTED:
                break;
            case CONNECTING:
            case REGISTERING:
            case REGISTERED:
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        String.format("@text/offline.comm-error-connexion-failed [ \"%s\" ]", error));
                break;
        }
    }

    public void setOptions(String channelId, List<StateOption> options) {
        logger.debug("setOptions channelId={} options.size()={}", channelId, options.size());
        stateDescriptionProvider.setStateOptions(new ChannelUID(getThing().getUID(), channelId), options);
    }

    public void postUpdate(String channelId, State state) {
        if (isLinked(channelId)) {
            updateState(channelId, state);
        }

        // channel subscription only works when livetv app is started,
        // therefore we need to slightly delay the subscription
        if (CHANNEL_APP_LAUNCHER.equals(channelId)) {
            if (APP_ID_LIVETV.equals(state.toString())) {
                scheduleChannelSubscriptionJob();
            } else {
                stopChannelSubscriptionJob();
            }
        }
    }

    private void scheduleChannelSubscriptionJob() {
        ScheduledFuture<?> job = channelSubscriptionJob;
        if (job == null || job.isCancelled()) {
            logger.debug("Schedule channel subscription job");
            channelSubscriptionJob = scheduler.schedule(
                    () -> channelHandlers.get(CHANNEL_CHANNEL).refreshSubscription(CHANNEL_CHANNEL, this),
                    CHANNEL_SUBSCRIPTION_DELAY_SECONDS, TimeUnit.SECONDS);
        }
    }

    private void stopChannelSubscriptionJob() {
        ScheduledFuture<?> job = channelSubscriptionJob;
        if (job != null && !job.isCancelled()) {
            logger.debug("Stop channel subscription job");
            job.cancel(true);
        }
        channelSubscriptionJob = null;
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singleton(LGWebOSActions.class);
    }

    /**
     * Make a best effort to automatically detect the MAC address of the TV.
     * If this does not work automatically, users can still set it manually in the Thing config.
     */
    private void findMacAddress() {
        LGWebOSConfiguration c = getLGWebOSConfig();
        String host = c.getHost();
        if (!host.isEmpty()) {
            try {
                // validate host, so that no command can be injected
                String macAddress = WakeOnLanUtility.getMACAddress(InetAddress.getByName(host).getHostAddress());
                if (macAddress != null && !macAddress.equals(c.macAddress)) {
                    c.macAddress = macAddress;
                    // persist the configuration change
                    Configuration configuration = editConfiguration();
                    configuration.put(LGWebOSBindingConstants.CONFIG_MAC_ADDRESS, macAddress);
                    updateConfiguration(configuration);
                }
            } catch (UnknownHostException e) {
                logger.debug("Unable to determine MAC address: {}", e.getMessage());
            }
        }
    }

    public List<String> reportApplications() {
        return appLauncher.reportApplications(getThing().getUID());
    }

    public List<String> reportChannels() {
        return ((TVControlChannel) channelHandlers.get(CHANNEL_CHANNEL)).reportChannels(getThing().getUID());
    }
}
