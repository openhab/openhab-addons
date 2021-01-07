/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
import java.time.ZoneId;
import java.util.Collection;
import java.util.Collections;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freeboxos.internal.action.ServerActions;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.connection.ConnectionStatus;
import org.openhab.binding.freeboxos.internal.api.lan.ConnectivityData;
import org.openhab.binding.freeboxos.internal.api.lan.LanConfig.NetworkMode;
import org.openhab.binding.freeboxos.internal.api.netshare.SambaConfig;
import org.openhab.binding.freeboxos.internal.api.system.DeviceConfig;
import org.openhab.core.library.dimension.DataTransferRate;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ServerHandler} handle common parts of Freebox bridges.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class ServerHandler extends FreeDeviceHandler {
    private final Logger logger = LoggerFactory.getLogger(ServerHandler.class);
    private final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    public ServerHandler(Thing thing, ZoneId zoneId) {
        super(thing, zoneId);
    }

    @Override
    protected void internalPoll() throws FreeboxException {
        super.internalPoll();
        logger.debug("Polling server state...");
        fetchConnectionStatus();
        fetchUPnPAVConfig();
        fetchSambaConfig();
        updateChannelOnOff(ACTIONS, FTP_STATUS, bridgeHandler.getFtpManager().getFtpStatus());
        updateChannelOnOff(ACTIONS, WIFI_STATUS, bridgeHandler.getWifiManager().getStatus());
    }

    @Override
    protected ConnectivityData fetchConnectivity() throws FreeboxException {
        return bridgeHandler.getLanManager().getLanConfig();
    }

    private void fetchConnectionStatus() throws FreeboxException {
        ConnectionStatus connectionStatus = bridgeHandler.getConnectionManager().getStatus();
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
    protected DeviceConfig getDeviceConfig() throws FreeboxException {
        return bridgeHandler.getSystemManager().getConfig();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (ON_OFF_CLASSES.contains(command.getClass())) {
            boolean enable = TRUE_COMMANDS.contains(command);
            try {
                switch (channelUID.getIdWithoutGroup()) {
                    case WIFI_STATUS:
                        updateChannelOnOff(ACTIONS, WIFI_STATUS, bridgeHandler.getWifiManager().setStatus(enable));
                        break;
                    case FTP_STATUS:
                        updateChannelOnOff(ACTIONS, FTP_STATUS, bridgeHandler.getFtpManager().changeFtpStatus(enable));
                        break;
                    case SAMBA_FILE_STATUS:
                        updateChannelOnOff(SAMBA, SAMBA_FILE_STATUS, enableSambaFileShare(enable));
                        break;
                    case SAMBA_PRINTER_STATUS:
                        updateChannelOnOff(SAMBA, SAMBA_PRINTER_STATUS, enableSambaPrintShare(enable));
                        break;
                    case UPNPAV_STATUS:
                        updateChannelOnOff(ACTIONS, UPNPAV_STATUS,
                                bridgeHandler.getuPnPAVManager().changeStatus(enable));
                        break;
                }
            } catch (FreeboxException e) {
                logger.debug("Invalid command {} on channel {} : {}", command, channelUID.getId(), e.getMessage());
            }
        }
    }

    private void fetchSambaConfig() throws FreeboxException {
        SambaConfig response = bridgeHandler.getNetShareManager().getSambaConfig();
        updateChannelOnOff(SAMBA, SAMBA_FILE_STATUS, response.isFileShareEnabled());
        updateChannelOnOff(SAMBA, SAMBA_PRINTER_STATUS, response.isPrintShareEnabled());
    }

    private boolean enableSambaFileShare(boolean enable) throws FreeboxException {
        SambaConfig config = bridgeHandler.getNetShareManager().getSambaConfig();
        config.setFileShareEnabled(enable);
        config = bridgeHandler.getNetShareManager().setSambaConfig(config);
        return config.isFileShareEnabled();
    }

    private boolean enableSambaPrintShare(boolean enable) throws FreeboxException {
        SambaConfig config = bridgeHandler.getNetShareManager().getSambaConfig();
        config.setPrintShareEnabled(enable);
        config = bridgeHandler.getNetShareManager().setSambaConfig(config);
        return config.isPrintShareEnabled();
    }

    private void fetchUPnPAVConfig() throws FreeboxException {
        if (bridgeHandler.getLanManager().getNetworkMode() == NetworkMode.ROUTER) {
            updateChannelOnOff(PLAYER_ACTIONS, UPNPAV_STATUS, bridgeHandler.getuPnPAVManager().getStatus());
        }
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singleton(ServerActions.class);
    }

    @Override
    protected void internalCallReboot() throws FreeboxException {
        bridgeHandler.getSystemManager().reboot();
    }
}
