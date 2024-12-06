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
package org.openhab.binding.samsungtv.internal.handler;

import static org.openhab.binding.samsungtv.internal.SamsungTvBindingConstants.*;
import static org.openhab.binding.samsungtv.internal.config.SamsungTvConfiguration.*;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.jupnp.UpnpService;
import org.jupnp.model.meta.Device;
import org.jupnp.model.meta.LocalDevice;
import org.jupnp.model.meta.RemoteDevice;
import org.jupnp.registry.Registry;
import org.jupnp.registry.RegistryListener;
import org.openhab.binding.samsungtv.internal.Utils;
import org.openhab.binding.samsungtv.internal.WakeOnLanUtility;
import org.openhab.binding.samsungtv.internal.WolSend;
import org.openhab.binding.samsungtv.internal.config.SamsungTvConfiguration;
import org.openhab.binding.samsungtv.internal.protocol.RemoteControllerException;
import org.openhab.binding.samsungtv.internal.protocol.RemoteControllerLegacy;
import org.openhab.binding.samsungtv.internal.service.MainTVServerService;
import org.openhab.binding.samsungtv.internal.service.MediaRendererService;
import org.openhab.binding.samsungtv.internal.service.RemoteControllerService;
import org.openhab.binding.samsungtv.internal.service.SmartThingsApiService;
import org.openhab.binding.samsungtv.internal.service.api.SamsungTvService;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.io.net.http.HttpUtil;
import org.openhab.core.io.net.http.WebSocketFactory;
import org.openhab.core.io.transport.upnp.UpnpIOService;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link SamsungTvHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Pauli Anttila - Initial contribution
 * @author Martin van Wingerden - Some changes for non-UPnP configured devices
 * @author Arjan Mels - Remove RegistryListener, manually create RemoteService in all circumstances, add sending of WOL
 *         package to power on TV
 * @author Nick Waterton - Improve Frame TV handling and some refactoring
 */
@NonNullByDefault
public class SamsungTvHandler extends BaseThingHandler implements RegistryListener {

    /** Path for the information endpoint (note the final slash!) */
    private static final String HTTP_ENDPOINT_V2 = "/api/v2/";

    // common Samsung TV remote control ports
    private static final List<Integer> PORTS = List.of(55000, 1515, 7001, 15500);

    private final Logger logger = LoggerFactory.getLogger(SamsungTvHandler.class);

    private final UpnpIOService upnpIOService;
    private final UpnpService upnpService;
    private final WebSocketFactory webSocketFactory;

    public SamsungTvConfiguration configuration;

    public String host = "";
    private String modelName = "";
    public int artApiVersion = 0;

    /* Samsung TV services */
    private final Set<SamsungTvService> services = new CopyOnWriteArraySet<>();

    /* Store powerState to be able to restore upon new link */
    private boolean powerState = false;

    /* Store if art mode is supported to be able to skip switching power state to ON during initialization */
    public boolean artModeSupported = false;
    /* Art Mode on TV's >= 2022 is not properly supported - need workarounds for power */
    public boolean artMode2022 = false;
    /* Is binding initialized? */
    public boolean initialized = false;

    private Optional<ScheduledFuture<?>> pollingJob = Optional.empty();
    private WolSend wolTask = new WolSend(this);

    /** Description of the json returned for the information endpoint */
    @NonNullByDefault({})
    public class TVProperties {
        class Device {
            boolean FrameTVSupport;
            boolean GamePadSupport;
            boolean ImeSyncedSupport;
            String OS;
            String PowerState;
            boolean TokenAuthSupport;
            boolean VoiceSupport;
            String countryCode;
            String description;
            String firmwareVersion;
            String model;
            String modelName;
            String name;
            String networkType;
            String resolution;
            String id;
            String wifiMac;
        }

        Device device;
        String isSupport;

        public boolean getFrameTVSupport() {
            return Optional.ofNullable(device).map(a -> a.FrameTVSupport).orElse(false);
        }

        public boolean getTokenAuthSupport() {
            return Optional.ofNullable(device).map(a -> a.TokenAuthSupport).orElse(false);
        }

        public String getPowerState() {
            if (!getOS().isBlank()) {
                return Optional.ofNullable(device).map(a -> a.PowerState).orElse("on");
            }
            return "off";
        }

        public String getOS() {
            return Optional.ofNullable(device).map(a -> a.OS).orElse("");
        }

        public String getWifiMac() {
            return Optional.ofNullable(device).map(a -> a.wifiMac).filter(m -> m.length() == 17).orElse("");
        }

        public String getModel() {
            return Optional.ofNullable(device).map(a -> a.model).orElse("");
        }

        public String getModelName() {
            return Optional.ofNullable(device).map(a -> a.modelName).orElse("");
        }
    }

    public SamsungTvHandler(Thing thing, UpnpIOService upnpIOService, UpnpService upnpService,
            WebSocketFactory webSocketFactory) {
        super(thing);
        this.upnpIOService = upnpIOService;
        this.upnpService = upnpService;
        this.webSocketFactory = webSocketFactory;
        this.configuration = getConfigAs(SamsungTvConfiguration.class);
        this.host = configuration.getHostName();
        logger.debug("{}: Create a Samsung TV Handler for thing '{}'", host, getThing().getUID());
    }

    /**
     * For Modern TVs get configuration, with 500 ms timeout
     *
     */
    public class FetchTVProperties implements Callable<Optional<TVProperties>> {
        public Optional<TVProperties> call() throws Exception {
            logger.trace("{}: getting TV properties", host);
            Optional<TVProperties> properties = Optional.empty();
            try {
                URI uri = new URI("http", null, host, PORT_DEFAULT_WEBSOCKET, HTTP_ENDPOINT_V2, null, null);
                // @Nullable
                String response = HttpUtil.executeUrl("GET", uri.toURL().toString(), 500);
                properties = Optional.ofNullable(new Gson().fromJson(response, TVProperties.class));
            } catch (IOException e) {
                logger.debug("{}: Cannot connect to TV: {}", host, e.getMessage());
                properties = Optional.empty();
            } catch (JsonSyntaxException | URISyntaxException e) {
                logger.warn("{}: Cannot connect to TV: {}", host, e.getMessage());
                properties = Optional.empty();
            }
            return properties;
        }
    }

    /**
     * For Modern TVs get configuration, with time delay, and retry
     *
     * @param ms int delay in milliseconds
     * @param retryCount int number of retries before giving up
     * @return TVProperties
     */
    public TVProperties fetchTVProperties(int ms, int retryCount) {
        ScheduledFuture<Optional<TVProperties>> future = scheduler.schedule(new FetchTVProperties(), ms,
                TimeUnit.MILLISECONDS);
        try {
            Optional<TVProperties> properties = future.get();
            while (retryCount-- >= 0) {
                if (properties.isPresent()) {
                    return properties.get();
                } else if (retryCount > 0) {
                    logger.warn("{}: Cannot get TVProperties - Retry: {}", host, retryCount);
                    return fetchTVProperties(1000, retryCount);
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            logger.warn("{}: Cannot get TVProperties: {}", host, e.getMessage());
        }
        logger.debug("{}: Cannot get TVProperties, return Empty properties", host);
        return new TVProperties();
    }

    /**
     * Update WOL MAC address
     * Discover the type of remote control service the TV supports.
     * update artModeSupported and PowerState
     * Update the configuration with results
     *
     */
    private void discoverConfiguration() {
        /* Check if configuration should be updated */
        configuration = getConfigAs(SamsungTvConfiguration.class);
        host = configuration.getHostName();
        switch (configuration.getProtocol()) {
            case PROTOCOL_NONE:
                if (configuration.getMacAddress().isBlank()) {
                    String macAddress = WakeOnLanUtility.getMACAddress(host);
                    if (macAddress != null) {
                        putConfig(MAC_ADDRESS, macAddress);
                    }
                }
                TVProperties properties = fetchTVProperties(0, 0);
                if ("Tizen".equals(properties.getOS())) {
                    if (properties.getTokenAuthSupport()) {
                        putConfig(PROTOCOL, PROTOCOL_SECUREWEBSOCKET);
                        putConfig(PORT, PORT_DEFAULT_SECUREWEBSOCKET);
                    } else {
                        putConfig(PROTOCOL, PROTOCOL_WEBSOCKET);
                        putConfig(PORT, PORT_DEFAULT_WEBSOCKET);
                    }
                    if ((configuration.getMacAddress().isBlank()) && !properties.getWifiMac().isBlank()) {
                        putConfig(MAC_ADDRESS, properties.getWifiMac());
                    }
                    updateSettings(properties);
                    break;
                }

                initialized = true;
                for (int port : PORTS) {
                    try {
                        RemoteControllerLegacy remoteController = new RemoteControllerLegacy(host, port, "openHAB",
                                "openHAB");
                        remoteController.openConnection();
                        remoteController.close();
                        putConfig(PROTOCOL, SamsungTvConfiguration.PROTOCOL_LEGACY);
                        putConfig(PORT, port);
                        setPowerState(true);
                        break;
                    } catch (RemoteControllerException e) {
                        // ignore error
                    }
                }
                break;
            case PROTOCOL_WEBSOCKET:
            case PROTOCOL_SECUREWEBSOCKET:
                initializeConfig();
                if (!initialized) {
                    logger.warn("{}: TV binding is not yet Initialized", host);
                }
                break;
            case PROTOCOL_LEGACY:
                initialized = true;
                break;
        }
        showConfiguration();
    }

    public void initializeConfig() {
        if (!initialized) {
            TVProperties properties = fetchTVProperties(0, 0);
            if ("on".equals(properties.getPowerState())) {
                updateSettings(properties);
            }
        }
    }

    public void updateSettings(TVProperties properties) {
        setPowerState("on".equals(properties.getPowerState()));
        setModelName(properties.getModelName());
        int year = Integer.parseInt(properties.getModel().substring(0, 2));
        if (properties.getFrameTVSupport() && year >= 22) {
            logger.warn("{}: Art Mode MAY NOT BE SUPPORTED on Frame TV's after 2021 model year", host);
            setArtMode2022(true);
            artApiVersion = 1;
        }
        setArtModeSupported(properties.getFrameTVSupport() && year < 22);
        logger.debug("{}: Updated artModeSupported: {} PowerState: {}({}) artMode2022: {}", host, getArtModeSupported(),
                getPowerState(), properties.getPowerState(), getArtMode2022());
        initialized = true;
    }

    public void showConfiguration() {
        logger.debug("{}: Configuration: {}, port: {}, token: {}, MAC: {}, subscription: {}", host,
                configuration.getProtocol(), configuration.getPort(), configuration.getWebsocketToken(),
                configuration.getMacAddress(), configuration.getSubscription());
        if (configuration.isWebsocketProtocol()) {
            if (configuration.getSmartThingsApiKey().isBlank()) {
                logger.debug("{}: SmartThings disabled", host);
            } else {
                logger.debug("{}: SmartThings enabled, device id: {}", host, configuration.getSmartThingsDeviceId());
            }
        }
    }

    /**
     * get PowerState from TVProperties
     * Note: Series 7 TV's do not have the PowerState value
     *
     * @return String giving power state (TV can be on or standby, off if unreachable)
     */
    public String fetchPowerState() {
        logger.trace("{}: fetching TV Power State", host);
        TVProperties properties = fetchTVProperties(0, 2);
        String powerState = properties.getPowerState();
        setPowerState("on".equals(powerState));
        logger.debug("{}: PowerState is: {}", host, powerState);
        return powerState;
    }

    public boolean handleCommand(String channel, Command command, int ms) {
        scheduler.schedule(() -> {
            handleCommand(channel, command);
        }, ms, TimeUnit.MILLISECONDS);
        return true;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("{}: Received channel: {}, command: {}", host, channelUID, Utils.truncCmd(command));
        handleCommand(channelUID.getId(), command);
    }

    public void handleCommand(String channel, Command command) {
        logger.trace("{}: Received: {}, command: {}", host, channel, Utils.truncCmd(command));

        // Delegate command to correct service
        for (SamsungTvService service : services) {
            for (String s : service.getSupportedChannelNames(command == RefreshType.REFRESH)) {
                if (channel.equals(s)) {
                    if (service.handleCommand(channel, command)) {
                        return;
                    }
                }
            }
        }
        // if power on/artmode on command try WOL if command failed:
        if (!wolTask.send(channel, command)) {
            if (getThing().getStatus() != ThingStatus.ONLINE) {
                logger.warn("{}: TV is {}", host, getThing().getStatus());
            } else {
                logger.warn("{}: Channel '{}' not connected/supported", host, channel);
            }
        }
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        logger.trace("{}: channelLinked: {}", host, channelUID);
        if (POWER.equals(channelUID.getId())) {
            valueReceived(POWER, OnOffType.from(getPowerState()));
        }
        services.stream().forEach(a -> a.clearCache());
        if (Arrays.asList(ART_COLOR_TEMPERATURE, ART_IMAGE).contains(channelUID.getId())) {
            // refresh channel as it's not polled
            services.stream().filter(a -> a.getServiceName().equals(RemoteControllerService.SERVICE_NAME))
                    .map(a -> a.handleCommand(channelUID.getId(), RefreshType.REFRESH));
        }
    }

    public void setModelName(String modelName) {
        if (!modelName.isBlank()) {
            this.modelName = modelName;
        }
    }

    public String getModelName() {
        return modelName;
    }

    public synchronized void setPowerState(boolean state) {
        powerState = state;
        logger.trace("{}: PowerState set to: {}", host, powerState ? "on" : "off");
    }

    public boolean getPowerState() {
        return powerState;
    }

    public void setArtMode2022(boolean artmode) {
        artMode2022 = artmode;
    }

    public boolean getArtMode2022() {
        return artMode2022;
    }

    public boolean getArtModeSupported() {
        return artModeSupported;
    }

    public synchronized void setArtModeSupported(boolean artmode) {
        if (!artModeSupported && artmode) {
            logger.debug("{}: ArtMode Enabled", host);
        }
        artModeSupported = artmode;
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);

        logger.debug("{}: Initializing Samsung TV handler for uid '{}'", host, getThing().getUID());
        if (host.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "host ip address or name is blank");
            return;
        }

        // note this can take up to 500ms to return if TV is off
        discoverConfiguration();

        upnpService.getRegistry().addListener(this);

        checkAndCreateServices();
    }

    /**
     * Start polling job with initial delay of 10 seconds if websocket protocol is selected
     *
     */
    private void startPolling() {
        int interval = configuration.getRefreshInterval();
        int delay = configuration.isWebsocketProtocol() ? 10000 : 0;
        if (pollingJob.map(job -> (job.isCancelled())).orElse(true)) {
            logger.debug("{}: Start refresh task, interval={}", host, interval);
            pollingJob = Optional
                    .of(scheduler.scheduleWithFixedDelay(this::poll, delay, interval, TimeUnit.MILLISECONDS));
        }
    }

    private void stopPolling() {
        pollingJob.ifPresent(job -> job.cancel(true));
        pollingJob = Optional.empty();
    }

    @Override
    public void dispose() {
        logger.debug("{}: Disposing SamsungTvHandler", host);
        stopPolling();
        wolTask.cancel();
        setArtMode2022(false);
        setArtModeSupported(false);
        artApiVersion = 0;
        stopServices();
        services.clear();
        upnpService.getRegistry().removeListener(this);
    }

    private synchronized void stopServices() {
        stopPolling();
        if (!services.isEmpty()) {
            if (isFrame2022()) {
                logger.debug("{}: Shutdown all Samsung services except RemoteControllerService", host);
                services.stream().forEach(a -> stopService(a));
            } else {
                logger.debug("{}: Shutdown all Samsung services", host);
                services.stream().forEach(a -> stopService(a));
                services.clear();
            }
        }
    }

    private synchronized void shutdown() {
        stopServices();
        putOffline();
    }

    public synchronized void putOnline() {
        if (getThing().getStatus() != ThingStatus.ONLINE) {
            updateStatus(ThingStatus.ONLINE);
            startPolling();
            if (!getArtModeSupported()) {
                if (getArtMode2022()) {
                    handleCommand(SET_ART_MODE, OnOffType.ON, 4000);
                } else if (configuration.isWebsocketProtocol()) {
                    // if TV is registered to SmartThings it wakes up regularly (every 5 minutes or so), even if it's in
                    // standby, so check the power state locally to see if it's actually on
                    fetchPowerState();
                    valueReceived(POWER, OnOffType.from(getPowerState()));
                } else {
                    valueReceived(POWER, OnOffType.ON);
                }
            }
            logger.debug("{}: TV is {}", host, getThing().getStatus());
        }
    }

    public synchronized void putOffline() {
        if (getThing().getStatus() != ThingStatus.OFFLINE) {
            stopPolling();
            valueReceived(ART_MODE, OnOffType.OFF);
            valueReceived(POWER, OnOffType.OFF);
            if (getArtMode2022()) {
                valueReceived(SET_ART_MODE, OnOffType.OFF);
            }
            valueReceived(ART_IMAGE, UnDefType.NULL);
            valueReceived(ART_LABEL, new StringType(""));
            valueReceived(SOURCE_APP, new StringType(""));
            updateStatus(ThingStatus.OFFLINE);
            logger.debug("{}: TV is {}", host, getThing().getStatus());
        }
    }

    public boolean isChannelLinked(String ch) {
        return isLinked(ch);
    }

    private boolean isDuplicateChannel(String channel) {
        // Avoid redundant REFRESH commands when 2 channels are linked to the same action request
        return (channel.equals(SOURCE_ID) && isLinked(SOURCE_NAME))
                || (channel.equals(CHANNEL_NAME) && isLinked(PROGRAM_TITLE));
    }

    private void poll() {
        try {
            // Skip channels if service is not connected/started
            // Only poll SmartThings if TV is ON (ie playing)
            services.stream().filter(service -> service.checkConnection()).filter(
                    service -> getPowerState() || !service.getServiceName().equals(SmartThingsApiService.SERVICE_NAME))
                    .forEach(service -> service.getSupportedChannelNames(true).stream()
                            .filter(channel -> isLinked(channel) && !isDuplicateChannel(channel))
                            .forEach(channel -> service.handleCommand(channel, RefreshType.REFRESH)));
        } catch (Exception e) {
            if (logger.isTraceEnabled()) {
                logger.trace("{}: Polling Job exception: ", host, e);
            } else {
                logger.debug("{}: Polling Job exception: {}", host, e.getMessage());
            }
        }
    }

    public synchronized void valueReceived(String variable, State value) {
        logger.debug("{}: Received value '{}':'{}' for thing '{}'", host, variable, value, this.getThing().getUID());

        if (POWER.equals(variable)) {
            setPowerState(OnOffType.ON.equals(value));
        }
        updateState(variable, value);
    }

    public void reportError(ThingStatusDetail statusDetail, @Nullable String message, @Nullable Throwable e) {
        if (logger.isTraceEnabled()) {
            logger.trace("{}: Error was reported: {}", host, message, e);
        } else {
            logger.debug("{}: Error was reported: {}, {}", host, message, (e != null) ? e.getMessage() : "");
        }
        updateStatus(ThingStatus.OFFLINE, statusDetail, message);
    }

    /**
     * One Samsung TV contains several UPnP devices. Samsung TV is discovered by
     * Media Renderer UPnP device. This function tries to find another UPnP
     * devices related to same Samsung TV and create handler for those.
     * Also attempts to create websocket services if protocol is set to websocket
     * And at least one UPNP service is discovered
     * Smartthings service is also started if PAT (Api key) is entered
     */
    private void checkAndCreateServices() {
        logger.debug("{}: Check and create missing services", host);

        boolean isOnline = false;

        // UPnP services
        for (Device<?, ?, ?> device : upnpService.getRegistry().getDevices()) {
            RemoteDevice rdevice = (RemoteDevice) device;
            if (host.equals(Utils.getHost(rdevice))) {
                setModelName(Utils.getModelName(rdevice));
                isOnline = createService(Utils.getType(rdevice), Utils.getUdn(rdevice)) || isOnline;
            }
        }

        // Websocket services and Smartthings service
        if ((isOnline | getArtMode2022()) && configuration.isWebsocketProtocol()) {
            createService(RemoteControllerService.SERVICE_NAME, "");
            if (!configuration.getSmartThingsApiKey().isBlank()) {
                createService(SmartThingsApiService.SERVICE_NAME, "");
            }
        }

        if (isOnline) {
            putOnline();
        } else {
            putOffline();
        }
    }

    /**
     * Create or restart existing Samsung TV service.
     * udn is used to determine whether to start upnp service or websocket
     *
     * @param type
     * @param udn
     * @param modelName
     * @return true if service restated or created, false otherwise
     */
    private synchronized boolean createService(String type, String udn) {
        Optional<SamsungTvService> service = findServiceInstance(type);

        if (service.isPresent()) {
            if ((!udn.isBlank() && service.get().isUpnp()) || (udn.isBlank() && !service.get().isUpnp())) {
                logger.debug("{}: Service rediscovered, clearing caches: {}, {} ({})", host, getModelName(), type, udn);
                service.get().clearCache();
                return true;
            }
            return false;
        }

        service = createNewService(type, udn);
        if (service.isPresent()) {
            startService(service.get());
            logger.debug("{}: Started service for: {}, {} ({})", host, getModelName(), type, udn);
            return true;
        }
        logger.trace("{}: Skipping unknown service: {}, {} ({})", host, modelName, type, udn);
        return false;
    }

    /**
     * Create Samsung TV service.
     * udn is used to determine whether to start upnp service or websocket
     *
     * @param type
     * @param udn
     * @return service or null
     */
    private synchronized Optional<SamsungTvService> createNewService(String type, String udn) {
        Optional<SamsungTvService> service = Optional.empty();

        switch (type) {
            case MainTVServerService.SERVICE_NAME:
                service = Optional.of(new MainTVServerService(upnpIOService, udn, host, this));
                break;
            case MediaRendererService.SERVICE_NAME:
                service = Optional.of(new MediaRendererService(upnpIOService, udn, host, this));
                break;
            case RemoteControllerService.SERVICE_NAME:
                try {
                    if (configuration.isWebsocketProtocol() && !udn.isEmpty()) {
                        throw new RemoteControllerException("config is websocket - ignoring UPNP service");
                    }
                    service = Optional
                            .of(new RemoteControllerService(host, configuration.getPort(), !udn.isEmpty(), this));
                } catch (RemoteControllerException e) {
                    logger.warn("{}: Not creating remote controller service: {}", host, e.getMessage());
                }
                break;
            case SmartThingsApiService.SERVICE_NAME:
                service = Optional.of(new SmartThingsApiService(host, this));
                break;
        }
        return service;
    }

    public synchronized Optional<SamsungTvService> findServiceInstance(String serviceName) {
        return services.stream().filter(a -> a.getServiceName().equals(serviceName)).findFirst();
    }

    private synchronized void startService(SamsungTvService service) {
        service.start();
        services.add(service);
    }

    private synchronized void stopService(SamsungTvService service) {
        if (isFrame2022() && service.getServiceName().equals(RemoteControllerService.SERVICE_NAME)) {
            // don't stop the remoteController service on 2022 frame TV's
            logger.debug("{}: not stopping: {}", host, service.getServiceName());
            return;
        }
        service.stop();
        services.remove(service);
    }

    @Override
    public void remoteDeviceAdded(@Nullable Registry registry, @Nullable RemoteDevice device) {
        if (device != null && host.equals(Utils.getHost(device))) {
            logger.debug("{}: remoteDeviceAdded: {}, {}, upnpUDN={}", host, Utils.getType(device),
                    device.getIdentity().getDescriptorURL(), Utils.getUdn(device));
            initializeConfig();
            checkAndCreateServices();
        }
    }

    @Override
    public void remoteDeviceRemoved(@Nullable Registry registry, @Nullable RemoteDevice device) {
        if (device != null && host.equals(Utils.getHost(device))) {
            if (services.stream().anyMatch(s -> s.getServiceName().equals(Utils.getType(device)))) {
                logger.debug("{}: Device removed: {}, udn={}", host, Utils.getType(device), Utils.getUdn(device));
                shutdown();
            }
        }
    }

    @Override
    public void remoteDeviceUpdated(@Nullable Registry registry, @Nullable RemoteDevice device) {
    }

    @Override
    public void remoteDeviceDiscoveryStarted(@Nullable Registry registry, @Nullable RemoteDevice device) {
    }

    @Override
    public void remoteDeviceDiscoveryFailed(@Nullable Registry registry, @Nullable RemoteDevice device,
            @Nullable Exception ex) {
    }

    @Override
    public void localDeviceAdded(@Nullable Registry registry, @Nullable LocalDevice device) {
    }

    @Override
    public void localDeviceRemoved(@Nullable Registry registry, @Nullable LocalDevice device) {
    }

    @Override
    public void beforeShutdown(@Nullable Registry registry) {
    }

    @Override
    public void afterShutdown() {
    }

    public boolean isFrame2022() {
        return getArtMode2022() || (getArtModeSupported() && artApiVersion >= 1);
    }

    public void setOffline() {
        // schedule this in the future to allow calling service to return immediately
        scheduler.submit(this::shutdown);
    }

    public void putConfig(@Nullable String key, @Nullable Object value) {
        if (key != null && value != null) {
            getConfig().put(key, value);
            Configuration config = editConfiguration();
            config.put(key, value);
            updateConfiguration(config);
            logger.debug("{}: Updated Configuration {}:{}", host, key, value);
            configuration = getConfigAs(SamsungTvConfiguration.class);
        }
    }

    public ScheduledExecutorService getScheduler() {
        return scheduler;
    }

    public WebSocketFactory getWebSocketFactory() {
        return webSocketFactory;
    }
}
