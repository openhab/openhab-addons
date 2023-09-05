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
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.openhab.core.library.unit.SIUnits;
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

        config.sensors().forEach(s -> updateChannelQuantity(GROUP_SENSORS, s.id(), s.value(), SIUnits.CELSIUS));
        config.fans().forEach(f -> updateChannelQuantity(GROUP_FANS, f.id(), f.value(), Units.RPM));

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

        updateRateBandwidth(status.rateUp(), status.bandwidthUp(), "up");
        updateRateBandwidth(status.rateDown(), status.bandwidthDown(), "down");

        updateChannelQuantity(CONNECTION_STATUS, BYTES_UP, new QuantityType<>(status.bytesUp(), OCTET), GIBIOCTET);
        updateChannelQuantity(CONNECTION_STATUS, BYTES_DOWN, new QuantityType<>(status.bytesDown(), OCTET), GIBIOCTET);
    }

    private void updateRateBandwidth(long rate, long bandwidth, String orientation) {
        QuantityType<?> rateUp = new QuantityType<>(rate * 8, Units.BIT_PER_SECOND);
        QuantityType<?> bandwidthUp = new QuantityType<>(bandwidth, BIT_PER_SECOND);
        updateChannelQuantity(CONNECTION_STATUS, RATE + "-" + orientation, rateUp, KILOBIT_PER_SECOND);
        updateChannelQuantity(CONNECTION_STATUS, BW + "-" + orientation, bandwidthUp, KILOBIT_PER_SECOND);
        updateChannelQuantity(CONNECTION_STATUS, PCT_BW + "-" + orientation,
                !bandwidthUp.equals(QuantityType.ZERO) ? rateUp.multiply(HUNDRED).divide(bandwidthUp)
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
                logger.warn("Error rebooting: {}", e.getMessage());
            }
        });
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(ServerActions.class);
    }

    @Override
    public ChannelUID getEventChannelUID() {
        return eventChannelUID;
    }

    @Override
    public void triggerChannel(ChannelUID channelUID, String event) {
        super.triggerChannel(channelUID, event);
    }
}
