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
package org.openhab.persistence.dynamodb.internal;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.openhab.core.library.items.DateTimeItem;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.types.State;

/**
 *
 * @author Sami Salonen - Initial contribution
 *
 */
@NonNullByDefault
public class DateTimeItemIntegrationTest extends AbstractTwoItemIntegrationTest {
    public static final boolean LEGACY_MODE = false;

    private static final String NAME = "datetime";
    private static final ZonedDateTime ZDT1 = ZonedDateTime.parse("2016-06-15T10:00:00Z");
    private static final ZonedDateTime ZDT2 = ZonedDateTime.parse("2016-06-15T16:00:00.123Z");
    private static final ZonedDateTime ZDT_BETWEEN = ZonedDateTime.parse("2016-06-15T14:00:00Z");

    // State1 stored as DateTimeType wrapping ZonedDateTime specified in UTC
    private static final DateTimeType STATE1 = new DateTimeType(ZDT1);
    // State2 stored as DateTimeType wrapping ZonedDateTime specified in UTC+5
    private static final DateTimeType STATE2 = new DateTimeType(ZDT2.withZoneSameInstant(ZoneOffset.ofHours(5)));
    private static final DateTimeType STATE_BETWEEN = new DateTimeType(ZDT_BETWEEN);

    @SuppressWarnings("null")
    @BeforeAll
    public static void storeData() throws InterruptedException {
        DateTimeItem item = (DateTimeItem) ITEMS.get(NAME);

        item.setState(STATE1);

        beforeStore = ZonedDateTime.now();
        Thread.sleep(10);
        service.store(item);
        afterStore1 = ZonedDateTime.now();
        Thread.sleep(10);
        item.setState(STATE2);
        service.store(item);
        Thread.sleep(10);
        afterStore2 = ZonedDateTime.now();
        LOGGER.info("Created item between {} and {}", AbstractDynamoDBItem.DATEFORMATTER.format(beforeStore),
                AbstractDynamoDBItem.DATEFORMATTER.format(afterStore1));
    }

    @Override
    protected String getItemName() {
        return NAME;
    }

    @Override
    protected State getFirstItemState() {
        // The persistence converts to system default timezone
        // Thus we need to convert here as well for comparison
        // In the logs:
        // [main] TRACE org.openhab.persistence.dynamodb.internal.DynamoDBPersistenceService - Dynamo item datetime
        // (Type=DateTimeItem, State=2016-06-15T16:00:00.123+0000, Label=null, Category=null) converted to historic
        // item: datetime: 2020-11-28T11:29:54.326Z: 2016-06-15T19:00:00.123+0300
        return STATE1.toZone(ZoneId.systemDefault());
    }

    @Override
    protected State getSecondItemState() {
        // The persistence converts to system default timezone
        // Thus we need to convert here as well for comparison
        // In the logs:
        // [main] TRACE org.openhab.persistence.dynamodb.internal.DynamoDBPersistenceService - Dynamo item datetime
        // (Type=DateTimeItem, State=2016-06-15T16:00:00.123+0000, Label=null, Category=null) converted to historic
        // item: datetime: 2020-11-28T11:29:54.326Z: 2016-06-15T19:00:00.123+0300
        return STATE2.toZone(ZoneId.systemDefault());
    }

    @Override
    protected @Nullable State getQueryItemStateBetween() {
        return STATE_BETWEEN;
    }
}
