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
package org.openhab.binding.freeathome.internal.type;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.freeathome.internal.util.UidUtils;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.test.storage.VolatileStorageService;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.StateDescription;
import org.openhab.core.types.StateDescriptionFragmentBuilder;

/**
 * Tests that a generated channel type outlives the provider that created it, so things restored from storage
 * resolve their channel types at boot, before the SysAP has answered.
 *
 * @author Martin Littkovsky - Initial contribution
 */
@NonNullByDefault
public class FreeAtHomeChannelTypeProviderImplTest {

    private final VolatileStorageService storageService = new VolatileStorageService();

    @Test
    public void addedChannelTypeSurvivesAProviderRestart() {
        FreeAtHomeChannelTypeProviderImpl provider = new FreeAtHomeChannelTypeProviderImpl(storageService);
        ChannelTypeUID uid = UidUtils.generateChannelTypeUID("boolean", true);
        provider.addChannelType(ChannelTypeBuilder.state(uid, "Window position", CoreItemFactory.SWITCH)
                .withStateDescriptionFragment(StateDescriptionFragmentBuilder.create().withReadOnly(true).build())
                .build());

        FreeAtHomeChannelTypeProviderImpl restartedProvider = new FreeAtHomeChannelTypeProviderImpl(storageService);
        ChannelType restoredType = restartedProvider.getChannelType(uid, null);

        assertNotNull(restoredType);
        assertEquals(uid, restoredType.getUID());
        assertEquals(CoreItemFactory.SWITCH, restoredType.getItemType());
        StateDescription state = restoredType.getState();
        assertNotNull(state);
        assertTrue(state.isReadOnly());
        assertTrue(restartedProvider.getChannelTypes(null).stream().anyMatch(type -> uid.equals(type.getUID())));
    }

    @Test
    public void restoredChannelTypeReferencesNoConfigDescription() {
        FreeAtHomeChannelTypeProviderImpl provider = new FreeAtHomeChannelTypeProviderImpl(storageService);
        ChannelTypeUID uid = UidUtils.generateChannelTypeUID("decimal", false);
        provider.addChannelType(ChannelTypeBuilder.state(uid, "Setpoint", CoreItemFactory.NUMBER).build());

        ChannelType restoredType = new FreeAtHomeChannelTypeProviderImpl(storageService).getChannelType(uid, null);

        assertNotNull(restoredType);
        assertNull(restoredType.getConfigDescriptionURI());
    }
}
