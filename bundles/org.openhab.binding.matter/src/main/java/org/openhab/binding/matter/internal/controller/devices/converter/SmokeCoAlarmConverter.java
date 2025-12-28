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

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.SmokeCoAlarmCluster;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.SmokeCoAlarmCluster.AlarmStateEnum;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.SmokeCoAlarmCluster.ContaminationStateEnum;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.SmokeCoAlarmCluster.EndOfServiceEnum;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.SmokeCoAlarmCluster.ExpressedStateEnum;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.SmokeCoAlarmCluster.MuteStateEnum;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.SmokeCoAlarmCluster.SensitivityEnum;
import org.openhab.binding.matter.internal.client.dto.ws.AttributeChangedMessage;
import org.openhab.binding.matter.internal.handler.MatterBaseThingHandler;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelGroupUID;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.types.StateDescription;
import org.openhab.core.types.StateDescriptionFragmentBuilder;
import org.openhab.core.types.StateOption;
import org.openhab.core.types.UnDefType;

/**
 * A converter for translating {@link SmokeCoAlarmCluster} events and attributes to openHAB channels.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class SmokeCoAlarmConverter extends GenericConverter<SmokeCoAlarmCluster> {

    public SmokeCoAlarmConverter(SmokeCoAlarmCluster cluster, MatterBaseThingHandler handler, int endpointNumber,
            String labelPrefix) {
        super(cluster, handler, endpointNumber, labelPrefix);
    }

    @Override
    public Map<Channel, @Nullable StateDescription> createChannels(ChannelGroupUID channelGroupUID) {
        Map<Channel, @Nullable StateDescription> channels = new HashMap<>();

        // Expressed State channel is the primary alarm indicator
        Channel expressedStateChannel = ChannelBuilder
                .create(new ChannelUID(channelGroupUID, CHANNEL_ID_SMOKECOALARM_EXPRESSEDSTATE), CoreItemFactory.NUMBER)
                .withType(CHANNEL_SMOKECOALARM_EXPRESSEDSTATE).build();
        List<StateOption> expressedStateOptions = new ArrayList<>();
        for (ExpressedStateEnum e : ExpressedStateEnum.values()) {
            expressedStateOptions.add(new StateOption(e.getValue().toString(), e.getLabel()));
        }
        channels.put(expressedStateChannel, StateDescriptionFragmentBuilder.create().withReadOnly(true)
                .withOptions(expressedStateOptions).build().toStateDescription());

        if (initializingCluster.featureMap != null && initializingCluster.featureMap.smokeAlarm) {
            Channel smokeStateChannel = ChannelBuilder
                    .create(new ChannelUID(channelGroupUID, CHANNEL_ID_SMOKECOALARM_SMOKESTATE), CoreItemFactory.NUMBER)
                    .withType(CHANNEL_SMOKECOALARM_SMOKESTATE).build();
            List<StateOption> smokeStateOptions = new ArrayList<>();
            for (AlarmStateEnum e : AlarmStateEnum.values()) {
                smokeStateOptions.add(new StateOption(e.getValue().toString(), e.getLabel()));
            }
            channels.put(smokeStateChannel, StateDescriptionFragmentBuilder.create().withReadOnly(true)
                    .withOptions(smokeStateOptions).build().toStateDescription());

            if (initializingCluster.interconnectSmokeAlarm != null) {
                Channel interconnectSmokeChannel = ChannelBuilder
                        .create(new ChannelUID(channelGroupUID, CHANNEL_ID_SMOKECOALARM_INTERCONNECTSMOKE),
                                CoreItemFactory.NUMBER)
                        .withType(CHANNEL_SMOKECOALARM_INTERCONNECTSMOKE).build();
                List<StateOption> interconnectSmokeOptions = new ArrayList<>();
                for (AlarmStateEnum e : AlarmStateEnum.values()) {
                    interconnectSmokeOptions.add(new StateOption(e.getValue().toString(), e.getLabel()));
                }
                channels.put(interconnectSmokeChannel, StateDescriptionFragmentBuilder.create().withReadOnly(true)
                        .withOptions(interconnectSmokeOptions).build().toStateDescription());
            }

            if (initializingCluster.contaminationState != null) {
                Channel contaminationChannel = ChannelBuilder
                        .create(new ChannelUID(channelGroupUID, CHANNEL_ID_SMOKECOALARM_CONTAMINATIONSTATE),
                                CoreItemFactory.NUMBER)
                        .withType(CHANNEL_SMOKECOALARM_CONTAMINATIONSTATE).build();
                List<StateOption> contaminationOptions = new ArrayList<>();
                for (ContaminationStateEnum e : ContaminationStateEnum.values()) {
                    contaminationOptions.add(new StateOption(e.getValue().toString(), e.getLabel()));
                }
                channels.put(contaminationChannel, StateDescriptionFragmentBuilder.create().withReadOnly(true)
                        .withOptions(contaminationOptions).build().toStateDescription());
            }

            if (initializingCluster.smokeSensitivityLevel != null) {
                Channel sensitivityChannel = ChannelBuilder
                        .create(new ChannelUID(channelGroupUID, CHANNEL_ID_SMOKECOALARM_SMOKESENSITIVITY),
                                CoreItemFactory.NUMBER)
                        .withType(CHANNEL_SMOKECOALARM_SMOKESENSITIVITY).build();
                List<StateOption> sensitivityOptions = new ArrayList<>();
                for (SensitivityEnum e : SensitivityEnum.values()) {
                    sensitivityOptions.add(new StateOption(e.getValue().toString(), e.getLabel()));
                }
                // Sensitivity is the only read-write channel
                channels.put(sensitivityChannel, StateDescriptionFragmentBuilder.create().withReadOnly(false)
                        .withOptions(sensitivityOptions).build().toStateDescription());
            }
        }

        if (initializingCluster.featureMap != null && initializingCluster.featureMap.coAlarm) {
            Channel coStateChannel = ChannelBuilder
                    .create(new ChannelUID(channelGroupUID, CHANNEL_ID_SMOKECOALARM_COSTATE), CoreItemFactory.NUMBER)
                    .withType(CHANNEL_SMOKECOALARM_COSTATE).build();
            List<StateOption> coStateOptions = new ArrayList<>();
            for (AlarmStateEnum e : AlarmStateEnum.values()) {
                coStateOptions.add(new StateOption(e.getValue().toString(), e.getLabel()));
            }
            channels.put(coStateChannel, StateDescriptionFragmentBuilder.create().withReadOnly(true)
                    .withOptions(coStateOptions).build().toStateDescription());

            if (initializingCluster.interconnectCoAlarm != null) {
                Channel interconnectCoChannel = ChannelBuilder
                        .create(new ChannelUID(channelGroupUID, CHANNEL_ID_SMOKECOALARM_INTERCONNECTCO),
                                CoreItemFactory.NUMBER)
                        .withType(CHANNEL_SMOKECOALARM_INTERCONNECTCO).build();
                List<StateOption> interconnectCoOptions = new ArrayList<>();
                for (AlarmStateEnum e : AlarmStateEnum.values()) {
                    interconnectCoOptions.add(new StateOption(e.getValue().toString(), e.getLabel()));
                }
                channels.put(interconnectCoChannel, StateDescriptionFragmentBuilder.create().withReadOnly(true)
                        .withOptions(interconnectCoOptions).build().toStateDescription());
            }
        }

        Channel batteryAlertChannel = ChannelBuilder
                .create(new ChannelUID(channelGroupUID, CHANNEL_ID_SMOKECOALARM_BATTERYALERT), CoreItemFactory.NUMBER)
                .withType(CHANNEL_SMOKECOALARM_BATTERYALERT).build();
        List<StateOption> batteryAlertOptions = new ArrayList<>();
        for (AlarmStateEnum e : AlarmStateEnum.values()) {
            batteryAlertOptions.add(new StateOption(e.getValue().toString(), e.getLabel()));
        }
        channels.put(batteryAlertChannel, StateDescriptionFragmentBuilder.create().withReadOnly(true)
                .withOptions(batteryAlertOptions).build().toStateDescription());

        // Device Muted channel is optional, but not listed in the feature map
        if (initializingCluster.deviceMuted != null) {
            Channel deviceMutedChannel = ChannelBuilder
                    .create(new ChannelUID(channelGroupUID, CHANNEL_ID_SMOKECOALARM_DEVICEMUTED),
                            CoreItemFactory.NUMBER)
                    .withType(CHANNEL_SMOKECOALARM_DEVICEMUTED).build();
            List<StateOption> muteOptions = new ArrayList<>();
            for (MuteStateEnum e : MuteStateEnum.values()) {
                muteOptions.add(new StateOption(e.getValue().toString(), e.getLabel()));
            }
            channels.put(deviceMutedChannel, StateDescriptionFragmentBuilder.create().withReadOnly(true)
                    .withOptions(muteOptions).build().toStateDescription());
        }

        Channel testInProgressChannel = ChannelBuilder
                .create(new ChannelUID(channelGroupUID, CHANNEL_ID_SMOKECOALARM_TESTINPROGRESS), CoreItemFactory.SWITCH)
                .withType(CHANNEL_SMOKECOALARM_TESTINPROGRESS).build();
        channels.put(testInProgressChannel, null);

        Channel hardwareFaultChannel = ChannelBuilder
                .create(new ChannelUID(channelGroupUID, CHANNEL_ID_SMOKECOALARM_HARDWAREFAULT), CoreItemFactory.SWITCH)
                .withType(CHANNEL_SMOKECOALARM_HARDWAREFAULT).build();
        channels.put(hardwareFaultChannel, null);

        Channel endOfServiceChannel = ChannelBuilder
                .create(new ChannelUID(channelGroupUID, CHANNEL_ID_SMOKECOALARM_ENDOFSERVICE), CoreItemFactory.NUMBER)
                .withType(CHANNEL_SMOKECOALARM_ENDOFSERVICE).build();
        List<StateOption> endOfServiceOptions = new ArrayList<>();
        for (EndOfServiceEnum e : EndOfServiceEnum.values()) {
            endOfServiceOptions.add(new StateOption(e.getValue().toString(), e.getLabel()));
        }
        channels.put(endOfServiceChannel, StateDescriptionFragmentBuilder.create().withReadOnly(true)
                .withOptions(endOfServiceOptions).build().toStateDescription());

        // Expiry Date channel is optional, but not listed in the feature map
        if (initializingCluster.expiryDate != null) {
            Channel expiryDateChannel = ChannelBuilder
                    .create(new ChannelUID(channelGroupUID, CHANNEL_ID_SMOKECOALARM_EXPIRYDATE),
                            CoreItemFactory.DATETIME)
                    .withType(CHANNEL_SMOKECOALARM_EXPIRYDATE).build();
            channels.put(expiryDateChannel, null);
        }

        return channels;
    }

    @Override
    public void onEvent(AttributeChangedMessage message) {
        switch (message.path.attributeName) {
            case SmokeCoAlarmCluster.ATTRIBUTE_EXPRESSED_STATE:
                if (message.value instanceof ExpressedStateEnum expressedState) {
                    updateState(CHANNEL_ID_SMOKECOALARM_EXPRESSEDSTATE, new DecimalType(expressedState.getValue()));
                }
                break;
            case SmokeCoAlarmCluster.ATTRIBUTE_SMOKE_STATE:
                if (message.value instanceof AlarmStateEnum smokeState) {
                    updateState(CHANNEL_ID_SMOKECOALARM_SMOKESTATE, new DecimalType(smokeState.getValue()));
                }
                break;
            case SmokeCoAlarmCluster.ATTRIBUTE_CO_STATE:
                if (message.value instanceof AlarmStateEnum coState) {
                    updateState(CHANNEL_ID_SMOKECOALARM_COSTATE, new DecimalType(coState.getValue()));
                }
                break;
            case SmokeCoAlarmCluster.ATTRIBUTE_BATTERY_ALERT:
                if (message.value instanceof AlarmStateEnum batteryAlert) {
                    updateState(CHANNEL_ID_SMOKECOALARM_BATTERYALERT, new DecimalType(batteryAlert.getValue()));
                }
                break;
            case SmokeCoAlarmCluster.ATTRIBUTE_DEVICE_MUTED:
                if (message.value instanceof MuteStateEnum deviceMuted) {
                    updateState(CHANNEL_ID_SMOKECOALARM_DEVICEMUTED, new DecimalType(deviceMuted.getValue()));
                }
                break;
            case SmokeCoAlarmCluster.ATTRIBUTE_TEST_IN_PROGRESS:
                if (message.value instanceof Boolean testInProgress) {
                    updateState(CHANNEL_ID_SMOKECOALARM_TESTINPROGRESS, OnOffType.from(testInProgress));
                }
                break;
            case SmokeCoAlarmCluster.ATTRIBUTE_HARDWARE_FAULT_ALERT:
                if (message.value instanceof Boolean hardwareFault) {
                    updateState(CHANNEL_ID_SMOKECOALARM_HARDWAREFAULT, OnOffType.from(hardwareFault));
                }
                break;
            case SmokeCoAlarmCluster.ATTRIBUTE_END_OF_SERVICE_ALERT:
                if (message.value instanceof EndOfServiceEnum endOfService) {
                    updateState(CHANNEL_ID_SMOKECOALARM_ENDOFSERVICE, new DecimalType(endOfService.getValue()));
                }
                break;
            case SmokeCoAlarmCluster.ATTRIBUTE_INTERCONNECT_SMOKE_ALARM:
                if (message.value instanceof AlarmStateEnum interconnectSmoke) {
                    updateState(CHANNEL_ID_SMOKECOALARM_INTERCONNECTSMOKE,
                            new DecimalType(interconnectSmoke.getValue()));
                }
                break;
            case SmokeCoAlarmCluster.ATTRIBUTE_INTERCONNECT_CO_ALARM:
                if (message.value instanceof AlarmStateEnum interconnectCo) {
                    updateState(CHANNEL_ID_SMOKECOALARM_INTERCONNECTCO, new DecimalType(interconnectCo.getValue()));
                }
                break;
            case SmokeCoAlarmCluster.ATTRIBUTE_CONTAMINATION_STATE:
                if (message.value instanceof ContaminationStateEnum contamination) {
                    updateState(CHANNEL_ID_SMOKECOALARM_CONTAMINATIONSTATE, new DecimalType(contamination.getValue()));
                }
                break;
            case SmokeCoAlarmCluster.ATTRIBUTE_SMOKE_SENSITIVITY_LEVEL:
                if (message.value instanceof SensitivityEnum sensitivity) {
                    updateState(CHANNEL_ID_SMOKECOALARM_SMOKESENSITIVITY, new DecimalType(sensitivity.getValue()));
                }
                break;
            case SmokeCoAlarmCluster.ATTRIBUTE_EXPIRY_DATE:
                if (message.value instanceof Number expiryDate) {
                    updateState(CHANNEL_ID_SMOKECOALARM_EXPIRYDATE, epochToDateTime(expiryDate.longValue()));
                }
                break;
            default:
                break;
        }
        super.onEvent(message);
    }

    @Override
    public void initState() {
        updateState(CHANNEL_ID_SMOKECOALARM_EXPRESSEDSTATE,
                initializingCluster.expressedState != null
                        ? new DecimalType(initializingCluster.expressedState.getValue())
                        : UnDefType.NULL);

        updateState(CHANNEL_ID_SMOKECOALARM_SMOKESTATE,
                initializingCluster.smokeState != null ? new DecimalType(initializingCluster.smokeState.getValue())
                        : UnDefType.NULL);

        updateState(CHANNEL_ID_SMOKECOALARM_COSTATE,
                initializingCluster.coState != null ? new DecimalType(initializingCluster.coState.getValue())
                        : UnDefType.NULL);

        updateState(CHANNEL_ID_SMOKECOALARM_BATTERYALERT,
                initializingCluster.batteryAlert != null ? new DecimalType(initializingCluster.batteryAlert.getValue())
                        : UnDefType.NULL);

        updateState(CHANNEL_ID_SMOKECOALARM_DEVICEMUTED,
                initializingCluster.deviceMuted != null ? new DecimalType(initializingCluster.deviceMuted.getValue())
                        : UnDefType.NULL);

        updateState(CHANNEL_ID_SMOKECOALARM_TESTINPROGRESS,
                initializingCluster.testInProgress != null ? OnOffType.from(initializingCluster.testInProgress)
                        : UnDefType.NULL);

        updateState(CHANNEL_ID_SMOKECOALARM_HARDWAREFAULT,
                initializingCluster.hardwareFaultAlert != null ? OnOffType.from(initializingCluster.hardwareFaultAlert)
                        : UnDefType.NULL);

        updateState(CHANNEL_ID_SMOKECOALARM_ENDOFSERVICE,
                initializingCluster.endOfServiceAlert != null
                        ? new DecimalType(initializingCluster.endOfServiceAlert.getValue())
                        : UnDefType.NULL);

        updateState(CHANNEL_ID_SMOKECOALARM_INTERCONNECTSMOKE,
                initializingCluster.interconnectSmokeAlarm != null
                        ? new DecimalType(initializingCluster.interconnectSmokeAlarm.getValue())
                        : UnDefType.NULL);

        updateState(CHANNEL_ID_SMOKECOALARM_INTERCONNECTCO,
                initializingCluster.interconnectCoAlarm != null
                        ? new DecimalType(initializingCluster.interconnectCoAlarm.getValue())
                        : UnDefType.NULL);

        updateState(CHANNEL_ID_SMOKECOALARM_CONTAMINATIONSTATE,
                initializingCluster.contaminationState != null
                        ? new DecimalType(initializingCluster.contaminationState.getValue())
                        : UnDefType.NULL);

        updateState(CHANNEL_ID_SMOKECOALARM_SMOKESENSITIVITY,
                initializingCluster.smokeSensitivityLevel != null
                        ? new DecimalType(initializingCluster.smokeSensitivityLevel.getValue())
                        : UnDefType.NULL);

        updateState(CHANNEL_ID_SMOKECOALARM_EXPIRYDATE,
                initializingCluster.expiryDate != null ? epochToDateTime(initializingCluster.expiryDate.longValue())
                        : UnDefType.NULL);
    }

    private DateTimeType epochToDateTime(long epochSeconds) {
        ZonedDateTime zdt = ZonedDateTime.ofInstant(Instant.ofEpochSecond(epochSeconds), ZoneId.systemDefault());
        return new DateTimeType(zdt);
    }
}
