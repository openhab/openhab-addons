/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.ahawastecollection.internal.CollectionDate.WasteType;

/**
 * @author Sönke Küper - Initial contribution
 */
@NonNullByDefault
public final class AhaCollectionScheduleStub implements AhaCollectionSchedule {

    public static final Date GENERAL_WASTE_DATE = new GregorianCalendar(2021, 2, 19).getTime();
    public static final Date LEIGHTWEIGHT_PACKAGING_DATE = new GregorianCalendar(2021, 2, 20).getTime();
    public static final Date BIO_WASTE_DATE = new GregorianCalendar(2021, 2, 21).getTime();
    public static final Date PAPER_DATE = new GregorianCalendar(2021, 2, 22).getTime();

    @Override
    public Map<WasteType, CollectionDate> getCollectionDates() throws IOException {
        final Map<WasteType, CollectionDate> result = new LinkedHashMap<>(4);
        result.put(WasteType.GENERAL_WASTE,
                new CollectionDate(WasteType.GENERAL_WASTE, Arrays.asList(GENERAL_WASTE_DATE)));
        result.put(WasteType.LIGHT_PACKAGES,
                new CollectionDate(WasteType.GENERAL_WASTE, Arrays.asList(LEIGHTWEIGHT_PACKAGING_DATE)));
        result.put(WasteType.BIO_WASTE, new CollectionDate(WasteType.GENERAL_WASTE, Arrays.asList(BIO_WASTE_DATE)));
        result.put(WasteType.PAPER, new CollectionDate(WasteType.GENERAL_WASTE, Arrays.asList(PAPER_DATE)));
        return result;
    }
}
