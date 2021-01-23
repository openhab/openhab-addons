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
package org.openhab.binding.mikrotik.internal.handler;

import static org.eclipse.smarthome.core.thing.ThingStatus.OFFLINE;
import static org.eclipse.smarthome.core.thing.ThingStatus.ONLINE;
import static org.eclipse.smarthome.core.thing.ThingStatusDetail.CONFIGURATION_ERROR;
import static org.eclipse.smarthome.core.thing.ThingStatusDetail.GONE;
import static org.openhab.binding.mikrotik.internal.MikrotikBindingConstants.*;

import java.math.BigDecimal;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.mikrotik.internal.MikrotikBindingConstants;
import org.openhab.binding.mikrotik.internal.config.InterfaceThingConfig;
import org.openhab.binding.mikrotik.internal.model.*;
import org.openhab.binding.mikrotik.internal.util.RateCalculator;
import org.openhab.binding.mikrotik.internal.util.StateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MikrotikInterfaceThingHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Oleg Vivtash - Initial contribution
 */
@NonNullByDefault
public class MikrotikInterfaceThingHandler extends MikrotikBaseThingHandler<InterfaceThingConfig> {
    private final Logger logger = LoggerFactory.getLogger(MikrotikInterfaceThingHandler.class);

    private @Nullable InterfaceThingConfig config;
    private @Nullable RouterosInterfaceBase iface;

    private final RateCalculator txByteRate = new RateCalculator(BigDecimal.ZERO);
    private final RateCalculator rxByteRate = new RateCalculator(BigDecimal.ZERO);
    private final RateCalculator txPacketRate = new RateCalculator(BigDecimal.ZERO);
    private final RateCalculator rxPacketRate = new RateCalculator(BigDecimal.ZERO);

    public static boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return MikrotikBindingConstants.THING_TYPE_INTERFACE.equals(thingTypeUID);
    }

    public MikrotikInterfaceThingHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected void initialize(@NonNull InterfaceThingConfig config) {
        if (this.config == null) {
            logger.debug("Assigning {} config: {}", getClass().getSimpleName(), config);
            if (!config.isValid()) {
                updateStatus(OFFLINE, CONFIGURATION_ERROR, "InterfaceThingConfig is invalid");
                return;
            }
            this.config = config;
            updateStatus(ONLINE);
        }
    }

    @Override
    protected void refreshModels() {
        if (getRouteros() != null && config != null) {
            logger.trace("Searching for {} interface", config.name);
            iface = getRouteros().findInterface(config.name);
            if (iface == null) {
                logger.warn("Interface {} is not found in RouterOS for thing {}", config.name, getThing().getUID());
                updateStatus(OFFLINE, GONE, "Interface not found in RouterOS");
            } else {
                txByteRate.update(iface.getTxBytes());
                rxByteRate.update(iface.getRxBytes());
                txPacketRate.update(iface.getTxPackets());
                rxPacketRate.update(iface.getRxPackets());
            }
        } else {
            logger.trace("getRouteros() || config is null in refreshModels()");
        }
    }

    @Override
    protected void refreshChannel(ChannelUID channelUID) {
        String channelID = channelUID.getIdWithoutGroup();
        State oldState = currentState.getOrDefault(channelID, UnDefType.NULL);
        State newState = oldState;

        if (iface == null) {
            newState = UnDefType.NULL;
        } else {
            switch (channelID) {
                case CHANNEL_NAME:
                    newState = StateUtil.stringOrNull(iface.getName());
                    break;
                case CHANNEL_COMMENT:
                    newState = StateUtil.stringOrNull(iface.getComment());
                    break;
                case CHANNEL_TYPE:
                    newState = StateUtil.stringOrNull(iface.getType());
                    break;
                case CHANNEL_MAC:
                    newState = StateUtil.stringOrNull(iface.getMacAddress());
                    break;
                case CHANNEL_ENABLED:
                    newState = StateUtil.boolOrNull(iface.isEnabled());
                    break;
                case CHANNEL_CONNECTED:
                    newState = StateUtil.boolOrNull(iface.isConnected());
                    break;
                case CHANNEL_LAST_LINK_DOWN_TIME:
                    newState = StateUtil.timeOrNull(iface.getLastLinkDownTime());
                    break;
                case CHANNEL_LAST_LINK_UP_TIME:
                    newState = StateUtil.timeOrNull(iface.getLastLinkUpTime());
                    break;
                case CHANNEL_LINK_DOWNS:
                    newState = StateUtil.intOrNull(iface.getLinkDowns());
                    break;
                case CHANNEL_TX_DATA_RATE:
                    newState = StateUtil.floatOrNull(txByteRate.getMegabitRate());
                    break;
                case CHANNEL_RX_DATA_RATE:
                    newState = StateUtil.floatOrNull(rxByteRate.getMegabitRate());
                    break;
                case CHANNEL_TX_PACKET_RATE:
                    newState = StateUtil.floatOrNull(txPacketRate.getMegabitRate());
                    break;
                case CHANNEL_RX_PACKET_RATE:
                    newState = StateUtil.floatOrNull(rxPacketRate.getMegabitRate());
                    break;
                case CHANNEL_TX_BYTES:
                    newState = StateUtil.bigIntOrNull(iface.getTxBytes());
                    break;
                case CHANNEL_RX_BYTES:
                    newState = StateUtil.bigIntOrNull(iface.getRxBytes());
                    break;
                case CHANNEL_TX_PACKETS:
                    newState = StateUtil.bigIntOrNull(iface.getTxPackets());
                    break;
                case CHANNEL_RX_PACKETS:
                    newState = StateUtil.bigIntOrNull(iface.getRxPackets());
                    break;
                case CHANNEL_TX_DROPS:
                    newState = StateUtil.bigIntOrNull(iface.getTxDrops());
                    break;
                case CHANNEL_RX_DROPS:
                    newState = StateUtil.bigIntOrNull(iface.getRxDrops());
                    break;
                case CHANNEL_TX_ERRORS:
                    newState = StateUtil.bigIntOrNull(iface.getTxErrors());
                    break;
                case CHANNEL_RX_ERRORS:
                    newState = StateUtil.bigIntOrNull(iface.getRxErrors());
                    break;
                default:
                    if (iface instanceof RouterosEthernetInterface) {
                        newState = getEtherIterfacefaceChannelState(channelID);
                    } else if (iface instanceof RouterosCapInterface) {
                        newState = getCapIterfaceChannelState(channelID);
                    } else if (iface instanceof RouterosPPPoECliInterface) {
                        newState = getPPPoECliChannelState(channelID);
                    } else if (iface instanceof RouterosL2TPSrvInterface) {
                        newState = getL2TPSrvChannelState(channelID);
                    }
            }
        }

        logger.trace("About to update state on channel {} for thing {} - newState({}) = {}, oldState = {}", channelUID,
                getThing().getUID(), newState.getClass().getSimpleName(), newState, oldState);
        if (newState != oldState) {
            updateState(channelID, newState);
            currentState.put(channelID, newState);
        }
    }

    protected State getEtherIterfacefaceChannelState(String channelID) {
        RouterosEthernetInterface etherIface = (RouterosEthernetInterface) iface;
        switch (channelID) {
            case CHANNEL_DEFAULT_NAME:
                return StateUtil.stringOrNull(etherIface.getDefaultName());
            default:
                return UnDefType.UNDEF;
        }
    }

    protected State getCapIterfaceChannelState(String channelID) {
        RouterosCapInterface capIface = (RouterosCapInterface) iface;
        switch (channelID) {
            case CHANNEL_STATE:
                return StateUtil.stringOrNull(capIface.getCurrentState());
            case CHANNEL_REGISTERED_CLIENTS:
                return StateUtil.intOrNull(capIface.getRegisteredClients());
            case CHANNEL_AUTHORIZED_CLIENTS:
                return StateUtil.intOrNull(capIface.getAuthorizedClients());
            default:
                return UnDefType.UNDEF;
        }
    }

    protected State getPPPoECliChannelState(String channelID) {
        RouterosPPPoECliInterface pppCli = (RouterosPPPoECliInterface) iface;
        switch (channelID) {
            case CHANNEL_UP_TIME:
                return StateUtil.stringOrNull(pppCli.getUptime());
            case CHANNEL_UP_SINCE:
                return StateUtil.timeOrNull(pppCli.calculateUptimeStart());
            default:
                return UnDefType.UNDEF;
        }
    }

    protected State getL2TPSrvChannelState(String channelID) {
        RouterosL2TPSrvInterface vpnSrv = (RouterosL2TPSrvInterface) iface;
        switch (channelID) {
            case CHANNEL_UP_TIME:
                return StateUtil.stringOrNull(vpnSrv.getUptime());
            case CHANNEL_UP_SINCE:
                return StateUtil.timeOrNull(vpnSrv.calculateUptimeStart());
            default:
                return UnDefType.UNDEF;
        }
    }

    @Override
    protected void executeCommand(ChannelUID channelUID, Command command) {
        if (iface == null)
            return;
        logger.warn("Ignoring unsupported command = {} for channel = {}", command, channelUID);
    }
}
