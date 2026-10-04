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
package org.openhab.binding.modbus.internal;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeUID;

/**
 * @author Sami Salonen - Initial contribution
 */
@NonNullByDefault
public class ModbusChannelTypeProviderTest {

    @Test
    public void reusesOneStableGeneratedChannelTypeForEachAcceptedItemType() {
        ModbusChannelTypeProvider provider = new ModbusChannelTypeProvider();

        ChannelTypeUID numberUID = requireNonNull(provider.getGeneratedChannelTypeUID("Number"));
        ChannelTypeUID temperatureUID = requireNonNull(provider.getGeneratedChannelTypeUID("Number:Temperature"));
        ChannelTypeUID reusedNumberUID = requireNonNull(provider.getGeneratedChannelTypeUID("Number"));

        assertEquals(new ChannelTypeUID("modbus", "poller2-generated-number"), numberUID);
        assertEquals(new ChannelTypeUID("modbus", "poller2-generated-number-temperature"), temperatureUID);
        assertEquals(numberUID, reusedNumberUID);
        assertEquals(2, provider.getChannelTypes(null).size());

        ChannelType numberType = requireNonNull(provider.getChannelType(numberUID, null));
        assertSame(numberType, provider.getChannelType(reusedNumberUID, null));
        assertEquals("Number", numberType.getItemType());
    }
}
