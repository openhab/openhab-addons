/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.awtrixng.internal.handler;

import static org.openhab.binding.awtrixng.internal.AwtrixNgBindingConstants.*;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.awtrixng.internal.AppChannels;
import org.openhab.binding.awtrixng.internal.AppConfigOptions;
import org.openhab.binding.awtrixng.internal.Helper;
import org.openhab.binding.awtrixng.internal.PayloadChannel;
import org.openhab.binding.awtrixng.internal.discovery.AwtrixNgBridgeDiscoveryService;
import org.openhab.binding.awtrixng.internal.dto.AppPayload;
import org.openhab.core.io.transport.mqtt.MqttMessageSubscriber;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonPrimitive;

/**
 * The {@link AwtrixNgAppHandler} represents a pushed app of an AWTRIX NG clock. It keeps the payload of the app,
 * changes
 * it with the commands of its channels, and sends it to the clock. As pushed apps do not survive a reboot of the clock,
 * the payload is also kept in a retained topic of the binding and sent again when the clock comes online.
 *
 * @author Keith T. Garner - Initial contribution
 */
@NonNullByDefault
public class AwtrixNgAppHandler extends BaseThingHandler implements MqttMessageSubscriber {

    // The stored state arrives right after subscribing, so an app that is still waiting has none
    private static final int SYNC_TIMEOUT_SECONDS = 5;

    private final Logger logger = LoggerFactory.getLogger(AwtrixNgAppHandler.class);

    private final Object lock = new Object();
    // Guarded by lock
    private AppPayload app = newApp();
    private boolean synchronizationRequired = true;

    private volatile String appName = "";
    private volatile boolean buttonControlled;
    // True between initialize() and dispose(), so a late message does not touch a disposed handler
    private volatile boolean active;
    private volatile @Nullable ScheduledFuture<?> syncTimeoutJob;

    /** The payload of the app and the channel states for it, taken at one moment. */
    private record Snapshot(String payload, Map<String, State> states) {
    }

    public AwtrixNgAppHandler(Thing thing) {
        super(thing);
    }

    /**
     * An empty payload deletes the app on the clock, so even a new app always sends its text.
     */
    private static AppPayload newApp() {
        AppPayload payload = new AppPayload();
        payload.text = new JsonPrimitive("");
        return payload;
    }

    @Override
    public void initialize() {
        AppConfigOptions config = getConfigAs(AppConfigOptions.class);
        String name = config.appname.trim();
        if (!APP_NAME_PATTERN.matcher(name).matches()) {
            updateStatus(ThingStatusDetail.CONFIGURATION_ERROR,
                    "The app name must have 1 to 32 letters, digits, - or _");
            return;
        }
        String oldName = this.appName;
        boolean renamed = !oldName.isEmpty() && !oldName.equals(name);
        AwtrixNgBridgeHandler bridgeHandler = getBridgeHandler();
        // Two things for one app would send their payloads over each other
        if (bridgeHandler != null && !name.equals(oldName) && bridgeHandler.isAppNameTaken(name, this)) {
            reportNameTaken(name);
            return;
        }
        if (renamed && bridgeHandler != null) {
            // The clock must not keep showing the app under the old name
            if (bridgeHandler.unregisterApp(oldName, this, thing.getUID())) {
                bridgeHandler.deleteApp(oldName);
            }
        }
        this.appName = name;
        this.buttonControlled = config.useButtons;
        this.active = true;
        // A change of the configuration initializes the handler again without the bridge being told, so what the app
        // holds stays, and an app that is renamed or that had no valid name before is registered under its name now.
        // Registering twice does no harm.
        if (bridgeHandler != null && bridgeHandler.registerApp(this, thing.getUID()) && renamed) {
            Snapshot snapshot = snapshotIfSynchronized();
            if (snapshot != null) {
                pushAndStore(snapshot);
            }
        }
        bridgeStatusChanged(getBridgeStatusInfo());
    }

    /**
     * Called by the bridge when another app thing got the name first, which the check of {@link #initialize} cannot
     * see for things that initialize at the same time. This one stays out of the app, as it would send over the other.
     */
    void rejectAppName(String name) {
        this.active = false;
        cancelSyncTimeout();
        reportNameTaken(name);
    }

    private void reportNameTaken(String name) {
        updateStatus(ThingStatusDetail.CONFIGURATION_ERROR, "Another app thing already uses the app name " + name);
    }

    @Override
    public void dispose() {
        this.active = false;
        cancelSyncTimeout();
    }

    @Override
    public void handleRemoval() {
        AwtrixNgBridgeHandler bridgeHandler = getBridgeHandler();
        // An app that is used by another thing is not this one's to delete
        if (bridgeHandler != null && !appName.isEmpty() && !bridgeHandler.isAppNameTaken(appName, this)) {
            bridgeHandler.deleteApp(appName);
        }
        super.handleRemoval();
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        if (!active) {
            return;
        }
        updateAppId();
        if (bridgeStatusInfo.getStatus() == ThingStatus.OFFLINE) {
            cancelSyncTimeout();
            updateStatus(ThingStatusDetail.BRIDGE_OFFLINE);
        } else if (bridgeStatusInfo.getStatus() == ThingStatus.UNKNOWN) {
            // The clock bridge is unknown until the clock has reported itself, which is normal after a start
            cancelSyncTimeout();
            updateStatus(ThingStatus.UNKNOWN, "Waiting for the clock");
        } else if (bridgeStatusInfo.getStatus() != ThingStatus.ONLINE) {
            updateStatus(ThingStatusDetail.COMMUNICATION_ERROR);
        } else if (isSynchronizationRequired()) {
            updateStatus(ThingStatus.UNKNOWN, "Waiting for the stored state of the app");
            cancelSyncTimeout();
            syncTimeoutJob = scheduler.schedule(this::finishSynchronization, SYNC_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } else {
            updateStatus(ThingStatus.ONLINE);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (!active) {
            // A thing that was refused its app name must not switch the app of the thing that owns it
            logger.debug("Ignoring command {}: the app is not in use by this thing", command);
            return;
        }
        String channelId = channelUID.getId();
        if (command instanceof RefreshType) {
            Snapshot snapshot = snapshotIfSynchronized();
            if (snapshot != null) {
                snapshot.states().forEach(this::updateChannel);
            }
            return;
        }
        switch (channelId) {
            case CHANNEL_ENABLED -> {
                AwtrixNgBridgeHandler bridgeHandler = getBridgeHandler();
                if (command instanceof OnOffType onOff && bridgeHandler != null) {
                    bridgeHandler.setAppEnabled(appName, onOff == OnOffType.ON);
                }
            }
            case CHANNEL_RESET -> {
                if (command == OnOffType.ON) {
                    reset();
                }
            }
            case CHANNEL_ACTIVE -> logger.debug("The active channel is read only");
            default -> {
                PayloadChannel<AppPayload> channel = AppChannels.get(channelId);
                if (channel != null) {
                    changeApp(channel, command);
                }
            }
        }
    }

    /** The stored state of the app, delivered by the bridge after it subscribed to the store topic. */
    @Override
    public void processMessage(String topic, byte[] payload) {
        if (!active) {
            return;
        }
        Snapshot snapshot;
        boolean readable;
        synchronized (lock) {
            if (!synchronizationRequired) {
                // Our own update, or one of another openHAB
                return;
            }
            String message = new String(payload, StandardCharsets.UTF_8);
            AppPayload stored = Helper.fromJson(message, AppPayload.class);
            readable = stored != null || message.isBlank();
            app = stored != null ? stored : newApp();
            ensureText(app);
            synchronizationRequired = false;
            snapshot = snapshotLocked();
        }
        if (!readable) {
            logger.warn("The stored state of the app {} cannot be read, it is kept until the app is changed", appName);
        }
        // What cannot be read is not written over, as it may be the only copy of what the user had set up
        publishSynchronized(snapshot, readable);
    }

    public String getAppName() {
        return appName;
    }

    public boolean isButtonControlled() {
        return buttonControlled;
    }

    /** Called by the bridge for the buttons pressed while the clock is locked on this app. */
    void handleButton(String channelId, String event) {
        triggerChannel(new ChannelUID(thing.getUID(), channelId), event);
    }

    /** Called by the bridge when the clock shows another app. */
    void setActive(boolean shown) {
        updateState(new ChannelUID(thing.getUID(), CHANNEL_ACTIVE), OnOffType.from(shown));
    }

    /** Called by the bridge when the clock comes online, as it has lost the pushed apps. */
    void deviceOnline() {
        Snapshot snapshot = snapshotIfSynchronized();
        AwtrixNgBridgeHandler bridgeHandler = getBridgeHandler();
        if (snapshot != null && bridgeHandler != null) {
            bridgeHandler.pushApp(appName, snapshot.payload());
        }
    }

    /** Without a stored state the app starts empty. Package private so that tests need not wait for the timeout. */
    void finishSynchronization() {
        Snapshot snapshot;
        synchronized (lock) {
            // A timeout that was already running when the thing was disposed must not write over the store
            if (!active || !synchronizationRequired) {
                return;
            }
            app = newApp();
            synchronizationRequired = false;
            snapshot = snapshotLocked();
        }
        publishSynchronized(snapshot, true);
    }

    private void reset() {
        Snapshot snapshot;
        synchronized (lock) {
            if (synchronizationRequired) {
                return;
            }
            app = newApp();
            snapshot = snapshotLocked();
        }
        snapshot.states().forEach(this::updateChannel);
        pushAndStore(snapshot);
    }

    private void changeApp(PayloadChannel<AppPayload> channel, Command command) {
        Snapshot snapshot;
        synchronized (lock) {
            if (synchronizationRequired) {
                // The command would be lost when the stored state arrives
                logger.debug("Ignoring command {} while the app is not synchronized", command);
                return;
            }
            if (!channel.apply(app, command)) {
                logger.warn("Ignoring command {}: its value is not valid for the channel", command);
                return;
            }
            ensureText(app);
            snapshot = snapshotLocked();
        }
        snapshot.states().forEach(this::updateChannel);
        pushAndStore(snapshot);
    }

    /**
     * The ID that identifies the app over all clocks, as the discovery gives it. Package private as the bridge tells
     * when the clock's unique ID got known after the app was online.
     */
    void updateAppId() {
        Bridge bridge = getBridge();
        String uniqueId = bridge == null ? null : bridge.getProperties().get(PROP_UNIQUEID);
        if (uniqueId != null) {
            updateProperty(PROP_APPID, AwtrixNgBridgeDiscoveryService.appId(uniqueId, appName));
        }
    }

    private void publishSynchronized(Snapshot snapshot, boolean store) {
        updateAppId();
        cancelSyncTimeout();
        snapshot.states().forEach(this::updateChannel);
        if (store) {
            pushAndStore(snapshot);
        } else {
            push(snapshot);
        }
        bridgeStatusChanged(getBridgeStatusInfo());
    }

    private void push(Snapshot snapshot) {
        AwtrixNgBridgeHandler bridgeHandler = getBridgeHandler();
        if (bridgeHandler != null) {
            bridgeHandler.pushApp(appName, snapshot.payload());
        }
    }

    private void pushAndStore(Snapshot snapshot) {
        AwtrixNgBridgeHandler bridgeHandler = getBridgeHandler();
        if (bridgeHandler != null) {
            bridgeHandler.pushApp(appName, snapshot.payload());
            bridgeHandler.storeApp(appName, snapshot.payload());
        }
    }

    private static void ensureText(AppPayload payload) {
        if (payload.text == null) {
            payload.text = new JsonPrimitive("");
        }
    }

    /** Must be called with the lock held. */
    private Snapshot snapshotLocked() {
        Map<String, State> states = new LinkedHashMap<>();
        for (Map.Entry<String, PayloadChannel<AppPayload>> channel : AppChannels.all()) {
            State state = channel.getValue().toState(app);
            states.put(channel.getKey(), state != null ? state : UnDefType.NULL);
        }
        return new Snapshot(Helper.toJson(app), states);
    }

    private @Nullable Snapshot snapshotIfSynchronized() {
        synchronized (lock) {
            return synchronizationRequired ? null : snapshotLocked();
        }
    }

    private boolean isSynchronizationRequired() {
        synchronized (lock) {
            return synchronizationRequired;
        }
    }

    private void cancelSyncTimeout() {
        ScheduledFuture<?> job = syncTimeoutJob;
        if (job != null) {
            job.cancel(false);
            syncTimeoutJob = null;
        }
    }

    private void updateChannel(String channelId, State state) {
        updateState(new ChannelUID(thing.getUID(), channelId), state);
    }

    private ThingStatusInfo getBridgeStatusInfo() {
        Bridge bridge = getBridge();
        return bridge != null ? bridge.getStatusInfo()
                : new ThingStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED, null);
    }

    private @Nullable AwtrixNgBridgeHandler getBridgeHandler() {
        Bridge bridge = getBridge();
        ThingHandler handler = bridge == null ? null : bridge.getHandler();
        return handler instanceof AwtrixNgBridgeHandler bridgeHandler ? bridgeHandler : null;
    }
}
