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
package org.openhab.persistence.dynamodb.internal;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.openhab.core.library.items.NumberItem;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.types.State;

/**
 *
 * @author Sami Salonen - Initial contribution
 *
 */
@NonNullByDefault
public class NumberItemIntegrationTest extends AbstractTwoItemIntegrationTest {

    public static final boolean LEGACY_MODE = false;
    private static final String NAME = "number";
    // On purpose we have super accurate number here (testing limits of aws)
    private static final DecimalType STATE1 = new DecimalType(new BigDecimal(
            "-32343243.193490838904389298049802398048923849032809483209483209482309840239840932840932849083094809483"));
    private static final DecimalType STATE2 = new DecimalType(600.9123);
    private static final DecimalType STATE_BETWEEN = new DecimalType(500);

    @SuppressWarnings("null")
    @BeforeAll
    public static void storeData() throws InterruptedException {
        NumberItem item = (NumberItem) ITEMS.get(NAME);

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

    /**
     * Use relaxed state comparison due to numerical rounding. See also DynamoDBBigDecimalItem.loseDigits
     */
    @Override
    protected void assertStateEquals(State expected, State actual) {
        BigDecimal expectedDecimal = ((DecimalType) expected).toBigDecimal();
        BigDecimal actualDecimal = ((DecimalType) actual).toBigDecimal();
        assertTrue(DynamoDBBigDecimalItem.loseDigits(expectedDecimal).compareTo(actualDecimal) == 0);
    }
}
