/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.matter.internal.controller.devices.converter;

import static org.openhab.binding.matter.internal.MatterBindingConstants.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.measure.quantity.Dimensionless;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.PowerSourceCluster;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.PowerSourceCluster.BatChargeLevelEnum;
import org.openhab.binding.matter.internal.client.dto.ws.AttributeChangedMessage;
import org.openhab.binding.matter.internal.handler.MatterBaseThingHandler;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelGroupUID;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.types.State;
import org.openhab.core.types.StateDescription;
import org.openhab.core.types.StateDescriptionFragmentBuilder;
import org.openhab.core.types.StateOption;
import org.openhab.core.types.UnDefType;

/**
 * A converter for translating {@link PowerSourceCluster} events and attributes to openHAB channels and back again.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class PowerSourceConverter extends GenericConverter<PowerSourceCluster> {

    public PowerSourceConverter(PowerSourceCluster cluster, MatterBaseThingHandler handler, int endpointNumber,
            String labelPrefix) {
        super(cluster, handler, endpointNumber, labelPrefix);
    }

    @Override
    public Map<Channel, @Nullable StateDescription> createChannels(ChannelGroupUID channelGroupUID) {
        Map<Channel, @Nullable StateDescription> channels = new HashMap<>();
        if (initializingCluster.featureMap.battery) {
            if (initializingCluster.batPercentRemaining != null) {
                Channel channel = ChannelBuilder
                        .create(new ChannelUID(channelGroupUID, CHANNEL_ID_POWER_BATTERYPERCENT),
                                "Number:Dimensionless")
                        .withType(CHANNEL_POWER_BATTERYPERCENT).build();
                channels.put(channel, null);
            }
            if (initializingCluster.batChargeLevel != null) {
                Channel channel = ChannelBuilder
                        .create(new ChannelUID(channelGroupUID, CHANNEL_ID_POWER_CHARGELEVEL), CoreItemFactory.NUMBER)
                        .withType(CHANNEL_POWER_CHARGELEVEL).build();
                List<StateOption> options = new ArrayList<>();
                for (BatChargeLevelEnum mode : BatChargeLevelEnum.values()) {
                    options.add(new StateOption(mode.value.toString(), mode.label));
                }
                StateDescription stateDescription = StateDescriptionFragmentBuilder.create().withPattern("%d")
                        .withOptions(options).build().toStateDescription();
                channels.put(channel, stateDescription);
            }
        }
        return channels;
    }

    @Override
    public void onEvent(AttributeChangedMessage message) {
        switch (message.path.attributeName) {
            case PowerSourceCluster.ATTRIBUTE_BAT_PERCENT_REMAINING:
                if (message.value instanceof Number batPercentRemaining) {
                    updateState(CHANNEL_ID_POWER_BATTERYPERCENT, convertToPercentage(batPercentRemaining.intValue()));
                }
                break;
            case PowerSourceCluster.ATTRIBUTE_BAT_CHARGE_LEVEL:
                if (message.value instanceof BatChargeLevelEnum batChargeLevel) {
                    updateState(CHANNEL_ID_POWER_CHARGELEVEL, new DecimalType(batChargeLevel.value));
                }
                break;
            default:
                logger.debug("Unknown attribute {}", message.path.attributeName);
        }
        super.onEvent(message);
    }

    @Override
    public void initState() {
        updateState(CHANNEL_ID_POWER_BATTERYPERCENT,
                initializingCluster.batPercentRemaining != null
                        ? convertToPercentage(initializingCluster.batPercentRemaining)
                        : UnDefType.NULL);
        updateState(CHANNEL_ID_POWER_CHARGELEVEL,
                initializingCluster.batChargeLevel != null ? new DecimalType(initializingCluster.batChargeLevel.value)
                        : UnDefType.NULL);
    }

    /**
     * Converts a battery charge value in half-percent units to a percentage (0-100).
     * Values are expressed in half percent units, ranging from 0 to 200.
     * For example, a value of 48 is equivalent to 24%.
     *
     * @param halfPercentValue the battery charge value in half-percent units.
     * @return the percentage of battery charge (0-100) or UnDefType.UNDEF if the value is null or invalid as a
     *         QuantityType.
     */
    private State convertToPercentage(Integer halfPercentValue) {
        if (halfPercentValue < 0 || halfPercentValue > 200) {
            return UnDefType.UNDEF; // Indicates that the node is unable to assess the value or invalid input.
        }
        return new QuantityType<Dimensionless>(halfPercentValue == 0 ? 0 : halfPercentValue / 2, Units.PERCENT);
    }
}
