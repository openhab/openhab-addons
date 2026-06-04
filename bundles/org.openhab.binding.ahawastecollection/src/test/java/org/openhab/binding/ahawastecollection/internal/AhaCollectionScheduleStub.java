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
package org.openhab.binding.ahawastecollection.internal;

import static java.util.Map.entry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.ahawastecollection.internal.CollectionDate.WasteType;

/**
 * @author Sönke Küper - Initial contribution
 */
@NonNullByDefault
public final class AhaCollectionScheduleStub implements AhaCollectionSchedule {

    public static final Date GENERAL_WASTE_DATE = new GregorianCalendar(2021, 2, 19).getTime();
    public static final Date LIGHTWEIGHT_PACKAGING_DATE = new GregorianCalendar(2021, 2, 20).getTime();
    public static final Date BIO_WASTE_DATE = new GregorianCalendar(2021, 2, 21).getTime();
    public static final Date PAPER_DATE = new GregorianCalendar(2021, 2, 22).getTime();
    public static final Date CHRISTMAS_TREE_DATE = new GregorianCalendar(2022, 1, 6).getTime();

    @Override
    public Map<WasteType, CollectionDate> getCollectionDates() throws IOException {
        return Map.ofEntries(
                entry(WasteType.GENERAL_WASTE,
                        new CollectionDate(WasteType.GENERAL_WASTE, new ArrayList<>(List.of(GENERAL_WASTE_DATE)))),
                entry(WasteType.LIGHT_PACKAGES,
                        new CollectionDate(WasteType.LIGHT_PACKAGES,
                                new ArrayList<>(List.of(LIGHTWEIGHT_PACKAGING_DATE)))),
                entry(WasteType.BIO_WASTE,
                        new CollectionDate(WasteType.BIO_WASTE, new ArrayList<>(List.of(BIO_WASTE_DATE)))),
                entry(WasteType.PAPER, new CollectionDate(WasteType.PAPER, new ArrayList<>(List.of(PAPER_DATE)))),
                entry(WasteType.CHRISTMAS_TREES,
                        new CollectionDate(WasteType.CHRISTMAS_TREES, new ArrayList<>(List.of(CHRISTMAS_TREE_DATE)))));
    }
}
