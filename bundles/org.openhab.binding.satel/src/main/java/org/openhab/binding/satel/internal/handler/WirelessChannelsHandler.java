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
package org.openhab.binding.satel.internal.handler;

import static org.openhab.binding.satel.internal.SatelBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.satel.internal.types.StateType;
import org.openhab.binding.satel.internal.types.TroubleState;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;

/**
 * The {@link WirelessChannelsHandler} is base thing handler class for things that can be wireless devices.
 * It adds support for two additional channels:
 * <ul>
 * <li><i>device_lobatt</i> - low battery indication</li>
 * <li><i>device_nocomm</i> - communication problems indication</li>
 * </ul>
 * adding them if the device is configured as a wireless device.
 *
 * @author Krzysztof Goworek - Initial contribution
 */
@NonNullByDefault
public abstract class WirelessChannelsHandler extends SatelStateThingHandler {

    public WirelessChannelsHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        super.initialize();

        withBridgeHandlerPresent(bridgeHandler -> {
            // add/remove channels depending on whether or not the device is wireless
            final int wirelessDeviceId = getThingConfig().getId() - bridgeHandler.getIntegraType().getOnMainboard();
            ThingBuilder thingBuilder = editThing();
            if (isWirelessDevice() && wirelessDeviceId > 0) {
                // if a wireless device, remove channels for wireless devices
                if (getChannel(TroubleState.DEVICE_LOBATT) == null) {
                    thingBuilder.withChannel(ChannelBuilder.create(getChannelUID(TroubleState.DEVICE_LOBATT), "Switch")
                            .withType(CHANNEL_TYPE_LOBATT).build());
                }
                if (getChannel(TroubleState.DEVICE_NOCOMM) == null) {
                    thingBuilder.withChannel(ChannelBuilder.create(getChannelUID(TroubleState.DEVICE_NOCOMM), "Switch")
                            .withType(CHANNEL_TYPE_NOCOMM).build());
                }
            } else {
                // if not a wireless device, remove channels for wireless devices
                thingBuilder.withoutChannel(getChannelUID(TroubleState.DEVICE_LOBATT))
                        .withoutChannel(getChannelUID(TroubleState.DEVICE_NOCOMM));
            }
            updateThing(thingBuilder.build());
        });
    }

    /**
     * Defines the thing as a wireless or wired device.
     *
     * @return <code>true</code> if the thing is, or is configured as a wireless device
     */
    protected boolean isWirelessDevice() {
        return getThingConfig().isWireless();
    }

    @Override
    protected int getStateBitNbr(StateType stateType) {
        int bitNbr = getThingConfig().getId() - 1;
        if (stateType instanceof TroubleState troubleState) {
            // for wireless devices we need to correct bit number
            switch (troubleState) {
                case DEVICE_LOBATT1:
                case DEVICE_NOCOMM1:
                case OUTPUT_NOCOMM1:
                    bitNbr -= 120;
                    // pass through
                case DEVICE_LOBATT:
                case DEVICE_NOCOMM:
                case OUTPUT_NOCOMM:
                    bitNbr -= getBridgeHandler().getIntegraType().getOnMainboard();
                    break;
                default:
                    // other states are either not supported or don't need correction
                    break;
            }
        }
        return bitNbr;
    }

    @Override
    protected StateType getStateType(String channelId) {
        String stateName = channelId.toUpperCase();
        if (TroubleState.DEVICE_LOBATT.name().equals(stateName)
                || TroubleState.DEVICE_NOCOMM.name().equals(stateName)) {
            if (getThingConfig().getId() - getBridgeHandler().getIntegraType().getOnMainboard() > 120) {
                // last 120 ACU-100 devices in INTEGRA 256 PLUS
                stateName += "1";
            }
            return TroubleState.valueOf(stateName);
        } else {
            return StateType.NONE;
        }
    }

    /**
     * Returns channel UID for given state type.
     *
     * @param stateType state type to get channel UID for
     * @return channel UID object
     */
    private ChannelUID getChannelUID(StateType stateType) {
        String channelId = stateType.toString().toLowerCase();
        return new ChannelUID(getThing().getUID(), channelId);
    }
}
