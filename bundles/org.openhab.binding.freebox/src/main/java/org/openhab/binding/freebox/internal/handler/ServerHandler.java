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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.i18n.TimeZoneProvider;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerService;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.freebox.internal.action.ServerActions;
import org.openhab.binding.freebox.internal.api.FreeboxException;
import org.openhab.binding.freebox.internal.api.model.AirMediaConfigResponse;
import org.openhab.binding.freebox.internal.api.model.ConnectionStatus;
import org.openhab.binding.freebox.internal.api.model.ConnectionStatusResponse;
import org.openhab.binding.freebox.internal.api.model.FtpConfig;
import org.openhab.binding.freebox.internal.api.model.FtpConfigResponse;
import org.openhab.binding.freebox.internal.api.model.FtthStatusResponse;
import org.openhab.binding.freebox.internal.api.model.LanConfig.NetworkMode;
import org.openhab.binding.freebox.internal.api.model.LanConfigResponse;
import org.openhab.binding.freebox.internal.api.model.RebootResponse;
import org.openhab.binding.freebox.internal.api.model.SambaConfig;
import org.openhab.binding.freebox.internal.api.model.SambaConfigResponse;
import org.openhab.binding.freebox.internal.api.model.SystemConfig;
import org.openhab.binding.freebox.internal.api.model.SystemConfigResponse;
import org.openhab.binding.freebox.internal.api.model.UPnPAVConfigResponse;
import org.openhab.binding.freebox.internal.api.model.WifiGlobalConfig;
import org.openhab.binding.freebox.internal.api.model.WifiGlobalConfigResponse;
import org.openhab.binding.freebox.internal.api.model.XdslStatusResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ServerHandler} handle common parts of Freebox bridges.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public abstract class ServerHandler extends APIConsumerHandler {
    final Logger logger = LoggerFactory.getLogger(ServerHandler.class);
    private long uptime = -1;

    public ServerHandler(Thing thing, TimeZoneProvider timeZoneProvider) {
        super(thing, timeZoneProvider);
    }

    @Override
    protected Map<String, String> discoverAttributes() throws FreeboxException {
        final Map<String, String> properties = super.discoverAttributes();
        SystemConfig systemConfig = bridgeHandler.executeGet(SystemConfigResponse.class, null);

        properties.put(Thing.PROPERTY_SERIAL_NUMBER, systemConfig.getSerial());
        properties.put(Thing.PROPERTY_HARDWARE_VERSION, systemConfig.getBoardName());
        properties.put(Thing.PROPERTY_FIRMWARE_VERSION, systemConfig.getFirmwareVersion());
        properties.put(Thing.PROPERTY_MAC_ADDRESS, systemConfig.getMac());

        List<String> itemList = new ArrayList<>();
        List<Channel> channels = new ArrayList<Channel>(getThing().getChannels());

        systemConfig.getFans().forEach(fan -> {
            itemList.add(fan.getId());
            Channel channel = ChannelBuilder.create(new ChannelUID(thing.getUID(), PROPERTY_SENSORS, fan.getId()), null)
                    .withLabel(fan.getName()).withType(new ChannelTypeUID("freebox:fanspeed")).build();
            channels.add(channel);
        });
        properties.put(PROPERTY_FANS, itemList.stream().collect(Collectors.joining(",")));

        itemList.clear();
        systemConfig.getSensors().forEach(sensor -> {
            itemList.add(sensor.getId());
            Channel channel = ChannelBuilder
                    .create(new ChannelUID(thing.getUID(), PROPERTY_SENSORS, sensor.getId()), null)
                    .withLabel(sensor.getName()).withType(new ChannelTypeUID("freebox:temperature")).build();
            channels.add(channel);
        });
        properties.put(PROPERTY_SENSORS, itemList.stream().collect(Collectors.joining(",")));

        // add all valid channels to the thing builder
        ThingBuilder thingBuilder = editThing();
        thingBuilder.withChannels(channels);
        updateThing(thingBuilder.build());

        return properties;
    }

    @Override
    protected boolean internalHandleCommand(@NonNull ChannelUID channelUID, @NonNull Command command) {
        if (command instanceof OnOffType || command instanceof OpenClosedType || command instanceof UpDownType) {
            boolean enable = command.equals(OnOffType.ON) || command.equals(UpDownType.UP)
                    || command.equals(OpenClosedType.OPEN);
            try {
                switch (channelUID.getIdWithoutGroup()) {
                    case WIFISTATUS:
                        updateChannelSwitchState(ACTIONS, WIFISTATUS, enableWifi(enable));
                        return true;
                    case FTPSTATUS:
                        updateChannelSwitchState(ACTIONS, FTPSTATUS, enableFtp(enable));
                        return true;
                    case SAMBAFILESTATUS:
                        updateChannelSwitchState(SAMBA, SAMBAFILESTATUS, enableSambaFileShare(enable));
                        return true;
                    case SAMBAPRINTERSTATUS:
                        updateChannelSwitchState(SAMBA, SAMBAPRINTERSTATUS, enableSambaPrintShare(enable));
                        return true;
                }
            } catch (FreeboxException e) {
                logger.debug("Thing {}: invalid command {} from channel {}", getThing().getUID(), command,
                        channelUID.getId());
            }

        }
        return false;

    }

    @Override
    protected void internalPoll() {
        try {

            logger.debug("Polling server state...");
            fetchSystemConfig();
            boolean commOk = true;
            commOk &= fetchWifiConfig();
            commOk &= (fetchxDslStatus() || fetchFtthPresent());
            commOk &= fetchConnectionStatus();
            commOk &= fetchFtpConfig();
            commOk &= fetchAirMediaConfig();
            commOk &= fetchUPnPAVConfig();
            commOk &= fetchSambaConfig();

            if (commOk) {
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            }

        } catch (FreeboxException e) {
            logger.debug("Thing {}: exception in fetchxDslStatus: {}", getThing().getUID(), e.getMessage(), e);
        }

    }

    private boolean fetchConnectionStatus() {
        try {
            ConnectionStatus connectionStatus = bridgeHandler.executeGet(ConnectionStatusResponse.class,
                    null);
            updateChannelStringState(CONNECTION_STATUS, LINESTATUS, connectionStatus.getState().name());
            updateChannelStringState(SYS_INFO, IPV4, connectionStatus.getIpv4());
            updateChannelQuantityType(CONNECTION_STATUS, RATEUP,
                    new QuantityType<>(connectionStatus.getRateUp() * 8, BIT_PER_SECOND).toUnit(KILOBIT_PER_SECOND));
            updateChannelQuantityType(CONNECTION_STATUS, RATEDOWN,
                    new QuantityType<>(connectionStatus.getRateDown() * 8, BIT_PER_SECOND).toUnit(KILOBIT_PER_SECOND));
            updateChannelQuantityType(CONNECTION_STATUS, BYTESUP,
                    new QuantityType<>(connectionStatus.getBytesUp(), OCTET).toUnit(GIBIOCTET));
            updateChannelQuantityType(CONNECTION_STATUS, BYTESDOWN,
                    new QuantityType<>(connectionStatus.getBytesDown(), OCTET).toUnit(GIBIOCTET));
            updateChannelQuantityType(CONNECTION_STATUS, BWUP,
                    new QuantityType<>(connectionStatus.getBandwidthUp(), BIT_PER_SECOND).toUnit(MEGABIT_PER_SECOND));
            updateChannelQuantityType(CONNECTION_STATUS, BWDOWN,
                    new QuantityType<>(connectionStatus.getBandwidthDown(), BIT_PER_SECOND).toUnit(MEGABIT_PER_SECOND));
            return true;
        } catch (FreeboxException e) {
            logger.debug("Thing {}: exception in fetchConnectionStatus: {}", getThing().getUID(), e.getMessage(), e);
            internalPoll();
            return false;
        }
    }

    private boolean fetchxDslStatus() {
        try {
            String status = bridgeHandler.executeGet(XdslStatusResponse.class, null).getStatus();
            if (StringUtils.isNotEmpty(status)) {
                updateChannelStringState(SYS_INFO, XDSLSTATUS, status);
            }
            return true;
        } catch (FreeboxException e) {
            logger.debug("Thing {}: exception in fetchxDslStatus: {}", getThing().getUID(), e.getMessage(), e);
            return false;
        }
    }

    private boolean fetchFtthPresent() {
        try {
            boolean status = bridgeHandler.executeGet(FtthStatusResponse.class, null).getSfpPresent();
            updateChannelSwitchState(SYS_INFO, FTTHSTATUS, status);
            return status;
        } catch (FreeboxException e) {
            logger.debug("Thing {}: exception in fetchxFtthStatus: {}", getThing().getUID(), e.getMessage(), e);
            return false;
        }
    }

    boolean fetchWifiConfig() {
        try {
            Boolean enabled = bridgeHandler.executeGet(WifiGlobalConfigResponse.class, null).isEnabled();
            updateChannelSwitchState(ACTIONS, WIFISTATUS, enabled);
            return true;
        } catch (FreeboxException e) {
            logger.debug("Thing {}: exception in fetchWifiConfig: {}", getThing().getUID(), e.getMessage(), e);
            return false;
        }
    }

    boolean fetchFtpConfig() {
        try {
            Boolean enabled = bridgeHandler.executeGet(FtpConfigResponse.class, null).isEnabled();
            updateChannelSwitchState(ACTIONS, FTPSTATUS, enabled); // TODO Auto-generated method stub
            return true;
        } catch (FreeboxException e) {
            logger.debug("Thing {}: exception in fetchFtpConfig: {}", getThing().getUID(), e.getMessage(), e);
            return false;
        }
    }

    boolean fetchAirMediaConfig() {
        try {
            if (!isInLanBridgeMode()) {
                Boolean enabled = bridgeHandler.executeGet(AirMediaConfigResponse.class, null).isEnabled();
                updateChannelSwitchState(PLAYER_ACTIONS, AIRMEDIASTATUS, enabled);
            }
            return true;
        } catch (FreeboxException e) {
            logger.debug("Thing {}: exception in fetchAirMediaConfig: {}", getThing().getUID(), e.getMessage(), e);
            return false;
        }
    }

    boolean fetchUPnPAVConfig() {
        try {
            if (!isInLanBridgeMode()) {
                Boolean enabled = bridgeHandler.executeGet(UPnPAVConfigResponse.class, null).isEnabled();
                updateChannelSwitchState(PLAYER_ACTIONS, UPNPAVSTATUS, enabled);
            }
            return true;
        } catch (FreeboxException e) {
            logger.debug("Thing {}: exception in fetchUPnPAVConfig: {}", getThing().getUID(), e.getMessage(), e);
            return false;
        }
    }

    boolean fetchSambaConfig() {
        try {
            SambaConfig config = bridgeHandler.executeGet(SambaConfigResponse.class, null);
            updateChannelSwitchState(SAMBA, SAMBAFILESTATUS, config.isFileShareEnabled());
            updateChannelSwitchState(SAMBA, SAMBAPRINTERSTATUS, config.isPrintShareEnabled()); // TODO Auto-generated
                                                                                               // method
            // stub);
            return true;
        } catch (FreeboxException e) {
            logger.debug("Thing {}: exception in fetchSambaConfig: {}", getThing().getUID(), e.getMessage(), e);
            return false;
        }
    }

    private void fetchSystemConfig() throws FreeboxException {
        SystemConfig systemConfig = bridgeHandler.executeGet(SystemConfigResponse.class, null);

        Map<String, String> properties = editProperties();
        if (!properties.get(Thing.PROPERTY_FIRMWARE_VERSION).equals(systemConfig.getFirmwareVersion())) {
            properties.put(Thing.PROPERTY_FIRMWARE_VERSION, systemConfig.getFirmwareVersion());
            triggerChannel(new ChannelUID(getThing().getUID(), SYS_INFO, BOX_EVENT), "firmware_updated");
            updateProperties(properties);
        }

        long newUptime = systemConfig.getUptimeVal();
        if (newUptime < uptime) {
            triggerChannel(new ChannelUID(getThing().getUID(), SYS_INFO, BOX_EVENT), "restarted");
        }
        uptime = newUptime;

        updateChannelQuantityType(SYS_INFO, UPTIME, new QuantityType<>(uptime, SmartHomeUnits.SECOND));

        systemConfig.getFans().forEach(fan -> {
            int value = fan.getValue();
            updateState(new ChannelUID(getThing().getUID(), PROPERTY_SENSORS, fan.getId()),
                    new QuantityType<>(value, SIUnits.CELSIUS));
        });

        systemConfig.getSensors().forEach(sensor -> {
            int value = sensor.getValue();
            updateState(new ChannelUID(getThing().getUID(), PROPERTY_SENSORS, sensor.getId()), new DecimalType(value));
        });
    }

    public boolean isInLanBridgeMode() throws FreeboxException {
        return bridgeHandler.executeGet(LanConfigResponse.class, null).getType() == NetworkMode.BRIDGE;
    }

    public boolean isWifiEnabled() throws FreeboxException {
        return bridgeHandler.executeGet(WifiGlobalConfigResponse.class, null).isEnabled();
    }

    public boolean enableWifi(boolean enable) throws FreeboxException {
        WifiGlobalConfig config = new WifiGlobalConfig();
        config.setEnabled(enable);
        config = bridgeHandler.execute(config, null);
        return config.isEnabled();
    }

    public boolean enableFtp(boolean enable) throws FreeboxException {
        FtpConfig config = new FtpConfig();
        config.setEnabled(enable);
        config = bridgeHandler.execute(config, null);
        return config.isEnabled();
    }

    public boolean enableSambaFileShare(boolean enable) throws FreeboxException {
        SambaConfig config = new SambaConfig();
        config.setFileShareEnabled(enable);
        config = bridgeHandler.execute(config, null);
        return config.isFileShareEnabled();
    }

    public boolean enableSambaPrintShare(boolean enable) throws FreeboxException {
        SambaConfig config = new SambaConfig();
        config.setPrintShareEnabled(enable);
        config = bridgeHandler.execute(config, null);
        return config.isPrintShareEnabled();
    }

    public void reboot() {
        try {
            bridgeHandler.executePost(RebootResponse.class, null, null);
        } catch (FreeboxException e) {
            logger.debug("Thing {}: error rebooting server", getThing().getUID());
        }
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singletonList(ServerActions.class);
    }

}
