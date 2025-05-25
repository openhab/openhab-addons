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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.matter.internal.client.dto.cluster.ClusterCommand;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.FanControlCluster;
import org.openhab.binding.matter.internal.client.dto.ws.AttributeChangedMessage;
import org.openhab.binding.matter.internal.handler.MatterBaseThingHandler;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelGroupUID;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.types.Command;
import org.openhab.core.types.StateDescription;
import org.openhab.core.types.StateDescriptionFragmentBuilder;
import org.openhab.core.types.StateOption;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A converter for translating {@link FanControlCluster} events and attributes to openHAB channels and back again.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class FanControlConverter extends GenericConverter<FanControlCluster> {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public FanControlConverter(FanControlCluster cluster, MatterBaseThingHandler handler, int endpointNumber,
            String labelPrefix) {
        super(cluster, handler, endpointNumber, labelPrefix);
    }

    @Override
    public Map<Channel, @Nullable StateDescription> createChannels(ChannelGroupUID channelGroupUID) {
        Map<Channel, @Nullable StateDescription> channels = new HashMap<>();
        Channel percentChannel = ChannelBuilder
                .create(new ChannelUID(channelGroupUID, CHANNEL_ID_FANCONTROL_PERCENT), CoreItemFactory.DIMMER)
                .withType(CHANNEL_FANCONTROL_PERCENT).build();
        channels.put(percentChannel, null);

        if (initializingCluster.fanModeSequence != null) {
            Channel modeChannel = ChannelBuilder
                    .create(new ChannelUID(channelGroupUID, CHANNEL_ID_FANCONTROL_MODE), CoreItemFactory.NUMBER)
                    .withType(CHANNEL_FANCONTROL_MODE).build();

            List<StateOption> modeOptions = new ArrayList<>();

            modeOptions.add(new StateOption(FanControlCluster.FanModeEnum.OFF.value.toString(),
                    FanControlCluster.FanModeEnum.OFF.label));

            switch (initializingCluster.fanModeSequence) {
                case OFF_HIGH:
                    modeOptions.add(new StateOption(FanControlCluster.FanModeEnum.HIGH.value.toString(),
                            FanControlCluster.FanModeEnum.HIGH.label));
                    break;
                case OFF_HIGH_AUTO:
                    modeOptions.add(new StateOption(FanControlCluster.FanModeEnum.HIGH.value.toString(),
                            FanControlCluster.FanModeEnum.HIGH.label));
                    modeOptions.add(new StateOption(FanControlCluster.FanModeEnum.AUTO.value.toString(),
                            FanControlCluster.FanModeEnum.AUTO.label));
                    break;
                case OFF_LOW_HIGH_AUTO:
                    modeOptions.add(new StateOption(FanControlCluster.FanModeEnum.LOW.value.toString(),
                            FanControlCluster.FanModeEnum.LOW.label));
                    modeOptions.add(new StateOption(FanControlCluster.FanModeEnum.HIGH.value.toString(),
                            FanControlCluster.FanModeEnum.HIGH.label));
                    modeOptions.add(new StateOption(FanControlCluster.FanModeEnum.AUTO.value.toString(),
                            FanControlCluster.FanModeEnum.AUTO.label));
                    break;
                case OFF_LOW_MED_HIGH_AUTO:
                    modeOptions.add(new StateOption(FanControlCluster.FanModeEnum.LOW.value.toString(),
                            FanControlCluster.FanModeEnum.LOW.label));
                    modeOptions.add(new StateOption(FanControlCluster.FanModeEnum.MEDIUM.value.toString(),
                            FanControlCluster.FanModeEnum.MEDIUM.label));
                    modeOptions.add(new StateOption(FanControlCluster.FanModeEnum.HIGH.value.toString(),
                            FanControlCluster.FanModeEnum.HIGH.label));
                    modeOptions.add(new StateOption(FanControlCluster.FanModeEnum.AUTO.value.toString(),
                            FanControlCluster.FanModeEnum.AUTO.label));

                    break;
                case OFF_LOW_HIGH:
                    modeOptions.add(new StateOption(FanControlCluster.FanModeEnum.LOW.value.toString(),
                            FanControlCluster.FanModeEnum.LOW.label));
                    modeOptions.add(new StateOption(FanControlCluster.FanModeEnum.HIGH.value.toString(),
                            FanControlCluster.FanModeEnum.HIGH.label));
                    break;
                default:
                    break;
            }

            StateDescription stateDescriptionMode = StateDescriptionFragmentBuilder.create().withPattern("%d")
                    .withOptions(modeOptions).build().toStateDescription();

            channels.put(modeChannel, stateDescriptionMode);
        }
        return channels;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (channelUID.getIdWithoutGroup().equals(CHANNEL_ID_FANCONTROL_PERCENT)) {
            if (command instanceof IncreaseDecreaseType increaseDecreaseType) {
                switch (increaseDecreaseType) {
                    case INCREASE:
                        moveCommand(FanControlCluster.step(FanControlCluster.StepDirectionEnum.INCREASE, false, false));
                        break;
                    case DECREASE:
                        moveCommand(FanControlCluster.step(FanControlCluster.StepDirectionEnum.DECREASE, false, true));
                        break;
                    default:
                        break;
                }
            } else if (command instanceof PercentType percentType) {
                handler.writeAttribute(endpointNumber, FanControlCluster.CLUSTER_NAME,
                        FanControlCluster.ATTRIBUTE_PERCENT_SETTING, percentType.toString());
            }
        }
        if (channelUID.getIdWithoutGroup().equals(CHANNEL_ID_FANCONTROL_MODE)) {
            if (command instanceof DecimalType decimalType) {
                handler.writeAttribute(endpointNumber, FanControlCluster.CLUSTER_NAME,
                        FanControlCluster.ATTRIBUTE_FAN_MODE, decimalType.toString());
            }
        }
        super.handleCommand(channelUID, command);
    }

    @Override
    public void onEvent(AttributeChangedMessage message) {
        switch (message.path.attributeName) {
            case FanControlCluster.ATTRIBUTE_FAN_MODE:
                if (message.value instanceof FanControlCluster.FanModeEnum fanMode) {
                    updateState(CHANNEL_ID_FANCONTROL_MODE, new DecimalType(fanMode.value));
                }
                break;
            case FanControlCluster.ATTRIBUTE_PERCENT_SETTING:
                if (message.value instanceof Number percentSetting) {
                    updateState(CHANNEL_ID_FANCONTROL_PERCENT, new PercentType(percentSetting.intValue()));
                }
                break;
            default:
                logger.debug("Unknown attribute {}", message.path.attributeName);
        }
        super.onEvent(message);
    }

    @Override
    public void initState() {
        updateState(CHANNEL_ID_FANCONTROL_MODE,
                initializingCluster.fanMode != null ? new DecimalType(initializingCluster.fanMode.value)
                        : UnDefType.NULL);
        updateState(CHANNEL_ID_FANCONTROL_PERCENT,
                initializingCluster.percentSetting != null ? new PercentType(initializingCluster.percentSetting)
                        : UnDefType.NULL);
    }

    private void moveCommand(ClusterCommand command) {
        handler.sendClusterCommand(endpointNumber, FanControlCluster.CLUSTER_NAME, command);
    }
}
