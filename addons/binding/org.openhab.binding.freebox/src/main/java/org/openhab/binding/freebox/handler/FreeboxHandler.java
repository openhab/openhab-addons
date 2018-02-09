/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.freebox.handler;

import static org.openhab.binding.freebox.FreeboxBindingConstants.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
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
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.matmaul.freeboxos.FreeboxException;
import org.matmaul.freeboxos.FreeboxOsClient;
import org.matmaul.freeboxos.airmedia.AirMediaConfig;
import org.matmaul.freeboxos.airmedia.AirMediaReceiver;
import org.matmaul.freeboxos.connection.ConnectionStatus;
import org.matmaul.freeboxos.connection.xDslStatus;
import org.matmaul.freeboxos.ftp.FtpConfig;
import org.matmaul.freeboxos.lan.LanConfig;
import org.matmaul.freeboxos.lan.LanHostsConfig;
import org.matmaul.freeboxos.lcd.LCDConfig;
import org.matmaul.freeboxos.login.Authorize;
import org.matmaul.freeboxos.login.LoginManager;
import org.matmaul.freeboxos.login.TrackAuthorizeStatus;
import org.matmaul.freeboxos.netshare.SambaConfig;
import org.matmaul.freeboxos.system.SystemConfiguration;
import org.matmaul.freeboxos.upnpav.UPnPAVConfig;
import org.matmaul.freeboxos.wifi.WifiGlobalConfig;
import org.openhab.binding.freebox.FreeboxBindingConstants;
import org.openhab.binding.freebox.internal.FreeboxDataListener;
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
 */
public class FreeboxHandler extends BaseBridgeHandler {

    private Logger logger = LoggerFactory.getLogger(FreeboxHandler.class);

    private ScheduledFuture<?> authorizeJob;
    private ScheduledFuture<?> globalJob;
    private FreeboxOsClient fbClient;
    private long uptime;
    private List<FreeboxDataListener> dataListeners = new CopyOnWriteArrayList<>();

    public FreeboxHandler(Bridge bridge) {
        super(bridge);

        authorizeJob = null;
        globalJob = null;
        fbClient = null;
        uptime = -1;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command == null || command instanceof RefreshType) {
            return;
        }
        if ((getThing().getStatus() == ThingStatus.OFFLINE)
                && (getThing().getStatusInfo().getStatusDetail() == ThingStatusDetail.CONFIGURATION_ERROR)) {
            return;
        }
        try {
            switch (channelUID.getId()) {
                case LCDBRIGHTNESS:
                    setBrightness(command);
                    break;
                case LCDORIENTATION:
                    setOrientation(command);
                    break;
                case LCDFORCED:
                    setForced(command);
                    break;
                case WIFISTATUS:
                    setWifiStatus(command);
                    break;
                case FTPSTATUS:
                    setFtpStatus(command);
                    break;
                case AIRMEDIASTATUS:
                    setAirMediaStatus(command);
                    break;
                case UPNPAVSTATUS:
                    setUPnPAVStatus(command);
                    break;
                case SAMBAFILESTATUS:
                    setSambaFileStatus(command);
                    break;
                case SAMBAPRINTERSTATUS:
                    setSambaPrinterStatus(command);
                    break;
                case REBOOT:
                    setReboot(command);
                    break;
                default:
                    logger.debug("Thing {}: unexpected command {} from channel {}", getThing().getUID(), command,
                            channelUID.getId());
                    break;
            }
        } catch (FreeboxException e) {
            logger.debug("Thing {}: error while handling command {} from channel {}", getThing().getUID(), command,
                    channelUID.getId(), e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        }
    }

    /**
     * Handles connection to the Freebox, including validation of the Apptoken
     * if none is provided in configuration
     *
     * @throws FreeboxException
     */
    private boolean authorize(boolean useHttps, String fqdn, String apiBaseUrl, String apiVersion) {

        FreeboxServerConfiguration configuration = getConfigAs(FreeboxServerConfiguration.class);

        Bundle bundle = FrameworkUtil.getBundle(getClass());

        fbClient = new FreeboxOsClient(bundle.getSymbolicName(), /* org.openhab.binding.freebox */
                useHttps, fqdn, apiBaseUrl, apiVersion);

        LoginManager loginManager = fbClient.getLoginManager();
        TrackAuthorizeStatus authorizeStatus = TrackAuthorizeStatus.UNKNOWN;
        try {

            if (StringUtils.isEmpty(configuration.appToken)) {

                Authorize authorize = loginManager.newAuthorize(bundle.getHeaders().get("Bundle-Name"), // Freebox
                                                                                                        // Binding
                        String.format("%d.%d", bundle.getVersion().getMajor(), bundle.getVersion().getMinor()), // eg.
                                                                                                                // 1.5
                        bundle.getHeaders().get("Bundle-Vendor"));

                configuration.appToken = authorize.getAppToken();

                logger.info("####################################################################");
                logger.info("# Please accept activation request directly on your freebox        #");
                logger.info("# Once done, record Apptoken in the Freebox Item configuration     #");
                logger.info("# {} #", configuration.appToken);
                logger.info("####################################################################");

                do {
                    Thread.sleep(2000);
                    authorizeStatus = loginManager.trackAuthorize();
                } while (authorizeStatus == TrackAuthorizeStatus.PENDING);
            } else {
                authorizeStatus = TrackAuthorizeStatus.GRANTED;
            }

            if (authorizeStatus != TrackAuthorizeStatus.GRANTED) {
                return false;
            }

            logger.debug("Apptoken valide : [{}]", configuration.appToken);
            loginManager.setAppToken(configuration.appToken);
            loginManager.openSession();
            return true;
        } catch (Exception e) {
            logger.debug("Thing {}: error while opening a session", getThing().getUID(), e);
            return false;
        }
    }

    @Override
    public void initialize() {
        logger.debug("initializing Freebox Server handler for thing {}", getThing().getUID());

        FreeboxServerConfiguration configuration = getConfigAs(FreeboxServerConfiguration.class);
        if ((configuration != null) && StringUtils.isNotEmpty(configuration.fqdn)) {
            updateStatus(ThingStatus.OFFLINE);

            logger.debug("Binding will schedule a job to establish a connection...");
            if (authorizeJob == null || authorizeJob.isCancelled()) {
                authorizeJob = scheduler.schedule(authorizeRunnable, 1, TimeUnit.SECONDS);
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Freebox Server FQDN not set in the thing configuration");
        }
    }

    private Runnable authorizeRunnable = new Runnable() {
        @Override
        @SuppressWarnings("null")
        public void run() {
            logger.debug("Authorize job...");

            FreeboxServerConfiguration configuration = getConfigAs(FreeboxServerConfiguration.class);
            String result = null;
            boolean httpsRequestOk = false;
            if (!Boolean.TRUE.equals(configuration.useOnlyHttp)) {
                try {
                    result = HttpUtil.executeUrl("GET", "https://" + configuration.fqdn + "/api_version", 5000);
                    httpsRequestOk = true;
                } catch (IOException e) {
                    logger.debug("Can't connect to {} with HTTPS", configuration.fqdn, e);
                }
            }
            if (result == null) {
                try {
                    result = HttpUtil.executeUrl("GET", "http://" + configuration.fqdn + "/api_version", 5000);
                } catch (IOException e) {
                    logger.debug("Can't connect to {} with HTTP", configuration.fqdn, e);
                }
            }
            String apiBaseUrl = null;
            String apiVersion = null;
            String hardwareVersion = null;
            String apiDomain = null;
            boolean httpsAvailable = false;
            String httpsPort = null;
            boolean useHttps = false;
            String fqdn = configuration.fqdn;
            String errorMsg = null;
            if (result == null) {
                errorMsg = "Can't connect to " + configuration.fqdn;
            } else {
                apiBaseUrl = StringUtils.trim(StringUtils
                        .replace(StringUtils.substringBetween(result, "\"api_base_url\":\"", "\""), "\\/", "/"));
                apiVersion = StringUtils.trim(StringUtils.substringBetween(result, "\"api_version\":\"", "\""));
                hardwareVersion = StringUtils.trim(StringUtils.substringBetween(result, "\"device_type\":\"", "\""));
                apiDomain = StringUtils.trim(StringUtils.substringBetween(result, "\"api_domain\":\"", "\""));
                httpsAvailable = StringUtils.containsIgnoreCase(result, "\"https_available\":true");
                httpsPort = StringUtils.trim(StringUtils.substringBetween(result, "\"https_port\":", ","));
                if (StringUtils.isEmpty(apiBaseUrl)) {
                    errorMsg = configuration.fqdn + " does not deliver any API base URL";
                } else if (StringUtils.isEmpty(apiVersion)) {
                    errorMsg = configuration.fqdn + " does not deliver any API version";
                } else if (httpsAvailable && !Boolean.TRUE.equals(configuration.useOnlyHttp)) {
                    if (StringUtils.isEmpty(httpsPort) || StringUtils.isEmpty(apiDomain)) {
                        if (httpsRequestOk) {
                            useHttps = true;
                        } else {
                            logger.info("{} does not deliver API domain or HTTPS port; use HTTP API",
                                    configuration.fqdn);
                        }
                    } else {
                        useHttps = true;
                        fqdn = apiDomain + ":" + httpsPort;
                    }
                }
            }

            if (errorMsg != null) {
                logger.debug("Thing {}: bad configuration: {}", getThing().getUID(), errorMsg);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, errorMsg);
            } else if (!authorize(useHttps, fqdn, apiBaseUrl, apiVersion)) {
                if (StringUtils.isEmpty(configuration.appToken)) {
                    errorMsg = "App token not set in the thing configuration";
                } else {
                    errorMsg = "Check your app token in the thing configuration; opening session with " + fqdn
                            + " using " + (useHttps ? "HTTPS" : "HTTP") + " API version " + apiVersion + " failed";
                }
                logger.debug("Thing {}: {}", getThing().getUID(), errorMsg);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, errorMsg);
            } else {
                logger.info("Thing {}: session opened with {} using {} API version {}", getThing().getUID(), fqdn,
                        (useHttps ? "HTTPS" : "HTTP"), apiVersion);
                updateStatus(ThingStatus.ONLINE);

                if (globalJob == null || globalJob.isCancelled()) {
                    long pollingInterval = getConfigAs(FreeboxServerConfiguration.class).refreshInterval;
                    logger.debug("Scheduling server state update every {} seconds...", pollingInterval);
                    globalJob = scheduler.scheduleWithFixedDelay(globalRunnable, 1, pollingInterval, TimeUnit.SECONDS);
                }
            }

            Map<String, String> properties = editProperties();
            boolean update = false;
            if (StringUtils.isNotEmpty(apiBaseUrl)
                    && !apiBaseUrl.equals(properties.get(FreeboxBindingConstants.API_BASE_URL))) {
                update = true;
                properties.put(FreeboxBindingConstants.API_BASE_URL, apiBaseUrl);
            }
            if (StringUtils.isNotEmpty(apiVersion)
                    && !apiVersion.equals(properties.get(FreeboxBindingConstants.API_VERSION))) {
                update = true;
                properties.put(FreeboxBindingConstants.API_VERSION, apiVersion);
            }
            if (StringUtils.isNotEmpty(hardwareVersion)
                    && !hardwareVersion.equals(properties.get(Thing.PROPERTY_HARDWARE_VERSION))) {
                update = true;
                properties.put(Thing.PROPERTY_HARDWARE_VERSION, hardwareVersion);
            }
            if (update) {
                updateProperties(properties);
            }
        }
    };

    private Runnable globalRunnable = new Runnable() {
        @Override
        public void run() {
            logger.debug("Polling server state...");

            try {
                fetchSystemConfig();
                fetchLCDConfig();
                fetchWifiConfig();
                fetchxDslStatus();
                fetchConnectionStatus();
                fetchFtpConfig();
                fetchAirMediaConfig();
                fetchUPnPAVConfig();
                fetchSambaConfig();
                LanHostsConfig lanHostsConfiguration = fetchLanHostsConfig();
                List<AirMediaReceiver> airPlayDevices = fetchAirPlayDevices();

                // Trigger a new discovery of things
                for (FreeboxDataListener dataListener : dataListeners) {
                    dataListener.onDataFetched(getThing().getUID(), lanHostsConfiguration, airPlayDevices);
                }

                if (getThing().getStatus() == ThingStatus.OFFLINE) {
                    updateStatus(ThingStatus.ONLINE);
                }

            } catch (Throwable t) {
                if (t instanceof FreeboxException) {
                    logger.error("Server state job - FreeboxException: {}", ((FreeboxException) t).getMessage());
                } else if (t instanceof Exception) {
                    logger.error("Server state job - Exception: {}", ((Exception) t).getMessage());
                } else if (t instanceof Error) {
                    logger.error("Server state job - Error: {}", ((Error) t).getMessage());
                } else {
                    logger.error("Server state job - Unexpected error");
                }
                StringWriter sw = new StringWriter();
                if ((t instanceof RuntimeException) && (t.getCause() != null)) {
                    t.getCause().printStackTrace(new PrintWriter(sw));
                } else {
                    t.printStackTrace(new PrintWriter(sw));
                }
                logger.error("{}", sw);
                if (getThing().getStatus() == ThingStatus.ONLINE) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
                }
            }

        }
    };

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
        super.dispose();
    }

    public FreeboxOsClient getFbClient() {
        return fbClient;
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

    private void fetchConnectionStatus() throws FreeboxException {
        ConnectionStatus connectionStatus = fbClient.getConnectionManager().getStatus();
        updateState(new ChannelUID(getThing().getUID(), LINESTATUS), new StringType(connectionStatus.getState()));
        updateState(new ChannelUID(getThing().getUID(), IPV4), new StringType(connectionStatus.getIpv4()));
        updateState(new ChannelUID(getThing().getUID(), RATEUP), new DecimalType(connectionStatus.getRate_up()));
        updateState(new ChannelUID(getThing().getUID(), RATEDOWN), new DecimalType(connectionStatus.getRate_down()));
        updateState(new ChannelUID(getThing().getUID(), BYTESUP), new DecimalType(connectionStatus.getBytes_up()));
        updateState(new ChannelUID(getThing().getUID(), BYTESDOWN), new DecimalType(connectionStatus.getBytes_down()));
    }

    private void fetchxDslStatus() throws FreeboxException {
        xDslStatus xdslStatus = fbClient.getConnectionManager().getxDslStatus();
        updateState(new ChannelUID(getThing().getUID(), XDSLSTATUS), new StringType(xdslStatus.getStatus()));
    }

    private void fetchWifiConfig() throws FreeboxException {
        WifiGlobalConfig wifiConfiguration = fbClient.getWifiManager().getGlobalConfig();
        updateState(new ChannelUID(getThing().getUID(), WIFISTATUS),
                wifiConfiguration.getEnabled() ? OnOffType.ON : OnOffType.OFF);
    }

    private void fetchFtpConfig() throws FreeboxException {
        FtpConfig ftpConfiguration = fbClient.getFtpManager().getConfig();
        updateState(new ChannelUID(getThing().getUID(), FTPSTATUS),
                ftpConfiguration.getEnabled() ? OnOffType.ON : OnOffType.OFF);
    }

    private void fetchAirMediaConfig() throws FreeboxException {
        LanConfig lc = fbClient.getLanManager().getLanConfig();
        String mode = lc.getMode();
        if ((mode != null) && !mode.equalsIgnoreCase("bridge")) {
            // Only when Freebox Revolution is not in bridge mode
            AirMediaConfig airMediaConfiguration = fbClient.getAirMediaManager().getConfig();
            updateState(new ChannelUID(getThing().getUID(), AIRMEDIASTATUS),
                    airMediaConfiguration.getEnabled() ? OnOffType.ON : OnOffType.OFF);
        }
    }

    private void fetchUPnPAVConfig() throws FreeboxException {
        LanConfig lc = fbClient.getLanManager().getLanConfig();
        String mode = lc.getMode();
        if ((mode != null) && !mode.equalsIgnoreCase("bridge")) {
            // Only when Freebox Revolution is not in bridge mode
            UPnPAVConfig upnpAvConfiguration = fbClient.getUPnPAVManager().getConfig();
            updateState(new ChannelUID(getThing().getUID(), UPNPAVSTATUS),
                    upnpAvConfiguration.getEnabled() ? OnOffType.ON : OnOffType.OFF);
        }
    }

    private void fetchSambaConfig() throws FreeboxException {
        SambaConfig sambaConfiguration = fbClient.getNetShareManager().getSambaConfig();
        updateState(new ChannelUID(getThing().getUID(), SAMBAFILESTATUS),
                sambaConfiguration.getFileShareEnabled() ? OnOffType.ON : OnOffType.OFF);
        updateState(new ChannelUID(getThing().getUID(), SAMBAPRINTERSTATUS),
                sambaConfiguration.getPrintShareEnabled() ? OnOffType.ON : OnOffType.OFF);
    }

    private void fetchLCDConfig() throws FreeboxException {
        LCDConfig lcdConfiguration = fbClient.getLCDManager().getLCDConfig();
        updateState(new ChannelUID(getThing().getUID(), LCDBRIGHTNESS),
                new DecimalType(lcdConfiguration.getBrightness()));
        updateState(new ChannelUID(getThing().getUID(), LCDORIENTATION),
                new DecimalType(lcdConfiguration.getOrientation()));
        updateState(new ChannelUID(getThing().getUID(), LCDFORCED),
                lcdConfiguration.getOrientationForced() ? OnOffType.ON : OnOffType.OFF);
    }

    private void fetchSystemConfig() throws FreeboxException {
        SystemConfiguration systemConfiguration = fbClient.getSystemManager().getConfiguration();

        Map<String, String> properties = editProperties();
        boolean update = false;
        if (!systemConfiguration.getSerial().isEmpty()
                && !systemConfiguration.getSerial().equals(properties.get(Thing.PROPERTY_SERIAL_NUMBER))) {
            update = true;
            properties.put(Thing.PROPERTY_SERIAL_NUMBER, systemConfiguration.getSerial());
        }
        if (!systemConfiguration.getFirmware_version().isEmpty()
                && !systemConfiguration.getFirmware_version().equals(properties.get(Thing.PROPERTY_FIRMWARE_VERSION))) {
            update = true;
            properties.put(Thing.PROPERTY_FIRMWARE_VERSION, systemConfiguration.getFirmware_version());
        }
        if (update) {
            updateProperties(properties);
        }

        updateState(new ChannelUID(getThing().getUID(), FWVERSION),
                new StringType(systemConfiguration.getFirmware_version()));

        long newUptime = systemConfiguration.getUptimeVal();
        updateState(new ChannelUID(getThing().getUID(), RESTARTED), newUptime < uptime ? OnOffType.ON : OnOffType.OFF);
        uptime = newUptime;

        updateState(new ChannelUID(getThing().getUID(), UPTIME), new DecimalType(uptime));
        updateState(new ChannelUID(getThing().getUID(), TEMPCPUM), new DecimalType(systemConfiguration.getTemp_cpum()));
        updateState(new ChannelUID(getThing().getUID(), TEMPCPUB), new DecimalType(systemConfiguration.getTemp_cpub()));
        updateState(new ChannelUID(getThing().getUID(), TEMPSWITCH), new DecimalType(systemConfiguration.getTemp_sw()));
        updateState(new ChannelUID(getThing().getUID(), FANSPEED), new DecimalType(systemConfiguration.getFan_rpm()));
    }

    private synchronized LanHostsConfig fetchLanHostsConfig() throws FreeboxException {
        LanHostsConfig lanHostsConfiguration = fbClient.getLanManager().getAllLanHostsConfig();

        // The update of channels is delegated to each thing handler
        for (Thing thing : getThing().getThings()) {
            if (thing.getHandler() != null) {
                ((FreeboxThingHandler) thing.getHandler()).updateNetInfo(lanHostsConfiguration);
            }
        }

        return lanHostsConfiguration;
    }

    private synchronized List<AirMediaReceiver> fetchAirPlayDevices() throws FreeboxException {
        List<AirMediaReceiver> airPlayDevices = fbClient.getAirMediaManager().getReceivers();

        // The update of channels is delegated to each thing handler
        for (Thing thing : getThing().getThings()) {
            if (thing.getHandler() != null) {
                ((FreeboxThingHandler) thing.getHandler()).updateAirPlayDevice(airPlayDevices);
            }
        }

        return airPlayDevices;
    }

    public void setBrightness(Command command) throws FreeboxException {
        if (command != null) {
            if (command instanceof OnOffType || command instanceof IncreaseDecreaseType
                    || command instanceof DecimalType || command instanceof PercentType) {

                LCDConfig lcd = fbClient.getLCDManager().getLCDConfig();
                int value = 0;
                int newValue = 0;

                if (command instanceof IncreaseDecreaseType) {
                    value = lcd.getBrightness();
                    if (command == IncreaseDecreaseType.INCREASE) {
                        newValue = Math.min(100, value + 1);
                    } else {
                        newValue = Math.max(0, value - 1);
                    }
                } else if (command instanceof OnOffType) {
                    newValue = (command == OnOffType.ON) ? 100 : 0;
                } else if (command instanceof DecimalType) {
                    newValue = Math.min(100, ((DecimalType) command).intValue());
                    newValue = Math.max(newValue, 0);
                } else {
                    return;
                }
                lcd.setBrightness(newValue);
                fbClient.getLCDManager().setLCDConfig(lcd);
                fetchLCDConfig();
            }
        }
    }

    private void setOrientation(Command command) throws FreeboxException {
        if (command != null && command instanceof DecimalType) {
            LCDConfig lcd = fbClient.getLCDManager().getLCDConfig();
            int newValue = Math.min(360, ((DecimalType) command).intValue());
            newValue = Math.max(newValue, 0);
            lcd.setOrientation(newValue);
            lcd.setOrientationForced(true);
            fbClient.getLCDManager().setLCDConfig(lcd);
            fetchLCDConfig();
        }
    }

    private void setForced(Command command) throws FreeboxException {
        if (command != null) {
            if (command instanceof OnOffType || command instanceof OpenClosedType || command instanceof UpDownType) {

                LCDConfig lcd = fbClient.getLCDManager().getLCDConfig();

                lcd.setOrientationForced(command.equals(OnOffType.ON) || command.equals(UpDownType.UP)
                        || command.equals(OpenClosedType.OPEN));
                fbClient.getLCDManager().setLCDConfig(lcd);
                fetchLCDConfig();
            }
        }
    }

    private void setWifiStatus(Command command) throws FreeboxException {
        if (command != null) {
            if (command instanceof OnOffType || command instanceof OpenClosedType || command instanceof UpDownType) {

                WifiGlobalConfig wifiConfiguration = new WifiGlobalConfig();

                wifiConfiguration.setEnabled(command.equals(OnOffType.ON) || command.equals(UpDownType.UP)
                        || command.equals(OpenClosedType.OPEN));

                fbClient.getWifiManager().setGlobalConfig(wifiConfiguration);
                fetchWifiConfig();
            }
        }
    }

    private void setFtpStatus(Command command) throws FreeboxException {
        if (command != null) {
            if (command instanceof OnOffType || command instanceof OpenClosedType || command instanceof UpDownType) {

                FtpConfig ftpConfiguration = new FtpConfig();

                ftpConfiguration.setEnabled(command.equals(OnOffType.ON) || command.equals(UpDownType.UP)
                        || command.equals(OpenClosedType.OPEN));

                fbClient.getFtpManager().setConfig(ftpConfiguration);
                fetchFtpConfig();
            }
        }
    }

    private void setAirMediaStatus(Command command) throws FreeboxException {
        if (command != null) {
            if (command instanceof OnOffType || command instanceof OpenClosedType || command instanceof UpDownType) {

                AirMediaConfig airMediaConfiguration = new AirMediaConfig();

                airMediaConfiguration.setEnabled(command.equals(OnOffType.ON) || command.equals(UpDownType.UP)
                        || command.equals(OpenClosedType.OPEN));

                fbClient.getAirMediaManager().setConfig(airMediaConfiguration);
                fetchAirMediaConfig();
            }
        }
    }

    private void setUPnPAVStatus(Command command) throws FreeboxException {
        if (command != null) {
            if (command instanceof OnOffType || command instanceof OpenClosedType || command instanceof UpDownType) {

                UPnPAVConfig upnpAvConfiguration = new UPnPAVConfig();

                upnpAvConfiguration.setEnabled(command.equals(OnOffType.ON) || command.equals(UpDownType.UP)
                        || command.equals(OpenClosedType.OPEN));

                fbClient.getUPnPAVManager().setConfig(upnpAvConfiguration);
                fetchUPnPAVConfig();
            }
        }
    }

    private void setSambaFileStatus(Command command) throws FreeboxException {
        if (command != null) {
            if (command instanceof OnOffType || command instanceof OpenClosedType || command instanceof UpDownType) {

                SambaConfig sambaConfiguration = new SambaConfig();

                sambaConfiguration.setFileShareEnabled(command.equals(OnOffType.ON) || command.equals(UpDownType.UP)
                        || command.equals(OpenClosedType.OPEN));

                fbClient.getNetShareManager().setSambaConfig(sambaConfiguration);
                fetchSambaConfig();
            }
        }
    }

    private void setSambaPrinterStatus(Command command) throws FreeboxException {
        if (command != null) {
            if (command instanceof OnOffType || command instanceof OpenClosedType || command instanceof UpDownType) {

                SambaConfig sambaConfiguration = new SambaConfig();

                sambaConfiguration.setPrintShareEnabled(command.equals(OnOffType.ON) || command.equals(UpDownType.UP)
                        || command.equals(OpenClosedType.OPEN));

                fbClient.getNetShareManager().setSambaConfig(sambaConfiguration);
                fetchSambaConfig();
            }
        }
    }

    private void setReboot(Command command) throws FreeboxException {
        if (command != null) {
            if (command.equals(OnOffType.ON) || command.equals(UpDownType.UP) || command.equals(OpenClosedType.OPEN)) {

                fbClient.getSystemManager().Reboot();
            }
        }
    }

}
