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
package org.openhab.binding.freebox.internal.handler;

import static org.openhab.binding.freebox.internal.FreeboxBindingConstants.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.openhab.binding.freebox.internal.FreeboxDataListener;
import org.openhab.binding.freebox.internal.api.FreeboxApiManager;
import org.openhab.binding.freebox.internal.api.FreeboxException;
import org.openhab.binding.freebox.internal.api.model.FreeboxAirMediaReceiver;
import org.openhab.binding.freebox.internal.api.model.FreeboxConnectionStatus;
import org.openhab.binding.freebox.internal.api.model.FreeboxDiscoveryResponse;
import org.openhab.binding.freebox.internal.api.model.FreeboxLanHost;
import org.openhab.binding.freebox.internal.api.model.FreeboxLcdConfig;
import org.openhab.binding.freebox.internal.api.model.FreeboxSambaConfig;
import org.openhab.binding.freebox.internal.api.model.FreeboxSystemConfig;
import org.openhab.binding.freebox.internal.config.FreeboxServerConfiguration;
import org.openhab.binding.freebox.internal.discovery.FreeboxDiscoveryService;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link FreeboxHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Gaël L'hopital - Initial contribution
 * @author Laurent Garnier - updated to a bridge handler and delegate few things to another handler
 * @author Laurent Garnier - update discovery configuration
 * @author Laurent Garnier - use new internal API manager
 */
public class FreeboxHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(FreeboxHandler.class);

    private ScheduledFuture<?> authorizeJob;
    private ScheduledFuture<?> globalJob;
    private FreeboxApiManager apiManager;
    private long uptime;
    private List<FreeboxDataListener> dataListeners = new CopyOnWriteArrayList<>();
    private FreeboxServerConfiguration configuration;

    public FreeboxHandler(Bridge bridge) {
        super(bridge);

        Bundle bundle = FrameworkUtil.getBundle(getClass());
        String appId = bundle.getSymbolicName();
        String appName = bundle.getHeaders().get("Bundle-Name");
        String appVersion = String.format("%d.%d", bundle.getVersion().getMajor(), bundle.getVersion().getMinor());
        String deviceName = bundle.getHeaders().get("Bundle-Vendor");
        this.apiManager = new FreeboxApiManager(appId, appName, appVersion, deviceName);
        uptime = -1;
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singleton(FreeboxDiscoveryService.class);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            return;
        }
        if (getThing().getStatus() == ThingStatus.UNKNOWN || (getThing().getStatus() == ThingStatus.OFFLINE
                && getThing().getStatusInfo().getStatusDetail() == ThingStatusDetail.CONFIGURATION_ERROR)) {
            return;
        }
        switch (channelUID.getId()) {
            case LCDBRIGHTNESS:
                setBrightness(channelUID, command);
                break;
            case LCDORIENTATION:
                setOrientation(channelUID, command);
                break;
            case LCDFORCED:
                setForced(channelUID, command);
                break;
            case WIFISTATUS:
                setWifiStatus(channelUID, command);
                break;
            case FTPSTATUS:
                setFtpStatus(channelUID, command);
                break;
            case AIRMEDIASTATUS:
                setAirMediaStatus(channelUID, command);
                break;
            case UPNPAVSTATUS:
                setUPnPAVStatus(channelUID, command);
                break;
            case SAMBAFILESTATUS:
                setSambaFileStatus(channelUID, command);
                break;
            case SAMBAPRINTERSTATUS:
                setSambaPrinterStatus(channelUID, command);
                break;
            case REBOOT:
                reboot(channelUID, command);
                break;
            default:
                logger.debug("Thing {}: unexpected command {} from channel {}", getThing().getUID(), command,
                        channelUID.getId());
                break;
        }
    }

    @Override
    public void initialize() {
        logger.debug("initializing Freebox Server handler for thing {}", getThing().getUID());

        configuration = getConfigAs(FreeboxServerConfiguration.class);

        if (configuration.fqdn != null && !configuration.fqdn.isEmpty()) {
            if (configuration.appToken == null || configuration.appToken.isEmpty()) {
                updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.CONFIGURATION_PENDING,
                        "Please accept pairing request directly on your freebox");
            } else {
                updateStatus(ThingStatus.UNKNOWN);
            }

            logger.debug("Binding will schedule a job to establish a connection...");
            if (authorizeJob == null || authorizeJob.isCancelled()) {
                authorizeJob = scheduler.schedule(() -> {
                    try {
                        authorize();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }, 1, TimeUnit.SECONDS);
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Freebox Server FQDN not set in the thing configuration");
        }
    }

    private void pollServerState() {
        logger.debug("Polling server state...");

        boolean commOk = true;
        commOk &= fetchSystemConfig();
        commOk &= fetchLCDConfig();
        commOk &= fetchWifiConfig();
        commOk &= (fetchxDslStatus() || fetchFtthPresent());
        commOk &= fetchConnectionStatus();
        commOk &= fetchFtpConfig();
        commOk &= fetchAirMediaConfig();
        commOk &= fetchUPnPAVConfig();
        commOk &= fetchSambaConfig();
        List<FreeboxLanHost> lanHosts = fetchLanHosts();
        commOk &= (lanHosts != null);
        List<FreeboxAirMediaReceiver> airPlayDevices = fetchAirPlayDevices();
        commOk &= (airPlayDevices != null);

        // Trigger a new discovery of things
        for (FreeboxDataListener dataListener : dataListeners) {
            dataListener.onDataFetched(getThing().getUID(), lanHosts, airPlayDevices);
        }

        if (commOk) {
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        }
    }

    private void authorize() throws InterruptedException {
        logger.debug("Authorize job...");

        String fqdn = configuration.fqdn;
        FreeboxDiscoveryResponse result = null;
        boolean httpsRequestOk = false;
        if (!Boolean.TRUE.equals(configuration.useOnlyHttp)) {
            result = apiManager.checkApi(fqdn, true);
            httpsRequestOk = (result != null);
        }
        if (!httpsRequestOk) {
            result = apiManager.checkApi(fqdn, false);
        }
        String apiBaseUrl = result == null ? null : result.getApiBaseUrl();
        String apiVersion = result == null ? null : result.getApiVersion();
        String deviceType = result == null ? null : result.getDeviceType();
        String apiDomain = result == null ? null : result.getApiDomain();
        Integer httpsPort = result == null ? null : result.getHttpsPort();
        boolean useHttps = false;
        String errorMsg = null;
        if (result == null) {
            errorMsg = "Can't connect to " + fqdn;
        } else if (apiBaseUrl == null || apiBaseUrl.isEmpty()) {
            errorMsg = fqdn + " does not deliver any API base URL";
        } else if (apiVersion == null || apiVersion.isEmpty()) {
            errorMsg = fqdn + " does not deliver any API version";
        } else if (Boolean.TRUE.equals(result.isHttpsAvailable()) && !Boolean.TRUE.equals(configuration.useOnlyHttp)) {
            if (httpsPort == null || apiDomain == null || apiDomain.isEmpty()) {
                if (httpsRequestOk) {
                    useHttps = true;
                } else {
                    logger.debug("{} does not deliver API domain or HTTPS port; use HTTP API", fqdn);
                }
            } else if (apiManager.checkApi(String.format("%s:%d", apiDomain, httpsPort), true) != null) {
                useHttps = true;
                fqdn = String.format("%s:%d", apiDomain, httpsPort);
            }
        }

        if (errorMsg != null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, errorMsg);
        } else if (!apiManager.authorize(useHttps, fqdn, apiBaseUrl, apiVersion, configuration.appToken)) {
            if (configuration.appToken == null || configuration.appToken.isEmpty()) {
                errorMsg = "Pairing request rejected or timeout";
            } else {
                errorMsg = "Check your app token in the thing configuration; opening session with " + fqdn + " using "
                        + (useHttps ? "HTTPS" : "HTTP") + " API version " + apiVersion + " failed";
            }
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, errorMsg);
        } else {
            logger.debug("Thing {}: session opened with {} using {} API version {}", getThing().getUID(), fqdn,
                    (useHttps ? "HTTPS" : "HTTP"), apiVersion);
            String appToken = apiManager.getAppToken();
            if ((configuration.appToken == null || configuration.appToken.isEmpty()) && appToken != null) {
                logger.debug("Store new app token in the thing configuration");
                configuration.appToken = appToken;
                Configuration thingConfig = editConfiguration();
                thingConfig.put(FreeboxServerConfiguration.APP_TOKEN, appToken);
                updateConfiguration(thingConfig);
            }
            updateStatus(ThingStatus.ONLINE);
            if (globalJob == null || globalJob.isCancelled()) {
                long pollingInterval = configuration.refreshInterval;
                logger.debug("Scheduling server state update every {} seconds...", pollingInterval);
                globalJob = scheduler.scheduleWithFixedDelay(() -> {
                    try {
                        pollServerState();
                    } catch (Exception e) {
                        logger.debug("Server state job failed: {}", e.getMessage(), e);
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
                    }
                }, 1, pollingInterval, TimeUnit.SECONDS);
            }
        }

        Map<String, String> properties = editProperties();
        if (apiBaseUrl != null && !apiBaseUrl.isEmpty()) {
            properties.put(API_BASE_URL, apiBaseUrl);
        }
        if (apiVersion != null && !apiVersion.isEmpty()) {
            properties.put(API_VERSION, apiVersion);
        }
        if (deviceType != null && !deviceType.isEmpty()) {
            properties.put(Thing.PROPERTY_HARDWARE_VERSION, deviceType);
        }
        updateProperties(properties);
    }

    @Override
    public void dispose() {
        logger.debug("Disposing Freebox Server handler for thing {}", getThing().getUID());
        if (authorizeJob != null && !authorizeJob.isCancelled()) {
            authorizeJob.cancel(true);
            authorizeJob = null;
        }
        if (globalJob != null && !globalJob.isCancelled()) {
            globalJob.cancel(true);
            globalJob = null;
        }
        apiManager.closeSession();
        super.dispose();
    }

    public FreeboxApiManager getApiManager() {
        return apiManager;
    }

    public String getAppToken() {
        return configuration == null ? null : configuration.appToken;
    }

    public boolean registerDataListener(FreeboxDataListener dataListener) {
        if (dataListener == null) {
            throw new IllegalArgumentException("It's not allowed to pass a null dataListener.");
        }
        return dataListeners.add(dataListener);
    }

    public boolean unregisterDataListener(FreeboxDataListener dataListener) {
        if (dataListener == null) {
            throw new IllegalArgumentException("It's not allowed to pass a null dataListener.");
        }
        return dataListeners.remove(dataListener);
    }

    private boolean fetchConnectionStatus() {
        try {
            FreeboxConnectionStatus connectionStatus = apiManager.getConnectionStatus();
            String state = connectionStatus.getState();
            if (state != null && !state.isEmpty()) {
                updateChannelStringState(LINESTATUS, state);
            }
            String ipv4 = connectionStatus.getIpv4();
            if (ipv4 != null && !ipv4.isEmpty()) {
                updateChannelStringState(IPV4, ipv4);
            }
            updateChannelDecimalState(RATEUP, connectionStatus.getRateUp());
            updateChannelDecimalState(RATEDOWN, connectionStatus.getRateDown());
            updateChannelDecimalState(BYTESUP, connectionStatus.getBytesUp());
            updateChannelDecimalState(BYTESDOWN, connectionStatus.getBytesDown());
            return true;
        } catch (FreeboxException e) {
            logger.debug("Thing {}: exception in fetchConnectionStatus: {}", getThing().getUID(), e.getMessage(), e);
            return false;
        }
    }

    private boolean fetchxDslStatus() {
        try {
            String status = apiManager.getxDslStatus();
            if (status != null && !status.isEmpty()) {
                updateChannelStringState(XDSLSTATUS, status);
            }
            return true;
        } catch (FreeboxException e) {
            logger.debug("Thing {}: exception in fetchxDslStatus: {}", getThing().getUID(), e.getMessage(), e);
            return false;
        }
    }

    private boolean fetchFtthPresent() {
        try {
            boolean status = apiManager.getFtthPresent();
            updateChannelSwitchState(FTTHSTATUS, status);
            return status;
        } catch (FreeboxException e) {
            logger.debug("Thing {}: exception in fetchxFtthStatus: {}", getThing().getUID(), e.getMessage(), e);
            return false;
        }
    }

    private boolean fetchWifiConfig() {
        try {
            updateChannelSwitchState(WIFISTATUS, apiManager.isWifiEnabled());
            return true;
        } catch (FreeboxException e) {
            logger.debug("Thing {}: exception in fetchWifiConfig: {}", getThing().getUID(), e.getMessage(), e);
            return false;
        }
    }

    private boolean fetchFtpConfig() {
        try {
            updateChannelSwitchState(FTPSTATUS, apiManager.isFtpEnabled());
            return true;
        } catch (FreeboxException e) {
            logger.debug("Thing {}: exception in fetchFtpConfig: {}", getThing().getUID(), e.getMessage(), e);
            return false;
        }
    }

    private boolean fetchAirMediaConfig() {
        try {
            if (!apiManager.isInLanBridgeMode()) {
                updateChannelSwitchState(AIRMEDIASTATUS, apiManager.isAirMediaEnabled());
            }
            return true;
        } catch (FreeboxException e) {
            logger.debug("Thing {}: exception in fetchAirMediaConfig: {}", getThing().getUID(), e.getMessage(), e);
            return false;
        }
    }

    private boolean fetchUPnPAVConfig() {
        try {
            if (!apiManager.isInLanBridgeMode()) {
                updateChannelSwitchState(UPNPAVSTATUS, apiManager.isUPnPAVEnabled());
            }
            return true;
        } catch (FreeboxException e) {
            logger.debug("Thing {}: exception in fetchUPnPAVConfig: {}", getThing().getUID(), e.getMessage(), e);
            return false;
        }
    }

    private boolean fetchSambaConfig() {
        try {
            FreeboxSambaConfig config = apiManager.getSambaConfig();
            updateChannelSwitchState(SAMBAFILESTATUS, config.isFileShareEnabled());
            updateChannelSwitchState(SAMBAPRINTERSTATUS, config.isPrintShareEnabled());
            return true;
        } catch (FreeboxException e) {
            logger.debug("Thing {}: exception in fetchSambaConfig: {}", getThing().getUID(), e.getMessage(), e);
            return false;
        }
    }

    private boolean fetchLCDConfig() {
        try {
            FreeboxLcdConfig config = apiManager.getLcdConfig();
            updateChannelDecimalState(LCDBRIGHTNESS, config.getBrightness());
            updateChannelDecimalState(LCDORIENTATION, config.getOrientation());
            updateChannelSwitchState(LCDFORCED, config.isOrientationForced());
            return true;
        } catch (FreeboxException e) {
            logger.debug("Thing {}: exception in fetchLCDConfig: {}", getThing().getUID(), e.getMessage(), e);
            return false;
        }
    }

    private boolean fetchSystemConfig() {
        try {
            FreeboxSystemConfig config = apiManager.getSystemConfig();
            Map<String, String> properties = editProperties();
            String value = config.getSerial();
            if (value != null && !value.isEmpty()) {
                properties.put(Thing.PROPERTY_SERIAL_NUMBER, value);
            }
            value = config.getBoardName();
            if (value != null && !value.isEmpty()) {
                properties.put(Thing.PROPERTY_HARDWARE_VERSION, value);
            }
            value = config.getFirmwareVersion();
            if (value != null && !value.isEmpty()) {
                properties.put(Thing.PROPERTY_FIRMWARE_VERSION, value);
                updateChannelStringState(FWVERSION, value);
            }
            value = config.getMac();
            if (value != null && !value.isEmpty()) {
                properties.put(Thing.PROPERTY_MAC_ADDRESS, value);
            }
            updateProperties(properties);

            long newUptime = config.getUptimeVal();
            updateChannelSwitchState(RESTARTED, newUptime < uptime);
            uptime = newUptime;

            updateChannelDecimalState(UPTIME, uptime);
            updateChannelDecimalState(TEMPCPUM, config.getTempCpum());
            updateChannelDecimalState(TEMPCPUB, config.getTempCpub());
            updateChannelDecimalState(TEMPSWITCH, config.getTempSw());
            updateChannelDecimalState(FANSPEED, config.getFanRpm());
            return true;
        } catch (FreeboxException e) {
            logger.debug("Thing {}: exception in fetchSystemConfig: {}", getThing().getUID(), e.getMessage(), e);
            return false;
        }
    }

    private synchronized List<FreeboxLanHost> fetchLanHosts() {
        try {
            List<FreeboxLanHost> hosts = apiManager.getLanHosts();
            if (hosts == null) {
                hosts = new ArrayList<>();
            }

            // The update of channels is delegated to each thing handler
            for (Thing thing : getThing().getThings()) {
                ThingHandler handler = thing.getHandler();
                if (handler instanceof FreeboxThingHandler) {
                    ((FreeboxThingHandler) handler).updateNetInfo(hosts);
                }
            }

            return hosts;
        } catch (FreeboxException e) {
            logger.debug("Thing {}: exception in fetchLanHosts: {}", getThing().getUID(), e.getMessage(), e);
            return null;
        }
    }

    private synchronized List<FreeboxAirMediaReceiver> fetchAirPlayDevices() {
        try {
            List<FreeboxAirMediaReceiver> devices = apiManager.getAirMediaReceivers();
            if (devices == null) {
                devices = new ArrayList<>();
            }

            // The update of channels is delegated to each thing handler
            for (Thing thing : getThing().getThings()) {
                ThingHandler handler = thing.getHandler();
                if (handler instanceof FreeboxThingHandler) {
                    ((FreeboxThingHandler) handler).updateAirPlayDevice(devices);
                }
            }

            return devices;
        } catch (FreeboxException e) {
            logger.debug("Thing {}: exception in fetchAirPlayDevices: {}", getThing().getUID(), e.getMessage(), e);
            return null;
        }
    }

    private void setBrightness(ChannelUID channelUID, Command command) {
        try {
            if (command instanceof IncreaseDecreaseType) {
                if (command == IncreaseDecreaseType.INCREASE) {
                    updateChannelDecimalState(LCDBRIGHTNESS, apiManager.increaseLcdBrightness());
                } else {
                    updateChannelDecimalState(LCDBRIGHTNESS, apiManager.decreaseLcdBrightness());
                }
            } else if (command instanceof OnOffType) {
                updateChannelDecimalState(LCDBRIGHTNESS,
                        apiManager.setLcdBrightness((command == OnOffType.ON) ? 100 : 0));
            } else if (command instanceof DecimalType) {
                updateChannelDecimalState(LCDBRIGHTNESS,
                        apiManager.setLcdBrightness(((DecimalType) command).intValue()));
            } else if (command instanceof PercentType) {
                updateChannelDecimalState(LCDBRIGHTNESS,
                        apiManager.setLcdBrightness(((PercentType) command).intValue()));
            } else {
                logger.debug("Thing {}: invalid command {} from channel {}", getThing().getUID(), command,
                        channelUID.getId());
            }
        } catch (FreeboxException e) {
            logCommandException(e, channelUID, command);
            fetchLCDConfig();
        }
    }

    private void setOrientation(ChannelUID channelUID, Command command) {
        if (command instanceof DecimalType) {
            try {
                FreeboxLcdConfig config = apiManager.setLcdOrientation(((DecimalType) command).intValue());
                updateChannelDecimalState(LCDORIENTATION, config.getOrientation());
                updateChannelSwitchState(LCDFORCED, config.isOrientationForced());
            } catch (FreeboxException e) {
                logCommandException(e, channelUID, command);
                fetchLCDConfig();
            }
        } else {
            logger.debug("Thing {}: invalid command {} from channel {}", getThing().getUID(), command,
                    channelUID.getId());
        }
    }

    private void setForced(ChannelUID channelUID, Command command) {
        if (command instanceof OnOffType || command instanceof OpenClosedType || command instanceof UpDownType) {
            try {
                updateChannelSwitchState(LCDFORCED, apiManager.setLcdOrientationForced(command.equals(OnOffType.ON)
                        || command.equals(UpDownType.UP) || command.equals(OpenClosedType.OPEN)));
            } catch (FreeboxException e) {
                logCommandException(e, channelUID, command);
                fetchLCDConfig();
            }
        } else {
            logger.debug("Thing {}: invalid command {} from channel {}", getThing().getUID(), command,
                    channelUID.getId());
        }
    }

    private void setWifiStatus(ChannelUID channelUID, Command command) {
        if (command instanceof OnOffType || command instanceof OpenClosedType || command instanceof UpDownType) {
            try {
                updateChannelSwitchState(WIFISTATUS, apiManager.enableWifi(command.equals(OnOffType.ON)
                        || command.equals(UpDownType.UP) || command.equals(OpenClosedType.OPEN)));
            } catch (FreeboxException e) {
                logCommandException(e, channelUID, command);
                fetchWifiConfig();
            }
        } else {
            logger.debug("Thing {}: invalid command {} from channel {}", getThing().getUID(), command,
                    channelUID.getId());
        }
    }

    private void setFtpStatus(ChannelUID channelUID, Command command) {
        if (command instanceof OnOffType || command instanceof OpenClosedType || command instanceof UpDownType) {
            try {
                updateChannelSwitchState(FTPSTATUS, apiManager.enableFtp(command.equals(OnOffType.ON)
                        || command.equals(UpDownType.UP) || command.equals(OpenClosedType.OPEN)));
            } catch (FreeboxException e) {
                logCommandException(e, channelUID, command);
                fetchFtpConfig();
            }
        } else {
            logger.debug("Thing {}: invalid command {} from channel {}", getThing().getUID(), command,
                    channelUID.getId());
        }
    }

    private void setAirMediaStatus(ChannelUID channelUID, Command command) {
        if (command instanceof OnOffType || command instanceof OpenClosedType || command instanceof UpDownType) {
            try {
                if (!apiManager.isInLanBridgeMode()) {
                    updateChannelSwitchState(AIRMEDIASTATUS, apiManager.enableAirMedia(command.equals(OnOffType.ON)
                            || command.equals(UpDownType.UP) || command.equals(OpenClosedType.OPEN)));
                } else {
                    logger.debug("Thing {}: command {} from channel {} unavailable when in bridge mode",
                            getThing().getUID(), command, channelUID.getId());
                }
            } catch (FreeboxException e) {
                logCommandException(e, channelUID, command);
                fetchAirMediaConfig();
            }
        } else {
            logger.debug("Thing {}: invalid command {} from channel {}", getThing().getUID(), command,
                    channelUID.getId());
        }
    }

    private void setUPnPAVStatus(ChannelUID channelUID, Command command) {
        if (command instanceof OnOffType || command instanceof OpenClosedType || command instanceof UpDownType) {
            try {
                if (!apiManager.isInLanBridgeMode()) {
                    updateChannelSwitchState(UPNPAVSTATUS, apiManager.enableUPnPAV(command.equals(OnOffType.ON)
                            || command.equals(UpDownType.UP) || command.equals(OpenClosedType.OPEN)));
                } else {
                    logger.debug("Thing {}: command {} from channel {} unavailable when in bridge mode",
                            getThing().getUID(), command, channelUID.getId());
                }
            } catch (FreeboxException e) {
                logCommandException(e, channelUID, command);
                fetchUPnPAVConfig();
            }
        } else {
            logger.debug("Thing {}: invalid command {} from channel {}", getThing().getUID(), command,
                    channelUID.getId());
        }
    }

    private void setSambaFileStatus(ChannelUID channelUID, Command command) {
        if (command instanceof OnOffType || command instanceof OpenClosedType || command instanceof UpDownType) {
            try {
                updateChannelSwitchState(SAMBAFILESTATUS, apiManager.enableSambaFileShare(command.equals(OnOffType.ON)
                        || command.equals(UpDownType.UP) || command.equals(OpenClosedType.OPEN)));
            } catch (FreeboxException e) {
                logCommandException(e, channelUID, command);
                fetchSambaConfig();
            }
        } else {
            logger.debug("Thing {}: invalid command {} from channel {}", getThing().getUID(), command,
                    channelUID.getId());
        }
    }

    private void setSambaPrinterStatus(ChannelUID channelUID, Command command) {
        if (command instanceof OnOffType || command instanceof OpenClosedType || command instanceof UpDownType) {
            try {
                updateChannelSwitchState(SAMBAPRINTERSTATUS,
                        apiManager.enableSambaPrintShare(command.equals(OnOffType.ON) || command.equals(UpDownType.UP)
                                || command.equals(OpenClosedType.OPEN)));
            } catch (FreeboxException e) {
                logCommandException(e, channelUID, command);
                fetchSambaConfig();
            }
        } else {
            logger.debug("Thing {}: invalid command {} from channel {}", getThing().getUID(), command,
                    channelUID.getId());
        }
    }

    private void reboot(ChannelUID channelUID, Command command) {
        if (command.equals(OnOffType.ON) || command.equals(UpDownType.UP) || command.equals(OpenClosedType.OPEN)) {
            try {
                apiManager.reboot();
            } catch (FreeboxException e) {
                logCommandException(e, channelUID, command);
            }
        } else {
            logger.debug("Thing {}: invalid command {} from channel {}", getThing().getUID(), command,
                    channelUID.getId());
        }
    }

    private void updateChannelStringState(String channel, String state) {
        updateState(new ChannelUID(getThing().getUID(), channel), new StringType(state));
    }

    private void updateChannelSwitchState(String channel, boolean state) {
        updateState(new ChannelUID(getThing().getUID(), channel), state ? OnOffType.ON : OnOffType.OFF);
    }

    private void updateChannelDecimalState(String channel, int state) {
        updateState(new ChannelUID(getThing().getUID(), channel), new DecimalType(state));
    }

    private void updateChannelDecimalState(String channel, long state) {
        updateState(new ChannelUID(getThing().getUID(), channel), new DecimalType(state));
    }

    public void logCommandException(FreeboxException e, ChannelUID channelUID, Command command) {
        if (e.isMissingRights()) {
            logger.debug("Thing {}: missing right {} while handling command {} from channel {}", getThing().getUID(),
                    e.getResponse().getMissingRight(), command, channelUID.getId());
        } else {
            logger.debug("Thing {}: error while handling command {} from channel {}", getThing().getUID(), command,
                    channelUID.getId(), e);
        }
    }
}
