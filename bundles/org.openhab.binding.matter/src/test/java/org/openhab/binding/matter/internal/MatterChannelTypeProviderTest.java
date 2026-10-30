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
package org.openhab.binding.matter.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.core.test.storage.VolatileStorageService;
import org.openhab.core.thing.type.ChannelGroupType;
import org.openhab.core.thing.type.ChannelGroupTypeBuilder;
import org.openhab.core.thing.type.ChannelGroupTypeUID;
import org.openhab.core.thing.type.ThingTypeRegistry;

/**
 * Tests for {@link MatterChannelTypeProvider}.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
class MatterChannelTypeProviderTest {
    private final VolatileStorageService storageService = new VolatileStorageService();
    private final ThingTypeRegistry thingTypeRegistry = mock(ThingTypeRegistry.class);

    @Test
    void testPrefixRemovesOnlyThatThingsGroupTypes() {
        MatterChannelTypeProvider provider = new MatterChannelTypeProvider(thingTypeRegistry, storageService);
        provider.updateChannelGroupTypesForPrefix("node-123", List.of(groupType("node-123-1")));
        provider.updateChannelGroupTypesForPrefix("node-1234", List.of(groupType("node-1234-1")));
        provider.updateChannelGroupTypesForPrefix("endpoint-123-3", List.of(groupType("endpoint-123-3-3")));

        provider.removeChannelGroupTypesForPrefix("node-123");
        assertEquals(Set.of("node-1234-1", "endpoint-123-3-3"), groupTypeIds(provider));
    }

    @Test
    void testUpdateRemovesGroupTypesOfEndpointsThatAreGone() {
        MatterChannelTypeProvider provider = new MatterChannelTypeProvider(thingTypeRegistry, storageService);
        provider.updateChannelGroupTypesForPrefix("node-123",
                List.of(groupType("node-123-1"), groupType("node-123-2")));

        provider.updateChannelGroupTypesForPrefix("node-123", List.of(groupType("node-123-1")));
        assertEquals(Set.of("node-123-1"), groupTypeIds(provider));
    }

    @Test
    void testLegacyGroupTypesAreRemovedOnStart() {
        MatterChannelTypeProvider provider = new MatterChannelTypeProvider(thingTypeRegistry, storageService);
        provider.putChannelGroupType(groupType("123-1"));
        provider.putChannelGroupType(groupType("node-123-1"));

        MatterChannelTypeProvider restarted = new MatterChannelTypeProvider(thingTypeRegistry, storageService);
        assertEquals(Set.of("node-123-1"), groupTypeIds(restarted));
    }

    private ChannelGroupType groupType(String id) {
        return ChannelGroupTypeBuilder.instance(new ChannelGroupTypeUID(MatterBindingConstants.BINDING_ID, id), id)
                .build();
    }

    private Set<String> groupTypeIds(MatterChannelTypeProvider provider) {
        return provider.getChannelGroupTypes(null).stream().map(type -> type.getUID().getId())
                .collect(Collectors.toSet());
    }
}
