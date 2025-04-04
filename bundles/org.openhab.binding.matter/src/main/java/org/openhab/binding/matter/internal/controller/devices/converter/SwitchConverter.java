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

import static org.openhab.binding.matter.internal.MatterBindingConstants.CHANNEL_ID_SWITCH_SWITCH;
import static org.openhab.binding.matter.internal.MatterBindingConstants.CHANNEL_LABEL_SWITCH_INITIALPRESS;
import static org.openhab.binding.matter.internal.MatterBindingConstants.CHANNEL_LABEL_SWITCH_LONGPRESS;
import static org.openhab.binding.matter.internal.MatterBindingConstants.CHANNEL_LABEL_SWITCH_LONGRELEASE;
import static org.openhab.binding.matter.internal.MatterBindingConstants.CHANNEL_LABEL_SWITCH_MULTIPRESSCOMPLETE;
import static org.openhab.binding.matter.internal.MatterBindingConstants.CHANNEL_LABEL_SWITCH_MULTIPRESSONGOING;
import static org.openhab.binding.matter.internal.MatterBindingConstants.CHANNEL_LABEL_SWITCH_SHORTRELEASE;
import static org.openhab.binding.matter.internal.MatterBindingConstants.CHANNEL_LABEL_SWITCH_SWITCH;
import static org.openhab.binding.matter.internal.MatterBindingConstants.CHANNEL_LABEL_SWITCH_SWITCHLATECHED;
import static org.openhab.binding.matter.internal.MatterBindingConstants.CHANNEL_SWITCH_INITIALPRESS;
import static org.openhab.binding.matter.internal.MatterBindingConstants.CHANNEL_SWITCH_LONGPRESS;
import static org.openhab.binding.matter.internal.MatterBindingConstants.CHANNEL_SWITCH_LONGRELEASE;
import static org.openhab.binding.matter.internal.MatterBindingConstants.CHANNEL_SWITCH_MULTIPRESSCOMPLETE;
import static org.openhab.binding.matter.internal.MatterBindingConstants.CHANNEL_SWITCH_MULTIPRESSONGOING;
import static org.openhab.binding.matter.internal.MatterBindingConstants.CHANNEL_SWITCH_SHORTRELEASE;
import static org.openhab.binding.matter.internal.MatterBindingConstants.CHANNEL_SWITCH_SWITCH;
import static org.openhab.binding.matter.internal.MatterBindingConstants.CHANNEL_SWITCH_SWITCHLATECHED;
import static org.openhab.binding.matter.internal.MatterBindingConstants.ITEM_TYPE_NUMBER;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.SwitchCluster;
import org.openhab.binding.matter.internal.client.dto.ws.AttributeChangedMessage;
import org.openhab.binding.matter.internal.client.dto.ws.EventTriggeredMessage;
import org.openhab.binding.matter.internal.client.dto.ws.TriggerEvent;
import org.openhab.binding.matter.internal.handler.MatterBaseThingHandler;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelGroupUID;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.type.ChannelKind;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.StateDescription;
import org.openhab.core.types.StateDescriptionFragmentBuilder;
import org.openhab.core.types.StateOption;

import com.google.gson.Gson;

/**
 * The {@link SwitchConverter}
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class SwitchConverter extends GenericConverter<SwitchCluster> {
    private Gson gson = new Gson();

    public SwitchConverter(SwitchCluster cluster, MatterBaseThingHandler handler, int endpointNumber,
            String labelPrefix) {
        super(cluster, handler, endpointNumber, labelPrefix);
    }

    @Override
    public Map<Channel, @Nullable StateDescription> createChannels(ChannelGroupUID thingUID) {
        final Map<Channel, @Nullable StateDescription> map = new HashMap<>();
        Map<ChannelTypeUID, String> triggerChannels = new HashMap<>();
        // See cluster specification table 1.13.4. Switch Features
        if (initializingCluster.featureMap.latchingSwitch) {
            triggerChannels.put(CHANNEL_SWITCH_SWITCHLATECHED, CHANNEL_LABEL_SWITCH_SWITCHLATECHED);
        }
        if (initializingCluster.featureMap.momentarySwitch) {
            triggerChannels.put(CHANNEL_SWITCH_INITIALPRESS, CHANNEL_LABEL_SWITCH_INITIALPRESS);
        }
        if (initializingCluster.featureMap.momentarySwitchRelease) {
            triggerChannels.put(CHANNEL_SWITCH_SHORTRELEASE, CHANNEL_LABEL_SWITCH_SHORTRELEASE);

        }
        if (initializingCluster.featureMap.momentarySwitchLongPress) {
            triggerChannels.put(CHANNEL_SWITCH_LONGPRESS, CHANNEL_LABEL_SWITCH_LONGPRESS);
            triggerChannels.put(CHANNEL_SWITCH_LONGRELEASE, CHANNEL_LABEL_SWITCH_LONGRELEASE);

        }
        if (initializingCluster.featureMap.momentarySwitchMultiPress) {
            triggerChannels.put(CHANNEL_SWITCH_MULTIPRESSCOMPLETE, CHANNEL_LABEL_SWITCH_MULTIPRESSCOMPLETE);
            triggerChannels.put(CHANNEL_SWITCH_MULTIPRESSONGOING, CHANNEL_LABEL_SWITCH_MULTIPRESSONGOING);
        }
        triggerChannels
                .forEach((type,
                        label) -> map.put(ChannelBuilder.create(new ChannelUID(thingUID, type.getId()), null)
                                .withType(type).withLabel(formatLabel(label)).withKind(ChannelKind.TRIGGER).build(),
                                null));

        Channel channel = ChannelBuilder.create(new ChannelUID(thingUID, CHANNEL_ID_SWITCH_SWITCH), ITEM_TYPE_NUMBER)
                .withType(CHANNEL_SWITCH_SWITCH).withLabel(formatLabel(CHANNEL_LABEL_SWITCH_SWITCH)).build();

        List<StateOption> options = new ArrayList<>();
        for (int i = 0; i < initializingCluster.numberOfPositions; i++) {
            options.add(new StateOption(String.valueOf(i), "Position " + i));
        }

        StateDescription stateDescriptionMode = StateDescriptionFragmentBuilder.create().withPattern("%d")
                .withOptions(options).build().toStateDescription();

        map.put(channel, stateDescriptionMode);

        return map;
    }

    @Override
    public void onEvent(AttributeChangedMessage message) {
        Integer numberValue = message.value instanceof Number number ? number.intValue() : 0;
        switch (message.path.attributeName) {
            case SwitchCluster.ATTRIBUTE_CURRENT_POSITION:
                initializingCluster.currentPosition = numberValue;
                updateState(CHANNEL_ID_SWITCH_SWITCH, new DecimalType(numberValue));
                break;
        }
        super.onEvent(message);
    }

    @Override
    public void onEvent(EventTriggeredMessage message) {
        String eventName = message.path.eventName.toLowerCase();
        for (TriggerEvent event : message.events) {
            triggerChannel("switch-" + eventName, gson.toJson(event.data));
        }
    }

    @Override
    public void initState() {
        updateState(CHANNEL_ID_SWITCH_SWITCH, new DecimalType(initializingCluster.currentPosition));
    }
}
