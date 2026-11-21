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
package org.openhab.binding.knx.internal.projectstore;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.nio.file.Path;
import java.time.Clock;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.openhab.binding.knx.internal.monitor.ProjectLookup;
import org.openhab.binding.knx.internal.project.ProjectTestFiles;
import org.openhab.core.thing.ThingUID;

import io.calimero.GroupAddress;
import io.calimero.IndividualAddress;

/**
 * Checks what the lookup of a bridge answers from its stored project.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
class StoreLookupTest {

    private static final ThingUID BRIDGE = KnxProjectStoreTest.BRIDGE;

    @TempDir
    @Nullable
    Path tempFolder;

    private KnxProjectStore store() {
        return new KnxProjectStore(Objects.requireNonNull(tempFolder), Clock.systemUTC());
    }

    private ProjectLookup imported() throws Exception {
        KnxProjectStore store = store();
        store.importProject(BRIDGE, ProjectTestFiles.plain(), null, null);
        return store.lookup(BRIDGE);
    }

    @Test
    void groupAddressesHaveTheirNames() throws Exception {
        ProjectLookup lookup = imported();

        assertEquals("Made-up light", lookup.groupAddressName(new GroupAddress(31, 7, 1)));
        assertEquals("Made-up temperature", lookup.groupAddressName(new GroupAddress(31, 7, 2)));
        assertNull(lookup.groupAddressName(new GroupAddress(31, 7, 99)));
    }

    @Test
    void namesAreCleaned() throws Exception {
        assertEquals("Made-up second line [31m", imported().groupAddressName(new GroupAddress(31, 7, 4)));
    }

    @Test
    void theTypeIsThatOfTheChannelTextWhenTheMonitorCanDecodeIt() throws Exception {
        ProjectLookup lookup = imported();

        assertEquals("1.001", lookup.dataType(new GroupAddress(31, 7, 1)));
        assertEquals("9.001", lookup.dataType(new GroupAddress(31, 7, 2)));
        assertNull(lookup.dataType(new GroupAddress(31, 7, 3)), "15.000 cannot be decoded");
        assertEquals("1.008", lookup.dataType(new GroupAddress(31, 7, 5)));
        assertNull(lookup.dataType(new GroupAddress(31, 7, 99)));
    }

    @Test
    void devicesHaveTheirOwnNameElseTheProductText() throws Exception {
        ProjectLookup lookup = imported();

        assertEquals(ProjectTestFiles.DEVICE_NAME, lookup.deviceName(new IndividualAddress(7, 7, 1)));
        assertEquals(ProjectTestFiles.PRODUCT_TEXT, lookup.deviceName(new IndividualAddress(7, 7, 2)));
        assertNull(lookup.deviceName(new IndividualAddress(7, 7, 3)));
    }

    @Test
    void theLookupFollowsImportAndRemove() throws Exception {
        KnxProjectStore store = store();
        ProjectLookup lookup = store.lookup(BRIDGE);
        GroupAddress light = new GroupAddress(31, 7, 1);
        assertNull(lookup.groupAddressName(light));

        store.importProject(BRIDGE, ProjectTestFiles.plain(), null, null);
        assertEquals("Made-up light", lookup.groupAddressName(light));

        store.remove(BRIDGE);
        assertNull(lookup.groupAddressName(light));
        assertNull(lookup.dataType(light));
        assertNull(lookup.deviceName(new IndividualAddress(7, 7, 1)));
    }

    @Test
    void theLookupLoadsTheStoredFileAtItsFirstQuestion() throws Exception {
        store().importProject(BRIDGE, ProjectTestFiles.plain(), null, null);

        assertEquals("Made-up light", store().lookup(BRIDGE).groupAddressName(new GroupAddress(31, 7, 1)));
    }

    @Test
    void aFailureInsideTheLookupIsAnsweredWithNull() throws Exception {
        ProjectLookup lookup = imported();
        GroupAddress broken = mock(GroupAddress.class);
        when(broken.getRawAddress()).thenThrow(new IllegalStateException("made-up failure"));
        IndividualAddress brokenDevice = mock(IndividualAddress.class);
        when(brokenDevice.getRawAddress()).thenThrow(new IllegalStateException("made-up failure"));

        assertNull(lookup.groupAddressName(broken));
        assertNull(lookup.dataType(broken));
        assertNull(lookup.deviceName(brokenDevice));
    }
}
