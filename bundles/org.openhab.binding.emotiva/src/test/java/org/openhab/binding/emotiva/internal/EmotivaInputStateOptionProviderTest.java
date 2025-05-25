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
package org.openhab.binding.emotiva.internal;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.BINDING_ID;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.THING_PROCESSOR;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaControlCommands.coax1;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaControlCommands.hdmi1;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaControlCommands.source_1;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaControlCommands.source_2;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaSubscriptionTags.source;

import java.util.EnumMap;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.emotiva.internal.protocol.EmotivaControlCommands;
import org.openhab.core.events.EventPublisher;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelGroupUID;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.i18n.ChannelTypeI18nLocalizationService;
import org.openhab.core.thing.link.ItemChannelLinkRegistry;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.StateOption;

/**
 * Unit tests for {@link EmotivaInputStateOptionProvider}.
 *
 * @author Espen Fossen - Initial contribution
 */
@NonNullByDefault
class EmotivaInputStateOptionProviderTest {

    @Test
    void initialMainZoneSourcesToStateOptionsWithCustomLabels() {
        EnumMap<EmotivaControlCommands, String> mainZoneSourcesMap = new EnumMap<>(EmotivaControlCommands.class);
        mainZoneSourcesMap.put(source_1, "Input 1");
        mainZoneSourcesMap.put(source_2, "SHIELD");
        mainZoneSourcesMap.put(hdmi1, "HDMI 1");
        mainZoneSourcesMap.put(coax1, "Coax 1");
        EmotivaProcessorState state = new EmotivaProcessorState();
        state.setSourcesMainZone(mainZoneSourcesMap);

        EmotivaInputStateOptionProvider provider = new EmotivaInputStateOptionProvider(mock(EventPublisher.class),
                mock(ItemChannelLinkRegistry.class), mock(ChannelTypeI18nLocalizationService.class));

        ThingUID thingUID = new ThingUID(THING_PROCESSOR, "XMC-2");
        ChannelUID channelUID = new ChannelUID(new ChannelGroupUID(thingUID, "main-zone"), source.name());
        Channel channel = ChannelBuilder.create(channelUID).withType(new ChannelTypeUID(BINDING_ID, source.name()))
                .build();

        List<StateOption> stateOptions = provider.gatherStateOptionsForSource(channel, mainZoneSourcesMap);
        assertThat(stateOptions, not(nullValue()));
        assertThat(stateOptions.size(), is(4));
        assertThat(stateOptions.get(0).getValue(), is(source_1.name()));
        assertThat(stateOptions.get(0).getLabel(), is("Input 1"));
        assertThat(stateOptions.get(1).getValue(), is(source_2.name()));
        assertThat(stateOptions.get(1).getLabel(), is("SHIELD")); // Overridden by user
        assertThat(stateOptions.get(2).getValue(), is(hdmi1.name()));
        assertThat(stateOptions.get(2).getLabel(), is("HDMI 1"));
        assertThat(stateOptions.get(3).getValue(), is(coax1.name()));
        assertThat(stateOptions.get(3).getLabel(), is("Coax 1"));
    }

    @Test
    void updatedMaineZoneSourcesToStateOptionsWithCustomLabels() {
        EnumMap<EmotivaControlCommands, String> mainZoneSourcesMap = new EnumMap<>(EmotivaControlCommands.class);
        mainZoneSourcesMap.put(source_1, "Input 1");
        mainZoneSourcesMap.put(source_2, "SHIELD");
        mainZoneSourcesMap.put(hdmi1, "HDMI 1");
        mainZoneSourcesMap.put(coax1, "Coax 1");
        EmotivaProcessorState state = new EmotivaProcessorState();
        state.setSourcesMainZone(mainZoneSourcesMap);

        EmotivaInputStateOptionProvider provider = new EmotivaInputStateOptionProvider(mock(EventPublisher.class),
                mock(ItemChannelLinkRegistry.class), mock(ChannelTypeI18nLocalizationService.class));

        ThingUID thingUID = new ThingUID(THING_PROCESSOR, "XMC-2");
        ChannelUID channelUID = new ChannelUID(new ChannelGroupUID(thingUID, "main-zone"), source.name());
        Channel channel = ChannelBuilder.create(channelUID).withType(new ChannelTypeUID(BINDING_ID, source.name()))
                .build();

        String sourceOneLabel = "Should be added to options";
        state.updateSourcesMainZone(EmotivaControlCommands.source_1, sourceOneLabel);
        state.updateSourcesMainZone(EmotivaControlCommands.hdmi1, "Should not be added to options");
        List<StateOption> stateOptions = provider.gatherStateOptionsForSource(channel, mainZoneSourcesMap);

        assertThat(stateOptions, not(nullValue()));
        assertThat(stateOptions.size(), is(4));
        assertThat(stateOptions.get(0), not(nullValue()));
        assertThat(stateOptions.get(0).getValue(), is(source_1.name()));
        assertThat(stateOptions.get(0).getLabel(), is(sourceOneLabel));
        assertThat(stateOptions.get(1).getValue(), is(source_2.name()));
        assertThat(stateOptions.get(1).getLabel(), is("SHIELD")); // Overridden by user
        assertThat(stateOptions.get(2).getValue(), is(hdmi1.name()));
        assertThat(stateOptions.get(2).getLabel(), is("HDMI 1"));
        assertThat(stateOptions.get(3).getValue(), is(coax1.name()));
        assertThat(stateOptions.get(3).getLabel(), is("Coax 1"));
    }
}
