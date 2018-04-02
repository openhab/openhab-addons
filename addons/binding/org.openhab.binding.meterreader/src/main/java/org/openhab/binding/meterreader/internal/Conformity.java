/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.meterreader.internal;

import java.util.function.Supplier;

import javax.measure.Quantity;

import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.meterreader.MeterReaderBindingConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author MatthiasS
 *
 */
public enum Conformity {

    NONE {
        @Override
        public <Q extends Quantity<Q>> State apply(Channel channel, QuantityType<Q> currentState, Thing thing,
                MeterDevice<?> device) {

            return retrieveOverwrittenNegate(channel, currentState, thing, device, null);
        }
    },
    /**
     * See
     * https://www.vde.com/resource/blob/951000/252eb3cdf1c7f6cdea10847be399da0d/fnn-lastenheft-edl-1-0-2010-01-13-data.pdf
     */
    EDL_FNN {
        /*
         * (non-Javadoc)
         *
         * @see org.openhab.binding.meterreader.internal.Conformity#apply(org.eclipse.smarthome.core.thing.Channel,
         * org.eclipse.smarthome.core.library.types.QuantityType, org.eclipse.smarthome.core.thing.Thing,
         * org.openhab.binding.meterreader.internal.MeterDevice)
         */
        @Override
        public <Q extends Quantity<Q>> QuantityType<?> apply(Channel channel, QuantityType<Q> currentState, Thing thing,
                MeterDevice<?> device) {
            try {
                return retrieveOverwrittenNegate(channel, currentState, thing, device, () -> {
                    // Negate if this channel has the unit "Watt" and the negate bit is set. Read from all other
                    // channels the state and check if there is a negate bit.
                    String channelObis = channel.getProperties().get(MeterReaderBindingConstants.CHANNEL_PROPERTY_OBIS);
                    MeterValue<?> value = device.getSmlValue(channelObis);
                    if (value != null && SmartHomeUnits.WATT.isCompatible(value.getUnit())) {

                        for (String obis : device.getObisCodes()) {
                            try {
                                MeterValue<?> otherValue = device.getSmlValue(obis);
                                ObisCode obisCode = ObisCode.from(obis);
                                if (obisCode.matches((byte) 0x60, (byte) 0x05, (byte) 0x05)) {

                                    // we found status status obis 96.5.5
                                    if (NegateHandler.isNegateSet(otherValue.getValue(), 5)) {
                                        return currentState.negate();
                                    }
                                } else if (obisCode.matches((byte) 0x01, (byte) 0x08, (byte) 0x00)) {

                                    // check obis 1.8.0 for status if status has negate bit set.
                                    String status = otherValue.getStatus();
                                    if (status != null && NegateHandler.isNegateSet(status, 5)) {
                                        return currentState.negate();
                                    }
                                }
                            } catch (Exception e) {
                                logger.warn("Failed to check negate status for obis {}", obis, e);
                            }

                        }
                    }
                    return currentState;
                });

            } catch (Exception e) {
                logger.error("Failed to apply negation for channel: {}", channel.getUID(), e);
            }
            return currentState;
        }
    };

    private final static Logger logger = LoggerFactory.getLogger(Conformity.class);

    /**
     * Applies the overwritten negation setting for the channel.
     *
     * @param currentState
     * @param thing
     * @param device
     * @param negateProperty
     * @return
     */
    private static <Q extends Quantity<Q>> QuantityType<Q> applyNegation(QuantityType<Q> currentState, Thing thing,
            MeterDevice<?> device, String negateProperty) {
        boolean shouldNegateState = NegateHandler.shouldNegateState(negateProperty, channelId -> {
            Channel negateChannel = thing.getChannel(channelId);
            if (negateChannel != null) {

                return device.getSmlValue(
                        negateChannel.getProperties().get(MeterReaderBindingConstants.CHANNEL_PROPERTY_OBIS));
            }
            return null;
        });

        if (shouldNegateState) {
            return currentState.negate();
        }
        return currentState;
    }

    /**
     *
     * @param channel
     * @param currentState
     * @param thing
     * @param device
     * @param elseDo If negate property was not overwritten call the given supplier.
     * @return
     */
    protected <Q extends Quantity<Q>> QuantityType<?> retrieveOverwrittenNegate(Channel channel,
            QuantityType<Q> currentState, Thing thing, MeterDevice<?> device, Supplier<QuantityType<Q>> elseDo) {
        // Negate setting
        String negateProperty = (String) channel.getConfiguration()
                .get(MeterReaderBindingConstants.CONFIGURATION_CHANNEL_NEGATE);

        if (negateProperty != null && !negateProperty.trim().isEmpty()) {

            return applyNegation(currentState, thing, device, negateProperty);
        } else {
            if (elseDo != null) {
                return elseDo.get();
            }
            return currentState;
        }
    }

    /**
     * Applies any changes according to the conformity and returns the new value.
     *
     * @param channel
     * @param currentState
     * @param thing
     * @param device
     * @return
     */
    public abstract <Q extends Quantity<Q>> State apply(Channel channel, QuantityType<Q> currentState, Thing thing,
            MeterDevice<?> device);
}
