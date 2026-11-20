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

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.openhab.binding.tailwind.internal.api.TailwindUdpReceiver;
import org.openhab.binding.tailwind.internal.api.TailwindUdpReceiver.Registration;
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
 * The controller reports door changes over UDP. The status is also requested regularly, because other changes, such
 * as the LED brightness, are not reported.
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
    private final TailwindUdpReceiver udpReceiver;

    private TailwindConfiguration config = new TailwindConfiguration();
    // A new instance for each initialization: work holding an older one is stale
    private volatile @Nullable TailwindApi api;
    private volatile @Nullable TailwindStatus lastStatus;
    private volatile Set<InetAddress> controllerAddresses = Set.of();
    private volatile @Nullable String notifyAddress;
    // While false, the status request decides when the controller counts as reachable again
    private volatile boolean reachable;
    private @Nullable ScheduledFuture<?> pollJob;
    // A status request of an older generation does not schedule the next one
    private int pollGeneration;
    private @Nullable Registration registration;
    private String hostname = "";
    private boolean usingLastKnownAddress;
    private boolean reportsWarned;
    private int retryDelaySeconds = RETRY_MIN_SECONDS;

    public TailwindHandler(Thing thing, HttpClient httpClient, NetworkAddressService networkAddressService,
            TailwindUdpReceiver udpReceiver) {
        super(thing);
        this.httpClient = httpClient;
        this.networkAddressService = networkAddressService;
        this.udpReceiver = udpReceiver;
    }

    @Override
    public void initialize() {
        config = getConfigAs(TailwindConfiguration.class);
        String hostname = config.hostname.trim();
        if (hostname.isEmpty()) {
            updateStatus(ThingStatusDetail.CONFIGURATION_ERROR, "@text/offline.conf-error.no-hostname");
            return;
        }
        if (!TailwindConfiguration.isValidHostname(hostname)) {
            updateStatus(ThingStatusDetail.CONFIGURATION_ERROR, "@text/offline.conf-error.invalid-hostname");
            return;
        }
        if (config.token.isBlank()) {
            updateStatus(ThingStatusDetail.CONFIGURATION_ERROR, "@text/offline.conf-error.no-token");
            return;
        }
        if (config.udpPort < 0 || config.udpPort > 65535) {
            updateStatus(ThingStatusDetail.CONFIGURATION_ERROR, "@text/offline.conf-error.invalid-udp-port");
            return;
        }
        reachable = false;
        updateStatus(ThingStatus.UNKNOWN);
        lastStatus = null;
        controllerAddresses = Set.of();
        notifyAddress = null;
        synchronized (this) {
            this.hostname = hostname;
            usingLastKnownAddress = false;
            reportsWarned = false;
            api = new TailwindApi(httpClient, hostname, config.token.trim());
            retryDelaySeconds = RETRY_MIN_SECONDS;
            schedulePoll(0);
        }
    }

    // Only on removal, not on dispose: after a restart, the next status request registers again
    @Override
    public void handleRemoval() {
        TailwindApi currentApi = api;
        String registered = notifyAddress;
        if (currentApi != null && registered != null) {
            scheduler.execute(() -> {
                try {
                    currentApi.disableNotifyUrl(registered);
                } catch (TailwindException e) {
                    logger.debug("Could not stop the status reports of {}: {}", thing.getUID(), e.getMessage());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }
        super.handleRemoval();
    }

    @Override
    public void dispose() {
        Registration oldRegistration;
        synchronized (this) {
            api = null;
            pollGeneration++;
            ScheduledFuture<?> job = pollJob;
            if (job != null) {
                job.cancel(true);
                pollJob = null;
            }
            oldRegistration = registration;
            registration = null;
        }
        if (oldRegistration != null) {
            oldRegistration.close();
        }
        lastStatus = null;
        notifyAddress = null;
        reachable = false;
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
            scheduler.execute(() -> sendCommand(currentApi, channelUID, command));
        }
    }

    private boolean isCurrent(TailwindApi currentApi) {
        return currentApi.equals(api);
    }

    private void sendCommand(TailwindApi currentApi, ChannelUID channelUID, Command command) {
        if (!isCurrent(currentApi)) {
            return;
        }
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
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (TailwindException e) {
            logger.debug("Command {} for {} failed: {}", command, channelUID, e.getMessage());
        }
    }

    private void sendControllerCommand(TailwindApi currentApi, ChannelUID channelUID, String channel, Command command)
            throws TailwindException, InterruptedException {
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

    private void sendDoorCommand(TailwindApi currentApi, String group, Command command)
            throws TailwindException, InterruptedException {
        int doorIndex;
        try {
            doorIndex = Integer.parseInt(group.substring(GROUP_DOOR_PREFIX.length())) - 1;
        } catch (NumberFormatException e) {
            return;
        }
        if (doorIndex < 0 || doorIndex >= MAX_DOORS) {
            return;
        }
        switch (command.toString().toLowerCase(Locale.ROOT)) {
            case DOOR_OPEN -> currentApi.openDoor(doorIndex);
            case DOOR_PARTIAL -> currentApi.openDoorPartially(doorIndex, config.partialOpenTimeMillis(doorIndex));
            case DOOR_CLOSE -> currentApi.closeDoor(doorIndex);
            default -> logger.debug("Unknown command {} for {}", command, group);
        }
    }

    // Package-private for tests
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
            ResolvedHost resolved = resolveHost();
            currentApi.setHost(resolved.host());
            TailwindStatus status = currentApi.getStatus();
            if (!isCurrent(currentApi)) {
                return;
            }
            String reportedMac = TailwindDeviceId.toMacAddress(status.deviceId);
            String mac = thing.getProperties().get(Thing.PROPERTY_MAC_ADDRESS);
            if (reportedMac != null && mac != null && !mac.equals(reportedMac)) {
                // For example after the router gave the last known address of the controller to another one
                reachable = false;
                updateStatus(ThingStatusDetail.COMMUNICATION_ERROR,
                        "@text/offline.comm-error.other-controller [\"" + reportedMac + "\"]");
            } else {
                reachable = true;
                updateStatus(ThingStatus.ONLINE);
                handleStatus(status);
                online = true;
                // After publishing the status, so that it cannot replace a newer report
                try {
                    registerNotifications(currentApi, resolved);
                    reportsWarned = false;
                } catch (TailwindException e) {
                    if (reportsWarned) {
                        logger.debug("{} still cannot receive status reports: {}", thing.getUID(), e.getMessage());
                    } else {
                        reportsWarned = true;
                        logger.warn("{} cannot receive status reports, so door changes show only every 5 minutes: {}",
                                thing.getUID(), e.getMessage());
                    }
                }
            }
        } catch (TailwindException e) {
            if (isCurrent(currentApi)) {
                setOffline(e);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        } catch (RuntimeException e) {
            logger.warn("Unexpected error while asking {} for its status", thing.getUID(), e);
        }
        scheduleNextPoll(generation, online);
    }

    private void setOffline(TailwindException e) {
        reachable = false;
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

    private synchronized void schedulePoll(int delaySeconds) {
        ScheduledFuture<?> job = pollJob;
        if (job != null) {
            job.cancel(false);
        }
        int generation = ++pollGeneration;
        pollJob = scheduler.schedule(() -> poll(generation), delaySeconds, TimeUnit.SECONDS);
    }

    private synchronized void pollSoon() {
        if (api != null) {
            schedulePoll(RETRY_MIN_SECONDS);
        }
    }

    // Repeated at every status request, because the controller forgets the address when it reboots
    // The receiver is called without holding this handler's lock, which its receive thread needs for updateState()
    private void registerNotifications(TailwindApi currentApi, ResolvedHost resolved)
            throws TailwindException, InterruptedException {
        if (resolved.addresses().isEmpty()) {
            throw new TailwindCommunicationException("Cannot resolve " + resolved.host());
        }
        String address = networkAddressService.getPrimaryIpv4HostAddress();
        if (address == null) {
            address = localAddressTowards(resolved.addresses().iterator().next());
        }
        if (address == null) {
            throw new TailwindCommunicationException(
                    "No IPv4 address of openHAB found to receive them. Set the primary address in Settings, Network Settings.");
        }
        Registration currentRegistration;
        synchronized (this) {
            if (!isCurrent(currentApi)) {
                return;
            }
            currentRegistration = registration;
            if (currentRegistration != null && currentRegistration.getPort() == -1) {
                registration = null;
            }
        }
        if (currentRegistration != null && currentRegistration.getPort() == -1) {
            currentRegistration.close();
            currentRegistration = null;
        }
        if (currentRegistration == null) {
            Registration newRegistration;
            try {
                newRegistration = udpReceiver.register(config.udpPort, this::handleNotification);
            } catch (SocketException e) {
                throw new TailwindCommunicationException("Cannot open a UDP port for status reports: " + e.getMessage(),
                        e);
            }
            boolean kept;
            synchronized (this) {
                kept = isCurrent(currentApi) && registration == null;
                if (kept) {
                    registration = newRegistration;
                }
            }
            if (!kept) {
                newRegistration.close();
                return;
            }
            currentRegistration = newRegistration;
        }
        int port = currentRegistration.getPort();
        controllerAddresses = resolved.addresses();
        String hostAndPort = address + ":" + port;
        currentApi.setNotifyUrl(hostAndPort);
        notifyAddress = hostAndPort;
        logger.debug("{} sends its status reports to {}:{}", thing.getUID(), address, port);
    }

    private record ResolvedHost(String host, Set<InetAddress> addresses) {
    }

    // Some systems, such as some Docker setups, cannot resolve .local names
    private ResolvedHost resolveHost() {
        String configured = hostname;
        try {
            Set<InetAddress> addresses = Set.copyOf(Arrays.asList(InetAddress.getAllByName(configured)));
            addresses.stream().filter(Inet4Address.class::isInstance).findFirst()
                    .filter(address -> !configured.equals(address.getHostAddress()))
                    .ifPresent(address -> updateProperty(PROPERTY_IP_ADDRESS, address.getHostAddress()));
            if (usingLastKnownAddress) {
                usingLastKnownAddress = false;
                logger.debug("{} can resolve {} again", thing.getUID(), configured);
            }
            return new ResolvedHost(configured, addresses);
        } catch (UnknownHostException e) {
            String lastKnown = thing.getProperties().get(PROPERTY_IP_ADDRESS);
            if (lastKnown == null || lastKnown.isBlank()) {
                return new ResolvedHost(configured, Set.of());
            }
            if (!usingLastKnownAddress) {
                usingLastKnownAddress = true;
                logger.debug("{} cannot resolve {}, so it uses the last known address {} of the controller",
                        thing.getUID(), configured, lastKnown);
            }
            try {
                // An address literal, so this does not look anything up
                return new ResolvedHost(lastKnown, Set.of(InetAddress.getByName(lastKnown)));
            } catch (UnknownHostException invalid) {
                return new ResolvedHost(lastKnown, Set.of());
            }
        }
    }

    // No packet is sent: connecting a UDP socket only chooses the route
    private static @Nullable String localAddressTowards(InetAddress controller) {
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.connect(controller, 80);
            InetAddress local = socket.getLocalAddress();
            return local instanceof Inet4Address && !local.isAnyLocalAddress() ? local.getHostAddress() : null;
        } catch (IOException | RuntimeException e) {
            return null;
        }
    }

    private void handleNotification(InetAddress sender, String message) {
        if (api == null || !reachable) {
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
        logger.debug("Status report from {}: {}", sender.getHostAddress(), status.doorSummary());
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

    private void updateFlag(ChannelUID channelUID, @Nullable Integer flag) {
        if (flag != null) {
            updateState(channelUID, OnOffType.from(flag == 1));
        }
    }

    private ChannelUID channel(String group, String id) {
        return new ChannelUID(thing.getUID(), group, id);
    }
}
