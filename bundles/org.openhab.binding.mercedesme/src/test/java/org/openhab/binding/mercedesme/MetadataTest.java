/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.mercedesme;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.mercedesme.internal.Constants;
import org.openhab.binding.mercedesme.internal.MercedesMeMetadataAdjuster;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.link.ItemChannelLink;
import org.openhab.core.thing.link.ItemChannelLinkRegistry;

/**
 * {@link MetadataTest} testing updates in item metadata changes
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
class MetadataTest {

    @Test
    public void testMetadataUpdate() {
        MetadataRegistryMock mdrm = new MetadataRegistryMock();
        LocaleProvider localeMock = mock(LocaleProvider.class);
        when(localeMock.getLocale()).thenReturn(Locale.GERMANY);

        ThingUID tuid = new ThingUID(Constants.BINDING_ID, Constants.BEV);
        MercedesMeMetadataAdjuster mmma = new MercedesMeMetadataAdjuster(mdrm, mock(ItemChannelLinkRegistry.class),
                localeMock);
        mmma.added(new ItemChannelLink("Soc_Test_Item", new ChannelUID(tuid, Constants.GROUP_RANGE, "soc")));
        assertEquals("%", mdrm.getAll().iterator().next().getValue(), "Percent Unit");

        mdrm = new MetadataRegistryMock();
        mmma = new MercedesMeMetadataAdjuster(mdrm, mock(ItemChannelLinkRegistry.class), localeMock);
        mmma.added(new ItemChannelLink("Mileage_Test_Item", new ChannelUID(tuid, Constants.GROUP_RANGE, "mileage")));
        assertEquals("km", mdrm.getAll().iterator().next().getValue(), "Kilometer Unit");

        mdrm = new MetadataRegistryMock();
        mmma = new MercedesMeMetadataAdjuster(mdrm, mock(ItemChannelLinkRegistry.class), localeMock);
        mmma.added(new ItemChannelLink("Mileage_Test_Item",
                new ChannelUID(tuid, Constants.GROUP_TIRES, "pressure-front-right")));
        assertEquals("bar", mdrm.getAll().iterator().next().getValue(), "Prressure Unit");
    }

    @Test
    public void testImperialMetadataUpdate() {
        MetadataRegistryMock mdrm = new MetadataRegistryMock();
        LocaleProvider localeMock = mock(LocaleProvider.class);
        when(localeMock.getLocale()).thenReturn(Locale.US);

        ThingUID tuid = new ThingUID(Constants.BINDING_ID, Constants.BEV);
        MercedesMeMetadataAdjuster mmma = new MercedesMeMetadataAdjuster(mdrm, mock(ItemChannelLinkRegistry.class),
                localeMock);
        mmma.added(new ItemChannelLink("Soc_Test_Item", new ChannelUID(tuid, Constants.GROUP_RANGE, "soc")));
        assertEquals("%", mdrm.getAll().iterator().next().getValue(), "Percent Unit");

        mdrm = new MetadataRegistryMock();
        mmma = new MercedesMeMetadataAdjuster(mdrm, mock(ItemChannelLinkRegistry.class), localeMock);
        mmma.added(new ItemChannelLink("Mileage_Test_Item", new ChannelUID(tuid, Constants.GROUP_RANGE, "mileage")));
        assertEquals("mi", mdrm.getAll().iterator().next().getValue(), "Mile Unit");

        mdrm = new MetadataRegistryMock();
        mmma = new MercedesMeMetadataAdjuster(mdrm, mock(ItemChannelLinkRegistry.class), localeMock);
        mmma.added(new ItemChannelLink("Mileage_Test_Item",
                new ChannelUID(tuid, Constants.GROUP_TIRES, "pressure-rear-right")));
        assertEquals("psi", mdrm.getAll().iterator().next().getValue(), "Prressure Unit");
    }
}
