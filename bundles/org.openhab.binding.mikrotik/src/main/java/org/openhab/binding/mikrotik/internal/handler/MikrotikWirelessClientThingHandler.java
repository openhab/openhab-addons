/**
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
package org.openhab.binding.mikrotik.internal.handler;

import static org.openhab.binding.mikrotik.internal.MikrotikBindingConstants.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mikrotik.internal.MikrotikBindingConstants;
import org.openhab.binding.mikrotik.internal.config.WirelessClientThingConfig;
import org.openhab.binding.mikrotik.internal.model.RouterosCapsmanRegistration;
import org.openhab.binding.mikrotik.internal.model.RouterosDevice;
import org.openhab.binding.mikrotik.internal.model.RouterosRegistrationBase;
import org.openhab.binding.mikrotik.internal.model.RouterosWirelessRegistration;
import org.openhab.binding.mikrotik.internal.util.RateCalculator;
import org.openhab.binding.mikrotik.internal.util.StateUtil;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MikrotikWirelessClientThingHandler} is a {@link MikrotikBaseThingHandler} subclass that wraps shared
 * functionality for all wireless clients listed either in CAPsMAN or Wireless RouterOS sections.
 * It is responsible for handling commands, which are sent to one of the channels and emit channel updates whenever
 * required.
 *
 * @author Oleg Vivtash - Initial contribution
 */
@NonNullByDefault
public class MikrotikWirelessClientThingHandler extends MikrotikBaseThingHandler<WirelessClientThingConfig> {
    private final Logger logger = LoggerFactory.getLogger(MikrotikWirelessClientThingHandler.class);

    private @Nullable RouterosRegistrationBase wifiReg;

    private boolean online = false;
    private boolean continuousConnection = false;
    private @Nullable LocalDateTime lastSeen;

    private final RateCalculator txByteRate = new RateCalculator(BigDecimal.ZERO);
    private final RateCalculator rxByteRate = new RateCalculator(BigDecimal.ZERO);
    private final RateCalculator txPacketRate = new RateCalculator(BigDecimal.ZERO);
    private final RateCalculator rxPacketRate = new RateCalculator(BigDecimal.ZERO);

    public static boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return MikrotikBindingConstants.THING_TYPE_WIRELESS_CLIENT.equals(thingTypeUID);
    }

    public MikrotikWirelessClientThingHandler(Thing thing) {
        super(thing);
    }

    private void fetchModels() {
        var cfg = this.config;
        if (cfg != null) {
            RouterosDevice routeros = getRouterOs();

            RouterosWirelessRegistration wifiRegistration = null;
            if (routeros != null) {
                wifiRegistration = routeros.findWirelessRegistration(cfg.mac);
            }
            this.wifiReg = wifiRegistration;
            if (wifiRegistration != null && !cfg.ssid.isBlank()
                    && !cfg.ssid.equalsIgnoreCase(wifiRegistration.getSSID())) {
                this.wifiReg = null;
            }

            if (this.wifiReg == null && routeros != null) {
                // try looking in capsman when there is no wirelessRegistration
                RouterosCapsmanRegistration capsmanReg = routeros.findCapsmanRegistration(cfg.mac);
                this.wifiReg = capsmanReg;
                if (capsmanReg != null && !cfg.ssid.isBlank() && !cfg.ssid.equalsIgnoreCase(capsmanReg.getSSID())) {
                    this.wifiReg = null;
                }
            }
        }
    }

    @Override
    protected void refreshModels() {
        fetchModels();
        var wifiReg = this.wifiReg;
        if (wifiReg != null) {
            this.lastSeen = LocalDateTime.now();
            this.online = true;
            var cfg = this.config;
            int considerContinuous = 180;
            if (cfg != null) {
                considerContinuous = cfg.considerContinuous;
            }
            txByteRate.update(wifiReg.getTxBytes());
            rxByteRate.update(wifiReg.getRxBytes());
            txPacketRate.update(wifiReg.getTxPackets());
            rxPacketRate.update(wifiReg.getRxPackets());
            LocalDateTime uptimeStart = wifiReg.getUptimeStart();
            continuousConnection = (uptimeStart != null)
                    && LocalDateTime.now().isAfter(uptimeStart.plusSeconds(considerContinuous));
        } else {
            this.online = false;
            this.continuousConnection = false;
        }
    }

    @Override
    protected void refreshChannel(ChannelUID channelUID) {
        var wifiReg = this.wifiReg;

        String channelID = channelUID.getIdWithoutGroup();
        State oldState = currentState.getOrDefault(channelID, UnDefType.NULL);
        State newState = oldState;

        if (channelID.equals(CHANNEL_CONNECTED)) {
            newState = StateUtil.boolContactOrNull(this.online);
        } else if (channelID.equals(CHANNEL_LAST_SEEN)) {
            newState = StateUtil.timeOrNull(lastSeen);
        } else if (channelID.equals(CHANNEL_CONTINUOUS)) {
            newState = StateUtil.boolContactOrNull(this.continuousConnection);
        } else if (!this.online || wifiReg == null) {
            newState = UnDefType.NULL;
        } else {
            switch (channelID) {
                case CHANNEL_NAME:
                    newState = StateUtil.stringOrNull(wifiReg.getName());
                    break;
                case CHANNEL_COMMENT:
                    newState = StateUtil.stringOrNull(wifiReg.getComment());
                    break;
                case CHANNEL_MAC:
                    newState = StateUtil.stringOrNull(wifiReg.getMacAddress());
                    break;
                case CHANNEL_INTERFACE:
                    newState = StateUtil.stringOrNull(wifiReg.getInterfaceName());
                    break;
                case CHANNEL_SSID:
                    newState = StateUtil.stringOrNull(wifiReg.getSSID());
                    break;
                case CHANNEL_UP_SINCE:
                    newState = StateUtil.timeOrNull(wifiReg.getUptimeStart());
                    break;
                case CHANNEL_TX_DATA_RATE:
                    newState = StateUtil.qtyMegabitPerSecOrNull(txByteRate.getMegabitRate());
                    break;
                case CHANNEL_RX_DATA_RATE:
                    newState = StateUtil.qtyMegabitPerSecOrNull(rxByteRate.getMegabitRate());
                    break;
                case CHANNEL_TX_PACKET_RATE:
                    newState = StateUtil.floatOrNull(txPacketRate.getRate());
                    break;
                case CHANNEL_RX_PACKET_RATE:
                    newState = StateUtil.floatOrNull(rxPacketRate.getRate());
                    break;
                case CHANNEL_TX_BYTES:
                    newState = StateUtil.bigIntOrNull(wifiReg.getTxBytes());
                    break;
                case CHANNEL_RX_BYTES:
                    newState = StateUtil.bigIntOrNull(wifiReg.getRxBytes());
                    break;
                case CHANNEL_TX_PACKETS:
                    newState = StateUtil.bigIntOrNull(wifiReg.getTxPackets());
                    break;
                case CHANNEL_RX_PACKETS:
                    newState = StateUtil.bigIntOrNull(wifiReg.getRxPackets());
                    break;
                default:
                    newState = UnDefType.NULL;
                    if (wifiReg instanceof RouterosWirelessRegistration) {
                        newState = getWirelessRegistrationChannelState(channelID);
                    } else if (wifiReg instanceof RouterosCapsmanRegistration) {
                        newState = getCapsmanRegistrationChannelState(channelID);
                    }
            }
        }

        if (!newState.equals(oldState)) {
            updateState(channelID, newState);
            currentState.put(channelID, newState);
        }
    }

    @SuppressWarnings("null")
    protected State getCapsmanRegistrationChannelState(String channelID) {
        if (this.wifiReg == null) {
            return UnDefType.UNDEF;
        }

        RouterosCapsmanRegistration capsmanReg = (RouterosCapsmanRegistration) this.wifiReg;
        switch (channelID) {
            case CHANNEL_SIGNAL:
                return StateUtil.intOrNull(capsmanReg.getRxSignal());
            default:
                return UnDefType.UNDEF;
        }
    }

    @SuppressWarnings("null")
    protected State getWirelessRegistrationChannelState(String channelID) {
        if (this.wifiReg == null) {
            return UnDefType.UNDEF;
        }

        RouterosWirelessRegistration wirelessReg = (RouterosWirelessRegistration) this.wifiReg;
        switch (channelID) {
            case CHANNEL_SIGNAL:
                return StateUtil.intOrNull(wirelessReg.getRxSignal());
            default:
                return UnDefType.UNDEF;
        }
    }

    @Override
    protected void executeCommand(ChannelUID channelUID, Command command) {
        if (!online) {
            return;
        }
        logger.warn("Ignoring unsupported command = {} for channel = {}", command, channelUID);
    }
}
