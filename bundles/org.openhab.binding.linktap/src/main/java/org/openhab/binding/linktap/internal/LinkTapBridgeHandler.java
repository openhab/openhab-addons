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

import static org.openhab.binding.linktap.internal.LinkTapBindingConstants.*;
import static org.openhab.binding.linktap.protocol.frames.TLGatewayFrame.*;
import static org.openhab.binding.linktap.protocol.frames.ValidationError.Cause.BUG;
import static org.openhab.binding.linktap.protocol.frames.ValidationError.Cause.USER;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.validation.constraints.NotNull;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.linktap.configuration.LinkTapBridgeConfiguration;
import org.openhab.binding.linktap.protocol.frames.GatewayConfigResp;
import org.openhab.binding.linktap.protocol.frames.GatewayDeviceResponse;
import org.openhab.binding.linktap.protocol.frames.TLGatewayFrame;
import org.openhab.binding.linktap.protocol.frames.ValidationError;
import org.openhab.binding.linktap.protocol.http.CommandNotSupportedException;
import org.openhab.binding.linktap.protocol.http.DeviceIdException;
import org.openhab.binding.linktap.protocol.http.GatewayIdException;
import org.openhab.binding.linktap.protocol.http.InvalidParameterException;
import org.openhab.binding.linktap.protocol.http.LinkTapException;
import org.openhab.binding.linktap.protocol.http.NotTapLinkGatewayException;
import org.openhab.binding.linktap.protocol.http.TransientCommunicationIssueException;
import org.openhab.binding.linktap.protocol.http.WebServerApi;
import org.openhab.binding.linktap.protocol.servers.BindingServlet;
import org.openhab.binding.linktap.protocol.servers.IHttpClientProvider;
import org.openhab.core.cache.ExpiringCache;
import org.openhab.core.config.discovery.DiscoveryServiceRegistry;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
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
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link LinkTapBridgeHandler} class defines the handler for a LinkTapHandler
 *
 * @author David Goodyear - Initial contribution
 */
@NonNullByDefault
public class LinkTapBridgeHandler extends BaseBridgeHandler {

    public static final LookupWrapper<@Nullable LinkTapBridgeHandler> ADDR_LOOKUP = new LookupWrapper<>();
    public static final LookupWrapper<@Nullable LinkTapBridgeHandler> GW_ID_LOOKUP = new LookupWrapper<>();
    public static final LookupWrapper<@Nullable LinkTapHandler> DEV_ID_LOOKUP = new LookupWrapper<>();
    public static final LookupWrapper<@Nullable String> MDNS_LOOKUP = new LookupWrapper<>();
    private static final long MIN_TIME_BETWEEN_MDNS_SCANS_MS = 600000;

    private final DiscoveryServiceRegistry discoverySrvReg;
    private final TranslationProvider translationProvider;
    private final LocaleProvider localeProvider;
    private final Bundle bundle;
    private final Logger logger = LoggerFactory.getLogger(LinkTapBridgeHandler.class);
    private final Object schedulerLock = new Object();
    private final Object reconnectFutureLock = new Object();
    private final Object getConfigLock = new Object();

    private volatile String currentGwId = "";
    private volatile LinkTapBridgeConfiguration config = new LinkTapBridgeConfiguration();
    private volatile long lastGwCommandRecvTs = 0L;
    private volatile long lastMdnsScanMillis = -1L;

    private String bridgeKey = "";
    private IHttpClientProvider httpClientProvider;
    private @Nullable ScheduledFuture<?> backgroundGwPollingScheduler;
    private @Nullable ScheduledFuture<?> connectRepair = null;

    protected ExpiringCache<String> lastGetConfigCache = new ExpiringCache<>(Duration.ofSeconds(10),
            LinkTapBridgeHandler::expireCacheContents);

    private static @Nullable String expireCacheContents() {
        return null;
    }

    @Activate
    public LinkTapBridgeHandler(final Bridge bridge, IHttpClientProvider httpClientProvider,
            @Reference DiscoveryServiceRegistry discoveryService, @Reference TranslationProvider translationProvider,
            @Reference LocaleProvider localeProvider) {
        super(bridge);
        this.httpClientProvider = httpClientProvider;
        this.discoverySrvReg = discoveryService;
        this.translationProvider = translationProvider;
        this.localeProvider = localeProvider;
        this.bundle = FrameworkUtil.getBundle(getClass());
        TransactionProcessor.getInstance().setTranslationProviderInfo(translationProvider, localeProvider, bundle);
        WebServerApi.getInstance().setTranslationProviderInfo(translationProvider, localeProvider, bundle);
        BindingServlet.getInstance().setTranslationProviderInfo(translationProvider, localeProvider, bundle);
    }

    public String getLocalizedText(String key, @Nullable Object @Nullable... arguments) {
        String result = translationProvider.getText(bundle, key, key, localeProvider.getLocale(), arguments);
        return Objects.nonNull(result) ? result : key;
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
            final ScheduledFuture<?> ref = backgroundGwPollingScheduler;
            if (ref != null) {
                ref.cancel(true);
                backgroundGwPollingScheduler = null;
            }
        }
    }

    private void requestMdnsScan() {
        final long sysMillis = System.currentTimeMillis();
        if (lastMdnsScanMillis + MIN_TIME_BETWEEN_MDNS_SCANS_MS < sysMillis) {
            logger.debug("Requesting MDNS Scan");
            discoverySrvReg.startScan(THING_TYPE_GATEWAY, null, null);
            lastMdnsScanMillis = sysMillis;
        } else {
            logger.trace("Not requesting MDNS Scan last ran under 10 min's ago");
        }
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);
        config = getConfigAs(LinkTapBridgeConfiguration.class);
        scheduleReconnect(0);
    }

    @Override
    public void dispose() {
        cancelReconnect();
        cancelGwPolling();
        deregisterBridge(this);
        GW_ID_LOOKUP.deregisterItem(currentGwId, this, () -> {
        });
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(LinkTapDeviceDiscoveryService.class);
    }

    public @Nullable String getGatewayId() {
        return currentGwId;
    }

    private void deregisterBridge(final LinkTapBridgeHandler ref) {
        if (!bridgeKey.isEmpty()) {
            ADDR_LOOKUP.deregisterItem(bridgeKey, ref, () -> {
                BindingServlet.getInstance().unregisterServlet();
            });
            bridgeKey = "";
        }
    }

    private boolean registerBridge(final LinkTapBridgeHandler ref) {
        final WebServerApi api = WebServerApi.getInstance();
        api.setHttpClient(httpClientProvider.getHttpClient());
        try {
            final String host = getHostname();

            if (!bridgeKey.equals(host)) {
                deregisterBridge(this);
                bridgeKey = host;
            }

            if (!ADDR_LOOKUP.registerItem(bridgeKey, this, () -> {
                BindingServlet.getInstance().registerServlet();
            })) {
                return false;
            }
        } catch (UnknownHostException e) {
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
                TLGatewayFrame req = new TLGatewayFrame(CMD_GET_CONFIGURATION);
                resp = sendApiRequest(req);
                GatewayDeviceResponse respFrame = LinkTapBindingConstants.GSON.fromJson(resp,
                        GatewayDeviceResponse.class);
                // The system may not have picked up the ID before in which case - extract it from the error response
                // and re-run the request to ensure a full configuration data-set is retrieved.
                // This is normally populated as part of the sendApiRequest sequencing where the gateway id is
                // auto-added,
                // if available.
                if (req.gatewayId.isEmpty() && respFrame != null
                        && respFrame.getRes() == GatewayDeviceResponse.ResultStatus.RET_GATEWAY_ID_NOT_MATCHED) {
                    // Use the response GW_ID from the error response - to re-request with the correct ID
                    // This only happens in occasional startup race conditions, but this removes a low change
                    // bug being hit.
                    req.gatewayId = respFrame.gatewayId;
                    resp = sendApiRequest(req);
                }
                lastGetConfigCache.putValue(resp);
            }

        }

        final GatewayConfigResp gwConfig = LinkTapBindingConstants.GSON.fromJson(resp, GatewayConfigResp.class);
        if (gwConfig == null) {
            return;
        }
        currentGwId = gwConfig.gatewayId;

        final String version = gwConfig.version;
        final String volUnit = gwConfig.volumeUnit;
        final String[] devIds = gwConfig.endDevices;
        final String[] devNames = gwConfig.deviceNames;
        final Integer utcOffset = gwConfig.utfOfs;
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
        if (utcOffset != DEFAULT_INT) { // This is only in later firmwares
            final String strVal = String.valueOf(utcOffset);
            if (!strVal.equals(editProperties().get(BRIDGE_PROP_UTC_OFFSET))) {
                final Map<String, String> props = editProperties();
                props.put(BRIDGE_PROP_UTC_OFFSET, strVal);
                updateProperties(props);
            }
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
                    if (th instanceof IBridgeData bridgeData) {
                        bridgeData.handleBridgeDataUpdated();
                    }
                }
            });
        }
    }

    public String sendApiRequest(final TLGatewayFrame req) {
        final UUID uid = UUID.randomUUID();

        final WebServerApi api = WebServerApi.getInstance();
        String host = "Unresolved";
        try {
            host = getHostname();
            final boolean confirmGateway = req.command != TLGatewayFrame.CMD_GET_CONFIGURATION;
            if (confirmGateway && (host.isEmpty() || currentGwId.isEmpty())) {
                logger.warn("{}", getLocalizedText("warning.host-gw-unknown-for-cmd", host, currentGwId, req.command));
                return "";
            }
            if (req.gatewayId.isEmpty()) {
                req.gatewayId = currentGwId;
            }
            final String reqData = LinkTapBindingConstants.GSON.toJson(req);
            logger.debug("{} = APP BRIDGE -> GW -> Request {}", uid, reqData);
            final String respData = api.sendRequest(host, reqData);
            logger.debug("{} = APP BRIDGE -> GW -> Response {}", uid, respData);
            final TLGatewayFrame gwResponseFrame = LinkTapBindingConstants.GSON.fromJson(respData,
                    TLGatewayFrame.class);
            if (confirmGateway && gwResponseFrame != null && !gwResponseFrame.gatewayId.equals(req.gatewayId)) {
                logger.warn("{}", getLocalizedText("warning.response-from-wrong-gw-id", uid, req.gatewayId,
                        gwResponseFrame.gatewayId));
                return "";
            }
            if (gwResponseFrame != null && req.command != gwResponseFrame.command) {
                logger.warn("{}",
                        getLocalizedText("warning.incorrect-cmd-resp", uid, req.command, gwResponseFrame.command));
                return "";
            }
            return respData;
        } catch (NotTapLinkGatewayException e) {
            logger.warn("{}", getLocalizedText("warning.not-taplink-gw", uid, host));
        } catch (UnknownHostException e) {
            logger.warn("{}", getLocalizedText("warning.comms-issue-auto-retry", uid, e.getMessage()));
            scheduleReconnect();
        } catch (TransientCommunicationIssueException e) {
            logger.warn("{}", getLocalizedText("warning.comms-issue-auto-retry", uid, getLocalizedText(e.getI18Key())));
            scheduleReconnect();
        }
        return "";
    }

    private void connect() {
        // Check if we can resolve the remote host, if so then it can be mapped back to a bridge handler.
        // If not further communications would fail - so it's offline.
        if (!registerBridge(this)) {
            requestMdnsScan();
            scheduleReconnect();
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    getLocalizedText("bridge.error.host-not-found"));
            return;
        }

        final WebServerApi api = WebServerApi.getInstance();
        api.setHttpClient(httpClientProvider.getHttpClient());
        try {
            final Map<String, String> bridgeProps = api.getBridgeProperities(bridgeKey);
            if (!bridgeProps.isEmpty()) {
                final String readGwId = bridgeProps.get(BRIDGE_PROP_GW_ID);
                if (readGwId != null) {
                    currentGwId = readGwId;
                }
                final Map<String, String> currentProps = editProperties();
                currentProps.putAll(bridgeProps);
                updateProperties(currentProps);
            } else {
                if (!api.unlockWebInterface(bridgeKey, config.username, config.password)) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            getLocalizedText("bridge.error.check-credentials"));
                    return;
                }
            }

            getGatewayConfiguration();

            // Update the GW ID -> this bridge lookup
            GW_ID_LOOKUP.registerItem(currentGwId, this, () -> {
            });

            if (Thread.currentThread().isInterrupted()) {
                return;
            }

            @NotNull
            final String hostname = getHostname(config);

            String localServerAddr = "";
            try (Socket socket = new Socket()) {
                try {
                    socket.connect(new InetSocketAddress(hostname, 80), 1500);
                } catch (IOException e) {
                    logger.warn("{}", getLocalizedText("warning.failed-local-address-detection", e.getMessage()));
                    throw new TransientCommunicationIssueException("Local address lookup failure",
                            "exception.local-addr-lookup-failure");
                }
                localServerAddr = socket.getLocalAddress().getHostAddress();
                logger.trace("Local address for connectivity is {}", socket.getLocalAddress().getHostAddress());
            } catch (IOException e) {
                logger.trace("Failed to connect to remote device due to exception", e);
            }

            final String servletEp = BindingServlet.getServletAddress(localServerAddr,
                    getLocalizedText("warning.no-http-server-port"));
            final Optional<String> servletEpOpt = (!servletEp.isEmpty()) ? Optional.of(servletEp) : Optional.empty();
            api.configureBridge(hostname, Optional.of(config.enableMDNS), Optional.of(config.enableJSONComms),
                    servletEpOpt);
            updateStatus(ThingStatus.ONLINE);
            if (Thread.currentThread().isInterrupted()) {
                return;
            }
            startGwPolling();
            connectRepair = null;

            final Firmware firmware = new Firmware(getThing().getProperties().get(BRIDGE_PROP_GW_VER));
            if (!firmware.supportsLocalConfig()) {
                logger.warn("{}", getLocalizedText("warning.fw-update-local-config", getThing().getLabel(),
                        firmware.getRecommendedMinVer()));
            }
        } catch (InterruptedException ignored) {
        } catch (LinkTapException | NotTapLinkGatewayException e) {
            deregisterBridge(this);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    getLocalizedText("bridge.error.target-is-not-gateway"));
        } catch (TransientCommunicationIssueException e) {
            scheduleReconnect();
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    getLocalizedText("bridge.error.cannot-connect"));
        } catch (UnknownHostException e) {
            scheduleReconnect();
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    getLocalizedText("bridge.error.unknown-host"));
        }
    }

    private void scheduleReconnect() {
        scheduleReconnect(15);
    }

    public void attemptReconnectIfNeeded() {
        if (ThingStatus.OFFLINE.equals(getThing().getStatus())) {
            synchronized (reconnectFutureLock) {
                if (connectRepair != null) {
                    scheduleReconnect(0);
                }
            }
        }
    }

    private void scheduleReconnect(int seconds) {
        if (seconds < 1) {
            seconds = 1;
        }
        logger.trace("Scheduling connection re-attempt in {} seconds", seconds);
        synchronized (reconnectFutureLock) {
            cancelReconnect();
            connectRepair = scheduler.schedule(this::connect, seconds, TimeUnit.SECONDS); // Schedule a retry
        }
    }

    private void cancelReconnect() {
        synchronized (reconnectFutureLock) {
            final @Nullable ScheduledFuture<?> ref = connectRepair;
            if (ref != null) {
                ref.cancel(true);
                connectRepair = null;
            }
        }
    }

    @Override
    public void handleCommand(final ChannelUID channelUID, final Command command) {
    }

    protected @NotNull String getHostname() throws UnknownHostException {
        return getHostname(config);
    }

    private @NotNull String getHostname(final LinkTapBridgeConfiguration config) throws UnknownHostException {
        @NotNull
        String hostname = config.host;
        final String mdnsLookup = MDNS_LOOKUP.getItem(hostname);
        if (mdnsLookup != null) {
            hostname = mdnsLookup;
        }
        return InetAddress.getByName(hostname).getHostAddress();
    }

    private final Object singleCommLock = new Object();

    public String sendRequest(final TLGatewayFrame frame) throws DeviceIdException, InvalidParameterException {
        // Validate the payload is within the expected limits for the device its being sent to
        if (config.enforceProtocolLimits) {
            final Collection<ValidationError> errors = frame.getValidationErrors();
            if (!errors.isEmpty()) {
                final String bugs = errors.stream().filter(x -> x.getCause() == BUG).map(ValidationError::toString)
                        .collect(Collectors.joining(","));
                final String userDataIssues = errors.stream().filter(x -> x.getCause() == USER)
                        .map(ValidationError::toString).collect(Collectors.joining(","));
                if (!bugs.isEmpty()) {
                    logger.warn("{}",
                            getLocalizedText("bug-report.unexpected-payload-failure", getThing().getLabel(), bugs));
                }
                if (!userDataIssues.isEmpty()) {
                    logger.warn("{}", getLocalizedText("warning.user-data-payload-failure", getThing().getLabel(),
                            userDataIssues));
                }
                return "";
            }
        }
        final TransactionProcessor tp = TransactionProcessor.getInstance();
        final String gatewayId = getGatewayId();
        if (gatewayId == null) {
            logger.warn("{}", getLocalizedText("warning.error-with-gw-id"));
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
                    logger.warn("{}",
                            getLocalizedText("warning.device-no-accept", getThing().getLabel(), cnse.getMessage()));
                }
            }
        } catch (final GatewayIdException gide) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, gide.getI18Key());
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
        // Store this so that the only when necessary can polls be done - aka
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
        super.childHandlerDisposed(childHandler, childThing);
    }
}
