/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.miio.internal;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.core.events.EventPublisher;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.i18n.ChannelTypeI18nLocalizationService;
import org.openhab.core.thing.link.ItemChannelLinkRegistry;
import org.openhab.core.types.StateDescription;
import org.openhab.core.types.StateOption;

/**
 * Test case for {@link MiIoStateDescriptionProvider}
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@NonNullByDefault
public class MiIoStateDescriptionProviderTest {

    private @Mock @NonNullByDefault({}) EventPublisher eventPublisher;
    private @Mock @NonNullByDefault({}) ItemChannelLinkRegistry itemChannelLinkRegistry;
    private @Mock @NonNullByDefault({}) ChannelTypeI18nLocalizationService channelTypeI18nLocalizationService;

    @Test
    public void testRemoveDescriptionsForThing() {
        MiIoStateDescriptionProvider provider = new MiIoStateDescriptionProvider(eventPublisher,
                itemChannelLinkRegistry, channelTypeI18nLocalizationService);
        ThingUID removedThing = new ThingUID(MiIoBindingConstants.THING_TYPE_VACUUM, "removed");
        ThingUID otherThing = new ThingUID(MiIoBindingConstants.THING_TYPE_VACUUM, "other");
        Channel removedChannel = channel(removedThing);
        Channel otherChannel = channel(otherThing);
        List<StateOption> options = List.of(new StateOption("0", "1NP"));
        provider.setStateOptions(removedChannel.getUID(), options);
        provider.setStateOptions(otherChannel.getUID(), options);

        provider.removeDescriptionsForThing(removedThing);

        assertNull(provider.getStateDescription(removedChannel, null, null));
        StateDescription otherDescription = provider.getStateDescription(otherChannel, null, null);
        assertNotNull(otherDescription);
        assertEquals(options, otherDescription.getOptions());
    }

    private static Channel channel(ThingUID thingUID) {
        return ChannelBuilder.create(new ChannelUID(thingUID, "actions", "current_map"), CoreItemFactory.NUMBER)
                .build();
    }
}
