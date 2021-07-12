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

import static org.openhab.core.thing.ThingStatus.OFFLINE;
import static org.openhab.core.thing.ThingStatus.ONLINE;
import static org.openhab.core.thing.ThingStatusDetail.GONE;

import java.math.BigDecimal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mikrotik.internal.MikrotikBindingConstants;
import org.openhab.binding.mikrotik.internal.config.InterfaceThingConfig;
import org.openhab.binding.mikrotik.internal.model.RouterosCapInterface;
import org.openhab.binding.mikrotik.internal.model.RouterosEthernetInterface;
import org.openhab.binding.mikrotik.internal.model.RouterosInterfaceBase;
import org.openhab.binding.mikrotik.internal.model.RouterosL2TPCliInterface;
import org.openhab.binding.mikrotik.internal.model.RouterosL2TPSrvInterface;
import org.openhab.binding.mikrotik.internal.model.RouterosPPPoECliInterface;
import org.openhab.binding.mikrotik.internal.model.RouterosWlanInterface;
import org.openhab.binding.mikrotik.internal.util.RateCalculator;
import org.openhab.binding.mikrotik.internal.util.StateUtil;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MikrotikInterfaceThingHandler} is a {@link MikrotikBaseThingHandler} subclass that wraps shared
 * functionality for all interface things of different types. It is responsible for handling commands, which are
 * sent to one of the channels and emit channel updates whenever required.
 *
 * @author Oleg Vivtash - Initial contribution
 */
@NonNullByDefault
public class MikrotikInterfaceThingHandler extends MikrotikBaseThingHandler<InterfaceThingConfig> {
    private final Logger logger = LoggerFactory.getLogger(MikrotikInterfaceThingHandler.class);

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
    protected void updateStatus(ThingStatus status, ThingStatusDetail statusDetail, @Nullable String description) {
        if (getRouterOs() != null) {
            if (status == ONLINE || (status == OFFLINE && statusDetail == ThingStatusDetail.COMMUNICATION_ERROR)) {
                getRouterOs().registerForMonitoring(config.name);
            } else if (status == OFFLINE
                    && (statusDetail == ThingStatusDetail.CONFIGURATION_ERROR || statusDetail == GONE)) {
                getRouterOs().unregisterForMonitoring(config.name);
            }
        }
        super.updateStatus(status, statusDetail, description);
    }

    @Override
    protected void refreshModels() {
        logger.trace("Searching for {} interface", config.name);
        iface = getRouterOs().findInterface(config.name);
        if (iface == null) {
            String statusMsg = String.format("Interface %s is not found in RouterOS for thing %s", config.name,
                    getThing().getUID());
            updateStatus(OFFLINE, GONE, statusMsg);
        } else {
            txByteRate.update(iface.getTxBytes());
            rxByteRate.update(iface.getRxBytes());
            txPacketRate.update(iface.getTxPackets());
            rxPacketRate.update(iface.getRxPackets());
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
                case MikrotikBindingConstants.CHANNEL_NAME:
                    newState = StateUtil.stringOrNull(iface.getName());
                    break;
                case MikrotikBindingConstants.CHANNEL_COMMENT:
                    newState = StateUtil.stringOrNull(iface.getComment());
                    break;
                case MikrotikBindingConstants.CHANNEL_TYPE:
                    newState = StateUtil.stringOrNull(iface.getType());
                    break;
                case MikrotikBindingConstants.CHANNEL_MAC:
                    newState = StateUtil.stringOrNull(iface.getMacAddress());
                    break;
                case MikrotikBindingConstants.CHANNEL_ENABLED:
                    newState = StateUtil.boolOrNull(iface.isEnabled());
                    break;
                case MikrotikBindingConstants.CHANNEL_CONNECTED:
                    newState = StateUtil.boolOrNull(iface.isConnected());
                    break;
                case MikrotikBindingConstants.CHANNEL_LAST_LINK_DOWN_TIME:
                    newState = StateUtil.timeOrNull(iface.getLastLinkDownTime());
                    break;
                case MikrotikBindingConstants.CHANNEL_LAST_LINK_UP_TIME:
                    newState = StateUtil.timeOrNull(iface.getLastLinkUpTime());
                    break;
                case MikrotikBindingConstants.CHANNEL_LINK_DOWNS:
                    newState = StateUtil.intOrNull(iface.getLinkDowns());
                    break;
                case MikrotikBindingConstants.CHANNEL_TX_DATA_RATE:
                    newState = StateUtil.floatOrNull(txByteRate.getMegabitRate());
                    break;
                case MikrotikBindingConstants.CHANNEL_RX_DATA_RATE:
                    newState = StateUtil.floatOrNull(rxByteRate.getMegabitRate());
                    break;
                case MikrotikBindingConstants.CHANNEL_TX_PACKET_RATE:
                    newState = StateUtil.floatOrNull(txPacketRate.getRate());
                    break;
                case MikrotikBindingConstants.CHANNEL_RX_PACKET_RATE:
                    newState = StateUtil.floatOrNull(rxPacketRate.getRate());
                    break;
                case MikrotikBindingConstants.CHANNEL_TX_BYTES:
                    newState = StateUtil.bigIntOrNull(iface.getTxBytes());
                    break;
                case MikrotikBindingConstants.CHANNEL_RX_BYTES:
                    newState = StateUtil.bigIntOrNull(iface.getRxBytes());
                    break;
                case MikrotikBindingConstants.CHANNEL_TX_PACKETS:
                    newState = StateUtil.bigIntOrNull(iface.getTxPackets());
                    break;
                case MikrotikBindingConstants.CHANNEL_RX_PACKETS:
                    newState = StateUtil.bigIntOrNull(iface.getRxPackets());
                    break;
                case MikrotikBindingConstants.CHANNEL_TX_DROPS:
                    newState = StateUtil.bigIntOrNull(iface.getTxDrops());
                    break;
                case MikrotikBindingConstants.CHANNEL_RX_DROPS:
                    newState = StateUtil.bigIntOrNull(iface.getRxDrops());
                    break;
                case MikrotikBindingConstants.CHANNEL_TX_ERRORS:
                    newState = StateUtil.bigIntOrNull(iface.getTxErrors());
                    break;
                case MikrotikBindingConstants.CHANNEL_RX_ERRORS:
                    newState = StateUtil.bigIntOrNull(iface.getRxErrors());
                    break;
                default:
                    if (iface instanceof RouterosEthernetInterface) {
                        newState = getEtherIterfaceChannelState(channelID);
                    } else if (iface instanceof RouterosCapInterface) {
                        newState = getCapIterfaceChannelState(channelID);
                    } else if (iface instanceof RouterosWlanInterface) {
                        newState = getWlanIterfaceChannelState(channelID);
                    } else if (iface instanceof RouterosPPPoECliInterface) {
                        newState = getPPPoECliChannelState(channelID);
                    } else if (iface instanceof RouterosL2TPSrvInterface) {
                        newState = getL2TPSrvChannelState(channelID);
                    } else if (iface instanceof RouterosL2TPCliInterface) {
                        newState = getL2TPCliChannelState(channelID);
                    }
            }
        }

        logger.trace("About to update state on channel {} for thing {} - newState({}) = {}, oldState = {}", channelUID,
                getThing().getUID(), newState.getClass().getSimpleName(), newState, oldState);
        if (!newState.equals(oldState)) {
            updateState(channelID, newState);
            currentState.put(channelID, newState);
        }
    }

    protected State getEtherIterfaceChannelState(String channelID) {
        RouterosEthernetInterface etherIface = (RouterosEthernetInterface) iface;
        switch (channelID) {
            case MikrotikBindingConstants.CHANNEL_DEFAULT_NAME:
                return StateUtil.stringOrNull(etherIface.getDefaultName());
            case MikrotikBindingConstants.CHANNEL_STATE:
                return StateUtil.stringOrNull(etherIface.getState());
            case MikrotikBindingConstants.CHANNEL_RATE:
                return StateUtil.stringOrNull(etherIface.getRate());
            default:
                return UnDefType.UNDEF;
        }
    }

    protected State getCapIterfaceChannelState(String channelID) {
        RouterosCapInterface capIface = (RouterosCapInterface) iface;
        switch (channelID) {
            case MikrotikBindingConstants.CHANNEL_STATE:
                return StateUtil.stringOrNull(capIface.getCurrentState());
            case MikrotikBindingConstants.CHANNEL_RATE:
                return StateUtil.stringOrNull(capIface.getRateSet());
            case MikrotikBindingConstants.CHANNEL_REGISTERED_CLIENTS:
                return StateUtil.intOrNull(capIface.getRegisteredClients());
            case MikrotikBindingConstants.CHANNEL_AUTHORIZED_CLIENTS:
                return StateUtil.intOrNull(capIface.getAuthorizedClients());
            default:
                return UnDefType.UNDEF;
        }
    }

    protected State getWlanIterfaceChannelState(String channelID) {
        RouterosWlanInterface wlIface = (RouterosWlanInterface) iface;
        switch (channelID) {
            case MikrotikBindingConstants.CHANNEL_STATE:
                return StateUtil.stringOrNull(wlIface.getCurrentState());
            case MikrotikBindingConstants.CHANNEL_RATE:
                return StateUtil.stringOrNull(wlIface.getRate());
            case MikrotikBindingConstants.CHANNEL_REGISTERED_CLIENTS:
                return StateUtil.intOrNull(wlIface.getRegisteredClients());
            case MikrotikBindingConstants.CHANNEL_AUTHORIZED_CLIENTS:
                return StateUtil.intOrNull(wlIface.getAuthorizedClients());
            default:
                return UnDefType.UNDEF;
        }
    }

    protected State getPPPoECliChannelState(String channelID) {
        RouterosPPPoECliInterface pppCli = (RouterosPPPoECliInterface) iface;
        switch (channelID) {
            case MikrotikBindingConstants.CHANNEL_STATE:
                return StateUtil.stringOrNull(pppCli.getStatus());
            case MikrotikBindingConstants.CHANNEL_UP_TIME:
                return StateUtil.stringOrNull(pppCli.getUptime());
            case MikrotikBindingConstants.CHANNEL_UP_SINCE:
                return StateUtil.timeOrNull(pppCli.getUptimeStart());
            default:
                return UnDefType.UNDEF;
        }
    }

    protected State getL2TPSrvChannelState(String channelID) {
        RouterosL2TPSrvInterface vpnSrv = (RouterosL2TPSrvInterface) iface;
        switch (channelID) {
            case MikrotikBindingConstants.CHANNEL_STATE:
                return StateUtil.stringOrNull(vpnSrv.getEncoding());
            case MikrotikBindingConstants.CHANNEL_UP_TIME:
                return StateUtil.stringOrNull(vpnSrv.getUptime());
            case MikrotikBindingConstants.CHANNEL_UP_SINCE:
                return StateUtil.timeOrNull(vpnSrv.getUptimeStart());
            default:
                return UnDefType.UNDEF;
        }
    }

    protected State getL2TPCliChannelState(String channelID) {
        RouterosL2TPCliInterface vpnCli = (RouterosL2TPCliInterface) iface;
        switch (channelID) {
            case MikrotikBindingConstants.CHANNEL_STATE:
                return StateUtil.stringOrNull(vpnCli.getEncoding());
            case MikrotikBindingConstants.CHANNEL_UP_TIME:
                return StateUtil.stringOrNull(vpnCli.getUptime());
            case MikrotikBindingConstants.CHANNEL_UP_SINCE:
                return StateUtil.timeOrNull(vpnCli.getUptimeStart());
            default:
                return UnDefType.UNDEF;
        }
    }

    @Override
    protected void executeCommand(ChannelUID channelUID, Command command) {
        if (iface == null) {
            return;
        }
        logger.warn("Ignoring unsupported command = {} for channel = {}", command, channelUID);
    }
}
