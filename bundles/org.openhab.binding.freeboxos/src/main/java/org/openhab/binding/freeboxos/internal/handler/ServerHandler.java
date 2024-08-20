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
import org.openhab.binding.freeboxos.internal.api.rest.ConnectionManager.FtthStatus;
import org.openhab.binding.freeboxos.internal.api.rest.ConnectionManager.Media;
import org.openhab.binding.freeboxos.internal.api.rest.ConnectionManager.Status;
import org.openhab.binding.freeboxos.internal.api.rest.ConnectionManager.SynchroState;
import org.openhab.binding.freeboxos.internal.api.rest.ConnectionManager.XdslInfos;
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

    private boolean tryConfigureMediaSink = true;

    public ServerHandler(Thing thing) {
        super(thing);
        eventChannelUID = new ChannelUID(getThing().getUID(), GROUP_SYS_INFO, BOX_EVENT);
    }

    @Override
    void initializeProperties(Map<String, String> properties) throws FreeboxException {
        LanConfig lanConfig = getManager(LanManager.class).getConfig();
        Config config = getManager(SystemManager.class).getConfig();

        properties.put(Thing.PROPERTY_SERIAL_NUMBER, config.serial());
        properties.put(Thing.PROPERTY_FIRMWARE_VERSION, config.firmwareVersion());
        properties.put(Thing.PROPERTY_HARDWARE_VERSION, config.modelInfo().prettyName());
        properties.put(Thing.PROPERTY_MAC_ADDRESS, config.mac().toColonDelimitedString());
        properties.put(Source.UPNP.name(), lanConfig.name());

        List<Channel> channels = new ArrayList<>(getThing().getChannels());

        // Remove channels of the not active media type
        Status connectionConfig = getManager(ConnectionManager.class).getConfig();
        channels.removeIf(c -> (GROUP_FTTH.equals(c.getUID().getGroupId()) && connectionConfig.media() != Media.FTTH)
                || (GROUP_XDSL.equals(c.getUID().getGroupId()) && connectionConfig.media() != Media.XDSL));

        // Add temperature sensors
        config.sensors().forEach(sensor -> {
            ChannelUID sensorId = new ChannelUID(thing.getUID(), GROUP_SENSORS, sensor.id());
            if (getThing().getChannel(sensorId) == null) {
                String label = sensor.name();
                // For revolution, API returns only "Disque dur" so we patch it to have naming consistency with other
                // temperature sensors
                if ("Disque dur".equals(label)) {
                    label = "Température " + label;
                }
                channels.add(ChannelBuilder.create(sensorId).withLabel(label).withAcceptedItemType("Number:Temperature")
                        .withType(new ChannelTypeUID(BINDING_ID, "temperature")).build());
            }
        });

        // Add fans sensors
        config.fans().forEach(sensor -> {
            ChannelUID sensorId = new ChannelUID(thing.getUID(), GROUP_FANS, sensor.id());
            if (getThing().getChannel(sensorId) == null) {
                channels.add(ChannelBuilder.create(sensorId).withLabel(sensor.name())
                        .withAcceptedItemType("Number:Frequency").withType(new ChannelTypeUID(BINDING_ID, "fanspeed"))
                        .build());
            }
        });

        // And finally update the thing with appropriate channels
        updateThing(editThing().withChannels(channels).build());
    }

    @Override
    protected void internalPoll() throws FreeboxException {
        logger.debug("Polling server state...");
        fetchConnectionStatus();
        fetchSystemConfig();

        if (anyChannelLinked(GROUP_ACTIONS, Set.of(WIFI_STATUS))) {
            updateChannelOnOff(GROUP_ACTIONS, WIFI_STATUS, getManager(WifiManager.class).getStatus());
        }
        if (anyChannelLinked(GROUP_ACTIONS, Set.of(AIRMEDIA_STATUS))) {
            updateChannelOnOff(GROUP_ACTIONS, AIRMEDIA_STATUS, getManager(AirMediaManager.class).getStatus());
        }
        if (anyChannelLinked(GROUP_ACTIONS, Set.of(UPNPAV_STATUS))) {
            updateChannelOnOff(GROUP_ACTIONS, UPNPAV_STATUS, getManager(UPnPAVManager.class).getStatus());
        }

        if (anyChannelLinked(GROUP_FILE_SHARING, Set.of(SAMBA_FILE_STATUS, SAMBA_PRINTER_STATUS))) {
            Samba response = getManager(SambaManager.class).getConfig();
            updateChannelOnOff(GROUP_FILE_SHARING, SAMBA_FILE_STATUS, response.fileShareEnabled());
            updateChannelOnOff(GROUP_FILE_SHARING, SAMBA_PRINTER_STATUS, response.printShareEnabled());
        }
        if (anyChannelLinked(GROUP_FILE_SHARING, Set.of(FTP_STATUS))) {
            updateChannelOnOff(GROUP_FILE_SHARING, FTP_STATUS, getManager(FtpManager.class).getStatus());
        }
        if (anyChannelLinked(GROUP_FILE_SHARING, Set.of(AFP_FILE_STATUS))) {
            updateChannelOnOff(GROUP_FILE_SHARING, AFP_FILE_STATUS, getManager(AfpManager.class).getStatus());
        }

        if (tryConfigureMediaSink) {
            configureMediaSink();
            tryConfigureMediaSink = false;
        }
    }

    @Override
    protected void internalForcePoll() throws FreeboxException {
        tryConfigureMediaSink = true;
        internalPoll();
    }

    private void fetchSystemConfig() throws FreeboxException {
        Config config = getManager(SystemManager.class).getConfig();

        config.sensors().forEach(s -> updateChannelQuantity(GROUP_SENSORS, s.id(), s.value(), SIUnits.CELSIUS));
        config.fans().forEach(f -> updateChannelQuantity(GROUP_FANS, f.id(), f.value(), Units.RPM));

        uptime = checkUptimeAndFirmware(config.uptimeVal(), uptime, config.firmwareVersion());
        updateChannelQuantity(GROUP_SYS_INFO, UPTIME, uptime, Units.SECOND);

        if (anyChannelLinked(GROUP_SYS_INFO, Set.of(IP_ADDRESS))) {
            LanConfig lanConfig = getManager(LanManager.class).getConfig();
            updateChannelString(GROUP_SYS_INFO, IP_ADDRESS, lanConfig.ip());
        }
    }

    private void fetchConnectionStatus() throws FreeboxException {
        if (anyChannelLinked(GROUP_CONNECTION_STATUS,
                Set.of(LINE_STATUS, LINE_TYPE, LINE_MEDIA, IP_ADDRESS, IPV6_ADDRESS, BYTES_UP, BYTES_DOWN, RATE + "-up",
                        BW + "-up", PCT_BW + "-up", RATE + "-down", BW + "-down", PCT_BW + "-down"))) {
            Status status = getManager(ConnectionManager.class).getConfig();
            updateChannelString(GROUP_CONNECTION_STATUS, LINE_STATUS, status.state());
            updateChannelString(GROUP_CONNECTION_STATUS, LINE_TYPE, status.type());
            updateChannelString(GROUP_CONNECTION_STATUS, LINE_MEDIA, status.media());
            updateChannelString(GROUP_CONNECTION_STATUS, IP_ADDRESS, status.ipv4());
            updateChannelString(GROUP_CONNECTION_STATUS, IPV6_ADDRESS, status.ipv6());
            updateRateBandwidth(status.rateUp(), status.bandwidthUp(), "up");
            updateRateBandwidth(status.rateDown(), status.bandwidthDown(), "down");

            updateChannelQuantity(GROUP_CONNECTION_STATUS, BYTES_UP, status.bytesUp(), OCTET);
            updateChannelQuantity(GROUP_CONNECTION_STATUS, BYTES_DOWN, status.bytesDown(), OCTET);
        }
        if (anyChannelLinked(GROUP_FTTH,
                Set.of(SFP_PRESENT, SFP_ALIM, SFP_POWER, SFP_SIGNAL, SFP_LINK, SFP_PWR_TX, SFP_PWR_RX))) {
            FtthStatus ftthStatus = getManager(ConnectionManager.class).getFtthStatus();
            updateChannelOnOff(GROUP_FTTH, SFP_PRESENT, ftthStatus.sfpPresent());
            updateChannelOnOff(GROUP_FTTH, SFP_ALIM, ftthStatus.sfpAlimOk());
            updateChannelOnOff(GROUP_FTTH, SFP_POWER, ftthStatus.sfpHasPowerReport());
            updateChannelOnOff(GROUP_FTTH, SFP_SIGNAL, ftthStatus.sfpHasSignal());
            updateChannelOnOff(GROUP_FTTH, SFP_LINK, ftthStatus.link());
            updateChannelQuantity(GROUP_FTTH, SFP_PWR_TX, ftthStatus.getTransmitDBM(), Units.DECIBEL_MILLIWATTS);
            updateChannelQuantity(GROUP_FTTH, SFP_PWR_RX, ftthStatus.getReceivedDBM(), Units.DECIBEL_MILLIWATTS);
        }
        if (anyChannelLinked(GROUP_XDSL, Set.of(XDSL_READY, XDSL_STATUS, XDSL_MODULATION, XDSL_UPTIME))) {
            XdslInfos xdslInfos = getManager(ConnectionManager.class).getXdslStatus();
            updateChannelOnOff(GROUP_XDSL, XDSL_READY, xdslInfos.status().status() == SynchroState.SHOWTIME);
            updateChannelString(GROUP_XDSL, XDSL_STATUS, xdslInfos.status().status());
            updateChannelString(GROUP_XDSL, XDSL_MODULATION, xdslInfos.status().modulation());
            updateChannelQuantity(GROUP_XDSL, XDSL_UPTIME, xdslInfos.status().uptime(), Units.SECOND);
        }
    }

    private void updateRateBandwidth(long rate, long bandwidth, String orientation) {
        QuantityType<?> rateUp = new QuantityType<>(rate * 8, BIT_PER_SECOND);
        QuantityType<?> bandwidthUp = new QuantityType<>(bandwidth, BIT_PER_SECOND);
        updateChannelQuantity(GROUP_CONNECTION_STATUS, RATE + "-" + orientation, rateUp);
        updateChannelQuantity(GROUP_CONNECTION_STATUS, BW + "-" + orientation, bandwidthUp);
        updateChannelQuantity(GROUP_CONNECTION_STATUS, PCT_BW + "-" + orientation,
                !bandwidthUp.equals(QuantityType.ZERO) ? rateUp.divide(bandwidthUp) : QuantityType.ZERO, PERCENT);
    }

    @Override
    protected boolean internalHandleCommand(String channelId, Command command) throws FreeboxException {
        if (ON_OFF_CLASSES.contains(command.getClass())) {
            boolean enable = TRUE_COMMANDS.contains(command);
            switch (channelId) {
                case WIFI_STATUS:
                    updateChannelOnOff(GROUP_ACTIONS, WIFI_STATUS, getManager(WifiManager.class).setStatus(enable));
                    return true;
                case FTP_STATUS:
                    updateChannelOnOff(GROUP_FILE_SHARING, FTP_STATUS, getManager(FtpManager.class).setStatus(enable));
                    return true;
                case SAMBA_FILE_STATUS:
                    updateChannelOnOff(GROUP_FILE_SHARING, SAMBA_FILE_STATUS,
                            getManager(SambaManager.class).setFileShare(enable));
                    return true;
                case SAMBA_PRINTER_STATUS:
                    updateChannelOnOff(GROUP_FILE_SHARING, SAMBA_PRINTER_STATUS,
                            getManager(SambaManager.class).setPrintShare(enable));
                    return true;
                case UPNPAV_STATUS:
                    updateChannelOnOff(GROUP_ACTIONS, UPNPAV_STATUS, getManager(UPnPAVManager.class).setStatus(enable));
                    return true;
                case AFP_FILE_STATUS:
                    updateChannelOnOff(GROUP_FILE_SHARING, AFP_FILE_STATUS,
                            getManager(AfpManager.class).setStatus(enable));
                    return true;
                case AIRMEDIA_STATUS:
                    updateChannelOnOff(GROUP_ACTIONS, AIRMEDIA_STATUS,
                            getManager(AirMediaManager.class).setStatus(enable));
                    tryConfigureMediaSink = true;
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
