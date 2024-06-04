/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.linktap.internal;

import static org.openhab.binding.linktap.internal.LinkTapBindingConstants.BRIDGE_PROP_GW_ID;
import static org.openhab.binding.linktap.internal.LinkTapBindingConstants.BRIDGE_PROP_GW_VER;
import static org.openhab.binding.linktap.internal.LinkTapBindingConstants.BRIDGE_PROP_VOL_UNIT;
import static org.openhab.binding.linktap.protocol.frames.TLGatewayFrame.CMD_DATETIME_SYNC;
import static org.openhab.binding.linktap.protocol.frames.TLGatewayFrame.CMD_GET_CONFIGURATION;
import static org.openhab.binding.linktap.protocol.frames.TLGatewayFrame.CMD_HANDSHAKE;
import static org.openhab.binding.linktap.protocol.frames.TLGatewayFrame.CMD_NOTIFICATION_WATERING_SKIPPED;
import static org.openhab.binding.linktap.protocol.frames.TLGatewayFrame.CMD_RAINFALL_DATA;
import static org.openhab.binding.linktap.protocol.frames.TLGatewayFrame.EMPTY_STRING_ARRAY;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import javax.validation.constraints.NotNull;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.linktap.configuration.LinkTapBridgeConfiguration;
import org.openhab.binding.linktap.protocol.frames.GatewayConfigResp;
import org.openhab.binding.linktap.protocol.frames.HandshakeReq;
import org.openhab.binding.linktap.protocol.frames.TLGatewayFrame;
import org.openhab.binding.linktap.protocol.http.CommandNotSupportedException;
import org.openhab.binding.linktap.protocol.http.DeviceIdException;
import org.openhab.binding.linktap.protocol.http.GatewayIdException;
import org.openhab.binding.linktap.protocol.http.InvalidParameterException;
import org.openhab.binding.linktap.protocol.http.NotTapLinkGatewayException;
import org.openhab.binding.linktap.protocol.http.TransientCommunicationIssueException;
import org.openhab.binding.linktap.protocol.http.WebServerApi;
import org.openhab.binding.linktap.protocol.servers.BindingServlet;
import org.openhab.binding.linktap.protocol.servers.IHttpClientProvider;
import org.openhab.core.cache.ExpiringCache;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link LinkTapBridgeHandler} class defines the handler for a LinkTapHandler
 *
 * @author David Goodyear - Initial contribution
 */
@NonNullByDefault
public class LinkTapBridgeHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(LinkTapBridgeHandler.class);
    private String bridgeKey = "";
    private String gwId = "";
    private IHttpClientProvider httpClientProvider;
    public static final LookupWrapper<@Nullable LinkTapBridgeHandler> ADDR_LOOKUP = new LookupWrapper<>();
    public static final LookupWrapper<@Nullable LinkTapBridgeHandler> GW_ID_LOOKUP = new LookupWrapper<>();
    public static final LookupWrapper<@Nullable LinkTapHandler> DEV_ID_LOOKUP = new LookupWrapper<>();
    public static final LookupWrapper<@Nullable String> MDNS_LOOKUP = new LookupWrapper<>();
    private final Object schedulerLock = new Object();
    private @Nullable ScheduledFuture<?> backgroundGwPollingScheduler;

    private volatile long lastGwCommandRecvTs = 0L;

    private final Object getConfigLock = new Object();

    protected ExpiringCache<String> lastGetConfigCache = new ExpiringCache<>(Duration.ofSeconds(10),
            LinkTapBridgeHandler::expireCacheContents);

    private static @Nullable String expireCacheContents() {
        return null;
    }

    public LinkTapBridgeHandler(final Bridge bridge, @NotNull IHttpClientProvider httpClientProvider) {
        super(bridge);
        this.httpClientProvider = httpClientProvider;
    }

    private void startGwPolling() {
        synchronized (schedulerLock) {
            cancelGwPolling();
            backgroundGwPollingScheduler = scheduler.scheduleWithFixedDelay(() -> {
                if (lastGwCommandRecvTs + 120000 < System.currentTimeMillis()) {
                    getGatewayConfiguration();
                }
            }, 5000, 120000, TimeUnit.MILLISECONDS);
        }
    }

    private void cancelGwPolling() {
        synchronized (schedulerLock) {
            ScheduledFuture<?> ref = backgroundGwPollingScheduler;
            if (ref != null) {
                ref.cancel(true);
                backgroundGwPollingScheduler = null;
            }
        }
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);
        scheduler.execute(this::connect);
    }

    @Override
    public void dispose() {
        cancelGwPolling();
        deregisterBridge(this);
        GW_ID_LOOKUP.deregisterItem(gwId, this, () -> {
        });
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(LinkTapDeviceDiscoveryService.class);
    }

    public @Nullable String getGatewayId() {
        return editProperties().get(BRIDGE_PROP_GW_ID);
    }

    private void deregisterBridge(final LinkTapBridgeHandler ref) {
        if (!bridgeKey.isEmpty()) {
            ADDR_LOOKUP.deregisterItem(bridgeKey, ref, () -> {
                BindingServlet.getInstance().unregisterServlet();
            });
            bridgeKey = "";
        }
    }

    public String getHost() {
        final LinkTapBridgeConfiguration config = getConfigAs(LinkTapBridgeConfiguration.class);
        return config.host;
    }

    private boolean registerBridge(final LinkTapBridgeHandler ref) {
        final WebServerApi api = WebServerApi.getInstance();
        api.setHttpClient(httpClientProvider.getHttpClient());
        final LinkTapBridgeConfiguration config = getConfigAs(LinkTapBridgeConfiguration.class);
        try {
            final InetAddress ip = InetAddress.getByName(new URL("http://" + config.host).getHost());
            final String newHost = ip.getHostAddress();
            if (!bridgeKey.equals(newHost)) {
                deregisterBridge(this);
                bridgeKey = newHost;
            }

            if (!ADDR_LOOKUP.registerItem(bridgeKey, this, () -> {
                BindingServlet.getInstance().registerServlet();
            })) {
                return false;
            }
        } catch (MalformedURLException | UnknownHostException e) {
            deregisterBridge(this);
            return false;
        }
        return true;
    }

    public void getGatewayConfiguration() {
        String resp = "";
        synchronized (getConfigLock) {
            resp = lastGetConfigCache.getValue();
            if (lastGetConfigCache.isExpired() || resp == null || resp.isBlank()) {
                resp = sendApiRequest(new TLGatewayFrame(CMD_GET_CONFIGURATION));
                lastGetConfigCache.putValue(resp);
            }
        }

        final GatewayConfigResp gwConfig = LinkTapBindingConstants.GSON.fromJson(resp, GatewayConfigResp.class);
        if (gwConfig == null) {
            return;
        }
        final String version = gwConfig.version;
        final String volUnit = gwConfig.volumeUnit;
        final String[] devIds = gwConfig.endDevices;
        final String[] devNames = gwConfig.deviceNames;
        if (!version.equals(editProperties().get(BRIDGE_PROP_GW_VER))) {
            final Map<String, String> props = editProperties();
            props.put(BRIDGE_PROP_GW_VER, version);
            updateProperties(props);
            return;
        }
        if (!volUnit.equals(editProperties().get(BRIDGE_PROP_VOL_UNIT))) {
            final Map<String, String> props = editProperties();
            props.put(BRIDGE_PROP_VOL_UNIT, volUnit);
            updateProperties(props);
        }

        boolean updatedDeviceInfo = devIds.length != discoveredDevices.size();

        for (int i = 0; i < devIds.length; ++i) {
            LinkTapDeviceMetadata deviceInfo = new LinkTapDeviceMetadata(devIds[i], devNames[i]);
            LinkTapDeviceMetadata replaced = discoveredDevices.put(deviceInfo.deviceId, deviceInfo);
            if (replaced != null
                    && (!replaced.deviceId.equals(devIds[i]) || !replaced.deviceName.equals(devNames[i]))) {
                updatedDeviceInfo = true;
            }
        }

        handlers.forEach(x -> x.handleMetadataRetrieved(this));

        if (updatedDeviceInfo) {
            this.scheduler.execute(() -> {
                for (Thing el : getThing().getThings()) {
                    final ThingHandler th = el.getHandler();
                    if (th instanceof IBridgeData) {
                        ((IBridgeData) th).handleBridgeDataUpdated();
                    }
                }
            });
        }
    }

    public String sendApiRequest(final TLGatewayFrame req) {
        final UUID uid = UUID.randomUUID();

        final WebServerApi api = WebServerApi.getInstance();
        final String host = getHost();
        final String gwId = getGatewayId();
        if (host.isEmpty() || gwId == null) {
            return "";
        }
        req.gatewayId = gwId;
        try {
            final String reqData = LinkTapBindingConstants.GSON.toJson(req);
            logger.debug("{} = APP BRIDGE -> GW -> Request {}", uid, reqData);
            final String respData = api.sendRequest(host, reqData);
            logger.debug("{} = APP BRIDGE -> GW -> Response {}", uid, respData);
            final TLGatewayFrame gwResponseFrame = LinkTapBindingConstants.GSON.fromJson(respData,
                    TLGatewayFrame.class);
            if (gwResponseFrame != null && !gwResponseFrame.gatewayId.equals(req.gatewayId)) {
                logger.warn("{} = Response from incorrect Gateway \"{}\" != \"{}\"", uid, req.gatewayId,
                        gwResponseFrame.gatewayId);
                return "";
            }
            if (gwResponseFrame != null && req.command != gwResponseFrame.command) {
                logger.warn("{} = Received incorrect CMD response {} != {}", uid, req.command, gwResponseFrame.command);
                return "";
            }
            return respData;
        } catch (NotTapLinkGatewayException e) {
            logger.warn("{} = {} is not a Link Tap Gateway!", uid, host);
        } catch (TransientCommunicationIssueException e) {
            logger.warn("{} = Possible communications issue (auto retry): {}", uid, e.getMessage());
        }
        return "";
    }

    private void connect() {
        final LinkTapBridgeConfiguration config = getConfigAs(LinkTapBridgeConfiguration.class);
        // Check if we can resolve the remote host, if so then it can be mapped back to a bridge handler.
        // If not further communications would fail - so its offline.
        if (!registerBridge(this)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Hostname / IP cannot be found");
            return;
        }
        final WebServerApi api = WebServerApi.getInstance();
        api.setHttpClient(httpClientProvider.getHttpClient());
        try {
            final Map<String, String> bridgeProps = api.getBridgeProperities(bridgeKey);
            if (!bridgeProps.isEmpty()) {
                final Map<String, String> currentProps = editProperties();
                currentProps.putAll(bridgeProps);
                updateProperties(currentProps);
            } else {
                if (!api.unlockWebInterface(bridgeKey, config.username, config.password)) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "Check credentials provided");
                    return;
                }
            }
            // Update the GW ID -> this bridge lookup
            String newId = getGatewayId();
            if (newId != null) {
                gwId = newId;
                GW_ID_LOOKUP.registerItem(gwId, this, () -> {
                });
            }
            getGatewayConfiguration();

            String localServerAddr = "";
            try (Socket socket = new Socket()) {
                try {
                    socket.connect(new InetSocketAddress(config.host, 80));
                } catch (IOException e) {
                    logger.warn("Failed to connect to remote device due to exception", e);
                    return;
                }
                localServerAddr = socket.getLocalAddress().getHostAddress();
                logger.trace("Local address for connectivity is {}", socket.getLocalAddress().getHostAddress());
            } catch (IOException e) {
                logger.warn("Failed to connect to remote device due to exception", e);
            }

            final String servletEp = BindingServlet.getServletAddress(localServerAddr);
            final Optional<String> servletEpOpt = (!servletEp.isEmpty()) ? Optional.of(servletEp) : Optional.empty();

            @NotNull
            final String hostname = getHostname(config);

            api.configureBridge(hostname, Optional.of(Boolean.TRUE), servletEpOpt);
            updateStatus(ThingStatus.ONLINE);
            startGwPolling();
        } catch (NotTapLinkGatewayException e) {
            deregisterBridge(this);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Target Host is not a LinkTap Gateway");
        } catch (TransientCommunicationIssueException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Cannot connect to LinkTap Gateway");
        }
    }

    @Override
    public void handleCommand(final ChannelUID channelUID, final Command command) {
    }

    protected @NotNull String getHostname() {
        final LinkTapBridgeConfiguration config = getConfigAs(LinkTapBridgeConfiguration.class);
        return getHostname(config);
    }

    private @NotNull String getHostname(final LinkTapBridgeConfiguration config) {
        @NotNull
        String hostname = config.host;
        final String mdnsLookup = MDNS_LOOKUP.getItem(config.host);
        if (mdnsLookup != null) {
            hostname = mdnsLookup;
        }
        return hostname;
    }

    public Map<String, String> getMetadataProperities(final @Nullable HandshakeReq handshake) {
        if (handshake == null) {
            return Map.of();
        }
        final Map<String, String> newProps = new HashMap<>(3);
        newProps.put(BRIDGE_PROP_GW_ID, handshake.gatewayId);
        newProps.put(BRIDGE_PROP_GW_VER, handshake.version);
        newProps.put(BRIDGE_PROP_VOL_UNIT, "?");
        return newProps;
    }

    private final Object singleCommLock = new Object();

    public String sendRequest(final TLGatewayFrame frame) throws DeviceIdException, InvalidParameterException {
        // Validate the payload is within the expected limits for the device its being sent to
        // if (!frame.isValid()) {
        // logger.warn("Payload validation failed - will not send");
        // return "";
        // }
        final TransactionProcessor tp = TransactionProcessor.getInstance();
        final String gatewayId = getGatewayId();
        if (gatewayId == null) {
            logger.warn("Error with gateway ID");
            return "";
        }
        frame.gatewayId = gatewayId;
        // The gateway is a single device that may have to do RF, limit the comm's to ensure
        // it can maintain a good QoS. Responses for most commands are very fast on a reasonable network.
        try {
            synchronized (singleCommLock) {
                try {
                    return tp.sendRequest(this, frame);
                } catch (final CommandNotSupportedException cnse) {
                    logger.warn("Device {} did not accept command {}", getThing().getLabel(), cnse.getMessage());
                }
            }
        } catch (final GatewayIdException gide) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, gide.getMessage());
        }
        return "";
    }

    public ThingUID getUID() {
        return thing.getUID();
    }

    /**
     * Discovery handling of Gateway owned Devices
     */

    public void registerMetaDataUpdatedHandler(DeviceMetaDataUpdatedHandler dmduh) {
        handlers.add(dmduh);
    }

    public void unregisterMetaDataUpdatedHandler(DeviceMetaDataUpdatedHandler dmduh) {
        handlers.remove(dmduh);
    }

    private final CopyOnWriteArrayList<DeviceMetaDataUpdatedHandler> handlers = new CopyOnWriteArrayList<>();

    private ConcurrentMap<String, LinkTapDeviceMetadata> discoveredDevices = new ConcurrentHashMap<>();

    public final Stream<LinkTapDeviceMetadata> getDiscoveredDevices() {
        return discoveredDevices.values().stream();
    }

    public final Map<String, LinkTapDeviceMetadata> getDeviceLookup() {
        return discoveredDevices;
    }

    public void processGatewayCommand(final int commandId, final String frame) {
        logger.debug("{} processing gateway request with command {}", this.getThing().getLabel(), commandId);
        // Store this so that the only when necessary can poll's be done - aka
        // no direct from Gateway communications.
        lastGwCommandRecvTs = System.currentTimeMillis();
        switch (commandId) {
            case CMD_HANDSHAKE:
                lastGetConfigCache.invalidateValue();
                processCommand0(frame);
                break;
            case CMD_RAINFALL_DATA:
            case CMD_NOTIFICATION_WATERING_SKIPPED:
            case CMD_DATETIME_SYNC:
                logger.debug("No implementation for command {} for processing the GW request", commandId);
        }
    }

    private void processCommand0(final String request) {
        final GatewayConfigResp decoded = LinkTapBindingConstants.GSON.fromJson(request, GatewayConfigResp.class);

        // Check the current version property matches and if not update it
        final String currentVerKnown = editProperties().get(BRIDGE_PROP_GW_VER);
        if (decoded != null && currentVerKnown != null && !decoded.version.isEmpty()) {
            if (!currentVerKnown.equals(decoded.version)) {
                final Map<String, String> currentProps = editProperties();
                currentProps.put(BRIDGE_PROP_GW_VER, decoded.version);
                updateProperties(currentProps);
            }
        }
        final String currentVolUnit = editProperties().get(BRIDGE_PROP_VOL_UNIT);
        if (decoded != null && currentVolUnit != null && !decoded.volumeUnit.isEmpty()) {
            if (!currentVolUnit.equals(decoded.volumeUnit)) {
                final Map<String, String> currentProps = editProperties();
                currentProps.put(BRIDGE_PROP_VOL_UNIT, decoded.volumeUnit);
                updateProperties(currentProps);
            }
        }
        final String[] devices = decoded != null ? decoded.endDevices : EMPTY_STRING_ARRAY;
        // Go through all the device ID's returned check we know about them.
        // If not a background scan should be done
        boolean fullScanRequired = false;
        if (discoveredDevices.size() != devices.length) {
            fullScanRequired = true;
        }
        if (!discoveredDevices.keySet().containsAll(Arrays.stream(devices).toList())) {
            fullScanRequired = true;
        }
        if (fullScanRequired) {
            logger.trace("The configured devices have changed a full scan should be run");
            scheduler.execute(this::getGatewayConfiguration);
        }
    }

    @Override
    public void childHandlerDisposed(ThingHandler childHandler, Thing childThing) {
        scheduler.execute(this::getGatewayConfiguration);
    }
}
