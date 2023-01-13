/**
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
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.freeboxos.internal.action.ServerActions;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.airmedia.AirMediaManager;
import org.openhab.binding.freeboxos.internal.api.connection.ConnectionManager;
import org.openhab.binding.freeboxos.internal.api.connection.ConnectionStatus;
import org.openhab.binding.freeboxos.internal.api.ftp.FtpManager;
import org.openhab.binding.freeboxos.internal.api.lan.LanConfig;
import org.openhab.binding.freeboxos.internal.api.lan.LanManager;
import org.openhab.binding.freeboxos.internal.api.netshare.afp.AfpManager;
import org.openhab.binding.freeboxos.internal.api.netshare.samba.SambaConfig;
import org.openhab.binding.freeboxos.internal.api.netshare.samba.SambaManager;
import org.openhab.binding.freeboxos.internal.api.system.SystemConfig;
import org.openhab.binding.freeboxos.internal.api.system.SystemManager;
import org.openhab.binding.freeboxos.internal.api.upnpav.UPnPAVManager;
import org.openhab.binding.freeboxos.internal.api.wifi.WifiManager;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
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
public class ServerHandler extends ApiConsumerHandler implements FreeDeviceIntf /* was FreeDeviceHandler */ {
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private final Logger logger = LoggerFactory.getLogger(ServerHandler.class);
    private long uptime = -1;
    private final ChannelUID eventChannelUID;

    public ServerHandler(
            Thing thing /* , AudioHTTPServer audioHTTPServer, String ipAddress, BundleContext bundleContext */) {
        super(thing /* , audioHTTPServer, ipAddress, bundleContext */);
        eventChannelUID = new ChannelUID(getThing().getUID(), SYS_INFO, BOX_EVENT);
    }

    @Override
    void initializeProperties(Map<String, String> properties) throws FreeboxException {
        LanConfig lanConfig = getManager(LanManager.class).getConfig();
        SystemConfig config = getManager(SystemManager.class).getConfig();
        properties.put(Thing.PROPERTY_SERIAL_NUMBER, config.getSerial());
        properties.put(Thing.PROPERTY_FIRMWARE_VERSION, config.getFirmwareVersion());
        properties.put(Thing.PROPERTY_HARDWARE_VERSION, config.getModelInfo().getPrettyName());
        properties.put(HOSTNAME, lanConfig.getName());

        List<Channel> channels = new ArrayList<>(getThing().getChannels());
        config.getSensors().forEach(sensor -> {
            ChannelUID sensorId = new ChannelUID(thing.getUID(), GROUP_SENSORS, sensor.getId());
            channels.add(ChannelBuilder.create(sensorId).withLabel(sensor.getName())
                    .withAcceptedItemType("Number:Temperature")
                    .withType(new ChannelTypeUID(BINDING_ID + ":temperature")).build());
        });
        config.getFans().forEach(sensor -> {
            ChannelUID sensorId = new ChannelUID(thing.getUID(), GROUP_FANS, sensor.getId());
            channels.add(ChannelBuilder.create(sensorId).withLabel(sensor.getName())
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

        SambaConfig response = getManager(SambaManager.class).getConfig();
        updateChannelOnOff(FILE_SHARING, SAMBA_FILE_STATUS, response.isFileShareEnabled());
        updateChannelOnOff(FILE_SHARING, SAMBA_PRINTER_STATUS, response.isPrintShareEnabled());
        updateChannelOnOff(FILE_SHARING, FTP_STATUS, getManager(FtpManager.class).getStatus());
        updateChannelOnOff(FILE_SHARING, AFP_FILE_STATUS, getManager(AfpManager.class).getStatus());
    }

    private void fetchSystemConfig() throws FreeboxException {
        SystemConfig config = getManager(SystemManager.class).getConfig();

        config.getSensors().forEach(sensor -> {
            updateChannelDecimal(GROUP_SENSORS, sensor.getId(), sensor.getValue());
        });
        config.getFans().forEach(fan -> {
            updateChannelDecimal(GROUP_FANS, fan.getId(), fan.getValue());
        });

        long newUptime = config.getUptimeVal();
        uptime = controlUptimeAndFirmware(newUptime, uptime, config.getFirmwareVersion());
        updateChannelQuantity(SYS_INFO, UPTIME, uptime, Units.SECOND);

        LanConfig lanConfig = getManager(LanManager.class).getConfig();
        updateChannelString(SYS_INFO, IP_ADDRESS, lanConfig.getIp());
    }

    private void fetchConnectionStatus() throws FreeboxException {
        ConnectionStatus status = getManager(ConnectionManager.class).getConfig();
        updateChannelString(CONNECTION_STATUS, LINE_STATUS, status.getState());
        updateChannelString(CONNECTION_STATUS, LINE_TYPE, status.getType());
        updateChannelString(CONNECTION_STATUS, LINE_MEDIA, status.getMedia());
        updateChannelString(CONNECTION_STATUS, IP_ADDRESS, status.getIpv4());
        updateChannelString(CONNECTION_STATUS, IPV6_ADDRESS, status.getIpv6());

        QuantityType<?> rateUp = new QuantityType<>(status.getRateUp() * 8, Units.BIT_PER_SECOND);
        updateChannelQuantity(CONNECTION_STATUS, RATE_UP, rateUp, KILOBIT_PER_SECOND);

        QuantityType<?> rateDown = new QuantityType<>(status.getRateDown() * 8, Units.BIT_PER_SECOND);
        updateChannelQuantity(CONNECTION_STATUS, RATE_DOWN, rateDown, KILOBIT_PER_SECOND);

        QuantityType<?> bandwidthUp = new QuantityType<>(status.getBandwidthUp(), BIT_PER_SECOND);
        updateChannelQuantity(CONNECTION_STATUS, BW_UP, bandwidthUp, KILOBIT_PER_SECOND);

        QuantityType<?> bandwidthDown = new QuantityType<>(status.getBandwidthDown(), BIT_PER_SECOND);
        updateChannelQuantity(CONNECTION_STATUS, BW_DOWN, bandwidthDown, KILOBIT_PER_SECOND);

        updateChannelQuantity(CONNECTION_STATUS, BYTES_UP, new QuantityType<>(status.getBytesUp(), OCTET), GIBIOCTET);
        updateChannelQuantity(CONNECTION_STATUS, BYTES_DOWN, new QuantityType<>(status.getBytesDown(), OCTET),
                GIBIOCTET);

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
                    updateChannelOnOff(FILE_SHARING, SAMBA_FILE_STATUS, enableSambaFileShare(enable));
                    return true;
                case SAMBA_PRINTER_STATUS:
                    updateChannelOnOff(FILE_SHARING, SAMBA_PRINTER_STATUS, enableSambaPrintShare(enable));
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

    private boolean enableSambaFileShare(boolean enable) throws FreeboxException {
        SambaConfig config = getManager(SambaManager.class).getConfig();
        config.setFileShareEnabled(enable);
        return getManager(SambaManager.class).setConfig(config).isFileShareEnabled();
    }

    private boolean enableSambaPrintShare(boolean enable) throws FreeboxException {
        SambaConfig config = getManager(SambaManager.class).getConfig();
        config.setPrintShareEnabled(enable);
        return getManager(SambaManager.class).setConfig(config).isPrintShareEnabled();
    }

    public void reboot() {
        try {
            getManager(SystemManager.class).reboot();
            triggerChannel(new ChannelUID(getThing().getUID(), SYS_INFO, BOX_EVENT), "reboot_requested");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.DUTY_CYCLE, "System rebooting...");
            stopRefreshJob();
            scheduler.schedule(this::initialize, 30, TimeUnit.SECONDS);
        } catch (FreeboxException e) {
            logger.warn("Error rebooting device : {}", e.getMessage());
        }
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
    }

    @Override
    public Map<String, String> editProperties() {
        return super.editProperties();
    }

    @Override
    public void updateProperties(@Nullable Map<String, String> properties) {
        super.updateProperties(properties);
    }

}
