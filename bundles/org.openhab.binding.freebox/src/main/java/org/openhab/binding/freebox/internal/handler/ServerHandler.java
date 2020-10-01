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
import static org.openhab.core.library.unit.SmartHomeUnits.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.freebox.internal.action.ServerActions;
import org.openhab.binding.freebox.internal.api.APIRequests;
import org.openhab.binding.freebox.internal.api.ApiManager;
import org.openhab.binding.freebox.internal.api.FreeboxException;
import org.openhab.binding.freebox.internal.api.model.ConnectionStatus;
import org.openhab.binding.freebox.internal.api.model.ConnectionStatus.Media;
import org.openhab.binding.freebox.internal.api.model.FtpConfig;
import org.openhab.binding.freebox.internal.api.model.FtthStatus;
import org.openhab.binding.freebox.internal.api.model.LanConfig;
import org.openhab.binding.freebox.internal.api.model.LanConfig.NetworkMode;
import org.openhab.binding.freebox.internal.api.model.LanHost;
import org.openhab.binding.freebox.internal.api.model.LanInterface;
import org.openhab.binding.freebox.internal.api.model.SambaConfig;
import org.openhab.binding.freebox.internal.api.model.SystemConfig;
import org.openhab.binding.freebox.internal.api.model.UPnPAVConfig;
import org.openhab.binding.freebox.internal.api.model.WifiConfig;
import org.openhab.binding.freebox.internal.api.model.XdslStatus;
import org.openhab.binding.freebox.internal.config.ServerConfiguration;
import org.openhab.binding.freebox.internal.discovery.FreeboxDiscoveryService;
import org.openhab.binding.freebox.internal.discovery.PlayerDiscoveryParticipant;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.library.dimension.DataTransferRate;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link ServerHandler} handle common parts of Freebox bridges.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public abstract class ServerHandler extends BaseBridgeHandler {
    private final Logger logger = LoggerFactory.getLogger(ServerHandler.class);
    private final BigDecimal HUNDRED = BigDecimal.valueOf(100);
    private @NonNullByDefault({}) ScheduledFuture<?> globalJob;

    protected @NonNullByDefault({}) ApiManager apiManager;
    private ConnectionStatus.Media media = Media.UNKNOWN;
    private NetworkMode networkMode = NetworkMode.UNKNOWN;
    private @NonNullByDefault({}) QuantityType<?> bandwidthUp;
    private @NonNullByDefault({}) QuantityType<?> bandwidthDown;

    private long uptime = -1;
    private final Gson gson;

    public ServerHandler(Bridge thing, Gson gson) {
        super(thing);
        this.gson = gson;
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Freebox Server handler for thing {}.", getThing().getUID());
        ServerConfiguration configuration = getConfigAs(ServerConfiguration.class);

        if (!configuration.isValidToken()) {
            updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.CONFIGURATION_PENDING,
                    "Please accept pairing request directly on your freebox");
        } else {
            updateStatus(ThingStatus.UNKNOWN);
        }

        logger.debug("Binding will schedule a job to establish a connection...");

        scheduler.submit(() -> {
            try {
                apiManager = new ApiManager(configuration, gson);
                String appToken = apiManager.getAppToken();
                if (!configuration.isValidToken() && appToken != null) {
                    logger.debug("Store new app token in the thing configuration");
                    configuration.appToken = appToken;
                    Configuration thingConfig = editConfiguration();
                    thingConfig.put(ServerConfiguration.APP_TOKEN, appToken);
                    updateConfiguration(thingConfig);
                }
                if (thing.getProperties().isEmpty()) {
                    discoverAttributes();
                }
                media = Media.valueOf(editProperties().get(PROPERTY_MEDIA));

                fetchLanConfig();

                if (globalJob == null || globalJob.isCancelled()) {
                    logger.debug("Scheduling state update every {} seconds...", configuration.refreshInterval);
                    globalJob = scheduler.scheduleWithFixedDelay(() -> {
                        try {
                            internalPoll();
                            updateStatus(ThingStatus.ONLINE);
                        } catch (FreeboxException e) {
                            logger.debug("Thing {}: exception : {}", getThing().getUID(), e);
                            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
                        }
                    }, 5, configuration.refreshInterval, TimeUnit.SECONDS);
                }
            } catch (FreeboxException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
            }
        });
    }

    protected void discoverAttributes() throws FreeboxException {
        SystemConfig systemConfig = apiManager.execute(new APIRequests.SystemConfig());
        ConnectionStatus connectionStatus = apiManager.execute(new APIRequests.ConnectionStatus());

        this.media = connectionStatus.getMedia();

        // Gather various generic informations regarding the 'server' thing
        final Map<String, String> properties = new HashMap<>();
        properties.put(PROPERTY_MEDIA, media.name());
        properties.put(Thing.PROPERTY_VENDOR, "Freebox SAS");
        properties.put(Thing.PROPERTY_SERIAL_NUMBER, systemConfig.getSerial());
        properties.put(Thing.PROPERTY_HARDWARE_VERSION, systemConfig.getBoardName());
        properties.put(Thing.PROPERTY_FIRMWARE_VERSION, systemConfig.getFirmwareVersion());
        properties.put(Thing.PROPERTY_MAC_ADDRESS, systemConfig.getMac());
        updateProperties(properties);

        // Gather the list of fans
        ThingBuilder thingBuilder = editThing();
        List<Channel> channels = new ArrayList<>(getThing().getChannels());
        systemConfig.getFans().forEach(fan -> {
            Channel channel = ChannelBuilder
                    .create(new ChannelUID(thing.getUID(), PROPERTY_SENSORS, fan.getId()), CoreItemFactory.NUMBER)
                    .withLabel(fan.getName()).withType(new ChannelTypeUID("freebox:fanspeed")).build();
            channels.add(channel);
        });

        // Gather the list of temperature sensors
        systemConfig.getSensors().forEach(sensor -> {
            Channel channel = ChannelBuilder
                    .create(new ChannelUID(thing.getUID(), PROPERTY_SENSORS, sensor.getId()), "Number:Temperature")
                    .withLabel(sensor.getName()).withType(new ChannelTypeUID("freebox:temperature")).build();
            channels.add(channel);
        });

        switch (media) {
            case FTTH:
                Channel ftthChannel = ChannelBuilder
                        .create(new ChannelUID(thing.getUID(), CONNECTION_STATUS, FTTH_STATUS), CoreItemFactory.SWITCH)
                        .withType(new ChannelTypeUID("freebox:" + FTTH_STATUS)).build();
                channels.add(ftthChannel);

                break;
            case XDSL:
                Channel xdslChannel = ChannelBuilder
                        .create(new ChannelUID(thing.getUID(), CONNECTION_STATUS, XDSL_STATUS), CoreItemFactory.STRING)
                        .withType(new ChannelTypeUID("freebox:" + XDSL_STATUS)).build();
                channels.add(xdslChannel);
                break;
            default:
                throw new FreeboxException("No internet connection media discovered");
        }

        thingBuilder.withChannels(channels);
        updateThing(thingBuilder.build());
    }

    private void fetchLanConfig() throws FreeboxException {
        LanConfig response = apiManager.execute(new APIRequests.LanConfig());
        networkMode = response.getType();
    }

    private void fetchConnectionStatus() throws FreeboxException {
        ConnectionStatus connectionStatus = apiManager.execute(new APIRequests.ConnectionStatus());
        updateChannelString(CONNECTION_STATUS, LINE_STATUS, connectionStatus.getState().name());
        updateChannelString(CONNECTION_STATUS, IPV4, connectionStatus.getIpv4());

        if (bandwidthUp == null || bandwidthDown == null) {
            bandwidthUp = new QuantityType<>(connectionStatus.getBandwidthUp(), BIT_PER_SECOND);
            bandwidthDown = new QuantityType<>(connectionStatus.getBandwidthDown(), BIT_PER_SECOND);
            updateChannelQuantity(CONNECTION_STATUS, BW_UP, bandwidthUp, MEGABIT_PER_SECOND);
            updateChannelQuantity(CONNECTION_STATUS, BW_DOWN, bandwidthDown, MEGABIT_PER_SECOND);
        }

        QuantityType<DataTransferRate> rateUp = new QuantityType<>(connectionStatus.getRateUp() * 8, BIT_PER_SECOND);
        updateChannelQuantity(CONNECTION_STATUS, RATE_UP, rateUp, KILOBIT_PER_SECOND);
        updateChannelQuantity(CONNECTION_STATUS, PCT_BW_UP, rateUp.multiply(HUNDRED).divide(bandwidthUp), PERCENT);

        QuantityType<DataTransferRate> rateDown = new QuantityType<>(connectionStatus.getRateDown() * 8,
                BIT_PER_SECOND);
        updateChannelQuantity(CONNECTION_STATUS, RATE_DOWN, rateDown, KILOBIT_PER_SECOND);
        updateChannelQuantity(CONNECTION_STATUS, PCT_BW_DOWN, rateDown.multiply(HUNDRED).divide(bandwidthDown),
                PERCENT);

        updateChannelQuantity(CONNECTION_STATUS, BYTES_UP, new QuantityType<>(connectionStatus.getBytesUp(), OCTET),
                GIBIOCTET);
        updateChannelQuantity(CONNECTION_STATUS, BYTES_DOWN, new QuantityType<>(connectionStatus.getBytesDown(), OCTET),
                GIBIOCTET);
    }

    private void fetchSystemConfig() throws FreeboxException {
        SystemConfig systemConfig = apiManager.execute(new APIRequests.SystemConfig());

        systemConfig.getFans().forEach(fan -> {
            updateChannelQuantity(PROPERTY_SENSORS, fan.getId(), fan.getValue(), SIUnits.CELSIUS);
        });

        systemConfig.getSensors().forEach(sensor -> {
            updateChannelDecimal(PROPERTY_SENSORS, sensor.getId(), sensor.getValue());
        });
        long newUptime = systemConfig.getUptimeVal();
        if (newUptime < uptime) {
            triggerChannel(new ChannelUID(getThing().getUID(), SYS_INFO, BOX_EVENT), "restarted");
            Map<String, String> properties = editProperties();
            if (!properties.get(Thing.PROPERTY_FIRMWARE_VERSION).equals(systemConfig.getFirmwareVersion())) {
                properties.put(Thing.PROPERTY_FIRMWARE_VERSION, systemConfig.getFirmwareVersion());
                triggerChannel(new ChannelUID(getThing().getUID(), SYS_INFO, BOX_EVENT), "firmware_updated");
                updateProperties(properties);
            }
        }
        uptime = newUptime;
        updateChannelQuantity(SYS_INFO, UPTIME, uptime, SECOND);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof OnOffType || command instanceof OpenClosedType || command instanceof UpDownType) {
            boolean enable = command.equals(OnOffType.ON) || command.equals(UpDownType.UP)
                    || command.equals(OpenClosedType.OPEN);
            try {
                switch (channelUID.getIdWithoutGroup()) {
                    case WIFI_STATUS:
                        updateChannelOnOff(ACTIONS, WIFI_STATUS, enableWifi(enable));
                        break;
                    case FTP_STATUS:
                        updateChannelOnOff(ACTIONS, FTP_STATUS, enableFtp(enable));
                        break;
                    case SAMBA_FILE_STATUS:
                        updateChannelOnOff(SAMBA, SAMBA_FILE_STATUS, enableSambaFileShare(enable));
                        break;
                    case SAMBA_PRINTER_STATUS:
                        updateChannelOnOff(SAMBA, SAMBA_PRINTER_STATUS, enableSambaPrintShare(enable));
                        break;
                    case UPNPAV_STATUS:
                        updateChannelOnOff(ACTIONS, UPNPAV_STATUS, enableUPnPAV(enable));
                        break;
                }
            } catch (FreeboxException e) {
                logger.debug("Thing {}: invalid command {} on channel {}", getThing().getUID(), command,
                        channelUID.getId());
            }
        }
    }

    @Override
    public void dispose() {
        logger.debug("Disposing Freebox Server handler for thing {}", getThing().getUID());
        if (globalJob != null && !globalJob.isCancelled()) {
            globalJob.cancel(true);
            globalJob = null;
        }
        if (apiManager != null) {
            apiManager.closeSession();
        }
        super.dispose();
    }

    private void fetchLinePresence() throws FreeboxException {
        if (media == Media.FTTH) {
            FtthStatus response = apiManager.execute(new APIRequests.FtthStatus());
            updateChannelOnOff(CONNECTION_STATUS, FTTH_STATUS, response.getSfpHasSignal());
        } else {
            XdslStatus response = apiManager.execute(new APIRequests.XdslStatus());
            updateChannelString(CONNECTION_STATUS, XDSL_STATUS, response.getStatus());

        }
    }

    private void fetchFtpConfig() throws FreeboxException {
        FtpConfig response = apiManager.execute(new APIRequests.GetFtpConfig());
        updateChannelOnOff(ACTIONS, FTP_STATUS, response.isEnabled());
    }

    private void fetchUPnPAVConfig() throws FreeboxException {
        if (networkMode != NetworkMode.BRIDGE) {
            UPnPAVConfig response = apiManager.execute(new APIRequests.GetUPnPAVConfig());
            updateChannelOnOff(PLAYER_ACTIONS, UPNPAV_STATUS, response.isEnabled());
        }
    }

    public boolean enableFtp(boolean enable) throws FreeboxException {
        FtpConfig config = apiManager.execute(new APIRequests.GetFtpConfig());
        config.setEnabled(enable);
        config = apiManager.execute(new APIRequests.SetFtpConfig(config));
        return config.isEnabled();
    }

    public boolean enableWifi(boolean enable) throws FreeboxException {
        WifiConfig config = apiManager.execute(new APIRequests.GetWifiConfig());
        config.setEnabled(enable);
        config = apiManager.execute(new APIRequests.SetWifiConfig(config));
        return config.isEnabled();
    }

    private void fetchWifiConfig() throws FreeboxException {
        WifiConfig wifiConfig = apiManager.execute(new APIRequests.GetWifiConfig());
        updateChannelOnOff(ACTIONS, WIFI_STATUS, wifiConfig.isEnabled());
    }

    private void fetchSambaConfig() throws FreeboxException {
        SambaConfig response = apiManager.execute(new APIRequests.GetSambaConfig());
        updateChannelOnOff(SAMBA, SAMBA_FILE_STATUS, response.isFileShareEnabled());
        updateChannelOnOff(SAMBA, SAMBA_PRINTER_STATUS, response.isPrintShareEnabled());
    }

    public boolean enableSambaFileShare(boolean enable) throws FreeboxException {
        SambaConfig config = apiManager.execute(new APIRequests.GetSambaConfig());
        config.setFileShareEnabled(enable);
        config = apiManager.execute(new APIRequests.SetSambaConfig(config));
        return config.isFileShareEnabled();
    }

    public boolean enableSambaPrintShare(boolean enable) throws FreeboxException {
        SambaConfig config = apiManager.execute(new APIRequests.GetSambaConfig());
        config.setPrintShareEnabled(enable);
        config = apiManager.execute(new APIRequests.SetSambaConfig(config));
        return config.isPrintShareEnabled();
    }

    public boolean enableUPnPAV(boolean enable) throws FreeboxException {
        UPnPAVConfig config = apiManager.execute(new APIRequests.GetUPnPAVConfig());
        config.setEnabled(enable);
        config = apiManager.execute(new APIRequests.SetUPnPAVConfig(config));
        return config.isEnabled();
    }

    public void reboot() {
        try {
            apiManager.execute(new APIRequests.Reboot());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.DUTY_CYCLE);
            scheduler.schedule(this::initialize, 2, TimeUnit.MINUTES);
        } catch (FreeboxException e) {
            logger.debug("Thing {}: error rebooting server", getThing().getUID());
        }
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.unmodifiableList(
                Arrays.asList(ServerActions.class, FreeboxDiscoveryService.class, PlayerDiscoveryParticipant.class));
    }

    public ServerConfiguration getConfiguration() {
        return getConfigAs(ServerConfiguration.class);
    }

    public ApiManager getApiManager() {
        return apiManager;
    }

    protected void updateChannelQuantity(String group, String channelId, QuantityType<?> qtty, Unit<?> unit) {
        updateChannelQuantity(group, channelId, qtty.toUnit(unit));
    }

    protected void updateChannelQuantity(String group, String channelId, double d, Unit<?> unit) {
        updateChannelQuantity(group, channelId, new QuantityType<>(d, unit));
    }

    protected void updateChannelQuantity(String group, String channelId, @Nullable QuantityType<?> quantity) {
        ChannelUID id = new ChannelUID(getThing().getUID(), group, channelId);
        if (isLinked(id)) {
            updateState(id, quantity != null ? quantity : UnDefType.NULL);
        }
    }

    protected void updateChannelString(String group, String channelId, String value) {
        ChannelUID id = new ChannelUID(getThing().getUID(), group, channelId);
        if (isLinked(id)) {
            updateState(id, new StringType(value));
        }
    }

    protected void updateChannelOnOff(String group, String channelId, boolean value) {
        ChannelUID id = new ChannelUID(getThing().getUID(), group, channelId);
        if (isLinked(id)) {
            updateState(id, OnOffType.from(value));
        }
    }

    protected void updateChannelDecimal(String group, String channelId, int value) {
        ChannelUID id = new ChannelUID(getThing().getUID(), group, channelId);
        if (isLinked(id)) {
            updateState(id, new DecimalType(value));
        }
    }

    public List<LanHost> getLanHosts() throws FreeboxException {
        List<LanHost> hosts = new ArrayList<>();
        List<LanInterface> lans = getApiManager().execute(new APIRequests.LanInterfaces());
        lans.stream().filter(LanInterface::hasHosts).forEach(lan -> {
            try {
                List<LanHost> lanHosts = getApiManager().execute(new APIRequests.LanHosts(lan.getName()));
                hosts.addAll(lanHosts);
            } catch (FreeboxException e) {
                logger.warn("Error getting hosts for interface {}", lan.getName());
            }
        });
        return hosts;
    }

    public NetworkMode getNetworkMode() {
        return networkMode;
    }

    protected void internalPoll() throws FreeboxException {
        logger.debug("Polling server state...");
        fetchConnectionStatus();
        fetchSystemConfig();
        fetchWifiConfig();
        fetchLinePresence();
        fetchFtpConfig();
        fetchUPnPAVConfig();
        fetchSambaConfig();
    }

    public @Nullable String getAppToken() {
        return apiManager.getAppToken();
    }
}
