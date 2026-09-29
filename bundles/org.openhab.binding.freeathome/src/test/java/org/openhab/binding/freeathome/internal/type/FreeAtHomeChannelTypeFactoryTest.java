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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.freeathome.internal.datamodel.FreeAtHomeDatapointGroup;
import org.openhab.binding.freeathome.internal.util.FreeAtHomeGeneralException;
import org.openhab.binding.freeathome.internal.util.PidTranslationUtils;
import org.openhab.binding.freeathome.internal.util.UidUtils;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.test.storage.VolatileStorageService;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.StateDescription;

/**
 * Tests that the channel type the binding generates for a datapoint group references no configuration description -
 * neither when built nor after the provider restored it from storage - because the binding registers none.
 *
 * @author Martin Littkovsky - Initial contribution
 */
@NonNullByDefault
public class FreeAtHomeChannelTypeFactoryTest {

    private static final String MEASURED_TEMPERATURE_LABEL = "pid-measured-temperature";
    private static final String WINDOW_POSITION_LABEL = "pid-window-door-position";

    private final VolatileStorageService storageService = new VolatileStorageService();

    @Test
    public void channelTypeBuiltFromADatapointGroupCarriesNoConfigDescription() throws FreeAtHomeGeneralException {
        FreeAtHomeDatapointGroup dpg = readOnlyDecimalDatapointGroup();
        ChannelTypeUID uid = UidUtils.generateChannelTypeUID(PidTranslationUtils.PID_VALUETYPE_DECIMAL, true);

        ChannelType channelType = FreeAtHomeChannelTypeFactory.createChannelType(dpg, uid);

        assertNull(channelType.getConfigDescriptionURI());
        assertEquals(uid, channelType.getUID());
        assertEquals(MEASURED_TEMPERATURE_LABEL + "-" + CoreItemFactory.NUMBER + "-"
                + PidTranslationUtils.CATEGORY_TEMPERATURE + "-type", channelType.getLabel());
        assertEquals(CoreItemFactory.NUMBER, channelType.getItemType());
        assertEquals(PidTranslationUtils.CATEGORY_TEMPERATURE, channelType.getCategory());
        StateDescription state = channelType.getState();
        assertNotNull(state);
        assertTrue(state.isReadOnly());
    }

    @Test
    public void channelTypeBuiltFromADatapointGroupSurvivesTheProviderStorageWithoutConfigDescription()
            throws FreeAtHomeGeneralException {
        FreeAtHomeDatapointGroup dpg = writableBooleanDatapointGroup();
        ChannelTypeUID uid = UidUtils.generateChannelTypeUID(PidTranslationUtils.PID_VALUETYPE_BOOLEAN, false);
        ChannelType channelType = FreeAtHomeChannelTypeFactory.createChannelType(dpg, uid);

        new FreeAtHomeChannelTypeProviderImpl(storageService).addChannelType(channelType);
        ChannelType restoredType = new FreeAtHomeChannelTypeProviderImpl(storageService).getChannelType(uid, null);

        assertNotNull(restoredType);
        assertNull(restoredType.getConfigDescriptionURI());
        assertEquals(uid, restoredType.getUID());
        assertEquals(CoreItemFactory.SWITCH, restoredType.getItemType());
    }

    private FreeAtHomeDatapointGroup readOnlyDecimalDatapointGroup() throws FreeAtHomeGeneralException {
        FreeAtHomeDatapointGroup dpg = mock(FreeAtHomeDatapointGroup.class);
        when(dpg.isReadOnly()).thenReturn(true);
        when(dpg.getTypePattern()).thenReturn("%.1f");
        when(dpg.isDecimal()).thenReturn(true);
        when(dpg.getMin()).thenReturn(7);
        when(dpg.getMax()).thenReturn(30);
        when(dpg.getLabel()).thenReturn(MEASURED_TEMPERATURE_LABEL);
        when(dpg.getOpenHabItemType()).thenReturn(CoreItemFactory.NUMBER);
        when(dpg.getOpenHabCategory()).thenReturn(PidTranslationUtils.CATEGORY_TEMPERATURE);
        return dpg;
    }

    private FreeAtHomeDatapointGroup writableBooleanDatapointGroup() throws FreeAtHomeGeneralException {
        FreeAtHomeDatapointGroup dpg = mock(FreeAtHomeDatapointGroup.class);
        when(dpg.isReadOnly()).thenReturn(false);
        when(dpg.getTypePattern()).thenReturn("");
        when(dpg.isDecimal()).thenReturn(false);
        when(dpg.isInteger()).thenReturn(false);
        when(dpg.getLabel()).thenReturn(WINDOW_POSITION_LABEL);
        when(dpg.getOpenHabItemType()).thenReturn(CoreItemFactory.SWITCH);
        when(dpg.getOpenHabCategory()).thenReturn(PidTranslationUtils.CATEGORY_CONTACT);
        return dpg;
    }
}
