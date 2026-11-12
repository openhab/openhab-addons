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
package org.openhab.binding.tailwind.internal.handler;

import static org.openhab.binding.tailwind.internal.TailwindBindingConstants.*;

import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.tailwind.internal.TailwindConfiguration;
import org.openhab.binding.tailwind.internal.TailwindDeviceId;
import org.openhab.binding.tailwind.internal.api.TailwindApi;
import org.openhab.binding.tailwind.internal.api.TailwindCommunicationException;
import org.openhab.binding.tailwind.internal.api.TailwindException;
import org.openhab.binding.tailwind.internal.api.TailwindUdpListener;
import org.openhab.binding.tailwind.internal.api.TailwindUnauthorizedException;
import org.openhab.binding.tailwind.internal.api.dto.TailwindDoor;
import org.openhab.binding.tailwind.internal.api.dto.TailwindStatus;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.net.NetworkAddressService;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelGroupUID;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link TailwindHandler} connects a TailWind controller Thing to the controller.
 *
 * The controller sends a status report as a UDP message whenever a door changes. The handler also asks for the
 * status regularly, which shows whether the controller can still be reached and picks up changes that have no
 * report, such as the LED brightness.
 *
 * @author Dave J. Schoepel - Initial contribution
 * @author Christopher Smith - Reworked lifecycle, channels and status handling
 */
@NonNullByDefault
public class TailwindHandler extends BaseThingHandler {
    private static final int POLL_INTERVAL_SECONDS = 300;
    private static final int RETRY_MIN_SECONDS = 30;
    private static final int RETRY_MAX_SECONDS = 300;

    private final Logger logger = LoggerFactory.getLogger(TailwindHandler.class);
    private final HttpClient httpClient;
    private final NetworkAddressService networkAddressService;

    private TailwindConfiguration config = new TailwindConfiguration();
    // Set by initialize() and cleared by dispose(). Work that started with another instance is stale and must not
    // publish anything or schedule more work.
    private volatile @Nullable TailwindApi api;
    private volatile @Nullable TailwindStatus lastStatus;
    // The controller's addresses, the only senders whose status reports are accepted
    private volatile Set<InetAddress> controllerAddresses = Set.of();
    private @Nullable ScheduledFuture<?> pollJob;
    // Changes whenever a status request is scheduled, or the handler is disposed. A request of an older generation
    // does not schedule the next one, so there is never more than one chain of requests.
    private int pollGeneration;
    private @Nullable TailwindUdpListener listener;
    private int retryDelaySeconds = RETRY_MIN_SECONDS;

    public TailwindHandler(Thing thing, HttpClient httpClient, NetworkAddressService networkAddressService) {
        super(thing);
        this.httpClient = httpClient;
        this.networkAddressService = networkAddressService;
    }

    @Override
    public void initialize() {
        config = getConfigAs(TailwindConfiguration.class);
        if (config.hostname.isBlank()) {
            updateStatus(ThingStatusDetail.CONFIGURATION_ERROR, "@text/offline.conf-error.no-hostname");
            return;
        }
        if (config.token.isBlank()) {
            updateStatus(ThingStatusDetail.CONFIGURATION_ERROR, "@text/offline.conf-error.no-token");
            return;
        }
        updateStatus(ThingStatus.UNKNOWN);
        lastStatus = null;
        controllerAddresses = Set.of();
        synchronized (this) {
            api = new TailwindApi(httpClient, config.hostname.trim(), config.token.trim());
            retryDelaySeconds = RETRY_MIN_SECONDS;
            schedulePoll(0);
        }
    }

    @Override
    public void dispose() {
        TailwindUdpListener oldListener;
        synchronized (this) {
            api = null;
            pollGeneration++;
            ScheduledFuture<?> job = pollJob;
            if (job != null) {
                job.cancel(true);
                pollJob = null;
            }
            oldListener = listener;
            listener = null;
        }
        if (oldListener != null) {
            oldListener.stop();
        }
        lastStatus = null;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            TailwindStatus status = lastStatus;
            if (status != null) {
                updateChannels(status);
            }
            return;
        }
        TailwindApi currentApi = api;
        if (currentApi != null) {
            // Not on the caller's thread, because the request waits for the controller
            scheduler.execute(() -> sendCommand(currentApi, channelUID, command));
        }
    }

    /**
     * Returns whether the given API is the one of the current initialization, and not one that was disposed since.
     */
    private boolean isCurrent(TailwindApi currentApi) {
        // TailwindApi has no equals(), so this compares the instances
        return currentApi.equals(api);
    }

    private void sendCommand(TailwindApi currentApi, ChannelUID channelUID, Command command) {
        String group = channelUID.getGroupId();
        String channel = channelUID.getIdWithoutGroup();
        try {
            if (GROUP_CONTROLLER.equals(group)) {
                sendControllerCommand(currentApi, channelUID, channel, command);
            } else if (group != null && group.startsWith(GROUP_DOOR_PREFIX) && CHANNEL_DOOR_CONTROL.equals(channel)) {
                sendDoorCommand(currentApi, group, command);
            }
        } catch (TailwindUnauthorizedException | TailwindCommunicationException e) {
            if (isCurrent(currentApi)) {
                setOffline(e);
                pollSoon();
            }
        } catch (TailwindException e) {
            // For example a command for a door that is disabled
            logger.debug("Command {} for {} failed: {}", command, channelUID, e.getMessage());
        }
    }

    private void sendControllerCommand(TailwindApi currentApi, ChannelUID channelUID, String channel, Command command)
            throws TailwindException {
        switch (channel) {
            case CHANNEL_LED_BRIGHTNESS -> {
                PercentType brightness = command instanceof PercentType percent ? percent
                        : command instanceof OnOffType onOff ? onOff.as(PercentType.class) : null;
                if (brightness != null) {
                    currentApi.setLedBrightness(brightness.intValue());
                    updateState(channelUID, brightness);
                }
            }
            case CHANNEL_SERVER_MONITOR -> {
                if (command instanceof OnOffType onOff) {
                    currentApi.setServerMonitor(onOff == OnOffType.ON);
                    updateState(channelUID, onOff);
                }
            }
            case CHANNEL_COMMAND -> {
                switch (command.toString().toLowerCase(Locale.ROOT)) {
                    case COMMAND_IDENTIFY -> currentApi.identify();
                    case COMMAND_REBOOT -> currentApi.reboot();
                    default -> logger.debug("Unknown command {} for {}", command, channelUID);
                }
            }
            default -> {
                // The other channels are read-only
            }
        }
    }

    private void sendDoorCommand(TailwindApi currentApi, String group, Command command) throws TailwindException {
        int doorIndex = -1;
        for (int index = 0; index < MAX_DOORS; index++) {
            if ((GROUP_DOOR_PREFIX + (index + 1)).equals(group)) {
                doorIndex = index;
            }
        }
        if (doorIndex < 0) {
            return;
        }
        switch (command.toString().toLowerCase(Locale.ROOT)) {
            case DOOR_OPEN -> currentApi.openDoor(doorIndex);
            case DOOR_PARTIAL -> currentApi.openDoorPartially(doorIndex, config.partialOpenTimeMillis(doorIndex));
            case DOOR_CLOSE -> currentApi.closeDoor(doorIndex);
            default -> logger.debug("Unknown command {} for {}", command, group);
        }
    }

    /**
     * Asks the controller for its status now and schedules the next request. Package-private for tests.
     */
    void poll() {
        int generation;
        synchronized (this) {
            generation = pollGeneration;
        }
        poll(generation);
    }

    private void poll(int generation) {
        TailwindApi currentApi = api;
        if (currentApi == null) {
            return;
        }
        boolean online = false;
        try {
            String address = networkAddressService.getPrimaryIpv4HostAddress();
            if (address == null) {
                if (isCurrent(currentApi)) {
                    updateStatus(ThingStatusDetail.CONFIGURATION_ERROR, "@text/offline.conf-error.no-primary-address");
                }
            } else {
                TailwindStatus status = currentApi.getStatus();
                if (isCurrent(currentApi)) {
                    updateStatus(ThingStatus.ONLINE);
                    handleStatus(status);
                    online = true;
                }
                // After publishing the status, so that it cannot replace a newer report. Without reports, door
                // changes still show at the next status request.
                try {
                    registerNotifications(currentApi, address);
                } catch (TailwindException e) {
                    logger.debug("{} cannot receive status reports: {}", thing.getUID(), e.getMessage());
                }
            }
        } catch (TailwindException e) {
            if (isCurrent(currentApi)) {
                setOffline(e);
            }
        } catch (RuntimeException e) {
            logger.warn("Unexpected error while asking {} for its status", thing.getUID(), e);
        }
        scheduleNextPoll(generation, online);
    }

    private void setOffline(TailwindException e) {
        if (e instanceof TailwindUnauthorizedException) {
            updateStatus(ThingStatusDetail.CONFIGURATION_ERROR, "@text/offline.conf-error.token-rejected");
        } else {
            updateStatus(ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    private synchronized void scheduleNextPoll(int generation, boolean online) {
        if (generation != pollGeneration || api == null) {
            return;
        }
        int delay;
        if (online) {
            delay = POLL_INTERVAL_SECONDS;
            retryDelaySeconds = RETRY_MIN_SECONDS;
        } else {
            delay = retryDelaySeconds;
            retryDelaySeconds = Math.min(retryDelaySeconds * 2, RETRY_MAX_SECONDS);
        }
        schedulePoll(delay);
    }

    /**
     * Replaces the scheduled status request with one after the given delay.
     */
    private synchronized void schedulePoll(int delaySeconds) {
        ScheduledFuture<?> job = pollJob;
        if (job != null) {
            job.cancel(false);
        }
        int generation = ++pollGeneration;
        pollJob = scheduler.schedule(() -> poll(generation), delaySeconds, TimeUnit.SECONDS);
    }

    /**
     * Brings the next status request forward, to find out sooner when the controller can be reached again.
     */
    private synchronized void pollSoon() {
        if (api != null) {
            schedulePoll(RETRY_MIN_SECONDS);
        }
    }

    /**
     * Starts the listener for status reports if it is not running, and tells the controller where to send them.
     * This is repeated at every status request, so the controller gets the address back after it lost it, for example
     * when it rebooted.
     */
    private void registerNotifications(TailwindApi currentApi, String address) throws TailwindException {
        int port;
        synchronized (this) {
            if (!isCurrent(currentApi)) {
                return;
            }
            TailwindUdpListener currentListener = listener;
            if (currentListener == null) {
                currentListener = new TailwindUdpListener("OH-binding-" + thing.getUID() + "-udp",
                        this::handleNotification);
                listener = currentListener;
            }
            port = currentListener.getPort();
            if (port == -1) {
                try {
                    port = currentListener.start();
                } catch (SocketException e) {
                    throw new TailwindCommunicationException(
                            "Cannot open a UDP port for status reports: " + e.getMessage(), e);
                }
            }
        }
        try {
            controllerAddresses = Set.of(InetAddress.getAllByName(config.hostname.trim()));
        } catch (UnknownHostException e) {
            throw new TailwindCommunicationException("Cannot resolve " + config.hostname + ": " + e.getMessage(), e);
        }
        currentApi.setNotifyUrl(address + ":" + port);
        logger.debug("{} sends its status reports to {}:{}", thing.getUID(), address, port);
    }

    private void handleNotification(InetAddress sender, String message) {
        if (api == null) {
            return;
        }
        if (!controllerAddresses.contains(sender)) {
            logger.debug("Ignoring a status report from {}, which is not the controller", sender.getHostAddress());
            return;
        }
        TailwindStatus status = TailwindApi.parse(message);
        if (status == null) {
            logger.debug("Ignoring a status report that is not valid JSON");
            return;
        }
        // A controller that took over the address of another one
        String reportedMac = TailwindDeviceId.toMacAddress(status.deviceId);
        String mac = thing.getProperties().get(Thing.PROPERTY_MAC_ADDRESS);
        if (reportedMac == null || !reportedMac.equals(mac)) {
            logger.debug("Ignoring a status report from device {}", status.deviceId);
            return;
        }
        handleStatus(status);
    }

    private void handleStatus(TailwindStatus status) {
        lastStatus = status;
        updatePropertyIfSet(Thing.PROPERTY_MAC_ADDRESS, TailwindDeviceId.toMacAddress(status.deviceId));
        updatePropertyIfSet(Thing.PROPERTY_MODEL_ID, status.product);
        updatePropertyIfSet(Thing.PROPERTY_FIRMWARE_VERSION, status.firmwareVersion);
        Integer doorCount = status.doorCount;
        if (doorCount != null) {
            updateDoorGroups(doorCount);
        }
        updateChannels(status);
    }

    /**
     * Makes the Thing have the channels of exactly the doors that are connected to the controller.
     */
    private void updateDoorGroups(int doorCount) {
        ThingHandlerCallback callback = getCallback();
        if (callback == null || doorCount < 1 || doorCount > MAX_DOORS) {
            return;
        }
        List<Channel> channels = new ArrayList<>(thing.getChannels());
        boolean changed = false;
        for (int door = 1; door <= MAX_DOORS; door++) {
            String group = GROUP_DOOR_PREFIX + door;
            boolean present = channels.stream().anyMatch(channel -> group.equals(channel.getUID().getGroupId()));
            if (door <= doorCount && !present) {
                ChannelGroupUID groupUID = new ChannelGroupUID(thing.getUID(), group);
                for (ChannelBuilder builder : callback.createChannelBuilders(groupUID, GROUP_TYPE_DOOR)) {
                    channels.add(builder.build());
                }
                changed = true;
            } else if (door > doorCount && present) {
                channels.removeIf(channel -> group.equals(channel.getUID().getGroupId()));
                changed = true;
            }
        }
        if (changed) {
            updateThing(editThing().withChannels(channels).build());
        }
    }

    private void updateChannels(TailwindStatus status) {
        updateFlag(channel(GROUP_CONTROLLER, CHANNEL_NIGHT_MODE), status.nightModeEnabled);
        Integer ledBrightness = status.ledBrightness;
        if (ledBrightness != null) {
            updateState(channel(GROUP_CONTROLLER, CHANNEL_LED_BRIGHTNESS),
                    new PercentType(Math.max(0, Math.min(100, ledBrightness))));
        }
        Integer rssi = status.routerRssi;
        if (rssi != null) {
            updateState(channel(GROUP_CONTROLLER, CHANNEL_RSSI), new QuantityType<>(rssi, Units.DECIBEL_MILLIWATTS));
        }
        Boolean serverMonitor = status.serverMonitor;
        if (serverMonitor != null) {
            updateState(channel(GROUP_CONTROLLER, CHANNEL_SERVER_MONITOR), OnOffType.from(serverMonitor));
        }
        for (int index = 0; index < MAX_DOORS; index++) {
            TailwindDoor door = status.door(index);
            if (door == null) {
                continue;
            }
            String group = GROUP_DOOR_PREFIX + (index + 1);
            String doorStatus = door.status;
            if (doorStatus != null) {
                StringType state = new StringType(doorStatus);
                updateState(channel(group, CHANNEL_DOOR_STATUS), state);
                updateState(channel(group, CHANNEL_DOOR_CONTROL), state);
            }
            updateFlag(channel(group, CHANNEL_DOOR_LOCKED), door.lockup);
            updateFlag(channel(group, CHANNEL_DOOR_DISABLED), door.disabled);
        }
    }

    private void updatePropertyIfSet(String name, @Nullable String value) {
        if (value != null) {
            updateProperty(name, value);
        }
    }

    /**
     * Publishes a flag that the controller reports as 1 for on and 0 for off.
     */
    private void updateFlag(ChannelUID channelUID, @Nullable Integer flag) {
        if (flag != null) {
            updateState(channelUID, OnOffType.from(flag == 1));
        }
    }

    private ChannelUID channel(String group, String id) {
        return new ChannelUID(thing.getUID(), group, id);
    }
}
