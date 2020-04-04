/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
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

        FreeboxServerConfiguration configuration = getConfigAs(FreeboxServerConfiguration.class);

        // Update the discovery configuration
        Map<String, Object> configDiscovery = new HashMap<>();
        configDiscovery.put(FreeboxServerConfiguration.DISCOVER_PHONE, configuration.discoverPhone);
        configDiscovery.put(FreeboxServerConfiguration.DISCOVER_NET_DEVICE, configuration.discoverNetDevice);
        configDiscovery.put(FreeboxServerConfiguration.DISCOVER_NET_INTERFACE, configuration.discoverNetInterface);
        configDiscovery.put(FreeboxServerConfiguration.DISCOVER_AIRPLAY_RECEIVER,
                configuration.discoverAirPlayReceiver);
        for (FreeboxDataListener dataListener : dataListeners) {
            dataListener.applyConfig(configDiscovery);
        }

        if (StringUtils.isNotEmpty(configuration.fqdn)) {
            updateStatus(ThingStatus.UNKNOWN);

            logger.debug("Binding will schedule a job to establish a connection...");
            if (authorizeJob == null || authorizeJob.isCancelled()) {
                authorizeJob = scheduler.schedule(this::authorize, 1, TimeUnit.SECONDS);
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

    private void authorize() {
        logger.debug("Authorize job...");

        FreeboxServerConfiguration configuration = getConfigAs(FreeboxServerConfiguration.class);
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
        boolean useHttps = false;
        String errorMsg = null;
        if (result == null) {
            errorMsg = "Can't connect to " + fqdn;
        } else if (StringUtils.isEmpty(result.getApiBaseUrl())) {
            errorMsg = fqdn + " does not deliver any API base URL";
        } else if (StringUtils.isEmpty(result.getApiVersion())) {
            errorMsg = fqdn + " does not deliver any API version";
        } else if (Boolean.TRUE.equals(result.isHttpsAvailable()) && !Boolean.TRUE.equals(configuration.useOnlyHttp)) {
            if (result.getHttpsPort() == null || StringUtils.isEmpty(result.getApiDomain())) {
                if (httpsRequestOk) {
                    useHttps = true;
                } else {
                    logger.debug("{} does not deliver API domain or HTTPS port; use HTTP API", fqdn);
                }
            } else if (apiManager.checkApi(String.format("%s:%d", result.getApiDomain(), result.getHttpsPort()),
                    true) != null) {
                useHttps = true;
                fqdn = String.format("%s:%d", result.getApiDomain(), result.getHttpsPort());
            }
        }

        if (errorMsg != null) {
            logger.debug("Thing {}: bad configuration: {}", getThing().getUID(), errorMsg);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, errorMsg);
        } else if (!apiManager.authorize(useHttps, fqdn, result.getApiBaseUrl(), result.getApiVersion(),
                configuration.appToken)) {
            if (StringUtils.isEmpty(configuration.appToken)) {
                errorMsg = "App token not set in the thing configuration";
            } else {
                errorMsg = "Check your app token in the thing configuration; opening session with " + fqdn + " using "
                        + (useHttps ? "HTTPS" : "HTTP") + " API version " + result.getApiVersion() + " failed";
            }
            logger.debug("Thing {}: {}", getThing().getUID(), errorMsg);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, errorMsg);
        } else {
            logger.debug("Thing {}: session opened with {} using {} API version {}", getThing().getUID(), fqdn,
                    (useHttps ? "HTTPS" : "HTTP"), result.getApiVersion());
            if (globalJob == null || globalJob.isCancelled()) {
                long pollingInterval = getConfigAs(FreeboxServerConfiguration.class).refreshInterval;
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
        if (result != null && StringUtils.isNotEmpty(result.getApiBaseUrl())) {
            properties.put(API_BASE_URL, result.getApiBaseUrl());
        }
        if (result != null && StringUtils.isNotEmpty(result.getApiVersion())) {
            properties.put(API_VERSION, result.getApiVersion());
        }
        if (result != null && StringUtils.isNotEmpty(result.getDeviceType())) {
            properties.put(Thing.PROPERTY_HARDWARE_VERSION, result.getDeviceType());
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
            if (StringUtils.isNotEmpty(connectionStatus.getState())) {
                updateChannelStringState(LINESTATUS, connectionStatus.getState());
            }
            if (StringUtils.isNotEmpty(connectionStatus.getIpv4())) {
                updateChannelStringState(IPV4, connectionStatus.getIpv4());
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
            if (StringUtils.isNotEmpty(status)) {
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
            if (StringUtils.isNotEmpty(config.getSerial())) {
                properties.put(Thing.PROPERTY_SERIAL_NUMBER, config.getSerial());
            }
            if (StringUtils.isNotEmpty(config.getBoardName())) {
                properties.put(Thing.PROPERTY_HARDWARE_VERSION, config.getBoardName());
            }
            if (StringUtils.isNotEmpty(config.getFirmwareVersion())) {
                properties.put(Thing.PROPERTY_FIRMWARE_VERSION, config.getFirmwareVersion());
                updateChannelStringState(FWVERSION, config.getFirmwareVersion());
            }
            if (StringUtils.isNotEmpty(config.getMac())) {
                properties.put(Thing.PROPERTY_MAC_ADDRESS, config.getMac());
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
