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
package org.openhab.binding.mikrotik.internal.handler;

import static org.eclipse.smarthome.core.thing.ThingStatus.OFFLINE;
import static org.eclipse.smarthome.core.thing.ThingStatus.ONLINE;
import static org.eclipse.smarthome.core.thing.ThingStatusDetail.CONFIGURATION_ERROR;
import static org.openhab.binding.mikrotik.internal.MikrotikBindingConstants.*;

import java.math.BigDecimal;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.joda.time.DateTime;
import org.openhab.binding.mikrotik.internal.MikrotikBindingConstants;
import org.openhab.binding.mikrotik.internal.config.WirelessClientThingConfig;
import org.openhab.binding.mikrotik.internal.model.RouterosCapsmanRegistration;
import org.openhab.binding.mikrotik.internal.model.RouterosWirelessRegistration;
import org.openhab.binding.mikrotik.internal.util.RateCalculator;
import org.openhab.binding.mikrotik.internal.util.StateUtil;
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

    private @Nullable WirelessClientThingConfig config;
    private @Nullable RouterosWirelessRegistration wirelessRegistration;
    private @Nullable RouterosCapsmanRegistration capsmanRegistration;

    private boolean online = false;
    private boolean continuousConnection = false;
    private @Nullable DateTime lastSeen;

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

    @Override
    protected void initialize(@NonNull WirelessClientThingConfig config) {
        if (this.config == null) {
            logger.debug("Initializing WirelessClientThingHandler with config = {}", config);
            if (!config.isValid()) {
                updateStatus(OFFLINE, CONFIGURATION_ERROR, "WirelessClientThingConfig is invalid");
                return;
            }
            this.config = config;
            updateStatus(ONLINE);
        }
    }

    private boolean fetchModels() {
        logger.trace("Searching for {} registration", config.mac);
        wirelessRegistration = getRouteros().findWirelessRegistration(config.mac);
        if (StringUtils.isNotBlank(config.ssid) && !config.ssid.equalsIgnoreCase(wirelessRegistration.getSSID())) {
            wirelessRegistration = null;
        }

        if (wirelessRegistration == null) { // try looking in capsman when there is no wirelessRegistration
            capsmanRegistration = getRouteros().findCapsmanRegistration(config.mac);
            if (StringUtils.isNotBlank(config.ssid) && !config.ssid.equalsIgnoreCase(capsmanRegistration.getSSID())) {
                capsmanRegistration = null;
            }
        } else {
            capsmanRegistration = null;
        }

        return wirelessRegistration != null || capsmanRegistration != null;
    }

    @Override
    protected void refreshModels() {
        if (getRouteros() != null && config != null) {
            online = fetchModels();
            if (online) {
                lastSeen = DateTime.now();
            } else {
                continuousConnection = false;
            }
            if (capsmanRegistration != null) {
                // TODO Should be applied to wirelessRegistration as well
                txByteRate.update(capsmanRegistration.getTxBytes());
                rxByteRate.update(capsmanRegistration.getRxBytes());
                txPacketRate.update(capsmanRegistration.getTxPackets());
                rxPacketRate.update(capsmanRegistration.getRxPackets());
                continuousConnection = DateTime.now()
                        .isAfter(capsmanRegistration.getUptimeStart().plusSeconds(config.considerContinuous));
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

        if (channelID.equals(CHANNEL_CONNECTED)) {
            newState = StateUtil.boolOrNull(online);
        } else if (channelID.equals(CHANNEL_LAST_SEEN)) {
            newState = StateUtil.timeOrNull(lastSeen);
        } else if (online) {
            if (wirelessRegistration != null) {
                newState = getWirelessRegistrationChannelState(channelID);
            } else if (capsmanRegistration != null) {
                newState = getCapsmanRegistrationChannelState(channelID);
            }
        } else {
            newState = UnDefType.NULL;
        }

        logger.trace("About to update state on channel {} for thing {} - newState({}) = {}, oldState = {}", channelUID,
                getThing().getUID(), newState.getClass().getSimpleName(), newState, oldState);
        if (newState != oldState) {
            updateState(channelID, newState);
            currentState.put(channelID, newState);
        }
    }

    protected State getCapsmanRegistrationChannelState(String channelID) {
        switch (channelID) {
            case CHANNEL_CONTINUOUS:
                return StateUtil.boolOrNull(continuousConnection);
            case CHANNEL_INTERFACE:
                return StateUtil.stringOrNull(capsmanRegistration.getInterfaceName());
            case CHANNEL_COMMENT:
                return StateUtil.stringOrNull(capsmanRegistration.getComment());
            case CHANNEL_MAC:
                return StateUtil.stringOrNull(capsmanRegistration.getMacAddress());
            case CHANNEL_SSID:
                return StateUtil.stringOrNull(capsmanRegistration.getSSID());
            case CHANNEL_SIGNAL:
                return StateUtil.intOrNull(capsmanRegistration.getRxSignal());
            case CHANNEL_UP_TIME:
                return StateUtil.stringOrNull(capsmanRegistration.getUptime());
            case CHANNEL_UP_SINCE:
                return StateUtil.timeOrNull(capsmanRegistration.getUptimeStart());
            case CHANNEL_TX_DATA_RATE:
                return StateUtil.floatOrNull(txByteRate.getMegabitRate());
            case CHANNEL_RX_DATA_RATE:
                return StateUtil.floatOrNull(rxByteRate.getMegabitRate());
            case CHANNEL_TX_PACKET_RATE:
                return StateUtil.floatOrNull(txPacketRate.getMegabitRate());
            case CHANNEL_RX_PACKET_RATE:
                return StateUtil.floatOrNull(rxPacketRate.getMegabitRate());
            case CHANNEL_TX_BYTES:
                return StateUtil.bigIntOrNull(capsmanRegistration.getTxBytes());
            case CHANNEL_RX_BYTES:
                return StateUtil.bigIntOrNull(capsmanRegistration.getRxBytes());
            case CHANNEL_TX_PACKETS:
                return StateUtil.bigIntOrNull(capsmanRegistration.getTxPackets());
            case CHANNEL_RX_PACKETS:
                return StateUtil.bigIntOrNull(capsmanRegistration.getRxPackets());
            default:
                return UnDefType.UNDEF;
        }
    }

    protected State getWirelessRegistrationChannelState(String channelID) {
        switch (channelID) {
            case CHANNEL_COMMENT:
                return StateUtil.stringOrNull(wirelessRegistration.getComment());
            case CHANNEL_MAC:
                return StateUtil.stringOrNull(wirelessRegistration.getMacAddress());
            case CHANNEL_SSID:
                return StateUtil.stringOrNull(wirelessRegistration.getSSID());
            default:
                return UnDefType.UNDEF;
        }
    }

    @Override
    protected void executeCommand(ChannelUID channelUID, Command command) {
        if (!online)
            return;
        logger.warn("Ignoring unsupported command = {} for channel = {}", command, channelUID);
    }
}
