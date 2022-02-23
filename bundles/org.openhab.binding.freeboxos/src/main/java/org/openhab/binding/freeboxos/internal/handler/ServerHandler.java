/**
<<<<<<< Upstream, based on origin/main
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.freeboxos.internal.handler;

import static org.openhab.binding.freeboxos.internal.FreeboxOsBindingConstants.*;
import static org.openhab.core.library.unit.Units.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freeboxos.internal.action.ServerActions;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.rest.AfpManager;
import org.openhab.binding.freeboxos.internal.api.rest.AirMediaManager;
import org.openhab.binding.freeboxos.internal.api.rest.ConnectionManager;
import org.openhab.binding.freeboxos.internal.api.rest.ConnectionManager.Status;
import org.openhab.binding.freeboxos.internal.api.rest.FtpManager;
import org.openhab.binding.freeboxos.internal.api.rest.LanBrowserManager.Source;
import org.openhab.binding.freeboxos.internal.api.rest.LanManager;
import org.openhab.binding.freeboxos.internal.api.rest.LanManager.LanConfig;
import org.openhab.binding.freeboxos.internal.api.rest.SambaManager;
import org.openhab.binding.freeboxos.internal.api.rest.SambaManager.Samba;
import org.openhab.binding.freeboxos.internal.api.rest.SystemManager;
import org.openhab.binding.freeboxos.internal.api.rest.SystemManager.Config;
import org.openhab.binding.freeboxos.internal.api.rest.UPnPAVManager;
import org.openhab.binding.freeboxos.internal.api.rest.WifiManager;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ServerHandler} handle common parts of Freebox bridges.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class ServerHandler extends ApiConsumerHandler implements FreeDeviceIntf {
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private final Logger logger = LoggerFactory.getLogger(ServerHandler.class);
    private final ChannelUID eventChannelUID;

    private long uptime = -1;

    public ServerHandler(Thing thing) {
        super(thing);
        eventChannelUID = new ChannelUID(getThing().getUID(), SYS_INFO, BOX_EVENT);
    }

    @Override
    void initializeProperties(Map<String, String> properties) throws FreeboxException {
        LanConfig lanConfig = getManager(LanManager.class).getConfig();
        Config config = getManager(SystemManager.class).getConfig();
        properties.put(Thing.PROPERTY_SERIAL_NUMBER, config.serial());
        properties.put(Thing.PROPERTY_FIRMWARE_VERSION, config.firmwareVersion());
        properties.put(Thing.PROPERTY_HARDWARE_VERSION, config.modelInfo().prettyName());
        properties.put(Source.UPNP.name(), lanConfig.name());

        List<Channel> channels = new ArrayList<>(getThing().getChannels());
        config.sensors().forEach(sensor -> {
            ChannelUID sensorId = new ChannelUID(thing.getUID(), GROUP_SENSORS, sensor.id());
            channels.add(
                    ChannelBuilder.create(sensorId).withLabel(sensor.name()).withAcceptedItemType("Number:Temperature")
                            .withType(new ChannelTypeUID(BINDING_ID + ":temperature")).build());
        });
        config.fans().forEach(sensor -> {
            ChannelUID sensorId = new ChannelUID(thing.getUID(), GROUP_FANS, sensor.id());
            channels.add(ChannelBuilder.create(sensorId).withLabel(sensor.name())
                    .withAcceptedItemType(CoreItemFactory.NUMBER).withType(new ChannelTypeUID(BINDING_ID + ":fanspeed"))
                    .build());
        });
        updateThing(editThing().withChannels(channels).build());
    }

    @Override
    protected void internalPoll() throws FreeboxException {
        logger.debug("Polling server state...");
        fetchConnectionStatus();
        fetchSystemConfig();

        updateChannelOnOff(ACTIONS, WIFI_STATUS, getManager(WifiManager.class).getStatus());
        updateChannelOnOff(ACTIONS, AIRMEDIA_STATUS, getManager(AirMediaManager.class).getStatus());
        updateChannelOnOff(ACTIONS, UPNPAV_STATUS, getManager(UPnPAVManager.class).getStatus());

        Samba response = getManager(SambaManager.class).getConfig();
        updateChannelOnOff(FILE_SHARING, SAMBA_FILE_STATUS, response.fileShareEnabled());
        updateChannelOnOff(FILE_SHARING, SAMBA_PRINTER_STATUS, response.printShareEnabled());
        updateChannelOnOff(FILE_SHARING, FTP_STATUS, getManager(FtpManager.class).getStatus());
        updateChannelOnOff(FILE_SHARING, AFP_FILE_STATUS, getManager(AfpManager.class).getStatus());
    }

    private void fetchSystemConfig() throws FreeboxException {
        Config config = getManager(SystemManager.class).getConfig();

        config.sensors().forEach(sensor -> updateChannelDecimal(GROUP_SENSORS, sensor.id(), sensor.value()));
        config.fans().forEach(fan -> updateChannelDecimal(GROUP_FANS, fan.id(), fan.value()));

        uptime = checkUptimeAndFirmware(config.uptimeVal(), uptime, config.firmwareVersion());
        updateChannelQuantity(SYS_INFO, UPTIME, uptime, Units.SECOND);

        LanConfig lanConfig = getManager(LanManager.class).getConfig();
        updateChannelString(SYS_INFO, IP_ADDRESS, lanConfig.ip());
    }

    private void fetchConnectionStatus() throws FreeboxException {
        Status status = getManager(ConnectionManager.class).getConfig();
        updateChannelString(CONNECTION_STATUS, LINE_STATUS, status.state());
        updateChannelString(CONNECTION_STATUS, LINE_TYPE, status.type());
        updateChannelString(CONNECTION_STATUS, LINE_MEDIA, status.media());
        updateChannelString(CONNECTION_STATUS, IP_ADDRESS, status.ipv4());
        updateChannelString(CONNECTION_STATUS, IPV6_ADDRESS, status.ipv6());

        QuantityType<?> rateUp = new QuantityType<>(status.rateUp() * 8, Units.BIT_PER_SECOND);
        updateChannelQuantity(CONNECTION_STATUS, RATE_UP, rateUp, KILOBIT_PER_SECOND);

        QuantityType<?> rateDown = new QuantityType<>(status.rateDown() * 8, Units.BIT_PER_SECOND);
        updateChannelQuantity(CONNECTION_STATUS, RATE_DOWN, rateDown, KILOBIT_PER_SECOND);

        QuantityType<?> bandwidthUp = new QuantityType<>(status.bandwidthUp(), BIT_PER_SECOND);
        updateChannelQuantity(CONNECTION_STATUS, BW_UP, bandwidthUp, KILOBIT_PER_SECOND);

        QuantityType<?> bandwidthDown = new QuantityType<>(status.bandwidthDown(), BIT_PER_SECOND);
        updateChannelQuantity(CONNECTION_STATUS, BW_DOWN, bandwidthDown, KILOBIT_PER_SECOND);

        updateChannelQuantity(CONNECTION_STATUS, BYTES_UP, new QuantityType<>(status.bytesUp(), OCTET), GIBIOCTET);
        updateChannelQuantity(CONNECTION_STATUS, BYTES_DOWN, new QuantityType<>(status.bytesDown(), OCTET), GIBIOCTET);

        updateChannelQuantity(CONNECTION_STATUS, PCT_BW_UP,
                !bandwidthUp.equals(QuantityType.ZERO) ? rateUp.multiply(HUNDRED).divide(bandwidthUp)
                        : QuantityType.ZERO,
                Units.PERCENT);

        updateChannelQuantity(CONNECTION_STATUS, PCT_BW_DOWN,
                !bandwidthDown.equals(QuantityType.ZERO) ? rateDown.multiply(HUNDRED).divide(bandwidthDown)
                        : QuantityType.ZERO,
                Units.PERCENT);
    }

    @Override
    protected boolean internalHandleCommand(String channelId, Command command) throws FreeboxException {
        if (ON_OFF_CLASSES.contains(command.getClass())) {
            boolean enable = TRUE_COMMANDS.contains(command);
            switch (channelId) {
                case WIFI_STATUS:
                    updateChannelOnOff(ACTIONS, WIFI_STATUS, getManager(WifiManager.class).setStatus(enable));
                    return true;
                case FTP_STATUS:
                    updateChannelOnOff(FILE_SHARING, FTP_STATUS, getManager(FtpManager.class).setStatus(enable));
                    return true;
                case SAMBA_FILE_STATUS:
                    updateChannelOnOff(FILE_SHARING, SAMBA_FILE_STATUS,
                            getManager(SambaManager.class).setFileShare(enable));
                    return true;
                case SAMBA_PRINTER_STATUS:
                    updateChannelOnOff(FILE_SHARING, SAMBA_PRINTER_STATUS,
                            getManager(SambaManager.class).setPrintShare(enable));
                    return true;
                case UPNPAV_STATUS:
                    updateChannelOnOff(ACTIONS, UPNPAV_STATUS, getManager(UPnPAVManager.class).setStatus(enable));
                    return true;
                case AFP_FILE_STATUS:
                    updateChannelOnOff(FILE_SHARING, AFP_FILE_STATUS, getManager(AfpManager.class).setStatus(enable));
                    return true;
                case AIRMEDIA_STATUS:
                    updateChannelOnOff(ACTIONS, AIRMEDIA_STATUS, getManager(AirMediaManager.class).setStatus(enable));
                    return true;
                default:
                    break;
            }
        }
        return super.internalHandleCommand(channelId, command);
    }

    public void reboot() {
        processReboot(() -> {
            try {
                getManager(SystemManager.class).reboot();
            } catch (FreeboxException e) {
                logger.warn("Error rebooting : {}", e.getMessage());
            }
        });
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singleton(ServerActions.class);
    }

    @Override
    public ChannelUID getEventChannelUID() {
        return eventChannelUID;
    }

    @Override
    public void triggerChannel(ChannelUID channelUID, String event) {
        super.triggerChannel(channelUID, event);
=======
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
package org.openhab.binding.freeboxos.internal.handler;

import static org.openhab.binding.freeboxos.internal.FreeboxOsBindingConstants.*;
import static org.openhab.core.library.unit.Units.*;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.freeboxos.internal.action.ServerActions;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.airmedia.AirMediaManager;
import org.openhab.binding.freeboxos.internal.api.connection.ConnectionManager;
import org.openhab.binding.freeboxos.internal.api.connection.ConnectionStatus;
import org.openhab.binding.freeboxos.internal.api.ftp.FtpManager;
import org.openhab.binding.freeboxos.internal.api.lan.ConnectivityData;
import org.openhab.binding.freeboxos.internal.api.lan.LanConfig;
import org.openhab.binding.freeboxos.internal.api.lan.LanConfig.NetworkMode;
import org.openhab.binding.freeboxos.internal.api.lan.LanManager;
import org.openhab.binding.freeboxos.internal.api.lan.NameSource;
import org.openhab.binding.freeboxos.internal.api.netshare.NetShareManager.SambaManager;
import org.openhab.binding.freeboxos.internal.api.netshare.SambaConfig;
import org.openhab.binding.freeboxos.internal.api.system.DeviceConfig;
import org.openhab.binding.freeboxos.internal.api.system.SystemConf;
import org.openhab.binding.freeboxos.internal.api.system.SystemManager;
import org.openhab.binding.freeboxos.internal.api.upnpav.UPnPAVManager;
import org.openhab.binding.freeboxos.internal.api.wifi.WifiManager;
import org.openhab.core.audio.AudioHTTPServer;
import org.openhab.core.library.dimension.DataTransferRate;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ServerHandler} handle common parts of Freebox bridges.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class ServerHandler extends FreeDeviceHandler {
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private final Logger logger = LoggerFactory.getLogger(ServerHandler.class);

    public ServerHandler(Thing thing, AudioHTTPServer audioHTTPServer, @Nullable String ipAddress,
            BundleContext bundleContext) {
        super(thing, audioHTTPServer, ipAddress, bundleContext);
    }

    @Override
    public void dispose() {
        super.dispose();
    }

    @Override
    void internalGetProperties(Map<String, String> properties) throws FreeboxException {
        super.internalGetProperties(properties);
        SystemConf config = getManager(SystemManager.class).getConfig();
        properties.put(Thing.PROPERTY_SERIAL_NUMBER, config.getSerial());
        properties.put(Thing.PROPERTY_FIRMWARE_VERSION, config.getFirmwareVersion());
        properties.put(Thing.PROPERTY_HARDWARE_VERSION, config.getPrettyName().orElse("Unknown"));
        LanConfig lanConfig = getManager(LanManager.class).getConfig();
        properties.put(NameSource.UPNP.name(), lanConfig.getName());
    }

    @Override
    protected void internalPoll() throws FreeboxException {
        super.internalPoll();
        logger.debug("Polling server state...");
        fetchConnectionStatus();
        fetchMediaServerConfig();
        fetchSambaConfig();
        updateChannelOnOff(FILE_SHARING, FTP_STATUS, getManager(FtpManager.class).getStatus());
        updateChannelOnOff(ACTIONS, WIFI_STATUS, getManager(WifiManager.class).getStatus());
    }

    @Override
    protected ConnectivityData fetchConnectivity() throws FreeboxException {
        return getManager(LanManager.class).getConfig();
    }

    private void fetchConnectionStatus() throws FreeboxException {
        ConnectionStatus connectionStatus = getManager(ConnectionManager.class).getConfig();
        QuantityType<?> bandwidthUp = new QuantityType<>(connectionStatus.getBandwidthUp(), BIT_PER_SECOND);
        QuantityType<?> bandwidthDown = new QuantityType<>(connectionStatus.getBandwidthDown(), BIT_PER_SECOND);
        updateChannelString(CONNECTION_STATUS, LINE_STATUS, connectionStatus.getState().name());
        updateChannelString(CONNECTION_STATUS, IP_ADDRESS, connectionStatus.getIpv4());

        QuantityType<DataTransferRate> rateUp = new QuantityType<>(connectionStatus.getRateUp() * 8,
                Units.BIT_PER_SECOND);
        updateChannelQuantity(CONNECTION_STATUS, RATE_UP, rateUp, KILOBIT_PER_SECOND);
        updateChannelQuantity(CONNECTION_STATUS, PCT_BW_UP, rateUp.multiply(HUNDRED).divide(bandwidthUp),
                Units.PERCENT);

        QuantityType<DataTransferRate> rateDown = new QuantityType<>(connectionStatus.getRateDown() * 8,
                Units.BIT_PER_SECOND);
        updateChannelQuantity(CONNECTION_STATUS, RATE_DOWN, rateDown, KILOBIT_PER_SECOND);
        updateChannelQuantity(CONNECTION_STATUS, PCT_BW_DOWN, rateDown.multiply(HUNDRED).divide(bandwidthDown),
                Units.PERCENT);

        updateChannelQuantity(CONNECTION_STATUS, BYTES_UP, new QuantityType<>(connectionStatus.getBytesUp(), OCTET),
                GIBIOCTET);
        updateChannelQuantity(CONNECTION_STATUS, BYTES_DOWN, new QuantityType<>(connectionStatus.getBytesDown(), OCTET),
                GIBIOCTET);
    }

    @Override
    protected Optional<DeviceConfig> getDeviceConfig() throws FreeboxException {
        return Optional.ofNullable(getManager(SystemManager.class).getConfig());
    }

    @Override
    protected boolean internalHandleCommand(ChannelUID channelUID, Command command) throws FreeboxException {
        if (ON_OFF_CLASSES.contains(command.getClass())) {
            boolean enable = TRUE_COMMANDS.contains(command);
            switch (channelUID.getIdWithoutGroup()) {
                case WIFI_STATUS:
                    updateChannelOnOff(ACTIONS, WIFI_STATUS, getManager(WifiManager.class).setStatus(enable));
                    return true;
                case FTP_STATUS:
                    updateChannelOnOff(FILE_SHARING, FTP_STATUS, getManager(FtpManager.class).setStatus(enable));
                    return true;
                case SAMBA_FILE_STATUS:
                    updateChannelOnOff(FILE_SHARING, SAMBA_FILE_STATUS,
                            getManager(SambaManager.class).setStatus(enable));
                    return true;
                case SAMBA_PRINTER_STATUS:
                    updateChannelOnOff(FILE_SHARING, SAMBA_PRINTER_STATUS, enableSambaPrintShare(enable));
                    return true;
                case UPNPAV_STATUS:
                    updateChannelOnOff(ACTIONS, UPNPAV_STATUS, getManager(UPnPAVManager.class).setStatus(enable));
                    return true;
                case AIRMEDIA_STATUS:
                    updateChannelOnOff(ACTIONS, AIRMEDIA_STATUS, getManager(AirMediaManager.class).setStatus(enable));
                    return true;
                default:
                    break;
            }
        }
        return super.internalHandleCommand(channelUID, command);
    }

    private void fetchSambaConfig() throws FreeboxException {
        SambaConfig response = getManager(SambaManager.class).getConfig();
        updateChannelOnOff(FILE_SHARING, SAMBA_FILE_STATUS, response.isEnabled());
        updateChannelOnOff(FILE_SHARING, SAMBA_PRINTER_STATUS, response.isPrintShareEnabled());
    }

    private boolean enableSambaPrintShare(boolean enable) throws FreeboxException {
        SambaConfig config = getManager(SambaManager.class).getConfig();
        config.setPrintShareEnabled(enable);
        config = getManager(SambaManager.class).setConfig(config);
        return config.isPrintShareEnabled();
    }

    private void fetchMediaServerConfig() throws FreeboxException {
        boolean airMediaStatus = false;
        boolean uPnPAVStatus = false;

        if (getManager(LanManager.class).getNetworkMode() == NetworkMode.ROUTER) {
            airMediaStatus = getManager(AirMediaManager.class).getConfig().isEnabled();
            uPnPAVStatus = getManager(UPnPAVManager.class).getStatus();
        }

        updateChannelOnOff(ACTIONS, AIRMEDIA_STATUS, airMediaStatus);
        updateChannelOnOff(ACTIONS, UPNPAV_STATUS, uPnPAVStatus);
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singleton(ServerActions.class);
    }

    @Override
    protected void internalCallReboot() throws FreeboxException {
        getManager(SystemManager.class).reboot();
>>>>>>> 46dadb1 SAT warnings handling
    }
}
