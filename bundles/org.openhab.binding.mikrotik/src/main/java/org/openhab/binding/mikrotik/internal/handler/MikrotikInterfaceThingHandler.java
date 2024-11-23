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
package org.openhab.binding.mikrotik.internal.handler;

import static org.openhab.core.thing.ThingStatus.*;
import static org.openhab.core.thing.ThingStatusDetail.GONE;

import java.math.BigDecimal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mikrotik.internal.MikrotikBindingConstants;
import org.openhab.binding.mikrotik.internal.config.InterfaceThingConfig;
import org.openhab.binding.mikrotik.internal.model.RouterosCapInterface;
import org.openhab.binding.mikrotik.internal.model.RouterosDevice;
import org.openhab.binding.mikrotik.internal.model.RouterosEthernetInterface;
import org.openhab.binding.mikrotik.internal.model.RouterosInterfaceBase;
import org.openhab.binding.mikrotik.internal.model.RouterosL2TPCliInterface;
import org.openhab.binding.mikrotik.internal.model.RouterosL2TPSrvInterface;
import org.openhab.binding.mikrotik.internal.model.RouterosLTEInterface;
import org.openhab.binding.mikrotik.internal.model.RouterosPPPCliInterface;
import org.openhab.binding.mikrotik.internal.model.RouterosPPPoECliInterface;
import org.openhab.binding.mikrotik.internal.model.RouterosWifiInterface;
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
        RouterosDevice routeros = getRouterOs();
        InterfaceThingConfig cfg = this.config;
        if (routeros != null && cfg != null) {
            if (status == ONLINE || (status == OFFLINE && statusDetail == ThingStatusDetail.COMMUNICATION_ERROR)) {
                routeros.registerForMonitoring(cfg.name);
            } else if (status == OFFLINE
                    && (statusDetail == ThingStatusDetail.CONFIGURATION_ERROR || statusDetail == GONE)) {
                routeros.unregisterForMonitoring(cfg.name);
            }
        }
        super.updateStatus(status, statusDetail, description);
    }

    @Override
    protected void refreshModels() {
        RouterosDevice routeros = getRouterOs();
        InterfaceThingConfig cfg = this.config;
        if (routeros != null && cfg != null) {
            RouterosInterfaceBase rosInterface = routeros.findInterface(cfg.name);
            this.iface = rosInterface;
            if (rosInterface == null) {
                String statusMsg = String.format("RouterOS interface %s is not found for thing %s", cfg.name,
                        getThing().getUID());
                updateStatus(OFFLINE, GONE, statusMsg);
            } else {
                txByteRate.update(rosInterface.getTxBytes());
                rxByteRate.update(rosInterface.getRxBytes());
                txPacketRate.update(rosInterface.getTxPackets());
                rxPacketRate.update(rosInterface.getRxPackets());
            }
        }
    }

    @Override
    protected void refreshChannel(ChannelUID channelUID) {
        String channelID = channelUID.getIdWithoutGroup();
        State oldState = currentState.getOrDefault(channelID, UnDefType.NULL);
        State newState = oldState;
        RouterosInterfaceBase iface = this.iface;
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
                    newState = StateUtil.boolSwitchOrNull(iface.isEnabled());
                    break;
                case MikrotikBindingConstants.CHANNEL_CONNECTED:
                    newState = StateUtil.boolContactOrNull(iface.isConnected());
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
                    newState = StateUtil.qtyMegabitPerSecOrNull(txByteRate.getMegabitRate());
                    break;
                case MikrotikBindingConstants.CHANNEL_RX_DATA_RATE:
                    newState = StateUtil.qtyMegabitPerSecOrNull(rxByteRate.getMegabitRate());
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
                    } else if (iface instanceof RouterosWifiInterface) {
                        newState = getWifiIterfaceChannelState(channelID);
                    } else if (iface instanceof RouterosWlanInterface) {
                        newState = getWlanIterfaceChannelState(channelID);
                    } else if (iface instanceof RouterosPPPCliInterface) {
                        newState = getPPPCliChannelState(channelID);
                    } else if (iface instanceof RouterosPPPoECliInterface) {
                        newState = getPPPoECliChannelState(channelID);
                    } else if (iface instanceof RouterosL2TPSrvInterface) {
                        newState = getL2TPSrvChannelState(channelID);
                    } else if (iface instanceof RouterosL2TPCliInterface) {
                        newState = getL2TPCliChannelState(channelID);
                    } else if (iface instanceof RouterosLTEInterface) {
                        newState = getLTEChannelState(channelID);
                    }
            }
        }

        if (!newState.equals(oldState)) {
            updateState(channelID, newState);
            currentState.put(channelID, newState);
        }
    }

    protected State getEtherIterfaceChannelState(String channelID) {
        RouterosEthernetInterface etherIface = (RouterosEthernetInterface) this.iface;
        if (etherIface == null) {
            return UnDefType.UNDEF;
        }

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
        RouterosCapInterface capIface = (RouterosCapInterface) this.iface;
        if (capIface == null) {
            return UnDefType.UNDEF;
        }

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
        RouterosWlanInterface wlIface = (RouterosWlanInterface) this.iface;
        if (wlIface == null) {
            return UnDefType.UNDEF;
        }

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

    protected State getWifiIterfaceChannelState(String channelID) {
        RouterosWifiInterface wlIface = (RouterosWifiInterface) this.iface;
        if (wlIface == null) {
            return UnDefType.UNDEF;
        }

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
        RouterosPPPoECliInterface pppCli = (RouterosPPPoECliInterface) this.iface;
        if (pppCli == null) {
            return UnDefType.UNDEF;
        }

        switch (channelID) {
            case MikrotikBindingConstants.CHANNEL_STATE:
                return StateUtil.stringOrNull(pppCli.getStatus());
            case MikrotikBindingConstants.CHANNEL_UP_SINCE:
                return StateUtil.timeOrNull(pppCli.getUptimeStart());
            default:
                return UnDefType.UNDEF;
        }
    }

    protected State getPPPCliChannelState(String channelID) {
        RouterosPPPCliInterface pppCli = (RouterosPPPCliInterface) this.iface;
        if (pppCli == null) {
            return UnDefType.UNDEF;
        }

        switch (channelID) {
            case MikrotikBindingConstants.CHANNEL_STATE:
                return StateUtil.stringOrNull(pppCli.getStatus());
            case MikrotikBindingConstants.CHANNEL_UP_SINCE:
                return StateUtil.timeOrNull(pppCli.getUptimeStart());
            default:
                return UnDefType.UNDEF;
        }
    }

    protected State getL2TPSrvChannelState(String channelID) {
        RouterosL2TPSrvInterface vpnSrv = (RouterosL2TPSrvInterface) this.iface;
        if (vpnSrv == null) {
            return UnDefType.UNDEF;
        }

        switch (channelID) {
            case MikrotikBindingConstants.CHANNEL_STATE:
                return StateUtil.stringOrNull(vpnSrv.getEncoding());
            case MikrotikBindingConstants.CHANNEL_UP_SINCE:
                return StateUtil.timeOrNull(vpnSrv.getUptimeStart());
            default:
                return UnDefType.UNDEF;
        }
    }

    protected State getL2TPCliChannelState(String channelID) {
        RouterosL2TPCliInterface vpnCli = (RouterosL2TPCliInterface) this.iface;
        if (vpnCli == null) {
            return UnDefType.UNDEF;
        }

        switch (channelID) {
            case MikrotikBindingConstants.CHANNEL_STATE:
                return StateUtil.stringOrNull(vpnCli.getEncoding());
            case MikrotikBindingConstants.CHANNEL_UP_SINCE:
                return StateUtil.timeOrNull(vpnCli.getUptimeStart());
            default:
                return UnDefType.UNDEF;
        }
    }

    protected State getLTEChannelState(String channelID) {
        RouterosLTEInterface lte = (RouterosLTEInterface) this.iface;
        if (lte == null) {
            return UnDefType.UNDEF;
        }

        switch (channelID) {
            case MikrotikBindingConstants.CHANNEL_STATE:
                return StateUtil.stringOrNull(lte.getStatus());
            case MikrotikBindingConstants.CHANNEL_UP_SINCE:
                return StateUtil.timeOrNull(lte.getUptimeStart());
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
