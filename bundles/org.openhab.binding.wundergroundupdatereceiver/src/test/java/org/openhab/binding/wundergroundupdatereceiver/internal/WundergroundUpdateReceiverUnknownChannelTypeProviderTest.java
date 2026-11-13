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
package org.openhab.binding.wundergroundupdatereceiver.internal;

import static org.junit.jupiter.api.Assertions.*;
import static org.openhab.binding.wundergroundupdatereceiver.internal.WundergroundUpdateReceiverBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.openhab.core.storage.StorageService;
import org.openhab.core.test.storage.VolatileStorageService;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ManagedThingProvider;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.AutoUpdatePolicy;
import org.openhab.core.thing.type.ChannelKind;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeUID;

/**
 * Tests for restoring dynamically generated channel types after a restart.
 *
 * @author Leo Siepel - Initial contribution
 */
@NonNullByDefault
class WundergroundUpdateReceiverUnknownChannelTypeProviderTest {
    private static final ThingUID THING_UID = new ThingUID(THING_TYPE_UPDATE_RECEIVER, "station");
    private static final ChannelTypeUID PRESSURE_TYPE = new ChannelTypeUID(BINDING_ID, "absbaromin");

    private final StorageService storageService = new VolatileStorageService();
    private final ManagedThingProvider managedThingProvider = new ManagedThingProvider(storageService);

    @ParameterizedTest
    @CsvSource({ "absbaromin,29.92,Number", "batteryWarning,on,Switch", "weatherDescription,Clear,String" })
    void generatedChannelTypeIsAvailableAfterProviderRestart(String parameter, String value, String itemType) {
        WundergroundUpdateReceiverUnknownChannelTypeProvider provider = createProvider();
        ChannelTypeUID uid = provider.getOrCreateChannelType(parameter, value).getUID();

        WundergroundUpdateReceiverUnknownChannelTypeProvider restartedProvider = createProvider();
        ChannelType restoredType = restartedProvider.getChannelType(uid, null);

        assertNotNull(restoredType);
        assertEquals(itemType, restoredType.getItemType());
        assertEquals(UNCATEGORIZED, restoredType.getCategory());
        assertTrue(restoredType.isAdvanced());
        assertEquals(AutoUpdatePolicy.DEFAULT, restoredType.getAutoUpdatePolicy());
        assertEquals(1, restartedProvider.getChannelTypes(null).size());
        assertEquals(itemType, restartedProvider.getOrCreateChannelType(parameter, "a different value").getItemType());
    }

    @ParameterizedTest
    @ValueSource(strings = { "Number", "Switch", "String" })
    void legacyChannelTypeIsRecoveredBeforeAnyStationUpdate(String itemType) {
        Channel channel = ChannelBuilder.create(new ChannelUID(THING_UID, UNCATEGORIZED, "absbaromin"), itemType)
                .withType(PRESSURE_TYPE).build();
        managedThingProvider
                .add(ThingBuilder.create(THING_TYPE_UPDATE_RECEIVER, THING_UID).withChannel(channel).build());

        WundergroundUpdateReceiverUnknownChannelTypeProvider provider = createProvider();
        ChannelType restoredType = provider.getChannelType(PRESSURE_TYPE, null);

        assertNotNull(restoredType);
        assertEquals(itemType, restoredType.getItemType());
        assertEquals(itemType, createProvider().getOrCreateChannelType("absbaromin", "0").getItemType());
    }

    @Test
    void aPersistedTypeIsNotOverwrittenByLegacyRecovery() {
        createProvider().getOrCreateChannelType("absbaromin", "29.92");
        Channel channel = ChannelBuilder.create(new ChannelUID(THING_UID, UNCATEGORIZED, "absbaromin"), "Switch")
                .withType(PRESSURE_TYPE).build();
        managedThingProvider
                .add(ThingBuilder.create(THING_TYPE_UPDATE_RECEIVER, THING_UID).withChannel(channel).build());

        ChannelType restoredType = createProvider().getChannelType(PRESSURE_TYPE, null);

        assertNotNull(restoredType);
        assertEquals("Number", restoredType.getItemType());
    }

    @Test
    void legacyRecoveryDoesNotProvideStaticOrForeignChannelTypes() {
        Channel known = ChannelBuilder
                .create(new ChannelUID(THING_UID, TEMPERATURE_GROUP, TEMPERATURE), "Number:Temperature")
                .withType(TEMPERATURE_CHANNELTYPEUID).build();
        Channel foreign = ChannelBuilder.create(new ChannelUID(THING_UID, UNCATEGORIZED, "foreign"), "Number")
                .withType(new ChannelTypeUID("other", "foreign")).build();
        Channel noType = ChannelBuilder.create(new ChannelUID(THING_UID, UNCATEGORIZED, "withoutType"), "Number")
                .build();
        Channel trigger = ChannelBuilder.create(new ChannelUID(THING_UID, UNCATEGORIZED, "event"))
                .withKind(ChannelKind.TRIGGER).withType(new ChannelTypeUID(BINDING_ID, "event")).build();
        managedThingProvider.add(ThingBuilder.create(THING_TYPE_UPDATE_RECEIVER, THING_UID)
                .withChannels(known, foreign, noType, trigger).build());
        ThingTypeUID otherType = new ThingTypeUID(BINDING_ID, "other");
        ThingUID otherUid = new ThingUID(otherType, "other");
        Channel otherThingChannel = ChannelBuilder
                .create(new ChannelUID(otherUid, UNCATEGORIZED, "absbaromin"), "Number").withType(PRESSURE_TYPE)
                .build();
        managedThingProvider.add(ThingBuilder.create(otherType, otherUid).withChannel(otherThingChannel).build());

        WundergroundUpdateReceiverUnknownChannelTypeProvider provider = createProvider();

        assertTrue(provider.getChannelTypes(null).isEmpty());
        assertNull(provider.getChannelType(TEMPERATURE_CHANNELTYPEUID, null));
        assertNull(provider.getChannelType(PRESSURE_TYPE, null));
    }

    private WundergroundUpdateReceiverUnknownChannelTypeProvider createProvider() {
        return new WundergroundUpdateReceiverUnknownChannelTypeProvider(storageService, managedThingProvider);
    }
}
