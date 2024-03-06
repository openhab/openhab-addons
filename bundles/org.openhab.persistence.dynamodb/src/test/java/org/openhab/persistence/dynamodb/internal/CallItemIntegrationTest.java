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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.ZonedDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.openhab.core.library.items.CallItem;
import org.openhab.core.library.types.StringListType;
import org.openhab.core.types.State;

/**
 *
 * @author Sami Salonen - Initial contribution
 *
 */
@NonNullByDefault
public class CallItemIntegrationTest extends AbstractTwoItemIntegrationTest {

    public static final boolean LEGACY_MODE = false;

    private static final String NAME = "call";
    // values are encoded as part1,part2 - ordering goes wrt strings
    private static final StringListType STATE1 = new StringListType("part1", "foo");
    private static final StringListType STATE2 = new StringListType("part3", "bar");
    private static final StringListType STATE_BETWEEN = new StringListType("part2", "zzz");

    @SuppressWarnings("null")
    @BeforeAll
    public static void storeData() throws InterruptedException {
        CallItem item = (CallItem) ITEMS.get(NAME);
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
        return STATE1;
    }

    @Override
    protected State getSecondItemState() {
        return STATE2;
    }

    @Override
    protected @Nullable State getQueryItemStateBetween() {
        return STATE_BETWEEN;
    }

    @Override
    protected void assertStateEquals(State expected, State actual) {
        // Since CallType.equals is broken, toString is used as workaround
        assertEquals(expected.toString(), actual.toString());
    }
}
