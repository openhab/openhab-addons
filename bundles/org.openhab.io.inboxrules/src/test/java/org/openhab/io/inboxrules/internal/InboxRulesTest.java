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
package org.openhab.io.inboxrules.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.builder.ThingBuilder;

/**
 * Tests for {@link InboxRules}.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
@SuppressWarnings("null")
class InboxRulesTest {
    private static final ThingTypeUID THING_TYPE_UID = new ThingTypeUID("demo", "thing");
    private static final ThingUID THING_UID = new ThingUID(THING_TYPE_UID, "device");
    private static final ThingUID BRIDGE_UID = new ThingUID("demo", "bridge", "main");

    @Test
    void testMatchesConfiguredCriteriaAndIgnoresComments() {
        InboxRules rules = new InboxRules();
        rules.modified(Map.of("rules",
                "# ignored comment\n// another comment\nthingUid=demo:thing:*; bridgeUid=demo:bridge:main; label=Door ?ensor; representationProperty=ABC-*; propertyMatch=model=WX-*,firmware:1.?; configMatch=mode=discovery"));

        assertTrue(rules.test(discoveryResult(
                Map.of("serialNumber", "ABC-123", "model", "WX-2", "firmware", "1.4", "mode", "discovery"))));
        assertFalse(rules.test(discoveryResult(
                Map.of("serialNumber", "ABC-123", "model", "WX-2", "firmware", "1.4", "mode", "manual"))));
    }

    @Test
    void testRulesCanBeDisabledGlobally() {
        ThingRegistry registry = mock(ThingRegistry.class);
        InboxRules rules = new InboxRules();
        rules.setThingRegistry(registry);
        rules.modified(Map.of("enabled", false, "rules", "thingUid=demo:thing:device; setLabel=Approved"));
        Thing thing = ThingBuilder.create(THING_TYPE_UID, THING_UID).build();

        assertFalse(rules.test(discoveryResult(Map.of("serialNumber", "ABC-123"))));
        rules.added(thing);

        verifyNoInteractions(registry);
    }

    @Test
    void testAppliesActionsWhenThingIsAdded() {
        ThingRegistry registry = mock(ThingRegistry.class);
        InboxRules rules = new InboxRules();
        rules.setThingRegistry(registry);
        rules.modified(Map.of("rules",
                "thingUid=demo:thing:device; setLabel=${label} (approved); setLocation=${properties.room}; setProperties=source=${properties.serialNumber}; setConfig=mode=automatic"));
        DiscoveryResult result = discoveryResult(Map.of("serialNumber", "ABC-123", "room", "Kitchen"));
        Thing approvedThing = ThingBuilder.create(THING_TYPE_UID, THING_UID).withLabel(result.getLabel())
                .withProperties(Map.of("serialNumber", "ABC-123"))
                .withConfiguration(new Configuration(Map.of("old", true))).build();
        when(registry.get(THING_UID)).thenReturn(approvedThing);

        assertTrue(rules.test(result));
        rules.added(approvedThing);

        ArgumentCaptor<Thing> updatedThing = ArgumentCaptor.forClass(Thing.class);
        verify(registry, timeout(2_000)).update(updatedThing.capture());
        assertEquals("Door Sensor (approved)", updatedThing.getValue().getLabel());
        assertEquals("Kitchen", updatedThing.getValue().getLocation());
        assertEquals("ABC-123", updatedThing.getValue().getProperties().get("source"));
        assertEquals(true, updatedThing.getValue().getConfiguration().get("old"));
        assertEquals("automatic", updatedThing.getValue().getConfiguration().get("mode"));
    }

    private DiscoveryResult discoveryResult(Map<String, Object> properties) {
        return DiscoveryResultBuilder.create(THING_UID).withThingType(THING_TYPE_UID).withBridge(BRIDGE_UID)
                .withLabel("Door Sensor").withRepresentationProperty("serialNumber").withProperties(properties).build();
    }
}
