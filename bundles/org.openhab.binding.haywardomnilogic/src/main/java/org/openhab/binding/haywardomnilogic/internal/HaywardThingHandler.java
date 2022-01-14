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

package org.openhab.binding.haywardomnilogic.internal;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.haywardomnilogic.internal.handler.HaywardBridgeHandler;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;

/**
 * The {@link HaywarThingHandler} is a subclass of the BaseThingHandler and a Super
 * Class to each Hayward Thing Handler
 *
 * @author Matt Myers - Initial contribution
 */

@NonNullByDefault
public abstract class HaywardThingHandler extends BaseThingHandler {

    public HaywardThingHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    public abstract void getTelemetry(String xmlResponse) throws HaywardException;

    public void setStateDescriptions() throws HaywardException {
    }

    public State toState(String type, String channelID, String value) throws NumberFormatException {
        switch (type) {
            case "Number":
                return new DecimalType(value);
            case "Switch":
            case "system.power":
                return Integer.parseInt(value) > 0 ? OnOffType.ON : OnOffType.OFF;
            case "Number:Dimensionless":
                switch (channelID) {
                    case HaywardBindingConstants.CHANNEL_CHLORINATOR_AVGSALTLEVEL:
                        return new QuantityType<>(Integer.parseInt(value), Units.PARTS_PER_MILLION);
                    case HaywardBindingConstants.CHANNEL_CHLORINATOR_INSTANTSALTLEVEL:
                        return new QuantityType<>(Integer.parseInt(value), Units.PARTS_PER_MILLION);
                    case HaywardBindingConstants.CHANNEL_CHLORINATOR_TIMEDPERCENT:
                        return new QuantityType<>(Integer.parseInt(value), Units.PERCENT);
                    case HaywardBindingConstants.CHANNEL_FILTER_LASTSPEED:
                        return new QuantityType<>(Integer.parseInt(value), Units.PERCENT);
                    case HaywardBindingConstants.CHANNEL_FILTER_SPEEDPERCENT:
                        return new QuantityType<>(Integer.parseInt(value), Units.PERCENT);
                    case HaywardBindingConstants.CHANNEL_FILTER_SPEEDRPM:
                    case HaywardBindingConstants.CHANNEL_PUMP_LASTSPEED:
                        return new QuantityType<>(Integer.parseInt(value), Units.PERCENT);
                    case HaywardBindingConstants.CHANNEL_PUMP_SPEEDPERCENT:
                        return new QuantityType<>(Integer.parseInt(value), Units.PERCENT);
                    case HaywardBindingConstants.CHANNEL_PUMP_SPEEDRPM:
                }
                return StringType.valueOf(value);
            case "Number:Temperature":
                Bridge bridge = getBridge();
                if (bridge != null) {
                    HaywardBridgeHandler bridgehandler = (HaywardBridgeHandler) bridge.getHandler();
                    if (bridgehandler != null) {
                        if (bridgehandler.account.units.equals("Standard")) {
                            return new QuantityType<>(Integer.parseInt(value), ImperialUnits.FAHRENHEIT);
                        } else {
                            return new QuantityType<>(Integer.parseInt(value), SIUnits.CELSIUS);
                        }
                    }
                }
                // default to imperial if no bridge
                return new QuantityType<>(Integer.parseInt(value), ImperialUnits.FAHRENHEIT);
            default:
                return StringType.valueOf(value);
        }
    }

    public String cmdToString(Command command) {
        if (command == OnOffType.OFF) {
            return "0";
        } else if (command == OnOffType.ON) {
            return "1";
        } else if (command instanceof DecimalType) {
            return ((DecimalType) command).toString();
        } else if (command instanceof QuantityType) {
            return ((QuantityType<?>) command).format("%1.0f");
        } else {
            return command.toString();
        }
    }

    public Map<String, State> updateData(String channelID, String data) {
        Map<String, State> channelStates = new HashMap<>();
        Channel chan = getThing().getChannel(channelID);
        if (chan != null) {
            String acceptedItemType = chan.getAcceptedItemType();
            if (acceptedItemType != null) {
                State state = toState(acceptedItemType, channelID, data);
                updateState(chan.getUID(), state);
                channelStates.put(channelID, state);
            }
        }
        return channelStates;
    }
}
