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

import static org.eclipse.smarthome.core.library.unit.SmartHomeUnits.*;
import static org.openhab.binding.freebox.internal.FreeboxBindingConstants.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.dimension.DataTransferRate;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerService;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.freebox.internal.action.ServerActions;
import org.openhab.binding.freebox.internal.api.ApiManager;
import org.openhab.binding.freebox.internal.api.FreeboxException;
import org.openhab.binding.freebox.internal.api.model.ConnectionStatus;
import org.openhab.binding.freebox.internal.api.model.ConnectionStatus.Media;
import org.openhab.binding.freebox.internal.api.model.ConnectionStatusResponse;
import org.openhab.binding.freebox.internal.api.model.FtpConfig;
import org.openhab.binding.freebox.internal.api.model.FtpConfigResponse;
import org.openhab.binding.freebox.internal.api.model.FtthStatus;
import org.openhab.binding.freebox.internal.api.model.FtthStatusResponse;
import org.openhab.binding.freebox.internal.api.model.LanConfig;
import org.openhab.binding.freebox.internal.api.model.LanConfig.NetworkMode;
import org.openhab.binding.freebox.internal.api.model.LanConfigResponse;
import org.openhab.binding.freebox.internal.api.model.LanHost;
import org.openhab.binding.freebox.internal.api.model.LanHostsResponse;
import org.openhab.binding.freebox.internal.api.model.LanInterface;
import org.openhab.binding.freebox.internal.api.model.LanInterfacesResponse;
import org.openhab.binding.freebox.internal.api.model.RebootResponse;
import org.openhab.binding.freebox.internal.api.model.SambaConfig;
import org.openhab.binding.freebox.internal.api.model.SambaConfigResponse;
import org.openhab.binding.freebox.internal.api.model.SystemConfig;
import org.openhab.binding.freebox.internal.api.model.SystemConfigResponse;
import org.openhab.binding.freebox.internal.api.model.UPnPAVConfig;
import org.openhab.binding.freebox.internal.api.model.UPnPAVConfigResponse;
import org.openhab.binding.freebox.internal.api.model.WifiConfig;
import org.openhab.binding.freebox.internal.api.model.WifiConfigResponse;
import org.openhab.binding.freebox.internal.api.model.XdslStatus;
import org.openhab.binding.freebox.internal.api.model.XdslStatusResponse;
import org.openhab.binding.freebox.internal.config.ServerConfiguration;
import org.openhab.binding.freebox.internal.discovery.FreeboxDiscoveryService;
import org.openhab.binding.freebox.internal.discovery.PlayerDiscoveryParticipant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    // private @Nullable String baseAddress;
    protected @NonNullByDefault({}) ApiManager apiManager;
    private ConnectionStatus.Media media = Media.UNKNOWN;
    private NetworkMode networkMode = NetworkMode.UNKNOWN;
    private @NonNullByDefault({}) QuantityType<DataTransferRate> bandwidthUp;
    private @NonNullByDefault({}) QuantityType<DataTransferRate> bandwidthDown;

    private long uptime = -1;

    public ServerHandler(Bridge thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Freebox Server handler for thing {}.", getThing().getUID());
        ServerConfiguration configuration = getConfigAs(ServerConfiguration.class);

        updateStatus(ThingStatus.UNKNOWN);

        logger.debug("Binding will schedule a job to establish a connection...");

        scheduler.submit(() -> {
            try {
                apiManager = new ApiManager(configuration);
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
                            logger.debug("Thing {}: exception : {} {}", getThing().getUID(), e.getMessage(), e);
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
        SystemConfig systemConfig = apiManager.executeGet(SystemConfigResponse.class, null);
        ConnectionStatus connectionStatus = apiManager.executeGet(ConnectionStatusResponse.class, null);

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
                    .create(new ChannelUID(thing.getUID(), PROPERTY_SENSORS, fan.getId()), "Number")
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
                        .create(new ChannelUID(thing.getUID(), CONNECTION_STATUS, FTTH_STATUS), "Switch")
                        .withType(new ChannelTypeUID("freebox:" + FTTH_STATUS)).build();
                channels.add(ftthChannel);

                break;
            case XDSL:
                Channel xdslChannel = ChannelBuilder
                        .create(new ChannelUID(thing.getUID(), CONNECTION_STATUS, XDSL_STATUS), "String")
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
        LanConfig response = apiManager.executeGet(LanConfigResponse.class, null);
        networkMode = response.getType();
    }

    private void fetchConnectionStatus() throws FreeboxException {
        ConnectionStatus connectionStatus = apiManager.executeGet(ConnectionStatusResponse.class, null);
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
        SystemConfig systemConfig = apiManager.executeGet(SystemConfigResponse.class, null);

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
        apiManager.closeSession();
        super.dispose();
    }

    private void fetchLinePresence() throws FreeboxException {
        if (media == Media.FTTH) {
            FtthStatus response = apiManager.executeGet(FtthStatusResponse.class, null);
            updateChannelOnOff(CONNECTION_STATUS, FTTH_STATUS, response.getSfpHasSignal());
        } else {
            XdslStatus response = apiManager.executeGet(XdslStatusResponse.class, null);
            updateChannelString(CONNECTION_STATUS, XDSL_STATUS, response.getStatus());

        }
    }

    private void fetchWifiConfig() throws FreeboxException {
        WifiConfig wifiConfig = apiManager.executeGet(WifiConfigResponse.class, null);
        updateChannelOnOff(ACTIONS, WIFI_STATUS, wifiConfig.isEnabled());
    }

    private void fetchFtpConfig() throws FreeboxException {
        FtpConfig response = apiManager.executeGet(FtpConfigResponse.class, null);
        updateChannelOnOff(ACTIONS, FTP_STATUS, response.isEnabled());
    }

    private void fetchUPnPAVConfig() throws FreeboxException {
        if (networkMode != NetworkMode.BRIDGE) {
            UPnPAVConfig response = apiManager.executeGet(UPnPAVConfigResponse.class, null);
            updateChannelOnOff(PLAYER_ACTIONS, UPNPAV_STATUS, response.isEnabled());
        }
    }

    private void fetchSambaConfig() throws FreeboxException {
        SambaConfig response = apiManager.executeGet(SambaConfigResponse.class, null);
        updateChannelOnOff(SAMBA, SAMBA_FILE_STATUS, response.isFileShareEnabled());
        updateChannelOnOff(SAMBA, SAMBA_PRINTER_STATUS, response.isPrintShareEnabled());
    }

    public boolean enableWifi(boolean enable) throws FreeboxException {
        WifiConfig config = new WifiConfig();
        config.setEnabled(enable);
        config = apiManager.execute(config, null);
        return config.isEnabled();
    }

    public boolean enableFtp(boolean enable) throws FreeboxException {
        FtpConfig config = new FtpConfig();
        config.setEnabled(enable);
        config = apiManager.execute(config, null);
        return config.isEnabled();
    }

    public boolean enableSambaFileShare(boolean enable) throws FreeboxException {
        SambaConfig config = new SambaConfig();
        config.setFileShareEnabled(enable);
        config = apiManager.execute(config, null);
        return config.isFileShareEnabled();
    }

    public boolean enableUPnPAV(boolean enable) throws FreeboxException {
        UPnPAVConfig config = new UPnPAVConfig();
        config.setEnabled(enable);
        config = apiManager.execute(config, null);
        return config.isEnabled();
    }

    public boolean enableSambaPrintShare(boolean enable) throws FreeboxException {
        SambaConfig config = new SambaConfig();
        config.setPrintShareEnabled(enable);
        config = apiManager.execute(config, null);
        return config.isPrintShareEnabled();
    }

    public void reboot() {
        try {
            apiManager.executePost(RebootResponse.class, null, null);
        } catch (FreeboxException e) {
            logger.debug("Thing {}: error rebooting server", getThing().getUID());
        }
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.unmodifiableList(
                Stream.of(ServerActions.class, FreeboxDiscoveryService.class, PlayerDiscoveryParticipant.class)
                        .collect(Collectors.toList()));

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
        List<LanInterface> lans = getApiManager().executeGet(LanInterfacesResponse.class, null);
        lans.stream().filter(LanInterface::hasHosts).forEach(lan -> {
            try {
                List<LanHost> lanHosts = getApiManager().executeGet(LanHostsResponse.class, lan.getName());
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
}
